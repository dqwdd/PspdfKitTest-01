package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.agem.pspdfkittest_01.BaseBindingActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.databinding.ActivityBtestBinding
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.ui.PdfFragment

/**
 * 밑에 페이지 나오는 거랑 툴바는 Fragment 에서는 안 나와서 따로 설정해야 하는 듯?
 * */

class BTestActivity : BaseBindingActivity<ActivityBtestBinding>(
    R.layout.activity_btest) {

    private lateinit var mFragment: PdfFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvGoBTestToAFragment.setOnClickListener {
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
            mFragment = supportFragmentManager.findFragmentById(R.id.fl_to_b_fragment) as PdfFragment?
                    // If no fragment was found, create a new one providing it with the configuration and document Uri.
                ?: run {

                    val config = PdfActivityConfiguration.Builder(this)
                        .layout(com.pspdfkit.R.layout.pspdf__pdf_activity)
                        .build().configuration

                    val newFragment = PdfFragment.newInstance(uri, config)
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fl_to_b_fragment, newFragment)
                        .commit()

                    return@run newFragment
                }
        }
    }
}