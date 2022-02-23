package com.zzs.customview.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
@author  zzs
@Date 2022/2/18
@describe
 */
class EvaLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var progressBgColor = Color.parseColor("#1C096D")
        set(value) {
            field = value
            invalidate()
        }
    var progressColor = Color.parseColor("#89F7FF")
        set(value) {
            field = value
            invalidate()
        }

    var progress = 60
        set(value) {
            field = value
            invalidate()
        }
    private var range = 0..100
    var space = 32f
        set(value) {
            field = value
            invalidate()
        }
    var strokeWidth = 4f
        set(value) {
            field = value
            mPaint.strokeWidth = field
            invalidate()
        }
    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val srcTop = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    private val path = Path()

    private val mProgressRect = RectF()

    init {
        mPaint.strokeWidth = strokeWidth
    }

    private fun drawBg(canvas: Canvas) {
        mPaint.color = progressBgColor
        mPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width * 1f, height * 1f, mPaint)
    }

    private fun drawProgress(canvas: Canvas) {
        val r = (width / (range.count() - 1).toFloat()) * progress
        mProgressRect.set(0f, 0f, r, height * 1f)
        mPaint.color = progressColor
        canvas.drawRect(mProgressRect, mPaint)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val layer = canvas.saveLayer(null, null)
        drawBg(canvas)
        val layer1 = canvas.saveLayer(null, null)
        drawBg(canvas)
        mPaint.xfermode = srcTop
        drawProgress(canvas)
        mPaint.xfermode = xfermode
        path.reset()
        path.moveTo(mProgressRect.right, 0f)
        path.rLineTo(0f, height * 1f)
        path.rLineTo(-space / 5f, 0f)
        path.close()
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(path, mPaint)
        canvas.restoreToCount(layer1)


        //斜线
        mPaint.xfermode = xfermode
        var startX = 0f
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.STROKE
        while (startX < width) {
            path.reset()
            path.moveTo(startX, height.toFloat())
            path.rLineTo(space / 5f, -height.toFloat())
            canvas.drawPath(path, mPaint)
            startX += space
        }

        //切三角
        path.reset()
        path.moveTo(0f, height * 1f)
        path.rLineTo(0f, -height * 1f)
        path.rLineTo(space / 5f, 0f)
        path.close()
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(path, mPaint)
        path.reset()
        path.moveTo(width * 1f, 0f)
        path.rLineTo(0f, height * 1f)
        path.rLineTo(-space / 5f, 0f)
        path.close()
        mPaint.style = Paint.Style.FILL
        canvas.drawPath(path, mPaint)
        mPaint.xfermode = null
        canvas.restoreToCount(layer)
    }
}