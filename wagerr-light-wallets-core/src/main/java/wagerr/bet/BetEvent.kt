package wagerr.bet

import android.text.TextUtils
import global.WagerrCoreContext.ORACLE_BET_EVENT_START_TIME
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset

//1|1.0|#453|1528992000|WCUP|R1|RUS|KSA|1.33|11|4.5
data class BetEvent(val txType: TxType, val protocolVersion: String, val eventId: String,
                    val timeStamp: Long, val eventLeague: String, val eventInfo: String,
                    var homeTeam: String, var awayTeam: String, val homeOdds: Double,
                    val awayOdds: Double, val drawOdds: Double, val transaction: Transaction)

fun Transaction.toBetEvent(): BetEvent? {
    if (this.isBetEvent()) {
        val items = this.getBetEventString().split("|").toMutableList()
        if (items[6] == "NIG") { // mistake from wagerr team in the testnet
            items[6] = "NGA"
        }
        if (items[7] == "NIG") {
            items[7] = "NGA"
        }

        val homeOdds: Double
        val awayOdds: Double
        val drawOdds: Double

        if (items[8].toDouble() > 10000) { // wagerr team change odd format
            homeOdds = items[8].toDouble() / 10000
            awayOdds = items[9].toDouble() / 10000
            drawOdds = items[10].toDouble() / 10000
        } else {
            homeOdds = items[8].toDouble()
            awayOdds = items[9].toDouble()
            drawOdds = items[10].toDouble()
        }

        return BetEvent(TxType.TxTypeEvent, items[1], items[2], items[3].toLong() * 1000,
                items[4], items[5],
                items[6], items[7], homeOdds, awayOdds, drawOdds, this)
    } else {
        return null
    }
}

fun List<Transaction>.toBetEvents(): List<BetEvent>? {
    return this.filter {
        it.updateTime.time > ORACLE_BET_EVENT_START_TIME
    }.mapNotNull {
        it.toBetEvent()
    }
}

fun Transaction.isBetEvent(): Boolean {
    return this.getBetEventString().isValidBetEventSource()
}

fun Transaction.getBetEventString(): String {
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
    if (this.split("|").size != 11) {
        return false
    }

    return true
}