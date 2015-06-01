package com.nxt.nxtvault.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.controls.MultiLineDoneEditText;
import com.nxt.nxtvault.framework.TransactionFactory;
import com.nxt.nxtvault.util.TextValidator;

/**
 * Created by Brandon on 5/24/2015.
 */
public class SendMoneyFragment extends BaseFragment {
    String mAccountRs, mSenderPublicKey;

    EditText txtAmount;
    ButtonFloat btnSend;
    Double amount;
    CheckBox chkMessage;
    View messageView;
    MultiLineDoneEditText txtMessage;
    TextView lblAmount;

    public static SendMoneyFragment getInstance(String accountRs, String senderPubKey){
        SendMoneyFragment fragment = new SendMoneyFragment();

        fragment.mAccountRs = accountRs;
        fragment.mSenderPublicKey = senderPubKey;

        return fragment;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_send_money, container, false);
    }

    @Override
    public void onReady(View rootView, Bundle savedInstanceState) {
        super.onReady(rootView, savedInstanceState);

        mActivity.setTitle("SEND MONEY");

        if (savedInstanceState != null){
            mAccountRs = savedInstanceState.getString("rs");
            mSenderPublicKey = savedInstanceState.getString("sender");
        }

        txtMessage = (MultiLineDoneEditText)rootView.findViewById(R.id.message);
        chkMessage = (CheckBox)rootView.findViewById(R.id.chkMessage);
        messageView = rootView.findViewById(R.id.messageView);

        chkMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkMessage.isChecked()){
                    messageView.setVisibility(View.VISIBLE);
                }
                else{
                    messageView.setVisibility(View.INVISIBLE);
                }
            }
        });

        lblAmount = (TextView)rootView.findViewById(R.id.lblAmount);
        txtAmount = (EditText)rootView.findViewById(R.id.txtAmount);
        btnSend = (ButtonFloat)rootView.findViewById(R.id.btnSend);

        btnSend.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        btnSend.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_accept));

        btnSend.setEnabled(false);

        txtAmount.addTextChangedListener(new TextValidator(txtAmount) {
            @Override
            public void validate(TextView textView, String text) {
                try{
                    amount = Double.parseDouble(text);
                }
                catch (Exception ex){
                    amount = 0d;
                }

                if (amount > 0){
                    btnSend.setEnabled(true);
                }
                else{
                    btnSend.setEnabled(false);
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJay.sendMoney(mAccountRs, amount, chkMessage.isChecked() ? txtMessage.getText().toString() : null, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        TransactionFactory txFactory = TransactionFactory.getTransactionFactory(getMainActivity().mPreferences);

                        Intent intent = txFactory.createSelfSignedTx("nxtvault.intent.action.SIGNANDBROADCAST", value);
                        intent.putExtra("PublicKey", mSenderPublicKey);

                        startActivityForResult(intent, 2);
                    }
                });
            }
        });

        setFonts();

        View.OnFocusChangeListener listener =new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm =  (InputMethodManager)getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        };

        txtMessage.setOnFocusChangeListener(listener);
        txtAmount.setOnFocusChangeListener(listener);

        txtAmount.clearFocus();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtAmount.requestFocus();
            }
        }, 500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getMainActivity(), "Transaction Complete!", Toast.LENGTH_LONG).show();
        }

        mActivity.onBackPressed();
    }

    private void setFonts() {
        txtAmount.setTypeface(getMainActivity().segoe);
        chkMessage.setTypeface(getMainActivity().segoe);
        lblAmount.setTypeface(getMainActivity().segoe);
        txtMessage.setTypeface(getMainActivity().segoe);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("rs", mAccountRs);
        outState.putString("sender", mSenderPublicKey);
    }
}
