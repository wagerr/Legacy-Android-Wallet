package com.wagerr.wallet.ui.bet.result

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.BET_ATCION_DRAW
import com.wagerr.wallet.data.bet.toBetActionAmount

class BetEventBetActionAdapter : BaseQuickAdapter<BetEventDetailData, BaseViewHolder>(R.layout.item_finished_bet_event_bet_action) {

    override fun convert(helper: BaseViewHolder, item: BetEventDetailData) {
        item.betEvent?.let {
            when (item.betAction?.betChoose) {
                it.homeTeam -> {
                    helper.setText(R.id.text_your_choice, "BET ${it.homeTeam} WIN (${it.homeOdds})")
                }
                it.awayTeam -> {
                    helper.setText(R.id.text_your_choice, "BET ${it.awayTeam} WIN (${it.awayOdds})")
                }
                BET_ATCION_DRAW -> {
                    helper.setText(R.id.text_your_choice, "BET DRAW (${it.drawOdds})")
                }
            }
            helper.setText(R.id.text_amount, item.betAction?.transaction?.toBetActionAmount()?.toFriendlyString())

        }


    }

}
