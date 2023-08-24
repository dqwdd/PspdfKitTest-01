package com.agem.pspdfkittest_01

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.agem.pspdfkittest_01.firstkit.preferenceSections
import com.agem.pspdfkittest_01.firstkit.getSectionsWithExamples
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Centralized ViewModel for the whole Catalog application.
 * The State within it serves as our single source of truth for anything state related.
 * This ViewModel should be the only way to access and mutate that state.
 */
class ExternalDocumentExample(context: Context) :
    PSPDFExample(context, R.string.externalDocumentExampleTitle, R.string.externalDocumentExampleDescription) {
    override fun launchExample(context: Context, configuration: PdfActivityConfiguration.Builder) {
        val intent = Intent(context, ExternalExampleActivity::class.java)
        intent.putExtra(ExternalExampleActivity.EXTRA_CONFIGURATION, configuration.build())
        context.startActivity(intent)
    }
}

class CatalogViewModel(
    application: Application,
    private val dataStore: DataStore<Preferences>
) : AndroidViewModel(application) {


    class Factory(private val application: Application, private val dataStore: DataStore<Preferences>) :
        ViewModelProvider.AndroidViewModelFactory(application) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return CatalogViewModel(application, dataStore) as T
        }
    }


    // Note that the MutableStateFlow is private. We only expose an immutable version for Composables to observe.
    private val mutableState = MutableStateFlow(State())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val examples = getSectionsWithExamples(application.applicationContext)
            val preferencesSections = preferenceSections(application.applicationContext)
            val storedPreferences = dataStore.data.first()
            val preferences = state.value.preferences.map { (preferenceKey, defaultValue) ->
                val entry = storedPreferences[preferenceKey] ?: defaultValue
                preferenceKey to entry
            }.toMap()

            mutableState.mutate {
                copy(
                    preferences = preferences,
                    examples = examples,
                    preferenceSections = preferencesSections,
                    expandedExampleSectionTitles = setOf(examples.first().name),
                    expandedPreferenceSectionTitles = setOf(preferencesSections.first().title)
                )
            }
        }
    }

    private fun <T> MutableStateFlow<T>.mutate(mutateFunction: T.() -> T) {
        value = value.mutateFunction()
    }
}
