package com.nxt.nxtvault;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.BroadcastTxResponse;
import com.nxt.nxtvault.screen.AboutFragment;
import com.nxt.nxtvault.screen.AccountFragment;
import com.nxt.nxtvault.screen.PreferenceFragment;
import com.nxt.nxtvaultclientlib.jay.RequestMethods;


public class MainActivity extends BaseActivity {
    Bundle mSavedInstanceState;

    private TextView mTitleBar;

    public AccountManager getAccountManager(){
        return mAccountManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSavedInstanceState = savedInstanceState;

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
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        mPreferences.getSharedPref().edit().putBoolean("fromOrient", true).commit();

        return super.onRetainCustomNonConfigurationInstance();
    }

    @Override
    protected void onDestroy() {
        mPreferences.getSharedPref().edit().putBoolean("fromOrient", false).commit();

        super.onDestroy();
    }

    public void setServerInfo() {
        if (mPreferences.getCustomServer() != null && mPreferences.getCustomServer() != ""){
            mJay.setNode(mPreferences.getCustomServer());
            mJay.setRequestMethod(RequestMethods.Single);
            mJay.setIsTestnet(mPreferences.getIsTestNet());
        }
        else{
            mJay.setRequestMethod(RequestMethods.Fastest);
            mJay.setIsTestnet(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        progress.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= 14) {
            ObjectAnimator.ofFloat(progress, View.ALPHA, 0, 1).start();
            ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 1, 0).start();
        }

        mAccountManager.changePin(s, mOldPin, new ValueCallback<Void>() {
            @Override
            public void onReceiveValue(Void value) {
                mPinManager.changePin(s);

                View progress = findViewById(R.id.progress);
                progress.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= 14) {
                    ObjectAnimator.ofFloat(progress, View.ALPHA, 1, 0).start();
                    ObjectAnimator.ofFloat(findViewById(R.id.mainView), View.ALPHA, 0, 1).start();
                }
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
        if (mPinShowing){
            return true;
        }

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
        else if (id == R.id.action_scan_cold){
            scanCold();
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanCold() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    private void logout() {
        mPreferences.putLastPinEntry(0L);

        System.exit(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2){
            if (resultCode == Activity.RESULT_CANCELED){
                if (data != null)
                    Toast.makeText(this, data.getAction(), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "Transaction Cancelled!", Toast.LENGTH_LONG).show();
            }
            else if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Transaction Complete!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                String re = scanResult.getContents();

                if (re != null) {
                    Intent intent = mTransactionFactory.createSelfSignedTx("nxtvault.intent.action.BROADCAST", re);
                    intent.putExtra("SignedBytes", re);

                    startActivityForResult(intent, 2);
                }
            }
        }
    }
}
