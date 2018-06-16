package com.wagerr.wallet.data.bet



sealed class BetType{
    object BetTypeHomeWin:BetType()
    object BetTypeDraw:BetType()
    object BetTypeAwayWin:BetType()
}