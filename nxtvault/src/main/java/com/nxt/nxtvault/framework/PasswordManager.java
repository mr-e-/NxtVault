package com.nxt.nxtvault.framework;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.MainActivity;
import com.nxt.nxtvault.MyApp;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.BroadcastTxResponse;

/**
 * Created by Brandon on 5/31/2015.
 */
public class PasswordManager {
    private JayClientApi mJay;
    EditText p1, p2;


    public PasswordManager(JayClientApi jay){
        mJay = jay;
    }

    public void setSpendingPassword(final MainActivity mainActivity, final AccountData accountData, final ValueCallback<Boolean> callback){
        MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                .title("Set Spending Password")
                .customView(R.layout.spending_passphrase_view, false)
                .positiveText("Accept")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        if (p1.getText() != null && p1.getText().toString() != "" && p1.getText().toString().equals(p2.getText().toString())) {
                            mJay.setSpendingPassword(accountData.accountRS, MyApp.SessionPin, "", p1.getText().toString(), new ValueCallback<AccountData>() {
                                @Override
                                public void onReceiveValue(AccountData value) {
                                    accountData.cipher = value.cipher;
                                    accountData.checksum = value.checksum;
                                    accountData.spendingPassphrase = value.spendingPassphrase;

                                    Toast.makeText(mainActivity, "Password Set!", Toast.LENGTH_LONG).show();
                                    callback.onReceiveValue(true);
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(mainActivity, "Passwords do not match!", Toast.LENGTH_LONG).show();
                            callback.onReceiveValue(false);
                        }
                    }
                })
                .build();

        View view = d.getCustomView();

        p1 = (EditText)view.findViewById(R.id.password);
        p2 = (EditText)view.findViewById(R.id.password2);

        d.show();
    }

    public void removeSpendingPassword(final MainActivity mainActivity, final AccountData accountData, final ValueCallback<Boolean> callback){
        MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                .title("Remove Spending Password")
                .customView(R.layout.remove_spending_passphrase_view, false)
                .positiveText("Accept")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        mJay.verifySpendingPassword(accountData, MyApp.SessionPin, p1.getText().toString(), new ValueCallback<Boolean>() {
                            @Override
                            public void onReceiveValue(Boolean value) {
                                if (value) {
                                    mJay.setSpendingPassword(accountData.accountRS, MyApp.SessionPin, p1.getText().toString(), "", new ValueCallback<AccountData>() {
                                        @Override
                                        public void onReceiveValue(AccountData value) {
                                            accountData.cipher = value.cipher;
                                            accountData.checksum = value.checksum;
                                            accountData.spendingPassphrase = value.spendingPassphrase;

                                            Toast.makeText(mainActivity, "Spending Password Removed Successfully", Toast.LENGTH_LONG).show();
                                            callback.onReceiveValue(true);
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(mainActivity, "Password incorrect", Toast.LENGTH_LONG).show();
                                    callback.onReceiveValue(false);
                                }
                            }
                        });
                    }
                })
                .build();

        View view = d.getCustomView();

        p1 = (EditText)view.findViewById(R.id.password);

        d.show();
    }

    public void getAccountKey(MainActivity mainActivity, final AccountData accountData, final ValueCallback<String> callback){
        if (accountData.getIsSpendingPasswordEnabled()) {
            MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                    .title("Enter Spending Password")
                    .customView(R.layout.remove_spending_passphrase_view, false)
                    .positiveText("Accept")
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            mJay.verifySpendingPassword(accountData, MyApp.SessionPin, p1.getText().toString(), new ValueCallback<Boolean>() {
                                @Override
                                public void onReceiveValue(Boolean value) {
                                    if (value){
                                        callback.onReceiveValue(p1.getText().toString());
                                    }
                                    else{
                                        callback.onReceiveValue(null);
                                    }
                                }
                            });
                        }
                    }).build();

            View view = d.getCustomView();

            p1 = (EditText)view.findViewById(R.id.password);

            d.show();
        }
        else{
            callback.onReceiveValue(MyApp.SessionPin);
        }
    }
}
