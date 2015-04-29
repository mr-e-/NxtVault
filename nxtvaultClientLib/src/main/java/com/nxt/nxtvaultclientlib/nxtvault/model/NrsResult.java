package com.nxt.nxtvaultclientlib.nxtvault.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bcollins on 2015-04-24.
 */
public abstract class NrsResult {
    @SerializedName("errorDescription")
    public String ErrorDescription;
    @SerializedName("errorCode")
    public int ErrorCode;
}
