package com.nxt.nxtvault;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.model.AccountInfo;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.screen.AboutFragment;
import com.nxt.nxtvault.screen.AccountFragment;
import com.nxt.nxtvault.screen.PreferenceFragment;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;
import com.nxt.nxtvaultclientlib.jay.RequestMethods;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {
    private static JayClientApi jay;
    Gson gson = new Gson();

    private static AccountInfo mAccountInfo;

    Bundle mSavedInstanceState;

    private TextView mTitleBar;

    public AccountInfo getAccountInfo(){
        return mAccountInfo;
    }

    public JayClientApi getJay(){
        return jay;
    }

    private ArrayList<Asset> mAssetList;

    public boolean getIsTestNet(){
        return sharedPref.getBoolean(getString(R.string.testnet_preference), false);
    }

    public String getCustomServer(){
        return sharedPref.getString(getString(R.string.server_preference), null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSavedInstanceState = savedInstanceState;

        if (sharedPref.getString("assets", null) != null) {
            mAssetList = gson.fromJson(sharedPref.getString("assets", null), new TypeToken<ArrayList<Asset>>() { }.getType());
        }

        if (savedInstanceState != null){
            mAccountInfo = (AccountInfo)savedInstanceState.getSerializable("mAccountInfo");
        }
        else{
            jay = new JayClientApi(this, new IJavascriptLoadedListener() {
                @Override
                public void onLoaded() {
                    setServerInfo();

                    refreshAccounts(null);
                }
            });
        }

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);

        mTitleBar = ((TextView)getSupportActionBar().getCustomView().findViewById(R.id.createAccount));
        mTitleBar.setTypeface(segoe);
    }

    public void setServerInfo() {
        if (getCustomServer() != null){
            getJay().setNode(getCustomServer());
            getJay().setRequestMethod(RequestMethods.Single);
        }
        else{
            getJay().setRequestMethod(RequestMethods.Fastest);
        }

        getJay().setIsTestnet(getIsTestNet());
    }

    private void refreshAccounts(final ValueCallback<String> completedCallback) {
        loadAccounts(new ValueCallback<ArrayList<AccountData>>() {
            @Override
            public void onReceiveValue(ArrayList<AccountData> value) {
                mAccountInfo = new AccountInfo(value);

                if (!mPinShowing)
                    pinAccepted();

                if (completedCallback != null){
                    completedCallback.onReceiveValue(null);
                }
            }
        });
    }

    public Asset findAssetById(String assetId){
        Asset result = null;

        if (mAssetList != null) {
            for (Asset asset : mAssetList) {
                if (asset.AssetId.equals(assetId)) {
                    result = asset;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSavedInstanceState != null){
            if (!mPinShowing)
                pinAccepted();
        }
    }

    @Override
    protected void showPin() {
        super.showPin();

        findViewById(R.id.mainView).setVisibility(View.GONE);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitleBar.setText(title);

        super.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mAccountInfo", mAccountInfo);
    }

    private void loadAccounts( ValueCallback<ArrayList<AccountData>> callback) {
        getJay().loadAccounts(callback);
    }

    public void addNewAccountToUI(AccountData accountData) {
        mAccountInfo.getAccountData().add(accountData);
    }

    @Override
    void pinAccepted() {
        super.pinAccepted();

        (findViewById(R.id.mainView)).setVisibility(View.VISIBLE);

        navigateStart();
    }

    @Override
    protected void pinChanged(String mOldPin, final String s) {
        super.pinChanged(mOldPin, s);

        View progress = findViewById(R.id.progress);
        ObjectAnimator.ofFloat(progress, View.ALPHA, 0, 1).start();
        progress.setVisibility(View.VISIBLE);

        ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 1, 0).start();

        getJay().changePin(mOldPin, s, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //refresh the local account list cash to receive the new cyphers
                refreshAccounts(new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //Store the changed pin
                        storePin(s);

                        View progress = findViewById(R.id.progress);
                        ObjectAnimator.ofFloat(progress, View.ALPHA, 1, 0).start();
                        progress.setVisibility(View.GONE);

                        ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 0, 1).start();
                    }
                });
            }
        });
    }

    protected void navigateStart(){
        if (mSavedInstanceState == null){
            navigate(new AccountFragment(), false);
        }
    }

    public void navigate(Fragment instance, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (addToBackStack)
            transaction.addToBackStack(null);

        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        transaction.replace(R.id.mainFragment, instance).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            navigate(new PreferenceFragment(), true);

            setTitle(getString(R.string.settings));
            return true;
        }
        else if (id == android.R.id.home) {
            onBackPressed();
        }
        else if(id == R.id.action_about){
            navigate(new AboutFragment(), true);
            setTitle(getString(R.string.about));

            return true;
        }
        else if (id == R.id.action_change_pin){
            changePin();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteAccount(AccountData accountData) {
        getAccountInfo().deleteAccount(accountData);
    }
}
