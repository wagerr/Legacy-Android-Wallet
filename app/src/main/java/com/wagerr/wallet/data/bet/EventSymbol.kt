package com.wagerr.wallet.data.bet

import com.wagerr.wallet.R

sealed class EventSymbol {
    abstract fun getFullEventInfo(eventInfoSymbol: String): String
    abstract fun getFullEventLeague(eventLeague: String): String
    abstract fun getFullTeam(teamSymbol: String): String
    abstract fun getTeamImage(teamSymbol: String): Int?

    object DefaultSymbol : EventSymbol() {

        override fun getFullEventInfo(eventInfoSymbol: String): String {
            return eventInfoSymbol
        }

        override fun getFullEventLeague(eventLeague: String): String {
            return eventLeague
        }

        override fun getFullTeam(teamSymbol: String): String {
            return teamSymbol
        }

        override fun getTeamImage(teamSymbol: String): Int? {
            return null
        }

    }
}

fun String.toEventSymbol(): EventSymbol {
    return EventSymbol.DefaultSymbol
}