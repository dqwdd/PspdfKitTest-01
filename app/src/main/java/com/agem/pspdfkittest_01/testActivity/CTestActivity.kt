package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.base.BaseBindingActivity
import com.agem.pspdfkittest_01.databinding.ActivityCtestBinding
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.listeners.DocumentListener
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

// 리스너 달면 될듯
class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(
    R.layout.activity_ctest),
    AnnotationManager.OnAnnotationCreationModeChangeListener,
    AnnotationManager.OnAnnotationEditingModeChangeListener,
    TextSelectionManager.OnTextSelectionModeChangeListener,
    DocumentListener{

    private lateinit var mFragment: PdfFragment

    private var annotationCreationActive = false

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
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    Log.e("tetest", "8788888")
                    Log.e("tetest", "binding.guideline.layoutParams === ${binding.guideline.layoutParams}")
                }
                MotionEvent.ACTION_UP -> {
                    Log.e("tetest", "214124")
                    val location = IntArray(2)
                    binding.guideline.getLocationInSurface(location)
//                    Log.e("tetest", "location[0] === ${location[0]}")
//                    Log.e("tetest", "location[1] === ${location[1]}")

                }

                else -> return@setOnTouchListener false
            }
            return@setOnTouchListener true
        }


        toolbarCoordinatorLayout = findViewById(R.id.toolbarCoordinatorLayout)

        annotationCreationToolbar = AnnotationCreationToolbar(this)
        textSelectionToolbar = TextSelectionToolbar(this)
        annotationEditingToolbar = AnnotationEditingToolbar(this)

        // Use this if you want to use annotation inspector with annotation creation and editing toolbars.
        inspectorCoordinatorLayout = findViewById(R.id.inspectorCoordinatorLayout)
        annotationEditingInspectorController = DefaultAnnotationEditingInspectorController(this, inspectorCoordinatorLayout)
        annotationCreationInspectorController = DefaultAnnotationCreationInspectorController(this, inspectorCoordinatorLayout)

        binding.btnGetAnnotationLocateData.setOnClickListener {
            getAnnotationLocateData()
        }

        binding.btnRenderAnnotation.setOnClickListener {
            renderAnnotation()
        }
    }

    private fun initAnnotationCreationButton() {
        annotationCreationButton = findViewById(R.id.openAnnotationEditing)
        annotationCreationButton.setOnClickListener {
            if (annotationCreationActive) {
                mFragment.exitCurrentlyActiveMode()
            } else {
                mFragment.enterAnnotationCreationMode()
            }
        }

        getAnnotation()

        updateButtonText()
    }

    private fun updateButtonText() {
        val tt = if (annotationCreationActive) "close_editor" else "open_editor"
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


            mFragment.addOnAnnotationCreationModeChangeListener(this)
            mFragment.addOnAnnotationEditingModeChangeListener(this)
            mFragment.addOnTextSelectionModeChangeListener(this)

            initAnnotationCreationButton()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mFragment.removeOnAnnotationCreationModeChangeListener(this)
        mFragment.removeOnAnnotationEditingModeChangeListener(this)
        mFragment.removeOnTextSelectionModeChangeListener(this)
    }

    // AnnotationManager.OnAnnotationCreationModeChangeListener
    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {

        annotationCreationInspectorController.bindAnnotationCreationController(controller)

        // When entering the annotation creation mode we bind the toolbar to the provided
        // controller, and
        // issue the coordinator layout to animate the toolbar in place.
        // Whenever the user presses an action, the toolbar forwards this command to the controller.
        // Instead of using the `AnnotationEditingToolbar` you could use a custom UI that operates
        // on the controller.
        // Same principle is used on all other toolbars.

        // When entering the annotation creation mode we bind the toolbar to the provided
        // controller, and
        // issue the coordinator layout to animate the toolbar in place.
        // Whenever the user presses an action, the toolbar forwards this command to the controller.
        // Instead of using the `AnnotationEditingToolbar` you could use a custom UI that operates
        // on the controller.
        // Same principle is used on all other toolbars.
        annotationCreationToolbar.bindController(controller)
        toolbarCoordinatorLayout.displayContextualToolbar(annotationCreationToolbar, true)
        annotationCreationActive = true
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
        annotationCreationActive = false

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

            Log.e("tetest", "366666")
            Log.e("tetest", "boundingBox === $boundingBox")

        }

        Log.e("tetest", "21512")
        Log.e("tetest", "currentAnnotation.boundingBox === ${currentAnnotation?.boundingBox}")
        Log.e("tetest", "currentAnnotation.boundingBox?.bottom === ${currentAnnotation?.boundingBox?.bottom}")

        // After conversion, the `boundingBox` will be in screen coordinates.
        mFragment.getVisiblePdfRect(currentAnnotation?.boundingBox!!, currentAnnotation.pageIndex)

    }

    private fun getAnnotation() {
        mFragment.addDocumentListener(object : SimpleDocumentListener() {
            @UiThread
            override fun onDocumentLoaded(loadedDocument: PdfDocument) {
                Log.e("tetest", "65555555")
                annotationLoadingDisposable = loadedDocument.annotationProvider
                    .getAllAnnotationsOfTypeAsync(EnumSet.allOf(AnnotationType::class.java))
                    .toList()
                    .subscribe { annotations -> documentAnnotations.addAll(annotations) }
            }

            override fun onDocumentLoadFailed(exception: Throwable) {
                Log.e("tetest", "onDocumentLoadFailed 222255543444")
            }
        })

        mFragment.addOnFormElementUpdatedListener {  }
    }

    private fun renderAnnotation() {
        if (currentAnnotation == null) return

        val annotationWidth = currentAnnotation!!.boundingBox.width()
        val annotationHeight = -currentAnnotation!!.boundingBox.height()

        val bitmapWidth = 300
        val heightFactor = bitmapWidth / annotationWidth
        val bitmapHeight = (annotationHeight * heightFactor).toInt()


        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888)

        mBitmap = bitmap

        binding.ivBitmapImage.setImageBitmap(mBitmap)
        Log.e("tetest", "!@221211")

    } // https://pspdfkit.com/guides/android/annotations/rendering-annotations/

    var mBitmap: Bitmap? = null

//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentAnnotation?.isAttached != null) {
//            val annotation = currentAnnotation
//            Log.e("tetest", "currentAnnotation?.isAttached === ${currentAnnotation?.isAttached}")
//
//            if (ev?.actionMasked == MotionEvent.ACTION_MOVE) {
//                Log.e("tetest", "211212122")
//                Log.e("tetest", "annotation?.boundingBox == ${annotation?.boundingBox}")
//                if (annotation?.boundingBox?.bottom!! < 0.25) {
//                    Log.e("tetest", "64646464646")
//                    if (mBitmap != null) {
//                        binding.ivBitmapImage.setImageBitmap(mBitmap)
//                        Log.e("tetest", "444444444")
////                        binding.ivBitmapImage.setImageResource(R.drawable.ic_opening_documents)
//                    }
//                    else {
//                        binding.ivBitmapImage.setImageBitmap(null)
//                        Log.e("tetest", "26666666666622")
//                    }
//                }
//            }
////            else if (ev?.actionMasked == MotionEvent.ACTION_DOWN) {
////                if (annotation?.boundingBox?.bottom!! < 0.25) {
////                    Log.e("tetest", "00005555")
////                    if (mBitmap != null) {
////                        binding.ivBitmapImage.setImageBitmap(mBitmap)
////                        Log.e("tetest", "111111")
////                    }
////                    else {
////                        binding.ivBitmapImage.setImageBitmap(null)
////                        Log.e("tetest", "22222")
////                    }
////                } else {
////                    Log.e("tetest", "114555555")
////                    Log.e("tetest", "annotation?.boundingBox?.bottom == ${annotation.boundingBox.bottom}")
////                }
////            }
//        }
//
//        return super.dispatchTouchEvent(ev)
//    }
}