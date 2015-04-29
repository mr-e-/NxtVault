package com.nxt.testwallet;

import android.content.Context;
import android.net.Uri;

import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;
import com.nxt.nxtvaultclientlib.jay.JayApi;

/**
 * Created by Brandon on 4/6/2015.
 */
public class JayClientApi extends JayApi {
    public JayClientApi(Context context, boolean isTestNet, IJavascriptLoadedListener listener) {
        super(context, Uri.parse("file:///android_asset/jayClient/request.html"), listener);
    }
}
