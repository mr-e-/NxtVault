package com.nxt.nxtvault;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.BroadcastTxResponse;
import com.nxt.nxtvaultclientlib.jay.JayApi;

import java.util.ArrayList;

/**
 * Created by Brandon on 4/6/2015.
 */
public class JayClientApi extends JayApi {
    public JayClientApi(Context context, com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener listener) {
        super(context, Uri.parse("file:///android_asset/jay/index.html"), listener);
    }

    ValueCallback<String> generateSecretPhraseCallback;
    public void generateSecretPhrase(final ValueCallback<String> callback){
        generateSecretPhraseCallback = callback;

        mWebView.loadUrl("javascript: MyInterface.generateSecretPhraseResult(AndroidExtensions.generatePassPhrase());");
    }

    @JavascriptInterface
    public void generateSecretPhraseResult(final String result){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateSecretPhraseCallback.onReceiveValue(result);
            }
        });
    }

    ValueCallback<AccountData> getNewAccountCallback;
    public void getNewAccount(String secretPhrase, String pin, final ValueCallback<AccountData> callback){
        getNewAccountCallback = callback;

        mWebView.loadUrl("javascript: MyInterface.getNewAccountResult(JSON.stringify(newAccount('" + secretPhrase + "', '" + pin + "')));");
    }

    @JavascriptInterface
    public void getNewAccountResult(final String result){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getNewAccountCallback.onReceiveValue(gson.fromJson(result, AccountData.class));
            }
        });
    }

    public void storeAccount(AccountData accountData){
       String result = gson.toJson(accountData)
               .replace("\"", "'")
               //gson wants to format = ?
               .replace("\\u003d", "=");

        mWebView.loadUrl("javascript: AndroidExtensions.storeAccount(" + result + ");", null);
    }

    ValueCallback<String> changePinCallback;
    public void changePin(String mOldPin, String s, final ValueCallback<String> callback) {
        changePinCallback = callback;

        mWebView.loadUrl("javascript:MyInterface.changePinResult(AndroidExtensions.changePin('" + mOldPin + "', '" + s + "'));");
    }

    @JavascriptInterface
    public void changePinResult(final String result){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changePinCallback.onReceiveValue(result);
            }
        });
    }

    ValueCallback<ArrayList<AccountData>> loadAccountsCallback;
    public void loadAccounts(final ValueCallback<ArrayList<AccountData>> callback){
        loadAccountsCallback = callback;

        mWebView.loadUrl("javascript: MyInterface.loadAccountsResult(AndroidExtensions.getAccounts());");
    }

    @JavascriptInterface
    public void loadAccountsResult(final String result){
        final ArrayList<AccountData> accounts = gson.fromJson(result, new TypeToken<ArrayList<AccountData>>() {
        }.getType());

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadAccountsCallback.onReceiveValue(accounts);
            }
        });
    }

    ValueCallback<String> decryptSecretPhraseCallback;
    public void decryptSecretPhrase(AccountData accountData, final ValueCallback<String> callback){
        decryptSecretPhraseCallback = callback;
        mWebView.loadUrl("javascript: MyInterface.decryptSecretPhraseResult(decryptSecretPhrase('" + accountData.cipher + "', '" + accountData.key + "', '" + accountData.checksum + "'));");
    }

    @JavascriptInterface
    public void decryptSecretPhraseResult(final String result) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                decryptSecretPhraseCallback.onReceiveValue(result);
            }
        });
    }

    ValueCallback<String> extractTxDetailsCallback;
    public void extractTxDetails(AccountData accountData, final String txData, final ValueCallback<String> callback){
        extractTxDetailsCallback = callback;
        mWebView.loadUrl("javascript: MyInterface.extractTxDetailsResult(JSON.stringify(AndroidExtensions.extractBytesData('" + accountData.accountRS + "', '" + txData + "')));");
    }

    @JavascriptInterface
    public void extractTxDetailsResult(final String result) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                extractTxDetailsCallback.onReceiveValue(result);
            }
        });
    }


    ValueCallback<String> signCallback;
    public void sign(final AccountData accountData, final String txData, final ValueCallback<String> callback) {
        signCallback = callback;

        //get the account secret phrase
        decryptSecretPhrase(accountData, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String secretPhrase) {
            mWebView.loadUrl("javascript:MyInterface.signResult(JSON.stringify(AndroidExtensions.signTrfBytes('" + accountData.accountRS + "', '" + txData + "', '" + secretPhrase + "')));");
            }
        });
    }

    @JavascriptInterface
    public void signResult(String signedBytes){
        final String signedBytesString = gson.fromJson(signedBytes, String.class);

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                signCallback.onReceiveValue(signedBytesString);
            }
        });
    }

    public void deleteAccount(AccountData accountData) {
        mWebView.loadUrl("javascript:AndroidExtensions.deleteAccount('" + accountData.accountRS + "')", null);
    }

    public void broadcast(String txBytes, final ValueCallback<BroadcastTxResponse> callback){
        request("broadcastTransaction", "{'transactionBytes': '" + txBytes + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                final BroadcastTxResponse account = gson.fromJson(value, BroadcastTxResponse.class);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(account);
                    }
                });
            }
            }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e("broadcastTransaction", value);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }
}
