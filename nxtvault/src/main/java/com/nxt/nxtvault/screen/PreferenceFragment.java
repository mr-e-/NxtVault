package com.nxt.nxtvault.screen;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.nxt.nxtvault.MainActivity;
import com.nxt.nxtvault.R;

/**
 * Created by Brandon on 4/19/2015.
 */
public class PreferenceFragment extends com.github.machinarius.preferencefragment.PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        menu.findItem(R.id.action_about).setVisible(false);
        menu.findItem(R.id.action_change_pin).setVisible(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((MainActivity)getActivity()).setServerInfo();
    }
}
