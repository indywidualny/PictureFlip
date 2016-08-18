package org.indywidualni.pictureflip.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.indywidualni.pictureflip.Constant;
import org.indywidualni.pictureflip.R;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences preferences;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        addPreferencesFromResource(R.xml.preferences);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext());

        findPreference("pref_rate").setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_rate")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Constant.GOOGLE_PLAY_LINK));

            // Track it by Firebase Analytics.
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "pref_rate");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "preference");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Rate the app");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            return true;
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case "pref_disable_ads":
                // Track it by Firebase Analytics.
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "preference");

                if (prefs.getBoolean("pref_disable_ads", false))
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "pref_disable_ads");
                else
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "pref_enable_ads");

                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                break;
        }
    }

}
