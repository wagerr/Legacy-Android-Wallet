package com.wagerr.wallet.ui.bet.event

import wagerr.bet.BetEvent
import wagerr.bet.BetResult
import com.wagerr.wallet.data.bet.BetMatchResult

data class FinishedBetData(val betEvent: BetEvent, val betResult: BetResult?, val betMatchResult: BetMatchResult?)