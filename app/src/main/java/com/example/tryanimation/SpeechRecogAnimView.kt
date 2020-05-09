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
import android.view.View

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
        for (i in 1 until SIZE + 1) {
            pivots.add(seg * i)
        }

        minAmp = halfHeight * 0.1f
        offset = halfHeight * 0.8f

        super.onLayout(changed, left, top, right, bottom)
    }

    private val pivots: MutableList<Float> = mutableListOf()

    private var minAmp: Float = 0f

    private var offset: Float = 0f

    var amp1: Float = 0f
        set(value) {
            field = minAmp + value * offset * weight
        }

    var amp2: Float = 0f
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
        repeatMode = REVERSE
        repeatCount = INFINITE
    }

    private val factor1: ObjectAnimator = ObjectAnimator.ofFloat(this, "amp1", 0f, 1f, 0f).apply(initAnim)
    private val factor2: ObjectAnimator = ObjectAnimator.ofFloat(this, "amp2",1f, 0f, 1f).apply(initAnim)

    private val animSet: AnimatorSet = AnimatorSet().apply {
        play(factor1).with(factor2)
        duration = 1000
    }

    fun startAnim() {
        animSet.start()

    }

    private var weight: Float = 1f

    fun setAmpWeight(weight: Int) {
        this.weight = weight / 100f
    }

    companion object{
        private const val SIZE = 20
    }
}