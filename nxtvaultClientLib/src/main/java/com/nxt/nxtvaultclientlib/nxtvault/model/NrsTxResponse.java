package com.nxt.nxtvaultclientlib.nxtvault.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bcollins on 2015-02-07.
 */
public class NrsTxResponse extends NrsResult {
    @SerializedName("requestProcessingTime")
    public String RequestProcessingTime;
    @SerializedName("fullHash")
    public String FullHash;
    @SerializedName("transaction")
    public String Transaction;
}