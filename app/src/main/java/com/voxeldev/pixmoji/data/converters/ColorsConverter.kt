package com.voxeldev.pixmoji.data.converters

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.voxeldev.pixmoji.data.pixmoji.PixmojiColors
import kotlin.math.pow
import kotlin.math.sqrt

class ColorsConverter {

    private val baseColors = arrayOf(
        Color.parseColor("#C71700"),
        Color.parseColor("#DB6300"),
        Color.parseColor("#E8D100"),
        Color.parseColor("#39BD00"),
        Color.parseColor("#00A2D4"),
        Color.parseColor("#CD00D4"),
        Color.parseColor("#733900"),
        Color.BLACK,
        Color.WHITE
    )

    fun pixmojiFromRgb(@ColorInt color: Int): PixmojiColors {
        var minDifference = Int.MAX_VALUE
        var minIndex = 0

        for (i in baseColors.indices) {
            val difference = compareColors(color, baseColors[i])

            if (difference < minDifference) {
                minDifference = difference
                minIndex = i
            }
        }

        return PixmojiColors.values()[minIndex]
    }

    private fun compareColors(@ColorInt color: Int, @ColorInt anotherColor: Int): Int {
        val colorToLab = rgb2lab(color.red, color.green, color.blue)
        val anotherColorToLab = rgb2lab(anotherColor.red, anotherColor.green, anotherColor.blue)

        return sqrt(
            (colorToLab[0] - anotherColorToLab[0]).toFloat().pow(2) +
                    (colorToLab[1] - anotherColorToLab[1]).toFloat().pow(2) +
                    (colorToLab[2] - anotherColorToLab[2]).toFloat().pow(2)
        ).toInt()
    }

    // Converts RGB to CIE-L*ab
    // The author of the function is: http://www.brucelindbloom.com
    private fun rgb2lab(R: Int, G: Int, B: Int): IntArray {
        val x: Float
        val y: Float
        val z: Float
        val fx: Float
        val fy: Float
        val fz: Float
        val xr: Float
        val yr: Float
        val zr: Float
        val eps = 216f / 24389f
        val k = 24389f / 27f
        val xrd = 0.964221f // reference white D50
        val yrd = 1.0f
        val zrd = 0.825211f

        // RGB to XYZ
        var r: Float = R / 255f //R 0..1
        var g: Float = G / 255f //G 0..1
        var b: Float = B / 255f //B 0..1

        // assuming sRGB (D65)
        r = if (r <= 0.04045) r / 12 else ((r + 0.055) / 1.055).pow(2.4).toFloat()
        g = if (g <= 0.04045) g / 12 else ((g + 0.055) / 1.055).pow(2.4).toFloat()
        b = if (b <= 0.04045) b / 12 else ((b + 0.055) / 1.055).pow(2.4).toFloat()
        x = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b
        y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b
        z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b

        // XYZ to Lab
        xr = x / xrd
        yr = y / yrd
        zr = z / zrd
        fx = if (xr > eps) xr.toDouble().pow(1 / 3.0)
            .toFloat() else ((k * xr + 16.0) / 116.0).toFloat()
        fy = if (yr > eps) yr.toDouble().pow(1 / 3.0)
            .toFloat() else ((k * yr + 16.0) / 116.0).toFloat()
        fz = if (zr > eps) zr.toDouble().pow(1 / 3.0)
            .toFloat() else ((k * zr + 16.0) / 116).toFloat()
        val Ls: Float = 116 * fy - 16
        val `as`: Float = 500 * (fx - fy)
        val bs: Float = 200 * (fy - fz)

        return intArrayOf((2.55 * Ls + .5).toInt(), (`as` + .5).toInt(), (bs + .5).toInt())
    }
}