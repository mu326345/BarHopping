package com.yuyu.barhopping.map.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

class QrCodeViewModel: ViewModel() {

    fun getRouteIdQrCodeBitmap(routId: String): Bitmap {
        val barcode = BarcodeFormat.QR_CODE
        val matrix = MultiFormatWriter().encode(routId, barcode, 600, 600, null)

        val w = matrix.width
        val h = matrix.height
        val rawData = IntArray(w * h)

        for (i in 0 until w) {
            for (j in 0 until h) {
                //沒內容的顏色
                var color = Color.WHITE
                if (matrix.get(i, j)) {
                    //有內容的顏色
                    color = Color.BLACK
                }
                rawData[i + (j * w)] = color
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h)
        return bitmap
    }
}