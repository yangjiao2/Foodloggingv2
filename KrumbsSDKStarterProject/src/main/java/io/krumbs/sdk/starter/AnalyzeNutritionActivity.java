package io.krumbs.sdk.starter;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by baconleung on 2/26/17.
 */

public class AnalyzeNutritionActivity extends AppCompatActivity {
    private TextView mTextViewNutrition;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_analyze_nutrition);

        mTextViewNutrition = (TextView) findViewById(R.id.tv_nutrition);
    //    handleIngredients();
    }

    private void handleIngredients(){
        Intent intentThatStartedThisActivity = getIntent();
        ArrayList<String> ingredients = intentThatStartedThisActivity.getStringArrayListExtra("INGREDIENTS");

        String displayText = new String();
        for(String str: ingredients){
            displayText+=str+"\n";
        }

        mTextViewNutrition.setText(displayText);
    }
}
