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

        super.onLayout(changed, left, top, right, bottom)
    }

    private val pivots: MutableList<Float> = mutableListOf()

    var amp1: Float = 10f

    var amp2: Float = 80f

    private val paths: List<Path> = List(SIZE) { Path() }

    private val halfHeight: Int
        get() = height.shr(1)

    private val runtimePaths: List<Path>
        get() {
            return paths.mapIndexed { index, path ->
                val x = pivots[index]
                path.reset()
                if (index % 2 == 0) {
                    path.moveTo(x, halfHeight + amp1)
                    path.lineTo(x, halfHeight - amp1)
                } else {
                    path.moveTo(x, halfHeight + amp2)
                    path.lineTo(x, halfHeight - amp2)
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

    private val anim1: ObjectAnimator = ObjectAnimator.ofFloat(this, "amp1", 10f, 80f, 10f).apply(initAnim)
    private val anim2: ObjectAnimator = ObjectAnimator.ofFloat(this, "amp2",80f, 10f, 80f).apply(initAnim)

    fun startAnim() {

        AnimatorSet().apply {
            play(anim1).with(anim2)
            interpolator = LinearInterpolator()
            duration = 1000
        }.start()
    }

    companion object{
        private const val SIZE = 30
    }
}