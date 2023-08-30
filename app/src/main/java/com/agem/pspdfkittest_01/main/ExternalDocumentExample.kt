package com.agem.pspdfkittest_01.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.basesupplies.Utils
import com.pspdfkit.PSPDFKit
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.document.ImageDocumentLoader
import com.pspdfkit.document.ImageDocumentUtils
import com.pspdfkit.document.download.DownloadJob
import com.pspdfkit.document.download.DownloadProgressFragment
import com.pspdfkit.document.download.DownloadRequest
import com.pspdfkit.ui.PdfActivityIntentBuilder
import com.pspdfkit.utils.getSupportParcelableExtra
import java.io.File

class ExternalExampleActivity : FragmentActivity() {

    private lateinit var configuration: PdfActivityConfiguration

    private var waitingForResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract the configuration for displaying the viewer activity.
        configuration = intent.getSupportParcelableExtra(EXTRA_CONFIGURATION, PdfActivityConfiguration::class.java)
            ?: throw ExceptionInInitializerError(
                ExternalExampleActivity::class.java.simpleName + " was started without a PdfActivityConfiguration.")

        // Check if the activity was recreated, and see if the user already started document picking.
        waitingForResult = savedInstanceState?.getBoolean(IS_WAITING_FOR_RESULT, false) ?: false

        // Prevent the example from requesting multiple documents at the same time.
        if (!waitingForResult) {
            waitingForResult = true
            // On Android 6.0+ we ask for SD card access permission. This isn't strictly necessary, but PSPDFKit
            // being able to access file directly will significantly improve performance.
            // Since documents can be annotated we ask for write permission as well.
            if (Utils.hasExternalStorageRwPermission(this)) {
                showOpenFileDialog()
            } else {
                showPermissionExplanationDialog()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OPEN_DOCUMENT) {
            waitingForResult = false
            if (resultCode == Activity.RESULT_OK && data != null) {
                val uri = data.data ?: return

                val isImageFile = ImageDocumentUtils.isImageUri(this, uri)

                // Some URIs can be opened directly, including local filesystem, app assets, and content provider URIs.
                if (PSPDFKit.isOpenableUri(this, uri)) {
                    startActivity(createActivityIntent(uri, configuration, isImageFile))
                    finish()
                } else {
                    // Find the DownloadProgressFragment for showing download progress, or create a new one.
                    val downloadFragment = supportFragmentManager.findFragmentByTag(
                        DOWNLOAD_PROGRESS_FRAGMENT
                    ) as DownloadProgressFragment? ?: run {
                        DownloadProgressFragment().apply {
                            val request = DownloadRequest.Builder(this@ExternalExampleActivity).uri(uri).build()
                            this.job = DownloadJob.startDownload(request)
                            show(supportFragmentManager, DOWNLOAD_PROGRESS_FRAGMENT)
                        }
                    }

                    // Once the download is complete we launch the PdfActivity from the downloaded file.
                    downloadFragment.job.setProgressListener(object : DownloadJob.ProgressListenerAdapter() {
                        override fun onComplete(output: File) {
                            startActivity(createActivityIntent(Uri.fromFile(output), configuration, isImageFile))
                            finish()
                        }
                    })
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // If the user cancelled document selection, we just close the activity.
                finish()
            }
        }
    }

    private fun createActivityIntent(uri: Uri, baseConfiguration: PdfActivityConfiguration, isImageFile: Boolean): Intent {
        val pdfActivityIntentBuilder: PdfActivityIntentBuilder
        var configuration = baseConfiguration

        if (isImageFile) {
            pdfActivityIntentBuilder = PdfActivityIntentBuilder.fromImageUri(this, uri)
            // Get the default image document configuration.
            // Default options in this configuration are specifically thought to enhance the user
            // experience for image documents (e.g. thumbnail bar and page number overlay are hidden).
            configuration = ImageDocumentLoader.getDefaultImageDocumentActivityConfiguration(configuration)
        } else {
            pdfActivityIntentBuilder = PdfActivityIntentBuilder.fromUri(this, uri)
        }

        return pdfActivityIntentBuilder.configuration(configuration).build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Retain if we are currently waiting for an intent to return, so we don't set it off twice by accident.
        outState.putBoolean(IS_WAITING_FOR_RESULT, waitingForResult)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ASK_FOR_PERMISSION) {
            // We attempt to open document after permissions have been requested.
            // PSPDFKit can open documents without the permissions when SAF is used, however the access
            // without permissions will be significantly slower.
            showOpenFileDialog()
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.externalDocumentExamplePermissionExplanation)
            .setCancelable(false)
            .setPositiveButton(R.string.grantAccess) { _, _ ->
                if (Utils.requestExternalStorageRwPermission(this, REQUEST_ASK_FOR_PERMISSION)) {
                    showOpenFileDialog()
                }
            }
            .setNegativeButton(R.string.continueWithout) { dialog, _ ->
                dialog.cancel()
                showOpenFileDialog()
            }
            .show()
    }

    // We're temporarily suppressing the warning for startActivityForResult being deprecated.
    // Issue: https://github.com/PSPDFKit/PSPDFKit/issues/31881
    @Suppress("DEPRECATION")
    private fun showOpenFileDialog() {
        // Prepare an implicit intent which allows the user to select any document.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"

        // We accept PDF files and images (for image documents).
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))

        // Set of the intent for result, so we can retrieve the Uri of the selected document.
        startActivityForResult(intent, REQUEST_OPEN_DOCUMENT)
    }

    companion object {
        internal const val EXTRA_CONFIGURATION = "PSPDFKit.ExternalExampleActivity.configuration"

        private const val REQUEST_OPEN_DOCUMENT = 1
        const val REQUEST_ASK_FOR_PERMISSION = 2
        private const val IS_WAITING_FOR_RESULT = "PSPDFKit.ExternalExampleActivity.waitingForResult"
        private const val DOWNLOAD_PROGRESS_FRAGMENT = "DownloadProgressFragment"
    }
}