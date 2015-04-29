package com.nxt.nxtvault.screen;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.nxt.nxtvault.MainActivity;

/**
 * Created by Brandon on 4/6/2015.
 */
public class BaseFragment extends Fragment {
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
}
