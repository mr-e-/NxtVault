package com.nxt.nxtvaultclientlib.jay;

import android.webkit.ValueCallback;

import com.nxt.nxtvaultclientlib.nxtvault.model.Account;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 5/2/2015.
 */
public interface IJayApi {
    void addReadyListener(IJavascriptLoadedListener javascriptLoadedListener);

    boolean getIsReady();

    void setNode(String nodeAddress);

    void setIsTestnet(boolean isTestnet);

    void setRequestMethod(RequestMethods requestMethod);

    void request(String requestName, String jsonParams, ValueCallback<String> onSuccess, ValueCallback<String> onFailed);

    void getBestNodes(ValueCallback<List<String>> callback);

    void getAccount(String accountRS, ValueCallback<Account> callback);

    void getAsset(String assetId, ValueCallback<Asset> callback);

    void getAllAssets(ValueCallback<ArrayList<Asset>> callback);

    void sendMoney(String accountRs, double amount, String message, ValueCallback<String> callback);

    void transferAsset(String accountRs, Asset asset, float amount, String message, ValueCallback<String> callback);
}
