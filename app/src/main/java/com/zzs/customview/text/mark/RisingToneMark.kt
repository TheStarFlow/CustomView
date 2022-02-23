package com.gzhlsoft.yougureader.widget.evaluation.mark

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.gzhlsoft.yougureader.widget.evaluation.IMark

/**
@author  zzs
@Date 2022/2/11
@describe
 */
class RisingToneMark: IMark {
    private var rising:Bitmap?=null

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
        rising?.run {
            if (isRecycled)recycle()
            canvas.drawBitmap(this,x+charWidth,y-charHeight+this.height,paint)
        }
    }

    fun setBitmap(rising:Bitmap){
        this.rising = rising
    }
}