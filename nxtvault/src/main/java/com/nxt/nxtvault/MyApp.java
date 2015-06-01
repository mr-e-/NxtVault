package com.nxt.nxtvault;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by bcollins on 2015-04-28.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "nxtvault@gmail.com",
        customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MyApp extends android.app.Application {
    public JayClientApi jay;
    public static String SessionPin;

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }


}
