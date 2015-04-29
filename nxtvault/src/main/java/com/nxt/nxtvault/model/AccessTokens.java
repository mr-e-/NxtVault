package com.nxt.nxtvault.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Brandon on 4/12/2015.
 */
public class AccessTokens implements Serializable {
    public String AccountPublicKey;
    public ArrayList<String> AccessTokens;
}
