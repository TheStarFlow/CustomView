package com.gzhlsoft.yougureader.widget.evaluation.mark

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.gzhlsoft.yougureader.widget.evaluation.IMark

/**
@author  zzs
@Date 2022/2/11
@describe
 */
class CReadingMark : IMark {

    override fun draw(
        context: Context,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint,
        charWidth: Float,
        charHeight: Float,
        CHAR_SPACE: Float
    ) {
        val style = paint.style
        paint.style = Paint.Style.STROKE
        val strokeWid = paint.strokeWidth
        paint.strokeWidth = 3f
        val left = x-CHAR_SPACE-charWidth/2
        val top = y-charHeight
        val right = x+charWidth/2f
        val bottom = y -charHeight+charHeight/2
        canvas.drawArc(left,top,right,bottom,-180f,180f,false,paint)
        paint.style = style
        paint.strokeWidth = strokeWid
    }
}