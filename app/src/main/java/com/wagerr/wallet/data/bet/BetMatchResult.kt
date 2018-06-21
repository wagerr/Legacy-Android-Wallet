package com.wagerr.wallet.data.bet

import android.util.Log
import com.wagerr.wallet.data.worldcup.model.WorldCupMatch
import java.text.SimpleDateFormat
import java.util.*


data class BetMatchResult(val eventId:String, val homeScore: Int?, val awayScore: Int?)

fun List<WorldCupMatch>.getMatchResult(betEvent: BetEvent): BetMatchResult? {
    return this.filter {
        return@filter (betEvent.homeTeam == it.team1.code && betEvent.awayTeam == it.team2.code) || (betEvent.homeTeam == it.team2.code && betEvent.awayTeam == it.team1.code)
    }.filter {
        val fmt = SimpleDateFormat("yyyy-MM-dd")
        fmt.timeZone = TimeZone.getTimeZone(it.timezone)
        return@filter it.date == fmt.format(Date(betEvent.timeStamp))
    }.map {
        if (betEvent.homeTeam == it.team1.code && betEvent.awayTeam == it.team2.code) {
            return BetMatchResult(betEvent.eventId, it.score1, it.score2)
        } else {
            return BetMatchResult(betEvent.eventId, it.score2, it.score1)
        }
    }.firstOrNull()
}