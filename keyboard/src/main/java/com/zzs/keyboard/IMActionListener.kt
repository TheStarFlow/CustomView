package com.zzs.keyboard

import android.content.Context
import android.view.View
import android.view.inputmethod.InputConnection

/**
@author  zzs
@Date 2022/7/6
@describe
 */
interface IMActionListener {

    fun onCreateView(context: Context,im:IMWrapper): View

    fun onIMWindowShown(conn:InputConnection)

    fun onIMWindowHidden()

    fun onBindInput(conn: InputConnection)

    fun onUnbindInput()

}