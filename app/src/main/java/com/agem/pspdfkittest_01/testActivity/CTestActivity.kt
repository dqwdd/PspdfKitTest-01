package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.agem.pspdfkittest_01.BaseBindingActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.databinding.ActivityCtestBinding
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.ui.PdfFragment

/**
 * 이 Activity 는 화면 Split 하게 나오는 거 테스트하는 Activity
 * */

// 리스너 달면 될듯
class CTestActivity : BaseBindingActivity<ActivityCtestBinding>(
    R.layout.activity_ctest) {

    private lateinit var mFragment: PdfFragment

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
        }
    }
}