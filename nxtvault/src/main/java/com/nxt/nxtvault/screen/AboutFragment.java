package com.nxt.nxtvault.screen;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nxt.nxtvault.R;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Brandon on 4/6/2015.
 */
public class AboutFragment extends BaseFragment {
    TextView txtVersion;
    TextView txtBuildDate;
    TextView txtCreatedBy;
    TextView txtSpecialThanks;
    TextView txtSupport;

    TextView lblVersion;
    TextView lblBuildDate;
    TextView lblCreatedBy;
    TextView lblSpecialThanks;
    TextView lblSupport;

    @Override
    protected View inflateView(LayoutInflater inflater, @Nullable ViewGroup container) {
        return LayoutInflater.from(mActivity).inflate(R.layout.fragment_about, null);
    }

    @Override
    public void onReady(View rootView, Bundle savedInstanceState) {
        super.onReady(rootView, savedInstanceState);

        setHasOptionsMenu(true);
        showBackButton(true);

        lblVersion = (TextView) rootView.findViewById(R.id.lblVersion);
        lblBuildDate = (TextView) rootView.findViewById(R.id.lblBuildDate);
        lblCreatedBy = (TextView) rootView.findViewById(R.id.lblCreatedBy);
        lblSpecialThanks = (TextView) rootView.findViewById(R.id.lblSpecialThanks);
        lblSupport = (TextView) rootView.findViewById(R.id.lblSupport);

        txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);
        txtBuildDate = (TextView) rootView.findViewById(R.id.txtBuildDate);
        txtCreatedBy = (TextView) rootView.findViewById(R.id.txtCreatedBy);
        txtSpecialThanks = (TextView) rootView.findViewById(R.id.txtSpecialThanks);
        txtSupport = (TextView) rootView.findViewById(R.id.txtSupport);

        lblVersion.setTypeface(getMainActivity().segoe);
        lblBuildDate.setTypeface(getMainActivity().segoe);
        lblCreatedBy.setTypeface(getMainActivity().segoe);
        lblSpecialThanks.setTypeface(getMainActivity().segoe);
        lblSupport.setTypeface(getMainActivity().segoe);
        txtVersion.setTypeface(getMainActivity().segoe);
        txtBuildDate.setTypeface(getMainActivity().segoe);
        txtCreatedBy.setTypeface(getMainActivity().segoe);
        txtSpecialThanks.setTypeface(getMainActivity().segoe);
        txtSupport.setTypeface(getMainActivity().segoe);


        hydrate(rootView);
    }

    private void hydrate(View rootView) {
        //get app version number
        try {
            PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            String version = pInfo.versionName;
            txtVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Get the build date/time
        try{
            ApplicationInfo ai = mActivity.getPackageManager().getApplicationInfo(mActivity.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            String s = SimpleDateFormat.getInstance().format(new java.util.Date(time));
            zf.close();
            txtBuildDate.setText(s);
        }catch(Exception e){
        }

        txtCreatedBy.setText("_mr_e");
        txtSpecialThanks.setText("jones");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(false);
        menu.findItem(R.id.action_change_pin).setVisible(false);
    }
}
