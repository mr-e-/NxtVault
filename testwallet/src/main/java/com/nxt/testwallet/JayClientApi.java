package com.nxt.testwallet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.ValueCallback;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvaultclientlib.jay.JayApi;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;
import com.nxt.testwallet.file.FileReader;

import java.util.ArrayList;

/**
 *  on 4/6/2015.
 */
public class JayClientApi extends JayApi {
    public JayClientApi(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    //temporary override while we fix a bug with some nodes requiring admin privs
    public void getAllAssets(ValueCallback<ArrayList<Asset>> callback) {
        String assetsJson = new FileReader().loadAssets(mContext);
        ArrayList<Asset> allAssets = new ArrayList<>();

        try {
            JsonParser parser = new JsonParser();
            String assets = parser.parse(assetsJson).getAsJsonObject().get("assets").toString();

            allAssets = gson.fromJson(assets, new TypeToken<ArrayList<Asset>>() { }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            Log.e("getAllAssets", e.getMessage());
        }

        callback.onReceiveValue(allAssets);
    }
}
