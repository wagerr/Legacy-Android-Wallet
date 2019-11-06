package com.wagerr.legacywallet.ui.bet_action_detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup

import com.wagerr.legacywallet.R
import com.wagerr.legacywallet.WagerrApplication
import com.wagerr.legacywallet.data.bet.toEventSymbol
import com.wagerr.legacywallet.ui.base.BaseActivity
import com.wagerr.legacywallet.ui.transaction_detail_activity.FragmentTxDetail
import com.wagerr.legacywallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER
import com.wagerr.legacywallet.ui.transaction_detail_activity.TransactionDetailActivity
import com.wagerr.legacywallet.utils.formatToViewDateTimeDefaults

import global.wrappers.TransactionWrapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bet_action_detail.*
import wagerr.bet.BetEvent
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
        title = getString(R.string.bet_action_detail_title)
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

    private fun getBetEventByIdAndTime(eventId: String, betTimeInMillis: Long):Observable<BetEvent?> {
        return Observable.fromCallable {
            return@fromCallable WagerrApplication.getInstance().module.betManager.getBetEventByIdAndTime(eventId,betTimeInMillis)
        }.subscribeOn(Schedulers.io())
    }

    private fun loadTx() {
        val betAction = transactionWrapper.transaction.toBetAction()
        betAction?.let {
            getBetEventByIdAndTime(it.eventId, transactionWrapper.transaction.updateTime.time)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it?.let {
                            val eventSymbol = it.eventLeague.toEventSymbol()
                            text_event_league.text = eventSymbol.getFullEventLeague(it.eventLeague)
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
                                    text_choice.text = "${it.homeTeam} ${getString(R.string.bet_win)} (${it.homeOdds})"
                                }
                                it.awayTeam -> {
                                    text_choice.text = "${it.awayTeam} ${getString(R.string.bet_win)} (${it.awayOdds})"
                                }
                                DRAW_SYMBOL -> {
                                    text_choice.text = "${getString(R.string.bet_draw)} (${it.drawOdds})"
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
