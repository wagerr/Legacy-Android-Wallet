package com.wagerr.wallet.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.wagerr.wallet.WagerrApplication;
import com.wagerr.wallet.R;
import global.WagerrModule;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.internal.operators.completable.CompletableDisposeOn;

import com.wagerr.wallet.ui.base.dialogs.SimpleTextDialog;
import com.wagerr.wallet.utils.DialogsUtil;

import static com.wagerr.wallet.service.IntentsConstants.ACTION_STORED_BLOCKCHAIN_ERROR;
import static com.wagerr.wallet.service.IntentsConstants.ACTION_TRUSTED_PEER_CONNECTION_FAIL;

/**
 * Created by furszy on 6/8/17.
 */

public class WagerrActivity extends AppCompatActivity {

    protected WagerrApplication wagerrApplication;
    protected WagerrModule wagerrModule;

    protected LocalBroadcastManager localBroadcastManager;
    private static final IntentFilter intentFilter = new IntentFilter(ACTION_TRUSTED_PEER_CONNECTION_FAIL);
    private static final IntentFilter errorIntentFilter = new IntentFilter(ACTION_STORED_BLOCKCHAIN_ERROR);

    protected boolean isOnForeground = false;
    public CompositeDisposable compositeDisposable = new CompositeDisposable();

    private BroadcastReceiver trustedPeerConnectionDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_TRUSTED_PEER_CONNECTION_FAIL)) {
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context,R.string.title_no_trusted_peer_connection,R.string.message_no_trusted_peer_connection);
                simpleTextDialog.show(getFragmentManager(),"fail_node_connection_dialog");
            }else if (action.equals(ACTION_STORED_BLOCKCHAIN_ERROR)){
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleErrorTextDialog(context,R.string.title_blockstore_error,R.string.message_blockstore_error);
                simpleTextDialog.show(getFragmentManager(),"blockstore_error_dialog");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wagerrApplication = WagerrApplication.getInstance();
        wagerrModule = wagerrApplication.getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnForeground = true;
        localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver,intentFilter);
        localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver,errorIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnForeground = false;
        localBroadcastManager.unregisterReceiver(trustedPeerConnectionDownReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
