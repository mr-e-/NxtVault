package com.nxt.nxtvault.model;

import java.io.Serializable;

/**
 *  on 4/6/2015.
 */
public class AccountData implements Serializable{
    public String secretPhrase;
    public String publicKey;
    public String accountName;
    public String accountRS;
    public String cipher;
    public String checksum;
    public boolean spendingPassphrase;

    public boolean getIsSpendingPasswordEnabled(){
        return spendingPassphrase;
    }
}
