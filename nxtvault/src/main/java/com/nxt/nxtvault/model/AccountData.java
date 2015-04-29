package com.nxt.nxtvault.model;

import java.io.Serializable;

/**
 * Created by Brandon on 4/6/2015.
 */
public class AccountData implements Serializable{
    public String secretPhrase;
    public String publicKey;
    public String accountName;
    public String accountRS;
    public String key;
    public String cipher;
    public String checksum;
}
