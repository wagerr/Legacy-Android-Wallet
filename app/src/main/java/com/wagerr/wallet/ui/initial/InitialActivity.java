package com.wagerr.wallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wagerr.wallet.WagerrApplication;
import com.wagerr.wallet.ui.splash_activity.SplashActivity;
import com.wagerr.wallet.ui.wallet_activity.WalletActivity;
import com.wagerr.wallet.utils.AppConf;

/**
 * Created by furszy on 8/19/17.
 */

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WagerrApplication wagerrApplication = WagerrApplication.getInstance();
        AppConf appConf = wagerrApplication.getAppConf();
        // show report dialog if something happen with the previous process
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}
