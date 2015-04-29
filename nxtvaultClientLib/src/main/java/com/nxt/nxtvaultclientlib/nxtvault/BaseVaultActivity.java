package com.nxt.nxtvaultclientlib.nxtvault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.gson.Gson;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;
import com.nxt.nxtvaultclientlib.jay.JayApi;
import com.nxt.nxtvaultclientlib.nxtvault.model.AccountSelectionResult;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Brandon on 4/20/2015.
 */
public abstract class BaseVaultActivity extends ActionBarActivity {
    private static JayApi jay;
    ArrayList<IJavascriptLoadedListener> mJayLoadedListeners;
    protected boolean mIsJayLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJayLoadedListeners = new ArrayList<>();
    }

    protected void initializeJay(Uri url){
        jay = new JayApi(this, url, new IJavascriptLoadedListener() {
            @Override
            public void onLoaded() {
                //Fired when the webview finished loading. You cannot make any Jay requests
                //before this event is fired
                jayLoaded();
            }
        });
    }

    protected void jayLoaded(){
        mIsJayLoaded = true;

        for(IJavascriptLoadedListener listener : mJayLoadedListeners){
            listener.onLoaded();
        }
    }

    boolean getIsTestNet(){return true;}

    public JayApi getJay(){
        return jay;
    }

    public void addListener(IJavascriptLoadedListener loadedListener){
        mJayLoadedListeners.add(loadedListener);

        if (mIsJayLoaded){
            loadedListener.onLoaded();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Gson gson = new Gson();

        if (resultCode == RESULT_OK) {
            if (requestCode == NxtVault.REQUEST_REQUESTACCOUNT) {
                String returnData = data.getAction();

                AccountSelectionResult result = gson.fromJson(returnData, AccountSelectionResult.class);

                onAccountRequestResult(true, result);
            }
            else if (requestCode == NxtVault.REQUEST_SIGNANDBROADCAST){
                onSignAndBroadcast(true, null);
            }
            else if (requestCode == NxtVault.REQUEST_SIGN){
                byte[] bytes = data.getAction().getBytes(Charset.forName("UTF-8"));

                onSign(true, null, bytes);
            }
        }
        else if (resultCode == RESULT_CANCELED){
            if (requestCode == NxtVault.REQUEST_REQUESTACCOUNT) {
                onAccountRequestResult(false, null);
            }
            else if (requestCode == NxtVault.REQUEST_SIGNANDBROADCAST){
                onSignAndBroadcast(false, data == null ? "Transaction Cancelled" : data.getAction());
            }
            else if (requestCode == NxtVault.REQUEST_SIGN){
                onSign(false, data.getAction(), null);
            }
        }
    }

    protected void onAccountRequestResult(boolean success, AccountSelectionResult accountSelectionResult){}
    protected void onSignAndBroadcast(boolean success, String message){}
    protected void onSign(boolean success, String message, byte[] signedBytes){}
}
