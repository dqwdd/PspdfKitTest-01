package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.base.BaseBindingActivity
import com.agem.pspdfkittest_01.databinding.ActivityCtestBinding
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.annotations.InkAnnotation
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.listeners.OnDocumentLongPressListener
import com.pspdfkit.listeners.SimpleDocumentListener
import com.pspdfkit.ui.PdfFragment
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import kotlin.math.min

/**
 * 이 Activity 는 화면 Split 하게 나오는 거 테스트하는 Activity
 * */

class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(R.layout.activity_ctest),
    DocumentListener, OnDocumentLongPressListener {

    private val viewModel: CTestViewModel by viewModels()

    private lateinit var mFragment: PdfFragment
    private var mBitmap: Bitmap? = null

    // 주석 값 저장하는 것들
    private val documentAnnotations = mutableListOf<Annotation>()
    private var currentAnnotation: Annotation? = null
    private var annotationLoadingDisposable: Disposable? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEvent()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ClickableViewAccessibility")
    override fun setupEvent() {
        super.setupEvent()

        binding.tvGetDocument.setOnClickListener {
            showOpenFileDialog()
        }

        binding.mainDividerImg.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {}
                MotionEvent.ACTION_MOVE -> {
                    val params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
                    params.guidePercent = motionEvent.rawY / binding.clSplit.height
                    binding.guideline.layoutParams = params
                }
                MotionEvent.ACTION_UP -> {
                    val location = IntArray(2)
                    binding.guideline.getLocationInSurface(location)
                }

                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }

        binding.btnGetAnnotationLocateData.setOnClickListener {
            getAnnotationLocateData()
        }
        binding.btnRenderAnnotation.setOnClickListener {
            renderAnnotation()
        }
    }

    private fun showOpenFileDialog() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        requestPDF.launch(intent)
    }
    // https://pspdfkit.com/guides/android/customizing-the-interface/using-toolbars-within-fragment/

    @SuppressLint("ResourceType")
    private val requestPDF = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { uri ->
            mFragment = supportFragmentManager.findFragmentById(R.id.flToCFragment) as PdfFragment?
                ?: run {
                    val newFragment = PdfFragment.newInstance(
                        uri,
                        PdfConfiguration.Builder().scrollDirection(PageScrollDirection.HORIZONTAL).build()
                    )
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.flToCFragment, newFragment)
                        .commit()

                    return@run newFragment
                }

            mFragment.apply {
                setOnDocumentLongPressListener(this@CTestActivity)
                addDocumentListener(object : SimpleDocumentListener() {
                    @UiThread
                    override fun onDocumentLoaded(loadedDocument: PdfDocument) {
                        annotationLoadingDisposable = loadedDocument.annotationProvider
                            .getAllAnnotationsOfTypeAsync(EnumSet.allOf(AnnotationType::class.java))
                            .toList()
                            .subscribe { annotations ->
                                documentAnnotations.addAll(annotations)
                                getAnnotationLocateData()
                            }
                    }

                    override fun onDocumentLoadFailed(exception: Throwable) {
                        Log.e("tetest", "onDocumentLoadFailed 222255543444")
                    }
                })
            }
        }
    }

    private fun getAnnotationLocateData() {
        if (documentAnnotations.isEmpty()) return

        var currentAnnotation = this.currentAnnotation
        val currentAnnotationIndex = if (currentAnnotation == null) {
            -1
        } else {
            documentAnnotations.indexOf(currentAnnotation)
        }
        val nextAnnotationIndex = min(currentAnnotationIndex + 1, documentAnnotations.size - 1)

        if (nextAnnotationIndex != currentAnnotationIndex) {
            currentAnnotation = documentAnnotations[nextAnnotationIndex]
            this.currentAnnotation = currentAnnotation

            val boundingBox = currentAnnotation.boundingBox
            boundingBox.inset(-ATestActivity.ANNOTATION_BOUNDING_BOX_PADDING_PX.toFloat(), -ATestActivity.ANNOTATION_BOUNDING_BOX_PADDING_PX.toFloat())
        }

        mFragment.getVisiblePdfRect(currentAnnotation?.boundingBox!!, currentAnnotation.pageIndex)

    }

    private fun renderAnnotation() {
        // annotation 의 크기를 변경할 시 annotation 값을 다시 저장해 주어야 함
        if (currentAnnotation == null) return

        val bitmap = viewModel.getBitmap(currentAnnotation!!)

        mBitmap = bitmap

        binding.ivBitmapImage.setImageBitmap(bitmap)
        currentAnnotation!!.renderToBitmap(bitmap)

        createImageViewCanMove(bitmap, currentAnnotation!!)
        createImageViewUnderGuideline()
    } // https://pspdfkit.com/guides/android/annotations/rendering-annotations/

    private fun createImageViewUnderGuideline() {
        val annotationImageView = ImageView(mContext)

        addContentView(
            annotationImageView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentAnnotation?.isAttached != null) {
//            val annotation = currentAnnotation
//            if (ev?.actionMasked == MotionEvent.ACTION_MOVE) {
//                if (annotation?.boundingBox?.bottom!! < 0) {
//                    if (mBitmap != null) {
//                        binding.ivBitmapImage.setImageBitmap(mBitmap)
//                        currentAnnotation!!.renderToBitmap(mBitmap!!)
//                    } else {
//                        binding.ivBitmapImage.setImageBitmap(null)
//                    }
//                }
//            }

//            this.moveA(mBitmap!!, currentAnnotation!!)
//        }

        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createImageViewCanMove(bitmap: Bitmap, currentAnnotation: Annotation) {

        var startX = 0f
        var startY = 0f

        val annotationImageView = ImageView(mContext)

        annotationImageView.setImageBitmap(bitmap)
        currentAnnotation.renderToBitmap(bitmap)
        addContentView(
            annotationImageView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        Log.e("tetest", "moveA() 동작")

        annotationImageView.bringToFront()

        annotationImageView.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val movedX: Float = event.x - startX
                    val movedY: Float = event.y - startY

                    v.x = v.x + movedX
                    v.y = v.y + movedY
                }
            }
            true
        }
    }

    override fun onDocumentLongPress(
        document: PdfDocument,
        @IntRange(from = 0) pageIndex: Int,
        event: MotionEvent?,
        pagePosition: PointF?,
        longPressedAnnotation: Annotation?
    ): Boolean {
        mFragment.view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

        Log.e("tetest", "2222412")
        Log.e("tetest", "pageIndex === $pageIndex")
        Log.e("tetest", "event === $event")
        Log.e("tetest", "pagePosition === $pagePosition")
        Log.e("tetest", "longPressedAnnotation?.type === ${longPressedAnnotation?.type}")
        if (longPressedAnnotation is InkAnnotation) {
            val action = longPressedAnnotation.boundingBox
        }

        return false
    }

}