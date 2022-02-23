package com.gzhlsoft.yougureader.widget.evaluation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

/**
@author  zzs
@Date 2022/2/11
@describe
 */
interface IMark {
    /**
     * @param x 文字坐标
     * @param y 文字坐标
     *
     * */
    fun draw(
        context: Context,
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: Paint,
        charWidth: Float,
        charHeight: Float,
        CHAR_SPACE: Float
    )
}