package com.nxt.nxtvault.upgrade;

import android.content.Context;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.preference.PreferenceManager;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;

/**
 *  on 5/30/2015.
 */
public class UpgradePinTask implements IUpgradeTask{
    PreferenceManager mPreferences;
    Context mContext;
    JayClientApi mJay;
    private PinManager mPinManager;

    public UpgradePinTask(Context context, PreferenceManager preferenceManager, JayClientApi jay, PinManager pinManager){
        mPreferences = preferenceManager;
        mContext = context;
        mJay = jay;
        mPinManager = pinManager;
    }

    @Override
    public void upgrade(int fromVersion, final ValueCallback<Void> callback) {
        String pin;

        if (fromVersion <= 9){
            pin = mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null);

            mPinManager.changePin(pin);

            mPinManager.clearSession();

            mPreferences.getSharedPref().edit().putString(mContext.getString(R.string.pin), null).commit();

            callback.onReceiveValue(null);
        }
    }

    @Override
    public boolean requiresUpgrade(int fromVersion) {
        return (fromVersion <= 9 && mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null) != null);
    }
}
