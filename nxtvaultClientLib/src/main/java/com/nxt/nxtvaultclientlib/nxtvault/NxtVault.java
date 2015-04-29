package com.nxt.nxtvaultclientlib.nxtvault;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Brandon on 4/20/2015.
 */
public class NxtVault {
    public static final int REQUEST_REQUESTACCOUNT = 1;
    public static final int REQUEST_SIGNANDBROADCAST = 2;
    public static final int REQUEST_SIGN = 3;

    //Requests NxtVault to return to you a valid account. The user must select the account
    //to give access
    public void requestAccount(Activity activity){
        Intent intent = new Intent("nxtvault.intent.action.REQUESTACCOUNT");

        activity.startActivityForResult(intent, REQUEST_REQUESTACCOUNT);
    }

    //Will both sign and broadcast the tx using NxtVault
    public void signAndBroadcastTx(Activity activity, String accessToken, String jayTx){
        Intent intent = new Intent("nxtvault.intent.action.SIGNANDBROADCAST");

        intent.putExtra("AccessToken", accessToken);
        intent.putExtra("TransactionData", jayTx);

        activity.startActivityForResult(intent, REQUEST_SIGNANDBROADCAST);
    }

    //Will only sign the TX and return the signed bytes back to the calling application
    public void signTx(Activity activity, String accessToken, String jayTx){
        Intent intent = new Intent("nxtvault.intent.action.SIGNANDBROADCAST");

        intent.putExtra("AccessToken", accessToken);
        intent.putExtra("TransactionData", jayTx);

        activity.startActivityForResult(intent, REQUEST_SIGN);
    }
}
