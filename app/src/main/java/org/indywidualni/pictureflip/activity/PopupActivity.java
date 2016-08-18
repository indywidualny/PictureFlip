package org.indywidualni.pictureflip.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;

import org.indywidualni.pictureflip.Constant;
import org.indywidualni.pictureflip.R;
import org.indywidualni.pictureflip.util.KeepAliveService;
import org.indywidualni.pictureflip.util.TransformationTask;

import java.io.File;

public class PopupActivity extends Activity implements View.OnTouchListener, View.OnClickListener,
        TransformationTask.TaskStatus {

    public static final String TAG = PopupActivity.class.getSimpleName();

    private CoordinatorLayout baseLayout;
    private ImageView popupImage;

    private InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences preferences;

    private File sourceFile;
    private File outputFile;

    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;

    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        baseLayout = (CoordinatorLayout) findViewById(R.id.base_popup_layout);
        popupImage = (ImageView) findViewById(R.id.popup_image);
        baseLayout.setOnTouchListener(this);

        findViewById(R.id.flip_v).setOnClickListener(this);
        findViewById(R.id.flip_h).setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
        }

        String path = null;
        Bundle extra = getIntent().getExtras();

        if (extra == null)
            finish();
        else
            path = extra.getString(Constant.PATH_EXTRA);

        if (!TextUtils.isEmpty(path)) {
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
            sourceFile = outputFile = new File(path);
            Picasso.with(this).load(sourceFile).into(popupImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    findViewById(R.id.multiple_actions).setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {
                }
            });
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!preferences.getBoolean("pref_disable_ads", false)) {
            mInterstitialAd = new InterstitialAd(this);

            mInterstitialAd.setAdUnitId("ca-app-pub-1012663270989447/1412258415");

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

            requestNewInterstitial();
        }
    }

    public boolean onTouch(View view, MotionEvent event) {

        // Get finger position on screen
        final int Y = (int) event.getRawY();

        // Switch on motion event type
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // save default base layout height
                defaultViewHeight = baseLayout.getHeight();

                // Init finger and view position
                previousFingerPosition = Y;
                baseLayoutPosition = (int) baseLayout.getY();
                break;

            case MotionEvent.ACTION_UP:
                // If user was doing a scroll up
                if (isScrollingUp){
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // We are not in scrolling up mode anymore
                    isScrollingUp = false;
                }

                // If user was doing a scroll down
                if (isScrollingDown){
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // Reset base layout size
                    baseLayout.getLayoutParams().height = defaultViewHeight;
                    baseLayout.requestLayout();
                    // We are not in scrolling down mode anymore
                    isScrollingDown = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isClosing) {
                    int currentYPosition = (int) baseLayout.getY();

                    // If we scroll up
                    if (previousFingerPosition > Y) {
                        // First time android rise an event for "up" move
                        if (!isScrollingUp) {
                            isScrollingUp = true;
                        }

                        // Has user scroll down before -> view is smaller than it's default size -> resize it instead of change it position
                        if (baseLayout.getHeight()<defaultViewHeight) {
                            baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                            baseLayout.requestLayout();
                        } else {
                            // Has user scroll enough to "auto close" popup ?
                            if ((baseLayoutPosition - currentYPosition) > defaultViewHeight / 4) {
                                closeUpAndDismissDialog(currentYPosition);
                                return true;
                            }
                        }
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));

                    }
                    // If we scroll down
                    else {

                        // First time android rise an event for "down" move
                        if (!isScrollingDown) {
                            isScrollingDown = true;
                        }

                        // Has user scroll enough to "auto close" popup ?
                        if (Math.abs(baseLayoutPosition - currentYPosition) > defaultViewHeight / 2) {
                            closeDownAndDismissDialog(currentYPosition);
                            return true;
                        }

                        // Change base layout size and position (must change position because view anchor is top left corner)
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));
                        baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                        baseLayout.requestLayout();
                    }

                    // Update position
                    previousFingerPosition = Y;
                }
                break;
        }
        return true;
    }

    private void closeUpAndDismissDialog(int currentPosition){
        isClosing = true;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition, -baseLayout.getHeight());
        positionAnimator.setDuration(300);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        positionAnimator.start();
    }

    private void closeDownAndDismissDialog(int currentPosition){
        isClosing = true;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition,
                screenHeight+baseLayout.getHeight());
        positionAnimator.setDuration(300);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        positionAnimator.start();
    }

    @Override
    public void onClick(View view) {
        new TransformationTask(view.getContext(), sourceFile, outputFile, view.getId()).execute();

        // track this action
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "action");
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "transformation");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    
    @Override
    public void processingStarted() {
        bindService(new Intent(getApplicationContext(), KeepAliveService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void processingDone(boolean status) {
        if (status) {
            // show ads after a success
            if (!preferences.getBoolean("pref_disable_ads", false) && mInterstitialAd.isLoaded())
                mInterstitialAd.show();

            final View fab = findViewById(R.id.multiple_actions);
            fab.setVisibility(View.GONE);
            // this is important, refresh the file in Picasso's cache
            Picasso.with(this).invalidate(sourceFile);
            Picasso.with(this).load(sourceFile).into(popupImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    fab.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {
                }
            });

            // Track the success by Firebase Analytics.
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "action_success");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Transformation passed");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            setResult(Constant.ACTIVITY_POPUP_MODIFIED_RESULT);
        } else {
            // Track the failure by Firebase Analytics.
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "action_failure");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Transformation failed");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void showDebugInfo(final Exception exception) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PopupActivity.this, "" + exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        // Report this exception to Firebase.
        FirebaseCrash.report(exception);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

}