package com.wagerr.wallet.data.bet

import android.text.TextUtils
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.module.WagerrContext
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

const val DRAW_SYMBOL = "DRW"

//3|1.0|#453|RUS
data class BetResult(val txType: TxType, val protocolVersion: String, val eventId: String,
                     val betResult: String, val transaction: Transaction)


fun Transaction.toBetResult(): BetResult? {
    if (this.isBetResult()) {
        val items = this.getBetResultString().split("|")
        return BetResult(TxType.TxTypeBet, items[1], items[2], items[3], this)
    } else {
        return null
    }
}

fun List<Transaction>.toBetResults(): List<BetResult>? {
    return this.filter {
        it.updateTime.time > WagerrContext.ORACLE_BET_EVENT_START_TIME
    }.mapNotNull {
        it.toBetResult()
    }
}

fun List<Transaction>.getBetResultByEventId(eventId: String): BetResult? {
    return this.toBetResults()?.firstOrNull {
                it.eventId == eventId
            }
}

fun Transaction.isBetResult(): Boolean {
    return this.getBetResultString().isValidBetResultSource()
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
    try {
        Integer.parseInt(this.split("|")[2].replace("#", ""))
    } catch (e: NumberFormatException) {
        return false
    }
    for (s in this.split("|")) {
        if (TextUtils.isEmpty(s)) {
            return false
        }
    }
    return true
}