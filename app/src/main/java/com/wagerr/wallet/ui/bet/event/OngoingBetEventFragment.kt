package com.wagerr.wallet.ui.bet.event

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.wagerr.wallet.R
import com.wagerr.wallet.R.id.swipe_refresh_layout
import com.wagerr.wallet.data.bet.BetType
import com.wagerr.wallet.module.bet.BetEventFetcher
import com.wagerr.wallet.ui.base.BaseFragment
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_ongoing_bet_event.*
import com.wagerr.wallet.R.id.ongoing_bet_event_list
import com.wagerr.wallet.data.bet.BetAction
import com.wagerr.wallet.data.bet.BetEvent
import com.wagerr.wallet.data.bet.toBetTransactionData
import com.wagerr.wallet.utils.wrapContent
import io.reactivex.android.schedulers.AndroidSchedulers


class OngoingBetEventFragment : BaseFragment() {


    private lateinit var adapter: OngoingBetEventAdapter
    private lateinit var emptyView: View
    private var layoutManager: RecyclerView.LayoutManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_ongoing_bet_event, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        swipe_refresh_layout.isRefreshing = true
        swipe_refresh_layout.setOnRefreshListener {
            load()
        }
        emptyView = layoutInflater.inflate(R.layout.layout_empty_view, ongoing_bet_event_list.getParent() as ViewGroup, false)

        ongoing_bet_event_list.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        ongoing_bet_event_list.layoutManager = layoutManager
        adapter = OngoingBetEventAdapter()
        adapter.setEnableLoadMore(false)
        ongoing_bet_event_list.adapter = adapter
        ongoing_bet_event_list.layoutManager = layoutManager

        adapter.setOnItemChildClickListener { adapter, view, position ->
            adapter as OngoingBetEventAdapter
            when (view.id) {
                R.id.button_home_odds -> {
                    showBetDialog(adapter.getItem(position)!!, BetType.BetTypeHomeWin)
                }
                R.id.button_draw_odds -> {
                    showBetDialog(adapter.getItem(position)!!, BetType.BetTypeDraw)
                }
                R.id.button_away_odds -> {
                    showBetDialog(adapter.getItem(position)!!, BetType.BetTypeAwayWin)
                }
            }
        }
    }

    fun showBetDialog(event: BetEvent, betType: BetType) {
        val sheetView = layoutInflater.inflate(R.layout.dialog_bet, null)
        val textChoose = sheetView.findViewById<TextView>(R.id.text_choose)
        val textEvent = sheetView.findViewById<TextView>(R.id.text_event)
        val textOdds = sheetView.findViewById<TextView>(R.id.text_odds)
        val betAmount = sheetView.findViewById<EditText>(R.id.edit_amount)
        val betGo = sheetView.findViewById<Button>(R.id.button_go_bet)
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setView(sheetView)

        val dialog = dialogBuilder.create()

        textEvent.text = "${event.homeTeam} vs ${event.awayTeam}"
        when (betType) {
            BetType.BetTypeHomeWin -> {
                textChoose.text = "${event.homeTeam} WIN"
                textOdds.text = "(${event.homeOdds})"
            }
            BetType.BetTypeDraw -> {
                textChoose.text = "DRAW"
                textOdds.text = "(${event.drawOdds})"
            }
            BetType.BetTypeAwayWin -> {
                textChoose.text = "${event.awayTeam} WIN"
                textOdds.text = "(${event.awayOdds})"
            }
        }
        betGo.setOnClickListener {
            (activity as BetEventActivity).sendBetTransaction(betAmount.text.toString(), BetAction(event.eventId, when (betType) {
                BetType.BetTypeHomeWin -> event.homeTeam
                BetType.BetTypeDraw -> "D"
                BetType.BetTypeAwayWin -> event.awayTeam
            }).toBetTransactionData())
            dialog.dismiss()
        }
        dialog.show()
        dialog.wrapContent()
    }

    override fun onStart() {
        super.onStart()
        // re load
        load()
    }

    private fun load() {
        // add loading..
        compositeDisposable += BetEventFetcher.getCanBetBetEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
            swipe_refresh_layout.isRefreshing = false
            if (it == null || it.isEmpty()) {
                adapter.setEmptyView(emptyView)
            } else {
                adapter.setNewData(it)
            }
        }, {})
    }
}