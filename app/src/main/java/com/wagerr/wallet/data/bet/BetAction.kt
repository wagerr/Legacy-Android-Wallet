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
