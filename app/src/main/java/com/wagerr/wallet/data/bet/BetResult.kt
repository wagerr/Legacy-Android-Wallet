package com.wagerr.wallet.data.bet

import android.text.TextUtils
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

//3|1.0|#453|RUS
data class BetResult(val txType: TxType, val protocolVersion: String, val eventId: String,
                     val betResult: String)


fun String.toBetResult(): BetResult {
    val items = this.split("|")
    return BetResult(TxType.TxTypeResult, items[1], items[2], items[3])
}

fun Transaction.getBetResultString(): String {
    val items = this.outputs.filter {
        it.scriptPubKey.isOpReturn
    }
    if (items.isEmpty()) {
        return ""
    }
    val hexString = items[0].toString().substring(items[0].toString().indexOf("[") + 1, items[0].toString().indexOf("]"))
    val bytes = Hex.decodeHex(hexString.toCharArray())
    val string = String(bytes, Charset.forName("UTF-8"))
    return string
}

fun String.isValidBetResultSource(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
    if (this.contains(",")) {
        return false
    }
    if (!this.startsWith("3")) {
        return false
    }
    for (s in this.split("|")) {
        if (TextUtils.isEmpty(s)) {
            return false
        }
    }
    return true
}