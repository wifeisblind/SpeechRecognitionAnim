package com.example.tryanimation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.graphics.Color.parseColor
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

class SpeechRecogAnimView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint().apply {
        style = STROKE
        color = parseColor("#D62872")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private var rect: Rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rect.set(0, 0, width, height)
        val seg = width.toFloat() / (SIZE + 1)
        val halfHeight = height.shr(1).toFloat()

        wPaths.forEachIndexed { index, wPath ->
            wPath.init {
                x = (index + 1) * seg
                y = halfHeight
                minAmp = halfHeight * 0.1f
                offset = when(index) {
                    in 0..5, in 24..29 -> halfHeight * 0.1f
                    in 6..10, in 19..23 -> halfHeight * 0.5f
                    in 11..18 -> halfHeight * 0.8f
                    else -> 0f
                }
            }
        }

        super.onLayout(changed, left, top, right, bottom)
    }

    private var wPaths: List<WPath> = List(SIZE) {
        WPath(paint)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.drawRect(rect, paint)
        wPaths.forEach { path ->
            path.draw(canvas)
        }
        canvas.restore()
    }

    fun startAnim() {
        AnimatorSet().apply {
            var builder: AnimatorSet.Builder? = null
            for ((index, path) in wPaths.withIndex()) {
                val anim = path.createAnimator(weight).apply {
                    addUpdateListener { invalidate() }
                }
                if (index == 0) {
                    builder = play(anim)
                } else {
                    builder?.with(anim)
                }
            }
            interpolator = LinearInterpolator()
            duration = 100L

            doOnEnd { startAnim() }
        }.start()
    }

    private var weight: Int = 40

    fun setAmpWeight(weight: Int) {
        this.weight = weight
    }


    companion object{
        private const val SIZE = 30
    }

    class WPath(
        private val paint: Paint
    ) {

        var x: Float = 0f
        var y: Float = 0f
        var minAmp: Float = 0f
        var offset: Float = 0f

        private val path: Path = Path()

        private val runtimePath: Path
            get() = path.apply {
                reset()
                moveTo(x, y + amp)
                lineTo(x, y - amp)
            }

        fun init(block: WPath.() -> Unit) {
            this.apply(block)
        }

        var amp: Float = 0f
            set(value) {
                field = minAmp + value * offset
            }

        private var lastKeyFrame: Float = 1f

        fun createAnimator(weight: Int): ValueAnimator {

                val keyframe = Random.nextInt(1, weight) / 100f

                return ObjectAnimator.ofFloat(this, "amp",
                    lastKeyFrame,
                    keyframe
                ).apply {
                    doOnStart { lastKeyFrame = keyframe }
                }
            }

        fun draw(canvas: Canvas) {
            canvas.drawPath(runtimePath, paint)
        }
    }
}