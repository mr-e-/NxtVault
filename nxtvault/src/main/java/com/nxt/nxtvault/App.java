package com.nxt.nxtvault;

import android.app.Application;

import com.nxt.nxtvault.modules.AppModule;

import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import dagger.ObjectGraph;

/**
 * Created by bcollins on 2015-04-28.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "nxtvault@gmail.com",
        customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class App extends Application {
    ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Dagger
        AppModule appModule = new AppModule(this);
        mObjectGraph = ObjectGraph.create(appModule);

        //ACRA.init(this);
    }

    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
}
