package com.example.tryanimation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.content.Context
import android.graphics.*
import android.graphics.Color.CYAN
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.jar.Attributes

class SpeechRecogAnimView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint: Paint = Paint().apply {
        style = STROKE
        color = Color.parseColor("#D62872")
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private var rect: Rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rect.set(0, 0, width, height)
        val seg = width.toFloat() / (SIZE + 1)
        for (i in 1 until SIZE + 1) {
            pivots.add(seg * i)
        }

        minAmp = halfHeight * 0.1f
        offset = halfHeight * 0.8f
        super.onLayout(changed, left, top, right, bottom)
    }

    private var offset: Float = 0f

    private var minAmp: Float = 0f

    private val pivots: MutableList<Float> = mutableListOf()

    var amp1: Float = 0f
        set(value) {
            field = minAmp + value * offset * weight
        }

    var amp2: Float = 0f
        set(value) {
            field = minAmp + value * offset * weight
        }

    var amp3: Float = 0f
        set(value) {
            field = minAmp + value * offset * weight
        }

    var amp4: Float = 0f
        set(value) {
            field = minAmp + value * offset * weight
        }

    private val paths: List<Path> = List(SIZE) { Path() }

    private val halfHeight: Int
        get() = height.shr(1)

    private val runtimePaths: List<Path>
        get() {
            return paths.mapIndexed { index, path ->
                val x = pivots[index]
                path.reset()
                when(index % 4) {
                    0 -> {
                        path.moveTo(x, halfHeight + amp1)
                        path.lineTo(x, halfHeight - amp1)
                    }
                    1 -> {
                        path.moveTo(x, halfHeight + amp2)
                        path.lineTo(x, halfHeight - amp2)
                    }
                    2 -> {
                        path.moveTo(x, halfHeight + amp3)
                        path.lineTo(x, halfHeight - amp3)
                    }
                    3 -> {
                        path.moveTo(x, halfHeight + amp4)
                        path.lineTo(x, halfHeight - amp4)
                    }
                }
                path
            }
        }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.drawRect(rect, paint)
        runtimePaths.forEach {
            canvas.drawPath(it, paint)
        }
        canvas.restore()
    }

    private val initAnim: ValueAnimator.() -> Unit = {
        addUpdateListener {
            invalidate()
        }
        repeatCount = INFINITE
    }

    private val anim1: ObjectAnimator
        get() = ObjectAnimator.ofFloat(this, "amp1", 0f, 0.3f, 0.2f, 1f, 0f).apply(initAnim)

    private val anim2: ObjectAnimator
        get() = ObjectAnimator.ofFloat(this, "amp2", 1f, 0.6f, 0.8f, 0.3f, 1f).apply(initAnim)

    private val anim3: ObjectAnimator
        get() = ObjectAnimator.ofFloat(this, "amp3", 0.8f, 0.4f, 0.5f, 0.2f, 0.8f).apply(initAnim)

    private val anim4: ObjectAnimator
        get() = ObjectAnimator.ofFloat(this, "amp4", 0.3f, 0.7f, 0.1f, 1f, 0.3f).apply(initAnim)

    fun startAnim() {
        AnimatorSet().apply {
            play(anim1).with(anim2).with(anim3).with(anim4)
            interpolator = LinearInterpolator()
            duration = 1000
        }.start()
    }

    private var weight: Float = 1f

    fun setAmpWeight(weight: Int) {
        this.weight = weight / 100f
    }


    companion object{
        private const val SIZE = 30
    }
}