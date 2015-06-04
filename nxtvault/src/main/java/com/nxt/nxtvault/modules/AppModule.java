package com.nxt.nxtvault.modules;

import android.content.Context;

import com.nxt.nxtvault.App;
import com.nxt.nxtvault.JayClientApi;
import com.nxt.nxtvault.MainActivity;
import com.nxt.nxtvault.SignTxActivity;
import com.nxt.nxtvault.screen.AboutFragment;
import com.nxt.nxtvault.screen.AccountFragment;
import com.nxt.nxtvault.screen.BaseFragment;
import com.nxt.nxtvault.screen.ManageAccountFragment;
import com.nxt.nxtvault.screen.PreferenceFragment;
import com.nxt.nxtvault.screen.SendMoneyFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by bcollins on 2015-06-03.
 */
@Module(injects = {MainActivity.class, SignTxActivity.class, BaseFragment.class, AccountFragment.class, ManageAccountFragment.class, PreferenceFragment.class, SendMoneyFragment.class, AboutFragment.class, SignTxActivity.TxConfirmationFragment.class})
public class AppModule {
    protected App mApplication;

    public AppModule(App app){
        mApplication = app;
    }

    @Provides
    @Singleton
    public JayClientApi getJayClient(){
        return new JayClientApi(mApplication);
    }

    @Provides
    public Context getAppContext(){
        return mApplication;
    }
}
