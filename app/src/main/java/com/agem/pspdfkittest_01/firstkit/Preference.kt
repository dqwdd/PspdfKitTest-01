/*
 *   Copyright © 2021-2023 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file.
 */

package com.agem.pspdfkittest_01.firstkit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.clearAppData
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.clearCache
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableFormEditing
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableImmersiveMode
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableLeakCanary
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableMultithreadingRendering
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableTextSelection
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.enableVolumeButtonNavigation
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.grayscale
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.invertPageColors
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.showPrintAction
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.showShareAction
import com.agem.pspdfkittest_01.firstkit.CatalogPreferences.startPage
import com.agem.pspdfkittest_01_application.PreferenceKeys
import java.util.*

sealed class Preference<T>(
    val title: String,
    val key: Preferences.Key<T>,
    val description: String = ""
)

class RadioPreference(
    title: String,
    key: Preferences.Key<String>,
    val possibleValuesResource: Int,
    val isInline: Boolean = false
) : Preference<String>(title, key)

class CheckboxPreference(
    title: String,
    key: Preferences.Key<Boolean>
) : Preference<Boolean>(title, key)

class ButtonPreference(
    title: String,
    key: Preferences.Key<String>,
    description: String = ""
) : Preference<String>(title, key, description)

class IntegerPreference(
    title: String,
    key: Preferences.Key<Int>,
    description: String = ""
) : Preference<Int>(title, key, description)

class PreferencesSection(
    val title: String,
    vararg preferences: Preference<*>
) : ArrayList<Preference<*>>() {
    init {
        Collections.addAll(this, *preferences)
    }
}

/**
 * Used for the main preferences layout.
 */
fun preferenceSections(context: Context): List<PreferencesSection> =
    listOf(
        elements = arrayOf(
            PreferencesSection(
                title = context.getString(R.string.preference_section_forms),
                preferences = arrayOf(
                    enableFormEditing(context)
                )
            ),

            PreferencesSection(
                title = context.getString(R.string.preference_section_actions),
                preferences = arrayOf(
                    showShareAction(context),
                    showPrintAction(context)
                )
            ),

            PreferencesSection(
                title = context.getString(R.string.preference_section_customization),
                preferences = arrayOf(
                    invertPageColors(context),
                    grayscale(context),
                    enableTextSelection(context),
                    enableVolumeButtonNavigation(context)
                )
            ),

            PreferencesSection(
                title = context.getString(R.string.preference_section_activity),
                preferences = arrayOf(
                    enableImmersiveMode(context)
                )
            ),

            PreferencesSection(
                title = context.getString(R.string.preference_section_other),
                preferences = arrayOf(
                    startPage(context),
                    enableMultithreadingRendering(context),
                    enableLeakCanary(context),
                    clearCache(context),
                    clearAppData(context)
                )
            )
        )
    )

/** Catalog-wide helper for accessing the shared preferences. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object CatalogPreferences {
    fun scrollContinuously(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_scroll_continuously),
        key = PreferenceKeys.PageScrollContinuous
    )

    fun fitPageToWidth(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_fit_page_to_width),
        key = PreferenceKeys.FitPageToWidth
    )

    fun restoreLastViewedPage(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_restore_last_viewed_page),
        key = PreferenceKeys.RestoreLastViewedPage
    )

    fun showPageNumberOverlay(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_page_number_overlay),
        key = PreferenceKeys.ShowPageNumberOverlay
    )

    fun showPageLabels(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_page_labels),
        key = PreferenceKeys.ShowPageLabels
    )

    fun hideUiWhenCreatingAnnotations(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_hide_ui_when_creating_annotations),
        key = PreferenceKeys.HideUiWhenCreatingAnnotations
    )

    fun displayFirstPageAsSingle(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_display_first_page_as_single),
        key = PreferenceKeys.FirstPageAsSingle
    )

    fun showGapsBetweenPages(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_gaps_between_pages),
        key = PreferenceKeys.ShowGapBetweenPages
    )

    fun showSearchAction(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_search_action),
        key = PreferenceKeys.ShowSearchAction
    )

    fun inlineSearch(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_inline_search),
        key = PreferenceKeys.InlineSearch
    )

    fun showThumbnailGrid(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_thumbnail_grid),
        key = PreferenceKeys.ShowThumbnailGridAction
    )

    fun enableDocumentOutline(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_document_outline),
        key = PreferenceKeys.EnableDocumentOutline
    )

    fun enableAnnotationList(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_annotation_list),
        key = PreferenceKeys.ShowAnnotationListAction
    )

    fun enableAnnotationEditing(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_annotation_editing),
        key = PreferenceKeys.EnableAnnotationEditing
    )

    fun enableAnnotationRotation(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_annotation_rotation),
        key = PreferenceKeys.EnableAnnotationRotation
    )

    fun enableFormEditing(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_annotation_editing),
        key = PreferenceKeys.EnableFormEditing
    )

    fun showShareAction(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_share_action),
        key = PreferenceKeys.ShowShareAction
    )

    fun showPrintAction(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_show_print_action),
        key = PreferenceKeys.ShowPrintAction
    )

    fun invertPageColors(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_invert_page_colors),
        key = PreferenceKeys.InvertColors
    )

    fun grayscale(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_grayscale),
        key = PreferenceKeys.Grayscale
    )

    fun enableTextSelection(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_text_selection),
        key = PreferenceKeys.EnableTextSelection
    )

    fun enableVolumeButtonNavigation(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_volume_buttons_navigation),
        key = PreferenceKeys.EnableVolumeButtonsNavigation
    )

    fun enableImmersiveMode(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_immersive_mode),
        key = PreferenceKeys.ImmersiveMode
    )

    fun startPage(context: Context) = IntegerPreference(
        title = context.getString(R.string.integer_preference_start_page),
        key = PreferenceKeys.StartPage,
        description = context.getString(R.string.integer_preference_start_page_description)
    )

    fun enableMultithreadingRendering(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_multithreaded_rendering),
        key = PreferenceKeys.MultiThreadedRendering
    )

    fun enableLeakCanary(context: Context) = CheckboxPreference(
        title = context.getString(R.string.checkbox_preference_enable_leakcanary),
        key = PreferenceKeys.LeakCanaryEnabled
    )

    fun clearCache(context: Context) = ButtonPreference(
        title = context.getString(R.string.button_preference_clear_cache),
        key = PreferenceKeys.ClearCache
    )

    fun clearAppData(context: Context) = ButtonPreference(
        title = context.getString(R.string.button_preference_clear_app_data),
        key = PreferenceKeys.ClearAppData,
        description = context.getString(R.string.button_preference_clear_app_data_description)
    )

}