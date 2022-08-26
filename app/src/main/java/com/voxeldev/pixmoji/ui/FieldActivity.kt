package com.voxeldev.pixmoji.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.voxeldev.pixmoji.R
import com.voxeldev.pixmoji.data.converters.BitmapConverter
import com.voxeldev.pixmoji.data.pixmoji.PixmojiColors
import com.voxeldev.pixmoji.data.pixmoji.PixmojiUtils
import com.voxeldev.pixmoji.databinding.ActivityFieldBinding
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates

private const val DEFAULT_SNACKBAR_DURATION = 3000

class FieldActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityFieldBinding? = null
    private val binding get() = _binding!!

    private var fieldWidth by Delegates.notNull<Int>()
    private var fieldHeight by Delegates.notNull<Int>()

    private lateinit var pixelColors: Array<Array<PixmojiColors?>>
    private var activeColor = PixmojiColors.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        val imageResult = getImageResult() // must be called before onCreate

        super.onCreate(savedInstanceState)

        _binding = ActivityFieldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fieldWidth = intent.getIntExtra("width", 2)
        fieldHeight = intent.getIntExtra("height", 2)

        val previousState = savedInstanceState?.getString("pixelColors")
        val previousActiveColor = savedInstanceState?.getString("activeColor")

        if (previousState != null) {
            val gson = Gson()

            createPixels(gson.fromJson(previousState, Array<Array<PixmojiColors?>>::class.java))

            activeColor = gson.fromJson(previousActiveColor, PixmojiColors::class.java)
            getCheckMarkByColor(PixmojiColors.RED, binding).visibility = View.GONE
            getCheckMarkByColor(activeColor, binding).visibility = View.VISIBLE
        } else
            createPixels()

        binding.buttonClearField.setOnClickListener {
            binding.layoutRows.removeAllViews()
            createPixels()

            Snackbar.make(binding.layoutField, R.string.cleared, DEFAULT_SNACKBAR_DURATION).show()
        }

        binding.buttonMenu.setOnClickListener { createMenu(imageResult) }

        setColorButtonsOnClickListener()
    }


    private fun createMenu(imageResult: ActivityResultLauncher<Intent>) {
        PopupMenu(this, binding.buttonMenu).apply {
            menuInflater.inflate(R.menu.menu_field, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.copy_as_emojis -> {
                        copyAsEmojis()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.import_image -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        imageResult.launch(intent)
                        return@setOnMenuItemClickListener true
                    }
                }

                return@setOnMenuItemClickListener false
            }
        }.show()
    }

    private fun copyAsEmojis() {
        val clipboard: ClipboardManager = applicationContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("Pixmoji", PixmojiUtils.convertColorsToEmoji(pixelColors))
        )

        Snackbar.make(binding.layoutField, R.string.copied, DEFAULT_SNACKBAR_DURATION).show()
    }

    private fun getImageResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK)
                processImageResult(result)
        }
    }

    private fun processImageResult(result: ActivityResult) {
        if (result.data == null || result.data!!.data == null) {
            return
        }

        pixelColors = BitmapConverter(fieldWidth, fieldHeight).convertToPixmoji(
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(contentResolver, result.data!!.data!!)

                ImageDecoder.decodeBitmap(
                    source
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    result.data!!.data!!
                )
            }
        )

        binding.layoutRows.removeAllViews()
        createPixels(pixelColors)
    }


    private fun setColorButtonsOnClickListener() {
        binding.buttonRed.setOnClickListener(this)
        binding.buttonOrange.setOnClickListener(this)
        binding.buttonYellow.setOnClickListener(this)
        binding.buttonGreen.setOnClickListener(this)
        binding.buttonBlue.setOnClickListener(this)
        binding.buttonPurple.setOnClickListener(this)
        binding.buttonBrown.setOnClickListener(this)
        binding.buttonBlack.setOnClickListener(this)
        binding.buttonWhite.setOnClickListener(this)
    }

    private fun getCheckMarkByColor(
        color: PixmojiColors,
        binding: ActivityFieldBinding
    ): ImageView {
        return when (color) {
            PixmojiColors.RED -> binding.buttonRedChecked
            PixmojiColors.ORANGE -> binding.buttonOrangeChecked
            PixmojiColors.YELLOW -> binding.buttonYellowChecked
            PixmojiColors.GREEN -> binding.buttonGreenChecked
            PixmojiColors.BLUE -> binding.buttonBlueChecked
            PixmojiColors.PURPLE -> binding.buttonPurpleChecked
            PixmojiColors.BROWN -> binding.buttonBrownChecked
            PixmojiColors.BLACK -> binding.buttonBlackChecked
            PixmojiColors.WHITE -> binding.buttonWhiteChecked
        }
    }


    private fun createPixels(
        previousState: Array<Array<PixmojiColors?>>? = null
    ) {
        try {
            pixelColors = previousState ?: Array(fieldHeight) { arrayOfNulls(fieldWidth) }
        } catch (e: OutOfMemoryError) {
            Log.e(LOG_TAG, "Out Of Memory error")
            finish()
            return
        }

        binding.scrollview.visibility = View.GONE
        binding.loaderField.setProgress(0, false)
        binding.loaderField.visibility = View.VISIBLE

        for (i in 0 until fieldHeight)
            binding.layoutRows.addView(createRow(i, previousState))
    }

    private fun createRow(
        height: Int,
        previousState: Array<Array<PixmojiColors?>>?
    ): LinearLayout {
        val layoutInflater = LayoutInflater.from(this)

        val layout = LinearLayout(this)
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.orientation = LinearLayout.HORIZONTAL

        val resources = resources
        val theme = theme

        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0 until fieldWidth) {
                layoutInflater.inflate(R.layout.view_pixel, layout, false).apply {
                    this.tag = arrayOf(height, i)

                    if (previousState != null && previousState[height][i] != null)
                        (this as MaterialCardView)
                            .setCardBackgroundColor(
                                resources.getColor(
                                    previousState[height][i]?.getColorResource()!!, theme
                                )
                            )

                    this.setOnClickListener {
                        val tag = this.tag as Array<*>

                        (this as MaterialCardView).setCardBackgroundColor(
                            resources.getColor(
                                activeColor.getColorResource(), theme
                            )
                        )
                        pixelColors[tag[0] as Int][tag[1] as Int] = activeColor
                    }
                }.also {
                    runOnUiThread {
                        layout.addView(it)
                    }
                }
            }

            runOnUiThread {
                val progress = (height.toFloat() / (fieldHeight - 1) * 100).roundToInt()
                try {
                    if (progress < 100)
                        binding.loaderField.setProgress(progress, true)
                    else {
                        binding.loaderField.visibility = View.GONE
                        binding.scrollview.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, e.message ?: e.toString())
                    this.cancel()
                }
            }
        }

        return layout
    }


    override fun onClick(view: View?) {
        if (view == null) return

        getCheckMarkByColor(activeColor, binding).visibility = View.GONE

        when (view.id) {
            R.id.button_red -> activeColor = PixmojiColors.RED
            R.id.button_orange -> activeColor = PixmojiColors.ORANGE
            R.id.button_yellow -> activeColor = PixmojiColors.YELLOW
            R.id.button_green -> activeColor = PixmojiColors.GREEN
            R.id.button_blue -> activeColor = PixmojiColors.BLUE
            R.id.button_purple -> activeColor = PixmojiColors.PURPLE
            R.id.button_brown -> activeColor = PixmojiColors.BROWN
            R.id.button_black -> activeColor = PixmojiColors.BLACK
            R.id.button_white -> activeColor = PixmojiColors.WHITE
        }

        getCheckMarkByColor(activeColor, binding).visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pixelColors", Gson().toJson(pixelColors))
        outState.putString("activeColor", Gson().toJson(activeColor))
    }
}