package org.indywidualni.pictureflip.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.indywidualni.pictureflip.Constant;
import org.indywidualni.pictureflip.R;
import org.indywidualni.pictureflip.fragment.MainFragment;
import org.indywidualni.pictureflip.util.PermissionUtil;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CoordinatorLayout coordinatorLayout;

    private FirebaseAnalytics mFirebaseAnalytics;

    private IMainActivityToFragment mainFragmentCallback;

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof IMainActivityToFragment)
            mainFragmentCallback = (IMainActivityToFragment) fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setTitle(R.string.title_activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        MainFragment fragment = (MainFragment) fm.findFragmentByTag(MainFragment.TAG_FRAGMENT);

        if (fragment == null) {
            fragment = new MainFragment();
            fm.beginTransaction().replace(R.id.content_frame, fragment, MainFragment.TAG_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.ACTIVITY_POPUP_REQUEST
                && resultCode == Constant.ACTIVITY_POPUP_MODIFIED_RESULT) {
            recreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Storage permission granted");
                    if (mainFragmentCallback != null)
                        mainFragmentCallback.startLoader();
                } else {
                    Log.e(TAG, "Storage permission denied");
                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                            getString(R.string.permission_storage_missing), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    PermissionUtil.requestStoragePermission(MainActivity.this);
                                }
                            });
                    snackbar.show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainFragmentCallback = null;
    }

    public interface IMainActivityToFragment {
        void startLoader();
    }

}
