package com.voxeldev.pixmoji.data.converters

import android.graphics.Bitmap
import com.voxeldev.pixmoji.data.pixmoji.PixmojiColors

class BitmapConverter(private val fieldWidth: Int, private val fieldHeight: Int) {

    private val colorsConverter = ColorsConverter()

    fun convertToPixmoji(bitmap: Bitmap): Array<Array<PixmojiColors?>> {
        val resizedBitmap = resizeBitmap(bitmap)

        val pixelColors = Array<Array<PixmojiColors?>>(fieldHeight) {
            arrayOfNulls(fieldWidth)
        }

        for (x in 0 until resizedBitmap.height) {
            for (y in 0 until resizedBitmap.width) {
                pixelColors[x][y] = colorsConverter.pixmojiFromRgb(
                    resizedBitmap.getPixel(y, x)
                )
            }
        }

        return pixelColors
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        var resizedBitmap = bitmap

        while (true) {
            val resizeWidth = resizedBitmap.width / 2 > fieldWidth
            val resizeHeight = resizedBitmap.height / 2 > fieldHeight

            val tempBitmap = Bitmap.createScaledBitmap(
                resizedBitmap,
                if (resizeWidth) resizedBitmap.width / 2 else fieldWidth,
                if (resizeHeight) resizedBitmap.height / 2 else fieldHeight,
                true
            )

            resizedBitmap.recycle()
            resizedBitmap = tempBitmap

            if (!(resizeWidth || resizeHeight))
                break
        }

        return resizedBitmap
    }
}