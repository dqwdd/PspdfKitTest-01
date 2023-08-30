package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.agem.pspdfkittest_01.base.BaseBindingActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.databinding.ActivityCtestBinding
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
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

/**
 * 이 Activity 는 화면 Split 하게 나오는 거 테스트하는 Activity
 * */

// 리스너 달면 될듯
class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(
    R.layout.activity_ctest),
    AnnotationManager.OnAnnotationCreationModeChangeListener,
    AnnotationManager.OnAnnotationEditingModeChangeListener,
    TextSelectionManager.OnTextSelectionModeChangeListener {

    private lateinit var mFragment: PdfFragment

    private var annotationCreationActive = false

    private lateinit var annotationCreationButton: Button
    private lateinit var toolbarCoordinatorLayout: ToolbarCoordinatorLayout

    private lateinit var annotationCreationToolbar: AnnotationCreationToolbar
    private lateinit var textSelectionToolbar: TextSelectionToolbar
    private lateinit var annotationEditingToolbar: AnnotationEditingToolbar

    private lateinit var inspectorCoordinatorLayout: PropertyInspectorCoordinatorLayout
    private lateinit var annotationEditingInspectorController: AnnotationEditingInspectorController
    private lateinit var annotationCreationInspectorController: AnnotationCreationInspectorController

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

        updateButtonText()
    }

    private fun updateButtonText() {
        annotationCreationButton.text = if (annotationCreationActive) "close_editor" else "open_editor"
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
            mFragment = supportFragmentManager.findFragmentById(R.id.fl_to_c_fragment) as PdfFragment?
                ?: run {
                    val newFragment = PdfFragment.newInstance(
                        /* documentUri = */ uri,
                        /* configuration = */ PdfConfiguration.Builder().scrollDirection(PageScrollDirection.HORIZONTAL).build())
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fl_to_c_fragment, newFragment)
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
}