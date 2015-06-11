package com.nxt.nxtvault.legacy;

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
}
