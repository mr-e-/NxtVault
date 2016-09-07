package com.nxt.testwallet.screens;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.nxt.testwallet.MainActivity;

/**
 *  on 4/21/2015.
 */
public abstract class BaseFragment extends Fragment {
    protected MainActivity mActivity;
    private boolean mIsLoaded;

    public void loadData(){}

    public void setIsLoaded(){
        mIsLoaded = true;
    }

    public boolean getIsLoaded(){
        return mIsLoaded;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity)activity;
    }
}
