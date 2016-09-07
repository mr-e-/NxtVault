package com.nxt.nxtvault.model;

import com.nxt.nxtvault.model.AccountData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  on 4/16/2015.
 */
public class AccountInfo implements Serializable{
    private ArrayList<AccountData> mAccountData;

    public AccountInfo(ArrayList<AccountData> accountData){
        mAccountData = accountData;
    }

    public ArrayList<AccountData> getAccountData(){
        if (mAccountData == null)
            mAccountData = new ArrayList<>();

        return mAccountData;
    }

    public boolean nameExists(String name){
        if (mAccountData != null) {
            if (findAccount(name) != null){
                return true;
            }
        }

        return false;
    }

    private AccountData findAccount(String name){
        for (AccountData accountData : mAccountData) {
            if (accountData.accountName.toLowerCase().equals(name.toLowerCase())) {
                return accountData;
            }
        }

        return null;
    }

    public void deleteAccount(AccountData accountData) {
        AccountData data = findAccount(accountData.accountName);
        mAccountData.remove(data);
    }
}
