package wagerr.bet

import global.WagerrCoreContext
import org.wagerrj.core.Transaction
import wallet.WalletManager

class BetManager(val walletManager: WalletManager) {

    fun getBetActionsByEventId(eventId: String): List<BetAction>? {
        return walletManager.mineSpent
                .toBetActions()
                ?.filter {
                    eventId == it.eventId
                }
    }

    //get all the bet events with newest odds, filter out the duplicate old one
    fun getCanBetBetEvents(): List<BetEvent>? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.timeStamp > System.currentTimeMillis() + WagerrCoreContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME
        }?.sortedByDescending { it.transaction.updateTime.time }?.distinctBy { it.eventId }?.filter {
            !(it.homeOdds.compareTo(0) == 0 && it.drawOdds.compareTo(0) == 0 && it.awayOdds.compareTo(0) == 0)
        }
    }

    fun getFinishedBetEvents(): List<BetEvent>? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.timeStamp < System.currentTimeMillis() + WagerrCoreContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME
        }?.sortedByDescending { it.transaction.updateTime.time }?.distinctBy { it.eventId }
    }

    //get bet event exact before the bet action with match odds
    fun getBetEventByIdAndTime(eventId: String, betTimeInMillis: Long): BetEvent? {
        return walletManager.watchedSpent.filter {
            betTimeInMillis > it.updateTime.time
        }.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.transaction.updateTime.time }?.last()
    }

    //get bet event exact before the bet action with match odds
    fun getBetEventsById(eventId: String): List<BetEvent>? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.transaction.updateTime.time }
    }

    fun getBetEventByBetAction(betAction: BetAction): BetEvent? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == betAction.eventId
        }?.sortedBy { it.transaction.updateTime.time }?.last { betEvent ->
            betAction.transaction.updateTime.time >
                    betEvent.transaction.updateTime.time
        }
    }

    fun getBetEventById(eventId: String): BetEvent? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.transaction.updateTime.time }?.first()
    }

    fun getLatestBetEventById(eventId: String): BetEvent? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedByDescending { it.transaction.updateTime.time }?.first()
    }

    fun getBetResults(): List<BetResult>? {
        return walletManager.watchedSpent.toBetResults()?.sortedBy {
            it.transaction.updateTime
        }
    }

    fun getBetResultByEventId(eventId: String): BetResult? {
        return walletManager.watchedSpent
                .toBetResults()?.firstOrNull {
                    it.eventId == eventId
                }
    }

    // won't be multiple betresult in same block
    fun getBetResultByRewardTransaction(transaction: Transaction): BetResult? {
        return walletManager.watchedSpent.toBetResults()?.firstOrNull {
            //update time is not correct, we use block height here
            it.transaction.confidence.appearedAtChainHeight ==
                    transaction.confidence.appearedAtChainHeight - 1
        }
    }

    private fun getBetRewardByRewardTransaction(transaction: Transaction): BetReward? {
        val rewardOutpoints = mutableListOf<BetRewardOutpoint>()
        for (output in transaction.outputs) {
            if (output.isMine(walletManager.wallet)) {
                val addressFromP2SH = output.getAddressFromP2SH(WagerrCoreContext.NETWORK_PARAMETERS)
                if (addressFromP2SH != null) {
                    rewardOutpoints.add(BetRewardOutpoint(addressFromP2SH, output.value))
                }
            }
        }
        if (rewardOutpoints.isEmpty()) {
            return BetReward(transaction, null)
        } else {
            return BetReward(transaction, rewardOutpoints)
        }
    }

    private fun getBetRewardByBetActionAndEventAndResult(betAction: BetAction, betEvent: BetEvent, betResult: BetResult?): BetReward? {
        betResult ?: return null
        val rewardTransaction = walletManager.mineReceived?.firstOrNull {
            it.isCoinStake && it.confidence.appearedAtChainHeight == betResult.transaction.confidence.appearedAtChainHeight + 1 // no need to check for pos
        } ?: return null

        if (betAction.betChoose != betResult.betResult) {
            return BetReward(rewardTransaction, null)
        }
        var rewardOutpoints = mutableListOf<BetRewardOutpoint>()
        val betAmount = betAction.transaction.getOutput(0).value
        var odds: Double
        if (betAction.betChoose == betEvent.homeTeam) {
            odds = betEvent.homeOdds
        } else if (betAction.betChoose == betEvent.awayTeam) {
            odds = betEvent.awayOdds
        } else {
            odds = betEvent.drawOdds
        }

        val expectReward = betAmount.multiply((odds*10000).toLong()).div(10000).multiply(94).div(100)
                .plus(betAmount.multiply(6).div(100))

        for (output in rewardTransaction.outputs) {
            if (output.isMine(walletManager.wallet)) {
                val betRewardAddress = betAction.transaction.getInput(0).getFromAddress()
                val rewardAddress = output.getAddressFromP2PKHScript(WagerrCoreContext.NETWORK_PARAMETERS)
                if (betRewardAddress != null && rewardAddress != null && betRewardAddress == rewardAddress
                        && expectReward == output.value) {
                    rewardOutpoints.add(BetRewardOutpoint(rewardAddress,output.value))
                    break
                }
            }
        }
        if (rewardOutpoints.isEmpty()) {
            return BetReward(rewardTransaction, null)
        } else {
            return BetReward(rewardTransaction, rewardOutpoints)
        }
    }

    fun getBetFullDataByRewardTransaction(transaction: Transaction): BetData.BetFullData? {
        val betResult = getBetResultByRewardTransaction(transaction)
        if (betResult == null) {
            return null
        } else {
            val eventId = betResult.eventId
            val betEvents = getBetEventsById(eventId)
            val betActions = getBetActionsByEventId(eventId)
            val betReward = getBetRewardByRewardTransaction(transaction)
            return BetData.BetFullData(betEvents, betActions,
                    betResult, betReward)
        }
    }

    fun getBetActionDataByActionTransaction(transaction: Transaction): BetData.BetActionData? {
        val betAction = transaction.toBetAction() ?: return null
        val betEvent = getBetEventByBetAction(betAction)
        val betResult = getBetResultByEventId(betAction.eventId)
        val betReward = getBetRewardByBetActionAndEventAndResult(betAction, betEvent!!, betResult)
        return BetData.BetActionData(betEvent, betAction,
                betResult, betReward)
    }
}