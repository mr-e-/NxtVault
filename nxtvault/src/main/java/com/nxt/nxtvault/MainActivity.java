package com.nxt.nxtvault;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.AccountInfo;
import com.nxt.nxtvault.screen.AboutFragment;
import com.nxt.nxtvault.screen.AccountFragment;
import com.nxt.nxtvault.screen.PreferenceFragment;
import com.nxt.nxtvault.upgrade.IUpgradeTask;
import com.nxt.nxtvault.upgrade.UpgradePinTask;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;
import com.nxt.nxtvaultclientlib.jay.RequestMethods;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {
    Gson gson = new Gson();

    Bundle mSavedInstanceState;

    private TextView mTitleBar;

    private ArrayList<Asset> mAssetList;

    AccountManager mAccountManager;

    public AccountManager getAccountManager(){
        return mAccountManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSavedInstanceState = savedInstanceState;
        mAccountManager = new AccountManager(this, getJay(), mPinManager, mPreferences.getSharedPref());

        if (mPreferences.getSharedPref().getString("assets", null) != null) {
            mAssetList = gson.fromJson(mPreferences.getSharedPref().getString("assets", null), new TypeToken<ArrayList<Asset>>() { }.getType());
        }

        //set up action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);

        mTitleBar = ((TextView)getSupportActionBar().getCustomView().findViewById(R.id.createAccount));
        mTitleBar.setTypeface(segoe);
    }

    @Override
    protected void jayLoaded() {
        setServerInfo();

        setIsJayLoaded(true);

        if (!mPinShowing){
            pinAccepted();
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        mPreferences.getSharedPref().edit().putBoolean("fromOrient", true).apply();

        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    protected void onDestroy() {
        mPreferences.getSharedPref().edit().putBoolean("fromOrient", false).apply();

        super.onDestroy();
    }

    public void setServerInfo() {
        if (mPreferences.getCustomServer() != null && !mPreferences.getCustomServer().isEmpty()){
            getJay().setNode(mPreferences.getCustomServer());
            getJay().setRequestMethod(RequestMethods.Single);
            getJay().setIsTestnet(mPreferences.getIsTestNet());
        }
        else{
            getJay().setRequestMethod(RequestMethods.Fastest);
            getJay().setIsTestnet(false);
        }
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
    void pinAccepted() {
        super.pinAccepted();

        (findViewById(R.id.mainView)).setVisibility(View.VISIBLE);

        navigateStart();
    }

    @Override
    protected void wipeDevice() {
        mAccountManager.deleteAllAccount();

        mPreferences.wipe();
    }

    @Override
    protected void pinChanged(String mOldPin, final String s) {
        super.pinChanged(mOldPin, s);

        View progress = findViewById(R.id.progress);
        ObjectAnimator.ofFloat(progress, View.ALPHA, 0, 1).start();
        progress.setVisibility(View.VISIBLE);

        ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 1, 0).start();

        mAccountManager.changePin(s, mOldPin, new ValueCallback<Void>() {
            @Override
            public void onReceiveValue(Void value) {
                mPinManager.changePin(s);

                View progress = findViewById(R.id.progress);
                ObjectAnimator.ofFloat(progress, View.ALPHA, 1, 0).start();
                progress.setVisibility(View.GONE);

                ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 0, 1).start();
            }
        });
    }

    protected void navigateStart(){
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
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
            for (AccountData account : mAccountManager.getAllAccounts()){
                if (account.getIsSpendingPasswordEnabled()) {
                    Toast.makeText(this, "You must remove all account passwords before you can change your pin", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            changePin();

            return true;
        }
        else if (id == R.id.action_logout){
            logout();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mPreferences.putLastPinEntry(0L);
        System.exit(0);
    }
}
