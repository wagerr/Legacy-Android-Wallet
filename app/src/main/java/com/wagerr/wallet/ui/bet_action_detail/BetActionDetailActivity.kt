package com.wagerr.wallet.ui.bet_action_detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.wagerr.wallet.R
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.module.bet.BetEventFetcher
import com.wagerr.wallet.ui.base.BaseActivity
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER
import com.wagerr.wallet.ui.transaction_detail_activity.TransactionDetailActivity
import com.wagerr.wallet.utils.NavigationUtils
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults

import org.wagerrj.script.Script

import java.text.SimpleDateFormat

import global.AddressLabel
import global.wrappers.TransactionWrapper
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bet_action_detail.*
import org.wagerrj.core.*
import java.util.*

/**
 * Created by Neoperol on 6/9/17.
 */

class BetActionDetailActivity : BaseActivity() {

    val transactionWrapper: TransactionWrapper by lazy {
        if (intent.getSerializableExtra(EXTRA_TX_WRAPPER) != null) {
            (intent.getSerializableExtra(EXTRA_TX_WRAPPER) as TransactionWrapper).apply {
                transaction = wagerrModule.getTx(txId)
            }
        } else {
            WagerrApplication.getInstance().module.listTx().filter {
                it.txId == Sha256Hash.wrap(intent.getStringExtra(EXTRA_TX_ID))
            }.first().apply {
                transaction = wagerrModule.getTx(txId)
            }
        }
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
                                BET_ATCION_DRAW -> {
                                    text_choice.text = "Draw (${it.drawOdds})"
                                }
                            }
                            text_amount.text = transactionWrapper.transaction.toBetActionAmount()?.toFriendlyString()
                        }

                    }, {})

        }
    }


    companion object {
        val EXTRA_TX_WRAPPER = "EXTRA_TX_WRAPPER"
        val EXTRA_TX_ID = "EXTRA_TX_ID"

        fun enter(activity: Context, data: TransactionWrapper) {
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_TX_WRAPPER, data)
            val intent = Intent(activity, BetActionDetailActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }

        fun enter(activity: Context, txId: String) {
            val intent = Intent(activity, BetActionDetailActivity::class.java)
            intent.putExtra(EXTRA_TX_ID, txId)
            activity.startActivity(intent)
        }
    }
}
