package com.wagerr.wallet.ui.bet.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.wagerr.wallet.R
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.toEventSymbol
import com.wagerr.wallet.ui.base.BaseActivity
import com.wagerr.wallet.ui.transaction_detail_activity.TransactionIdDetailActivity
import com.wagerr.wallet.ui.transaction_detail_activity.TransactionIdDetailFragment
import com.wagerr.wallet.utils.formatToViewDateTimeDefaults
import com.wagerr.wallet.utils.wrapContent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_finished_bet_event_detail.*
import wagerr.bet.*
import java.util.*


/**
 * Created by Neoperol on 5/11/17.
 */


class BetEventDetailActivity : BaseActivity() {


    val eventId: String by lazy {
        intent.getStringExtra(EXTRA_EVENT_ID)
    }
    private lateinit var betActionsAdapter: BetEventBetActionAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        layoutInflater.inflate(R.layout.activity_finished_bet_event_detail, container)
        title = getString(R.string.bet_event_detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))
        swipe_refresh_layout.setRefreshing(true)
        swipe_refresh_layout.setOnRefreshListener {
            load()
        }
        bet_action_list.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        bet_action_list.layoutManager = layoutManager
        betActionsAdapter = BetEventBetActionAdapter()
        betActionsAdapter.setEnableLoadMore(false)
        bet_action_list.adapter = betActionsAdapter
        betActionsAdapter.bindToRecyclerView(bet_action_list)
        betActionsAdapter.setOnItemClickListener { adapter, view, position ->
            val betActionData = betActionsAdapter.getItem(position)!!
            if (betActionData.betReward != null && betActionData.betReward!!.rewards != null) {
                showSelectDialog(betActionData)
            } else {
                Observable.fromCallable {
                    WagerrApplication.getInstance().module.getTxWrapper(betActionData.betAction.transaction.hashAsString)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            val bundle = Bundle()
                            bundle.putSerializable(TransactionIdDetailFragment.EXTRA_TX_ID, it.transaction.hashAsString)
                            bundle.putBoolean(TransactionIdDetailFragment.IS_DETAIL, true)
                            val intent = Intent(this, TransactionIdDetailActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }, {})
            }
        }

        load()
    }

    private fun showSelectDialog(betActionData:BetData.BetActionData) {
        val sheetView = layoutInflater.inflate(R.layout.dialog_choose_action_reward, null)
        val seeActionTx = sheetView.findViewById<TextView>(R.id.text_see_action_tx)
        val seeRewardTx = sheetView.findViewById<TextView>(R.id.text_see_reward_tx)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(sheetView)
        val dialog = dialogBuilder.create()
        seeActionTx.setOnClickListener {
            Observable.fromCallable {
                WagerrApplication.getInstance().module.getTxWrapper(betActionData.betAction.transaction.hashAsString)
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val bundle = Bundle()
                        bundle.putSerializable(TransactionIdDetailFragment.EXTRA_TX_ID, it.transaction.hashAsString)
                        bundle.putBoolean(TransactionIdDetailFragment.IS_DETAIL, true)
                        val intent = Intent(this, TransactionIdDetailActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        dialog.dismiss()
                    }, {})
        }
        seeRewardTx.setOnClickListener {
            Observable.fromCallable {
                WagerrApplication.getInstance().module.getTxWrapper(betActionData.betReward!!.transaction.hashAsString)
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val bundle = Bundle()
                        bundle.putSerializable(TransactionIdDetailFragment.EXTRA_TX_ID, it.transaction.hashAsString)
                        bundle.putBoolean(TransactionIdDetailFragment.IS_DETAIL, true)
                        val intent = Intent(this, TransactionIdDetailActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        dialog.dismiss()
                    }, {})
        }
        dialog.show()
        dialog.wrapContent()
    }

    private fun getBetEventById(eventId: String):Observable<BetEvent?> {
        return Observable.fromCallable {
            return@fromCallable WagerrApplication.getInstance().module.betManager.getBetEventById(eventId)
        }.subscribeOn(Schedulers.io())
    }

    private fun load() {
        compositeDisposable += getBetEventById(eventId).observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it?.let {
                        val eventSymbol = it.eventLeague.toEventSymbol()
                        text_event_league.text = eventSymbol.getFullEventLeague(it.eventLeague)
                        text_event_info.text = eventSymbol.getFullEventInfo(it.eventInfo)
                        text_time.text = Date(it.timeStamp).formatToViewDateTimeDefaults()
                        eventSymbol.getTeamImage(it.homeTeam)?.let {
                            image_home_team.setImageResource(it)
                        }?: run {
                            image_home_team.visibility = View.GONE
                        }
                        eventSymbol.getTeamImage(it.awayTeam)?.let {
                            image_away_team.setImageResource(it)
                        }?: run {
                            image_away_team.visibility = View.GONE
                        }
                        text_home_team.text = eventSymbol.getFullTeam(it.homeTeam)
                        text_away_team.text = eventSymbol.getFullTeam(it.awayTeam)
                    }
                }, {})
        Observable.fromCallable {
            val betEvents =   WagerrApplication.getInstance().module.betManager.getBetEventsById(eventId)
            val betResult = WagerrApplication.getInstance().module.betManager.getBetResultByEventId(eventId)
             betEvents to betResult
        }.subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.second?.let {
                        if (it.isRefund()) {
                            button_status.text = getString(R.string.bet_refund)
                        } else if (it.betResult == DRAW_SYMBOL) {
                            button_status.text = getString(R.string.bet_draw)
                        } else {
                            button_status.text = "${it.betResult} ${getString(R.string.bet_win)}"
                        }
                        button_status.setTextAppearance(this, R.style.WgrButtonWithBorder)
                    } ?: run {
                        if (it.first?.get(0)?.timeStamp!! > System.currentTimeMillis()) {
                            button_status.text = getString(R.string.bet_game_not_started)
                            button_status.setTextAppearance(this, R.style.WgrHintButtonWithBorder)
                        } else {
                            button_status.text = getString(R.string.bet_waiting_for_oracle)
                            button_status.setTextAppearance(this, R.style.WgrHintButtonWithBorder)
                        }
                    }
                }, {
                    it.printStackTrace()
                })
        Observable.fromCallable {
            WagerrApplication.getInstance().module.mineSpent.toBetActions()?.filter {
                it.eventId == eventId
            }?.mapNotNull {
                WagerrApplication.getInstance().module.betManager.getBetActionDataByActionTransaction(it.transaction)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    swipe_refresh_layout.isRefreshing = false
                    if (it.orEmpty().isEmpty()) {
                        betActionsAdapter.setEmptyView(R.layout.layout_empty_bet_action)
                    } else {
                        betActionsAdapter.setNewData(it?.sortedByDescending { it.betAction.transaction.updateTime })
                    }
                }, {
                    swipe_refresh_layout.isRefreshing = false
                    it.printStackTrace()
                })

    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

        fun enter(activity: Context, eventId: String) {
            val intent = Intent(activity, BetEventDetailActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ID, eventId)
            activity.startActivity(intent)
        }
    }
}
