package com.nxt.nxtvault.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.ValueCallback;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.model.AccountData;

import java.util.ArrayList;

/**
 * Created by bcollins on 2015-06-01.
 */
public class AccountManager {
    Context mContext;
    JayClientApi mJayApi;
    SharedPreferences mSharedPreferences;

    private static final String preferenceKey = "accounts";

    Gson gson = new Gson();

    public AccountManager(Context context, JayClientApi jayClientApi, SharedPreferences sharedPreferences){
        mContext = context;
        mJayApi = jayClientApi;
        mSharedPreferences = sharedPreferences;
    }

    public ArrayList<AccountData> getAllAccounts(){
        ArrayList<AccountData> accountsList = null;

        String accounts = mSharedPreferences.getString(preferenceKey, null);

        if (accounts != null){
            accountsList = gson.fromJson(accounts, new TypeToken<ArrayList<AccountData>>() {}.getType());
        }

        return accountsList;
    }

    public void getNewAccount(final String pin, final ValueCallback<AccountData> callback){
        mJayApi.generateSecretPhrase(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                mJayApi.getNewAccount(value, pin, callback);
            }
        });
    }
//    public void storeAccount(AccountData account);
//    public void changePin(String newPin, String oldPin);
//    public void deleteAccount();
//    public void setSpendingPassword(AccountData accountData, String oldPassword, String newPassword);
}
