package io.github.kaczmarek.animationshadow

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnNextLayout
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvMain = findViewById<TextView>(R.id.tvMainDescription)
        tvMain.text = getGreetingMessage()
        tvMain.doOnNextLayout {
            val colors = intArrayOf(
                    ContextCompat.getColor(this, R.color.color_blue),
                    ContextCompat.getColor(this, R.color.color_pink),
                    ContextCompat.getColor(this, R.color.color_blue)
            )
            val cornerRadius = 16f.toPx()
            val padding = 30.toPx()
            val centerX = it.width.toFloat() / 2 - padding
            val centerY = it.height.toFloat() / 2 - padding

            val shadowDrawable = createBackgroundDrawable(
                    colors = colors,
                    cornerRadius = cornerRadius,
                    elevation = padding / 2f,
                    centerX = centerX,
                    centerY = centerY
            )

            it.setColorShadowBackground(
                    shadowDrawable = shadowDrawable,
                    colorDrawable = createForegroundDrawable(cornerRadius),
                    padding = padding.toInt()
            )

            val endColors = intArrayOf(
                    ContextCompat.getColor(this, R.color.color_pink),
                    ContextCompat.getColor(this, R.color.color_blue),
                    ContextCompat.getColor(this, R.color.color_pink)
            )
            animateShadow(
                    shapeDrawable = shadowDrawable,
                    startColors = colors,
                    endColors = endColors,
                    centerX = centerX,
                    centerY = centerY
            )
        }
    }

    private fun createBackgroundDrawable(
            @ColorInt colors: IntArray,
            cornerRadius: Float,
            elevation: Float,
            centerX: Float,
            centerY: Float
    ): ShapeDrawable {
        val shadowDrawable = ShapeDrawable()
        with(shadowDrawable.paint) {
            setShadowLayer(elevation, 0f, 0f, Color.BLACK)
            shader = SweepGradient(centerX, centerY, colors, null)
        }

        val outerRadius = FloatArray(8) { cornerRadius }
        shadowDrawable.shape = RoundRectShape(outerRadius, null, null)

        return shadowDrawable
    }

    private fun createForegroundDrawable(cornerRadius: Float) = GradientDrawable().apply {
        setColor(ContextCompat.getColor(this@MainActivity, R.color.color_foreground))
        setCornerRadius(cornerRadius)
    }

    private fun View.setColorShadowBackground(
            shadowDrawable: ShapeDrawable,
            colorDrawable: Drawable,
            padding: Int
    ) {
        val drawable = LayerDrawable(arrayOf(shadowDrawable, colorDrawable))
        drawable.setLayerInset(0, padding, padding, padding, padding)
        drawable.setLayerInset(1, padding, padding, padding, padding)
        setPadding(padding, padding, padding, padding)
        background = drawable
    }

    private fun animateShadow(
            shapeDrawable: ShapeDrawable,
            @ColorInt startColors: IntArray,
            @ColorInt endColors: IntArray,
            centerX: Float,
            centerY: Float
    ) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            val invalidateDelay = 100
            var deltaTime = System.currentTimeMillis()
            val mixedColors = IntArray(startColors.size)

            addUpdateListener { animation ->
                if (System.currentTimeMillis() - deltaTime > invalidateDelay) {
                    val animatedFraction = animation.animatedValue as Float
                    deltaTime = System.currentTimeMillis()

                    for (i in 0..mixedColors.lastIndex) {
                        mixedColors[i] = ColorUtils.blendARGB(startColors[i], endColors[i], animatedFraction)
                    }
                    shapeDrawable.paint.shader = SweepGradient(
                            centerX,
                            centerY,
                            mixedColors,
                            null
                    )
                    shapeDrawable.invalidateSelf()
                }
            }
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
            duration = 1500L
            start()
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()

        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> getString(R.string.common_morning)
            in 12..15 -> getString(R.string.common_afternoon)
            in 16..20 -> getString(R.string.common_evening)
            in 21..23 -> getString(R.string.common_night)
            else -> getString(R.string.common_hello)
        }
    }

    private fun Float.toPx(): Float {
        return this * this@MainActivity.resources.displayMetrics.density
    }

    private fun Int.toPx(): Float {
        return this * this@MainActivity.resources.displayMetrics.density
    }
}