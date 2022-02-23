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
class NoneMark: IMark {
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

    }
}