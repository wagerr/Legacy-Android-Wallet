package com.wagerr.wallet.data.bet

import android.text.TextUtils
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

//1|1.0|#453|1528992000|WCUP|R1|RUS|KSA|1.33|11|4.5
data class BetEvent(val txType: TxType, val protocolVersion: String, val eventId: String,
                    val timeStamp: Long, val eventLeague: String, val eventInfo: String,
                    val homeTeam: String, val awayTeam: String, val homeOdds: Double,
                    val awayOdds: Double, val drawOdds: Double)

fun String.toBetEvent(): BetEvent? {
    val items = this.split("|")
    return BetEvent(TxType.TxTypeEvent, items[1], items[2], items[3].toLong() * 1000, items[4], items[5],
            items[6], items[7], items[8].toDouble(), items[9].toDouble(), items[10].toDouble())
}

fun Transaction.getBetEventString(): String? {
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

fun String.isValidBetEventSource(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
    if (this.contains(",")) {
        return false
    }
    if (!this.startsWith("1")) {
        return false
    }
    for (s in this.split("|")) {
        if (TextUtils.isEmpty(s)) {
            return false
        }
    }
    return true
}