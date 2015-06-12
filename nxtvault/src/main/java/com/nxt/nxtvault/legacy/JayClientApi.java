package com.nxt.nxtvault.legacy;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvaultclientlib.jay.JayApi;

import java.util.ArrayList;

/**
 * Created by Brandon on 4/6/2015.
 */
public class JayClientApi extends JayApi {
    public JayClientApi(Context context) {
        super(context, Uri.parse("file:///android_asset/jay/index.html"));
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

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                loadAccountsCallback.onReceiveValue(accounts);
            }
        });
    }

    public void deleteAllAccounts() {
        mWebView.loadUrl("javascript: localStorage.clear();");
    }

    ////VERIFY PIN
    ValueCallback<Boolean> verifyPinCallback;
    public void verifyPin(String pin, final ValueCallback<Boolean> callback) {
        verifyPinCallback = callback;

        pin = pin.replace("\\", "\\\\").replace("'", "\\'");

        mWebView.loadUrl("javascript:MyInterface.verifyPinResult(AndroidExtensions.verifyPin('" + pin + "'));");
    }

    @JavascriptInterface
    public void verifyPinResult(final String result) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                verifyPinCallback.onReceiveValue(gson.fromJson(result, Boolean.class));
            }
        });
    }
}
