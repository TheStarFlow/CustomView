package com.zzs.keyboard

import android.content.Context
import android.inputmethodservice.Keyboard
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import java.lang.ref.WeakReference

/**
@author  zzs
@Date 2022/7/5
@describe
 */
class KeyBoardManager private constructor() : IMActionListener, IMManagerWrapper {

    private var mCurrKeyboardView: MyKeyboardView? = null
    private var inputView: View? = null
    private var mCurrInputConnection: InputConnection? = null
    private var mCurrType = KB_TYPE_NUM_ABC_LOW
    private var mContext: Context? = null
    private val mKeyboardCache by lazy { SparseArray<WeakReference<Keyboard>>() }
    private var mCurrKeyboardAction:KeyboardAction?=null
    private val xml by lazy {
        SparseIntArray().apply {
            put(KB_TYPE_NUM_ABC_LOW, R.xml.land_keyboar_num_abc_low)
            put(KB_TYPE_NUM_ABC_UPPER, R.xml.land_keyboar_num_abc_upper)
            put(KB_TYPE_NUM_SYMBOL_1, R.xml.land_keyboard_num_symbol_1)
            put(KB_TYPE_NUM_SYMBOL_2, R.xml.land_keyboard_num_symbol_2)
        }
    }

    companion object {

        private val sInstance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { KeyBoardManager() }

        fun get() = sInstance

        const val KB_TYPE_NUM_ABC_LOW = 0X01
        const val KB_TYPE_NUM_ABC_UPPER = 0X02
        const val KB_TYPE_NUM_SYMBOL_1 = 0X03
        const val KB_TYPE_NUM_SYMBOL_2 = 0X04
    }

    override fun onCreateView(context: Context, im: IMWrapper): View {
        mContext = context
        if (inputView == null) {
            inputView =
                LayoutInflater.from(context).inflate(R.layout.layout_input_view, null, false)
            mCurrKeyboardView = inputView?.findViewById<MyKeyboardView>(R.id.sKeyboard)
            inputView?.findViewById<View>(R.id.down)?.setOnClickListener { im.doHideWindow() }
        }
        mCurrKeyboardView?.run {
            isProximityCorrectionEnabled = false
            onKeyboardActionListener =
                KeyboardAction(mCurrInputConnection, im, this@KeyBoardManager)
                    .also { setOnKeyDrawListener(it)
                    mCurrKeyboardAction = it}
        }
        getTargetKeyboard(mCurrType)?.also { mCurrKeyboardView?.keyboard = it }
        return inputView!!
    }

    override fun onIMWindowShown(conn: InputConnection) {
        mCurrInputConnection = conn
        mCurrKeyboardAction?.mConn = conn
        val kb = getTargetKeyboard(mCurrType) ?: return
        mCurrKeyboardView?.keyboard = kb
        mCurrKeyboardView?.isPreviewEnabled = true
    }

    private fun getTargetKeyboard(currType: Int): Keyboard? {
        mContext ?: return null
        var kb = mKeyboardCache.get(currType)
        if (kb?.get() == null) {
            kb = WeakReference(Keyboard(mContext, xml.get(currType)))
            mKeyboardCache.put(currType, kb)
        }
        return handleKeyBoard(kb.get())
    }

    private fun handleKeyBoard(kb: Keyboard?): Keyboard? {
        kb?.run {
           for (key in keys){
               if (key.codes.isEmpty())continue
               when(key.codes[0]){
                   KeyboardAction.KEY_ACTION_DEL->{}
                   KeyboardAction.KEY_CONFIRM->{}
                   KeyboardAction.KEY_ABC_NUM_SWITCH->{}
                   KeyboardAction.KEY_ACTION_UPPER->{}
                   KeyboardAction.KEY_SPACE->{}
                   else ->{
                       key.text = key.label
                   }
               }
           }
        }
        return kb
    }

    override fun onIMWindowHidden() {
        mCurrType = KB_TYPE_NUM_ABC_LOW
    }

    override fun onBindInput(conn: InputConnection) {
        mCurrInputConnection = conn
    }

    override fun onUnbindInput() {
        mCurrKeyboardView?.isPreviewEnabled = false
    }

    override fun onSwitchKeyboard(type: Int) {
        mCurrType = type
        val kb = getTargetKeyboard(type) ?: return
        mCurrKeyboardView?.keyboard = kb
    }

    override fun onPreviewChange(enable: Boolean) {
        mCurrKeyboardView?.isPreviewEnabled = enable
    }

    override fun isUpperCase(): Boolean {
        return mCurrType == KB_TYPE_NUM_ABC_UPPER
    }

    override fun getCurrKeyBoardType(): Int {
        return mCurrType
    }
}