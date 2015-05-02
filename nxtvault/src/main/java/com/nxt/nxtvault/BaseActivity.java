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
import android.widget.TextView;

import com.nxt.nxtvault.preference.PreferenceManager;
import com.nxt.nxtvault.security.pin.IPinEnteredListener;
import com.nxt.nxtvault.security.pin.PinEntryView;

/**
 * Created by bcollins on 2015-04-17.
 */
public abstract class BaseActivity extends ActionBarActivity {
    public PreferenceManager mPreferences;

    private PinMode mCurrentPinMode;
    PinFragment pinFragment;
    protected boolean mPinShowing;

    public Typeface segoe;
    public Typeface segoeb;
    public Typeface segoel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        segoe = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        segoeb = Typeface.createFromAsset(getAssets(), "fonts/segoeuib.ttf");
        segoel = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");

        mPreferences = new PreferenceManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String pin = mPreferences.getPin();
        int pinTimeout = Integer.parseInt(mPreferences.getPinTimeout()) * 60 * 1000;

        long time = System.currentTimeMillis() - mPreferences.getLastPinEntry();

        if (time > pinTimeout && !mPinShowing){
            if (pin == null || pin.isEmpty()){
                mCurrentPinMode = PinMode.Initialize;
            }
            else
                mCurrentPinMode = PinMode.Enter;

            showPin();
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

        transaction.add(R.id.pin, pinFragment).commit();

        findViewById(R.id.pin).setVisibility(View.VISIBLE);

        //hide the action bar so the pin takes up full screen
        getSupportActionBar().hide();
    }

    void pinAccepted(){
        //pin has been accepted so we'll hide the pin screen and log the last entry time
        findViewById(R.id.pin).setVisibility(View.GONE);
        getSupportActionBar().show();

        mPinShowing = false;
    }

    void storePin(String pin){
        //Store the entered pin number for later verification
        mPreferences.putPin(pin);
    }

    public static class PinFragment extends Fragment{
        private String mFirstPinEntry, mOldPin;
        PinEntryView pinEntryView;
        BaseActivity mActivity;
        TextView headerText;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mActivity = (BaseActivity)getActivity();

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pin_entry, null);

            //Set up the pin entry view
            pinEntryView = (PinEntryView)view.findViewById(R.id.pin_entry_view);
            pinEntryView.setFocus();
            pinEntryView.setOnPinEnteredListener(new IPinEnteredListener() {
                @Override
                public void pinEntered(final String pin) {
                    //delay continuation to give the illusion of accepting the last entered number
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (verifyPin(pin)) {
                                ((BaseActivity)getActivity()).mPreferences.putLastPinEntry(System.currentTimeMillis());

                                mActivity.pinAccepted();
                                pinEntryView.clearKeyBoard();
                            }
                            else {
                                pinEntryView.clearText();
                            }
                        }
                    }, 500);
                };
            });

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

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private boolean verifyPin(String s) {
            boolean accept = false;

            if (mActivity.mCurrentPinMode == PinMode.Initialize){
                //check if entering first time, request second to confirm
                if (mFirstPinEntry == null){
                    mFirstPinEntry = s;

                    headerText.setText("Confirm your PIN Number");

                }
                else{
                    accept = storePin(s);
                }
            }
            else if (mActivity.mCurrentPinMode == PinMode.Enter){
                //verify pin and allow access to application
                if (mActivity.mPreferences.getPin() != null && mActivity.mPreferences.getPin().equals(s)){
                    accept = true;
                }
                else{
                    headerText.setText("Please try again");
                }
            }
            else if (mActivity.mCurrentPinMode == PinMode.Change){
                //Enter in the old pin number first
                if (mOldPin == null) {
                    if (s.equals(mActivity.mPreferences.getPin())) {
                        mOldPin = s;

                        headerText.setText("Enter your new PIN number");
                    }
                    else{
                        headerText.setText("Incorrect, enter your current PIN");
                    }
                }
                //enter the new pin number
                else if (mFirstPinEntry == null){
                    mFirstPinEntry = s;
                    headerText.setText("Confirm your PIN Number");
                }
                else{
                    //confirm the new pin numbers match
                    if (mFirstPinEntry.equals(s)) {
                        //reencrypt the accounts that are currently encrypted with the old pin
                        mActivity.pinChanged(mOldPin, s);
                        accept = true;
                    }
                    else{
                        pinEntryView.clearText();
                        mFirstPinEntry = null;
                        headerText.setText("PIN mismatch. Try again.");
                    }
                }
            }

            return accept;
        }

        private boolean storePin(String s) {
            boolean accept = false;

            //Store the pin for good
            if (mFirstPinEntry.equals(s)){
                mActivity.storePin(s);
                accept = true;
            }
            else{
                pinEntryView.clearText();
                mFirstPinEntry = null;
                headerText.setText("PIN mismatch. Try again.");
            }

            return accept;
        }
    }

    protected void pinChanged(String mOldPin, String s){}
}
