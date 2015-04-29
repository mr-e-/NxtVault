package com.nxt.nxtvaultclientlib.jay;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
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
public class JayApi {
    protected WebView mWebView;
    protected Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private IJavascriptLoadedListener mLoadedListener;

    public JayApi(Context context, Uri path, IJavascriptLoadedListener listener){
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

    public void setNode(String nodeAddress){
        mWebView.evaluateJavascript("Jay.setNode('" + nodeAddress + "')", null);
    }

    public void setIsTestnet(boolean isTestnet){
        mWebView.evaluateJavascript("Jay.isTestnet=" + isTestnet, null);
    }

    public void setRequestMethod(RequestMethods requestMethod){
        mWebView.evaluateJavascript("Jay.setRequestMethod(" + requestMethod.ordinal() + ");", null);
    }

    //*********** Jay Request Functionality ********** Requires to pass in a callback so I've stubbed it out with custom js code in AndroidExtensions to call back to an @Javascript interface API
    //Please contact me if you know a better way to do this!

    ValueCallback<String> requestSuccess;
    ValueCallback<String> requestFailed;
    public void request(String requestName, String jsonParams, ValueCallback<String> onSuccess, ValueCallback<String> onFailed){
        requestSuccess = onSuccess;
        requestFailed = onFailed;

        mWebView.evaluateJavascript("Jay.request('" + requestName + "', " + jsonParams + ", AndroidExtensions.onRequestSuccess, AndroidExtensions.onRequestFailed);", null);
    }

    @JavascriptInterface
    public void onRequestSuccess(String result){
        requestSuccess.onReceiveValue(result);
    }

    @JavascriptInterface
    public void onRequestFailed(String result){
        requestFailed.onReceiveValue(result);
    }

    //**********End Jay Request Functionality *****************************************

    //same issue as above
    ValueCallback<List<String>> getBestNodesResult;
    public void getBestNodes(final ValueCallback<List<String>> callback){
        getBestNodesResult = callback;

        mWebView.evaluateJavascript("AndroidExtensions.getBestNodes();", null);
    }

    @JavascriptInterface
    public void getBestNodesResult(String result){
        ArrayList<String> nodes = gson.fromJson(result, new TypeToken<ArrayList<String>>() { }.getType());

        getBestNodesResult.onReceiveValue(nodes);
    }

    public void getAccount(String accountRS, final ValueCallback<Account> callback){
        request("getAccount", "{'account': '" + accountRS + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Account account = gson.fromJson(value, Account.class);

                callback.onReceiveValue(account);
            }
        }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(null);
            }
        });
    }

    public void getAsset(String assetId, final ValueCallback<Asset> callback){
        request("getAsset", "{'asset': '" + assetId + "'}", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Asset account = gson.fromJson(value, Asset.class);

                callback.onReceiveValue(account);
            }
        }, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.e("getAsset", value);
                callback.onReceiveValue(null);
            }
        });
    }

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

    public void sendMoney(String accountRs, float amount, String message, final ValueCallback<String> callback) {
        String messageJs = "";

        if (message != null){
            mWebView.evaluateJavascript("AndroidExtensions.messageAppendage = Jay.addAppendage(Jay.appendages.message, '" + message + "');", null);
            messageJs = ", AndroidExtensions.messageAppendage";
        }

        mWebView.evaluateJavascript("Jay.sendMoney('" + accountRs + "', " + amount + messageJs + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(gson.fromJson(value, String.class));
            }
        });
    }

    public void transferAsset(String accountRs, Asset asset, float amount, String message, final ValueCallback<String> callback) {
        String messageJs = "";

        long num = Math.round(amount*Math.pow(10, asset.Decimals));

        if (message != null){
            mWebView.evaluateJavascript("AndroidExtensions.messageAppendage = Jay.addAppendage(Jay.appendages.message, '" + message + "');", null);
            messageJs = ", AndroidExtensions.messageAppendage";
        }

        mWebView.evaluateJavascript("Jay.transferAsset('" + accountRs + "', '" + asset.AssetId + "', " + num + messageJs + ");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                callback.onReceiveValue(gson.fromJson(value, String.class));
            }
        });
    }
}
