package com.nxt.nxtvault.framework;

import android.view.View;
import android.webkit.ValueCallback;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nxt.nxtvault.MainActivity;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.model.AccountData;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *  on 5/31/2015.
 */
@Singleton
public class PasswordManager {
    EditText p1, p2;

    @Inject
    public PasswordManager(){

    }

    public void setSpendingPassword(final MainActivity mainActivity, final AccountData accountData, final AccountManager accountManager, final ValueCallback<Boolean> callback){
        MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                .title("Set Spending Password")
                .customView(R.layout.spending_passphrase_view, false)
                .positiveText("Accept")
                .negativeText("Cancel")
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        if (p1.getText() != null && p1.getText().toString() != "" && p1.getText().toString().equals(p2.getText().toString())) {
                            accountManager.setSpendingPassword(accountData, "", p1.getText().toString(), new ValueCallback<Void>() {
                                @Override
                                public void onReceiveValue(Void value) {
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

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        callback.onReceiveValue(false);
                    }
                })
                .build();

        View view = d.getCustomView();

        p1 = (EditText)view.findViewById(R.id.password);
        p2 = (EditText)view.findViewById(R.id.password2);

        d.show();
    }

    public void removeSpendingPassword(final MainActivity mainActivity, final AccountData accountData, final AccountManager accountManager, final ValueCallback<Boolean> callback){
        MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                .title("Remove Spending Password")
                .customView(R.layout.remove_spending_passphrase_view, false)
                .cancelable(false)
                .positiveText("Accept")
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        accountManager.verifySpendingPassword(accountData, p1.getText().toString(), new ValueCallback<Boolean>() {
                            @Override
                            public void onReceiveValue(Boolean value) {
                                if (value) {
                                    accountManager.setSpendingPassword(accountData, p1.getText().toString(), "", new ValueCallback<Void>() {
                                        @Override
                                        public void onReceiveValue(Void value) {
                                            Toast.makeText(mainActivity, "Spending Password Removed Successfully", Toast.LENGTH_LONG).show();
                                            callback.onReceiveValue(true);
                                        }
                                    });
                                } else {
                                    Toast.makeText(mainActivity, "Password incorrect", Toast.LENGTH_LONG).show();
                                    callback.onReceiveValue(false);
                                }
                            }
                        });
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        callback.onReceiveValue(false);
                    }
                })
                .build();

        View view = d.getCustomView();

        p1 = (EditText)view.findViewById(R.id.password);

        d.show();
    }

    public void getAccountKey(MainActivity mainActivity, final AccountData accountData, final AccountManager accountManager, final ValueCallback<String> callback){
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

                            accountManager.verifySpendingPassword(accountData, p1.getText().toString(), new ValueCallback<Boolean>() {
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

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);

                            callback.onReceiveValue(null);
                        }
                    }).build();

            View view = d.getCustomView();

            p1 = (EditText)view.findViewById(R.id.password);

            d.show();
        }
        else{
            callback.onReceiveValue(null);
        }
    }
}
