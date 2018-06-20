package com.wagerr.wallet.ui.bet.event

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wagerr.wallet.R
import com.wagerr.wallet.data.bet.getMatchResult
import com.wagerr.wallet.data.worldcup.api.WorldCupApi
import com.wagerr.wallet.module.bet.BetEventFetcher
import com.wagerr.wallet.module.bet.BetResultFetcher
import com.wagerr.wallet.ui.base.BaseFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_finished_bet_event.*

class FinishedBetEventFragment : BaseFragment() {


    private lateinit var finishedAdapter: FinishedBetEventAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_finished_bet_event, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        swipe_refresh_layout.setRefreshing(true)
        swipe_refresh_layout.setOnRefreshListener {
            load()
        }
        finished_bet_event_list.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        finished_bet_event_list.layoutManager = layoutManager
        finishedAdapter = FinishedBetEventAdapter()
        finishedAdapter.setEnableLoadMore(false)
        finished_bet_event_list.adapter = finishedAdapter
        finishedAdapter.bindToRecyclerView(finished_bet_event_list)

    }

    override fun onStart() {
        super.onStart()
        // re load
        load()
    }

    private fun load() {
        // add loading..
        Observables.zip(BetEventFetcher.getFinishedBetEvents(), WorldCupApi.getWorldCupMatchData(),
                BetResultFetcher.getBetResults()) { finishedBetEvents, worldCupMatches, betResults ->
            return@zip finishedBetEvents?.map { event ->
                return@map FinishedBetData(event, betResults?.firstOrNull() {
                    event.eventId == it.eventId
                }, worldCupMatches.getMatchResult(event))
            }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    swipe_refresh_layout.isRefreshing = false
                    if (it.orEmpty().isEmpty()) {
                        finishedAdapter.setEmptyView(R.layout.layout_empty_view)
                    } else {
                        finishedAdapter.setNewData(it?.sortedByDescending { it.betEvent.timeStamp })
                    }
                }, { it.printStackTrace() })
    }


}