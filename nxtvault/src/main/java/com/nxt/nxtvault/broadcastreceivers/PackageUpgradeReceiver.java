package com.nxt.nxtvault.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Brandon on 5/30/2015.
 */
public class PackageUpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String msg="intent:"+intent+" action:"+intent.getAction();
        Log.d("DEBUG", msg);
        Toast.makeText(context,msg, Toast.LENGTH_SHORT).show();
    }
}
