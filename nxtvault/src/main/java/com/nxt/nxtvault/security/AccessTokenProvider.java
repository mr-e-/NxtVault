package com.nxt.nxtvault.security;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.model.AccessTokens;
import com.nxt.nxtvault.model.AccountData;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Brandon on 4/12/2015.
 */
public class AccessTokenProvider {
    Gson gson = new Gson();

    String FILENAME = "access_tokens";

    private SharedPreferences mSharedPrefs;
    private ArrayList<AccessTokens> mAccessTokens;


    public AccessTokenProvider(SharedPreferences sharedPrefs){
        mSharedPrefs = sharedPrefs;

        String tokensJson = mSharedPrefs.getString(FILENAME, null);
        if (tokensJson != null){
            mAccessTokens = gson.fromJson(tokensJson, new TypeToken<ArrayList<AccessTokens>>() { }.getType());
        }
        else{
            mAccessTokens = new ArrayList<>();
        }
    }

    public String getNewToken(AccountData accountData){
        String guid = UUID.randomUUID().toString().replace("-", "");

        AccessTokens existing = findExistingAccount(accountData.publicKey);

        if (existing == null) {
            existing = new AccessTokens();
            existing.AccountPublicKey = accountData.publicKey;
            existing.AccessTokens = new ArrayList<>();
            existing.AccessTokens.add(guid);

            mAccessTokens.add(existing);
        }

        existing.AccessTokens.add(guid);

        mSharedPrefs.edit().putString(FILENAME, gson.toJson(mAccessTokens)).apply();

        return guid;
    }

    private AccessTokens findExistingAccount(String publicKey){
        for(AccessTokens token : mAccessTokens){
            if (token.AccountPublicKey.equals(publicKey)){
                return token;
            }
        }

        return null;
    }

    public String verify(String accessToken) {
        for(AccessTokens token : mAccessTokens){
            for(String key : token.AccessTokens){
                if (key.equals(accessToken)){
                    return token.AccountPublicKey;
                }
            }
        }

        return null;
    }
}
