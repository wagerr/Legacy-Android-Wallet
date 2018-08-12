package wagerr.bet

import global.WagerrCoreContext
import org.wagerrj.core.Coin
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
        }?.sortedByDescending { it.timeStamp }?.distinctBy { it.eventId }?.sortedBy { it.timeStamp }
    }

    fun getFinishedBetEvents(): List<BetEvent>? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.timeStamp < System.currentTimeMillis() + WagerrCoreContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME
        }?.sortedByDescending { it.timeStamp }?.distinctBy { it.eventId }?.sortedBy { it.timeStamp }
    }

    //get bet event exact before the bet action with match odds
    fun getBetEventByIdAndTime(eventId: String, betTimeInMillis: Long): BetEvent? {
        return walletManager.watchedSpent.filter {
            betTimeInMillis > it.updateTime.time
        }.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.timeStamp }?.last()
    }

    //get bet event exact before the bet action with match odds
    fun getBetEventsById(eventId: String): List<BetEvent>? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.timeStamp }
    }

    fun getBetEventById(eventId: String): BetEvent? {
        return walletManager.watchedSpent.toBetEvents()?.filter {
            it.eventId == eventId
        }?.sortedBy { it.timeStamp }?.first()
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

    fun getBetRewardByEventId(eventId: String): BetReward? {
        val betResult = getBetResultByEventId(eventId) ?: return null
        val transaction = walletManager.mineReceived?.filter {
            it.isCoinStake && it.updateTime > betResult.transaction.updateTime // no need to check for pos
        }?.sortedBy { it.updateTime }?.first() ?: return null
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

    fun getBetRewardByBetActionAndEvent(betAction: BetAction, betEvent: BetEvent): BetReward? {
        val betResult = getBetResultByEventId(betAction.eventId) ?: return null
        val rewardTransaction = walletManager.mineReceived?.filter {
            it.isCoinStake && it.updateTime > betResult.transaction.updateTime // no need to check for pos
        }?.sortedBy { it.updateTime }?.firstOrNull() ?: return null

        if (betAction.betChoose != betResult.betResult) {
            return BetReward(rewardTransaction, null)
        }
        var rewardOutpoints = mutableListOf<BetRewardOutpoint>()
        val betAmount = betAction.transaction.getOutput(0).value.value
        var odds: Double
        if (betAction.betChoose == betEvent.homeTeam) {
            odds = betEvent.homeOdds
        } else if (betAction.betChoose == betEvent.awayTeam) {
            odds = betEvent.awayOdds
        } else {
            odds = betEvent.drawOdds
        }

        val expectReward = (betAmount * odds - (betAmount * odds - betAmount) * 0.06).toLong()

        for (output in rewardTransaction.outputs) {
            if (output.isMine(walletManager.wallet)) {
                val actionChangeAddress = betAction.transaction.getOutput(1).getAddressFromP2PKHScript(WagerrCoreContext.NETWORK_PARAMETERS)
                val rewardAddress = output.getAddressFromP2PKHScript(WagerrCoreContext.NETWORK_PARAMETERS)
                if (actionChangeAddress != null && rewardAddress != null && actionChangeAddress == rewardAddress) {
                    rewardOutpoints.add(BetRewardOutpoint(rewardAddress, Coin.valueOf(expectReward)))
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

    fun getBetFullDataByEventId(eventId: String): BetData.BetFullData {
        return BetData.BetFullData(getBetEventsById(eventId), getBetActionsByEventId(eventId),
                getBetResultByEventId(eventId), getBetRewardByEventId(eventId))
    }

    fun getBetFullDataByRewardTransaction(transaction: Transaction): BetData.BetFullData? {
        val betResult = getBetResults()?.filter {
            it.transaction.updateTime < transaction.updateTime
        }?.sortedBy { it.transaction.updateTime }?.last()
        return if (betResult == null) {
            null
        } else {
            return getBetFullDataByEventId(betResult.eventId)
        }
    }

    fun getBetActionDataByActionTransaction(transaction: Transaction): BetData.BetActionData? {
        val betAction = transaction.toBetAction() ?: return null
        val betEvent = getBetEventsById(betAction.eventId)?.last { betEvent ->
            betAction.transaction.updateTime > betEvent.transaction.updateTime
        }
        return BetData.BetActionData(betEvent, betAction,
                getBetResultByEventId(betAction.eventId), getBetRewardByBetActionAndEvent(betAction, betEvent!!))
    }
}