package com.wagerr.wallet.data.bet

import android.support.annotation.DrawableRes
import com.wagerr.wallet.R

sealed class EventSymbol {
    abstract fun getFullEventInfo(eventInfoSymbol: String): String
    abstract fun getFullEventLeague(): String
    abstract fun getFullTeam(teamSymbol: String): String
    abstract fun getTeamImage(teamSymbol: String): Int?


    object WorldCupSymbol : EventSymbol() {

        val TEAM_MAP = hashMapOf(
                "ARG" to "Argentina",
                "AUS" to "Australia",
                "BRA" to "Brazil",
                "BEL" to "Belgium",
                "CRO" to "Croatia",
                "COL" to "Colombia",
                "CRC" to "Costa Rica",
                "DEN" to "Denmark",
                "EGY" to "Egypt",
                "ENG" to "England",
                "ESP" to "Spain",
                "FRA" to "France",
                "GER" to "Germany",
                "IRN" to "IR Iran",
                "ISL" to "Iceland",
                "JPN" to "Japan",
                "KOR" to "Korea Republic",
                "KSA" to "Saudi Arabia",
                "MAR" to "Morocco",
                "MEX" to "Mexico",
                "NIG" to "Nigeria",
                "PAN" to "Panama",
                "PER" to "Peru",
                "POR" to "Portugal",
                "POL" to "Poland",
                "RUS" to "Russia",
                "SRB" to "Serbia",
                "SEN" to "Senegal",
                "SUI" to "Switzerland",
                "SWE" to "Sweden",
                "TUN" to "Tunisia",
                "URU" to "Uruguay"
        )

        val TEAM_IMAGE_MAP = hashMapOf(
                "ARG" to R.drawable.img_flag_arg,
                "AUS" to R.drawable.img_flag_aus,
                "BRA" to R.drawable.img_flag_bra,
                "BEL" to R.drawable.img_flag_bel,
                "CRO" to R.drawable.img_flag_cro,
                "COL" to R.drawable.img_flag_col,
                "CRC" to R.drawable.img_flag_crc,
                "DEN" to R.drawable.img_flag_den,
                "EGY" to R.drawable.img_flag_egy,
                "ENG" to R.drawable.img_flag_eng,
                "ESP" to R.drawable.img_flag_esp,
                "FRA" to R.drawable.img_flag_fra,
                "GER" to R.drawable.img_flag_ger,
                "IRN" to R.drawable.img_flag_irn,
                "ISL" to R.drawable.img_flag_isl,
                "JPN" to R.drawable.img_flag_jpn,
                "KOR" to R.drawable.img_flag_kor,
                "KSA" to R.drawable.img_flag_ksa,
                "MAR" to R.drawable.img_flag_mar,
                "MEX" to R.drawable.img_flag_mex,
                "NIG" to R.drawable.img_flag_nig,
                "PAN" to R.drawable.img_flag_pan,
                "PER" to R.drawable.img_flag_per,
                "POR" to R.drawable.img_flag_por,
                "POL" to R.drawable.img_flag_pol,
                "RUS" to R.drawable.img_flag_rus,
                "SRB" to R.drawable.img_flag_srb,
                "SEN" to R.drawable.img_flag_sen,
                "SUI" to R.drawable.img_flag_sui,
                "SWE" to R.drawable.img_flag_swe,
                "TUN" to R.drawable.img_flag_tun,
                "URU" to R.drawable.img_flag_uru
        )
        val EVENT_INFO_MAP = hashMapOf(
                "R1" to "Round 1",
                "RD2" to "Round 2",
                "RD3" to "Round 3",
                "F16" to "Final 16",
                "QFL" to "Quarter Final",
                "SFL" to "Semi Final",
                "FIN" to "Final"
        )

        override fun getFullEventInfo(eventInfoSymbol: String): String {
            return EVENT_INFO_MAP[eventInfoSymbol] ?: eventInfoSymbol
        }

        override fun getFullEventLeague(): String {
            return "World Cup"
        }

        override fun getFullTeam(teamSymbol: String): String {
            return TEAM_MAP[teamSymbol] ?: teamSymbol
        }

        override fun getTeamImage(teamSymbol: String): Int? {
            return TEAM_IMAGE_MAP[teamSymbol]
        }

    }
}

fun String.toEventSymbol(): EventSymbol {
    return when (this) {
        "WCUP" -> EventSymbol.WorldCupSymbol
        else -> EventSymbol.WorldCupSymbol
    }
}