package com.zzs.customview.path

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.util.Pools
import androidx.core.util.valueIterator
import com.zzs.customview.R
import kotlin.math.abs

/**
@author  zzs
@Date 2022/2/23
@describe
 */
class ThrowStarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMatrix = Matrix()
    private val mPathPool by lazy { Pools.SimplePool<Path>(10) }
    private val mAnimPool by lazy { Pools.SimplePool<PathValueAnimator>(10) }
    private val mShowStar by lazy { SparseArray<Path>() }
    private val mStarBitmap by lazy {
        BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ceping_img_star
        )
    }
    private val mDuration = 2000L
    private val mPathMeasure by lazy { PathMeasure() }
    private val mCurrPathLength by lazy { SparseArray<Float>() }
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)
    private val mListeners by lazy { SparseArray<ThrowStarListener>() }

    companion object {
        private const val FULL_DEGREES = 360f
        private const val FULL_ALPHA = 255
        private const val ALPHA_PROGRESS = 0.6f
    }

    init {
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        val iterator = mShowStar.valueIterator()
        while (iterator.hasNext()) {
            val path = iterator.next()
            val progress = mCurrPathLength[path.hashCode()]
            mPathMeasure.setPath(path, false)
            mPathMeasure.getPosTan(progress * mPathMeasure.length, pos, tan)
            mMatrix.reset()
            if (progress >= ALPHA_PROGRESS) {
                mPaint.alpha = (((1 - progress)) * 2 * FULL_ALPHA).toInt()
            } else {
                val degrees = progress * 2 * FULL_DEGREES
                mMatrix.postRotate(
                    degrees, (mStarBitmap.width / 2).toFloat(),
                    (mStarBitmap.height / 2).toFloat()
                )
                mPaint.alpha = FULL_ALPHA
            }
            mMatrix.postTranslate(pos[0] - mStarBitmap.width / 2, pos[1] - mStarBitmap.height / 2)
            canvas.drawBitmap(mStarBitmap, mMatrix, mPaint)
        }
    }

    @JvmOverloads
    fun throwStar(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        l: ThrowStarListener? = null
    ) {
        var sPath = mPathPool.acquire()
        if (sPath == null) {
            sPath = Path()
        }
        sPath.reset()
        mShowStar.put(sPath.hashCode(), sPath)
        var pathAnimator = mAnimPool.acquire()
        if (pathAnimator == null) {
            pathAnimator = PathValueAnimator(sPath.hashCode(), ValueAnimator.ofFloat(0f, 1f).apply {
                duration = mDuration
                interpolator = DecelerateInterpolator()
            }, sPath)
        }
        pathAnimator.hashCodeId = sPath.hashCode()
        sPath.moveTo(startX, startY)
        val dx = abs(startX - endX)
        val dy = abs(startY - endY)
        var controlX = startX + dx
        var controlY = startY - dy
        if (controlX > width) {
            controlX = width.toFloat()
        }
        if (controlY < 0) {
            controlY = 0f
        }
        sPath.quadTo(controlX, controlY, endX, endY)
        pathAnimator.anim.addUpdateListener {
            val value = it.animatedValue as Float
            mCurrPathLength.put(pathAnimator.hashCodeId, value)
            invalidate()
        }
        pathAnimator.anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeAllListeners()
                mCurrPathLength.put(pathAnimator.hashCodeId, 0.0f)
                pathAnimator.path?.run {
                    mPathPool.release(this)
                }
                mAnimPool.release(pathAnimator)
                mShowStar.remove(sPath.hashCode())
                mListeners.get(sPath.hashCode()).onFinish()
                mListeners.remove(sPath.hashCode())
                pathAnimator.recycle()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

        })
        l?.run {
            mListeners.put(sPath.hashCode(), this)
        }
        pathAnimator.anim.start()
    }

    data class PathValueAnimator(
        var hashCodeId: Int,
        val anim: ValueAnimator,
        var path: Path? = null
    ) {
        fun recycle() {
            path?.reset()
            hashCodeId = -1
            path = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mShowStar.clear()
        mCurrPathLength.clear()
    }

    interface ThrowStarListener {
        fun onFinish()
    }
}