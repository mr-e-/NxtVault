package com.nxt.nxtvault;

import android.os.Handler;
import android.test.ActivityTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.nxt.nxtvault.framework.AccountManager;
import com.nxt.nxtvault.model.AccountData;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bcollins on 2015-06-01.
 */
public class AccountManagerTest extends ActivityTestCase {
    private static final String KEY_SP_PACKAGE = "AccountsTest";
    Gson gson = new Gson();
    private AccountManager mAccountManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final CountDownLatch lock = new CountDownLatch(1);

        JayClientApi jayClient = new JayClientApi(getInstrumentation().getTargetContext());

        jayClient.addReadyListener(new IJavascriptLoadedListener() {
            @Override
            public void onLoaded() {
                lock.countDown();
            }
        });

        WebView webView = new WebView(getInstrumentation().getTargetContext());
        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public void onPageFinished(WebView view, String url) {
                                         super.onPageFinished(view, url);
                                     }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webView.loadUrl("http://www.google.com");

        Thread.sleep(10000);

        getInstrumentation().waitForIdleSync();

//        SharedPreferences sharedPreferences = getInstrumentation().getTargetContext().getSharedPreferences(KEY_SP_PACKAGE, Context.MODE_PRIVATE);
//
//        mAccountManager = new AccountManager(getInstrumentation().getTargetContext(), new JayClientApi(getInstrumentation().getTargetContext(), new IJavascriptLoadedListener() {
//            @Override
//            public void onLoaded() {
//                lock.countDown();
//            }
//        }), sharedPreferences);
//
//        ArrayList<AccountData> accountsList = new ArrayList<>();
//        AccountData accountData = new AccountData();
//        accountData.accountName = "Brandon";
//        accountsList.add(accountData);
//
//        AccountData accountData2 = new AccountData();
//        accountData2.accountName = "rawr";
//        accountsList.add(accountData2);
//
//        sharedPreferences.edit().putString("accounts", gson.toJson(accountsList)).commit();
        //lock.await();
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

        mAccountManager.getNewAccount(new ValueCallback<AccountData>() {
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
