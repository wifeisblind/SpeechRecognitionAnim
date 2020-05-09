package com.example.tryanimation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.graphics.Color.CYAN
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

class SpeechRecogAnimView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {


    private val paint: Paint = Paint().apply {
        style = STROKE
        color = CYAN
        strokeWidth = 10f
    }

    private var rect: Rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rect.set(0, 0, width, height)
        val seg = width.toFloat() / (SIZE + 1)

        val halfHeight = height.shr(1).toFloat()

        wPaths.forEachIndexed { index, wPath ->
            wPath.init {
                x = (index + 1) * seg
                originY = halfHeight
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
        WPath(paint, object : ReadOnlyProperty<WPath, Float> {
            override fun getValue(thisRef: WPath, property: KProperty<*>): Float {
                return weight
            }
        })
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
            val builder = play(wPaths[0].animator.also(listener))
            for (path in wPaths.subList(1, wPaths.lastIndex)) {
                builder.with(path.animator.apply(listener))
            }
            interpolator = LinearInterpolator()
            duration = DURATION
        }.start()
    }

    private val listener: ValueAnimator.() -> Unit = {
        addUpdateListener { invalidate() }
    }

    private var weight: Float = 1f

    fun setAmpWeight(weight: Int) {
        this.weight = weight / 100f
    }

    companion object{
        private const val SIZE = 30
        private const val DURATION = 1000L
    }



    class WPath(
        private val paint: Paint,
        property: ReadOnlyProperty<WPath, Float>
    ) {

        var x: Float = 0f
        var originY: Float = 0f
        var minAmp: Float = 0f
        var offset: Float = 0f

        private val weight: Float by property

        private val path: Path = Path()

        private val runtimePath: Path
            get() = path.apply {
                reset()
                moveTo(x, originY + amp)
                lineTo(x, originY - amp)
            }

        fun init(block: WPath.() -> Unit) {
            this.apply(block)
        }

        var amp: Float = 0f
            set(value) {
                field = minAmp + value * offset * weight
            }

        val animator: ObjectAnimator by lazy {

            val randoms = List(4) { Random.nextInt(1, 100) / 100f }

            ObjectAnimator.ofFloat(this, "amp",
                randoms[0],
                randoms[1],
                randoms[2],
                randoms[3],
                randoms[0]
            ).apply {
                repeatCount = INFINITE
            }
        }

        fun draw(canvas: Canvas) {
            canvas.drawPath(runtimePath, paint)
        }
    }
}