package com.zzs.customview.progress

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.zzs.customview.R

/**
@author  zzs
@Date 2022/3/8
@describe
 */
/**
@author  zzs
@Date 2022/3/7
@describe
 */
class BeeLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mBgDrawable by lazy { RoundRectBackground() }
    private val mBitmapDrawable by lazy { BitmapDrawable(BitmapFactory.decodeResource(context.resources,R.mipmap.bee)) }
    var factor = 0.0f
        set(value) {
            field = value
            invalidate()
        }
    var marginOuter = 19.5f
        set(value) {
            field = value
            invalidate()
        }
    var marginInner = 6f
        set(value) {
            field = value
            invalidate()
        }
    var space = 35f
        set(value) {
            field = value
            invalidate()
        }
    var parWidth = 38f
        set(value) {
            field = value
            invalidate()
        }

    init {
        handleTa(context, attrs)
    }

    private fun handleTa(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BeeLoadingView)
        try {
            factor = ta.getFloat(R.styleable.BeeLoadingView_currFactor, factor)
            marginOuter =
                ta.getDimensionPixelSize(
                    R.styleable.BeeLoadingView_marginOuter,
                    marginOuter.toInt()
                ).toFloat()
            marginInner =
                ta.getDimensionPixelSize(
                    R.styleable.BeeLoadingView_marginInner,
                    marginInner.toInt()
                ).toFloat()
            space =
                ta.getDimensionPixelSize(R.styleable.BeeLoadingView_space, space.toInt()).toFloat()
            parWidth = ta.getDimensionPixelSize(
                R.styleable.BeeLoadingView_parWidth,
                parWidth.toInt()
            ).toFloat()
        } finally {
            ta.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mBgDrawable.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val bound = mBgDrawable.bounds
        mBgDrawable.setBounds(
            bound.left + paddingLeft,
            bound.top + paddingTop,
            bound.right - paddingRight,
            bound.bottom - paddingBottom
        )
        mBgDrawable.fatcor = factor
        mBgDrawable.space = space
        mBgDrawable.firstStroke = marginOuter
        mBgDrawable.secondStroke = marginInner
        mBgDrawable.mParWidth = parWidth
        mBgDrawable.draw(canvas)
        val progress  = mBgDrawable.getProgressRectF()
        mBitmapDrawable.setProgressRectF(progress)
        mBitmapDrawable.draw(canvas)
    }
}

class BitmapDrawable(val bitmap: Bitmap): Drawable(){

    private val progressRect by lazy { RectF() }

    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    fun setProgressRectF(rectF: RectF){
        progressRect.set(rectF)
    }


    override fun draw(canvas: Canvas) {
        val height = bitmap.height
        val width = bitmap.width
        val x = progressRect.right-width/2f
        val y = if (height>progressRect.height()){
            progressRect.top - (height-progressRect.height())
        }else{
            progressRect.top+ (progressRect.height()-height)/2f
        }
        canvas.drawBitmap(bitmap,x,y,mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return when (mPaint.alpha) {
            0xff -> PixelFormat.OPAQUE
            0x00 -> PixelFormat.TRANSPARENT
            else -> PixelFormat.TRANSLUCENT
        }
    }

}

class RoundRectBackground : Drawable() {

    private val mProgressRect by lazy { RectF() }
    var fatcor = 0f
    private var bgColor = Color.WHITE
    var firstStroke = 19.5f
    var secondStroke = 6f
    var space = 35f
    var mParWidth = 38f
    private val bgAlpha = 255 * 0.5
    private val mPath = Path()
    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mRect by lazy { RectF() }
    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    private val xfermode2 = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val topColor = Color.parseColor("#80FFF7C7")

    private var mBottomShader: LinearGradient? = null
    private val bColors = intArrayOf(
        Color.parseColor("#FCE384"),
        Color.parseColor("#FFDB4F"),
        Color.parseColor("#F8C34D")
    )

    private var mShader: LinearGradient? = null
    private val mColors = intArrayOf(Color.parseColor("#00FCB484"), Color.parseColor("#80FF911B"))

    private var bbShder: LinearGradient? = null
    private val bbColors = intArrayOf(
        Color.parseColor("#FCB484"),
        Color.parseColor("#FF911B"),
        Color.parseColor("#F8B44D")
    )


    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        mBottomShader =
            LinearGradient(0f, 0f, 0f, bottom.toFloat(), bColors, null, Shader.TileMode.CLAMP)
        mShader = LinearGradient(0f, 0f, 0f, bottom.toFloat(), mColors, null, Shader.TileMode.CLAMP)
        bbShder =
            LinearGradient(0f, 0f, 0f, bottom.toFloat(), bbColors, null, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        mRect.set(bounds)
        val layer0 = canvas.saveLayer(null, null)
        mPaint.color = bgColor
        mPaint.alpha = bgAlpha.toInt()
        canvas.drawRoundRect(mRect, mRect.height() / 2f, mRect.height() / 2f, mPaint)
        mRect.inset(firstStroke, firstStroke)
        mPaint.xfermode = xfermode
        mPaint.shader = bbShder
        mPaint.alpha = 255
        canvas.drawRoundRect(mRect, mRect.height() / 2f, mRect.height() / 2f, mPaint)
        canvas.restoreToCount(layer0)
        mRect.inset(secondStroke, secondStroke)
        mPaint.xfermode = null
        mProgressRect.set(mRect.left, mRect.top, mRect.right * fatcor, mRect.bottom)
        val layer1 = canvas.saveLayer(null, null)
        mPath.reset()
        mPath.addRoundRect(mRect, mRect.height() / 2f, mRect.height() / 2f, Path.Direction.CCW)
        canvas.clipPath(mPath)
        mPaint.shader = mBottomShader
        canvas.drawRoundRect(
            mProgressRect,
            mProgressRect.height() / 2f,
            mProgressRect.height() / 2f,
            mPaint
        )
        val layer = canvas.saveLayer(null, null)
        mPaint.shader = mShader
        canvas.drawRoundRect(
            mProgressRect,
            mProgressRect.height() / 2f,
            mProgressRect.height() / 2f,
            mPaint
        )
        mPaint.shader = null
        mPaint.xfermode = xfermode2
        var sWidth = mProgressRect.left - mParWidth / 2f
        while (sWidth < mProgressRect.width()) {
            mPath.reset()
            mPath.moveTo(sWidth, mProgressRect.height() + firstStroke + secondStroke)
            mPath.rLineTo(mParWidth, 0f)
            mPath.rLineTo(mParWidth, -mProgressRect.height())
            mPath.rLineTo(-mParWidth, 0f)
            mPath.close()
            canvas.drawPath(mPath, mPaint)
            sWidth += mParWidth + space
        }
        canvas.restoreToCount(layer)
        canvas.restoreToCount(layer1)
        mPaint.xfermode = null
        mPaint.shader = null
        mPaint.color = topColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 0.2f
        var times = 0
        val stroke = 8f
        var useWidth = 0f
        val alphaStep = 2.5f
        val widthStep = 0.2f
        while (useWidth <= stroke) {
            if (mPaint.alpha == 0) break
            canvas.drawRoundRect(mRect, mRect.height() / 2f, mRect.height() / 2f, mPaint)
            mRect.inset(widthStep, widthStep)
            val currAlpha = mPaint.alpha - alphaStep
            mPaint.alpha = if (currAlpha >= 0) currAlpha.toInt() else 0
            useWidth += widthStep
            times++
        }
        mPaint.style = Paint.Style.FILL
        mPaint.xfermode = null
        mPaint.shader = null
    }

    fun getProgressRectF(): RectF {
        mProgressRect.inset(-secondStroke,-secondStroke)
        return mProgressRect
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return when (mPaint.alpha) {
            0xff -> PixelFormat.OPAQUE
            0x00 -> PixelFormat.TRANSPARENT
            else -> PixelFormat.TRANSLUCENT
        }
    }
}
