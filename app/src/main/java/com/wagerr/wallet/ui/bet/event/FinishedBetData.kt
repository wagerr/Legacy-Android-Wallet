package com.wagerr.wallet.ui.bet.event

import com.wagerr.wallet.data.bet.BetEvent
import com.wagerr.wallet.data.bet.BetResult
import com.wagerr.wallet.data.bet.TxType
import com.wagerr.wallet.data.worldcup.model.Match

data class FinishedBetData(val betEvent: BetEvent, val betResult: BetResult?)