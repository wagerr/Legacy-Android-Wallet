package com.wagerr.wallet.data.bet

import android.text.TextUtils
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

//2|1.0|#453|RUS
data class BetTransactionData(val txType: TxType, val protocolVersion: String, val eventId: String,
                              val betChoose: String)

data class BetAction(val eventId: String, val betChoose: String)

fun BetAction.toBetTransactionData(): String {
    return "2|1.0|${this.eventId}|${this.betChoose}"
}

fun String.toBetAction(): BetAction {
    val items = this.split("|")
    return BetAction(items[2], items[3])
}

fun Transaction.getBetActionString(): String {
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

fun String.isValidBetActionSource(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
    if (this.contains(",")) {
        return false
    }
    if (!this.startsWith("2")) {
        return false
    }
    for (s in this.split("|")) {
        if (TextUtils.isEmpty(s)) {
            return false
        }
    }
    return true
}