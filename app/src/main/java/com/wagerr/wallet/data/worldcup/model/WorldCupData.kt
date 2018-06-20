package com.wagerr.wallet.data.worldcup.model

import com.google.gson.annotations.SerializedName

data class WorldCupData(
        @SerializedName("name") val name: String,
        @SerializedName("rounds") val rounds: List<Round>
)

data class Round(
        @SerializedName("name") val name: String,
        @SerializedName("matches") val matches: List<WorldCupMatch>
)

data class WorldCupMatch(
        @SerializedName("num") val num: Int,
        @SerializedName("date") val date: String,
        @SerializedName("time") val time: String,
        @SerializedName("team1") val team1: Team,
        @SerializedName("team2") val team2: Team,
        @SerializedName("score1") val score1: Int?,
        @SerializedName("score2") val score2: Int?,
        @SerializedName("score1i") val score1i: Int?,
        @SerializedName("score2i") val score2i: Int?,
        @SerializedName("goals1") val goals1: List<Goals>,
        @SerializedName("goals2") val goals2: List<Goals>,
        @SerializedName("group") val group: String,
        @SerializedName("stadium") val stadium: Stadium,
        @SerializedName("city") val city: String,
        @SerializedName("timezone") val timezone: String
)

data class Goals(
        @SerializedName("name") val name: String,
        @SerializedName("minute") val minute: Int,
        @SerializedName("score1") val score1: Int,
        @SerializedName("score2") val score2: Int,
        @SerializedName("offset") val offset: Int
)

data class Team(
        @SerializedName("name") val name: String,
        @SerializedName("code") var code: String
)

data class Stadium(
        @SerializedName("key") val key: String,
        @SerializedName("name") val name: String
)