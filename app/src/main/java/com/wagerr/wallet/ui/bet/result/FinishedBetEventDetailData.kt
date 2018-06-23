package com.wagerr.wallet.ui.bet.result

import com.wagerr.wallet.data.bet.BetAction
import com.wagerr.wallet.data.bet.BetEvent
import com.wagerr.wallet.data.bet.BetResult
import com.wagerr.wallet.data.bet.BetMatchResult

data class FinishedBetEventDetailData(val betEvent: BetEvent?, val betResult: BetResult?, val betAction: BetAction?)