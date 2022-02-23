package com.zzs.customview.others

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView

/**
@author  zzs
@Date 2021/6/11
@describe 让图片某个地方显示高亮
 */
class HighLightRectImageView : AppCompatImageView {

    private val mPaint by lazy { Paint().apply {
        style = Paint.Style.FILL

    } }
    private val mode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val dstIn = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    private val mPath by lazy { Path() }
    var radiu:Float = 0f
    set(value) {
        highLightRect?.run {
            field = value
            mPath.reset()
            mPath.addRoundRect(this,radiu,radiu,Path.Direction.CW)
            invalidate()
        }
    }
    var coverColor = Color.TRANSPARENT
        set(value) {
            field = value
            invalidate()
        }

    var highLightRect: RectF? = null
        set(value) {
            field = value
            invalidate()
        }

    var highLightBitmap:Bitmap?=null
    set(value) {
        field = value
        invalidate()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val layer1 = canvas?.saveLayer(null,null)
        highLightBitmap?.run {
            if (highLightRect!=null){
                canvas?.drawBitmap(this,null,highLightRect!!,mPaint.apply {
                    xfermode=null
                })
            }
            canvas?.drawPath(mPath,mPaint.apply {
                xfermode = dstIn
            })
        }
        canvas?.restoreToCount(layer1!!)
        val layer = canvas?.saveLayer(null,null)
        canvas?.drawColor(coverColor)
        highLightRect?.run {
            mPaint.xfermode = mode
            canvas?.drawPath(mPath, mPaint)
        }
        canvas?.restoreToCount(layer!!)

    }
}