
package io.krumbs.sdk.starter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayOutputStream;

import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static io.krumbs.sdk.starter.StarterApplication.INGREDIENT;
import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URI;
import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URL;
import io.krumbs.sdk.starter.Adapter.IngredientAdapter;

/**
 * Created by baconleung on 2/16/17.
 */

public class CaptureActivity extends AppCompatActivity {
 //   private TextView mDisplayText;
    private ProgressBar mProgressBar;
    private Button mButtonReturn;
    private Button mButtonAnalyze;
    private ImageView mImageView;
    private RecyclerView mIngredientList;
    private IngredientAdapter mAdapter = new IngredientAdapter();

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        /* Typical usage of findViewById... */
        mProgressBar = (ProgressBar) findViewById(R.id.pb_processing);
        mButtonReturn = (Button) findViewById(R.id.bn_return);
        mButtonAnalyze = (Button) findViewById(R.id.bn_analyze);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mIngredientList = (RecyclerView) findViewById(R.id.rv_prediction);

        //Setting RecyclerView
        mProgressBar.setVisibility(View.VISIBLE);
        mIngredientList.setLayoutManager(new LinearLayoutManager(this));
        mIngredientList.setAdapter(mAdapter);

        setUpItemTouchHelper();
        handleImage();
        initReturnButton();
        initAnalyzeButton();
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

    //Setting ANALYZE Button
    //When user clicks this button, current page will forward to the AnalyzeNutritionActivity
    private void initAnalyzeButton(){
        mButtonAnalyze.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = CaptureActivity.this;
                Class destinationActivity = io.krumbs.sdk.starter.AnalyzeNutritionActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
//TODO need modify
                List<String> ingredients;
                ingredients = new ArrayList<String>();
                for(Concept concept:mAdapter.getConcepts()){
                    ingredients.add(concept.name());
                }

                startChildActivityIntent.putStringArrayListExtra(INGREDIENT,(ArrayList<String>) ingredients);
                startActivity(startChildActivityIntent);
            }
        });
    }

    // SWIPE TO DELETE
    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            //not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.removeItem(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }
                // draw red background
                Drawable background = new ColorDrawable(Color.RED);
                background.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mIngredientList);
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
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }.execute();
    }
}
