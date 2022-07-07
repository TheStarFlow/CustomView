package com.zzs.keyboard

/**
@author  zzs
@Date 2022/7/6
@describe
 */
interface IMManagerWrapper {
    fun onSwitchKeyboard(type: Int)

    fun onPreviewChange(enable:Boolean)

    fun isUpperCase():Boolean

    fun getCurrKeyBoardType():Int
}