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
import android.widget.Button
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
import com.pspdfkit.ui.inspector.PropertyInspectorCoordinatorLayout
import com.pspdfkit.ui.inspector.annotation.AnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.AnnotationEditingInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationEditingInspectorController
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationEditingController
import com.pspdfkit.ui.special_mode.controller.TextSelectionController
import com.pspdfkit.ui.special_mode.manager.AnnotationManager
import com.pspdfkit.ui.special_mode.manager.TextSelectionManager
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.AnnotationEditingToolbar
import com.pspdfkit.ui.toolbar.TextSelectionToolbar
import com.pspdfkit.ui.toolbar.ToolbarCoordinatorLayout
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import kotlin.math.min

/**
 * 이 Activity 는 화면 Split 하게 나오는 거 테스트하는 Activity
 * */

class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(
    R.layout.activity_ctest),
    AnnotationManager.OnAnnotationCreationModeChangeListener,
    AnnotationManager.OnAnnotationEditingModeChangeListener,
    TextSelectionManager.OnTextSelectionModeChangeListener,
    DocumentListener,
    OnDocumentLongPressListener{

    private val viewModel: CTestViewModel by viewModels()

    private lateinit var mFragment: PdfFragment

    private var annotationToolbarCreationActive = false

    private var mBitmap: Bitmap? = null

    // 툴바 열기 위한 것들
    private lateinit var annotationCreationButton: Button
    private lateinit var toolbarCoordinatorLayout: ToolbarCoordinatorLayout

    private lateinit var annotationCreationToolbar: AnnotationCreationToolbar
    private lateinit var textSelectionToolbar: TextSelectionToolbar
    private lateinit var annotationEditingToolbar: AnnotationEditingToolbar

    private lateinit var inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
    private lateinit var annotationEditingInspectorController: AnnotationEditingInspectorController
    private lateinit var annotationCreationInspectorController: AnnotationCreationInspectorController

    // 주석 값 저장하는 것들
    private val documentAnnotations = mutableListOf<Annotation>()
    private var currentAnnotation: Annotation? = null
    private var annotationLoadingDisposable: Disposable? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar()
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

    private fun setToolbar() {
        toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout)

        annotationCreationToolbar = AnnotationCreationToolbar(this)
        textSelectionToolbar = TextSelectionToolbar(this)
        annotationEditingToolbar = AnnotationEditingToolbar(this)

        // Use this if you want to use annotation inspector with annotation creation and editing toolbars.
        inspectorCoordinatorLayout = findViewById(R.id.inspectorCoordinatorLayout)
        annotationEditingInspectorController = DefaultAnnotationEditingInspectorController(this, inspectorCoordinatorLayout)
        annotationCreationInspectorController = DefaultAnnotationCreationInspectorController(this, inspectorCoordinatorLayout)

    }

    private fun initAnnotationCreationButton() {
        annotationCreationButton = findViewById(R.id.openAnnotationEditing)
        annotationCreationButton.setOnClickListener {
            if (annotationToolbarCreationActive) {
                mFragment.exitCurrentlyActiveMode()
            } else {
                mFragment.enterAnnotationCreationMode()
            }
        }

        updateButtonText()
    }

    private fun updateButtonText() {
        val tt = if (annotationToolbarCreationActive) "close_editor" else "open_editor"
        binding.openAnnotationEditing.text = tt
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


            // 툴바 리스너 apply
            mFragment.apply {
                addOnAnnotationCreationModeChangeListener(this@CTestActivity)
                addOnAnnotationEditingModeChangeListener(this@CTestActivity)
                addOnTextSelectionModeChangeListener(this@CTestActivity)
                addDocumentListener(object : SimpleDocumentListener() {
                    @UiThread
                    override fun onDocumentLoaded(loadedDocument: PdfDocument) {
                        annotationLoadingDisposable = loadedDocument.annotationProvider
                            .getAllAnnotationsOfTypeAsync(EnumSet.allOf(AnnotationType::class.java))
                            .toList()
                            .subscribe { annotations -> documentAnnotations.addAll(annotations) }
                    }

                    override fun onDocumentLoadFailed(exception: Throwable) {
                        Log.e("tetest", "onDocumentLoadFailed 222255543444")
                    }
                })
                setOnDocumentLongPressListener(this@CTestActivity)
            }

            initAnnotationCreationButton()
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

        return true
    }



    // 아래는 툴바 관련

    override fun onDestroy() {
        super.onDestroy()
        mFragment.removeOnAnnotationCreationModeChangeListener(this)
        mFragment.removeOnAnnotationEditingModeChangeListener(this)
        mFragment.removeOnTextSelectionModeChangeListener(this)
    }

    // AnnotationManager.OnAnnotationCreationModeChangeListener
    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {
        annotationCreationInspectorController.bindAnnotationCreationController(controller)

        annotationCreationToolbar.bindController(controller)
        toolbarCoordinatorLayout.displayContextualToolbar(annotationCreationToolbar, true)
        annotationToolbarCreationActive = true
        updateButtonText()
    }

    // AnnotationManager.OnAnnotationCreationModeChangeListener
    override fun onChangeAnnotationCreationMode(controller: AnnotationCreationController) {
        // Nothing to be done here,
        // if toolbar is bound to the controller it will pick up the
        // changes.
    }

    // AnnotationManager.OnAnnotationCreationModeChangeListener
    override fun onExitAnnotationCreationMode(controller: AnnotationCreationController) {
        toolbarCoordinatorLayout.removeContextualToolbar(true)
        annotationCreationToolbar.unbindController()
        annotationToolbarCreationActive = false

        annotationCreationInspectorController.unbindAnnotationCreationController()
        updateButtonText()

    }

    // AnnotationManager.OnAnnotationEditingModeChangeListener
    override fun onEnterAnnotationEditingMode(controller: AnnotationEditingController) {
        annotationEditingInspectorController.bindAnnotationEditingController(controller)

        annotationEditingToolbar.bindController(controller)
        toolbarCoordinatorLayout.displayContextualToolbar(annotationEditingToolbar, true)
    }

    // AnnotationManager.OnAnnotationEditingModeChangeListener
    override fun onChangeAnnotationEditingMode(controller: AnnotationEditingController) {
        // Nothing to be done here, if toolbar is bound to the controller it will pick up the
        // changes.
    }

    // AnnotationManager.OnAnnotationEditingModeChangeListener
    override fun onExitAnnotationEditingMode(controller: AnnotationEditingController) {
        toolbarCoordinatorLayout.removeContextualToolbar(true)
        annotationEditingToolbar.unbindController()

        annotationEditingInspectorController.unbindAnnotationEditingController()
    }

    // TextSelectionManager.OnTextSelectionModeChangeListener
    override fun onEnterTextSelectionMode(controller: TextSelectionController) {
        textSelectionToolbar.bindController(controller)
        toolbarCoordinatorLayout.displayContextualToolbar(textSelectionToolbar, true)
    }

    // TextSelectionManager.OnTextSelectionModeChangeListener
    override fun onExitTextSelectionMode(controller: TextSelectionController) {
        toolbarCoordinatorLayout.removeContextualToolbar(true)
        textSelectionToolbar.unbindController()
    }

}