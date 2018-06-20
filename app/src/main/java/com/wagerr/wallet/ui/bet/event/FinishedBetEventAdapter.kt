package com.wagerr.wallet.ui.bet.event

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.BetEvent
import com.wagerr.wallet.data.bet.toEventSymbol
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults
import java.text.SimpleDateFormat
import java.util.*

class FinishedBetEventAdapter : BaseQuickAdapter<FinishedBetData, BaseViewHolder>(R.layout.item_finished_bet_event) {

    override fun convert(helper: BaseViewHolder, item: FinishedBetData) {

        val eventSymbol = item.betEvent.eventLeague.toEventSymbol()
        helper.setText(R.id.text_event_league, eventSymbol.getFullEventLeague())
        helper.setText(R.id.text_event_info, eventSymbol.getFullEventInfo(item.betEvent.eventInfo))
        helper.setText(R.id.text_time, Date(item.betEvent.timeStamp).formatToViewDateTimeDefaults())
        helper.setText(R.id.text_home_team, eventSymbol.getFullTeam(item.betEvent.homeTeam))
        helper.setText(R.id.text_away_team, eventSymbol.getFullTeam(item.betEvent.awayTeam))
        eventSymbol.getTeamImage(item.betEvent.homeTeam)?.let {
            helper.setImageResource(R.id.image_home_team, it)
        }
        eventSymbol.getTeamImage(item.betEvent.awayTeam)?.let {
            helper.setImageResource(R.id.image_away_team, it)
        }
//        item.match.score1?.let {
//            if (item.match.team1.code == item.betEvent.homeTeam) {
//                helper.setText(R.id.text_score, "${item.match.score1}:${item.match.score2}")
//            } else {
//                helper.setText(R.id.text_score, "${item.match.score2}:${item.match.score1}")
//            }
//        } ?: run {
//            helper.setText(R.id.text_score, "VS")
//        }

        item.betResult?.let {
            if (item.betResult.betResult == "D") {
                helper.setText(R.id.button_status, "DRAW")
            } else {
                helper.setText(R.id.button_status, "${item.betResult.betResult}")
            }
        } ?: run {
            helper.setText(R.id.button_status, "Waiting For Oracle Result")
        }
    }

}

