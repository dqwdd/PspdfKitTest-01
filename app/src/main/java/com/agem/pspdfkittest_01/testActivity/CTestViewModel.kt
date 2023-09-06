package com.agem.pspdfkittest_01.testActivity

import android.graphics.Bitmap
import com.agem.pspdfkittest_01.base.BaseViewModel

class CTestViewModel : BaseViewModel() {

    fun getBitmap(annotation: com.pspdfkit.annotations.Annotation): Bitmap {
        val annotationWidth = annotation.boundingBox.width()
        val annotationHeight = -annotation.boundingBox.height()

        val bitmapWidth = 300
        val heightFactor = bitmapWidth / annotationWidth
        val bitmapHeight = (annotationHeight * heightFactor).toInt()

        return Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
    }
}