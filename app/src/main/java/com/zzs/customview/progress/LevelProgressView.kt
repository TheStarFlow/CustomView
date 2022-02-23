package com.zzs.customview.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zzs.customview.R
import kotlin.math.abs

/**
@author  zzs
@Date 2022/2/23
@describe
 */
class LevelProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {


    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mBgColor: Int = Color.parseColor("#4D719FFF")
        set(value) {
            field = value
            invalidate()
        }
    private var mColors: IntArray? = null
    private var mPos:FloatArray?=null
    private val levelNames by lazy { mutableListOf<String>() }
    private val mProgressBgRect by lazy { RectF() }
    private val mProgressRect by lazy { RectF() }
    var currLevelColor = Color.parseColor("#89F7FF")
        set(value) {
            field = value
            invalidate()
        }
    var leverColor = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }
    private val Xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

    var textColor = Color.parseColor("#313B9D")
        set(value) {
            field = value
            invalidate()
        }
    var textSize = 15f
        set(value) {
            field = value
            invalidate()
        }
    var currLevelTextSize = 20f
        set(value) {
            field = value
            invalidate()
        }
    private var mShader: LinearGradient? = null
    var thickness = 30f
        set(value) {
            field = value
            invalidate()
        }

    var ringThickness = 3f
        set(value) {
            field = value
            invalidate()
        }
    var currLevel = 0
        set(value) {
            if (value < 0) {
                field = 0
            } else if (value > levelNames.size - 1) {
                field = levelNames.size - 1
            } else {
                field = value
            }
            invalidate()
        }
    private var orientation = HORIZONTAL
    var levelRadius = 18f
        set(value) {
            if (value < 0) {
                field = 0f
            } else {
                field = value
            }
            invalidate()
        }
    var currRadius = 24f
        set(value) {
            field = if (value < 0) {
                0f
            } else {
                value
            }
            invalidate()
        }

    var mode: Mode = Mode.PROGRESS
        set(value) {
            field = value
            calcProgress()
            invalidate()
        }
    var progressRange = 0..100
        set(value) {
            field = value
            if (progress < field.first) {
                progress = field.first
            } else if (progress > field.last) {
                progress = field.last
            }
            calcProgress()
            invalidate()
        }
    var progress: Int = 0
        set(value) {
            if (mode == Mode.LEVEL) return
            field = if (value < progressRange.first) {
                progressRange.first
            } else if (value > progressRange.last) {
                progressRange.last
            } else {
                value
            }
            calcProgress()
            configShader()
            invalidate()
        }


    init {
        mPaint.style = Paint.Style.FILL
        mPaint.textSize = textSize
        mPaint.color = mBgColor
        levelNames.apply {
            add("D")
            add("C")
            add("B")
            add("A")
            add("S")
        }
        handleTypeArray(context, attrs)
    }

    private fun handleTypeArray(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LevelProgressView)
        try {
            mBgColor = ta.getColor(R.styleable.LevelProgressView_progressBgColor, mBgColor)
            thickness =
                ta.getDimensionPixelSize(R.styleable.LevelProgressView_android_thickness, 30)
                    .toFloat()
            textColor = ta.getColor(R.styleable.LevelProgressView_android_textColor, textColor)
            textSize = ta.getFloat(R.styleable.LevelProgressView_levelTextSize, 15f)
            currLevelTextSize = ta.getFloat(R.styleable.LevelProgressView_currLevelTextSize, 20f)
            leverColor = ta.getColor(R.styleable.LevelProgressView_leverColor, leverColor)
            currLevelColor =
                ta.getColor(R.styleable.LevelProgressView_currLevelColor, currLevelColor)
            levelRadius = ta.getDimensionPixelSize(
                R.styleable.LevelProgressView_levelRadius,
                levelRadius.toInt()
            ).toFloat()
            currRadius = ta.getDimensionPixelSize(
                R.styleable.LevelProgressView_currLevelRadius,
                currRadius.toInt()
            ).toFloat()
            ringThickness = ta.getDimensionPixelSize(
                R.styleable.LevelProgressView_ringThickness,
                ringThickness.toInt()
            ).toFloat()
            currLevel = ta.getInt(R.styleable.LevelProgressView_currLevel, currLevel)
            orientation = ta.getInt(R.styleable.LevelProgressView_android_orientation, VERTICAL)
        } finally {
            ta.recycle()
        }
    }


    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val FULL_ALPHA = 255
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        when (orientation) {
            HORIZONTAL -> {
                val l = -w / 2f + currRadius
                val t = -thickness / 2f
                var r = w / 2f - currRadius
                val b = thickness / 2f
                mProgressBgRect.set(l, t, r, b)
                calcProgress()
                if (mColors != null) {
                    configShader()
                }
            }
            VERTICAL -> {
                val l = -thickness / 2f
                var t = -h / 2f + currRadius
                val r = thickness / 2f
                val b = h / 2f - currRadius
                mProgressBgRect.set(l, t, r, b)
                calcProgress()
                if (mColors != null) {
                    configShader()
                }
            }
        }

    }

    private fun calcProgress() {
        when (mode) {
            Mode.LEVEL -> {
                if (orientation == VERTICAL) {
                    val levelSize = levelNames.size
                    val space = (mProgressBgRect.height()) / (levelSize - 1)
                    var t = mProgressBgRect.top
                    t += (levelSize - currLevel - 1) * space
                    mProgressRect.set(
                        mProgressBgRect.left,
                        t,
                        mProgressBgRect.right,
                        mProgressBgRect.bottom
                    )
                } else {
                    val levelSize = levelNames.size
                    val space = (mProgressBgRect.width()) / (levelSize - 1)
                    var r = mProgressBgRect.right
                    r -= (levelSize - currLevel - 1) * space
                    mProgressRect.set(
                        mProgressBgRect.left,
                        mProgressBgRect.top,
                        r,
                        mProgressBgRect.bottom
                    )
                }

            }
            Mode.PROGRESS -> {
                when (orientation) {
                    VERTICAL -> {
                        val count = progressRange.count() - 1
                        val height = mProgressBgRect.height()
                        val space = height / count.toFloat()
                        val bottom = mProgressBgRect.bottom
                        val top = bottom - space * (progress - progressRange.first)
                        mProgressRect.set(
                            mProgressBgRect.left,
                            top,
                            mProgressBgRect.right,
                            mProgressBgRect.bottom
                        )
                    }
                    HORIZONTAL -> {
                        val count = progressRange.count() - 1
                        val width = mProgressBgRect.width()
                        val space = width / count.toFloat()
                        val l = mProgressBgRect.left
                        val r = l + space * (progress - progressRange.first)
                        mProgressRect.set(
                            mProgressBgRect.left,
                            mProgressBgRect.top,
                            r,
                            mProgressBgRect.bottom
                        )
                    }
                }
            }

        }
        if (mode == Mode.PROGRESS) {
            val sProgress = progress - progressRange.first
            val levelSize = levelNames.size - 1
            val levelSpace = (progressRange.count() - 1) / levelSize
            currLevel = sProgress / levelSpace
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        mPaint.color = mBgColor
        canvas.drawRect(mProgressBgRect, mPaint)
        mPaint.alpha = FULL_ALPHA
        mPaint.shader = mShader
        canvas.drawRect(mProgressRect, mPaint)
        mPaint.shader = null
        drawBottomLayerCircle(canvas, orientation)
        drawTopLayerCircle(canvas, orientation)
    }

    @JvmOverloads
    fun setGradientColor(colors: IntArray,pos:FloatArray?=null) {
        mColors = colors
        mPos = pos
        configShader()
        invalidate()
    }

    private fun configShader() {
        if (mColors == null) {
            mShader = null
        } else {
            if (mode == Mode.PROGRESS) {//进度条模式
                mShader = if (orientation == VERTICAL) {
                    getShader(
                        mProgressRect.left,
                        mProgressRect.bottom,
                        mProgressRect.left,
                        mProgressRect.top,
                        mColors!!,
                        mPos)
                } else {
                    getShader(
                        mProgressRect.left,
                        mProgressRect.top,
                        mProgressRect.right,
                        mProgressRect.top,
                        mColors!!,
                        mPos)
                }
            } else {//等级模式,需要支持多种颜色渐变
                mShader = if (orientation == VERTICAL) {
                    getShader(
                        mProgressRect.left,
                        mProgressRect.bottom,
                        mProgressRect.left,
                        mProgressRect.top,
                        mColors!!,
                        mPos)
                } else {
                    getShader(
                        mProgressRect.left,
                        mProgressRect.top,
                        mProgressRect.right,
                        mProgressRect.top,
                        mColors!!,
                        mPos)
                }
            }
        }
    }

    private fun getShader(
        left: Float, top: Float, right: Float,
        bottom: Float, colors: IntArray,pos:FloatArray?=null
    ): LinearGradient {
        return LinearGradient(left, top, right, bottom, colors, pos, Shader.TileMode.CLAMP)
    }

    private fun drawTopLayerCircle(canvas: Canvas, orientation: Int) {
        mPaint.xfermode = Xfermode
        val space: Float = if (orientation == HORIZONTAL) {
            val w = mProgressBgRect.width()
            w / (levelNames.size - 1)
        } else {
            val h = mProgressBgRect.height()
            h / (levelNames.size - 1)
        }

        for (i in levelNames.indices) {
            var cx: Float
            var cy: Float
            if (orientation == HORIZONTAL) {
                cx = mProgressBgRect.left + space * i
                cy = 0f
            } else {
                cy = mProgressBgRect.bottom - space * i
                cx = 0f
            }
            var isCurrLevel = false
            val radius = if (i == currLevel) {
                isCurrLevel = true
                canvas.save()
                canvas.translate(cx, cy)
                cx = 0f
                cy = 0f
                mPaint.color = currLevelColor
                currRadius - ringThickness
            } else {
                mPaint.color = leverColor
                levelRadius - ringThickness
            }
            val name = levelNames[i]
            canvas.drawCircle(cx, cy, radius, mPaint)
            drawTextOnCenter(canvas, cx, cy, name, mPaint, isCurrLevel)
            if (isCurrLevel) {
                canvas.restore()
            }
        }
        mPaint.xfermode = null
    }

    private fun drawBottomLayerCircle(canvas: Canvas, orientation: Int) {
        val space: Float = if (orientation == HORIZONTAL) {
            val w = mProgressBgRect.width()
            w / (levelNames.size - 1)
        } else {
            val h = mProgressBgRect.height()
            h / (levelNames.size - 1)
        }
        for (i in levelNames.indices) {
            var cx: Float
            var cy: Float
            if (orientation == HORIZONTAL) {
                cx = mProgressBgRect.left + space * i
                cy = 0f
            } else {
                cy = mProgressBgRect.bottom - space * i
                cx = 0f
            }
            var isCurrLevel = false
            val radius = if (i == currLevel) {
                isCurrLevel = true
                canvas.save()
                canvas.translate(cx, cy)
                cx = 0f
                cy = 0f
                mPaint.color = leverColor
                currRadius
            } else {
                mPaint.color = currLevelColor
                levelRadius
            }
            canvas.drawCircle(cx, cy, radius, mPaint)
            if (isCurrLevel) {
                canvas.restore()
            }
        }
    }

    private fun drawTextOnCenter(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        name: String,
        mPaint: Paint,
        isCurrLevel: Boolean,
    ) {
        mPaint.typeface = Typeface.DEFAULT_BOLD
        mPaint.textSize = if (isCurrLevel) currLevelTextSize else textSize
        mPaint.color = textColor
        mPaint.textAlign = Paint.Align.CENTER
        val fontMetrics = mPaint.fontMetrics
        val textHeight = abs(fontMetrics.bottom + fontMetrics.top)
        canvas.drawText(name, cx, cy + textHeight / 2f, mPaint)
    }


    enum class Mode {
        PROGRESS,
        LEVEL
    }
}