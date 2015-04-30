package com.nxt.nxtvault;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.nxt.nxtvault.preference.PreferenceManager;
import com.nxt.nxtvaultclientlib.nxtvault.model.PreferredServerResult;

public class ApiActivity extends ActionBarActivity {

    PreferenceManager mPref;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        mPref = new PreferenceManager(this);

        final Intent intent = getIntent();

        if (intent.getAction().equals("nxtvault.intent.action.REQUESTPREFERREDSERVER")) {
            returnPreferredServer();
        }
        else {
            setResult(RESULT_CANCELED, null);
            finish();
        }
    }

    private void returnPreferredServer() {
        PreferredServerResult preferredServerResult = new PreferredServerResult();
        preferredServerResult.IsTestNet = mPref.getIsTestNet();
        preferredServerResult.PreferredServer = mPref.getCustomServer();

        setResult(RESULT_OK, new Intent(gson.toJson(preferredServerResult)));
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_api, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
