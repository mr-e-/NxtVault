package com.nxt.nxtvault.upgrade;

import android.webkit.ValueCallback;

/**
 * Created by Brandon on 5/30/2015.
 */
public interface IUpgradeTask {
    void upgrade(final ValueCallback<Void> callback);
    boolean requiresUpgrade();
}
