package com.nxt.nxtvaultclientlib.nxtvault.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Brandon on 4/21/2015.
 */
public class Account extends NrsResult {
    @SerializedName("name")
    public String Name;

    @SerializedName("description")
    public String Description;

    @SerializedName("balanceNQT")
    public String BalanceNQT;

    @SerializedName("accountRS")
    public String Rs;

    @SerializedName("assetBalances")
    public ArrayList<Asset> Assets;

    public class Asset{
        @SerializedName("balanceQNT")
        public String BalanceQNT;
        @SerializedName("asset")
        public String AssetId;
        @SerializedName("name")
        public String Name;
        @SerializedName("decimals")
        public double Decimals;
    }
}
