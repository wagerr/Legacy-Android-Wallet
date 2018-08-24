package com.wagerr.wallet.ui.bet.result

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import com.wagerr.wallet.WagerrApplication
import wagerr.bet.BetData
import wagerr.bet.DRAW_SYMBOL
import wagerr.bet.isRefund
import wagerr.bet.toBetActionAmount

class BetEventBetActionAdapter :
        BaseQuickAdapter<BetData.BetActionData, BaseViewHolder>(R.layout.item_finished_bet_event_bet_action) {

    override fun convert(helper: BaseViewHolder, item: BetData.BetActionData) {
        item.betEvent?.let {
            when (item.betAction.betChoose) {
                it.homeTeam -> {
                    helper.setText(R.id.text_your_choice, "BET ${it.homeTeam} WIN (${it.homeOdds})")
                }
                it.awayTeam -> {
                    helper.setText(R.id.text_your_choice, "BET ${it.awayTeam} WIN (${it.awayOdds})")
                }
                DRAW_SYMBOL -> {
                    helper.setText(R.id.text_your_choice, "BET DRAW (${it.drawOdds})")
                }
            }
            helper.setText(R.id.text_amount, item.betAction.transaction.toBetActionAmount()?.toFriendlyString())
            if (item.betResult != null) {
                if (item.betResult!!.isRefund()) {
                    helper.setText(R.id.text_reward, "REFUND ${item.betAction.transaction.getOutput(0).value.toFriendlyString()}")
                } else if (item.betAction.betChoose == item.betResult!!.betResult) {
                    item.betReward?.rewards?.let {
                        helper.setText(R.id.text_reward, "WIN ${it[0].amount.toFriendlyString()}")
                    } ?: run {
                        helper.setText(R.id.text_reward, "WIN (NOT PAID)")
                    }
                } else {
                    helper.setText(R.id.text_reward, "LOSE")
                }
            } else {
                helper.setText(R.id.text_reward, "WAITING")
            }
        }
    }
}
