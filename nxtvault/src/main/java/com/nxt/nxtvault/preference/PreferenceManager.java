package com.nxt.nxtvault.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.nxt.nxtvault.R;

/**
 * Created by bcollins on 2015-04-30.
 */
public class PreferenceManager {
    protected SharedPreferences sharedPref;
    Context mContext;

    public PreferenceManager(Context context){
        mContext = context;

        sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.app_file_key), Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPref(){
        return sharedPref;
    }

    public void putPin(String pin){
        sharedPref.edit()
                .putString(mContext.getString(R.string.pin), pin)
                .apply();
    }

    public void putLastPinEntry(Long millis){
        sharedPref.edit()
                .putLong(mContext.getString(R.string.time_btwen_last_pin_entry), millis)
                .apply();
    }

    public boolean getIsTestNet(){
        return sharedPref.getBoolean(mContext.getString(R.string.testnet_preference), false);
    }

    public String getCustomServer(){
        return sharedPref.getString(mContext.getString(R.string.server_preference), null);
    }

    public String getPin(){
        return sharedPref.getString(mContext.getString(R.string.pin), null);
    }

    public String getPinTimeout(){
        return sharedPref.getString(mContext.getString(R.string.pin_timeout), "5");
    }

    public Long getLastPinEntry(){
        return sharedPref.getLong(mContext.getString(R.string.time_btwen_last_pin_entry), 0);
    }
}
