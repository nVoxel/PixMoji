package com.voxeldev.pixmoji.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.voxeldev.pixmoji.R
import com.voxeldev.pixmoji.databinding.ActivityMainBinding

const val LOG_TAG = "PixMoji"

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textWidth.doOnTextChanged { _, _, _, _ ->
            run {
                binding.layoutWidth.isErrorEnabled = false
            }
        }
        binding.textHeight.doOnTextChanged { _, _, _, _ ->
            run {
                binding.layoutHeight.isErrorEnabled = false
            }
        }

        binding.buttonContinue.setOnClickListener {
            if (TextUtils.isEmpty(binding.textWidth.text)) {
                showError(binding.layoutWidth, resources.getString(R.string.enter_value))
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(binding.textHeight.text)) {
                showError(binding.layoutHeight, resources.getString(R.string.enter_value))
                return@setOnClickListener
            }

            val width: Int
            val height: Int
            var heightError = false

            try {
                width = binding.textWidth.text?.toString()?.toInt()!!
                heightError = true
                height = binding.textHeight.text?.toString()?.toInt()!!
            } catch (e: Exception) {
                showError(
                    if (heightError) binding.layoutHeight else binding.layoutWidth,
                    resources.getString(R.string.incorrect_value)
                )
                return@setOnClickListener
            }

            if (width < 2 || height < 2) {
                showError(
                    if (width < 2) binding.layoutWidth else binding.layoutHeight,
                    resources.getString(R.string.incorrect_value)
                )
                return@setOnClickListener
            }

            if (width.toLong() * height > 4096)
                AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title)
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.alert_positive)
                    { _, _ ->
                        run {
                            startField(width, height)
                        }
                    }
                    .setNegativeButton(R.string.alert_negative, null)
                    .show()
            else
                startField(width, height)
        }
    }

    private fun showError(textInputLayout: TextInputLayout, error: String) {
        textInputLayout.isErrorEnabled = true
        textInputLayout.error = error
    }

    private fun startField(width: Int, height: Int) = startActivity(
        Intent(
            applicationContext, FieldActivity::class.java
        ).apply {
            this.putExtra(
                "width", width
            )
            this.putExtra(
                "height", height
            )
        }
    )

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}