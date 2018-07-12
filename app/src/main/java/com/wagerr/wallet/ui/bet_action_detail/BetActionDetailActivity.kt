package com.wagerr.wallet.ui.bet_action_detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup

import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.module.bet.BetEventFetcher
import com.wagerr.wallet.ui.base.BaseActivity
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER
import com.wagerr.wallet.ui.transaction_detail_activity.TransactionDetailActivity
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults

import global.wrappers.TransactionWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_bet_action_detail.*
import wagerr.bet.DRAW_SYMBOL
import wagerr.bet.toBetAction
import wagerr.bet.toBetActionAmount
import java.util.*

/**
 * Created by Neoperol on 6/9/17.
 */


class BetActionDetailActivity : BaseActivity() {
    val txId:String by lazy {
        intent.getStringExtra(EXTRA_TX_ID)
    }

    val transactionWrapper: TransactionWrapper by lazy {
        wagerrModule.getTxWrapper(txId)
    }

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        layoutInflater.inflate(R.layout.activity_bet_action_detail, container)
        title = "Bet Action Detail"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        button_see_transaction_detail.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(TX_WRAPPER, transactionWrapper)
            bundle.putBoolean(FragmentTxDetail.IS_DETAIL, true)
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        loadTx()
    }

    private fun loadTx() {
        val betAction = transactionWrapper.transaction.toBetAction()
        betAction?.let {
            BetEventFetcher.getBetEventByIdAndTime(it.eventId, transactionWrapper.transaction.updateTime.time)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it?.let {
                            val eventSymbol = it.eventLeague.toEventSymbol()
                            text_event_league.text = eventSymbol.getFullEventLeague()
                            text_event_info.text = eventSymbol.getFullEventInfo(it.eventInfo)
                            text_time.text = Date(it.timeStamp).formatToViewDateTimeDefaults()
                            eventSymbol.getTeamImage(it.homeTeam)?.let {
                                image_home_team.setImageResource(it)
                            }
                            eventSymbol.getTeamImage(it.awayTeam)?.let {
                                image_away_team.setImageResource(it)
                            }
                            text_home_team.text = eventSymbol.getFullTeam(it.homeTeam)
                            text_away_team.text = eventSymbol.getFullTeam(it.awayTeam)
                            when (betAction?.betChoose) {
                                it.homeTeam -> {
                                    text_choice.text = "${it.homeTeam} WIN (${it.homeOdds})"
                                }
                                it.awayTeam -> {
                                    text_choice.text = "${it.awayTeam} WIN (${it.awayOdds})"
                                }
                                DRAW_SYMBOL -> {
                                    text_choice.text = "Draw (${it.drawOdds})"
                                }
                            }
                            text_amount.text = transactionWrapper.transaction.toBetActionAmount()?.toFriendlyString()
                        }

                    }, {})

        }
    }


    companion object {
        val EXTRA_TX_ID = "EXTRA_TX_ID"

        fun enter(activity: Context, txId:String) {
            val bundle = Bundle()
            bundle.putString(EXTRA_TX_ID, txId)
            val intent = Intent(activity, BetActionDetailActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }
}
