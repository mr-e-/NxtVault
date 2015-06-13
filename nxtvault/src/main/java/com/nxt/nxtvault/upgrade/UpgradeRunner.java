package com.nxt.nxtvault.upgrade;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.preference.PreferenceManager;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Brandon on 6/12/2015.
 */
@Singleton
public class UpgradeRunner {
    private Context mContext;
    private PreferenceManager mPreferences;
    private JayClientApi mJay;
    private PinManager mPinManager;
    private AccountManager mAccountManager;

    int count = 0;

    ValueCallback<Boolean> onCompletedCallback;

    final ArrayList<IUpgradeTask> upgradeTasks = new ArrayList<>();

    @Inject
    public UpgradeRunner(Context context, PreferenceManager preferenceManager, JayClientApi jayClientApi, PinManager pinManager, AccountManager accountManager){
        mContext = context;

        mPreferences = preferenceManager;
        mJay = jayClientApi;
        mPinManager = pinManager;
        mAccountManager = accountManager;

        //upgrade pin so it is no longer stored in internal storage
        upgradeTasks.add(new UpgradePinTask(mContext, mPreferences, mJay, mPinManager));
        upgradeTasks.add(new UpgradeAccountsToJavaTask(mContext, mPreferences, mJay, mAccountManager));
        upgradeTasks.add(new UpgradePin2Task(mContext, mPreferences, mJay));
    }

    public void run(ValueCallback<Boolean> callback){
        onCompletedCallback = callback;

        int version = mPreferences.getCurrentVersion();

        try {
            for (IUpgradeTask task : upgradeTasks) {
                if (task.requiresUpgrade(version)) {
                    task.upgrade(version, new ValueCallback<Void>() {
                        @Override
                        public void onReceiveValue(Void value) {
                            if (++count == upgradeTasks.size()) {
                                finish(true);
                            }
                        }
                    });
                } else {
                    if (++count == upgradeTasks.size()) {
                        finish(true);
                    }
                }
            }
        }
        catch (Exception ex){
            finish(false);
        }
    }

    public boolean requiresUpgrade(){
        int version = mPreferences.getCurrentVersion();

        for(IUpgradeTask task : upgradeTasks) {
            if (task.requiresUpgrade(version)) {
                return true;
            }
        }

        return false;
    }

    private void finish(boolean result){
        if (result){
            try {
                PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                int version = pInfo.versionCode;
                mPreferences.putCurrentVersion(version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        onCompletedCallback.onReceiveValue(result);
    }
}
