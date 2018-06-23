package com.wagerr.wallet.ui.bet.result

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import com.wagerr.wallet.R
import com.wagerr.wallet.ui.base.BaseDrawerActivity

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.Log
import com.wagerr.wallet.R.id.bet_action_list
import com.wagerr.wallet.R.id.swipe_refresh_layout


import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.data.worldcup.api.WorldCupApi
import com.wagerr.wallet.module.bet.BetActionFetcher
import com.wagerr.wallet.module.bet.BetEventFetcher
import com.wagerr.wallet.module.bet.BetResultFetcher
import com.wagerr.wallet.ui.base.BaseActivity
import com.wagerr.wallet.ui.base.WagerrActivity
import com.wagerr.wallet.ui.bet.event.FinishedBetEventAdapter
import com.wagerr.wallet.ui.bet_action_detail.BetActionDetailActivity
import com.wagerr.wallet.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_finished_bet_event_detail.*
import java.util.*


/**
 * Created by Neoperol on 5/11/17.
 */


class FinishedBetEventDetailActivity : BaseActivity() {


    val eventId: String by lazy {
        intent.getStringExtra(EXTRA_EVENT_ID)
    }
    private lateinit var betActionsAdapter: FinishedBetEventBetActionAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        layoutInflater.inflate(R.layout.activity_finished_bet_event_detail, container)
        title = "Finished Bet Event Detail"
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
        betActionsAdapter = FinishedBetEventBetActionAdapter()
        betActionsAdapter.setEnableLoadMore(false)
        bet_action_list.adapter = betActionsAdapter
        betActionsAdapter.bindToRecyclerView(bet_action_list)
        betActionsAdapter.setOnItemClickListener { adapter, view, position ->
            BetActionDetailActivity.enter(this, betActionsAdapter.getItem(position)!!.betAction!!.transaction.hashAsString)
        }

        load()
    }

    private fun load() {
        compositeDisposable += BetEventFetcher.getBetEventById(eventId).observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
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
                    }
                }
                .observeOn(Schedulers.io())
                .flatMap { betEvent ->
                    WorldCupApi.getWorldCupMatchData().map { it.getMatchResult(betEvent) }
                }.observeOn(AndroidSchedulers.mainThread()).subscribe({ matchResult ->
                    matchResult?.homeScore?.let {
                        text_vs.text = "${matchResult.homeScore}:${matchResult.awayScore}"
                    } ?: run {
                        text_vs.text = "VS"
                    }
                }, {})

        Observable.fromCallable {
            WagerrApplication.getInstance().module.watchedSpent to WagerrApplication.getInstance().module.mineSpent

        }.map {
            val betEvents = it.first.getBetEventsById(eventId)
            val betResult = it.first.getBetResultByEventId(eventId)
            val betActions = it.second.getBetActionsByEventId(eventId)
            return@map betActions?.map { betAction ->
                FinishedBetEventDetailData(betEvents?.last { betEvent ->
                    betAction.transaction.updateTime > betEvent.transaction.updateTime
                }, betResult, betAction)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it?.firstOrNull()?.betResult?.let {
                        if (it.betResult == "D") {
                            button_status.text = "DRAW"
                        } else {
                            button_status.text = "${it.betResult}"
                        }
                    } ?: run {
                        button_status.text = "Waiting For Oracle Result"
                    }
                    swipe_refresh_layout.isRefreshing = false
                    if (it.orEmpty().isEmpty()) {
                        betActionsAdapter.setEmptyView(R.layout.layout_empty_bet_action)
                    } else {
                        betActionsAdapter.setNewData(it?.sortedByDescending { it.betAction?.transaction?.updateTime })
                    }
                }, { it.printStackTrace() })

    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

        fun enter(activity: Context, eventId: String) {
            val intent = Intent(activity, FinishedBetEventDetailActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ID, eventId)
            activity.startActivity(intent)
        }
    }
}
