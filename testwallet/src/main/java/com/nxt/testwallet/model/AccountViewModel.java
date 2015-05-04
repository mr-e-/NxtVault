package com.nxt.testwallet.model;

import com.nxt.nxtvaultclientlib.nxtvault.model.Account;

import java.util.ArrayList;

/**
 * Created by bcollins on 2015-04-22.
 */
public class AccountViewModel {
    public AccountViewModel(Account account){
        Assets = new ArrayList<>();

        if (account != null) {
            Name = account.Name;
            Description = account.Description;
            Rs = account.Rs;
            BalanceNQT = account.BalanceNQT;
        }
    }

    public String Name;
    public String Description;
    public String Rs;
    public String BalanceNQT;
    public ArrayList<AssetViewModel> Assets;

    public String getBalanceNXT(){
        String result = null;

        try {
            if (BalanceNQT == null)
                result = "";
            else
                result = String.valueOf(Double.parseDouble(BalanceNQT) / 100000000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
