package com.zzs.customview.text

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import com.zzs.customview.R

/**
@author  zzs
@Date 2021/9/2
@describe
 */
class MongolianText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private const val Rotation = 90f
        private const val STATE_NORMAL = 0
        private const val STATE_COUNTDOWN = 1

    }

    private var state = STATE_NORMAL

    private var countDownTimer: CountDownTimer? = null

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = currentTextColor
        textSize = this@MongolianText.textSize
        style = Paint.Style.STROKE
    }


    private var centerX = 0f
    private var centerY = 0f
    private var mWidth = 0
    private var mHeight = 0

    private var mGravity = 0
    var multiColumn = false
        set(value) {
            field = value
            requestLayout()
        }
    private var singleWords: List<String>? = null
    var mongolianText: String? = ""
        set(value) {
            field = value
            initWord()
            requestLayout()
        }

    private val textBoundRect = Rect()
    private val mViewRect = Rect()

    private var countDown = 0
    private var isMongolian = true

    fun startCountDown(s: Int) {
        state = STATE_COUNTDOWN
        isEnabled = false
        countDownTimer?.cancel()
        countDown = s
        countDownTimer = object : CountDownTimer(s * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                countDown = ((millisUntilFinished / 1000).toInt())
                invalidate()
            }

            override fun onFinish() {
                state = STATE_NORMAL
                invalidate()
                isEnabled = true

            }

        }
        countDownTimer?.start()
    }

    init {
        mongolianText = text.toString()
        handleTa(context, attrs)
        initWord()
        mPaint.color = currentTextColor
        mPaint.textSize = this@MongolianText.textSize
        mPaint.style = Paint.Style.STROKE
        if (isMongolian) {
            mPaint.typeface = Typeface.createFromAsset(context.assets, "MGQ8102.ttf")
        }
    }

    private fun handleTa(context: Context, attrs: AttributeSet?) {
        attrs?.run {
            var ta: TypedArray? = null
            try {
                ta = context.obtainStyledAttributes(attrs, R.styleable.MongolianText)
                multiColumn = ta.getBoolean(R.styleable.MongolianText_multiColumn, false)
                isMongolian = ta.getBoolean(R.styleable.MongolianText_isMongolian, true)
                mGravity = ta.getInt(R.styleable.MongolianText_android_gravity, 0)
            } finally {
                ta?.recycle()
            }
        }

    }

    override fun onDraw(canvas: Canvas?) {
        mPaint.color = currentTextColor
        canvas ?: return
        mongolianText ?: return
        when (state) {
            STATE_NORMAL -> {
                if (multiColumn) {
                    singleWords ?: return
                    if (singleWords.isNullOrEmpty()) return
                    drawMultiColumnText(canvas)
                } else {
                    canvas.save()
                    canvas.translate(centerX, centerY)
                    val textLen = mPaint.measureText(mongolianText)
                    if (textLen > mHeight) {
                        canvas.translate(0f, (textLen - mHeight) / 2f)
                    }
                    canvas.rotate(Rotation)
                    drawTextOnCenter(canvas, mPaint, mongolianText!!)
                    canvas.restore()
                }
            }
            STATE_COUNTDOWN -> {
                canvas.save()
                canvas.translate(centerX, centerY)
                drawTextOnCenter(canvas, mPaint, countDown.toString())
                val fontMetrics: Paint.FontMetrics = mPaint.fontMetrics
                val top: Float = fontMetrics.top
                val bottom: Float = fontMetrics.bottom
                val textY = -(top + bottom) / 2 + 5
                val width = mPaint.measureText(countDown.toString())
                canvas.drawLine(-width / 2f, textY, width / 2f, textY, mPaint)
                canvas.restore()
            }
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
        valueAnimator?.cancel()
    }

    private fun initWord() {
        singleWords = mongolianText?.split(" ")
        singleWords = singleWords?.filter { !TextUtils.isEmpty(it) }
        if (multiColumn) {
            val sb = StringBuilder()
            val dst = mutableListOf<String>()
            singleWords?.run {
                for (index in indices) {
                    if (index == lastIndex) {
                        dst.add(get(index))
                        break
                    }
                    sb.clear()
                    sb.append(get(index)).append(" ")
                    dst.add(sb.toString())
                }
            }
            singleWords = dst
        }
    }

    private fun drawMultiColumnText(canvas: Canvas) {
        val words = singleWords
        canvas.save()
        words?.run {
            val rect = calc(this)
            canvas.translate(centerX, centerY)
            //canvas.drawPoint(0f, 0f, mPaint)
            canvas.rotate(90f)
            var drawX = 0 - rect.centerX()
            var drawY = 0 - rect.centerY()
            var first = true
            var maxWith = 0
            if (mGravity == Gravity.TOP) {
                drawX -= (mViewRect.height() - rect.width()) / 2
            }
            words.forEach { word ->
                val size = measureText(word)
                if (first) {
                    drawY += rect.height()
                    first = false
                }
                if (maxWith + size.width() > height) {
                    drawX = 0 - rect.centerX()
                    maxWith = 0
                    drawY -= (size.height() + lineSpacingExtra.toInt())
                }
                canvas.drawText(word, drawX.toFloat(), drawY.toFloat(), mPaint)
                drawX += size.width()
                maxWith += size.width()
            }
        }
        canvas.restore()

    }

    private fun calc(list: List<String>): Rect {
        mPaint.textSize = textSize
        var mX = 0
        var mY = 0
        var mMaxX = 0
        list.forEach { word ->
            val size = measureText(word)
            if (mY == 0) {
                mY += size.height()
            }
            if (mX + size.width() > height) {
                mX = 0
                mY += size.height() + lineSpacingExtra.toInt()
            }
            mX += size.width()
            if (mX > mMaxX) {
                mMaxX = mX
            }
        }
        return Rect(0, 0, mMaxX, mY)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
    }

    private fun drawTextOnCenter(canvas: Canvas, paint: Paint, text: CharSequence) {
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = textSize
        val fontMetrics: Paint.FontMetrics = paint.fontMetrics
        val top: Float = fontMetrics.top
        val bottom: Float = fontMetrics.bottom
        val textY = -(top + bottom) / 2
        when (gravity) {
            Gravity.TOP -> {
                val rect = measureText(mongolianText)
                val dx = (mViewRect.width() - rect.width()) / 2f
                canvas.drawText(text, 0, text.length, 0f - dx, textY, paint)
            }
            Gravity.START -> {

            }
            Gravity.BOTTOM -> {
//                val rect = measureText(mongolianText)
//                val textRectCenterDis = rect.width() / 2f
//                val dx = textRectCenterDis - centerY
//                canvas.drawText(text, 0, text.length, 0f - dx, textY, paint)
            }
            Gravity.END -> {

            }
            else -> {//默认居中、
                canvas.drawText(text, 0, text.length, 0f, textY, paint)

            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val rect = measureText(mongolianText)
        var isExactly = false
        mWidth = if (widthMode == MeasureSpec.EXACTLY) {
            isExactly = true
            widthSize
        } else {
            rect.height()
        }
        mHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            rect.width()
        }
        val textWidth = rect.width()
        if (textWidth > mHeight && multiColumn && !isExactly) {
            val line = (textWidth / mHeight + if (textWidth % mHeight > 0) 1 else 0)
            mWidth *= line
            mWidth += line * lineSpacingExtra.toInt()
        }
        mViewRect.set(0, 0, mWidth, mHeight)
        setMeasuredDimension(mWidth, mHeight)

    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        mongolianText = text.toString()
        initWord()
    }

    private var valueAnimator: ValueAnimator? = null
    private var scrollDis = 0f


    fun startScroller() {
        if (multiColumn) return
        if (state == STATE_COUNTDOWN) return
        val textLen = mPaint.measureText(mongolianText)
        if (textLen < mHeight) return
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 10000
            interpolator = LinearInterpolator()

        }
        valueAnimator?.addUpdateListener {
            val current = it.animatedValue as Float
            scrollDis = current * (textLen + mHeight) - mHeight
            scrollTo(0, scrollDis.toInt())

        }
        valueAnimator?.repeatMode = ValueAnimator.RESTART
        valueAnimator?.repeatCount = ValueAnimator.INFINITE
        valueAnimator?.start()

    }

    fun reset() {
        valueAnimator?.cancel()
    }


    private fun measureText(content: String?): Rect {
        mPaint.textSize = textSize
        content ?: return textBoundRect.apply {
            set(0, 0, 0, 0)
        }
        textBoundRect.set(0, 0, 0, 0)
        mPaint.getTextBounds(content, 0, content.length, textBoundRect)
        val s = mPaint.measureText(content)
        return textBoundRect.apply {
            set(0, top, s.toInt(), bottom)
        }
    }
}