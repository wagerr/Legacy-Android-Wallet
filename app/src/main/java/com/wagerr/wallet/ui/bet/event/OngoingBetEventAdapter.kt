package com.wagerr.wallet.ui.bet.event

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import wagerr.bet.BetEvent
import com.wagerr.wallet.data.bet.toEventSymbol
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults
import java.util.*

class OngoingBetEventAdapter : BaseQuickAdapter<BetEvent, BaseViewHolder>(R.layout.item_bet_event) {

    override fun convert(helper: BaseViewHolder, item: BetEvent) {

        val eventSymbol = item.eventLeague.toEventSymbol()
        helper.setText(R.id.text_event_league,eventSymbol.getFullEventLeague(item.eventLeague))
        helper.setText(R.id.text_event_info, eventSymbol.getFullEventInfo(item.eventInfo))
        helper.setText(R.id.text_time, Date(item.timeStamp).formatToViewDateTimeDefaults())
        helper.setText(R.id.text_home_team, eventSymbol.getFullTeam(item.homeTeam))
        helper.setText(R.id.text_away_team, eventSymbol.getFullTeam(item.awayTeam))

        helper.setText(R.id.button_home_odds, "${item.homeTeam} WIN\n${item.homeOdds}")
        helper.setText(R.id.button_draw_odds, "DRAW\n${item.drawOdds}")
        helper.setText(R.id.button_away_odds, "${item.awayTeam} WIN\n${item.awayOdds}")
        helper.addOnClickListener(R.id.button_home_odds)
                .addOnClickListener(R.id.button_draw_odds)
                .addOnClickListener(R.id.button_away_odds)


    }

}
