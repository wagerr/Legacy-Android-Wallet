package wagerr.bet



sealed class TxType{
    object TxTypeEvent: TxType()
    object TxTypeBet: TxType()
    object TxTypeResult: TxType()
}