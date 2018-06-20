package com.wagerr.wallet.utils

import java.text.SimpleDateFormat
import java.util.*


fun Date.formatToViewDateTimeDefaults(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(this)
}

