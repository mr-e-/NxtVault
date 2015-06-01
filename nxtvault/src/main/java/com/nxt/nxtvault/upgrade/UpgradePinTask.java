package com.nxt.nxtvault.upgrade;

import android.content.Context;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.MyApp;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.preference.PreferenceManager;

/**
 * Created by Brandon on 5/30/2015.
 */
public class UpgradePinTask implements IUpgradeTask{
    PreferenceManager mPreferences;
    Context mContext;
    JayClientApi mJay;


    public UpgradePinTask(Context context, PreferenceManager preferenceManager, JayClientApi jay){
        mPreferences = preferenceManager;
        mContext = context;
        mJay = jay;
    }

    @Override
    public void upgrade(final ValueCallback<Void> callback) {
        final String pin = mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null);

        mJay.storePinChecksum(pin, new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                mPreferences.getSharedPref().edit().putString(mContext.getString(R.string.pin), null).commit();
                mPreferences.getSharedPref().edit().putBoolean(mContext.getString(R.string.pinIsSet), true).commit();
                MyApp.SessionPin = pin;

                callback.onReceiveValue(null);
            }
        });
    }

    @Override
    public boolean requiresUpgrade() {
        String pin = mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null);
        return pin != null;
    }
}
