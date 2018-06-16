package com.wagerr.wallet.ui.bet.event

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import com.wagerr.wallet.R
import com.wagerr.wallet.ui.base.BaseDrawerActivity

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import com.squareup.okhttp.internal.Internal.logger
import com.wagerr.wallet.data.bet.*
import com.wagerr.wallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION
import com.wagerr.wallet.service.IntentsConstants.DATA_TRANSACTION_HASH
import com.wagerr.wallet.service.WagerrWalletService
import com.wagerr.wallet.ui.base.dialogs.SimpleTextDialog
import com.wagerr.wallet.ui.base.dialogs.SimpleTwoButtonsDialog
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.*
import com.wagerr.wallet.ui.transaction_send_activity.SendTxDetailActivity
import com.wagerr.wallet.ui.transaction_send_activity.custom.ChangeAddressActivity
import com.wagerr.wallet.ui.transaction_send_activity.custom.CustomFeeFragment
import com.wagerr.wallet.ui.transaction_send_activity.custom.outputs.OutputWrapper
import com.wagerr.wallet.utils.CrashReporter
import com.wagerr.wallet.utils.DialogsUtil
import com.wagerr.wallet.utils.NavigationUtils
import com.wagerr.wallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT
import com.wagerr.wallet.utils.wrapContent
import global.exceptions.NoPeerConnectedException
import global.wrappers.InputWrapper
import global.wrappers.TransactionWrapper
import org.wagerrj.core.Address
import org.wagerrj.core.Coin
import org.wagerrj.core.InsufficientMoneyException
import org.wagerrj.core.Transaction
import org.wagerrj.script.ScriptBuilder
import org.wagerrj.script.ScriptOpCodes
import org.wagerrj.uri.WagerrURI
import org.wagerrj.wallet.Wallet
import wallet.exceptions.InsufficientInputsException
import wallet.exceptions.TxNotFoundException
import java.io.IOException


/**
 * Created by Neoperol on 5/11/17.
 */


class BetEventActivity : BaseDrawerActivity() {

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BetEventAdapter
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val oracleEvents: List<BetEvent>? = null
    private var emptyView: LinearLayout? = null
    private var executor: ExecutorService? = null
    private val errorDialog: SimpleTextDialog by lazy {
        DialogsUtil.buildSimpleErrorTextDialog(this, resources.getString(R.string.invalid_inputs), "")
    }

    private var transaction: Transaction? = null

    private val REQ_CODE_SEND_DETAIL = 8990

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        layoutInflater.inflate(R.layout.activity_bet, container)
        setTitle(R.string.bet_screen_title)
        recyclerView = findViewById<View>(R.id.addressList) as RecyclerView
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = BetEventAdapter()
        adapter.setEnableLoadMore(false)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        //        adapter.setListEventListener(this);
        recyclerView.adapter = adapter
        emptyView = findViewById<View>(R.id.empty_view) as LinearLayout
        emptyView!!.visibility = View.GONE

        adapter.setOnItemChildClickListener { adapter, view, position ->
            adapter as BetEventAdapter
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
        val dialogBuilder = AlertDialog.Builder(this)
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
            sendBetTransaction(betAmount.text.toString(), BetAction(event.eventId, when (betType) {
                BetType.BetTypeHomeWin -> event.homeTeam
                BetType.BetTypeDraw -> "D"
                BetType.BetTypeAwayWin -> event.awayTeam
            }).toBetTransactionData())
            dialog.dismiss()
        }
        dialog.show()
        dialog.wrapContent()
    }

    fun sendBetTransaction(amountStr: String, betActionStr: String) {
        try {
            if (checkConnectivity(amountStr, betActionStr)) {
                send(false, amountStr, betActionStr)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            showErrorDialog(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorDialog(e.message)
        }
    }

    private fun send(sendOffline: Boolean, amountString: String, betActionStr: String) {
        try {

            // check if the wallet is still syncing
            try {
                if (!wagerrModule.isSyncWithNode) {
                    throw IllegalArgumentException(getString(R.string.wallet_is_not_sync))
                }
            } catch (e: NoPeerConnectedException) {
                if (!sendOffline) {
                    e.printStackTrace()
                    throw IllegalArgumentException(getString(R.string.no_peer_connection))
                }
            }

            var amountStr = amountString
            // first check amount
            if (amountStr.length < 1) throw IllegalArgumentException("Amount not valid")
            if (amountStr.length == 1 && amountStr == ".") throw IllegalArgumentException("Amount not valid")
            if (amountStr.get(0) == '.') {
                amountStr = "0$amountStr"
            }

            val amount = Coin.parseCoin(amountStr)
            if (amount.isZero) throw IllegalArgumentException("Amount zero, please correct it")
            if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT)) throw IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, " + Transaction.MIN_NONDUST_OUTPUT.toFriendlyString())
            if (amount.isGreaterThan(Coin.valueOf(wagerrModule.availableBalance)))
                throw IllegalArgumentException("Insuficient balance")

            val params = wagerrModule.conf.networkParams

            transaction = Transaction(params)
            // then outputs

            val script = ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(betActionStr.toByteArray()).build()
            transaction?.addOutput(amount, script) //bo BTC will be destroyed

            transaction = wagerrModule.completeTx(transaction)


            Log.i("APP", "tx: " + transaction.toString())

            val transactionWrapper = TransactionWrapper(transaction, null, null, amount, TransactionWrapper.TransactionUse.SENT_SINGLE)

            // Confirmation screen
            val intent = Intent(this, SendTxDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(TX_WRAPPER, transactionWrapper)
            bundle.putSerializable(TX, transaction?.bitcoinSerialize())
            intent.putExtras(bundle)
            startActivityForResult(intent, REQ_CODE_SEND_DETAIL)

        } catch (e: InsufficientMoneyException) {
            e.printStackTrace()
            throw IllegalArgumentException("Insuficient balance\nMissing coins " + e.missing!!.toFriendlyString())
        } catch (e: InsufficientInputsException) {
            e.printStackTrace()
            throw IllegalArgumentException("Insuficient balance\nMissing coins " + e.missing.toFriendlyString())
        } catch (e: Wallet.DustySendRequested) {
            e.printStackTrace()
            throw IllegalArgumentException("Dusty send output, please increase the value of your outputs")
        }

    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (transaction != null)
            outState.putSerializable(TX, transaction?.unsafeBitcoinSerialize())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // todo: test this roting the screen..
        if (savedInstanceState.containsKey(TX)) {
            transaction = Transaction(wagerrModule.conf.networkParams, savedInstanceState.getByteArray(TX))
        }
    }


    private fun checkConnectivity(amountStr: String, betActionStr: String): Boolean {
        if (!isOnline()) {
            val noConnectivityDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                    this,
                    getString(R.string.error_no_connectivity_title),
                    getString(R.string.error_no_connectivity_body),
                    object : SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener {
                        override fun onRightBtnClicked(dialog: SimpleTwoButtonsDialog) {
                            try {
                                send(true, amountStr, betActionStr)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorDialog(e.message)
                            }

                            dialog.dismiss()

                        }

                        override fun onLeftBtnClicked(dialog: SimpleTwoButtonsDialog) {
                            dialog.dismiss()
                        }
                    }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                noConnectivityDialog.setRightBtnTextColor(getColor(R.color.lightGreen))
            } else {
                noConnectivityDialog.setRightBtnTextColor(ContextCompat.getColor(this, R.color.lightGreen))
            }
            noConnectivityDialog.setLeftBtnTextColor(Color.WHITE)
                    .setRightBtnTextColor(Color.BLACK)
                    .setRightBtnBackgroundColor(Color.WHITE)
                    .setLeftBtnTextColor(Color.BLACK)
                    .setLeftBtnText(getString(R.string.button_cancel))
                    .setRightBtnText(getString(R.string.button_ok))
                    .show()

            return false
        }
        return true
    }

    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }


    private fun showErrorDialog(message: String?) {
        errorDialog?.setBody(message)
        errorDialog?.show(fragmentManager, resources.getString(R.string.send_error_dialog_tag))
    }

    override fun onResume() {
        super.onResume()
        // check current activity in the navigation drawer
        setNavigationMenuItemChecked(1)

    }

    override fun onStart() {
        super.onStart()
        // re load
        load()
    }

    override fun onStop() {
        super.onStop()
        if (executor != null) {
            executor!!.shutdownNow()
            executor = null
        }
    }

    private fun load() {
        // add loading..
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor()
        }
        executor!!.submit {
            val list = wagerrModule.watchedSpent.filter {
                it.updateTime.time > 1528539485000
            }.map {
                return@map it.getBetEventString()
            }.filter { it!!.isValidBetEventSource() }
                    .map { it?.toBetEvent() }
                    .sortedBy {
                        it?.timeStamp
                    }

            runOnUiThread { adapter.setNewData(list) }

        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       if (requestCode == REQ_CODE_SEND_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    // pin ok, send the tx now
                    sendConfirmed()
                } catch (e: Exception) {
                    e.printStackTrace()
                    CrashReporter.saveBackgroundTrace(e, wagerrApplication.packageInfo)
                    showErrorDialog(getString(R.string.commit_tx_fail))
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun sendConfirmed() {
        if (transaction == null) {
            try {
                CrashReporter.appendSavedBackgroundTraces(StringBuilder().append("ERROR ### sendActivity - sendConfirmed - transaction NULL"))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            showErrorDialog(getString(R.string.commit_tx_fail))
            return
        }
        wagerrModule.commitTx(transaction)
        val intent = Intent(this, WagerrWalletService::class.java)
        intent.action = ACTION_BROADCAST_TRANSACTION
        intent.putExtra(DATA_TRANSACTION_HASH, transaction?.getHash()?.getBytes())
        startService(intent)
        Toast.makeText(this, R.string.sending_tx, Toast.LENGTH_LONG).show()
        finish()
        NavigationUtils.goBackToHome(this)
    }
}
