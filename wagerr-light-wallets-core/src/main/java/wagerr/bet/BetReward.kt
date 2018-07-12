package wagerr.bet

import org.wagerrj.core.Address
import org.wagerrj.core.Transaction
import org.wagerrj.core.Coin

data class BetReward(val transaction: Transaction, val rewards: List<BetRewardOutpoint>?)
data class BetRewardOutpoint(val address: Address, val amount: Coin)
open class BetData {
    data class BetFullData(val betEvents: List<BetEvent>?, val betActions: List<BetAction>?,
                           val betResult: BetResult?, val betReward: BetReward?) : BetData()

    data class BetActionData(val betEvent: BetEvent?, val betAction: BetAction,
                             val betResult: BetResult?, val betReward: BetReward?) : BetData()
}