package com.nxt.nxtvault;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.TextView;

import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.framework.PasswordManager;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.framework.TransactionFactory;
import com.nxt.nxtvault.preference.PreferenceManager;
import com.nxt.nxtvault.security.pin.IPinEnteredListener;
import com.nxt.nxtvault.security.pin.PinEntryView;
import com.nxt.nxtvault.upgrade.UpgradeRunner;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;

import javax.inject.Inject;

/**
 * Created by bcollins on 2015-04-17.
 */
public abstract class BaseActivity extends ActionBarActivity {
    protected App mApplication;

    @Inject
    PreferenceManager mPreferences;

    @Inject
    JayClientApi mJay;

    @Inject
    PinManager mPinManager;

    @Inject
    AccountManager mAccountManager;

    @Inject
    PasswordManager mPasswordManager;

    @Inject
    TransactionFactory mTransactionFactory;

    @Inject
    UpgradeRunner mUpgradeRunner;

    private PinMode mCurrentPinMode;
    PinFragment pinFragment;
    protected boolean mPinShowing;

    public Typeface segoe;
    public Typeface segoeb;
    public Typeface segoel;

    boolean mIsUpgrading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = (App)getApplication();

        mApplication.inject(this);

        segoe = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        segoeb = Typeface.createFromAsset(getAssets(), "fonts/segoeuib.ttf");
        segoel = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");

        if (!mJay.getIsReady()){
            if (mUpgradeRunner.requiresUpgrade()){
                mIsUpgrading = true;
            }

            mJay.addReadyListener(new IJavascriptLoadedListener() {
                @Override
                public void onLoaded() {
                    mUpgradeRunner.run(new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean value) {
                            mIsUpgrading = false;
                            jayLoaded();
                            resume();
                        }
                    });
                }
            });
        }
        else{
            jayLoaded();
        }
    }

    abstract protected void jayLoaded();

    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsUpgrading) {
            resume();
        }
    }

    private void resume() {
        boolean pinIsSet = mPreferences.getPinIsSet() || mPreferences.getSharedPref().getBoolean(getString(R.string.pin2upgraderequired), false); //required for upgrade from version 10 to 11
        int pinTimeout = Integer.parseInt(mPreferences.getPinTimeout()) * 60 * 1000;

        long time = System.currentTimeMillis() - mPreferences.getLastPinEntry();

        if ((time > pinTimeout || mPinManager.getSessionPin() == null) && !mPinShowing) {
            if (!pinIsSet) {
                mCurrentPinMode = PinMode.Initialize;
            } else
                mCurrentPinMode = PinMode.Enter;

            showPin();
        } else if (!mPinShowing) {
            pinAccepted();
        }
    }

    public void changePin(){
        mCurrentPinMode = PinMode.Change;

        showPin();
    }

    protected void showPin(){
        mPinShowing = true;

        //load the pin fragment
        pinFragment = new PinFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.pin, pinFragment, "pin").commit();

        findViewById(R.id.pin).setVisibility(View.VISIBLE);

        //hide the action bar so the pin takes up full screen
        getSupportActionBar().hide();
    }

    void pinAccepted(){
        Fragment f = getSupportFragmentManager().findFragmentByTag("pin");
        if (f != null) {
            getSupportFragmentManager().beginTransaction().remove(f).commit();
        }

        getSupportActionBar().show();

        mPinShowing = false;
    }

    public static class PinFragment extends Fragment{
        private String mFirstPinEntry, mOldPin;
        PinEntryView pinEntryView;
        BaseActivity mActivity;
        TextView headerText;

        long lockoutTime;
        long numPinTries;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mActivity = (BaseActivity)getActivity();

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pin_entry, null);

            //Set up the pin entry view
            pinEntryView = (PinEntryView)view.findViewById(R.id.pin_entry_view);
            pinEntryView.setOnPinEnteredListener(new IPinEnteredListener() {
                @Override
                public void pinEntered(final String pin) {
                    //delay continuation to give the illusion of accepting the last entered number
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            verifyPin(pin, new ValueCallback<Boolean>() {
                                @Override
                                public void onReceiveValue(Boolean value) {
                                    if (value){
                                        ((BaseActivity)getActivity()).mPreferences.putLastPinEntry(System.currentTimeMillis());
                                        ((BaseActivity)getActivity()).mPreferences.putPinTryLockoutTime(0);
                                        ((BaseActivity)getActivity()).mPreferences.putPinTryAttempts(0);

                                        mActivity.pinAccepted();
                                    }
                                    else{
                                        pinEntryView.clearText();
                                    }
                                }
                            });
                        }
                    }, 500);
                };
            });

            numPinTries = ((BaseActivity)getActivity()).mPreferences.getPinTryAttempts();
            lockoutTime = ((BaseActivity)getActivity()).mPreferences.getPinTryLockoutTime();

            //determine what instructions to give the user.
            //we are either entering pin for the first time (requires second confirmation entry)
            //or we are changing the pin (requires second confirmation entry)
            //or we are just entering existing pin to log in
            headerText = (TextView)view.findViewById(R.id.pin_instructions);
            if (mActivity.mCurrentPinMode == PinMode.Initialize){
                headerText.setText("Enter a PIN number");
            }
            else if (mActivity.mCurrentPinMode == PinMode.Enter){
                headerText.setText("Enter your PIN number");
            }
            else if (mActivity.mCurrentPinMode == PinMode.Change){
                headerText.setText("Enter your current PIN number");
            }

            if (!canEnterPin()){
                setPinLockoutMessage();
                pinEntryView.setEnabled(false);
            }

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();

            pinEntryView.setFocus();
        }

        @Override
        public void onPause() {
            super.onPause();

            pinEntryView.clearKeyBoard();
        }

        private void verifyPin(final String s, final ValueCallback<Boolean> callback) {
            boolean accept = false;

            if (mActivity.mCurrentPinMode == PinMode.Initialize) {
                initializeNewPin(s, callback);
            } else if (mActivity.mCurrentPinMode == PinMode.Enter) {
                if (canEnterPin()) {

                    //*UPGRADEPIN*upgrade pin from version 10 to 11. had to be done here because we need to wait until right after the pin is entered
                    //this is set to true in the pin2upgrade runner
                    boolean upgradeRequired = mActivity.mPreferences.getSharedPref().getBoolean(mActivity.getString(R.string.pin2upgraderequired), false);

                    if (upgradeRequired){
                        final com.nxt.nxtvault.legacy.JayClientApi legacyClient = new com.nxt.nxtvault.legacy.JayClientApi(getActivity());
                        legacyClient.addReadyListener(new IJavascriptLoadedListener() {
                            @Override
                            public void onLoaded() {
                                legacyClient.verifyPin(s, new ValueCallback<Boolean>() {
                                    @Override
                                    public void onReceiveValue(Boolean value) {
                                        if (!value) {
                                            headerText.setText("Please try again");
                                            handleIncorrectPin();
                                        } else {
                                            //*UPGRADEPIN*this has to happen first so that the pin manager receives pinNotSet and allows the setting of a new pin. I hate this code with a passion.
                                            mActivity.mPreferences.getSharedPref().edit().putBoolean(mActivity.getString(R.string.pin2upgraderequired), false).commit();

                                            mActivity.mPinManager.changePin(s);

                                            legacyClient.deletePin();
                                        }

                                        doCallback(callback, value);
                                    }
                                });
                            }
                        });
                    }
                    else {
                        boolean signIn = mActivity.mPinManager.signIn(s);
                        if (!signIn) {
                            headerText.setText("Please try again");
                            handleIncorrectPin();
                        }

                        doCallback(callback, signIn);
                    }
                } else {
                    setPinLockoutMessage();
                    pinEntryView.setEnabled(false);

                    doCallback(callback, false);
                }
            } else if (mActivity.mCurrentPinMode == PinMode.Change) {
                //Enter in the old pin number first
                if (mOldPin == null) {
                    boolean signIn = mActivity.mPinManager.signIn(s);
                    if (signIn){
                        mOldPin = s;

                        headerText.setText("Enter your new PIN number");
                    }
                    else{
                        headerText.setText("Incorrect, enter your current PIN");
                    }

                    doCallback(callback, false);
                }
                else if (mFirstPinEntry == null) {
                    mFirstPinEntry = s;
                    headerText.setText("Confirm your PIN Number");
                    doCallback(callback, accept);
                } else {
                    //confirm the new pin numbers match
                    if (mFirstPinEntry.equals(s)) {
                        //reencrypt the accounts that are currently encrypted with the old pin
                        mActivity.pinChanged(mOldPin, s);
                        accept = true;
                    } else {
                        pinEntryView.clearText();
                        mFirstPinEntry = null;
                        headerText.setText("PIN mismatch. Try again.");
                    }

                    doCallback(callback, accept);
                }
            }
        }

        private void initializeNewPin(String s, final ValueCallback<Boolean> callback) {
            //check if entering first time, request second to confirm
            if (mFirstPinEntry == null) {
                mFirstPinEntry = s;

                headerText.setText("Confirm your PIN Number");
                doCallback(callback, false);
            } else {
                doCallback(callback, storePin(s));
            }
        }

        private void doCallback(ValueCallback<Boolean> callback, boolean accept) {
            if (callback != null){
                callback.onReceiveValue(accept);
            }
        }

        private void setPinLockoutMessage() {
            headerText.setText("PIN Lockout. Please try again in " + getRemainingLockoutTime());
        }

        private boolean canEnterPin() {
            return System.currentTimeMillis() > lockoutTime;
        }

        private void handleIncorrectPin() {
            numPinTries++;

            if (numPinTries == 3){
                lockoutTime = System.currentTimeMillis() + (60*60*1000);
            }
            if (numPinTries == 4){
                lockoutTime = System.currentTimeMillis() + (24*60*60*1000);
            }
            if (numPinTries == 5){
                mActivity.wipeDevice();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                }, 2000);
            }

            ((BaseActivity)mActivity).mPreferences.putPinTryAttempts(numPinTries);
            ((BaseActivity)mActivity).mPreferences.putPinTryLockoutTime(lockoutTime);

            String message = getPinLockoutString();

            if (message != null) {
                headerText.setText(message);
                pinEntryView.setEnabled(false);
                pinEntryView.clearText();
            }
        }

        public String getPinLockoutString() {
            if (numPinTries == 3){
                return "Three bad attempts. Device locked for one hour.";
            }
            if (numPinTries == 4){
                return "Four bad attempts. Device locked for 24 hours.";
            }
            if (numPinTries == 5){
                return "Five bad attempts. Wiping all data...";
            }

            return null;
        }

        public String getRemainingLockoutTime(){
            Long remaining = lockoutTime - System.currentTimeMillis();

            if (remaining > (1000 * 60 * 60)){
                return (remaining / 1000 / 60 / 60) + " hours";
            }
            else if (remaining > 1000 * 60){
                return  (remaining / 1000 / 60) + " minutes";
            }
            else if (remaining > 1000){
                return (remaining / 1000) + " seconds";
            }

            return null;
        }

        private boolean storePin(String pin) {
            //Store the pin for good
            if (mFirstPinEntry.equals(pin)){
                mActivity.mPinManager.changePin(pin);

                return true;
            }
            else{
                pinEntryView.clearText();
                mFirstPinEntry = null;
                headerText.setText("PIN mismatch. Try again.");

                return false;
            }
        }
    }

    protected abstract void wipeDevice();

    protected void pinChanged(String mOldPin, String s){}
}
