package com.nxt.nxtvault.model;

import com.google.gson.annotations.SerializedName;
import com.nxt.nxtvaultclientlib.nxtvault.model.NrsResult;

/**
 * Created by bcollins on 2015-02-07.
 */
public class BroadcastTxResponse extends NrsResult {
    @SerializedName("requestProcessingTime")
    public String RequestProcessingTime;
    @SerializedName("fullHash")
    public String FullHash;
    @SerializedName("transaction")
    public String Transaction;
}
