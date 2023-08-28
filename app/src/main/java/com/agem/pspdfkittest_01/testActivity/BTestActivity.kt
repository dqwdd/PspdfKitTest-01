package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.agem.pspdfkittest_01.BaseBindingActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.databinding.ActivityBtestBinding
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.ui.PdfFragment

/**
 * 이 Activity 는 Fragment 그냥 나오게 한 Activity
 *
 * 밑에 몇 번째 페이지인지 나오는 거랑 툴바는 Fragment 에서는 안 나와서 따로 설정해야 하는 듯?
 * */

class BTestActivity : BaseBindingActivity<ActivityBtestBinding>(
    R.layout.activity_btest) {

    private lateinit var mFragment: PdfFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvGoBTestToAFragment.setOnClickListener {
            Log.e("tetest", "!51")
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
                ?: run {
                    val newFragment = PdfFragment.newInstance(uri, PdfConfiguration.Builder().build())
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fl_to_b_fragment, newFragment)
                        .commit()

                    return@run newFragment
                }
        }
    }
}