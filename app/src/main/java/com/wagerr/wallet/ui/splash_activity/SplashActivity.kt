package com.wagerr.wallet.ui.splash_activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.VideoView

import com.wagerr.wallet.WagerrApplication
import com.wagerr.wallet.R
import com.wagerr.wallet.ui.start_activity.StartActivity
import com.wagerr.wallet.ui.wallet_activity.WalletActivity

import java.util.concurrent.TimeUnit

import io.reactivex.Observable

/**
 * Created by Neoperol on 6/13/17.
 */

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        Observable.timer(1500, TimeUnit.MILLISECONDS).subscribe({
            jump()
        },{})
    }


    private fun jump() {
        if (WagerrApplication.getInstance().appConf.isAppInit) {
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
        } else {
            // Jump to your Next Activity or MainActivity
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

}
