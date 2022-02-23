package com.gzhlsoft.yougureader.widget.evaluation

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Looper
import android.util.*
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Scroller
import androidx.annotation.RequiresApi
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.gzhlsoft.yougureader.widget.evaluation.mark.FallingToneMark
import com.gzhlsoft.yougureader.widget.evaluation.mark.MarkFactory
import com.gzhlsoft.yougureader.widget.evaluation.mark.RisingToneMark
import com.zzs.customview.R
import kotlin.math.abs

/**
@author  zzs
@Date 2022/2/9
@describe 测评文本View

 @text 范例文本   "        "content":"乡下人家[/]总爱[/]在屋前搭一瓜架，或[/]种南瓜，或[/]种丝瓜，让那些瓜藤[/]攀<•>上棚架，爬<•>上屋檐。当[/]花<•>儿落<•>了的时候，藤<•>上[/]便<•>结出了[/]青的、红的瓜，它们[/]一个个挂<•>在房前，衬<•>着[/]那长<•>长<•>的[/]藤<•>，绿<•>绿<•>的[/]叶。青、红的瓜，碧绿的[/]藤和叶，构成了一道别<•>有<•>风<•>趣<•>的装<•>饰<•>，比那[/]高楼门前(⌒)蹲着(⌒)一对石(⌒)狮子[/]或是[/]竖着(⌒)两根(⌒)大旗杆，可<•>爱<•>多了。\n有些人家，还在门前的场地上[/]种<•>几株花，芍药，凤仙，鸡冠花，大丽菊，它们依着时令，顺序开放，朴<•>素<•>中[/]帶着几分华<•>丽<•>，显出一派独<•>特<•>的农家风光。还有些人家，在屋后种几十枝竹，绿<•>的叶，青<•>的竿，投下一片[/]浓<•>浓<•>的绿<•>荫<•>。几场春雨过后，到那里走走，你常常会看见[/]许多鲜嫩的笋,成群地[/]从土里[/]探出头来。\n鸡，乡下人家照例总<•>要养几只的。从他们的房前屋后走过，你肯<•>定<•>会瞧见[/]一只母鸡，率领一群小鸡，在竹林中觅食;或是瞧见[/]耸着尾巴的雄鸡，在场地上大踏步地走来走去。\n他们的屋后[/]倘若[/]有一条小河，那么[/]在石桥旁边，在绿树荫下，会见到[/]一群鸭子游戏水中，不时地[/]把头扎到水下去觅食。即使[/]附近的石头上[/]有妇女在捣衣，它[/]们也从<•>不吃惊。\n若是[/]在夏天的傍晚出去散步，常常会瞧见[/]乡下人家吃<•>晚<•>饭<•>的情景。他们[/]把桌椅饭菜[/]搬到门前，天高地阔地[/]吃<•>起来。天边的红<•>霞<•>，向晚的微<•>风<•>，头上飞过的[/]归巢的鸟儿，都是他们的好友，它们[/]和乡下人家一起，绘成了[/]一幅自<•>然<•>、和<•>谐<•>的[/]田<•>园<•>风<•>景<•>画<•>。\n秋天[/]到了，纺织娘[/]寄住在[/]他们屋前的瓜架上。月明人静的[/]夜<•>里，它们[/]便唱起歌来:“织，织，织，织啊!织织，织，织啊!”那歌声[/]真<•>好听，赛过催眠曲，让那些[/]辛苦一天的人们，甜甜蜜蜜地[/]进入梦乡。\n乡下人家，不论[/]什么时候，不论[/]什么季<•>节，都有一道[/]独<•>特<•>、迷<•>人<•>的[/]风景。""
 */
class EvaluationTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ViewTreeObserver.OnPreDrawListener {

    private companion object {
        private const val CURR_TEXT_SIZE = 42f
        private const val CURR_TEXT_ALPHA = 255
        private const val NOR_TEXT_ALPHA = 128
        private const val CURR_LINE_SPACING = 46.5f
        private const val MAX_LINE_CHAR_NUM = 20
        private const val CHAR_SPACE = 20f
        private const val CENTER_COLOR = "#6E06CBF8"
        private const val START_COLOR = "#0006CBF8"
        private const val LINE_CENTER_COLOR = "#FF06CBF8"
    }

    private var currLineWidth: Float = 0f
    private var normalLineWidth: Float = 0f
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCurrPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mWidth = 0
    private var mHeight = 0
    private var mContent = ""
    private lateinit var mTextList: CharArray
    var normalColor = Color.WHITE
        set(value) {
            field = value
            invalidateMySelf()
        }
    var markColor = Color.parseColor("#FDEB00")
        set(value) {
            field = value
            invalidateMySelf()
        }
    var normalTextSize = 29f
    set(value) {
        field = value
        mPaint.textSize = field
        invalidateMySelf()
    }
    private var normalCharWidth = 0f
    private var currCharWidth = 0f
    private var normalCharHeight = 0f
    private var currCharHeight = 0f
    private var normalIndent = 0f
    private var currIndent = 0f
    private lateinit var mLineRange: SparseArray<IntRange>
    private lateinit var mLineNums: SparseIntArray
    private lateinit var mPassageStart: SparseBooleanArray
    private val mScroller by lazy { Scroller(context) }

    private val centerColor = Color.parseColor(CENTER_COLOR)
    private val startColor = Color.parseColor(START_COLOR)
    private val lineCenterColor = Color.parseColor(LINE_CENTER_COLOR)

    private val mEvaDataBean by lazy { SparseArray<TextCharBean>() } //key = evaIndex
    private val mOriDataBean by lazy { SparseArray<TextCharBean>() } // key  = oriIndex
    var isSwitchIndent = false  //是否首行缩进
    var isStableNumOneLine = false  //一行是否固定字数
        set(value) {
            field = value
            invalidateMySelf()
        }
    var maxLineNum = MAX_LINE_CHAR_NUM
    private val rising by lazy {
        BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ceping_icon_up
        )
    }
    private val falling by lazy {
        BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ceping_icon_down
        )
    }

    private var mSelectBgShader: LinearGradient? = null
    private var mSelectLineShader: LinearGradient? = null


    init {
        mPaint.textSize = normalTextSize
        mCurrPaint.textSize = CURR_TEXT_SIZE
        mPaint.color = normalColor
        mPaint.alpha = NOR_TEXT_ALPHA
        mCurrPaint.color = normalColor
        mMarkPaint.color = markColor
        mMarkPaint.textSize = normalTextSize
        viewTreeObserver.addOnPreDrawListener(this)

    }

    private lateinit var mModels: SparseArray<SparseArray<EvaViewBean>>
    /**
     * @return 需要测评的字数
     * */
    fun setEvaluationText(content: String?):Int {
        content ?: return 0
        if (content.isEmpty()) return 0
        mModels = EvaViewBean.handleContent(content)
        mContent = content
        initDrawData(mContent)
        invalidateMySelf()
        return mEvaDataBean.size()
    }


    private fun initDrawData(content: String) {
        if (this.mContent.isNotEmpty()) {
            mTextList = this.mContent.toCharArray()
        } else {
            return
        }
        var line = 0
        val maxLineNum = if (isStableNumOneLine) maxLineNum else {
            val num = findMaxNumOneLine(content)
            maxLineNum = num
            num
        }
        val lineRange = SparseArray<IntRange>()
        val lineNums = SparseIntArray()
        val passageStart = SparseBooleanArray()
        var charNum = 0
        var lineRangeStartIndex = 0
        var switchLine = true
        var evaIndex = 0
        var curLineNum = 0
        for (index in mTextList.indices) {
            val s = mTextList[index]
            if (switchLine) {
                lineRangeStartIndex = index
                curLineNum =
                    if ((line == 0 || mTextList[index - 1].toString() == System.lineSeparator()) && isSwitchIndent) {
                        maxLineNum - 2
                    } else {
                        maxLineNum
                    }
                switchLine = false
            }
            if (s.toString() == System.lineSeparator()) {
                switchLine = true
            }
            val isMark =
                s.toString() in EvaViewBean.SYMBOLS || (s.toString() == "。" && mTextList[index - 1].toString() == "<")
                        || s.toString() == System.lineSeparator()
            if (isMark) {
                //标注
            } else {
                if (s.toString() !in EvaViewBean.PASSAGE_SYMBOL&& s!='”') {
                    val bean = TextCharBean()
                    bean.oriIndex = index
                    bean.word = s.toString()
                    bean.evaIndex = evaIndex
                    evaIndex++
                    mEvaDataBean.put(bean.evaIndex, bean)
                    mOriDataBean.put(bean.oriIndex, bean)
                }
                //正常计数文字符号
                charNum++
            }
            if (charNum == curLineNum && (findNextWord(
                    index + 1,
                    content
                ) in EvaViewBean.PASSAGE_SYMBOL)
            ) {//符号换行放到上面
                curLineNum += 1
            }
            val bracketType = mModels.get(EvaViewBean.TYPE_BRACKETS)
            if (bracketType != null
                && bracketType.containsKey(index + 1)
                && (bracketType.get(index + 1) != null)
                && (bracketType.get(index + 1).mark == Mark.C_READING)
                && !isMark
                && charNum == curLineNum
            ) {
                curLineNum += 1
            }
            if (charNum == curLineNum || switchLine || index == mTextList.size - 1) {
                if (switchLine) {
                    val nextLine = if (charNum == 0) line else line + 1 //刚好按字数段末遇上换行符
                    passageStart.put(nextLine, true) //段落的开始
                }
                if (charNum == 0) {
                    continue
                }
                val range = IntRange(start = lineRangeStartIndex, index)
                lineRange.put(line, range)
                lineNums.put(line, curLineNum)
                line++
                charNum = 0
                switchLine = true
            }

        }
        mLineRange = lineRange
        mLineNums = lineNums
        mPassageStart = passageStart
    }

    /**
     * 找第一个换行符前有多少个字包括符号
     *
     *
     * */

    private fun findMaxNumOneLine(content: String): Int {
        if (content.isEmpty()) return 0
        val list = content.split(System.lineSeparator())
        var max = 0
        for (sentence in list) {
            val num = findOneLineNum(sentence)
            if (max <= num) {
                max = num
            }
        }
        return max
    }

    private fun findOneLineNum(content: String): Int {
        if (content.isEmpty()) return 0
        var num = 0
        var sIndex = 0
        var char: Char? = null
        var isMark = false
        while (sIndex <= content.length - 1) {
            char = content[sIndex]
            isMark =
                char.toString() in EvaViewBean.SYMBOLS || (char.toString() == "。" && sIndex > 0 && mTextList[sIndex - 1].toString() == "<")
                        || char.toString() == System.lineSeparator()
            if (!isMark) {
                num++
            }
            sIndex++
        }
        return num
    }

    private var mLastRealTimeSize = 0
    /**
     *
     * 返回实时朗读应该滚动的距离
     *
     * */
    fun setRealTimeText(list: List<TextCharBean>):Int {
        if (!isShown)return 0
        var dSize = 0
        if (list.size >= mLastRealTimeSize) {
            val oriSize = mLastRealTimeSize
            dSize = list.size - oriSize
        }
        if (dSize <= 0) {
            mLastRealTimeSize = list.size
            return 0
        }
        if (list.isNotEmpty()){
            Log.i("RealTimeInfo","${ list.last().word} dSize = $dSize  index = ${list.lastIndex}")
        }
        var count = 0
        var mIndex = list.lastIndex
        do {
            val bean = list[mIndex]
            val evaBean = mEvaDataBean.get(bean.evaIndex)
            val oriBean = mOriDataBean.get(evaBean.oriIndex)
            oriBean.color = if (bean.word != evaBean.word) {
                normalColor
            } else{
                bean.color
            }
            Log.i("RealTimeInfo","word = ${oriBean.word}, evaIndex = ${oriBean.evaIndex} oriIndex = ${oriBean.oriIndex} color = ${oriBean.color}")
            mIndex--
            count++

        } while (count != dSize)
        mLastRealTimeSize = list.size
        if (list.isNotEmpty()) {
            try {
                val textBean = mEvaDataBean.get(list.last().evaIndex)
                if (textBean != null) {
                    val oriIndex = textBean.oriIndex
                    if (this::mLineRange.isInitialized) {
                        mLineRange.forEach { key, value ->
                            if (oriIndex in value) {
                                mCurrReadLine = key
                                invalidateMySelf()
                                return@forEach
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        invalidateMySelf()
        return getCurrLinePos() - getParentHeight()/2
    }

    private fun getParentHeight():Int{
        if (parent is ViewGroup){
            return (parent as ViewGroup).height
        }
        return height
    }

    private fun getCurrLinePos():Int{
        val size=  getCurrPos()
        val pos = IntArray(2)
         getLocationOnScreen(pos)
        return (size.height-pos[1]).toInt()
    }

    private fun invalidateMySelf() {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    private fun findNextWord(index: Int, content: String): String {
        var sIndex = index
        while (sIndex > 0 && sIndex < content.length) {
            val char = content[sIndex].toString()
            if (char in EvaViewBean.SYMBOLS) {
                sIndex++
                continue
            }
            return char
        }
        return ""
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        mSelectBgShader = LinearGradient(
            0f, 0f, width.toFloat(), 0f, intArrayOf(
                startColor, centerColor,
                startColor
            ), floatArrayOf(0.0f, 0.5f, 1.0f), Shader.TileMode.CLAMP
        )
        mSelectLineShader = LinearGradient(
            0f, 0f, width.toFloat(), 0f, intArrayOf(
                startColor, lineCenterColor,
                startColor
            ), floatArrayOf(0.0f, 0.5f, 1.0f), Shader.TileMode.CLAMP
        )
    }

    var mCurrReadLine = -2

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var fontMetrics = mPaint.fontMetrics
        normalCharHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
        fontMetrics = mCurrPaint.fontMetrics
        currCharHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
        val heiMode = MeasureSpec.getMode(heightMeasureSpec)
        val heiSize = MeasureSpec.getSize(heightMeasureSpec)
        var measureHeight = 0
        when (heiMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                if (this::mLineNums.isInitialized) {
                    measureHeight =
                        ((mLineNums.size() - 2) * normalCharHeight + (mLineNums.size()) * CURR_LINE_SPACING + 2 * currCharHeight).toInt()
                } else {
                    measureHeight = 0
                }
            }
            MeasureSpec.EXACTLY -> {
                measureHeight = heiSize
            }
        }
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), measureHeight)

    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        if (!this::mTextList.isInitialized) return
        var line: Int = 0
        var currLine: Int
        var isCurrLine: Boolean
        var space: Int
        var startX: Float//每一行的起点
        var startY = currCharHeight
        var lineWidth = 0f//每一行当前所用距离
        var lineCharNums = 0
        var drawCurrSelect = false
        for (index in mTextList.indices) {
            val s = mTextList[index]
            if (s.toString() in EvaViewBean.SYMBOLS || (s.toString() == "。" && mTextList[index - 1].toString() == "<")) continue
            currLine = getCurrLine(index)
            if (currLine == mCurrReadLine) {//当前读到的行数
                isCurrLine = true
                space = if ((currLine == 0 || isPassageStart(currLine)) && isSwitchIndent) {
                    ((2 * (normalCharWidth + CHAR_SPACE))).toInt()
                } else {
                    0
                }
                startX = (currIndent + space)
            } else {
                isCurrLine = false
                space = if ((currLine == 0 || isPassageStart(currLine)) && isSwitchIndent) {
                    ((2 * (normalCharWidth + CHAR_SPACE))).toInt()
                } else {
                    0
                }
                startX = (normalIndent + space)
            }
            if (!drawCurrSelect && isCurrLine) {
                drawCurrSelect = true
                val bgStroke = 4f
                val strokeWidth = mCurrPaint.strokeWidth
                mCurrPaint.setShader(mSelectBgShader)
                mCurrPaint.strokeWidth = bgStroke
                val spacing = abs(mCurrPaint.fontMetrics.ascent) / 2f
                val top = startY - abs(mCurrPaint.fontMetrics.top) - spacing
                val bottom = startY + abs(mCurrPaint.fontMetrics.bottom) + spacing
                canvas.drawRect(0f, top, width.toFloat(), bottom, mCurrPaint)
                mCurrPaint.shader = mSelectLineShader
                canvas.drawLine(0f, top, width.toFloat(), top, mCurrPaint)
                canvas.drawLine(0f, bottom, width.toFloat(), bottom, mCurrPaint)
                mCurrPaint.strokeWidth = strokeWidth
                mCurrPaint.shader = null
            }
            var dataBean = mOriDataBean.get(index)
            val textColor = dataBean?.color ?: normalColor
            lineWidth += if (isCurrLine) {
                mCurrPaint.color = textColor
                mCurrPaint.alpha = CURR_TEXT_ALPHA
                canvas.drawText(s.toString(), startX + lineWidth, startY, mCurrPaint)
                drawMark(startX + lineWidth, startY, index, canvas, isCurrLine)
                currCharWidth + CHAR_SPACE
            } else {
                mPaint.color = textColor
                mPaint.alpha = NOR_TEXT_ALPHA
                canvas.drawText(s.toString(), startX + lineWidth, startY, mPaint)
                drawMark(startX + lineWidth, startY, index, canvas, isCurrLine)
                normalCharWidth + CHAR_SPACE
            }
            lineCharNums++
            if (isLineSeparator(s) || lineCharNums == getCurrLineNums(currLine)) {//换行符和到最大的字数
                if (isLineSeparator(s) && lineCharNums == 1) {//满字数换行之后遇上换行符 省略
                    lineCharNums = 0
                    lineWidth = 0f
                    continue
                }
                lineCharNums = 0
                lineWidth = 0f
                startY += if (isCurrLine) {
                    currCharHeight + CURR_LINE_SPACING
                } else {
                    normalCharHeight + CURR_LINE_SPACING
                }
                line++
            }
        }
    }


    private fun drawMark(
        textX: Float,
        textY: Float,
        index: Int,
        canvas: Canvas,
        isCurrLine: Boolean
    ) {
        val containList = mutableListOf<SparseArray<EvaViewBean>>()
        mModels.forEach { _, value ->
            if (value.containsKey(index)) {
                containList.add(value)
            }
        }
        if (containList.isEmpty()) return
        containList.forEach {
            val value = it.get(index)
            if (value != null) {
                val markDrawBean = MarkFactory.create(value.mark)
                val paint = if (isCurrLine) {
                    mCurrPaint.color = mMarkPaint.color
                    mCurrPaint
                } else {
                    mMarkPaint
                }
                val height = if (isCurrLine) {
                    currCharHeight
                } else {
                    normalCharHeight
                }
                val width = if (isCurrLine) {
                    currCharWidth
                } else {
                    normalCharWidth
                }
                if (markDrawBean is RisingToneMark) {
                    markDrawBean.setBitmap(rising)
                }
                if (markDrawBean is FallingToneMark) {
                    markDrawBean.setBitmap(falling)
                }
                markDrawBean.draw(context, textX, textY, canvas, paint.apply {
                    alpha = if (isCurrLine) {
                        CURR_TEXT_ALPHA
                    } else {
                        NOR_TEXT_ALPHA
                    }
                }, width, height, CHAR_SPACE)
                mCurrPaint.color = normalColor
            }
        }

    }

    /**
     * @return 返回屏幕坐标
     * */
    fun getCurrPos(): SizeF {
        val location = IntArray(2)
        getLocationOnScreen(location)
        val x = (width - currLineWidth) / 2 + currLineWidth + location[0]
        val y =
            (mCurrReadLine) * (normalCharHeight + CURR_LINE_SPACING) + currCharHeight / 2 + location[1]
        return SizeF(x, y)
    }


    var mScrollCount = 0f
    var mSingleScroll = 0f


    private fun isLineSeparator(char: Char) = char.toString() == System.lineSeparator()

    private fun getCurrLine(index: Int): Int {
        mLineRange.forEach { key, value ->
            if (index in value) return key
        }
        return -1
    }

    private fun getCurrLineNums(index: Int): Int {
        return mLineNums.get(index)
    }

    private fun isPassageStart(index: Int) = mPassageStart[index]


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mLastRealTimeSize = 0
        viewTreeObserver.removeOnPreDrawListener(this)
        rising?.recycle()
        falling?.recycle()
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
        }
    }

    class TextCharBean {
        var evaIndex: Int = -1 //测评index
        var oriIndex: Int = -1 //标记文本index
        var color: Int = Color.WHITE //需要的颜色
        var word: String = ""
    }

    override fun onPreDraw(): Boolean {
        normalCharWidth = mPaint.measureText("啊")
        currCharWidth = mCurrPaint.measureText("啊")
        normalLineWidth = maxLineNum * normalCharWidth + (maxLineNum - 1) * CHAR_SPACE
        currLineWidth = maxLineNum * currCharWidth + (maxLineNum - 1) * CHAR_SPACE
        normalIndent = (width - normalLineWidth) / 2f
        currIndent = (width - currLineWidth) / 2f
        if (::mLineNums.isInitialized && mLineNums.isNotEmpty()) {
            mSingleScroll =
                ((mLineRange.size() - 1) * (CURR_LINE_SPACING + normalCharHeight) + currCharHeight) / mLineRange.size()
        }
        return true
    }
}


