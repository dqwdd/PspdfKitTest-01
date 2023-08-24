package com.agem.pspdfkittest_01.testActivity

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import com.agem.pspdfkittest_01.R
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.PdfActivity

class ATestActivity : PdfActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setValues()
    }

    private fun setValues() {
        findViewById<TextView>(R.id.tv_open_pdf).setOnClickListener {
            Toast.makeText(this, "123", Toast.LENGTH_SHORT).show()
        }
    }
}