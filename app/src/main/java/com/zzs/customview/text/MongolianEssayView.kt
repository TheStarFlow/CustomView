package com.zzs.customview.text

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.*
import android.widget.Scroller
import androidx.annotation.RequiresApi
import com.zzs.customview.R
import kotlin.math.abs
import kotlin.math.max

/**
@author  zzs
@Date 2021/9/3
@describe
 */
class MongolianEssayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mListener: OnScrollDisListener? = null

    interface OnScrollDisListener {
        fun onScroll(dis: Int, factor: Float, index: Int)
    }


    private var mLineSpace = 0
    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private var mTextSize = 12f
    private var mTextColor = Color.BLACK
    private val mPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mContent: String? = null
    private val mSentence by lazy { mutableListOf<String>() }
    private val mSentenceRect by lazy { HashMap<String, Rect>() }
    private val mDrawTextRect = Rect()
    private val mMeasureRect = Rect()
    private val mViewRect = Rect()
    private var mode = MODE_STATIC
    private var mGravity = 0
    private var spreadExtra = 0
    private var showLine:Int = -1


    private var mAutoScroll = true
    fun setAutoScroll(auto: Boolean) {
        mAutoScroll = auto
    }

    var mScrollDuration = SCROLL_LEVEL_DEFAULT_2
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        set(value) {
            field = value
            if (mode == MODE_AUTO_SCROLLER) {
                val oriRunning = mValueAnimator?.isRunning
                mValueAnimator?.pause()
                mValueAnimator = createAnimator()
                if (oriRunning == true) {
                    mValueAnimator?.start()
                }
            }
        }

    private var singleSentenceWidth = 0

    private var mScrollerColor = Color.YELLOW

    //scroll
    private var mCurrentIndex = 0
    private val mScroller by lazy { Scroller(context) }
    private var mScrollDistance = 0
    private var mValueAnimator: Animator? = null
    private var mVelocityTracker: VelocityTracker? = null
    private val mMiniVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val mMaxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private var isFling = false
    private var mMiniOverScrollDis = 0f
    private var isTouch = false


    companion object {
        const val MODE_STATIC = 0
        const val MODE_AUTO_SCROLLER = 1
        const val MODE_SCROLL = 2

        const val SCROLL_LEVEL_DEFAULT_0 = 1500
        const val SCROLL_LEVEL_DEFAULT_1 = 2500
        const val SCROLL_LEVEL_DEFAULT_2 = 3500
        const val SCROLL_LEVEL_DEFAULT_3 = 4500
        const val SCROLL_LEVEL_DEFAULT_4 = 5500

    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setLayoutMode(mode: Int) {
        when (mode) {
            MODE_SCROLL -> {
                this.mode = mode
            }
            MODE_AUTO_SCROLLER -> {
                this.mode = mode
                if (isAutoScroll()) {
                    launchScrollMode()
                } else if (mValueAnimator?.isRunning == true) {
                    pauseScroller()
                }
            }
            else -> {
                this.mode = MODE_STATIC
            }
        }
        invalidate()
    }

    fun setScrollerColor(color: Int) {
        this.mScrollerColor = color
        requestLayout()
    }

    private fun createAnimator(): Animator {
        val s = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = mScrollDuration.toLong()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {

                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun onAnimationRepeat(animation: Animator?) {
                    if (mCurrentIndex >= mEachHeight.size()) {
                        mCurrentIndex = mEachHeight.size() - 1
                    }
                    val scrollDis = mEachHeight.valueAt(mCurrentIndex)
                    mCurrentIndex++
                    if (mCurrentIndex < mEachHeight.size()) {
                        mListener?.onScroll(
                            mScrollDistance,
                            mScrollDistance / 1f / max(mMeasureRect.width(), 1),
                            mCurrentIndex
                        )
                        mScroller.startScroll(
                            mScrollDistance,
                            0, /*singleSentenceWidth + mLineSpace*/
                            scrollDis,
                            0,
                            mScrollDuration
                        )
                        mScrollDistance += scrollDis//singleSentenceWidth + mLineSpace
                        invalidate()
                    } else {
                        mValueAnimator?.pause()
                        mCurrentIndex = mEachHeight.size() - 1
                    }
                }

            })
        }
        return s
    }

    private fun launchScrollMode() {
        reset()
        mValueAnimator?.start()
    }


    fun setIndex(index: Int) {
        mCurrentIndex = index
    }

    fun scrollToDis(scrollDis: Int, factor: Float) {
        val dis = factor * mMeasureRect.width() - mScrollDistance
        mScroller.startScroll(
            mScrollDistance,
            0, /*singleSentenceWidth + mLineSpace*/
            dis.toInt(),
            0,
            0
        )
        mScrollDistance += dis.toInt()//singleSentenceWidth + mLineSpace
        invalidate()
    }


    init {
        handleTa(context, attrs)
        initPaint()
        mValueAnimator = createAnimator()
    }

    private fun initPaint() {
        mPaint.color = mTextColor
        mPaint.textSize = mTextSize
        mPaint.typeface = Typeface.createFromAsset(context.assets, "MHR8102.ttf")
        mPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        if (mSentence.isEmpty()) return
        canvas.save()
        when (mode) {
            MODE_STATIC -> {
                canvas.translate(mWidth / 2f, mHeight / 2f)
                canvas.rotate(90f)
                drawStaticSentence(mSentence, canvas)
            }
            MODE_AUTO_SCROLLER -> {
                canvas.translate(mWidth / 2f, mHeight / 2f)
                canvas.rotate(90f)
                drawStaticSentence(mSentence, canvas, true)
            }
            MODE_SCROLL -> {
                canvas.translate(mWidth / 2f, mHeight / 2f)
                canvas.rotate(90f)
                drawStaticSentence(mSentence, canvas)
            }
        }
        canvas.restore()
    }

    private fun drawStaticSentence(
        sSentence: List<String>,
        canvas: Canvas,
        autoScroll: Boolean = false
    ) {
        var dy = 0f - mDrawTextRect.height() / 2f
        var dx = 0f - mDrawTextRect.width() / 2f
        //滚动绘制 从中点开始
        if (autoScroll) {
            dy -= mWidth / 2f
        }
        //绘制区域大于view区域 ， 左端对齐
        if (mDrawTextRect.height() > mViewRect.width()) {
            val dis = (mDrawTextRect.height() - mViewRect.width()) / 2f
            dy -= dis
        }
        //绘制区域大于view区域 ，上端对其
        if (mDrawTextRect.width() > mViewRect.height()) {
            val dis = (mDrawTextRect.width() - mViewRect.height()) / 2f
            dx += dis
        }
        //处理padding start
        if (paddingStart > 0) {
            dy -= paddingStart
        }
        //处理padding top
        if (paddingTop > 0) {
            dx += paddingTop
        }
        if (mGravity != 0) {
            when (mGravity) {
                Gravity.START -> {
                    if (mDrawTextRect.height() < mViewRect.width()) {
                        val dis = abs(mDrawTextRect.height() - mViewRect.width()) / 2f
                        dy += dis
                    }

                }
                Gravity.TOP -> {
                    if (mDrawTextRect.width() < mViewRect.height()) {
                        val dis = abs(mDrawTextRect.width() - mViewRect.height()) / 2f
                        dx -= dis
                    }
                }
                (Gravity.START or Gravity.TOP) -> {
                    if (mDrawTextRect.width() < mViewRect.height()) {
                        val dis = abs(mDrawTextRect.width() - mViewRect.height()) / 2f
                        dx -= dis
                    }
                    if (mDrawTextRect.height() < mViewRect.width()) {
                        val dis = abs(mDrawTextRect.height() - mViewRect.width()) / 2f
                        dy += dis
                    }
                }
            }
        }

        for (i in sSentence.indices) {

            val sentence = sSentence[i]
            val sentenceRect = mSentenceRect[sentence]
            dy += sentenceRect?.height() ?: 0
            if (autoScroll) {
                if (mCurrentIndex + i == sSentence.size - 1) {
                    mPaint.color = mScrollerColor
                } else {
                    mPaint.color = mTextColor
                }
            }
            if (showLine!=-1&&showLine>0){
                if (i+showLine>=sSentence.size){
                    canvas.drawText(sentence, 0, sentence.length, dx, dy, mPaint.apply {
                        textSize = mTextSize

                    })
                }
            }else{
                canvas.drawText(sentence, 0, sentence.length, dx, dy, mPaint.apply {
                    textSize = mTextSize

                })
            }


            dy += mLineSpace

        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator?.removeAllListeners()
        mValueAnimator?.cancel()
    }


    private fun handleTa(context: Context, attrs: AttributeSet?) {
        var ta: TypedArray? = null
        try {
            ta = context.obtainStyledAttributes(attrs, R.styleable.MongolianEssayView)
            mTextSize =
                ta.getDimensionPixelSize(R.styleable.MongolianEssayView_android_textSize, 12)
                    .toFloat()
            mTextColor = ta.getColor(R.styleable.MongolianEssayView_android_textColor, Color.BLACK)
            mLineSpace =
                ta.getDimensionPixelSize(R.styleable.MongolianEssayView_android_lineSpacingExtra, 0)
            mGravity = ta.getInt(R.styleable.MongolianEssayView_android_gravity, 0)
            spreadExtra = ta.getDimensionPixelSize(R.styleable.MongolianEssayView_spreadExtra,0)
            showLine = ta.getInt(R.styleable.MongolianEssayView_showLine,-1)
        } finally {
            ta?.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val rect = getEssayRect()
        mWidth = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            rect.width() + paddingStart + paddingEnd
        }
        mHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            rect.height() + paddingTop + paddingBottom
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    private var spreadDistance = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewRect.set(0, 0, w, h)
        val maxWidth = (h - paddingTop-spreadExtra).toFloat()
        cutSentence(maxWidth)
    }

    private fun cutSentence(maxWidth: Float){
        val newCache = mutableListOf<String>()
        val sCache = mutableListOf<String>()
        spreadDistance = maxWidth.toInt()
        for (s in mSentence) {
            sCache.clear()
            cutSentence(s,sCache, maxWidth)
            sCache.reverse()
            newCache.addAll(sCache)
        }
        mSentence.clear()
        mSentence.addAll(newCache)
        getEssayRect()
    }


    private fun cutSentence(target: String, dstList: MutableList<String>, maxWidth: Float) {
        var length = mPaint.breakText(target, true, maxWidth, null)
        if (length >= target.length || length == 0) {
            dstList.add(target)
        }else{
            length = getLastBlankIndex(target,length)
            val s1 = target.substring(0..length)
            val s2 = target.substring(length + 1)
            dstList.add(s1)
            cutSentence(s2,dstList,maxWidth)
        }
    }

    private fun getLastBlankIndex(target: String, length: Int): Int {
        var index = length
        while (target[index].toString()!=" "){
            index--
        }
        return index
    }


    private val mScrollDisNum = SparseIntArray()
    private val mEachHeight = SparseIntArray()

    private fun getEssayRect(): Rect {
        if (mSentence.isEmpty()) return Rect()
        var maxWidth = 0
        var heightNum = 0
        val filter = mSentence.filter { !TextUtils.isEmpty(it) }
        mSentence.clear()
        mSentenceRect.clear()
        mScrollDisNum.clear()
        mSentence.addAll(filter)
        var i = 0
        var mNumHeight = 0
        mSentence.forEach { sentence ->
            mMeasureRect.set(0, 0, 0, 0)
            mPaint.textSize = mTextSize
            mPaint.getTextBounds(sentence, 0, sentence.length, mMeasureRect)
            mNumHeight += mMeasureRect.height() + mLineSpace
            mScrollDisNum.put(i, mNumHeight)
            mEachHeight.put(i, mMeasureRect.height() + mLineSpace)
            singleSentenceWidth = mMeasureRect.height()
            val width = mPaint.measureText(sentence)
            mSentenceRect[sentence] = Rect(mMeasureRect)
            if (width > maxWidth) {
                maxWidth = width.toInt()
            }
            heightNum += mMeasureRect.height() + mLineSpace
            i++
        }
        mDrawTextRect.set(0, 0, maxWidth, heightNum)
        return mMeasureRect.apply {
            set(0, 0, heightNum, maxWidth)
        }

    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setMongolianContent(ct: String?) {
        ct?.run {
            mContent = this
            mSentence.clear()
            val sContent = mContent!!.split(System.lineSeparator()).reversed()
            reset()
            mSentence.addAll(sContent)
            if (spreadDistance!=0){
                cutSentence(spreadDistance.toFloat())
                invalidate()
            }else{
                getEssayRect()
                invalidate()
            }
        }
    }

    fun reset() {
        scrollToDis(0, 0f)
        mCurrentIndex = 0
        mScrollDistance = 0
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            if (isFling) {
//                mCurrentIndex = if (di == 0) {
//                    0
//                } else {
//                    max(mScroller.currX / di, 0)
//
//                }
                mCurrentIndex = getScrollIndex(mScroller.currX)
            }
            invalidate()
            mListener?.onScroll(
                mScroller.currX,
                mScroller.currX / 1f / max(mMeasureRect.width(), 1),
                mCurrentIndex
            )
        } else if (isFling) {
            mScrollDistance = mScroller.currX
            mValueAnimator?.run {
                if (!isTouch && isAutoScroll()) {
                    if (!isRunning) {
                        start()
                    } else if (isPaused) {
                        resume()
                    }
                }
            }
            isFling = false
        }
    }

    private fun getScrollIndex(dis: Int): Int {
        for (i in 0 until mScrollDisNum.size()) {
            if (i < mScrollDisNum.size() - 1) {
                val scroll1 = mScrollDisNum.valueAt(i)
                val scrolll2 = mScrollDisNum.valueAt(i + 1)
                if (dis in scroll1..scrolll2) {
                    return i
                }
            }
        }
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun pauseScroller() {
        mValueAnimator?.pause()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun resumeScroller() {
        mValueAnimator?.resume()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun launchScroller() {
        mValueAnimator?.run {
            if (isPaused) {
                resume()
            } else {
                start()
            }
        }
    }

    private var mLastX = 0f

    private fun isAutoScroll() = mode == MODE_AUTO_SCROLLER && mAutoScroll

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        event ?: return false
        if (mode == MODE_STATIC) return false
        val scrollDis = (mDrawTextRect.height() - mViewRect.width()) + paddingEnd + paddingStart
        if (scrollDis < 0 && mode != MODE_AUTO_SCROLLER) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain()
                }
                if (isFling) {
                    if (!mScroller.isFinished) {
                        mScroller.forceFinished(true)
                        mScrollDistance = mScroller.currX
                    }
                }
                mVelocityTracker?.addMovement(event)
                mValueAnimator?.pause()
                isTouch = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = mLastX - event.x
                val min = 0 //if (isAutoScroll()) -mMiniOverScrollDis else 0f
                val max =
                    if (isAutoScroll()) mMeasureRect.width() + paddingStart + paddingEnd else scrollDis/*(0.8 * mMeasureRect.width()).toInt()*/
                if (mScrollDistance + dx >= min && mScrollDistance + dx < max) {
                    mScroller.startScroll(mScrollDistance, 0, dx.toInt(), 0, 0)
                    mScrollDistance += dx.toInt()
                    mScrollDistance = max(mScrollDistance, 0)
                    val di = (singleSentenceWidth + mLineSpace)
//                    mCurrentIndex = if (di == 0) {
//                        0
//                    } else {
//                        mScrollDistance / di
//                    }
                    mCurrentIndex = getScrollIndex(mScrollDistance)
                    invalidate()
                }
                mVelocityTracker?.addMovement(event)
                mListener?.onScroll(
                    mScrollDistance,
                    mScrollDistance / 1f / max(mMeasureRect.width(), 1),
                    mCurrentIndex
                )
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
                val xVelocity = mVelocityTracker?.xVelocity
                xVelocity?.run {
                    if (abs(this) > mMiniVelocity) {
                        isFling = true
                        val end =
                            if (isAutoScroll()) mMeasureRect.width() + paddingStart + paddingEnd else scrollDis//(0.8 * mMeasureRect.width()).toInt()
                        mScroller.fling(mScroller.currX, 0, -xVelocity.toInt(), 0, 0, end, 0, 0)
                    } else {
                        mValueAnimator?.run {
                            if (isAutoScroll()) {
                                if (!isRunning) {
                                    start()
                                } else if (isPaused) {
                                    resume()
                                }
                            }

                        }
                    }
                }
                mVelocityTracker?.recycle()
                mVelocityTracker = null
                invalidate()
                isTouch = false

            }
        }
        mLastX = event.x
        return true
    }

    fun setLineSpace(space: Int) {
        mLineSpace = space
        requestLayout()
    }

    fun setTextSize(textSize: Float) {
        mTextSize = textSize
    }
}