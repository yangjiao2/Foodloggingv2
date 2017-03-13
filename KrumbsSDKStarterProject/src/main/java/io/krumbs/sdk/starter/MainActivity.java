/*
 * Copyright (c) 2016 Krumbs Inc
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import com.google.android.gms.maps.MapView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.util.Map;

import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.dashboard.KDashboardFragment;
import io.krumbs.sdk.dashboard.KGadgetDataTimePeriod;
import io.krumbs.sdk.dashboard.KGadgetType;
import io.krumbs.sdk.data.model.Event;
import io.krumbs.sdk.krumbscapture.KCaptureCompleteListener;
import io.krumbs.sdk.krumbscapture.settings.KUserPreferences;
import io.krumbs.sdk.starter.Adapter.FoodlogHistoryAdapter;
import io.krumbs.sdk.starter.Data.DbContract;
import io.krumbs.sdk.starter.Data.FoodlogDbHelper;

import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class MainActivity extends AppCompatActivity implements KrumbsSDK.KCaptureReadyCallback {

    private KGadgetDataTimePeriod defaultInitialTimePeriod = KGadgetDataTimePeriod.TODAY;
    private KDashboardFragment kDashboard;
    private View startCaptureButton;
    private RecyclerView mRVAllFoodlogHistory;
    private CalendarView mCalendar;
    private Toolbar mToolbar;
    private FloatingActionButton mFABGallery;

    private FoodlogHistoryAdapter mAdapter;
    private SQLiteDatabase mDb;
    private String mDate = "";

    private static final int SELECT_PICTURE = 212;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //preloadMaps();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            startActivity(new Intent(this, EmailPasswordActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);


        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mFABGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        startCaptureButton = findViewById(R.id.start_report_button);
        mRVAllFoodlogHistory = (RecyclerView) findViewById(R.id.rv_all_foodlog_history);
        mCalendar = (CalendarView) findViewById(R.id.cv_calendar);

        //setting the toolbar
        setSupportActionBar(mToolbar);

        //setting the dashboard, only MAP shows up
        //We don't need the map
/*        if (savedInstanceState == null) {
            kDashboard = buildDashboard();
            getSupportFragmentManager().beginTransaction().replace(R.id.content, kDashboard).commit();
        }*/

        //setting Krumbs's preference , I don't know why Krumbs needs this.
/*        KrumbsSDK.setUserPreferences(
                new KUserPreferences.KUserPreferencesBuilder().audioRecordingEnabled(true).build());*/

        initButton();

        initFoodlogRecyclerView();

        initCalendar();

        setupSwipeDelete();

        // It is REQUIRED to set this Callback for Krumbs Capture to Work.
        // You can  invoke KrumbsSDK.startCapture only when this callback returns. Not setting this correctly will
        // result in exceptions. Also note that the startCaptureButton is hidden until this callback returns.
        KrumbsSDK.setKCaptureReadyCallback(this);
    }

    private void initButton(){
        //setting the capture button
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
        mFABGallery.setVisibility(View.VISIBLE);
        mFABGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClickFABGallery();
            }
        });
    }

    private void initFoodlogRecyclerView(){
        //setting the recyclerView, showing all foodlog history
        //set layout for the RecyclerView, because it's a list we are using the linear layout
        mRVAllFoodlogHistory.setLayoutManager(new LinearLayoutManager(this));
        //Create a DB helper
        FoodlogDbHelper dbHelper = new FoodlogDbHelper(this);
        // Keep a reference to the mDb until paused or killed. Get a writable database
        mDb = dbHelper.getWritableDatabase();
        //Get all foodlog history from the database and save in a cursor
        mDate = new Date(mCalendar.getDate()).toString();
        Cursor cursor = getFoodlogForChosenDate(mDate);
        // Create an adapter for that cursor to display the data
        mAdapter = new FoodlogHistoryAdapter(cursor);
        //Link the adapter to the RecyclerView
        mRVAllFoodlogHistory.setAdapter(mAdapter);
    }

    private void initCalendar(){
        mCalendar.setFirstDayOfWeek(2);
        mCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            /*
            * month [0,11]
            * day of Month [1,31]
            * */
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                //Get all foodlog from the database and save in a cursor
                //Build the  YYYY-MM-DD format
                String date = String.valueOf(year) + "-" ;
                month++;
                if(month<10) date+="0";
                date+=String.valueOf(month) + "-";
                if(dayOfMonth<10) date+="0";
                date+=String.valueOf(dayOfMonth);

                mDate = date;
                Cursor cursor = getFoodlogForChosenDate(mDate);
                //Update the data in Foodlog RecyclerView
                mAdapter.swapCursor(cursor);

                Toast.makeText(getApplicationContext(),mDate, Toast.LENGTH_SHORT).show();
            }
        });
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

    }

    //We don't need so much intent
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

                    /*TODO switch to the CaptureActivity with INTENT_IMAGE_URL*/
                      switchToCaptureActivity(Uri.fromFile(new File(imagePath)).toString());


                } else if (completionState == CompletionState.CAPTURE_CANCELLED ||
                        completionState == CompletionState.SDK_NOT_INITIALIZED) {
                }
            }
        });
    }

/*
* Do not delete or comment the onSaveInstanceState() function
* */
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

    /*
    * Receive result returned by other intent.
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //handle the image result returned from the ImageGallery
        if (resultCode == RESULT_OK) {
            //The intent is chossing picture from gallery.
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Change to child activity
                    /*TODO switch to the CaptureActivity with INTENT_IMAGE_URI*/
                    switchToCaptureActivity(selectedImageUri.toString());
                }
            }
        }
    }

    private void switchToCaptureActivity(String image_uri){
        Context context = MainActivity.this;
        Class destinationActivity = CaptureActivity.class;
        Intent startChildActivityIntent = new Intent(context, destinationActivity);
        startChildActivityIntent.putExtra(INTENT_IMAGE_URI, image_uri);
        startActivity(startChildActivityIntent);
    }

    /*
    * SQLitedatabase.query
    * @param table:String  The table name to compile the query against.
    * @param columns:String[]  A list of which columns to return. Passing null will return all
    *        columns, which is discouraged to prevent reading data from storage that isn't going
    *        to be used.
    * @param selection:String A filter declaring which rows to return, formatted as an SQL WHERE
     *       clause (excluding the WHERE itself). Passing null will return all rows for the given
      *       table.
    * @param selectionArgs:String[] You may include ?s in selection, which will be replaced by
    *        the values from selectionArgs, in order that they appear in the selection. The values
    *        will be bound as Strings.
    * @param groupBy:String A filter declaring how to group rows, formatted as an SQL GROUP BY
    *        clause (excluding the GROUP BY itself). Passing null will cause the rows to not be
    *        grouped.
    * @param having:String A filter declare which row groups to include in the cursor, if row
    *        grouping is being used, formatted as an SQL HAVING clause (excluding the HAVING
    *        itself). Passing null will cause all row groups to be included, and is required when
     *        row grouping is not being used.
    * @param orderBy:String How to order the rows, formatted as an SQL ORDER BY clause (excluding
    *        the ORDER BY itself). Passing null will use the default sort order, which may be
     *        unordered.
    * */

    //@param date : YYYY-MM-DD
    private Cursor getFoodlogForChosenDate(String date){

        String sortOrder = DbContract.FoodlogEntry.COLUMN_DATE + " DESC ";
        // { date = 'YYYY-MM-DD' }
        String selection = DbContract.FoodlogEntry.COLUMN_DATE + "= '" + date + "'";
        return mDb.query(
                DbContract.FoodlogEntry.TABLE_NAME,
                null,
                selection,    //where date = "YYYY-MM-DD"
                null,
                null,
                null,
                sortOrder
        );
    }

    private void setupSwipeDelete(){
        // Create a new ItemTouchHelper with a SimpleCallback that handles LEFT swipe directions
        // Create an item touch helper to handle swiping items off the list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }

             @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //get the id of the item being swiped
                long id = (long) viewHolder.itemView.getTag();
                //remove from DB
                removeFoodlog(id);
                //update the list
                mAdapter.swapCursor(getFoodlogForChosenDate(mDate));
            }

            //attach the ItemTouchHelper to the waitlistRecyclerView
        }).attachToRecyclerView(mRVAllFoodlogHistory);
    }

    /**
     * Removes the record with the specified id
     *
     * @param id the DB id to be removed
     * @return True: if removed successfully, False: if failed
     */
    private boolean removeFoodlog(long id) {
        return mDb.delete(DbContract.FoodlogEntry.TABLE_NAME, DbContract.FoodlogEntry._ID + "=" + id, null) > 0;
    }
}
