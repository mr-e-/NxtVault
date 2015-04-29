package com.nxt.nxtvaultclientlib.nxtvault.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bcollins on 2015-04-24.
 */
public class Asset extends NrsResult implements Comparable<Asset>{
    @SerializedName("asset")
    public String AssetId;
    @SerializedName("name")
    public String Name;
    @SerializedName("description")
    public String Description;
    @SerializedName("decimals")
    public double Decimals;
    @SerializedName("numberOfAccounts")
    public int numberOfAccounts;
    @SerializedName("numberOfTransfers")
    public int NumberOfTransfers;
    @SerializedName("numberOfTrades")
    public int numberOfTrades;
    @SerializedName("account")
    public String AccountId;
    @SerializedName("quantityQNT")
    public String quantityQNT;
    @SerializedName("accountRS")
    public String AccountRS;

    @Override
    public String toString() {
        return Name;
    }

    @Override
    public int compareTo(Asset another) {
        return AssetId.compareTo(another.AssetId);
    }
}
