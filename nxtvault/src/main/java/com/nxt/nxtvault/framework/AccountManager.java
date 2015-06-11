package com.nxt.nxtvault.framework;

import android.content.Context;
import android.webkit.ValueCallback;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.legacy.JayClientApi;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.preference.PreferenceManager;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by bcollins on 2015-06-01.
 */
@Singleton
public class AccountManager {
    Context mContext;
    JayClientApi mJayApi;
    PreferenceManager mPreferenceManager;
    PinManager mPinManager;

    private static final String preferenceKey = "accounts";

    Gson gson = new Gson();

    private ArrayList<AccountData> mAccountData;

    @Inject
    public AccountManager(Context context, JayClientApi jayClientApi, PinManager pinManager, PreferenceManager preferenceManager){
        mContext = context;
        mJayApi = jayClientApi;
        mPreferenceManager = preferenceManager;
        mPinManager = pinManager;
    }

    public ArrayList<AccountData> getAllAccounts(){
        if (mAccountData == null) {
            String accounts = mPreferenceManager.getSharedPref().getString(preferenceKey, null);

            if (accounts != null) {
                mAccountData = gson.fromJson(accounts, new TypeToken<ArrayList<AccountData>>() {
                }.getType());
            }
            else{
                mAccountData = new ArrayList<>();
            }
        }

        return mAccountData;
    }

    public AccountData getAccountByName(String name){
        for(AccountData account: mAccountData){
            if (account.accountName.toLowerCase().equals(name.toLowerCase())){
                return account;
            }
        }

        return null;
    }

    public void getNewAccount(final ValueCallback<AccountData> callback){
        mJayApi.generateSecretPhrase(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                mJayApi.getNewAccount(value, mPinManager.getSessionPin(), callback);
            }
        });
    }

    public void getNewAccount(String passphrase, String pin, final ValueCallback<AccountData> callback){
        mJayApi.getNewAccount(passphrase, pin, callback);
    }

    public void storeAccount(AccountData account){
        account.secretPhrase = null;

        mAccountData.add(account);

        saveAccounts();
    }

    public void changePin(final String newPin, final String oldPin, final ValueCallback<Void> callback){
        final int [] count = new int[1];

        count[0] = mAccountData.size();

        for (final AccountData accountData : mAccountData){
            mJayApi.decryptSecretPhrase(accountData, oldPin, "", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    mJayApi.encryptSecretPhrase(value, newPin, "", new ValueCallback<JayClientApi.EncryptSecretPhraseResult>() {
                        @Override
                        public void onReceiveValue(JayClientApi.EncryptSecretPhraseResult value) {
                            accountData.checksum = value.checksum;
                            accountData.cipher = value.cipher;

                            if (--count[0] == 0){
                                saveAccounts();

                                mPinManager.changePin(newPin);

                                callback.onReceiveValue(null);
                            }
                        }
                    });
                }
            });
        }
    }

    public void deleteAccount(AccountData accountData){
        mAccountData.remove(accountData);

        saveAccounts();
    }

    public void setSpendingPassword(final AccountData accountData, String oldPassword, final String newPassword, final ValueCallback<Void> callback){
        mJayApi.decryptSecretPhrase(accountData, mPinManager.getSessionPin(), oldPassword, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                mJayApi.encryptSecretPhrase(value, mPinManager.getSessionPin(), newPassword, new ValueCallback<JayClientApi.EncryptSecretPhraseResult>() {
                    @Override
                    public void onReceiveValue(JayClientApi.EncryptSecretPhraseResult value) {
                        accountData.checksum = value.checksum;
                        accountData.cipher = value.cipher;
                        accountData.spendingPassphrase = newPassword != null && newPassword != "";

                        saveAccounts();

                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }

    public void verifySpendingPassword(AccountData accountData, String password, final ValueCallback<Boolean> callback){
        mJayApi.decryptSecretPhrase(accountData, mPinManager.getSessionPin(), password, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(!value.equals("false"));
            }
        });
    }

    private void saveAccounts(){
        mPreferenceManager.getSharedPref().edit().putString(preferenceKey, gson.toJson(mAccountData)).apply();
    }

    public void deleteAllAccount() {
        mAccountData.clear();
        saveAccounts();
    }
}
