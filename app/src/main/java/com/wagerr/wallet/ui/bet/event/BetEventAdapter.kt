package com.wagerr.wallet.ui.bet.event

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.toEventSymbol
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults
import wagerr.bet.BetEvent
import java.util.*

class BetEventAdapter : BaseQuickAdapter<BetEvent, BaseViewHolder>(R.layout.item_bet_event) {

    override fun convert(helper: BaseViewHolder, item: BetEvent) {

        val eventSymbol = item.eventLeague.toEventSymbol()
        helper.setText(R.id.text_event_league,eventSymbol.getFullEventLeague(item.eventLeague))
        helper.setText(R.id.text_event_info, eventSymbol.getFullEventInfo(item.eventInfo))
        helper.setText(R.id.text_time, Date(item.timeStamp).formatToViewDateTimeDefaults())
        helper.setText(R.id.text_home_team, eventSymbol.getFullTeam(item.homeTeam))
        helper.setText(R.id.text_away_team, eventSymbol.getFullTeam(item.awayTeam))
        eventSymbol.getTeamImage(item.homeTeam)?.let {
            helper.setImageResource(R.id.image_home_team, it)
        }
        eventSymbol.getTeamImage(item.awayTeam)?.let {
            helper.setImageResource(R.id.image_away_team, it)
        }
        helper.setText(R.id.button_home_odds, "${item.homeTeam} ${mContext.getString(R.string.bet_win)}\n${item.homeOdds}")
        helper.setText(R.id.button_draw_odds, "${mContext.getString(R.string.bet_draw)}\n${item.drawOdds}")
        helper.setText(R.id.button_away_odds, "${item.awayTeam} ${mContext.getString(R.string.bet_win)}\n${item.awayOdds}")
        helper.addOnClickListener(R.id.button_home_odds)
                .addOnClickListener(R.id.button_draw_odds)
                .addOnClickListener(R.id.button_away_odds)


    }

}
