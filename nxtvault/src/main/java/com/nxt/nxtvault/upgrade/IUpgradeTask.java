package com.nxt.nxtvault.upgrade;

/**
 * Created by Brandon on 5/30/2015.
 */
public interface IUpgradeTask {
    void upgrade();
    boolean requiresUpgrade();
}
