/*
 * Copyright (c) 2016 Krumbs Inc.
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.net.URL;

import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.KrumbsUser;
import io.krumbs.sdk.data.model.Media;
import io.krumbs.sdk.krumbscapture.KMediaUploadListener;
import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;


public class StarterApplication extends Application {

    private static StarterApplication INSTANCE;

    //URI and URL below are used to distinguished the form of image path.
    public static final String INTENT_IMAGE_URI = "image_uri";
    public static final String INTENT_IMAGE_URL = "image_url";
    public static final String INGREDIENT = "ingredient";

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        initKrumbs();
        initClarifai();
    }

    public static StarterApplication getInstance(){
        final StarterApplication instance = INSTANCE;
        if(instance == null){
            throw new IllegalStateException("App has not been created yet!");
        }
        return instance;
    }

    ///////////////////////////////////////////////////////////////
    //Krumbs
    ///////////////////////////////////////////////////////////////
    public static final String KRUMBS_SDK_APPLICATION_ID = "io.krumbs.sdk.APPLICATION_ID";
    public static final String KRUMBS_SDK_CLIENT_KEY = "io.krumbs.sdk.CLIENT_KEY";
    public static final String SDK_STARTER_PROJECT_USER_FN = "JohnQ";
    public static final String SDK_STARTER_PROJECT_USER_SN = "Public";

    private void initKrumbs(){
        String appID = getMetadata(getApplicationContext(), KRUMBS_SDK_APPLICATION_ID);
        String clientKey = getMetadata(getApplicationContext(), KRUMBS_SDK_CLIENT_KEY);
        if (appID != null && clientKey != null) {
            // SDK usage step 1 - initialize the SDK with your application id and client key
            // Make sure the application id and client key are correctly initialized in the Manifest
            KrumbsSDK.initialize(getApplicationContext(), appID, clientKey);

// Implement the interface KMediaUploadListener.
// After a Capture completes, the media (photo and audio) is uploaded to the cloud
// KMediaUploadListener will be used to listen for various state of media upload from the SDK.
            KMediaUploadListener kMediaUploadListener = new KMediaUploadListener() {
                // onMediaUpload listens to various status of media upload to the cloud.
                @Override
                public void onMediaUpload(String id, KMediaUploadListener.MediaUploadStatus mediaUploadStatus,
                                          Media.MediaType mediaType, URL mediaUrl) {
                    if (mediaUploadStatus != null) {
                        Log.i("KRUMBS Status", mediaUploadStatus.toString());
                        if (mediaUploadStatus == KMediaUploadListener.MediaUploadStatus.UPLOAD_SUCCESS) {
                            if (mediaType != null && mediaUrl != null) {
                                Log.i("KRUMBS Media, Type: ", mediaType + ": ID:" + id + ", URL:" + mediaUrl);
                            }
                        }
                    }
                }
            };
            // pass the KMediaUploadListener object to the sdk
            KrumbsSDK.setKMediaUploadListener(this, kMediaUploadListener);

            try {
// SDK usage step 3 (optional) - register users so you can associate their ID (email) with created content with Cloud
// API
                // Register user information (if your app requires login)
                // to improve security on the mediaJSON created.
                String userEmail = DeviceUtils.getPrimaryUserID(getApplicationContext());
                KrumbsSDK.registerUser(new KrumbsUser.KrumbsUserBuilder()
                        .email(userEmail)
                        .firstName(SDK_STARTER_PROJECT_USER_FN)
                        .lastName(SDK_STARTER_PROJECT_USER_SN).build());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getMetadata(Context context, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
// if we can’t find it in the manifest, just return null
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////
    // Initialize Clarifai client
    ///////////////////////////////////////////////////////////////
    private ClarifaiClient client;

    private void initClarifai(){
        client = new ClarifaiBuilder(getString(R.string.clarifai_client_id), getString(R.string.clarifai_client_secret))
                .buildSync();
    }

    public ClarifaiClient getClarifaiClient(){
        final ClarifaiClient client = this.client;
        if(client == null){
            throw new IllegalStateException("Cannot use Clarifai client before initialized");
        }
        return client;
    }

}
