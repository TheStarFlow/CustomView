package com.gzhlsoft.yougureader.widget.evaluation

import android.content.Context
import android.graphics.Color
import android.util.SparseArray
import androidx.core.content.ContextCompat
import com.gzhlsoft.yougureader.R
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
@author  zzs
@Date 2022/2/9
@describe
 */
data class EvaViewBean(val mark: Mark) {
    var word: String = ""
    var index: Int = -1
    var contentIndex = -1

    companion object {
        const val TYPE_SQUARE = 7
        const val TYPE_DASH = 8
        const val TYPE_BRACKETS = 9
        const val SHORT_PAUSE = "[/]"
        const val LONG_PAUSE = "[//]"
        const val RISING_TONE = "[↗]"
        const val FALLING_TONE = "[↘]"
        const val CRESCENDO = "[<]"
        const val FADING = "[>]"
        const val READ_LIGHTLY = "<。>"
        const val READ_HEAVY = "<•>"
        const val RHYME = "<▴>"
        const val C_READING = "⌒"
        val SYMBOLS = ArrayList<String>().apply {
            add("[")
            add("]")
            add("/")
            add("↗")
            add("↘")
            add(">")
            add("<")
            add("•")
            add("▴")
            add("(")
            add(")")
            add("⌒")
        }
        val PASSAGE_SYMBOL = ArrayList<String>().apply {
            add("，")
            add("。")
            add("？")
            add("！")
            add("：")
            add("；")
            add("、")
            add(",")
            add(".")
            add(";")
            add("!")
            add("“")
            add(":")
        }

        fun handleContent(content: String): SparseArray<SparseArray<EvaViewBean>> {
            val squareModel = SparseArray<EvaViewBean>()
            val dashModel = SparseArray<EvaViewBean>()
            val bracketsModel = SparseArray<EvaViewBean>()
            val bracketsPattern: Pattern = Pattern.compile("(?<=\\()[^)]+")
            val squareBracketsPattern = Pattern.compile("\\[(.*?)]")
            val dashPattern = Pattern.compile("(<[^>]*>)")
            val bracketsMatcher: Matcher = bracketsPattern.matcher(content)
            val squareBracketsMatcher = squareBracketsPattern.matcher(content)
            val dashMatcher = dashPattern.matcher(content)
            var searchIndex = 0
            var startIndex = 0
            while (squareBracketsMatcher.find(searchIndex)) {
                val type = squareBracketsMatcher.group()
                val bean = EvaViewBean(handleSquare(type))
                val index = content.indexOf(type, startIndex)
                startIndex = index + 2
                searchIndex = index + 2
                val wordBean = findWord(index, content)
                bean.word = wordBean?.word ?: ""
                bean.index = wordBean?.index ?: -1
                squareModel.put(bean.index, bean)
            }
            searchIndex = 0
            startIndex = 0
            while (dashMatcher.find(searchIndex)) {
                val type = dashMatcher.group()
                val bean = EvaViewBean(handleDash(type))
                val index = content.indexOf(type, startIndex)
                searchIndex = index + 2
                startIndex = index + 2
                val wordBean = findWord(index, content)
                bean.word = wordBean?.word ?: ""
                bean.index = wordBean?.index ?: -1
                dashModel.put(bean.index, bean)
            }
            searchIndex = 0
            startIndex = 0
            while (bracketsMatcher.find(searchIndex)) {
                val type = bracketsMatcher.group()
                val bean = EvaViewBean(handleBrackets(type))
                val index = content.indexOf(type, startIndex)
                startIndex = index + 1
                searchIndex = index + 1
                val wordBean = findWord(index, content)
                bean.word = wordBean?.word ?: ""
                bean.index = wordBean?.index ?: -1
                bracketsModel.put(bean.index, bean)
            }
            val models = SparseArray<SparseArray<EvaViewBean>>()
            models.put(TYPE_BRACKETS, bracketsModel)
            models.put(TYPE_DASH, dashModel)
            models.put(TYPE_SQUARE, squareModel)
            return models
        }

        private fun handleBrackets(type: String): Mark {
            return when (type) {
                C_READING -> {
                    Mark.C_READING
                }
                else -> {
                    Mark.NONE
                }
            }
        }

        private fun findWord(index: Int, content: String): WordIndexBean? {
            var sIndex = index
            while (sIndex > 0) {
                val char = content[sIndex].toString()
                if (char in SYMBOLS) {
                    sIndex--
                    continue
                }
                return WordIndexBean(sIndex, char)
            }
            return null
        }

        /**
         * @param type 破折号类型
         * */
        private fun handleDash(type: String): Mark {
            return when (type) {
                READ_LIGHTLY -> {
                    Mark.READ_LIGHTLY
                }
                READ_HEAVY -> {
                    Mark.READ_HEAVY
                }
                RHYME -> {
                    Mark.RHYME
                }
                else -> {
                    Mark.NONE
                }
            }
        }

        /**
         * @param type 中括号类型
         * */
        private fun handleSquare(type: String): Mark {
            return when (type) {
                SHORT_PAUSE -> {
                    Mark.SHORT_PAUSE
                }
                LONG_PAUSE -> {
                    Mark.LONG_PAUSE
                }
                RISING_TONE -> {
                    Mark.RISING_TONE
                }
                FALLING_TONE -> {
                    Mark.FALLING_TONE
                }
                CRESCENDO -> {
                    Mark.CRESCENDO
                }
                FADING -> {
                    Mark.FADING
                }
                else -> {
                    Mark.NONE
                }
            }
        }

        fun getRealTimeEvaColor(score: Int, context: Context): Int {
            var color = Color.WHITE
            when {
                score > 69 -> {
                    color = ContextCompat.getColor(context, R.color.color_green48F057)
                }
                score >29 -> {
                    color = ContextCompat.getColor(context, R.color.color_orangeF9B630)
                }
                else-> {
                    color = ContextCompat.getColor(context, R.color.color_redFF724F)
                }
            }
            return color

        }
    }

}

data class WordIndexBean(val index: Int, val word: String)

enum class Mark {
    SHORT_PAUSE,//停顿
    LONG_PAUSE,//长停顿
    RISING_TONE,//升调
    FALLING_TONE,//降调
    CRESCENDO,//渐强
    FADING,//渐弱
    READ_LIGHTLY,//轻读
    READ_HEAVY,//重读
    RHYME,//韵脚
    C_READING,//连读
    NONE,
}
