package com.gzhlsoft.yougureader.widget.evaluation.mark

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.gzhlsoft.yougureader.widget.evaluation.IMark

/**
@author  zzs
@Date 2022/2/11
@describe
 */
class RhymeMark: IMark {

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
        val path = Path()
        val style = paint.style
        val strokeWidth = paint.strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        path.moveTo(x+(charWidth/2),y+(charHeight/5))
        path.rLineTo(-5f,8.6f)
        path.rLineTo(10f,0f)
        path.close()
        canvas.drawPath(path,paint)
        paint.style = style
        paint.strokeWidth = strokeWidth
    }
}