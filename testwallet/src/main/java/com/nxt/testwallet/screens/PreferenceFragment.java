package com.nxt.testwallet.screens;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.nxt.testwallet.MainActivity;
import com.nxt.testwallet.R;

/**
 *  on 4/19/2015.
 */
public class PreferenceFragment extends com.github.machinarius.preferencefragment.PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(getString(R.string.preference_file_key));
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_settings).setVisible(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((MainActivity)getActivity()).setServerInfo();
    }
}
