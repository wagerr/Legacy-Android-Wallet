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
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.ui.base.BaseFragment
import com.wagerr.wallet.ui.bet.result.BetEventDetailActivity
import com.wagerr.wallet.utils.wrapContent
import global.WagerrCoreContext
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ongoing_bet_event.*
import wagerr.bet.*
import java.text.DecimalFormat

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
        emptyView = layoutInflater.inflate(R.layout.layout_empty_view, ongoing_bet_event_list.getParent() as ViewGroup,
                false)

        ongoing_bet_event_list.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        ongoing_bet_event_list.layoutManager = layoutManager
        adapter = OngoingBetEventAdapter()
        adapter.setEnableLoadMore(false)
        ongoing_bet_event_list.adapter = adapter
        ongoing_bet_event_list.layoutManager = layoutManager

        adapter.setOnItemChildClickListener { adapter, view, position ->
            adapter as OngoingBetEventAdapter

            val betEvent = adapter.getItem(position)

            betEvent?.let {
                when (view.id) {
                    R.id.button_home_odds -> {
                        showBetDialog(it, BetType.BetTypeHomeWin)
                    }
                    R.id.button_draw_odds -> {
                        showBetDialog(it, BetType.BetTypeDraw)
                    }
                    R.id.button_away_odds -> {
                        showBetDialog(it, BetType.BetTypeAwayWin)
                    }
                }
            }
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            adapter as OngoingBetEventAdapter
            val betEvent = adapter.getItem(position)
            BetEventDetailActivity.enter(activity!!, betEvent!!.eventId)
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
                textOdds.text = "${event.homeOdds} (return ${DecimalFormat("0.0000").format((event.homeOdds-1)*0.94+1)})"
            }
            BetType.BetTypeDraw -> {
                textChoose.text = "DRAW"
                textOdds.text = "${event.drawOdds} (return ${DecimalFormat("0.0000").format((event.drawOdds-1)*0.94+1)})"
            }
            BetType.BetTypeAwayWin -> {
                textChoose.text = "${event.awayTeam} WIN"
                textOdds.text = "${event.awayOdds} (return ${DecimalFormat("0.0000").format((event.awayOdds-1)*0.94+1)})"
            }
        }
        betGo.setOnClickListener {
            if (!WagerrApplication.getInstance().module.isAnyPeerConnected ||  WagerrApplication.getInstance().module.connectedPeerHeight == -1L) {
                (activity as BetEventActivity).showErrorDialog(getString(R.string.warning_title),
                        getString(R.string.bet_event_not_connect_peer))
                dialog.dismiss()
                return@setOnClickListener
            }
            if (WagerrApplication.getInstance().module.chainHeight.toLong() != WagerrApplication.getInstance().module.connectedPeerHeight) {
                val behindBlocks = WagerrApplication.getInstance().module.connectedPeerHeight - WagerrApplication.getInstance().module.chainHeight.toLong()
                (activity as BetEventActivity).showErrorDialog(getString(R.string.warning_title),
                            "${getString(R.string.bet_event_not_sync)} ${behindBlocks.toInt()} blocks behind. ")
                dialog.dismiss()
                return@setOnClickListener
            }
            if (event.timeStamp < System.currentTimeMillis() + WagerrCoreContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME ||
                    event.timeStamp < WagerrApplication.getInstance().module.chainHead?.header?.timeSeconds!! * 1000
                    + WagerrCoreContext.STOP_ACCEPT_BET_BEFORE_EVENT_TIME) {
                (activity as BetEventActivity).showErrorDialog(getString(R.string.warning_title),
                        getString(R.string.bet_event_stop))
                dialog.dismiss()
                return@setOnClickListener
            }
            if (WagerrApplication.getInstance().module.betManager.getLatestBetEventById(event.eventId)?.transaction?.updateTime
                    !== event.transaction.updateTime) {
                (activity as BetEventActivity).showErrorDialog(getString(R.string.warning_title),
                        getString(R.string.bet_event_odds_change))
                dialog.dismiss()
                return@setOnClickListener
            }
            (activity as BetEventActivity).sendBetTransaction(event.transaction.updateTime.time, betAmount.text.toString(), BetActionForSend(
                    event.eventId, when (betType) {
                BetType.BetTypeHomeWin -> event.homeTeam
                BetType.BetTypeDraw -> DRAW_SYMBOL
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
        compositeDisposable += getCanBetBetEvents()
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

    private fun getCanBetBetEvents(): Observable<List<BetEvent>?> {
        return Observable.fromCallable {
            return@fromCallable WagerrApplication.getInstance().module.betManager.getCanBetBetEvents()?.sortedBy { it.timeStamp }
        }.subscribeOn(Schedulers.io())
    }
}