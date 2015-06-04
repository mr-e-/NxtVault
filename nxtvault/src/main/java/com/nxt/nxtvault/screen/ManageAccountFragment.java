package com.nxt.nxtvault.screen;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.framework.PasswordManager;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.framework.TransactionFactory;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.util.TextValidator;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

/**
 * Created by Brandon on 4/18/2015.
 */
public class ManageAccountFragment extends BaseFragment {
    public static final String ENCRYPTED_PASSPHRASE = "****************************************";

    @Inject
    PasswordManager mPasswordManager;

    @Inject
    PinManager mPinManager;

    @Inject
    TransactionFactory mTransactionFactory;

    TextView txtAccountName;
    TextView txtAccountRs;
    TextView txtPublicKey;
    TextView lblAccountName;
    TextView lblAccountRs;
    TextView lblPublicKey;
    TextView lblPassphrase;
    EditText txtPassphrase;
    ImageView imgPassphrase;
    ButtonFloat btnSave;
    CheckBox chkSPendingPassword;

    boolean newAccount;
    AccountData accountData;

    private boolean mIsEdited;

    Timer passphraseEntryTimer;

    private static final String REQUEST_SCAN_PRIVATE_KEY = "scan_private_key";
    private static final String REQUEST_SCAN_TX = "scan_tx";

    private String mRequestCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);
    }

    public static ManageAccountFragment getInstance(Boolean newAccount, AccountData accountData){
        ManageAccountFragment fragment = new ManageAccountFragment();

        fragment.accountData = accountData;
        fragment.newAccount = newAccount;

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!newAccount)
            getMainActivity().getMenuInflater().inflate(R.menu.menu_account, menu);

        menu.findItem(R.id.action_change_pin).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan_tx) {
            //Deleting account
            new AlertDialog.Builder(getMainActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you wish to delete this account?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getMainActivity().getAccountManager().deleteAccount(accountData);
                            getMainActivity().onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        else if (id == R.id.action_export_account){
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Account RS: " + accountData.accountRS + ", " + "Account Public Key: " + accountData.publicKey);
            sendIntent.setType("text/plain");

            startActivity(sendIntent);
        }

        return true;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_manage_account, container, false);
    }

    @Override
    public void onReady(View rootView, Bundle savedInstanceState) {
        super.onReady(rootView, savedInstanceState);

        ButtonFloat btnSave = (ButtonFloat)rootView.findViewById(R.id.btnSave);
        btnSave.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        btnSave.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_accept));

        if (savedInstanceState != null){
            accountData = (AccountData)savedInstanceState.getSerializable("accountData");
            newAccount = savedInstanceState.getBoolean("newAccount");
            mRequestCode = savedInstanceState.getString("mRequestCode");
        }

        if (newAccount && accountData == null){
            //disable spending password until account is save
            rootView.findViewById(R.id.spendingPasswordView).setVisibility(View.GONE);

            generateNewAccount(rootView);
        }
        else{
            hydrate(rootView);
        }
    }

    private void generateNewAccount(final View rootView) {
        showLoadingSpinner(rootView, null);

        getMainActivity().getAccountManager().getNewAccount(new ValueCallback<AccountData>() {
            @Override
            public void onReceiveValue(AccountData value) {
                accountData = value;

                hydrate(rootView);

                hideLoadingSpinner(rootView);
            }
        });
    }

    private void showLoadingSpinner(View rootView, String message) {
        //show loading spinner
        final View progress = rootView.findViewById(R.id.progress);

        if (message != null) {
            final TextView txtProgressText = (TextView) rootView.findViewById(R.id.txtProgress);
            txtProgressText.setText(message);
        }

        ObjectAnimator.ofFloat(progress, View.ALPHA, 0, 1).setDuration(500).start();
        ObjectAnimator.ofFloat(rootView.findViewById(R.id.scrollview), View.ALPHA, 1, 0).setDuration(500).start();
        ObjectAnimator.ofFloat(rootView.findViewById(R.id.btnSave), View.ALPHA, 1, 0).setDuration(500).start();

        progress.setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.btnSave).setVisibility(View.INVISIBLE);
    }

    private void hideLoadingSpinner(View rootView) {
        final View progress = rootView.findViewById(R.id.progress);

        ObjectAnimator.ofFloat(progress, View.ALPHA, 1, 0).setDuration(500).start();
        ObjectAnimator.ofFloat(rootView.findViewById(R.id.scrollview), View.ALPHA, 0, 1).setDuration(500).start();
        ObjectAnimator.ofFloat(rootView.findViewById(R.id.btnSave), View.ALPHA, 0, 1).setDuration(500).start();

        rootView.findViewById(R.id.scrollview).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.btnSave).setVisibility(View.VISIBLE);

        progress.setVisibility(View.GONE);
    }

    private void hydrate(View rootView) {
        lblAccountName = (TextView) rootView.findViewById(R.id.lblAccount);
        lblAccountRs = (TextView) rootView.findViewById(R.id.lblAccount);
        lblPublicKey = (TextView) rootView.findViewById(R.id.lblPubKey);
        lblPassphrase = (TextView) rootView.findViewById(R.id.lblPassphrase);

        txtAccountName = (EditText) rootView.findViewById(R.id.accountName);
        txtAccountRs = (TextView) rootView.findViewById(R.id.accountRs);
        txtPublicKey = (TextView) rootView.findViewById(R.id.publicKey);
        txtPassphrase = (EditText) rootView.findViewById(R.id.passphrase);
        imgPassphrase = (ImageView) rootView.findViewById(R.id.showHidePrivateKey);

        chkSPendingPassword = (CheckBox)rootView.findViewById(R.id.chkSpending);

        txtAccountName.setTypeface(getMainActivity().segoe);
        txtAccountRs.setTypeface(getMainActivity().segoe);
        txtPublicKey.setTypeface(getMainActivity().segoe);
        txtPassphrase.setTypeface(getMainActivity().segoe);
        lblAccountName.setTypeface(getMainActivity().segoe);
        lblAccountRs.setTypeface(getMainActivity().segoe);
        lblPublicKey.setTypeface(getMainActivity().segoe);
        lblPassphrase.setTypeface(getMainActivity().segoe);

        txtAccountName.setText(accountData.accountName);
        txtAccountRs.setText(accountData.accountRS);
        txtPublicKey.setText(accountData.publicKey);


        View.OnFocusChangeListener listener =new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm =  (InputMethodManager)getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        };

        txtAccountName.setOnFocusChangeListener(listener);
        txtPassphrase.setOnFocusChangeListener(listener);

        txtAccountRs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getMainActivity().getSystemService(getMainActivity().CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Account RS", txtAccountRs.getText().toString()));

                Toast.makeText(getMainActivity(), txtAccountRs.getText().toString() + " copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        txtPublicKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getMainActivity().getSystemService(getMainActivity().CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", txtPublicKey.getText().toString()));

                Toast.makeText(getMainActivity(), txtPublicKey.getText().toString() + " copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });


        if (newAccount){
            txtPassphrase.setText(accountData.secretPhrase);
            txtPassphrase.setEnabled(true);
            getMainActivity().setTitle(getMainActivity().getString(R.string.add_account));
        }
        else{
            txtPassphrase.setText(ENCRYPTED_PASSPHRASE);
            imgPassphrase.setVisibility(View.VISIBLE);
            getMainActivity().setTitle(getMainActivity().getString(R.string.view_account));
        }

        chkSPendingPassword.setChecked(accountData.getIsSpendingPasswordEnabled());

        if (newAccount){
            txtPassphrase.setTextColor(getResources().getColor(R.color.primary_dark));
            imgPassphrase.setImageResource(R.drawable.qr_scan);
            imgPassphrase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startScan(REQUEST_SCAN_PRIVATE_KEY);
                }
            });

            txtPassphrase.addTextChangedListener(new TextValidator(txtPassphrase) {
                @Override
                public void validate(TextView textView, String text) {
                    btnSave.setEnabled(false);

                    if (passphraseEntryTimer != null){
                        passphraseEntryTimer.cancel();
                        passphraseEntryTimer = null;
                    }
                    //only load the new account info after a pause in typing
                    passphraseEntryTimer = new Timer();
                    passphraseEntryTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getMainActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run () {
                                 generateAccount();
                                }
                            });
                        }
                    }, 500);
                }
            });
        }
        else {
            imgPassphrase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHidePassphrase();
                }
            });
        }

        btnSave = (ButtonFloat)rootView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsEdited && !newAccount){
                    startScan(REQUEST_SCAN_TX);
                }
                else {
                    String accountName = txtAccountName.getText().toString();

                    if (accountName == null || accountName.isEmpty()) {
                        txtAccountName.setError("Please enter an account name", getResources().getDrawable(R.drawable.indicator_input_error));
                        txtAccountName.requestFocus();
                    } else if (getMainActivity().getAccountManager().getAccountByName(accountName) != null) {
                        txtAccountName.setError("Account name already in use",getResources().getDrawable(R.drawable.indicator_input_error));
                        txtAccountName.requestFocus();
                    } else {
                        accountData.accountName = accountName;
                        getMainActivity().getAccountManager().storeAccount(accountData);

                        Toast.makeText(getMainActivity(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                        getMainActivity().onBackPressed();
                    }
                }
            }
        });

        setButton();

        txtAccountName.addTextChangedListener(new TextValidator(txtAccountName) {
            @Override
            public void validate(TextView textView, String text) {
                if (accountData.accountName != null && !accountData.accountName.equals(text)){
                    mIsEdited = true;
                }
                else{
                    mIsEdited = false;
                }

                setButton();
            }
        });

        chkSPendingPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkSPendingPassword.isChecked()){
                    if (!newAccount) {
                        mPasswordManager.setSpendingPassword(getMainActivity(), accountData, getMainActivity().getAccountManager(), new ValueCallback<Boolean>() {
                            @Override
                            public void onReceiveValue(Boolean value) {
                                if (!value)
                                    chkSPendingPassword.setChecked(false);
                            }
                        });
                    }
                    else{

                    }
                }
                else{
                    mPasswordManager.removeSpendingPassword(getMainActivity(), accountData, getMainActivity().getAccountManager(), new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean value) {
                            if (!value)
                                chkSPendingPassword.setChecked(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        showBackButton(true);
    }

    private void startScan(String requestCode){
        mRequestCode = requestCode;

        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(ManageAccountFragment.this);
        integrator.initiateScan();
    }

    private void showHidePassphrase() {
        if (txtPassphrase.getText().toString().equals(ENCRYPTED_PASSPHRASE)) {
            showLoadingSpinner(getView(), "Decrypting passphrase");

            if (accountData.getIsSpendingPasswordEnabled()){
                mPasswordManager.getAccountKey(getMainActivity(), accountData, getMainActivity().getAccountManager(), new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (value == null){
                            Toast.makeText(getMainActivity(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                            hideLoadingSpinner(getView());
                        }
                        else {
                            decryptSecretPhrase(value);
                        }
                    }
                });
            }
            else{
                decryptSecretPhrase("");
            }
        }
        else{
            txtPassphrase.setText(ENCRYPTED_PASSPHRASE);
        }
    }

    private void decryptSecretPhrase(String password) {
        mJay.decryptSecretPhrase(accountData, mPinManager.getSessionPin(), password, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                txtPassphrase.setText(value);

                hideLoadingSpinner(getView());
            }
        });
    }

    private void setButton() {
        if (mIsEdited || newAccount) {
            btnSave.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnSave.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_accept));
        }
        else{
            btnSave.setBackgroundColor(getResources().getColor(R.color.accent));
            btnSave.setDrawableIcon(getResources().getDrawable(R.drawable.icon_scan));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void generateAccount() {
        mJay.getNewAccount(txtPassphrase.getText().toString(), mPinManager.getSessionPin(), new ValueCallback<AccountData>() {
            @Override
            public void onReceiveValue(final AccountData value) {
                accountData = value;
                if (accountData == null) {
                    txtPassphrase.setError("Invalid passphrase");
                } else {
                    txtAccountRs.setText(value.accountRS);
                    txtPublicKey.setText(value.publicKey);

                    btnSave.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("accountData", accountData);
        outState.putBoolean("newAccount", newAccount);
        outState.putString("mRequestCode", mRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get the result of a transaction broadcase
        if (requestCode == 2){
            if (resultCode == Activity.RESULT_CANCELED){
                if (data != null)
                    Toast.makeText(getMainActivity(), data.getAction(), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getMainActivity(), "Transaction Cancelled!", Toast.LENGTH_LONG).show();
            }
            else if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getMainActivity(), "Transaction Complete!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            //qr code was scanned
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                String re = scanResult.getContents();
                if (re != null && mRequestCode != null) {
                    if (mRequestCode.equals(REQUEST_SCAN_PRIVATE_KEY)) {
                        txtPassphrase.setText(re);

                        generateAccount();
                    } else if (mRequestCode.equals(REQUEST_SCAN_TX)) {
                        if (re.startsWith("NXT-")){
                            //Scanned an address code, load the sendmoney fragment
                            getMainActivity().navigate(SendMoneyFragment.getInstance(re, accountData.publicKey), true);
                        }
                        else {
                            Intent intent = mTransactionFactory.createSelfSignedTx("nxtvault.intent.action.SIGNANDBROADCAST", re);
                            intent.putExtra("PublicKey", accountData.publicKey);

                            startActivityForResult(intent, 2);
                        }
                    }
                }
            }
        }
    }


}
