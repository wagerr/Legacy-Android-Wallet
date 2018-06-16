package com.wagerr.wallet.ui.bet.event

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.BetEvent
import org.apache.commons.codec.binary.Hex
import org.wagerrj.core.Transaction
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class BetEventAdapter : BaseQuickAdapter<BetEvent, BaseViewHolder>(R.layout.item_bet_event) {

    override fun convert(helper: BaseViewHolder, item: BetEvent) {

        helper.setText(R.id.text_event_id, item.eventId)
        helper.setText(R.id.text_event_league, item.eventLeague)
        helper.setText(R.id.text_event_info, item.eventInfo)
        helper.setText(R.id.text_time, Date(item.timeStamp).formatToViewDateTimeDefaults())
        helper.setText(R.id.text_home_team, item.homeTeam)
        helper.setText(R.id.text_away_team, item.awayTeam)
        helper.setText(R.id.button_home_odds, "${item.homeTeam} WIN\n${item.homeOdds}")
        helper.setText(R.id.button_draw_odds, "DRAW\n${item.drawOdds}")
        helper.setText(R.id.button_away_odds, "${item.awayTeam} WIN\n${item.awayOdds}")
        helper.addOnClickListener(R.id.button_home_odds)
                .addOnClickListener(R.id.button_draw_odds)
                .addOnClickListener(R.id.button_away_odds)


    }

}

fun Date.formatToViewDateTimeDefaults(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(this)
}

