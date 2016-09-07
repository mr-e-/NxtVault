package com.nxt.testwallet.screens;

/**
 *  on 4/21/2015.
 */
public class MSTransferFragment extends BaseFragment {
    private static MSTransferFragment mInstance;

    public static <T extends BaseFragment> BaseFragment getInstance(){
        if (mInstance == null){
            mInstance = new MSTransferFragment();
        }

        return mInstance;
    }
}
