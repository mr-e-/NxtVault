package com.nxt.testwallet.screens;

import android.app.ListActivity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.nxt.testwallet.R;
import com.nxt.testwallet.model.AccountViewModel;
import com.nxt.testwallet.model.AssetViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *  on 4/21/2015.
 */
public class BalanceFragment extends BaseFragment {
    TextView txtAccountRs;
    TextView txtAccountBalance;
    TextView txtDescription;
    TextView txtAccountName;
    ListView listAssets;

    private static BalanceFragment mInstance;

    public static <T extends BaseFragment> BaseFragment getInstance(){
        if (mInstance == null){
            mInstance = new BalanceFragment();
        }

        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_wallet, null);

        txtAccountRs = (TextView)view.findViewById(R.id.lblAccountRs);
        txtAccountBalance = (TextView)view.findViewById(R.id.txtBalance);
        txtAccountName = (TextView)view.findViewById(R.id.txtAccountName);
        txtDescription = (TextView)view.findViewById(R.id.txtDescription);
        listAssets = (ListView)view.findViewById(R.id.assetList);

        txtAccountRs.setTextIsSelectable(true);

        loadData();

        return view;
    }

    @Override
    public void loadData() {
        super.loadData();

        AccountViewModel account = mActivity.getCurrentAccount();

        if (account != null) {
            txtAccountRs.setText(account.Rs);
            txtAccountBalance.setText(account.getBalanceNXT() + " NXT");
            txtAccountName.setText(account.Name);
            txtDescription.setText(account.Description);

            ArrayList<HashMap<String, String>> assetsMap = new ArrayList<>();

            for (AssetViewModel asset : account.Assets) {
                HashMap<String, String> map = new HashMap<>();

                map.put("Asset Id", asset.AssetId);
                map.put("Asset Name", asset.Name);
                map.put("Amount Owned", String.valueOf(asset.getBalance()));

                assetsMap.add(map);
            }

            createAssetAdapter(assetsMap);
        }
    }

    private void createAssetAdapter(ArrayList<HashMap<String, String>> assetsMap) {

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.asset_list_row, listAssets, false);
        TextView txtAssetId = (TextView)headerView.findViewById(R.id.assetId);
        TextView txtAssetName = (TextView)headerView.findViewById(R.id.assetName);
        TextView txtBalance = (TextView)headerView.findViewById(R.id.assetBalance);

        txtAssetId.setText("Asset Id");
        txtAssetName.setText("Asset Name");
        txtBalance.setText("Balance");

        txtAssetId.setTypeface(null, Typeface.BOLD);
        txtAssetName.setTypeface(null, Typeface.BOLD);
        txtBalance.setTypeface(null, Typeface.BOLD);


        View c = getView().findViewById(R.id.headerPlaceholder);
        if (c != null) {
            ViewGroup parent = ((ViewGroup) c.getParent());

            int index = parent.indexOfChild(c);
            parent.removeView(c);
            parent.addView(headerView, index);
        }

        //Not sure why this isn't working, fake header added above
        //listAssets.addHeaderView(headerView, null, false);

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), assetsMap,
                R.layout.asset_list_row, new String[]{"Asset Id", "Asset Name", "Amount Owned"},
                new int[]{R.id.assetId, R.id.assetName, R.id.assetBalance});

        listAssets.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();

        setListViewHeightBasedOnChildren(listAssets);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("balance", txtAccountBalance.getText().toString());
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, TableLayout.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
