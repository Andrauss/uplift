package me.seebrock3r.elevationtester

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.alpha
import kotlinx.android.synthetic.main.activity_color_picker.*
import me.seebrock3r.elevationtester.widget.BetterSeekListener

class ColorPickerActivity : AppCompatActivity() {

    private val selectedColor: Int
        get() = dialogColorWheel.selectedColor.setAlphaTo(dialogColorAlpha.progress)

    private val initialColor: Int
        get() = intent.getIntExtra(EXTRA_COLOR, Color.BLACK)

    private var changingBrightnessFromCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)

        setResult(Activity.RESULT_CANCELED)

        DialogLayoutParameters.wrapHeight(this)
            .applyTo(window)

        dialogTitle.text = intent.getStringExtra(EXTRA_TITLE)

        val color = initialColor
        setupBrightnessControls(color)
        setupAlphaControls()
        setupColorWheel()

        dialogColorWheel.setColor(color)

        dialogClose.setOnClickListener { finish() }
    }

    private fun setupBrightnessControls(color: Int) {
        dialogColorAlpha.progress = color.alpha

        dialogColorAlpha.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    dialogColorPreview.backgroundTintList = ColorStateList.valueOf(dialogColorWheel.selectedColor.setAlphaTo(progress))
                    val alpha = progress / dialogColorAlpha.max.toFloat()
                    dialogAlphaValue.text = "%.2f".format(alpha)
                }
            }
        )
    }

    private fun setupAlphaControls() {
        dialogColorBrightness.setOnSeekBarChangeListener(
            object : BetterSeekListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (changingBrightnessFromCode) {
                        return
                    }

                    val brightness = progress / dialogColorBrightness.max.toFloat()
                    dialogColorWheel.setBrightness(brightness)
                    dialogBrightnessValue.text = "%.2f".format(brightness)
                }
            }
        )
    }

    private fun setupColorWheel() {
        dialogColorWheel.onColorChangedListener = {
            changingBrightnessFromCode = true
            dialogColorBrightness.progress = (it.brightness * dialogColorBrightness.max).toInt()
            changingBrightnessFromCode = false

            dialogColorPreview.backgroundTintList = ColorStateList.valueOf(it)

            setResult(Activity.RESULT_OK, Intent().apply { putExtra(EXTRA_COLOR, selectedColor) })
        }
    }

    companion object {
        private const val EXTRA_TITLE = "ColorPickerActivity_title"
        private const val EXTRA_COLOR = "ColorPickerActivity_color"
        private const val EXTRA_ORIGIN_BOUNDS = "ColorPickerActivity_origin_bounds"

        fun createIntent(context: Context, title: String, @ColorInt color: Int, originBounds: Rect) =
            Intent(context, ColorPickerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_COLOR, color)
                putExtra(EXTRA_ORIGIN_BOUNDS, originBounds)
            }

        @ColorInt
        fun extractResultFrom(resultData: Intent?) =
            resultData?.getIntExtra(EXTRA_COLOR, Color.BLACK)
    }
}
