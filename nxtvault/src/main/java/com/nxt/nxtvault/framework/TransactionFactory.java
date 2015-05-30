package com.nxt.nxtvault.framework;

import android.content.Intent;

import com.nxt.nxtvault.preference.PreferenceManager;

import java.util.UUID;

/**
 * Created by Brandon on 5/30/2015.
 */
public class TransactionFactory {
    private static TransactionFactory txFactory;

    public static String TEMP_TOKEN = "tempToken";

    private PreferenceManager mPreference;

    protected TransactionFactory(PreferenceManager preferenceManager){
        mPreference = preferenceManager;
    }

    public static TransactionFactory getTransactionFactory(PreferenceManager preferenceManager){
        if (txFactory == null){
            txFactory = new TransactionFactory(preferenceManager);
        }

        return txFactory;
    }

    public Intent createSelfSignedTx(String intentName, String txData){
        String tempToken = UUID.randomUUID().toString();
        mPreference.getSharedPref().edit().putString(TEMP_TOKEN, tempToken).commit();

        Intent intent = new Intent(intentName);

        intent.putExtra("AccessToken", tempToken);
        intent.putExtra("TransactionData", txData);

        return intent;
    }
}
