package com.wagerr.wallet.ui.bet.event

import android.app.Activity
import android.content.ClipData
import android.content.ClipData.newPlainText
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import chain.BlockchainState
import com.wagerr.wallet.R
import com.wagerr.wallet.R.id.container_bet_event_syncing
import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.module.bet.BetActionFetcher
import com.wagerr.wallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION
import com.wagerr.wallet.service.IntentsConstants.DATA_TRANSACTION_HASH
import com.wagerr.wallet.service.WagerrWalletService
import com.wagerr.wallet.ui.base.BaseDrawerActivity
import com.wagerr.wallet.ui.base.dialogs.SimpleTextDialog
import com.wagerr.wallet.ui.qr_activity.QrActivity
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.TX
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER
import com.wagerr.wallet.ui.transaction_send_activity.SendTxDetailActivity
import com.wagerr.wallet.utils.*
import com.wagerr.wallet.utils.scanner.ScanActivity
import global.WagerrCoreContext
import global.WagerrCoreContext.MAX_BET_AMOUNT
import global.WagerrCoreContext.MIN_BET_AMOUNT
import global.exceptions.NoPeerConnectedException
import global.wrappers.TransactionWrapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_bet_event.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import org.wagerrj.core.Coin
import org.wagerrj.core.InsufficientMoneyException
import org.wagerrj.core.Transaction
import org.wagerrj.script.ScriptBuilder
import org.wagerrj.script.ScriptOpCodes
import org.wagerrj.wallet.Wallet
import wagerr.bet.toBetActions
import wagerr.bet.toListBets
import wallet.exceptions.InsufficientInputsException
import java.io.IOException


/**
 * Created by Neoperol on 5/11/17.
 */


class BetEventActivity : BaseDrawerActivity() {

    private val errorDialog: SimpleTextDialog by lazy {
        DialogsUtil.buildSimpleErrorTextDialog(this, resources.getString(R.string.invalid_inputs), "")
    }
    val pagerTitleList = listOf("Ongoing", "Finished")
    private var transaction: Transaction? = null

    private val REQ_CODE_SEND_DETAIL = 8990

    lateinit var viewpagerAdapter: BetEventPagerAdapter

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        layoutInflater.inflate(R.layout.activity_bet_event, container)
        setTitle(R.string.bet_screen_title)
        initViewPager()
        initMagicIndicator()
    }

    private fun initViewPager() {
        viewpagerAdapter = BetEventPagerAdapter(supportFragmentManager, listOf(OngoingBetEventFragment(), FinishedBetEventFragment()))
        view_pager.adapter = viewpagerAdapter
    }

    private fun initMagicIndicator() {
        val commonNavigator = CommonNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return pagerTitleList.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ColorTransitionPagerTitleView(context)
                simplePagerTitleView.text = pagerTitleList[index]
                simplePagerTitleView.normalColor = ContextCompat.getColor(this@BetEventActivity, R.color.colorPrimaryVeryLight)
                simplePagerTitleView.selectedColor = ContextCompat.getColor(this@BetEventActivity, R.color.colorPrimary)
                simplePagerTitleView.setOnClickListener {
                    view_pager.currentItem = index
                }
                return simplePagerTitleView
            }


            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.setColors(ContextCompat.getColor(this@BetEventActivity, R.color.colorPrimary))
                return indicator
            }

            override fun getTitleWeight(context: Context?, index: Int): Float {
                return 1F
            }
        }
        magic_indicator.navigator = commonNavigator
        ViewPagerHelper.bind(magic_indicator, view_pager)
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
            if (amount.isLessThan(MIN_BET_AMOUNT) || amount.isGreaterThan(MAX_BET_AMOUNT))
                throw IllegalArgumentException("Incorrect bet amount. Please ensure your bet is beteen 1 - 10000 WGR inclusive.")
            if (amount.isGreaterThan(Coin.valueOf(wagerrModule.availableBalance)))
                throw IllegalArgumentException("Insuficient balance")
            val params = WagerrCoreContext.NETWORK_PARAMETERS

            transaction = Transaction(params)
            // then outputs

            val script = ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(betActionStr.toByteArray()).build()
            transaction?.addOutput(amount, script) //bo BTC will be destroyed

            transaction = wagerrModule.completeTx(transaction)

            if (transaction?.outputs?.size != 2) {
                throw IllegalArgumentException("Bet transaction needs to have a change address. Please try with larger or smaller bet amount.")
            }

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
            transaction = Transaction(WagerrCoreContext.NETWORK_PARAMETERS, savedInstanceState.getByteArray(TX))
        }
    }


    private fun checkConnectivity(amountStr: String, betActionStr: String): Boolean {
        if (!isOnline()) {
            showErrorDialog(getString(R.string.error_no_connectivity_title), getString(R.string.error_no_connectivity_body))
            return false
        }
        return true
    }

    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }


    fun showErrorDialog(message: String?) {
        errorDialog.setBody(message)
        errorDialog.show(fragmentManager, resources.getString(R.string.send_error_dialog_tag))
    }

    fun showErrorDialog(title: String, message: String?) {
        errorDialog.setTitle(title)
        errorDialog.setBody(message)
        errorDialog.show(fragmentManager, resources.getString(R.string.send_error_dialog_tag))
    }

    override fun onResume() {
        super.onResume()
        // check current activity in the navigation drawer
        setNavigationMenuItemChecked(1)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.bet, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_listbets) {
            Observable.fromCallable {
                WagerrApplication.getInstance().module.listTx().map { it.transaction }
                        .toBetActions()?.toListBets()
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        AndroidUtils.copyToClipboard(this, it)
                        Toast.makeText(this,"Listbets Copied",Toast.LENGTH_SHORT).show()
                    }, {})
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBlockchainStateChange() {
        if (blockchainState == BlockchainState.SYNCING) {
            AnimationUtils.fadeInView(container_bet_event_syncing, 500)
        } else if (blockchainState == BlockchainState.SYNC) {
            AnimationUtils.fadeOutGoneView(container_bet_event_syncing, 500)
        } else if (blockchainState == BlockchainState.NOT_CONNECTION) {
            AnimationUtils.fadeInView(container_bet_event_syncing, 500)
        }
    }
}
