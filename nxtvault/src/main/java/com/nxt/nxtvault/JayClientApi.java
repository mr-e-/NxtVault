package com.nxt.nxtvault;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.BroadcastTxResponse;
import com.nxt.nxtvaultclientlib.jay.JayApi;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by bcollins on 2015-06-11.
 */
public class JayClientApi extends JayApi {
    public JayClientApi(Context context) {
        super(context, Uri.parse("file:///android_asset/jay/index.html"));
    }

    //GenerateSecretPhrase----------------------------------------------------------------------------------------------

    ValueCallback<String> generateSecretPhraseCallback;
    public void generateSecretPhrase(final ValueCallback<String> callback){
        generateSecretPhraseCallback = callback;

        mWebView.loadUrl("javascript: MyInterface.generateSecretPhraseResult(AndroidExtensions.generatePassPhrase());");
    }

    @JavascriptInterface
    public void generateSecretPhraseResult(final String result){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                generateSecretPhraseCallback.onReceiveValue(result);
            }
        });
    }

    //DecryptSecretPhrase----------------------------------------------------------------------------------------------

    ValueCallback<String> decryptSecretPhraseCallback;
    public void decryptSecretPhrase(AccountData accountData, String pin, String password, final ValueCallback<String> callback){
        decryptSecretPhraseCallback = callback;

        password = password.replace("\\", "\\\\").replace("'", "\\'");

        mWebView.loadUrl("javascript: MyInterface.decryptSecretPhraseResult(decryptSecretPhrase('" + accountData.cipher + "', '" + password + pin + "', '" + accountData.checksum + "'));");
    }

    @JavascriptInterface
    public void decryptSecretPhraseResult(final String result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                decryptSecretPhraseCallback.onReceiveValue(result);
            }
        });
    }

    //GetNewAccount----------------------------------------------------------------------------------------------

    ValueCallback<AccountData> getNewAccountCallback;
    public void getNewAccount(String secretPhrase, String pin, final ValueCallback<AccountData> callback){
        getNewAccountCallback = callback;

        secretPhrase = secretPhrase.replace("\\", "\\\\").replace("'", "\\'");

        mWebView.loadUrl("javascript: MyInterface.getNewAccountResult(JSON.stringify(newAccount('" + secretPhrase + "', '" + pin + "')));");
    }

    @JavascriptInterface
    public void getNewAccountResult(final String result){
        final AccountData accountData = gson.fromJson(result, AccountData.class);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getNewAccountCallback.onReceiveValue(accountData);
            }
        });
    }

    //ExtractUnsignedTxDetails----------------------------------------------------------------------------------------------

    ValueCallback<String> extractUnsignedTxDetailsCallback;
    public void extractUnsignedTxBytes(AccountData accountData, final String txData, final ValueCallback<String> callback){
        extractUnsignedTxDetailsCallback = callback;
        mWebView.loadUrl("javascript: MyInterface.extractUnsignedTxDetailsResult(JSON.stringify(AndroidExtensions.extractBytesData('" + txData + "', '" + accountData.publicKey + "')));");
    }

    @JavascriptInterface
    public void extractUnsignedTxDetailsResult(final String result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                extractUnsignedTxDetailsCallback.onReceiveValue(result);
            }
        });
    }

    //ExtractSignedTxDetails----------------------------------------------------------------------------------------------

    ValueCallback<String> extractSignedTxDetailsCallback;
    public void extractSignedTxBytes(final String txData, final ValueCallback<String> callback){
        extractSignedTxDetailsCallback = callback;
        mWebView.loadUrl("javascript: MyInterface.extractSignedTxDetailsResult(JSON.stringify(AndroidExtensions.extractBytesData('" + txData + "')));");
    }

    @JavascriptInterface
    public void extractSignedTxDetailsResult(final String result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                extractSignedTxDetailsCallback.onReceiveValue(result);
            }
        });
    }

    //Sign----------------------------------------------------------------------------------------------

    ValueCallback<String> signCallback;
    public void sign(final AccountData accountData, String key, String password, final String txData, final ValueCallback<String> callback) {
        signCallback = callback;

        //get the account secret phrase
        decryptSecretPhrase(accountData, key, password, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String secretPhrase) {
                String parsePhrase = secretPhrase.replace("\\", "\\\\").replace("'", "\\'");

                mWebView.loadUrl("javascript:MyInterface.signResult(JSON.stringify(AndroidExtensions.signTrfBytes('" + accountData.publicKey + "', '" + txData + "', '" + parsePhrase + "')));");
            }
        });
    }

    @JavascriptInterface
    public void signResult(String signedBytes){
        final String signedBytesString = gson.fromJson(signedBytes, String.class);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                signCallback.onReceiveValue(signedBytesString);
            }
        });
    }

    //Broadcast----------------------------------------------------------------------------------------------

    public void broadcast(String txBytes, final ValueCallback<BroadcastTxResponse> callback){
        request("broadcastTransaction", "{'transactionBytes': '" + txBytes + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                final BroadcastTxResponse account = gson.fromJson(value, BroadcastTxResponse.class);

                mHandler.post(new Runnable() {
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }

    //EncryptSecretPhrase----------------------------------------------------------------------------------------------

    ValueCallback<EncryptSecretPhraseResult> encryptSecretPhraseCallback;
    public void encryptSecretPhrase(String secretPhrase, String pin, String password, ValueCallback<EncryptSecretPhraseResult> callback) {
        encryptSecretPhraseCallback = callback;

        mWebView.loadUrl("javascript:MyInterface.encryptSecretPhraseResult(AndroidExtensions.encryptSecretPhrase('" + secretPhrase + "', '" + password + pin + "'));");
    }

    @JavascriptInterface
    public void encryptSecretPhraseResult(final String result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                EncryptSecretPhraseResult result1 = null;

                try {
                    JSONObject obj = new JSONObject(result);
                    result1 = new EncryptSecretPhraseResult();
                    result1.cipher = obj.getString("cipher");
                    result1.checksum = obj.getString("checksum");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                encryptSecretPhraseCallback.onReceiveValue(result1);
            }
        });
    }

    public class EncryptSecretPhraseResult{
        public String cipher;
        public String checksum;
    }
}
