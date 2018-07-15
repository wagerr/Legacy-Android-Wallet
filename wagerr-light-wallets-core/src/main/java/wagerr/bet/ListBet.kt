package wagerr.bet

import com.google.gson.annotations.SerializedName

data class ListBet(
        @SerializedName("tx-id")
        val txId: String,
        @SerializedName("event-id")
        val eventId: String,
        @SerializedName("team-to-win")
        val teamToWIn: String,
        @SerializedName("amount")
        val amount: Float)