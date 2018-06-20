package com.wagerr.wallet.ui.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.wagerr.wallet.WagerrApplication;
import global.WagerrModule;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by furszy on 6/29/17.
 */

public class BaseFragment extends Fragment {

    protected WagerrApplication wagerrApplication;
    protected WagerrModule wagerrModule;
    public CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wagerrApplication = WagerrApplication.getInstance();
        wagerrModule = wagerrApplication.getModule();
    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getActivity(),permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
