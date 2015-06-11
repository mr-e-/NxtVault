package com.nxt.nxtvault.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxt.nxtvault.App;
import com.nxt.nxtvault.legacy.JayClientApi;
import com.nxt.nxtvault.MainActivity;
import com.nxt.nxtvault.framework.TransactionFactory;

import javax.inject.Inject;

/**
 * Created by Brandon on 4/6/2015.
 */
public abstract class BaseFragment extends Fragment {
    protected MainActivity mActivity;

    @Inject
    protected JayClientApi mJay;

    @Inject
    TransactionFactory mTransactionFactory;

    public MainActivity getMainActivity(){
        return mActivity;
    }

    protected void showBackButton(boolean show){
        getMainActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(show);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity)activity;

        ((App)mActivity.getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflateView(inflater, container);

        onReady(view, savedInstanceState);

        return view;
    }

    protected abstract View inflateView(LayoutInflater inflater, @Nullable ViewGroup container);

    public void onReady(View rootView, Bundle savedInstanceState){

    }
}
