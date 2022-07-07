package com.zzs.keyboard

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS

/**
@author  zzs
@Date 2022/7/5
@describe simple input
 */
class YGInputService : InputMethodService(), IMWrapper {
    companion object {
        private val TAG = "YGInputService"
    }

    private lateinit var mListener: IMActionListener

    override fun onCreateInputView(): View {
        Log.i(TAG, "onCreateInputView")
        return mListener.onCreateView(this, this)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        Log.i(TAG, "onWindowShown")
        mListener.onIMWindowShown(currentInputConnection)
    }

    override fun onWindowHidden() {
        Log.i(TAG, "onWindowHidden")
        super.onWindowHidden()
        mListener.onIMWindowHidden()
    }


    override fun onBindInput() {
        Log.i(TAG, "onBindInput")
        super.onBindInput()
        mListener = KeyBoardManager.get()
        mListener.onBindInput(currentInputConnection)
    }

    override fun onUnbindInput() {
        Log.i(TAG, "onUnbindInput")
        super.onUnbindInput()
        mListener.onUnbindInput()
    }

    override fun doHideWindow() {
        Log.i(TAG, "doHideWindow")
        hideWindow()
        requestHideSelf(HIDE_NOT_ALWAYS)
    }


}