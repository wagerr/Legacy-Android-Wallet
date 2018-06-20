package com.wagerr.wallet.data.bet

import android.util.Log
import com.wagerr.wallet.data.worldcup.model.WorldCupMatch
import java.text.SimpleDateFormat
import java.util.*


data class BetMatchResult(val eventId:String, val homeScore: Int?, val awayScore: Int?)

fun List<WorldCupMatch>.getMatchResult(betEvent: BetEvent): BetMatchResult? {
    return this.map {
        //Nigeria is NIG in the api
        if (it.team1.code == "NGA") {
            it.team1.code = "NIG"
        }
        if (it.team2.code == "NGA") {
            it.team2.code = "NIG"
        }
        return@map it
    }.filter {
        return@filter (betEvent.homeTeam == it.team1.code && betEvent.awayTeam == it.team2.code) || (betEvent.homeTeam == it.team2.code && betEvent.awayTeam == it.team1.code)
    }.filter {
        Log.d("fefefe", "${it.team1.code}+${it.team2.code}")
        Log.d("fefefe", it.toString())
        Log.d("fefefe", betEvent.toString())

        val fmt = SimpleDateFormat("yyyy-MM-dd")
        fmt.timeZone = TimeZone.getTimeZone(it.timezone)
        return@filter it.date == fmt.format(Date(betEvent.timeStamp))
    }.map {
        Log.d("fefefe", "${it.team1.code}+${it.team2.code}")
        if (betEvent.homeTeam == it.team1.code && betEvent.awayTeam == it.team2.code) {
            return BetMatchResult(betEvent.eventId, it.score1, it.score2)
        } else {
            return BetMatchResult(betEvent.eventId, it.score2, it.score1)
        }
    }.firstOrNull()
}