package com.example.tryanimation

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.content.Context
import android.graphics.*
import android.graphics.Color.CYAN
import android.graphics.Paint.Style.STROKE
import android.util.AttributeSet
import android.view.View
import java.util.jar.Attributes

class SpeechRecogAnimView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {


    private val paint: Paint = Paint().apply {
        style = STROKE
        color = CYAN
        strokeWidth = 10f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private var rect: Rect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rect.set(0, 0, width, height)
        val seg = width.toFloat() / (SIZE + 1)
        for (i in 1 until SIZE + 1) {
            pivots.add(seg * i)
        }

        minAmp = height * 0.1f
        maxAmp = height * 0.9f

        super.onLayout(changed, left, top, right, bottom)
    }

    private val pivots: MutableList<Float> = mutableListOf()

    private var minAmp: Float = 0f

    private var maxAmp: Float = 0f

    private var amp: Float = 10f

    private val paths: List<Path> = List(SIZE) { Path() }

    private val halfHeight: Int
        get() = height.shr(1)

    private val runtimePaths: List<Path>
        get() {
            return paths.mapIndexed { index, path ->
                val x = pivots[index]
                path.reset()
                path.moveTo(x, halfHeight + amp)
                path.lineTo(x, halfHeight - amp)
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

    lateinit var anim: ValueAnimator

    fun startAnim() {
        anim = ValueAnimator.ofFloat(10f, 80f, 10f).apply {
            addUpdateListener {
                amp = it.animatedValue as Float
                invalidate()
            }
            duration = 1000
            repeatMode = REVERSE
            repeatCount = INFINITE
        }
        anim.start()
    }

    companion object{
        private const val SIZE = 20
    }
}