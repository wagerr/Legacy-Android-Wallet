package com.wagerr.wallet.data.bet

import android.text.TextUtils
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.module.WagerrContext
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Coin
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

const val BET_ATCION_DRAW = "D"

//2|1.0|#453|RUS
data class BetAction(val txType: TxType, val protocolVersion: String, val eventId: String,
                     val betChoose: String, val transaction: Transaction)

data class BetActionForSend(val eventId: String, val betChoose: String)

fun BetActionForSend.toBetTransactionData(): String {
    return "2|1.0|${this.eventId}|${this.betChoose}"
}

fun Transaction.toBetAction(): BetAction? {
    if (this.isBetAction()) {
        val items = this.getBetActionString().split("|").toMutableList()
        if (items[3] == "NIG") { // mistake from wagerr team in the testnet
            items[3] = "NGA"
        }
        if (items[3] == "NIG") {
            items[3] = "NGA"
        }
        return BetAction(TxType.TxTypeBet, items[1], items[2], items[3], this)
    } else {
        return null
    }
}

fun List<Transaction>.toBetActions(): List<BetAction>? {
    return this.filter {
        it.updateTime.time > WagerrContext.ORACLE_BET_EVENT_START_TIME
    }.mapNotNull {
        it.toBetAction()
    }
}

fun List<Transaction>.getBetActionsByEventId(eventId: String): List<BetAction>? {
    return this.toBetActions()
            ?.filter {
                eventId == it.eventId
            }
}

fun Transaction.toBetActionAmount(): Coin? {
    if (this.isBetAction()) {
        val item = this.outputs.filter {
            it.scriptPubKey.isOpReturn
        }[0]
        return item.value
    } else {
        return null
    }
}

fun Transaction.isBetAction(): Boolean {
    return this.getBetActionString().isValidBetActionSource()
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