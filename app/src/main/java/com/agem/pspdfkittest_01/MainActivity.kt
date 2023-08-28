package com.agem.pspdfkittest_01

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.agem.pspdfkittest_01.databinding.ActivityMainBinding
import com.agem.pspdfkittest_01.firstkit.dataStore
import com.agem.pspdfkittest_01.testActivity.ATestActivity
import com.agem.pspdfkittest_01.testActivity.BTestActivity
import com.agem.pspdfkittest_01.testActivity.CTestActivity
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.ui.PdfActivityIntentBuilder

class MainActivity : BaseBindingActivity<ActivityMainBinding>(
    R.layout.activity_main) {

    private val viewModel: CatalogViewModel by viewModels {
        CatalogViewModel.Factory(application, dataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.vm = viewModel
        setValues()
    }

    override fun setValues() {
        super.setValues()

        binding.tvText.setOnClickListener {
//            val documentUri = Uri.parse("file:///android_asset/document.pdf")
//            val intent = PdfActivityIntentBuilder.fromUri(this, documentUri).build()
//            startActivity(intent)

            val intent = Intent(this, ExternalExampleActivity::class.java)
            intent.putExtra(ExternalExampleActivity.EXTRA_CONFIGURATION, getPdfActivityConfigurationBuilder().build())
            startActivity(intent)
        }

        binding.tvATestActivity.setOnClickListener {
//            val uri: Uri = "content://com.android.providers.media.documents/document/document%3A2380"

            if (Utils.hasExternalStorageRwPermission(this)) {
                showOpenFileDialog()
            } else {
                showPermissionExplanationDialog()
            }
        }

        binding.tvBTestActivity.setOnClickListener {
            val intent = Intent(this, BTestActivity::class.java)
            startActivity(intent)
        }

        binding.tvCTestActivity.setOnClickListener {
            val intent = Intent(this, CTestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getPdfActivityConfigurationBuilder() = viewModel.state.value.getPdfActivityConfigurationBuilder(this)

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.externalDocumentExamplePermissionExplanation)
            .setCancelable(false)
            .setPositiveButton(R.string.grantAccess) { _, _ ->
                if (Utils.requestExternalStorageRwPermission(this, ExternalExampleActivity.REQUEST_ASK_FOR_PERMISSION)) {
                    showOpenFileDialog()
                }
            }
            .setNegativeButton(R.string.continueWithout) { dialog, _ ->
                dialog.cancel()
                showOpenFileDialog()
            }
            .show()
    }

    private fun showOpenFileDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        requestPDF.launch(intent)
    }

    @SuppressLint("ResourceType")
    private val requestPDF = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { uri ->

            val config = PdfActivityConfiguration.Builder(this)
                .layout(com.pspdfkit.R.layout.pspdf__pdf_activity)
                .build()
            // val config = PdfConfiguration.Builder().build()

            val intent = PdfActivityIntentBuilder.fromUri(this, uri)
                .configuration(config)
                .activityClass(ATestActivity::class.java)
                .build()

            startActivity(intent)
        }
    }
}