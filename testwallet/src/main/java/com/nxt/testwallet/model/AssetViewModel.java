package com.nxt.testwallet.model;

import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

/**
 * Created by bcollins on 2015-04-22.
 */
public class AssetViewModel {
    public AssetViewModel(Asset asset, double balance){
        Name = asset.Name;
        Decimals = asset.Decimals;
        AssetId = asset.AssetId;
        BalanceNQT = balance;
    }

    public String Name;
    public double BalanceNQT;
    public String AssetId;
    public double Decimals;

    public double getBalance(){
         return BalanceNQT / Math.pow(10, Decimals);
    }
}
