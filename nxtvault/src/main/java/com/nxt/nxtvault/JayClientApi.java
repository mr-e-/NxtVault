package com.nxt.nxtvault;

import android.content.Context;
import android.net.Uri;
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

    public void generateSecretPhrase(final ValueCallback<String> callback){
        mWebView.evaluateJavascript("generateSecretPhrase()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            callback.onReceiveValue(gson.fromJson(value, String.class));
            }
        });
    }

    public void getNewAccount(String secretPhrase, String pin, final ValueCallback<AccountData> callback){
        mWebView.evaluateJavascript("newAccount('" + secretPhrase + "', '" + pin + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            callback.onReceiveValue(gson.fromJson(value, AccountData.class));
            }
        });
    }

    public void storeAccount(AccountData accountData){
       String result = gson.toJson(accountData)
               .replace("\"", "'")
               //gson wants to format = ?
               .replace("\\u003d", "=");

        mWebView.evaluateJavascript("AndroidExtensions.storeAccount(" + result + ");", null);
    }

    public void changePin(String mOldPin, String s, final ValueCallback<String> callback) {
        mWebView.evaluateJavascript("AndroidExtensions.changePin('" + mOldPin + "', '" + s + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(value);
            }
        });
    }

    public void loadAccounts(final ValueCallback<ArrayList<AccountData>> callback){
        mWebView.evaluateJavascript("(function() { return JSON.parse(localStorage['accounts']); })();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                ArrayList<AccountData> accounts = gson.fromJson(value, new TypeToken<ArrayList<AccountData>>() {}.getType());

                callback.onReceiveValue(accounts);
            }
        });
    }

    public void decryptSecretPhrase(AccountData accountData, final ValueCallback<String> callback){
        mWebView.evaluateJavascript("decryptSecretPhrase('" + accountData.cipher + "', '" + accountData.key + "', '" + accountData.checksum + "');" , new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(gson.fromJson(value, String.class));
            }
        });
    }

    public void extractTxDetails(AccountData accountData, final String txData, final ValueCallback<String> callback){
        mWebView.evaluateJavascript("AndroidExtensions.extractBytesData('" + accountData.accountRS + "', '" + txData + "');", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            callback.onReceiveValue(value);
            }
        });
    }

    public void sign(final AccountData accountData, final String txData, final ValueCallback<String> callback) {
        //get the account secret phrase
        decryptSecretPhrase(accountData, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String secretPhrase) {
                mWebView.evaluateJavascript("AndroidExtensions.signTrfBytes('" + accountData.accountRS + "', '" + txData + "', '" + secretPhrase + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String signedBytes) {
                        signedBytes = gson.fromJson(signedBytes, String.class);

                        callback.onReceiveValue(signedBytes);
                    }
                });
            }
        });
    }

    public void deleteAccount(AccountData accountData) {
        mWebView.evaluateJavascript("AndroidExtensions.deleteAccount('" + accountData.accountRS + "')", null);
    }

    public void broadcast(String txBytes, final ValueCallback<BroadcastTxResponse> callback){
        request("broadcastTransaction", "{'transactionBytes': '" + txBytes + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                BroadcastTxResponse account = gson.fromJson(value, BroadcastTxResponse.class);

                callback.onReceiveValue(account);
            }
        }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(null);
            }
        });
    }

    String EscapeJavaScriptFunctionParameter(String param) {
        char[] chars = param.toCharArray();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(chars[i]);
                    break;
            }
        }

        return sb.toString();
    }


}
