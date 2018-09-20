package com.wagerr.wallet.ui.pincode_activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.wagerr.wallet.R
import com.wagerr.wallet.ui.backup_mnemonic_activity.MnemonicActivity
import com.wagerr.wallet.ui.backup_mnemonic_activity.MnemonicActivity.INTENT_EXTRA_INIT_VIEW
import com.wagerr.wallet.ui.base.BaseActivity
import com.wagerr.wallet.ui.settings_pincode_activity.KeyboardFragment
import com.wagerr.wallet.ui.start_activity.StartActivity
import com.wagerr.wallet.utils.TIME_OUT
import com.wagerr.wallet.utils.getPing
import global.WagerrCoreContext
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import network.PeerData
import network.PeerGlobalData
import org.slf4j.LoggerFactory
import java.util.Random

/**
 * Created by Neoperol on 4/20/17.
 */

class PincodeActivity : BaseActivity(), KeyboardFragment.onKeyListener {
    private val logger = LoggerFactory.getLogger(PincodeActivity::class.java)

    private var checkPin = false

    private var i1: ImageView? = null
    private var i2: ImageView? = null
    private var i3: ImageView? = null
    private var i4: ImageView? = null
    private val pin = IntArray(4)
    private var lastPos = 0

    private var keyboardFragment: KeyboardFragment? = null

    override fun onCreateView(savedInstanceState: Bundle?, container: ViewGroup) {
        title = "Create Pin"

        if (intent != null && intent.hasExtra(CHECK_PIN)) {
            checkPin = true
            title = "Check Pin"
        }

        if (wagerrApplication.appConf.pincode != null && !checkPin) {
            goNext()
            finish()
        }

        layoutInflater.inflate(R.layout.fragment_pincode, container)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        i1 = findViewById(R.id.imageview_circle1)
        i2 = findViewById(R.id.imageview_circle2)
        i3 = findViewById(R.id.imageview_circle3)
        i4 = findViewById(R.id.imageview_circle4)
        keyboardFragment = supportFragmentManager.findFragmentById(R.id.fragment_keyboard) as KeyboardFragment
        keyboardFragment!!.setOnKeyListener(this)
        keyboardFragment!!.setTextButtonsColor(Color.WHITE)
    }

    private fun goNext() {
        if (wagerrApplication.appConf.trustedNode == null) {
            // select random trusted node
            selectNodesAndGotoWords()
        } else {
            gotoWords()
        }
    }

    private fun selectNodesAndGotoWords() {
        val nodes: List<PeerData>
        if (WagerrCoreContext.IS_TEST) {
            nodes = PeerGlobalData.listTrustedTestHosts()
        } else {
            nodes = PeerGlobalData.listTrustedHosts()
        }
        compositeDisposable += Observable.fromIterable(nodes)
                .flatMap { peerData ->
                    Observable.fromCallable {
                        val ping = getPing(peerData.host, peerData.tcpPort)
                        (peerData to ping)
                    }.subscribeOn(Schedulers.io())
                }
                .filter { it.second != TIME_OUT }
                .first(nodes[Random().nextInt(nodes.size)] to 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    logger.info(it.first.toString())
                    setNode(it.first)
                    gotoWords()
                }, { it.printStackTrace() })
    }

    private fun setNode(peerData: PeerData) {
        wagerrApplication.setTrustedServer(peerData)
        wagerrApplication.stopBlockchain()
    }

    private fun gotoWords() {
        wagerrApplication.appConf.isAppInit = true
        val myIntent = Intent(this@PincodeActivity, MnemonicActivity::class.java)
        myIntent.putExtra(INTENT_EXTRA_INIT_VIEW, true)
        startActivity(myIntent)
        finish()
    }

    override fun onKeyClicked(key: KeyboardFragment.KEYS) {
        if (lastPos < 4) {
            if (key.value < 10) {
                pin[lastPos] = key.value
                activeCheck(lastPos)
                lastPos++
                if (lastPos == 4) {
                    val pincode = pin[0].toString() + pin[1].toString() + pin[2].toString() + pin[3].toString()

                    if (!checkPin) {
                        wagerrApplication.appConf.savePincode(pincode)
                        Toast.makeText(this, R.string.pincode_saved, Toast.LENGTH_SHORT).show()
                        goNext()
                    } else {
                        // check pin and return result
                        if (wagerrApplication.appConf.pincode == pincode) {
                            val intent = Intent()
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            Toast.makeText(this, R.string.bad_pin_code, Toast.LENGTH_LONG).show()
                            clear()
                        }
                    }
                }
            } else if (key == KeyboardFragment.KEYS.DELETE) {
                if (lastPos != 0) {
                    lastPos--
                    unactiveCheck(lastPos)
                }
            } else if (key == KeyboardFragment.KEYS.CLEAR) {
                clear()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // todo: controlar esto
        if (wagerrApplication.appConf.pincode == null) {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }

    private fun clear() {
        unactiveCheck(0)
        unactiveCheck(1)
        unactiveCheck(2)
        unactiveCheck(3)
        lastPos = 0
    }

    private fun activeCheck(pos: Int) {
        when (pos) {
            0 -> i1!!.setImageResource(R.drawable.pin_circle_active)
            1 -> i2!!.setImageResource(R.drawable.pin_circle_active)
            2 -> i3!!.setImageResource(R.drawable.pin_circle_active)
            3 -> i4!!.setImageResource(R.drawable.pin_circle_active)
        }
    }

    private fun unactiveCheck(pos: Int) {
        when (pos) {
            0 -> i1!!.setImageResource(R.drawable.pin_circle)
            1 -> i2!!.setImageResource(R.drawable.pin_circle)
            2 -> i3!!.setImageResource(R.drawable.pin_circle)
            3 -> i4!!.setImageResource(R.drawable.pin_circle)
        }
    }

    companion object {

        const val CHECK_PIN = "check_pin"
    }
}
