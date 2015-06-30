package com.nxt.nxtvault;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.gson.Gson;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.model.AccountSelectionResult;
import com.nxt.nxtvault.model.BroadcastTxResponse;
import com.nxt.nxtvault.screen.BaseFragment;
import com.nxt.nxtvault.security.AccessTokenProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class SignTxActivity extends MainActivity {
    AccessTokenProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProvider = new AccessTokenProvider(mPreferences.getSharedPref());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void navigateStart() {
        handleIntent();
    }

    public enum Operation{
        SIGNANDBROADCAST,
        SIGN,
        REQUESTACCOUNT,
        BROADCAST
    }

    public Operation mCurrentOperation;

    private void handleIntent() {
        //Handle the intent
        final Intent intent = getIntent();

        try {
            if (intent.getAction().equals("nxtvault.intent.action.SIGNANDBROADCAST")) {
                mCurrentOperation = Operation.SIGNANDBROADCAST;

                final BaseFragment fragment = new TxConfirmationFragment();

                final String txData = intent.getExtras().getString("TransactionData");
                String accessToken = intent.getExtras().getString("AccessToken");

                String publicKey;
                publicKey = getPublicKeyFromToken(intent, accessToken);
                final AccountData accountData = getAccount(publicKey, mAccountManager.getAllAccounts());

                if (publicKey == null) {
                    setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.access_denied)));
                } else {
                    mJay.extractUnsignedTxBytes(accountData, txData, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            ArrayList<TransactionLineItem> lineItems = getLineItems(value);

                            Bundle args = new Bundle();

                            if (lineItems == null || lineItems.size() == 0) {
                                setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.invalid_transaction)));
                            } else {
                                args.putSerializable(TxConfirmationFragment.ARGS, lineItems);
                                fragment.setArguments(args);

                                setTitle(getString(R.string.nxtvault_signtx));

                                navigate(fragment, false);
                            }
                        }
                    });
                }
            } else if (intent.getAction().equals("nxtvault.intent.action.SIGN")) {
                mCurrentOperation = Operation.SIGN;

                setTitle(getString(R.string.nxtvault_signtx));

            } else if (intent.getAction().equals("nxtvault.intent.action.REQUESTACCOUNT")) {
                mCurrentOperation = Operation.REQUESTACCOUNT;

                setTitle(getString(R.string.select_account));

                navigate(new AccountAccessFragment(), false);
            }
            else if (intent.getAction().equals("nxtvault.intent.action.BROADCAST")){
                mCurrentOperation = Operation.BROADCAST;

                setTitle(getString(R.string.broadcastTx));

                final BaseFragment fragment = new TxConfirmationFragment();

                final String txData = intent.getExtras().getString("TransactionData");

                mJay.extractSignedTxBytes(txData, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        ArrayList<TransactionLineItem> lineItems = getLineItems(value);

                        Bundle args = new Bundle();

                        if (lineItems == null || lineItems.size() == 0) {
                            setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.invalid_transaction)));
                        } else {
                            args.putSerializable(TxConfirmationFragment.ARGS, lineItems);
                            args.putBoolean(TxConfirmationFragment.SIGN, false);
                            fragment.setArguments(args);

                            navigate(fragment, false);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Log.e(getClass().getName(), ex.getMessage());
        }
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private ArrayList<TransactionLineItem> getLineItems(String value) {
        final ArrayList<TransactionLineItem> txLineItems = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(value);

            for (int i = 0; i < array.length(); i++) {
                TransactionLineItem lineItem = new TransactionLineItem();

                lineItem.LineItemTitle = array.getJSONObject(i).getString("key");
                lineItem.LineItem = processValue(lineItem.LineItemTitle, array.getJSONObject(i).getString("value"));

                txLineItems.add(lineItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return txLineItems;
    }

    private String processValue(String lineItemTitle, String value) {
        String result = value;

        if (lineItemTitle.equals("Asset Id")){
//            Asset asset = findAssetById(value);
//
//            if (asset != null){
//                result = value + " - " + asset.Name;
//            }
        }

        return result;
    }

    private String getPublicKeyFromToken(Intent intent, String accessToken) {
        String publicKey;//check if request came from within our own app, if so we don't need an access token
        if (mPreferences.getSharedPref().getString("tempToken", "").equals(accessToken)){
            publicKey = intent.getExtras().getString("PublicKey");
        }
        else{
            publicKey = mProvider.verify(accessToken);
        }
        return publicKey;
    }

    private boolean signTransaction(final ValueCallback<String> onSigned, final ValueCallback<String> onCancelled) {
        Intent intent = getIntent();

        final String txData = intent.getExtras().getString("TransactionData");
        String accessToken = intent.getExtras().getString("AccessToken");

        String publicKey = getPublicKeyFromToken(intent, accessToken);

        if (publicKey == null){
            onCancelled.onReceiveValue(getString(R.string.access_denied));
        }
        else{
            // access granted
            final AccountData accountData = getAccount(publicKey, mAccountManager.getAllAccounts());

            if (accountData == null) {
                onCancelled.onReceiveValue(getString(R.string.access_denied));
            }
            else {
                if (accountData.getIsSpendingPasswordEnabled()){
                    //user needs to enter their spending key first
                    mPasswordManager.getAccountKey(this, accountData, getAccountManager(), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String password) {
                            //Wrong password entered
                            if (password == null) {
                                Toast.makeText(SignTxActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();

                                onCancelled.onReceiveValue(getString(R.string.password_incorrect));
                            } else {
                                //sign the tx with the password
                                mJay.sign(accountData, mPinManager.getSessionPin(), password, txData, new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String signedBytes) {
                                        onSigned.onReceiveValue(signedBytes);
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    //sign the tx with no password
                    mJay.sign(accountData, mPinManager.getSessionPin(), "", txData, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String signedBytes) {
                            onSigned.onReceiveValue(signedBytes);
                        }
                    });
                }
            }
        }

        return false;
    }

    private void setResultAndFinish(int result, Intent intent){
        setResult(result, intent);
        finish();
    }

    private AccountData getAccount(String publicKey, ArrayList<AccountData> accountDatas){
        for(AccountData data : accountDatas){
            if (data.publicKey.equals(publicKey)){
                return data;
            }
        }

        return null;
    }

    private void itemSelected(AccountData accountData){
        AccountSelectionResult result = new AccountSelectionResult();
        result.AccessToken = mProvider.getNewToken(accountData);
        result.AccountRs = accountData.accountRS;
        result.PublicKey = accountData.publicKey;

        String json = new Gson().toJson(result);

        setResult(RESULT_OK, new Intent(json));

        finish();
    }

    public void confirm(){
        if (mCurrentOperation == Operation.SIGNANDBROADCAST) {
            signTransaction(new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String signedBytesString) {
                    broadcast(signedBytesString, null);
                }
            }, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.access_denied)));
                }
            });
        }
        else if (mCurrentOperation == Operation.BROADCAST) {
            broadcast(getIntent().getExtras().getString("TransactionData"), null);
        }
    }

    public void broadcast(String signedBytesString, final ValueCallback<Boolean> callback) {
        //broadcast the transaction
        mJay.broadcast(signedBytesString, new ValueCallback<BroadcastTxResponse>() {
            @Override
            public void onReceiveValue(BroadcastTxResponse response) {
                Gson gson = new Gson();
                if (response != null && response.ErrorCode == 0)
                    setResultAndFinish(RESULT_OK, new Intent(gson.toJson(response, BroadcastTxResponse.class)));
                else {
                    if (response != null)
                        setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.error_broadcasting) + response.ErrorCode + " - " + response.ErrorDescription));
                    else {
                        setResultAndFinish(RESULT_CANCELED, new Intent(getString(R.string.error_broadcasting) + "Unknown error. Please check your settings if you've overriden the broadcast server and make sure it is correct."));
                    }
                }

                if (callback != null)
                    callback.onReceiveValue(true);
            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AccountAccessFragment extends BaseFragment {
        public AccountAccessFragment() {
        }

        @Override
        protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
            return inflater.inflate(R.layout.fragment_account_access, container, false);
        }

        @Override
        public void onReady(View rootView, Bundle savedInstanceState) {
            super.onReady(rootView, savedInstanceState);

            final ListView listView = (ListView)rootView.findViewById(R.id.accountList);

            final AccountAdapter adapter = new AccountAdapter(getActivity());

            ButtonFloat btnCancel = (ButtonFloat)rootView.findViewById(R.id.btnCancel);

            btnCancel.setBackgroundColor(getResources().getColor(R.color.delete));
            btnCancel.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_cancel));
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            for (AccountData accountData : getMainActivity().mAccountManager.getAllAccounts()) {
                adapter.add(accountData);
            }

            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ((SignTxActivity) getActivity()).itemSelected(adapter.getItem(position));
                        }
                    }).start();
                }
            });
        }

        @Override
        public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
            Animation anim;

            if (enter) {
                anim = AnimationUtils.loadAnimation(getActivity(), android.support.v7.appcompat.R.anim.abc_slide_in_bottom);
            } else {
                anim = AnimationUtils.loadAnimation(getActivity(), android.support.v7.appcompat.R.anim.abc_slide_out_bottom);
            }

            return anim;
        }

        public class AccountAdapter extends ArrayAdapter<AccountData>{
            public AccountAdapter(Context context) {
                super(context,  R.layout.account_item_first);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = null;

                if (convertView == null){
                    //place a margin on every row except the first one since it is already padded by the surrounded CardView
                    if (position > 0)
                        view = LayoutInflater.from(getContext()).inflate(R.layout.account_item, null);
                    else
                        view = LayoutInflater.from(getContext()).inflate(R.layout.account_item_first, null);
                }
                else{
                    view = convertView;
                }

                TextView accountRs = (TextView)view.findViewById(R.id.accountRs);
                TextView txtAccountName = (TextView)view.findViewById(R.id.accountName);

                AccountData accountData = getItem(position);

                txtAccountName.setTextColor(getResources().getColor(android.R.color.background_dark));

                accountRs.setText(accountData.accountRS);
                txtAccountName.setText(accountData.accountName);

                accountRs.setTypeface(((SignTxActivity)getActivity()).segoe);
                txtAccountName.setTypeface(((SignTxActivity) getActivity()).segoel);

                return view;
            }
        }
    }

    public static class TxConfirmationFragment extends BaseFragment {
        public static final String ARGS = "lineItems";
        public static final String SIGN = "sign";

        public TxConfirmationFragment() {

        }

        @Override
        protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
            return inflater.inflate(R.layout.fragment_tx_confirmation, container, false);
        }



        @Override
        public void onReady(View rootView, Bundle savedInstanceState) {
            super.onReady(rootView, savedInstanceState);

            final ButtonFloat btnConfirm = (ButtonFloat)rootView.findViewById(R.id.btnConfirm);
            final ButtonFloat btnCancel = (ButtonFloat)rootView.findViewById(R.id.btnCancel);
            final ListView lst_tx_details = (ListView)rootView.findViewById(R.id.tx_details_list);
            final View progress = (View)rootView.findViewById(R.id.progress);
            final View buttons = (View)rootView.findViewById(R.id.buttons);

            final SignTxActivity activity = (SignTxActivity)getActivity();


            //TODO: Lower android version
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progress.setVisibility(View.VISIBLE);

                    ObjectAnimator.ofFloat(lst_tx_details, View.ALPHA, 1, 0).start();
                    ObjectAnimator.ofFloat(buttons, View.ALPHA, 1, 0).start();
                    ObjectAnimator.ofFloat(progress, View.ALPHA, 0, 1).start();

                    ((SignTxActivity)mActivity).confirm();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                    activity.cancel();
                }
            });

            btnConfirm.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnConfirm.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_accept));

            btnCancel.setBackgroundColor(getResources().getColor(R.color.delete));
            btnCancel.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_cancel));

            //parse tx line args
            Bundle args = getArguments();
            ArrayList<TransactionLineItem> lineItem = (ArrayList<TransactionLineItem>)args.getSerializable(ARGS);

            TxDetailsAdapter adapter = new TxDetailsAdapter(getActivity());
            for(TransactionLineItem item : lineItem){
                adapter.add(item);
            }

            lst_tx_details.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
            Animation anim;

            if (enter) {
                anim = AnimationUtils.loadAnimation(getActivity(), android.support.v7.appcompat.R.anim.abc_slide_in_bottom);
            } else {
                anim = AnimationUtils.loadAnimation(getActivity(), android.support.v7.appcompat.R.anim.abc_slide_out_bottom);
            }

            return anim;
        }

        private class TxDetailsAdapter extends ArrayAdapter<TransactionLineItem>{
            public TxDetailsAdapter(Context context){
                super(context, R.layout.tx_detail_line_item);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = null;

                if (convertView == null){
                    view = LayoutInflater.from(getContext()).inflate(R.layout.tx_detail_line_item, null);
                }
                else{
                    view = convertView;
                }

                TextView txtLineItemTitle = (TextView)view.findViewById(R.id.lineItemTitle);
                TextView txtLineItem = (TextView)view.findViewById(R.id.lineItem);
                TextView txtLineAmount = (TextView)view.findViewById(R.id.lineAmount);

                txtLineItemTitle.setTypeface(((SignTxActivity)getActivity()).segoe);
                txtLineItem.setTypeface(((SignTxActivity)getActivity()).segoel);
                txtLineAmount.setTypeface(((SignTxActivity)getActivity()).segoel);

                        TransactionLineItem tx = getItem(position);

                txtLineItemTitle.setText(tx.LineItemTitle);
                txtLineItem.setText(tx.LineItem);

                //Don't show if line amount is 0
                if (tx.LineAmount != 0) {
                    DecimalFormat df = new DecimalFormat("#.########");
                    df.setMinimumFractionDigits(0);
                    df.setMaximumFractionDigits(8);
                    df.setMinimumIntegerDigits(1);
                    df.setGroupingSize(3);
                    df.setGroupingUsed(true);
                    String val = df.format(tx.LineAmount);
                    txtLineAmount.setText(val);
                }
                else{
                    txtLineAmount.setText(null);
                }

                return view;
            }
        }
    }
}
