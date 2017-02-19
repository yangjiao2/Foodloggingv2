/*
 * Copyright (c) 2016 Krumbs Inc
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import com.google.android.gms.maps.MapView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Map;

import bolts.Capture;
import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.dashboard.KDashboardFragment;
import io.krumbs.sdk.dashboard.KGadgetDataTimePeriod;
import io.krumbs.sdk.dashboard.KGadgetType;
import io.krumbs.sdk.data.model.Event;
import io.krumbs.sdk.krumbscapture.KCaptureCompleteListener;
import io.krumbs.sdk.krumbscapture.settings.KUserPreferences;

import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URI;
import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URL;


public class MainActivity extends AppCompatActivity implements KrumbsSDK.KCaptureReadyCallback {
    private KGadgetDataTimePeriod defaultInitialTimePeriod = KGadgetDataTimePeriod.TODAY;
    private KDashboardFragment kDashboard;
    private View startCaptureButton;
    private FloatingActionButton mFABGallery;

    private static final int SELECT_PICTURE = 212;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preloadMaps();

        setContentView(R.layout.app_bar_main);
        //setting the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            kDashboard = buildDashboard();
            getSupportFragmentManager().beginTransaction().replace(R.id.content, kDashboard).commit();
        }
        KrumbsSDK.setUserPreferences(
                new KUserPreferences.KUserPreferencesBuilder().audioRecordingEnabled(true).build());
        //setting the capture button
        startCaptureButton = findViewById(R.id.start_report_button);
        startCaptureButton.setEnabled(false);
        startCaptureButton.setVisibility(View.INVISIBLE);
        if (startCaptureButton != null) {
            startCaptureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //set FAB_Gallery invisible
                    mFABGallery.setVisibility(View.INVISIBLE);
                    startCapture();
                }
            });
        }

        //setting the gallery button
        mFABGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        mFABGallery.setVisibility(View.VISIBLE);
        mFABGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClickFABGallery();
            }
        });

        // It is REQUIRED to set this Callback for Krumbs Capture to Work.
        // You can  invoke KrumbsSDK.startCapture only when this callback returns. Not setting this correctly will
        // result in exceptions. Also note that the startCaptureButton is hidden until this callback returns.
        KrumbsSDK.setKCaptureReadyCallback(this);
    }

    private void preloadMaps() {
        // hack to load mapsgadget faster: http://stackoverflow
        // .com/questions/26265526/what-makes-my-map-fragment-loading-slow
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                } catch (Exception ignored){
                    Log.e("KRUMBS-ERROR", "error while init maps/ google play serv");
                }
            }
        });
        // alternatively: http://stackoverflow.com/questions/26178212/first-launch-of-activity-with-google-maps-is-very-slow

    }

    private KDashboardFragment buildDashboard() {
        return new KDashboardFragment.KDashboardBuilder()
                .addGadget(KGadgetType.REPORTS)
                .addGadget(KGadgetType.PEOPLE)
                .addGadget(KGadgetType.TOP_INTENTS)
                .addGadget(KGadgetType.TOP_PLACES)
                .timePeriod(defaultInitialTimePeriod).build();

    }


    private void startCapture() {
        int containerId = R.id.camera_container;
// SDK usage step 4 - Start the K-Capture component and add a listener to handle returned images and URLs
        KrumbsSDK.startCapture(containerId, this, new KCaptureCompleteListener() {
            @Override
            public void captureCompleted(CompletionState completionState, boolean audioCaptured,
                                         Map<String, Object> map) {
                if (completionState != null) {
                    Log.i("KRUMBS-CALLBACK", "STATUS" + ": " + completionState.toString());
                }
                if (completionState == CompletionState.CAPTURE_SUCCESS) {
// The local image url for your capture
                    String imagePath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_IMAGE_PATH);
                    if (audioCaptured) {
// The local audio url for your capture (if user decided to record audio)
                        String audioPath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_AUDIO_PATH);
                        Log.i("KRUMBS-CALLBACK", audioPath);
                    }
// The mediaJSON url for your capture
                    String mediaJSONUrl = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_JSON_URL);
                    Log.i("KRUMBS-CALLBACK", mediaJSONUrl + ", " + imagePath);
                    if (map.containsKey(KCaptureCompleteListener.CAPTURE_EVENT)) {
                        Event ev = (Event) map.get(KCaptureCompleteListener.CAPTURE_EVENT);
                        Log.i("KRUMBS-CALLBACK", "Event captured = " + ev.objectId());
                    }

                    /*TODO send the image to the CaptureActivity*/
                    Context context = MainActivity.this;
                    Class destinationActivity = CaptureActivity.class;
                    Intent startChildActivityIntent = new Intent(context, destinationActivity);
                    startChildActivityIntent.putExtra(INTENT_IMAGE_URL, imagePath);
                    startActivity(startChildActivityIntent);


                } else if (completionState == CompletionState.CAPTURE_CANCELLED ||
                        completionState == CompletionState.SDK_NOT_INITIALIZED) {
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        switch (defaultInitialTimePeriod) {
            case TODAY:
                menu.findItem(R.id.last_day).setChecked(true);
                break;
            case LAST_24_HOURS:
                menu.findItem(R.id.last_24h).setChecked(true);
                break;
            case LAST_30_DAYS:
                menu.findItem(R.id.last_month).setChecked(true);
                break;
            case LAST_12_MONTHS:
                menu.findItem(R.id.last_year).setChecked(true);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.last_day:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.TODAY;
                break;
            case R.id.last_24h:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_24_HOURS;
                break;
            case R.id.last_month:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_30_DAYS;
                break;
            case R.id.last_year:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_12_MONTHS;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        //send notification to the SDK to update the Dashboard
        if (kDashboard != null) {
            kDashboard.refreshDashboard(defaultInitialTimePeriod);
        }
        return true;
    }

    //    http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onKCaptureReady() {
        if (startCaptureButton != null) {
            startCaptureButton.setVisibility(View.VISIBLE);
            startCaptureButton.setEnabled(true);
        }
    }


    public void onClickFABGallery(){
        /*TODO send the image chosen from gallery to the CaptureActivity*/
        //Open image Gallery
        Intent intentGallery = new Intent();
        intentGallery.setType("image/*");
        intentGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentGallery,"Select Picture"),SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle the image result returned from the ImageGallery
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Change to child activity
                    Context context = MainActivity.this;
                    Class destinationActivity = CaptureActivity.class;
                    Intent startChildActivityIntent = new Intent(context, destinationActivity);
                    startChildActivityIntent.putExtra(INTENT_IMAGE_URI, selectedImageUri.toString());
                    startActivity(startChildActivityIntent);
                }
            }
        }
    }
}
