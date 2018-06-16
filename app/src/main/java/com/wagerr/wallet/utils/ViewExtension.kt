package com.wagerr.wallet.utils

import android.app.Dialog
import android.view.WindowManager
import android.view.Gravity



fun Dialog.wrapContent() {
    val window = this.getWindow()
    window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
    window.setGravity(Gravity.CENTER)
}