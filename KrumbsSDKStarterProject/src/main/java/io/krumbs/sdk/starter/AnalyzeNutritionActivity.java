package io.krumbs.sdk.starter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;

import io.krumbs.sdk.starter.Utilities.Nutrition;

import static io.krumbs.sdk.starter.StarterApplication.INGREDIENT;

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
            Nutrition nutrition = new Nutrition();
            if(jsonResult!=null)
                try {
                    for(int i=0;i<jsonResult.size();i++){
                        Nutrition tmp = Nutrition.getSimpleNutritionFromJson(jsonResult.get(i));
                        nutrition.add(tmp);
                    }

                    mTVCalorieValue.setText(String.valueOf(nutrition.calorie));
                    mTVWeightValue.setText(String.valueOf(nutrition.totalweight));
                    mTVFatValue.setText(String.valueOf(nutrition.fat));
                    mTVProteinValue.setText(String.valueOf(nutrition.protein));
                    mTVCarbsValue.setText(String.valueOf(nutrition.carbs));

                }catch (JSONException e){
                    e.printStackTrace();
                }
        }
    }

}
