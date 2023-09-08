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
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.listeners.OnDocumentLongPressListener
import com.pspdfkit.listeners.SimpleDocumentListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.utils.getSupportParcelableExtra
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(R.layout.activity_ctest),
    DocumentListener, OnDocumentLongPressListener {

    private val viewModel: CTestViewModel by viewModels()

    private lateinit var mFragment: PdfFragment
    private lateinit var configuration: PdfConfiguration
    private lateinit var mBitmap: Bitmap

    // 주석 값 저장하는 것들
    private val documentAnnotations = mutableListOf<Annotation>()
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

        binding.btnRenderAnnotation.setOnClickListener {
            renderAnnotation()
        }

        binding.btnImageViewToAnnotation.setOnClickListener {
            setImageToAnnotation()
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
            mFragment = supportFragmentManager.findFragmentById(R.id.flToCFragment) as PdfFragment?
                ?: run {

                    configuration = intent.getSupportParcelableExtra(C_TEST_EXTRA_CONFIGURATION, PdfConfiguration::class.java)
                        ?: throw IllegalStateException("Activity Intent was missing configuration extra! at CTestActivity")

                    val newFragment = PdfFragment.newInstance(uri, configuration)
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
                            }
                    }

                    override fun onDocumentLoadFailed(exception: Throwable) {
                        Log.e("tetest", "onDocumentLoadFailed 222255543444")
                    }
                })
            }
        }
    }

    private fun renderAnnotation() {
        // annotation 의 크기를 변경할 시 annotation 값을 다시 저장해 주어야 함
        if (documentAnnotations.isEmpty()) return

        setBitmap()

//        documentAnnotations[0].renderToBitmap(mBitmap)

        createImageViewCanMove(mBitmap, documentAnnotations[1])
    } // https://pspdfkit.com/guides/android/annotations/rendering-annotations/

    @SuppressLint("CheckResult")
    private fun setImageToAnnotation() {
        // annotation 의 크기를 변경할 시 annotation 값을 다시 저장해 주어야 함
        setBitmap2()

        val mAnnotation = InkAnnotation(0)
        mAnnotation.color = documentAnnotations[1].color
        mAnnotation.alpha = documentAnnotations[1].alpha
        mAnnotation.lineWidth = documentAnnotations[1].borderWidth

        Log.e("tetest", "232332")
        Log.e("tetest", "documentAnnotations[1].type == ${documentAnnotations[1].type}")
        Log.e("tetest", "documentAnnotations[1].customData == ${documentAnnotations[1].customData}")

        val line: MutableList<PointF> = ArrayList()
        var x = 120
        while (x < 720) {
            val y = if (x % 120 == 0) 400 else 350
            line.add(PointF(x.toFloat(), y.toFloat()))
            x += 60
        }

        mFragment.document

        // Ink annotations can hold multiple lines. This example only uses a single line.
        mAnnotation.lines = listOf<List<PointF>>(line)
        mFragment.addAnnotationToPage(mAnnotation, true)
//        mAnnotation.renderToBitmapAsync(mBitmap).subscribe { bitmap, _ ->
//            mAnnotation.renderToBitmap(bitmap)
//        }
    }


    private fun setBitmap() {
        val bitmap = viewModel.getBitmap(documentAnnotations[0])
        mBitmap = bitmap
    }

    private fun setBitmap2() {
        val bitmap = viewModel.getBitmap(documentAnnotations[1])
        mBitmap = bitmap
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createImageViewCanMove(bitmap: Bitmap, firstAnnotation: Annotation) {

        Log.e("tetest", "createImageViewCanMove() 동작")

        var startX = 0f
        var startY = 0f

        val annotationImageView = ImageView(mContext)

        annotationImageView.setImageBitmap(bitmap)
        firstAnnotation.renderToBitmap(bitmap)
        addContentView(
            annotationImageView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

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

    @SuppressLint("ClickableViewAccessibility")
    private fun createImageViewOnCoordinate(bitmap: Bitmap, currentAnnotation: Annotation, x: Float, y: Float) {

        Log.e("tetest", "createImageViewOnTouchCoordinate() 동작")

        var startX = 0f
        var startY = 0f

        val annotationImageView = ImageView(mContext)

        annotationImageView.setImageBitmap(bitmap)
        currentAnnotation.renderToBitmap(bitmap)
        addContentView(
            annotationImageView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        annotationImageView.bringToFront()
        annotationImageView.x = x
        annotationImageView.y = y

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

        Log.e("tetest", "event === $event")
        Log.e("tetest", "pagePosition === $pagePosition")
        Log.e("tetest", "longPressedAnnotation?.type === ${longPressedAnnotation?.type}")

        if (longPressedAnnotation is InkAnnotation) {
            setBitmap()
            createImageViewOnCoordinate(mBitmap, documentAnnotations[0], pagePosition!!.x, pagePosition.y)
        }

        return false
    }

    companion object {
        const val C_TEST_EXTRA_CONFIGURATION = "CTestActivity.EXTRA_CONFIGURATION"
    }
}