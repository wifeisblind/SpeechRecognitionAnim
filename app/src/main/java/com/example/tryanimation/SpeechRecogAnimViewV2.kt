package com.example.tryanimation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import kotlin.random.Random


/**
 * 當音量高過該閥值，動畫會劇烈變動
 */
const val THRESHOLD = 50

/**
 * 震幅的根數
 */
const val SIZE = 45

/**
 * 動畫的時間
 */
const val DURATION = 100L

class SpeechRecogAnimViewV2(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {


    private val paint: Paint = Paint().apply {
        style = STROKE
        color = Color.parseColor("#D62872")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val seg = width.toFloat() / (SIZE + 1)

        val halfHeight = height.shr(1).toFloat()

        val p = SIZE / 6

        for ((index, wPath) in wPaths.withIndex()) {
            wPath.init {
                x = (index + 1) * seg
                y = halfHeight
                minAmp = halfHeight * 0.1f
                offset = when(index) {
                    in 0 until p, in (SIZE - p) until SIZE -> halfHeight * 0.1f
                    in p until p*2, in (SIZE - p*2) until (SIZE - p) -> halfHeight * 0.5f
                    else -> halfHeight * 0.8f
                }
            }
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    private var wPaths: List<WPath> = List(SIZE) { WPath(paint) }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        wPaths.forEach { path ->
            path.onDraw(canvas)
        }
        canvas.restore()
    }

    private var isStop: Boolean = false

    fun startAnim() {
        isStop = false
        startRecursiveAnim()
    }

    private fun startRecursiveAnim() {
        AnimatorSet().apply {

            var builder: AnimatorSet.Builder? = null
            for ((index, path) in wPaths.withIndex()) {

                val anim = path.createAnimator(volume).apply {
                    addUpdateListener { invalidate() }
                }
                if (index == 0) {
                    builder = play(anim)
                } else  {
                    builder?.with(anim)
                }
            }
            interpolator = LinearInterpolator()
            duration = DURATION

            doOnEnd {
                if (isStop) return@doOnEnd
                startRecursiveAnim()
            }
        }.start()
    }

    fun stopAnim() {
        isStop = true
    }

    var volume: Int = 40
        set(value) {
            field = if (value > THRESHOLD) 100 else 40
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
                field = minAmp + value
            }

        var lastKeyFrame: Float = 0f

        fun createAnimator(upperBound: Int): ValueAnimator {

            val factor = Random.nextInt(0, upperBound) / 100f
            val keyFrame = offset * factor

            return ObjectAnimator.ofFloat(this, "amp",
                    lastKeyFrame,
                    keyFrame
            ).apply {
                doOnStart { lastKeyFrame = keyFrame }
            }
        }

        fun onDraw(canvas: Canvas) {
            canvas.drawPath(runtimePath, paint)
        }
    }
}