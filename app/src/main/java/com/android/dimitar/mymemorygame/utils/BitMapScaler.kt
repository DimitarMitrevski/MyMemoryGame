package com.android.dimitar.mymemorygame.utils

import android.graphics.Bitmap

class BitMapScaler {

    fun scaleToFitWidth(b:Bitmap, width:Int): Bitmap {
        val factor = width / b.width.toFloat();
        return Bitmap.createScaledBitmap(b,width,(b.height * factor).toInt(), true);
    }

    fun scaleToFitHeight(b:Bitmap, height:Int): Bitmap {
        val factor = height / b.height.toFloat();
        return Bitmap.createScaledBitmap(b,height,(b.width * factor).toInt(), true);
    }

}
