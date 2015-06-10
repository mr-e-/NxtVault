package com.nxt.nxtvault;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.webkit.ValueCallback;

import com.google.gson.Gson;
import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.framework.PinManager;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvault.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bcollins on 2015-06-01.
 */
public class AccountManagerTest extends InstrumentationTestCase {
    private static final String KEY_SP_PACKAGE = "AccountsTest";
    Gson gson = new Gson();
    private AccountManager mAccountManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final CountDownLatch lock = new CountDownLatch(1);

        SharedPreferences sharedPreferences = getInstrumentation().getTargetContext().getSharedPreferences(KEY_SP_PACKAGE, Context.MODE_PRIVATE);
        PreferenceManager preferenceManager = new PreferenceManager(getInstrumentation().getTargetContext(), sharedPreferences);

        PinManager pinManager = new PinManager(preferenceManager);
        pinManager.changePin("7777");

        mAccountManager = new AccountManager(getInstrumentation().getTargetContext(), new JayClientApi(getInstrumentation().getTargetContext()), pinManager, preferenceManager);

        ArrayList<AccountData> accountsList = new ArrayList<>();
        AccountData accountData = new AccountData();
        accountData.accountName = "Brandon";
        accountsList.add(accountData);

        AccountData accountData2 = new AccountData();
        accountData2.accountName = "rawr";
        accountsList.add(accountData2);

        sharedPreferences.edit().putString("accounts", gson.toJson(accountsList)).commit();

        lock.await();
    }

    @MediumTest
    public void testAccountsAreReturned(){
        ArrayList<AccountData> accounts = mAccountManager.getAllAccounts();

        assertNotNull(accounts);
        assertTrue(accounts.size() == 2);
        assertTrue(accounts.get(0).accountName.equals("Brandon"));
        assertTrue(accounts.get(1).accountName.equals("rawr"));
    }

    @MediumTest
    public void testNewAccountCreated() throws Throwable{
        final CountDownLatch lock = new CountDownLatch(1);

        mAccountManager.getNewAccount("1111", "", new ValueCallback<AccountData>() {
            @Override
            public void onReceiveValue(AccountData value) {
                assertNotNull(value);
                assertTrue(value.secretPhrase.length() > 10);

                lock.countDown();
            }
        });

        lock.await(30, TimeUnit.SECONDS);
    }
}
