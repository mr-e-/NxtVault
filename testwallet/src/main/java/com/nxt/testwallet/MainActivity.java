package com.nxt.testwallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxt.nxtvaultclientlib.jay.IJavascriptLoadedListener;
import com.nxt.nxtvaultclientlib.jay.RequestMethods;
import com.nxt.nxtvaultclientlib.nxtvault.BaseVaultActivity;
import com.nxt.nxtvaultclientlib.nxtvault.NxtVault;
import com.nxt.nxtvaultclientlib.nxtvault.model.Account;
import com.nxt.nxtvaultclientlib.nxtvault.model.AccountSelectionResult;
import com.nxt.nxtvaultclientlib.nxtvault.model.Asset;
import com.nxt.nxtvaultclientlib.nxtvault.model.PreferredServerResult;
import com.nxt.testwallet.model.AccountViewModel;
import com.nxt.testwallet.model.AssetViewModel;
import com.nxt.testwallet.screens.AssetTransferFragment;
import com.nxt.testwallet.screens.BalanceFragment;
import com.nxt.testwallet.screens.BaseFragment;
import com.nxt.testwallet.screens.PaymentFragment;
import com.nxt.testwallet.screens.PreferenceFragment;
import com.nxt.testwallet.slidingTab.SlidingTabLayout;

import java.util.ArrayList;


public class MainActivity extends BaseVaultActivity {
    private static final boolean mIsTestnet = true;

    SharedPreferences sharedPref;

    NxtVault mNxtVault;

    BaseFragment mCurrentFragment;

    ViewPager mViewPager;

    private AccountViewModel mAccountInfo;
    private AccountSelectionResult mNxtVaultAccount;
    private ArrayList<Asset> mAssetList;

    Gson gson = new Gson();

    private Object syncLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        mNxtVault = new NxtVault();

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPref.getString("account", null) != null) {
            mNxtVaultAccount = gson.fromJson(sharedPref.getString("account", null), AccountSelectionResult.class);
        }

        initializeJay(Uri.parse("file:///android_asset/jayClient/request.html"));
    }

    @Override
    protected void initializeJay(Uri url) {
        jay = new JayClientApi(this, url);
        jay.addReadyListener(new IJavascriptLoadedListener() {
            @Override
            public void onLoaded() {
                //Fired when the webview finished loading. You cannot make any Jay requests
                //before this event is fired
                jayLoaded();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void navigateSettings() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.addToBackStack(null);

        //transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        transaction.replace(R.id.main, new PreferenceFragment()).commit();
    }

    private void createAccount() {
        mNxtVault.requestAccount(this);
    }

    public NxtVault getNxtVault() {
        return mNxtVault;
    }

    public String getAccessToken() {
        return mNxtVaultAccount.AccessToken;
    }

    public ArrayList<Asset> getAssetList() {
        return mAssetList;
    }

    public AccountViewModel getCurrentAccount() {
        return mAccountInfo;
    }

    public void setServerInfo() {
        if (getCustomServer() != null && !getCustomServer().isEmpty()) {
            getJay().setNode(getCustomServer());
            getJay().setRequestMethod(RequestMethods.Single);
            getJay().setIsTestnet(getIsTestNet());
        } else {
            getJay().setRequestMethod(RequestMethods.Fastest);
            getJay().setIsTestnet(false);
        }
    }

    public boolean getIsTestNet() {
        return sharedPref.getBoolean(getString(R.string.testnet_preference), false);
    }

    public String getCustomServer() {
        return sharedPref.getString(getString(R.string.server_preference), null);
    }

    @Override
    protected void jayLoaded() {
        super.jayLoaded();

        mNxtVault.requestPreferredServer(this);

        setServerInfo();

        //set up the tab strip
        // Give the SlidingTabLayout the ViewPager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        // Center the tabs in the layout
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);
        slidingTabLayout.setOnPageChangeListener(pageChangedListener);

        loadAssets(new ValueCallback<ArrayList>() {
                    @Override
                    public void onReceiveValue(ArrayList value) {
                        loadAccountData();
                    }
                });
    }

    private void loadAssets(final ValueCallback<ArrayList> assetListCallback) {
        //Needs to be updated to occasionally load new assets
        if (sharedPref.getString("assets", null) == null) {
            getJay().getAllAssets(new ValueCallback<ArrayList<Asset>>() {
                @Override
                public void onReceiveValue(ArrayList<Asset> assets) {
                    if (assets != null) {
                        sharedPref.edit().putString("assets", gson.toJson(assets)).apply();

                        synchronized (syncLock) {
                            mAssetList = assets;
                        }
                    }

                    assetListCallback.onReceiveValue(mAssetList);
                }
            });
        } else {
            mAssetList = gson.fromJson(sharedPref.getString("assets", null), new TypeToken<ArrayList<Asset>>() {
            }.getType());
            assetListCallback.onReceiveValue(mAssetList);
        }
    }

    private void loadAccountData() {
        setServerInfo();

        if (mNxtVaultAccount != null) {
            //Load account information from NRS
            getJay().getAccount(mNxtVaultAccount.AccountRs, new ValueCallback<Account>() {
                @Override
                public void onReceiveValue(final Account account) {
                    if (account != null && account.ErrorCode == 0) {
                        mAccountInfo = new AccountViewModel(account);

                        if (account.Assets != null && account.Assets.size() > 0) {
                            loadAssetsRecursive(account.Assets, 0);
                        }
                        else {
                            updateCurrentPage();
                        }
                    } else {
                        mAccountInfo = new AccountViewModel(new Account());

                        String errorMessage = "";
                        if (account == null){
                            if (getCustomServer() != null && !getCustomServer().isEmpty()){
                                errorMessage = "Problem loading account information. You may have entered an invalid custom server.";
                            }
                            else {
                                errorMessage = "Unknown error. Please try again later";
                            }
                        }
                        else{
                            errorMessage = "ErrorCode: " +  account.ErrorCode + " - '" + account.ErrorDescription + "'";
                        }

                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                //if we run async and start too many threads some of the calls will fail. loading 1 asset at a time for now.
                private void loadAssetsRecursive(final ArrayList<Account.Asset> assets, final int i) {
                    final Account.Asset current = assets.get(i);

                    getJay().getAsset(current.AssetId, new ValueCallback<Asset>() {
                        @Override
                        public void onReceiveValue(Asset value) {
                            if (value != null) {
                                mAccountInfo.Assets.add(new AssetViewModel(value, current.BalanceQNT));
                            }

                            int next = i + 1;
                            if (next == assets.size()) {
                                updateCurrentPage();
                            }
                            else{
                                loadAssetsRecursive(assets, next);
                            }
                        }
                    });
                }
            });
        }
    }

    private void updateCurrentPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mNxtVaultAccount != null) {
                    mCurrentFragment = (BaseFragment) ((PagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(mViewPager.getCurrentItem());

                    if (mCurrentFragment != null)
                        mCurrentFragment.loadData();
                }
            }
        });
    }

    @Override
    protected void onAccountRequestResult(boolean success, AccountSelectionResult accountSelectionResult) {
        super.onAccountRequestResult(success, accountSelectionResult);

        if (success) {
            sharedPref.edit().putString("account", gson.toJson(accountSelectionResult)).apply();

            mNxtVaultAccount = accountSelectionResult;

            if (mIsJayLoaded)
                loadAccountData();
        } else {
            Toast.makeText(this, "Unable to add account", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSignAndBroadcast(boolean success, String message) {
        super.onSignAndBroadcast(success, message);

        if (success) {
            Toast.makeText(this, "Transaction Sent Successfully!", Toast.LENGTH_LONG).show();

            loadAccountData();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onRequestPreferredServer(PreferredServerResult result) {
        //pull the server information from nxt vault
        sharedPref.edit().putString(getString(R.string.server_preference), result.PreferredServer).apply();
        sharedPref.edit().putBoolean(getString(R.string.testnet_preference), result.IsTestNet).apply();

        setServerInfo();

        super.onRequestPreferredServer(result);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_account) {
            createAccount();
        } else if (id == R.id.action_settings) {
            navigateSettings();
        }

        return true;
    }

    ViewPager.OnPageChangeListener pageChangedListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            updateCurrentPage();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    class PagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        private final String[] TITLES = {"Wallet", "Payment", "Asset"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BalanceFragment.getInstance();
                case 1:
                    return PaymentFragment.getInstance();
                case 2:
                    return AssetTransferFragment.getInstance();
            }

            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseFragment fragment = (BaseFragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
