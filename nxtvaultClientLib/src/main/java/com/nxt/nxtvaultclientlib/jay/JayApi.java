package com.nxt.nxtvaultclientlib.jay;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvaultclientlib.nxtvault.model.Account;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/6/2015.
 */
public class JayApi implements IJayApi {
    protected Handler mHandler = new Handler();

    protected WebView mWebView;
    protected Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private IJavascriptLoadedListener mLoadedListener;

    protected Context mContext;

    public JayApi(Context context, Uri path, IJavascriptLoadedListener listener){
        mContext = context;

        mLoadedListener = listener;
        gson = new Gson();

        //Load the the webview context for processing jay javascript
        mWebView = new WebView(context);

        //enable WebView debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (mLoadedListener != null) {
                    mLoadedListener.onLoaded();
                }
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabasePath(context.getDir("databases", Context.MODE_PRIVATE).getPath());

        mWebView.addJavascriptInterface(this, "MyInterface");

        mWebView.loadUrl(path.toString());
    }

    @Override
    public void setNode(String nodeAddress){
        mWebView.loadUrl("javascript:Jay.setNode('" + nodeAddress + "')", null);
    }

    @Override
    public void setIsTestnet(boolean isTestnet){
        mWebView.loadUrl("javascript:Jay.isTestnet=" + isTestnet, null);
    }

    @Override
    public void setRequestMethod(RequestMethods requestMethod){
        mWebView.loadUrl("javascript:Jay.setRequestMethod(" + requestMethod.ordinal() + ");", null);
    }

    //*********** Jay Request Functionality ********** Requires to pass in a callback so I've stubbed it out with custom js code in AndroidExtensions to call back to an @Javascript interface API
    //Please contact me if you know a better way to do this!

    ValueCallback<String> requestSuccess;
    ValueCallback<String> requestFailed;
    String json;
    @Override
    public void request(String requestName, String jsonParams, ValueCallback<String> onSuccess, ValueCallback<String> onFailed){
        requestSuccess = onSuccess;
        requestFailed = onFailed;
        json = jsonParams;

        mWebView.loadUrl("javascript:Jay.request('" + requestName + "', " + jsonParams + ", AndroidExtensions.onRequestSuccess, AndroidExtensions.onRequestFailed);", null);
    }

    @JavascriptInterface
    public void onRequestSuccess(String result){
        requestSuccess.onReceiveValue(result);
    }

    @JavascriptInterface
    public void onRequestFailed(String result){
        Log.e("jay request failed", result);
        requestFailed.onReceiveValue(json);
    }

    //**********End Jay Request Functionality *****************************************

    //same issue as above
    ValueCallback<List<String>> getBestNodesCallback;
    @Override
    public void getBestNodes(final ValueCallback<List<String>> callback){
        getBestNodesCallback = callback;

        mWebView.loadUrl("javascript:AndroidExtensions.getBestNodes();", null);
    }

    @JavascriptInterface
    public void getBestNodesResult(String result){
        final ArrayList<String> nodes = gson.fromJson(result, new TypeToken<ArrayList<String>>() { }.getType());

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getBestNodesCallback.onReceiveValue(nodes);
            }
        });
    }

    @Override
    public void getAccount(String accountRS, final ValueCallback<Account> callback){
        request("getAccount", "{'account': '" + accountRS + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                final Account account = gson.fromJson(value, Account.class);

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
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }

    @Override
    public void getAsset(String assetId, final ValueCallback<Asset> callback){
        request("getAsset", "{'asset': '" + assetId + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                final Asset account = gson.fromJson(value, Asset.class);

                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(account);
                    }
                });
            }
        }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e("getAsset", value);

                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onReceiveValue(null);
                    }
                });
            }
        });
    }

    @Override
    public void getAllAssets(final ValueCallback<ArrayList<Asset>> callback) {
        request("getAllAssets", "{}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String result) {
                ArrayList<Asset> allAssets = null;

                try {
                    JsonParser parser = new JsonParser();
                    String assets = parser.parse(result).getAsJsonObject().get("assets").toString();

                    allAssets = gson.fromJson(assets, new TypeToken<ArrayList<Asset>>() { }.getType());
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    Log.e("getAllAssets", e.getMessage());
                }

                callback.onReceiveValue(allAssets);
            }
        }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e("getAllAssets", value);
                callback.onReceiveValue(null);
            }
        });
    }

    ValueCallback<String> sendMoneyCallback;
    @Override
    public void sendMoney(String accountRs, double amount, String message, final ValueCallback<String> callback) {
        sendMoneyCallback = callback;

        String messageJs = getMessage(message);

        mWebView.loadUrl("javascript:MyInterface.sendMoneyResult(Jay.sendMoney('" + accountRs + "', " + amount + messageJs + "));");
    }

    @JavascriptInterface
    public void sendMoneyResult(final String result) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendMoneyCallback.onReceiveValue(gson.fromJson(result, String.class));
            }
        });
    }

    ValueCallback<String> transferAssetCallback;
    @Override
    public void transferAsset(String accountRs, Asset asset, float amount, String message, final ValueCallback<String> callback) {
        transferAssetCallback = callback;

        long num = Math.round(amount*Math.pow(10, asset.Decimals));

        String messageJs = getMessage(message);

        mWebView.loadUrl("MyInterface.transferAssetResult(Jay.transferAsset('" + accountRs + "', '" + asset.AssetId + "', " + num + messageJs + "));");
    }

    @JavascriptInterface
    public void transferAssetResult(final String result) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                transferAssetCallback.onReceiveValue(gson.fromJson(result, String.class));
            }
        });
    }

    private String getMessage(String message) {
        String messageJs = "";

        if (message != null && !message.isEmpty()){
            mWebView.loadUrl("javascript:AndroidExtensions.messageAppendage = Jay.addAppendage(Jay.appendages.message, '" + message + "');");
            messageJs = ", AndroidExtensions.messageAppendage";
        }
        return messageJs;
    }
}
