package com.nxt.nxtvault.upgrade;

import android.content.Context;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.legacy.JayClientApi;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.preference.PreferenceManager;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Brandon on 5/30/2015.
 */
@Singleton
public class UpgradeAccountsToJavaTask implements IUpgradeTask{
    PreferenceManager mPreferences;
    Context mContext;
    JayClientApi mJay;
    AccountManager mAccountManager;

    private final String UPGRADE_ACCOUNTS = "upgrade_accounts";

    @Inject
    public UpgradeAccountsToJavaTask(Context context, PreferenceManager preferenceManager, JayClientApi jay, AccountManager accountManager){
        mPreferences = preferenceManager;
        mContext = context;
        mJay = jay;
        mAccountManager = accountManager;
    }

    @Override
    public void upgrade(final ValueCallback<Void> callback) {
        final JayClientApi legacyClientApi = new JayClientApi(mContext);
        legacyClientApi.addReadyListener(new IJavascriptLoadedListener() {
            @Override
            public void onLoaded() {
                legacyClientApi.loadAccounts(new ValueCallback<ArrayList<AccountData>>() {
                    @Override
                    public void onReceiveValue(ArrayList<AccountData> value) {
                        if (value != null && value.size() > 0){
                            for (AccountData account : value){
                                mAccountManager.storeAccount(account);
                            }
                        }

                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }

    @Override
    public boolean requiresUpgrade(int fromVersion) {
        return fromVersion <= 10;
    }
}
