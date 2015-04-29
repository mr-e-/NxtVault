package com.nxt.testwallet.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.TextView;

import com.nxt.testwallet.R;

/**
 * Created by Brandon on 4/21/2015.
 */
public class PaymentFragment extends BaseFragment {
    TextView txtRecip;
    TextView txtAmount;
    TextView txtMessage;
    Button btnSend;


    private static PaymentFragment mInstance;

    public static <T extends BaseFragment> BaseFragment getInstance(){
        if (mInstance == null){
            mInstance = new PaymentFragment();
        }

        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_payment, null);

        txtRecip = (TextView)view.findViewById(R.id.txtRecip);
        txtAmount = (TextView)view.findViewById(R.id.txtAmount);
        txtMessage = (TextView)view.findViewById(R.id.txtMessage);
        btnSend = (Button)view.findViewById(R.id.btnSend);

        loadData();

        return view;
    }

    @Override
    public void loadData() {
        super.loadData();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getJay().sendMoney(txtRecip.getText().toString(), Float.parseFloat(txtAmount.getText().toString()), txtMessage.getText().toString(), new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        mActivity.getNxtVault().signAndBroadcastTx(mActivity, mActivity.getAccessToken(), value);
                    }
                });
            }
        });
    }
}
