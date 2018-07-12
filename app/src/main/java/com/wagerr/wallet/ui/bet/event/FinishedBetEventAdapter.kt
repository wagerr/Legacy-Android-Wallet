package com.wagerr.wallet.ui.bet.event

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import wagerr.bet.DRAW_SYMBOL
import com.wagerr.wallet.data.bet.toEventSymbol
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults
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
            helper.setVisible(R.id.image_home_team, true)
            helper.setImageResource(R.id.image_home_team, it)
        }?: run {
            helper.setVisible(R.id.image_home_team, false)
        }
        eventSymbol.getTeamImage(item.betEvent.awayTeam)?.let {
            helper.setVisible(R.id.image_away_team, true)
            helper.setImageResource(R.id.image_away_team, it)
        }?: run {
            helper.setVisible(R.id.image_away_team, false)
        }
        item.betMatchResult?.homeScore?.let {
            helper.setText(R.id.text_score, "${item.betMatchResult.homeScore}:${item.betMatchResult.awayScore}")
        } ?: run {
            helper.setText(R.id.text_score, "VS")
        }

        item.betResult?.let {
            if (item.betResult.betResult == DRAW_SYMBOL) {
                helper.setText(R.id.button_status, "DRAW")
            } else {
                helper.setText(R.id.button_status, "${item.betResult.betResult} WIN")
            }
            helper.getView<TextView>(R.id.button_status).setTextAppearance(mContext, R.style.WgrButtonWithBorder)
        } ?: run {
            if (item.betEvent.timeStamp > System.currentTimeMillis()) {
                helper.setText(R.id.button_status, "Game Not Started")
                helper.getView<TextView>(R.id.button_status).setTextAppearance(mContext, R.style.WgrHintButtonWithBorder)
            } else {
                helper.setText(R.id.button_status, "Waiting For Oracle Result")
                helper.getView<TextView>(R.id.button_status).setTextAppearance(mContext, R.style.WgrHintButtonWithBorder)
            }
        }
    }

}

