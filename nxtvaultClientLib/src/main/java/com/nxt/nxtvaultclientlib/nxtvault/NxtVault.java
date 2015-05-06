package com.nxt.nxtvaultclientlib.nxtvault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by Brandon on 4/20/2015.
 */
public class NxtVault {
    public static final int REQUEST_REQUESTACCOUNT = 1;
    public static final int REQUEST_SIGNANDBROADCAST = 2;
    public static final int REQUEST_SIGN = 3;
    public static final int REQUEST_PREFERRED_ACCOUNT = 4;

    private boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.size() > 0;
    }

    private boolean confirmAppInstalled(Context context, Intent intent) {
        if (isIntentAvailable(context, intent)){
            return true;
        }
        else{
            //TODO: pop up store app
            return false;
        }
    }

    //Requests NxtVault to return to you a valid account. The user must select the account
    //to give access
    public void requestAccount(Activity activity){
        Intent intent = new Intent("nxtvault.intent.action.REQUESTACCOUNT");
        if (confirmAppInstalled(activity, intent)) {
            activity.startActivityForResult(intent, REQUEST_REQUESTACCOUNT);
        }
    }

    //Will both sign and broadcast the tx using NxtVault
    public void signAndBroadcastTx(Activity activity, String accessToken, String jayTx){
        Intent intent = new Intent("nxtvault.intent.action.SIGNANDBROADCAST");

        if (confirmAppInstalled(activity, intent)) {
            intent.putExtra("AccessToken", accessToken);
            intent.putExtra("TransactionData", jayTx);

            activity.startActivityForResult(intent, REQUEST_SIGNANDBROADCAST);
        }
    }

    //Will only sign the TX and return the signed bytes back to the calling application
    public void signTx(Activity activity, String accessToken, String jayTx){
        Intent intent = new Intent("nxtvault.intent.action.SIGN");

        if (confirmAppInstalled(activity, intent)) {
            intent.putExtra("AccessToken", accessToken);
            intent.putExtra("TransactionData", jayTx);

            activity.startActivityForResult(intent, REQUEST_SIGN);
        }
    }

    public void requestPreferredServer(Activity activity){
        Intent intent = new Intent("nxtvault.intent.action.REQUESTPREFERREDSERVER");
        if (confirmAppInstalled(activity, intent)){
            activity.startActivityForResult(intent, REQUEST_PREFERRED_ACCOUNT);
        }
    }
}
