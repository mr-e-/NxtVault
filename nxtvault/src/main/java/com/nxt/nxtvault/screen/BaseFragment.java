package com.nxt.nxtvault.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxt.nxtvault.IJayLoadedListener;
import com.nxt.nxtvault.MainActivity;

/**
 * Created by Brandon on 4/6/2015.
 */
public abstract class BaseFragment extends Fragment {
    MainActivity mActivity;

    public MainActivity getMainActivity(){
        return mActivity;
    }
    protected void showBackButton(boolean show){
        getMainActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(show);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflateView(inflater, container);

        if (!mActivity.getIsJayLoaded()){
            mActivity.subscribeJayLoaded(new IJayLoadedListener() {
                @Override
                public void onLoaded() {
                    onReady(view, savedInstanceState);
                }
            });
        }
        else{
            onReady(view, savedInstanceState);
        }

        return view;
    }

    protected abstract View inflateView(LayoutInflater inflater, @Nullable ViewGroup container);

    public void onReady(View rootView, Bundle savedInstanceState){

    }
}
