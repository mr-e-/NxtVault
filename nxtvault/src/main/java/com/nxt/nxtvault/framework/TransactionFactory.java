package com.nxt.nxtvault.framework;

import android.content.Intent;

import com.nxt.nxtvault.preference.PreferenceManager;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *  on 5/30/2015.
 */
@Singleton
public class TransactionFactory {
    public static String TEMP_TOKEN = "tempToken";

    private PreferenceManager mPreference;

    @Inject
    protected TransactionFactory(PreferenceManager preferenceManager){
        mPreference = preferenceManager;
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
