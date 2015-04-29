package com.nxt.testwallet.screens;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;
import com.nxt.testwallet.R;

import java.util.Arrays;

/**
 * Created by Brandon on 4/21/2015.
 */
public class AssetTransferFragment extends BaseFragment {
    TextView txtRecip;
    AutoCompleteTextView txtAssetId;
    TextView txtAmount;
    TextView txtMessage;
    Button btnSend;

    Asset mSelectedAsset;

    Asset[] mAssetList;

    private static AssetTransferFragment mInstance;

    public static <T extends BaseFragment> BaseFragment getInstance(){
        if (mInstance == null){
            mInstance = new AssetTransferFragment();
        }

        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_asset_transfer, null);

        txtRecip = (TextView)view.findViewById(R.id.txtRecip);
        txtAmount = (TextView)view.findViewById(R.id.txtAmount);
        txtAssetId = (AutoCompleteTextView)view.findViewById(R.id.txtAssetId);
        txtMessage = (TextView)view.findViewById(R.id.txtMessage);
        btnSend = (Button)view.findViewById(R.id.btnSend);

        loadData();

        return view;
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mActivity.getAssetList() != null && mActivity.getAssetList().size() > 0) {
            mAssetList = mActivity.getAssetList().toArray(new Asset[mActivity.getAssetList().size()]);
            Arrays.sort(mAssetList);

            final ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mAssetList);
            txtAssetId.setAdapter(adapter);
            txtAssetId.setThreshold(1);

            txtAssetId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.v("Test", "Focus changed");
                    if (!hasFocus) {
                        Log.v("Test", "Performing validation");
                        ((AutoCompleteTextView) v).performValidation();
                    }
                }
            });

            txtAssetId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedAsset = (Asset) adapter.getItem(position);
                    Log.v("Test", "Asset selected: " + mSelectedAsset.Name);
                }
            });

            txtAssetId.setValidator(new AutoCompleteTextView.Validator() {
                @Override
                public boolean isValid(CharSequence text) {
                    boolean valid = false;

                    for (Asset asset : mActivity.getAssetList()) {
                        if (asset.Name.equals(text.toString())) {
                            valid = true;
                            break;
                        }
                    }

                    return valid;
                }

                @Override
                public CharSequence fixText(CharSequence invalidText) {
                    return "";
                }
            });


            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (mSelectedAsset == null) {
                    txtAssetId.setError("Please select a valid asset");
                } else {
                    mActivity.getJay().transferAsset(txtRecip.getText().toString(), mSelectedAsset, Float.parseFloat(txtAmount.getText().toString()), txtMessage.getText().toString(), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            mActivity.getNxtVault().signAndBroadcastTx(mActivity, mActivity.getAccessToken(), value);
                        }
                    });
                }
                }
            });
        }
    }
}
