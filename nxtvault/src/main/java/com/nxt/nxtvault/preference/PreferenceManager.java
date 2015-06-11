package com.nxt.nxtvault.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.nxt.nxtvault.R;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by bcollins on 2015-04-30.
 */
@Singleton
public class PreferenceManager {
    protected SharedPreferences sharedPref;
    Context mContext;

    @Inject
    public PreferenceManager(Context context, SharedPreferences sharedPreferences){
        mContext = context;

        sharedPref = sharedPreferences;
    }

    public SharedPreferences getSharedPref(){
        return sharedPref;
    }

    public void putPinDigest(String digest){
        sharedPref.edit().putString(mContext.getString(R.string.pinDigest), digest).commit();
    }

    public void putLastPinEntry(Long millis){
        sharedPref.edit()
                .putLong(mContext.getString(R.string.time_btwen_last_pin_entry), millis)
                .commit();
    }

    public void putPinTryAttempts(long attempts){
        sharedPref.edit()
                .putLong(mContext.getString(R.string.pin_lockout_time), attempts)
                .commit();
    }

    public void putPinTryLockoutTime(long lockoutTime){
        sharedPref.edit()
                .putLong(mContext.getString(R.string.pin_attempts), lockoutTime)
                .commit();
    }

    public boolean getIsTestNet(){
        return sharedPref.getBoolean(mContext.getString(R.string.testnet_preference), false);
    }

    public String getCustomServer(){
        return sharedPref.getString(mContext.getString(R.string.server_preference), null);
    }

    public String getPinDigest(){
        return sharedPref.getString(mContext.getString(R.string.pinDigest), null);
    }

    public boolean getPinIsSet(){
        return getPinDigest() != null;
    }

    public int getCurrentVersion(){
        return getPinDigest() != null;
    }

    public String getPinTimeout(){
        return sharedPref.getString(mContext.getString(R.string.pin_timeout), "5");
    }

    public Long getLastPinEntry(){
        return sharedPref.getLong(mContext.getString(R.string.time_btwen_last_pin_entry), 0);
    }

    public Long getPinTryAttempts(){
        return sharedPref.getLong(mContext.getString(R.string.pin_lockout_time), 0);
    }

    public Long getPinTryLockoutTime(){
        return sharedPref.getLong(mContext.getString(R.string.pin_attempts), 0);
    }

    public void wipe() {
        sharedPref.edit().clear().commit();
    }

    public void logout() {

    }
}
