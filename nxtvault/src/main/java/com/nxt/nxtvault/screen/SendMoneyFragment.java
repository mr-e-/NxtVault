package com.nxt.nxtvault.screen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxt.nxtvault.model.AccountData;

/**
 * Created by Brandon on 5/24/2015.
 */
public class SendMoneyFragment extends BaseFragment {
    String mAccountRs;

    public static SendMoneyFragment getInstance(String accountRs){
        SendMoneyFragment fragment = new SendMoneyFragment();

        fragment.mAccountRs = accountRs;

        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
