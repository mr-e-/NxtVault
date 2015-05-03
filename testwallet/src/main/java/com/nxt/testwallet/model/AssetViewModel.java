package com.nxt.testwallet.model;

import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;

import java.util.Comparator;

/**
 * Created by bcollins on 2015-04-22.
 */
public class AssetViewModel implements Comparable<AssetViewModel> {
    private Asset mAsset;

    public AssetViewModel(Asset asset, long balance){
        mAsset = asset;
        Name = asset.Name;
        Decimals = asset.Decimals;
        AssetId = asset.AssetId;
        BalanceNQT = balance;
    }

    public String Name;
    public long BalanceNQT;
    public String AssetId;
    public long Decimals;

    public double getBalance(){
         return BalanceNQT / Math.pow(10, Decimals);
    }
    public Asset getAsset(){return mAsset;}

    @Override
    public String toString() {
        return Name;
    }

    @Override
    public int compareTo(AssetViewModel another) {
        return AssetId.compareTo(another.AssetId);
    }
}
