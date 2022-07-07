package com.zzs.keyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.inputmethodservice.Keyboard
import android.util.Log
import android.view.inputmethod.InputConnection
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.lang.StringBuilder

/**
@author  zzs
@Date 2022/7/6
@describe
 */
class KeyboardAction(
     var mConn: InputConnection?,
    private val mImWrapper: IMWrapper?,
    private val mManagerWrapper: IMManagerWrapper?
) : MyKeyboardView.OnKeyboardActionListener, MyKeyboardView.OnKeyDrawListener {
    companion object {
        const val KEY_ACTION_UPPER = -1
        const val KEY_ACTION_DEL = -5
        const val KEY_ABC_NUM_SWITCH = -2
        const val KEY_CONFIRM = -3
        const val KEY_SPACE = 120

        private val FILTER_KEY = mutableListOf(
            KEY_ACTION_DEL, KEY_ACTION_UPPER, KEY_ABC_NUM_SWITCH, KEY_CONFIRM
        )
    }

    private val mInputTextBuilder by lazy { StringBuilder() }

    override fun onPress(primaryCode: Int) {
        Log.i("KeyboardAction","primaryCode: $primaryCode")
        val previewEnable = primaryCode !in FILTER_KEY
        mManagerWrapper?.onPreviewChange(previewEnable)
    }

    override fun onRelease(primaryCode: Int) {

    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            KEY_CONFIRM -> {
                mImWrapper?.doHideWindow()
            }
            KEY_ACTION_DEL -> {
                mConn?.deleteSurroundingText(1,0)
            }
            KEY_ACTION_UPPER -> {
                when (mManagerWrapper?.getCurrKeyBoardType()) {
                    KeyBoardManager.KB_TYPE_NUM_ABC_LOW -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_ABC_UPPER)
                    }
                    KeyBoardManager.KB_TYPE_NUM_ABC_UPPER -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_ABC_LOW)
                    }
                    KeyBoardManager.KB_TYPE_NUM_SYMBOL_1 -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_SYMBOL_2)
                    }
                    KeyBoardManager.KB_TYPE_NUM_SYMBOL_2 -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_SYMBOL_1)
                    }
                }
            }
            KEY_ABC_NUM_SWITCH -> {
                when (mManagerWrapper?.getCurrKeyBoardType()) {
                    KeyBoardManager.KB_TYPE_NUM_ABC_LOW,
                    KeyBoardManager.KB_TYPE_NUM_ABC_UPPER -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_SYMBOL_1)
                    }
                    KeyBoardManager.KB_TYPE_NUM_SYMBOL_1,
                    KeyBoardManager.KB_TYPE_NUM_SYMBOL_2 -> {
                        mManagerWrapper.onSwitchKeyboard(KeyBoardManager.KB_TYPE_NUM_ABC_LOW)
                    }
                }
            }
        }
    }

    override fun onText(text: CharSequence?) {
        mConn?.commitText(text,1)
    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {

    }

    override fun swipeDown() {
        mImWrapper?.doHideWindow()
    }

    override fun swipeUp() {

    }

    /**
     * draw my own key
     * */
    override fun onDrawKey(
        context: Context,
        key: Keyboard.Key,
        canvas: Canvas?,
        paint: Paint?,
        keyBackground: Drawable?,
        padding: Rect,
        parentPaddingLeft: Int,
        parentPaddingTop: Int,
        keyTextSize: Int,
        labelTextSize: Int
    ): Boolean {
        val code = key.codes?.get(0) ?: return false
        if (code !in FILTER_KEY) return false
        if (newBackground == null) {
            newBackground = keyBackground?.constantState?.newDrawable()
        }
        val kb = newBackground
        kb?.run {
            if (kb is LayerDrawable) {
                val top = kb.findDrawableByLayerId(R.id.btn_background_top)
                DrawableCompat.setTint(
                    top,
                    ContextCompat.getColor(context, R.color.gray_d9d9d9)
                )
            }
            val bounds = bounds
            if (key.width != bounds.right ||
                key.height != bounds.bottom
            ) {
                setBounds(0, 0, key.width, key.height)
            }
        }
        canvas?.run {
            translate(
                (key.x + parentPaddingLeft).toFloat(),
                (key.y + parentPaddingTop).toFloat()
            )
            drawMyKeyBackground(canvas, kb)
            drawMyKey(canvas, code, key, context, paint, padding, keyTextSize, labelTextSize)
            canvas.translate(
                (-key.x - parentPaddingLeft).toFloat(),
                (-key.y - parentPaddingTop).toFloat()
            )
        }
        return true
    }

    private fun drawMyKeyBackground(canvas: Canvas, kb: Drawable?) {
        kb?.draw(canvas)
    }

    private var delBmp: Bitmap? = null
    private var newBackground: Drawable? = null
    private val delRect = Rect(0, 0, 44, 29)
    private val caseRect = Rect(0, 0, 34, 32)
    private var lowerCaseBmp: Bitmap? = null
    private var upperCaseBmp: Bitmap? = null

    private fun drawMyKey(
        canvas: Canvas,
        code: Int,
        key: Keyboard.Key,
        context: Context,
        paint: Paint?,
        padding: Rect,
        keyTextSize: Int,
        labelTextSize: Int
    ) {
        val currKbType = mManagerWrapper?.getCurrKeyBoardType()
        when (code) {
            KEY_ACTION_UPPER -> {
                if (currKbType == KeyBoardManager.KB_TYPE_NUM_ABC_UPPER ||
                    currKbType == KeyBoardManager.KB_TYPE_NUM_ABC_LOW
                ) {
                    val isUppercase = mManagerWrapper?.isUpperCase() ?: false
                    val bitmap = if (isUppercase) {
                        if (upperCaseBmp == null) {
                            upperCaseBmp =
                                BitmapFactory.decodeResource(context.resources, R.mipmap.uppercase)
                        }
                        upperCaseBmp
                    } else {
                        if (lowerCaseBmp == null) {
                            lowerCaseBmp =
                                BitmapFactory.decodeResource(context.resources, R.mipmap.lowcase)
                        }
                        lowerCaseBmp
                    }
                    val x = (key.width - caseRect.width()) / 2
                    val y = (key.height - caseRect.height()) / 2
                    caseRect.set(x, y, x + caseRect.width(), y + caseRect.height())
                    canvas.drawBitmap(bitmap!!, null, caseRect, paint)
                } else {
                    var currLabel = ""
                    if (currKbType == KeyBoardManager.KB_TYPE_NUM_SYMBOL_1) {
                        key.label = "~|￥"
                        currLabel = key.label.toString()
                    }
                    if (currKbType == KeyBoardManager.KB_TYPE_NUM_SYMBOL_2) {
                        key.label = "@*#"
                        currLabel = key.label.toString()
                    }
                    paint?.textSize = labelTextSize.toFloat()
                    drawKeyText(key, canvas, padding, paint, currLabel)
                }
            }
            KEY_ACTION_DEL -> {
                if (delBmp == null) {
                    delBmp = BitmapFactory.decodeResource(context.resources, R.mipmap.del)
                }
                val x = (key.width - delRect.width()) / 2
                val y = (key.height - delRect.height()) / 2
                delRect.set(x, y, x + delRect.width(), y + delRect.height())
                canvas.drawBitmap(delBmp!!, null, delRect, paint)
            }
            KEY_ABC_NUM_SWITCH -> {
                var currLabel: CharSequence = ""
                if (currKbType == KeyBoardManager.KB_TYPE_NUM_ABC_UPPER
                    || currKbType == KeyBoardManager.KB_TYPE_NUM_ABC_LOW
                ) {
                    key.label = "?123"
                    currLabel = key.label
                }
                if (currKbType == KeyBoardManager.KB_TYPE_NUM_SYMBOL_1
                    || currKbType == KeyBoardManager.KB_TYPE_NUM_SYMBOL_2
                ) {
                    key.label = "ABC"
                    currLabel = key.label
                }
                paint?.textSize = labelTextSize.toFloat()
                drawKeyText(key, canvas, padding, paint, currLabel.toString())
            }
            KEY_CONFIRM -> {
                paint?.textSize = labelTextSize.toFloat()
                drawKeyText(key, canvas, padding, paint, "确定")
            }
        }
    }

    private fun drawKeyText(
        key: Keyboard.Key,
        canvas: Canvas,
        padding: Rect,
        paint: Paint?,
        text: String
    ) {
        canvas.drawText(
            text, (
                    (key.width - padding.left - padding.right) / 2
                            + padding.left).toFloat(),
            (key.height - padding.top - padding.bottom) / 2 + (paint!!.textSize - paint.descent()) / 2 + padding.top,
            paint
        )
    }
}