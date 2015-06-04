package com.nxt.nxtvault.upgrade;

import android.content.Context;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.preference.PreferenceManager;

/**
 * Created by Brandon on 5/30/2015.
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
    public void upgrade(final ValueCallback<Void> callback) {
        final String pin = mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null);

        mPinManager.changePin(pin);
    }

    @Override
    public boolean requiresUpgrade() {
        String pin = mPreferences.getSharedPref().getString(mContext.getString(R.string.pin), null);
        return pin != null;
    }
}
