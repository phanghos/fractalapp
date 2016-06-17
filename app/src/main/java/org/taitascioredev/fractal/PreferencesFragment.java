package org.taitascioredev.fractal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by roberto on 23/11/15.
 */
public class PreferencesFragment extends PreferenceFragmentCompat {

    private static final String KEY_PREF_DISPLAY_STYLE = "pref_displayStyle";

    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        //PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String displayPref = pref.getString(KEY_PREF_DISPLAY_STYLE, "");
        Preference p = findPreference(KEY_PREF_DISPLAY_STYLE);
        if (displayPref.equals("1"))
            p.setSummary("Card");
        else
            p.setSummary("List");
    }

    @Override
    public void onResume() {
        super.onResume();
        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(KEY_PREF_DISPLAY_STYLE)) {
                    String displayPref = sharedPreferences.getString(KEY_PREF_DISPLAY_STYLE, "");
                    Preference p = findPreference(KEY_PREF_DISPLAY_STYLE);
                    if (displayPref.equals("1"))
                        p.setSummary("Card");
                    else
                        p.setSummary("List");
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(prefChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(prefChangeListener);
    }
}
