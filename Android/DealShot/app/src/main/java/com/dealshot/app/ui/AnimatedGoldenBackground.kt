package com.dealshot.app.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.sin

class AnimatedGoldenBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animationProgress = 0f
    private var animator: ValueAnimator? = null

    private val backgroundColor = Color.rgb(5, 5, 9)
    private val goldColor = Color.rgb(198, 143, 53)
    private val lightGold = Color.rgb(233, 195, 111)

    init {
        setBackgroundColor(backgroundColor)
        isClickable = false
        isFocusable = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0 || h <= 0) return

        canvas.drawColor(backgroundColor)

        // First golden glow
        drawGlow(
            canvas,
            w * (0.20f + 0.10f * sin(animationProgress)),
            h * 0.25f,
            w * 0.65f,
            goldColor,
            0.16f
        )

        // Second golden glow
        drawGlow(
            canvas,
            w * (0.80f - 0.10f * sin(animationProgress + 1f)),
            h * 0.60f,
            w * 0.70f,
            goldColor,
            0.13f
        )

        // Bottom ambient glow
        drawGlow(
            canvas,
            w * 0.50f,
            h * (0.90f + 0.04f * sin(animationProgress + 2f)),
            w * 0.75f,
            lightGold,
            0.08f
        )
    }

    private fun drawGlow(
        canvas: Canvas,
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        intensity: Float
    ) {
        val alpha = (255 * intensity).toInt()

        val centerColor = Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )

        glowPaint.shader = RadialGradient(
            x,
            y,
            radius,
            centerColor,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)

        glowPaint.shader = null
    }

    private fun startAnimation() {
        if (animator?.isRunning == true) return

        animator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
            duration = 18000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener {
                animationProgress = it.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)

        if (visibility == VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }
}

@Composable
fun AnimatedGoldenBackground(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AnimatedGoldenBackground(context)
        }
    )
}

