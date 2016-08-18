package org.indywidualni.pictureflip.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.indywidualni.pictureflip.R;
import org.indywidualni.pictureflip.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // set a toolbar to replace the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FragmentManager fm = getFragmentManager();
        SettingsFragment fragment = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.TAG);

        if (fragment == null) {
            fragment = new SettingsFragment();
            fm.beginTransaction()
                    .replace(R.id.content_frame, fragment, SettingsFragment.TAG)
                    .commit();
        }
    }

}
