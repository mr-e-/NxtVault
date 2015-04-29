package com.nxt.nxtvault;

import java.io.Serializable;

/**
 * Created by bcollins on 2015-04-17.
 */
public class TransactionLineItem implements Serializable {
    public String LineItemTitle;
    public String LineItem;
    public float LineAmount;

    public TransactionLineItem(){

    }

    public TransactionLineItem(String lineItemTitle, String lineItem, float lineAmount){
        LineItemTitle = lineItemTitle;
        LineItem = lineItem;
        LineAmount = lineAmount;
    }
}
