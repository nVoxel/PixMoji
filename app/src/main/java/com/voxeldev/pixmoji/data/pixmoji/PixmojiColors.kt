package com.voxeldev.pixmoji.data.pixmoji

import com.voxeldev.pixmoji.R

enum class PixmojiColors {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE,
    BROWN,
    BLACK,
    WHITE;

    fun getColorResource(): Int =
        when (this) {
            RED -> R.color.red
            ORANGE -> R.color.orange
            YELLOW -> R.color.yellow
            GREEN -> R.color.green
            BLUE -> R.color.blue
            PURPLE -> R.color.purple
            BROWN -> R.color.brown
            BLACK -> R.color.black
            WHITE -> R.color.white
        }

    fun getColorEmoji(): String =
        when (this) {
            RED -> "\uD83D\uDFE5"
            ORANGE -> "\uD83D\uDFE7"
            YELLOW -> "\uD83D\uDFE8"
            GREEN -> "\uD83D\uDFE9"
            BLUE -> "\uD83D\uDFE6"
            PURPLE -> "\uD83D\uDFEA"
            BROWN -> "\uD83D\uDFEB"
            BLACK -> "⬛"
            WHITE -> "⬜"
        }
}