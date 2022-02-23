package com.gzhlsoft.yougureader.widget.evaluation.mark

import com.gzhlsoft.yougureader.widget.evaluation.IMark
import com.gzhlsoft.yougureader.widget.evaluation.Mark

/**
@author  zzs
@Date 2022/2/11
@describe
 */
class MarkFactory {
    companion object {
        fun create(mark: Mark): IMark {
            return when (mark) {
                Mark.SHORT_PAUSE -> {
                    ShortPauseMark()
                }
                Mark.LONG_PAUSE -> {
                    LongPauseMark()
                }
                Mark.RISING_TONE -> {
                    RisingToneMark()
                }
                Mark.FALLING_TONE -> {
                    FallingToneMark()
                }
                Mark.CRESCENDO -> {
                    CrescendoMark()
                }
                Mark.FADING -> {
                    FadingMark()
                }
                Mark.READ_HEAVY -> {
                    ReadHeavyMark()
                }
                Mark.READ_LIGHTLY -> {
                    ReadLightlyMark()
                }
                Mark.RHYME -> {
                    RhymeMark()
                }
                Mark.C_READING -> {
                    CReadingMark()
                }
                else -> {
                    NoneMark()
                }

            }
        }
    }
}