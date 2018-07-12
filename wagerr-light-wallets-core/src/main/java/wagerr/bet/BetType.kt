package wagerr.bet



sealed class BetType{
    object BetTypeHomeWin:BetType()
    object BetTypeDraw:BetType()
    object BetTypeAwayWin:BetType()
}