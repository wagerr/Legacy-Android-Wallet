package com.wagerr.wallet.ui.bet.event

import wagerr.bet.BetEvent
import wagerr.bet.BetResult

data class FinishedBetData(val betEvent: BetEvent, val betResult: BetResult?)