package com.nxt.nxtvaultclientlib.nxtvault.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  on 4/21/2015.
 */
public class Account extends NrsResult {
    @SerializedName("name")
    public String Name;

    @SerializedName("description")
    public String Description;

    @SerializedName("unconfirmedBalanceNQT")
    public String BalanceNQT;

    @SerializedName("accountRS")
    public String Rs;

    @SerializedName("unconfirmedAssetBalances")
    public ArrayList<Asset> Assets;

    public class Asset{
        @SerializedName("unconfirmedBalanceQNT")
        public long BalanceQNT;
        @SerializedName("asset")
        public String AssetId;
        @SerializedName("name")
        public String Name;
        @SerializedName("decimals")
        public long Decimals;
    }
}
