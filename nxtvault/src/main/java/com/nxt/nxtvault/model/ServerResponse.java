package com.nxt.nxtvault.model;

/**
 * Created by bcollins on 2015-02-07.
 */
public class ServerResponse<T> {
    public int errorCode;
    public String errorDescription;
    public T data;
}
