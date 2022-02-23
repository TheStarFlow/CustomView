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
class ReadHeavyMark: IMark {
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
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x+(charWidth/2),y+(charHeight/3),4f,paint)
        paint.style = style
    }
}