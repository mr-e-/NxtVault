package com.nxt.nxtvault.screen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.R;
import com.nxt.nxtvault.model.AccountData;

import java.util.ArrayList;

/**
 * Created by Brandon on 4/6/2015.
 */
public class AccountFragment extends BaseFragment {
    ButtonFloat btnNewAccount;

    JayClientApi jay;

    ArrayList<AccountData> mAccountData;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)LayoutInflater.from(getActivity()).inflate(R.layout.fragment_account, container, false);

        ListView listView = (ListView)view.findViewById(R.id.accountList);

        if (savedInstanceState != null){
            mAccountData = (ArrayList<AccountData>)savedInstanceState.getSerializable("mAccountData");
        }
        else {
            mAccountData = getMainActivity().getAccountInfo().getAccountData();
        }

        final AccountAdapter accountAdapter = new AccountAdapter(getMainActivity(), R.layout.account_item);
        for(AccountData accountData : mAccountData){
            accountAdapter.add(accountData);
        }

        listView.setAdapter(accountAdapter);
        accountAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            getMainActivity().navigate(ManageAccountFragment.getInstance(false, accountAdapter.getItem(position)), true);
            }
        });

        btnNewAccount = (ButtonFloat)view.findViewById(R.id.new_account);
        btnNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        btnNewAccount.setBackgroundColor(getResources().getColor(R.color.primary__extra_light));
        btnNewAccount.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_new));

        return view;
    }

    private void createNewAccount() {
        getMainActivity().navigate(ManageAccountFragment.getInstance(true, null), true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        jay = getMainActivity().getJay();
    }

    @Override
    public void onResume() {
        super.onResume();

        getMainActivity().setTitle(getMainActivity().getString(R.string.accounts));
        showBackButton(false);

        if (mAccountData.size() == 0){
            createNewAccount();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mAccountInfo", mAccountData);
    }

    private class AccountAdapter extends ArrayAdapter<AccountData> {
        public AccountAdapter(Context context, int resource) {
            super(context, resource);
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

            TextView txtAccountName = (TextView)view.findViewById(R.id.accountName);
            TextView txtAccountRs = (TextView)view.findViewById(R.id.accountRs);

            AccountData accountData = getItem(position);

            txtAccountName.setText(accountData.accountName);

            txtAccountName.setTextColor(getResources().getColor(android.R.color.background_dark));
            txtAccountName.setTypeface(getMainActivity().segoe);
            txtAccountRs.setTypeface(getMainActivity().segoel);

            txtAccountRs.setText(accountData.accountRS);

            return view;
        }
    }
}
