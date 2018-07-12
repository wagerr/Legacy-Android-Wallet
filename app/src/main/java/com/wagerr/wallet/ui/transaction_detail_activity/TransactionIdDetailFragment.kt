package com.wagerr.wallet.ui.transaction_detail_activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.wagerr.wallet.R
import com.wagerr.wallet.R.id.*
import com.wagerr.wallet.ui.base.BaseFragment
import com.wagerr.wallet.ui.base.tools.adapter.BaseRecyclerAdapter
import com.wagerr.wallet.ui.base.tools.adapter.BaseRecyclerViewHolder
import com.wagerr.wallet.ui.base.tools.adapter.ListItemListeners
import com.wagerr.wallet.ui.bet_action_detail.BetActionDetailActivity.Companion.EXTRA_TX_ID
import com.wagerr.wallet.ui.transaction_detail_activity.FragmentTxDetail.*
import com.wagerr.wallet.utils.DialogsUtil

import org.wagerrj.core.Coin
import org.wagerrj.core.Transaction
import org.wagerrj.core.TransactionInput
import org.wagerrj.core.TransactionOutPoint
import org.wagerrj.core.TransactionOutput
import org.wagerrj.script.Script

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.concurrent.Callable

import global.AddressLabel
import global.WagerrCoreContext
import global.wrappers.TransactionWrapper
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import wallet.exceptions.TxNotFoundException

import com.wagerr.wallet.ui.transaction_send_activity.custom.inputs.InputsActivity.INTENT_NO_TOTAL_AMOUNT
import com.wagerr.wallet.ui.transaction_send_activity.custom.inputs.InputsFragment.INTENT_EXTRA_UNSPENT_WRAPPERS
import io.reactivex.rxkotlin.plusAssign

/**
 * Created by furszy on 8/7/17.
 */

class TransactionIdDetailFragment : BaseFragment(), View.OnClickListener {

    private var root: View? = null
    private var txt_transaction_id: TextView? = null
    private var txt_amount: TextView? = null
    private var txt_date: TextView? = null
    private var recycler_outputs: RecyclerView? = null
    private var txt_memo: TextView? = null
    private var txt_fee: TextView? = null
    private var txt_inputs: TextView? = null
    private var txt_date_title: TextView? = null
    private var txt_confirmations: TextView? = null
    private var container_confirmations: TextView? = null
    private var txt_tx_weight: TextView? = null

    private lateinit var transactionWrapper: TransactionWrapper
    private var isTxDetail = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_transaction_id_detail, container, false)


        txt_transaction_id = root!!.findViewById<View>(R.id.txt_transaction_id) as TextView
        txt_amount = root!!.findViewById<View>(R.id.txt_amount) as TextView
        txt_date = root!!.findViewById<View>(R.id.txt_date) as TextView
        txt_memo = root!!.findViewById<View>(R.id.txt_memo) as TextView
        txt_fee = root!!.findViewById<View>(R.id.txt_fee) as TextView
        txt_inputs = root!!.findViewById<View>(R.id.txt_inputs) as TextView
        txt_date_title = root!!.findViewById<View>(R.id.txt_date_title) as TextView
        recycler_outputs = root!!.findViewById<View>(R.id.recycler_outputs) as RecyclerView
        txt_confirmations = root!!.findViewById<View>(R.id.txt_confirmations) as TextView
        container_confirmations = root!!.findViewById<View>(R.id.container_confirmations) as TextView
        txt_tx_weight = root!!.findViewById<View>(R.id.txt_tx_weight) as TextView

        txt_inputs!!.setOnClickListener(this)

       compositeDisposable += Observable.fromCallable {
            wagerrModule.getTxWrapper(activity?.intent?.getStringExtra(EXTRA_TX_ID))
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    this.transactionWrapper = it
                    val intent = activity!!.intent
                    if (intent != null) {
                        if (intent.hasExtra(IS_DETAIL)) {
                            transactionWrapper.transaction = wagerrModule.getTx(transactionWrapper.txId)
                            isTxDetail = true
                        } else {
                            transactionWrapper.transaction = Transaction(WagerrCoreContext.NETWORK_PARAMETERS, intent.getByteArrayExtra(TX))
                            if (intent.hasExtra(TX_MEMO)) {
                                transactionWrapper.transaction.memo = intent.getStringExtra(TX_MEMO)
                            }
                            isTxDetail = false
                        }
                    }
                    try {
                        loadTx()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { it.printStackTrace() })
        return root
    }

    private fun loadTx() {
        if (!isTxDetail) {
            txt_date_title!!.visibility = View.GONE
            txt_date!!.visibility = View.GONE
            container_confirmations!!.visibility = View.GONE
            txt_confirmations!!.visibility = View.GONE
        } else {
            // set date
            val simpleDateFormat = SimpleDateFormat("dd/MM/yy HH:mm")
            txt_date!!.text = simpleDateFormat.format(transactionWrapper!!.transaction.updateTime)
        }
        txt_transaction_id!!.text = transactionWrapper!!.transaction.hashAsString
        txt_amount!!.text = transactionWrapper.amount.toFriendlyString()
        var fee: Coin? = null
        if (transactionWrapper.isStake) {
            fee = Coin.ZERO
        } else if (transactionWrapper.transaction.fee != null) {
            fee = transactionWrapper.transaction.fee
        } else {
            try {
                // Fee calculation with low performance, have to check why the fee is null here..
                var inputsSum = Coin.ZERO
                for (input in transactionWrapper.transaction.inputs) {
                    val unspent = input.outpoint
                    inputsSum = inputsSum.plus(wagerrModule.getUnspentValue(unspent.hash, unspent.index.toInt()))
                }
                fee = inputsSum.subtract(transactionWrapper.transaction.outputSum)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        if (fee != null)
            txt_fee!!.text = fee.toFriendlyString()
        else
            txt_fee!!.setText(R.string.no_data_available)

        if (transactionWrapper.transaction.memo != null && transactionWrapper.transaction.memo.length > 0) {
            txt_memo!!.text = transactionWrapper.transaction.memo
        } else {
            txt_memo!!.setText(R.string.tx_detail_no_memo)
        }

        txt_confirmations!!.text = transactionWrapper.transaction.confidence.depthInBlocks.toString()

        txt_tx_weight!!.text = transactionWrapper.transaction.unsafeBitcoinSerialize().size.toString() + " bytes"

        txt_inputs!!.text = getString(R.string.tx_detail_inputs, transactionWrapper.transaction.inputs.size)

        val list = ArrayList<OutputUtil>()

        for (transactionOutput in transactionWrapper.transaction.outputs) {

            val label: String
            if (transactionWrapper.outputLabels != null && transactionWrapper.outputLabels.containsKey(transactionOutput.index)) {
                val addressLabel = transactionWrapper.outputLabels[transactionOutput.index]
                if (addressLabel != null) {
                    if (addressLabel.name != null) {
                        label = addressLabel.name
                    } else
                    //label = addressLabel.getAddresses().get(0);
                        label = transactionOutput.scriptPubKey.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true).toBase58()
                } else {
                    label = transactionOutput.scriptPubKey.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true).toBase58()
                }
            } else {
                val script = transactionOutput.scriptPubKey
                if (script.isPayToScriptHash || script.isSentToRawPubKey || script.isSentToAddress) {
                    label = script.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true).toBase58()
                } else if (script.isOpReturn) {
                    label = script.toString()
                } else {
                    label = "NON-STANDARD"
                }
            }

            list.add(
                    OutputUtil(
                            transactionOutput.index,
                            label, // for now.. //label,
                            transactionOutput.value
                    )
            )
        }

        setupOutputs(list)

    }

    private fun setupOutputs(list: List<OutputUtil>) {
        recycler_outputs!!.layoutManager = LinearLayoutManager(activity)
        recycler_outputs!!.setHasFixedSize(true)
        val listItemListener = object : ListItemListeners<OutputUtil> {
            override fun onItemClickListener(data: OutputUtil, position: Int) {

            }

            override fun onLongItemClickListener(data: OutputUtil, position: Int) {
                if (wagerrModule.chechAddress(data.label)) {
                    DialogsUtil.showCreateAddressLabelDialog(activity, data.label)
                }
            }
        }
        recycler_outputs!!.adapter = object : BaseRecyclerAdapter<OutputUtil, DetailOutputHolder>(activity, list, listItemListener) {
            override fun createHolder(itemView: View, type: Int): DetailOutputHolder {
                return DetailOutputHolder(itemView, type)
            }

            override fun getCardViewResource(type: Int): Int {
                return R.layout.detail_output_row
            }

            override fun bindHolder(holder: DetailOutputHolder, data: OutputUtil, position: Int) {
                holder.txt_num.text = "Position $position"
                holder.txt_address.text = data.label
                holder.txt_value.text = data.amount.toFriendlyString()
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.txt_inputs) {
            try {
                val intent = Intent(activity, InputsDetailActivity::class.java)
                val bundle = Bundle()
                bundle.putBoolean(INTENT_NO_TOTAL_AMOUNT, true)
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, wagerrModule.convertFrom(transactionWrapper!!.transaction.inputs) as Serializable)
                intent.putExtras(bundle)
                startActivity(intent)
            } catch (e: TxNotFoundException) {
                e.printStackTrace()
                Toast.makeText(activity, R.string.detail_no_available_inputs, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private inner class OutputUtil(val pos: Int, val label: String, val amount: Coin)

    class DetailOutputHolder(itemView: View, holderType: Int) : BaseRecyclerViewHolder(itemView, holderType) {

        internal var txt_num: TextView
        internal var txt_address: TextView
        internal var txt_value: TextView

        init {
            txt_num = itemView.findViewById<View>(R.id.txt_num) as TextView
            txt_address = itemView.findViewById<View>(R.id.txt_address) as TextView
            txt_value = itemView.findViewById<View>(R.id.txt_value) as TextView

        }
    }

    companion object {

        val TX = "tx"
        val EXTRA_TX_ID = "EXTRA_TX_ID"
        val IS_DETAIL = "is_detail"
        val TX_MEMO = "tx_memo"
    }
}
