package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.base.BaseBindingActivity
import com.agem.pspdfkittest_01.databinding.ActivityDtestBinding
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.document.editor.PdfDocumentEditorFactory
import com.pspdfkit.ui.PdfActivity.showDocument
import com.pspdfkit.ui.PdfFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

/**
 * 이 Activity 는 화면 Link 기능을 테스트하는 Activity
 * 스콘에는 있지만 pspdfkit 에는 이 기능이 없는 것 같다
 * 그래서 movePage 메서드로 그냥 페이지만 이동해야 할 듯?
 * (link 를 걸었던 위치로 이동 및 zoom 설정하기를 같이 해도 될 듯)
 * */

class DTestActivity : BaseBindingActivity<ActivityDtestBinding>(
    R.layout.activity_dtest) {

    private lateinit var mFragment: PdfFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvMovePage.setOnClickListener {
            movePages()
        }

        binding.tvOpenDocument.setOnClickListener {
            showOpenFileDialog()
        }
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
            mFragment = supportFragmentManager.findFragmentById(R.id.flDFragment) as PdfFragment?
                ?: run {
                    val newFragment = PdfFragment.newInstance(uri, PdfConfiguration.Builder().build())
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.flDFragment, newFragment)
                        .commit()

                    return@run newFragment
                }
        }
    }

    private fun movePages() {
        // Use the [`PdfDocumentEditorFactory`][] to create a `PdfDocumentEditor`.
        val documentEditor = mFragment.document?.let { PdfDocumentEditorFactory.createForDocument(it) }

        // Set up the output file location.
        val outputFile = File(filesDir, "outputDocument.pdf")

        // Move the current page.
        Log.e("tetest", "21512")
        Log.e("tetest", "mFragment.pageIndex == ${mFragment.pageIndex}")
        val pagesToMove = setOf(mFragment.pageIndex)

        // Make sure `disposable` is managed by the activity and destroyed correctly
        // when the activity is destroyed.
        val disposable = mFragment.document?.let {
            Log.e("tetest", "1333353")
            // 관련이 있는지 보고 아님 말고
            Log.e("tetest", "documentEditor?.document?.bookmarkProvider == ${documentEditor?.document?.bookmarkProvider?.bookmarks}")
            Log.e("tetest", "documentEditor?.document?.checkPointer == ${documentEditor?.document?.checkpointer?.isSaving}")
            documentEditor?.movePages(pagesToMove, it.pageCount)
                ?.flatMapCompletable { documentEditor.saveDocument(mContext, outputFile.outputStream(), null) }
                // Use `subscribeOn` to put `saveDocument` on a background thread, as it can be slow.
                ?.subscribeOn(Schedulers.io())
                // Make sure the resulting document rendering goes back on the main thread.
                ?.observeOn(AndroidSchedulers.mainThread())
                // You can display the saved document in a new activity with the following action on `subscribe`.
                ?.subscribe { showDocument(mContext, Uri.fromFile(outputFile), null) }
            }

        disposable?.dispose()
    }
}