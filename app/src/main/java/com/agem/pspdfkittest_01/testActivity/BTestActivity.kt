package com.agem.pspdfkittest_01.testActivity

import android.os.Bundle
import com.agem.pspdfkittest_01.BaseBindingActivity
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.databinding.ActivityBtestBinding
import com.pspdfkit.ui.PdfActivity

class BTestActivity : BaseBindingActivity<ActivityBtestBinding>(
    R.layout.activity_btest) {

    private val testPdf = PdfActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testPdf
    }
}