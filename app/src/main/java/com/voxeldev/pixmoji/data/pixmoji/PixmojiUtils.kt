package com.voxeldev.pixmoji.data.pixmoji

class PixmojiUtils {

    companion object {
        fun convertColorsToEmoji(pixelColors: Array<Array<PixmojiColors?>>): String {
            val stringBuilder = StringBuilder()

            for (row in pixelColors) {
                for (column in row) {
                    stringBuilder.append(
                        column?.getColorEmoji() ?: PixmojiColors.WHITE.getColorEmoji()
                    )
                }
                stringBuilder.append(System.lineSeparator())
            }

            return stringBuilder.toString()
        }
    }
}