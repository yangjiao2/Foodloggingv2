
package io.krumbs.sdk.starter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayOutputStream;

import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URI;
import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URL;
import io.krumbs.sdk.starter.Adapter.IngredientAdapter;

/**
 * Created by baconleung on 2/16/17.
 */

public class CaptureActivity extends AppCompatActivity {
 //   private TextView mDisplayText;
    private Button mButtonReturn;
    private ImageView mImageView;
    private RecyclerView mIngredientList;
    private IngredientAdapter mAdapter = new IngredientAdapter();
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        /* Typical usage of findViewById... */
        mButtonReturn = (Button) findViewById(R.id.bn_return);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mIngredientList = (RecyclerView) findViewById(R.id.rv_prediction);

        //Setting RecyclerView
        mIngredientList.setLayoutManager(new LinearLayoutManager(this));
        mIngredientList.setAdapter(mAdapter);

        handleImage();
        initReturnButton();
    }

    //Setting RETURN Button
    //When user clicks this button, current page will return to the mainActiviy
    private void initReturnButton(){
        mButtonReturn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = CaptureActivity.this;
                Class destinationActivity = MainActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
                startActivity(startChildActivityIntent);
            }
        });
    }


    private void handleImage(){
        Intent intentThatStartedThisActivity = getIntent();
        Bitmap bitmap=null;
        //The format of image path is either URL or URI.
        if (intentThatStartedThisActivity.hasExtra(INTENT_IMAGE_URL)) {
            String imagePath = intentThatStartedThisActivity.getStringExtra(INTENT_IMAGE_URL);
            File imgFile = new File(imagePath);
            if(imgFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
        }
        else if (intentThatStartedThisActivity.hasExtra(INTENT_IMAGE_URI)){
            String str_imageUri= intentThatStartedThisActivity.getStringExtra(INTENT_IMAGE_URI);
            Uri mImageUri = Uri.parse(str_imageUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
            }catch(Exception e){
                e.printStackTrace();
            }
        }else { return ;}

        //Display image with bitmap
        mImageView.setImageBitmap(bitmap);
        //
        try{
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            final byte[] imageBytes = outStream.toByteArray();

            if(imageBytes!=null){
                onImagePicked(imageBytes);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Using Clarifai, predict image
    ///////////////////////////////////////////////////////////////////////////
    private void onImagePicked(final byte[] imageBytes) {

        // Make sure we don't show a list of old concepts while the image is being uploaded
        mAdapter.setData(Collections.<Concept>emptyList());

        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {

            //Upload the image to Clarifai server and fetch the result.
            protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
                // Get Clarifai's foodModel from ClarifaiClient
                final ConceptModel foodModel = StarterApplication
                        .getInstance()
                        .getClarifaiClient()
                        .getDefaultModels()
                        .foodModel();

                //Predict ingredients using foodModel. Predict the contents of an image via image bytes
                return foodModel.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                        .executeSync();
            }

            protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response){
                //If the prediction fails
                if(!response.isSuccessful()){
                    return ;
                }

                final List<ClarifaiOutput<Concept>> predictions = response.get();
                //If no prediction
                if(predictions.isEmpty()){
                    return;
                }

                //Here, we get predictions analyzed from the image
                //////////////////////////////////////////////////////////////////////////////
                // List<Concept>  concepts =  predictions.get(0).data());
                // final Concept concept = concepts.get(index);
                // mTextView.setText(concept.name() != null ? concept.name() : concept.id());
                //////////////////////////////////////////////////////////////////////////////
                //Set the data to the RecyclerView Adapter
                List<Concept> concepts = predictions.get(0).data();
                mAdapter.setData(concepts);
            }
        }.execute();
    }
}
