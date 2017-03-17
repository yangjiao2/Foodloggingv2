package io.krumbs.sdk.starter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.sql.Date;

import io.krumbs.sdk.starter.Data.DbContract;
import io.krumbs.sdk.starter.Data.FoodlogDbHelper;
import io.krumbs.sdk.starter.Utilities.Nutrition;

import static io.krumbs.sdk.starter.StarterApplication.INGREDIENT;
import static io.krumbs.sdk.starter.StarterApplication.INTENT_IMAGE_URI;

/**
 * Created by baconleung on 2/26/17.
 */

public class AnalyzeNutritionActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private TextView mTVCalorieValue;
    private TextView mTVWeightValue;
    private TextView mTVFatValue;
    private TextView mTVProteinValue;
    private TextView mTVCarbsValue;
    private Button mButtonReturn;
    private SeekBar mSBChangeWeight;
    private TextView mTVDisplayWeight;
    private Nutrition mNutrition;
    private TextView mFoodScoreValue;
    private float foodscore = 100;
    private float calorie;
    private float fat;
    private float protein;
    private float carbs;

    // unit gram
    private static final int max_food_weight = 800;
    private static final int min_food_weight = 100;
    private static final int number_of_levels = 8;
    private static final float difference_of_weight = (max_food_weight - min_food_weight) / number_of_levels;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_nutrition);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_processing);
        mTVCalorieValue = (TextView) findViewById(R.id.tv_calorie_value);
        mTVWeightValue= (TextView) findViewById(R.id.tv_weight_value);
        mTVFatValue= (TextView) findViewById(R.id.tv_fat_value);
        mTVProteinValue= (TextView) findViewById(R.id.tv_protein_value);
        mTVCarbsValue= (TextView) findViewById(R.id.tv_carbs_value);
        mButtonReturn = (Button) findViewById(R.id.bn_return);
        mSBChangeWeight = (SeekBar) findViewById(R.id.sb_change_weight);
        mTVDisplayWeight = (TextView) findViewById(R.id.tv_display_weight);
        mNutrition = new Nutrition();
        mFoodScoreValue = (TextView) findViewById(R.id.foodscore_value);
        initReturnButton();
        initSeekBar();
        handleIngredients();
    }

    private void handleIngredients(){
        Intent intentThatStartedThisActivity = getIntent();
        if(intentThatStartedThisActivity.hasExtra(INGREDIENT)) {
            ArrayList<String> ingredients = intentThatStartedThisActivity.getStringArrayListExtra(INGREDIENT);
            new FetchNutritionTask().execute(ingredients);
        }
    }


    public class FetchNutritionTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            if(params.length==0) return null;
            ArrayList<String> ingredients = params[0];
            ArrayList<String> jsonResult = new ArrayList<String>();
            try {
                for(int i=0;i<ingredients.size();i++){
                    String query = Nutrition.STANDARD_WEIGHT+" "+ ingredients.get(i);
                    URL ingrRequestUrl = Nutrition.buildUrl(AnalyzeNutritionActivity.this,query);
                    String jsonIngredientResponse = Nutrition.getResponseFromHttpUrl(ingrRequestUrl);
                    jsonResult.add(jsonIngredientResponse);
                }
                return jsonResult;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(ArrayList<String> jsonResult) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mNutrition.reset();
            if(jsonResult!=null)
                try {
                    for(int i=0;i<jsonResult.size();i++){
                        Nutrition tmp = Nutrition.getSimpleNutritionFromJson(jsonResult.get(i));
                        mNutrition.add(tmp);
                    }
                    mNutrition.setWeight(max_food_weight);
                    calculateFoodscore();
                    updateDisplayNutrition();

                }catch (JSONException e){
                    e.printStackTrace();
                }
            mButtonReturn.setVisibility(View.VISIBLE);
        }
    }

    private long storeData(){
        String str_imageUri = null;
        FoodlogDbHelper dbHelper = new FoodlogDbHelper(this);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity.hasExtra(INTENT_IMAGE_URI))
            str_imageUri= intentThatStartedThisActivity.getStringExtra(INTENT_IMAGE_URI);

        ContentValues content = new ContentValues();
        //content.put(DbContract.FoodlogEntry.COLUMN_EVENT, mEvent);
        content.put(DbContract.FoodlogEntry.COLUMN_DATE, new Date(System.currentTimeMillis()).toString());
        content.put(DbContract.FoodlogEntry.COLUMN_IMAGE_URI, str_imageUri);

        content.put(DbContract.FoodlogEntry.COLUMN_FOOD_CALORIE, mNutrition.calorie);
        content.put(DbContract.FoodlogEntry.COLUMN_FOOD_TOTAL_WEIGHT, mNutrition.totalweight);
        content.put(DbContract.FoodlogEntry.COLUMN_FOOD_FAT, mNutrition.fat);
        content.put(DbContract.FoodlogEntry.COLUMN_FOOD_PROTEIN, mNutrition.protein);
        content.put(DbContract.FoodlogEntry.COLUMN_FOOD_CARBOHYDRATES, mNutrition.carbs);

        return mDb.insert(DbContract.FoodlogEntry.TABLE_NAME, null,content);
    }

    private void initReturnButton(){
        mButtonReturn.setVisibility(View.INVISIBLE);
        mButtonReturn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = AnalyzeNutritionActivity.this;
                Class destinationActivity = MainActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
                storeData();
                startActivity(startChildActivityIntent);

            }
        });
    }

    private void calculateFoodscore(){
        calorie = mNutrition.calorie;
        fat = mNutrition.fat;
        protein = mNutrition.protein;
        carbs =  mNutrition.carbs;

        if (calorie * 0.35 <= fat) {
            foodscore -= (fat - calorie * 0.35) / (calorie * 0.225);
        }else if (calorie * 0.2 >= fat) {
            foodscore += (calorie * 0.2 - fat) / (calorie * 0.225);
        }

        if (calorie *.65 <= carbs) {
            foodscore -= (carbs - calorie * 0.65) / (calorie * 0.5);
        }else if (calorie *.65 >= carbs) {
            foodscore += (calorie * 0.45 - carbs) / (calorie * 0.5);
        }

        if (calorie *.35 <= protein) {
            foodscore -= (protein - calorie * 0.35) / (calorie * 0.5);
        }else if (calorie * 0.10 >= protein) {
            foodscore += (calorie * 0.10 - protein) / (calorie * 0.5);
        }


    }

    private void initSeekBar(){
        mTVDisplayWeight.setText(String.valueOf(min_food_weight));
        mSBChangeWeight.setMax(number_of_levels);
        mSBChangeWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float increase = progress * difference_of_weight;
                int result = progress==number_of_levels? max_food_weight : min_food_weight+ (int)increase;
                mTVDisplayWeight.setText(String.valueOf(result)+"g");
                mNutrition.setWeight(result);
                updateDisplayNutrition();
                calculateFoodscore();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateDisplayNutrition(){
        mTVCalorieValue.setText(String.valueOf(mNutrition.calorie));
        mTVWeightValue.setText(String.valueOf(mNutrition.totalweight));
        mTVFatValue.setText(String.format("%.2f",mNutrition.fat));
        mTVProteinValue.setText(String.format("%.2f",mNutrition.protein));
        mTVCarbsValue.setText(String.format("%.2f",mNutrition.carbs));
        mFoodScoreValue.setText(String.format("%.2f", foodscore));
    }

}
