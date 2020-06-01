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



class SpeechRecogAnimView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {


    private val paint: Paint = Paint().apply {
        style = STROKE
        color = Color.parseColor("#D62872")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val highBounceFactors: List<Float> = List(20) { index ->
        if (index > 10) {
            Random.nextInt(0, 30) / 100f
        } else {
            Random.nextInt(80, 100) / 100f
        }
    }

    private val lowBounceFactors: List<Float> = List(20) {
        Random.nextInt(0, 40) / 100f
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

                val anim = path.createAnimator(factorPool).apply {
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

    private var factorPool: List<Float> = lowBounceFactors

    var volume: Int = 0
        set(value) {
            factorPool = if (value > THRESHOLD) highBounceFactors else lowBounceFactors
            field = value
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

        fun createAnimator(factorPool: List<Float>): ValueAnimator {

            val factor = factorPool[Random.nextInt(0, factorPool.lastIndex)]
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