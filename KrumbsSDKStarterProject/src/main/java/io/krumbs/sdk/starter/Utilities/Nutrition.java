package io.krumbs.sdk.starter.Utilities;

import android.net.Uri;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import io.krumbs.sdk.starter.R;

/**
 * Created by baconleung on 2/28/17.
 */

public class Nutrition {
    public static final String STANDARD_WEIGHT="100g";
    //Unit: KCal
    public int calorie;
    //Unit: gram
    public int totalweight;
    //Macro nutrition
    public float fat;
    public float protein;
    public float carbs;

    //Other nutrition
    public float saturated;
    public float monounsaturated;
    public float polyunsaturated;
    public float fiber;
    public float sugars;
    //
    public float sodium;
    public float calcium;
    public float magnesium;
    public float potassium;
    public float iron;
    public float zinc;
    public float phosphorus;
    public float vitaminA;
    public float vitaminC;


    public Nutrition(){
        calorie=0;
        totalweight=0;
        fat=0;
        protein=0;
        carbs=0;
    }

    public void reset(){
        calorie=0;
        totalweight=0;
        fat=0;
        protein=0;
        carbs=0;
    }

    public void add(Nutrition nutri){
        this.calorie+=nutri.calorie;
        this.totalweight+=nutri.totalweight;
        this.fat+=nutri.fat;
        this.protein+=nutri.protein;
        this.carbs+=nutri.carbs;
    }

    public void setWeight(int newWeight){
        float rate = newWeight / (float)totalweight;
        this.carbs = this.carbs * rate;
        this.fat = this.fat * rate;
        this.protein = this.protein * rate;
        this.calorie = (int) (this.calorie * rate);
        totalweight = newWeight;
    }

    /*Edamam Nutrition JSON format
    * calories: double
    * totalWeight: double
    * totalNutrients
    *  +FAT
    *   -label: String
    *   -quantity: double
    *   -unit: String
    *  +CHOCDF
    *   -label: String
    *   -quantity: double
    *   -unit: String
    *  +PROCNT
    *   -label: String
    *   -quantity: double
    *   -unit: String
    * */
    private static String CAL="calories";
    private static String WEIGHT="totalWeight";
    private static String NUTRITIONS="totalNutrients";
    private static String FAT="FAT";
    private static String CARBS="CHOCDF";
    private static String PROTEIN="PROCNT";
    private static String QUANTITY="quantity";

    public static Nutrition getSimpleNutritionFromJson(String nutritionJsonStr)
            throws JSONException {
        Nutrition nutrition = new Nutrition();
        JSONObject jsonGlobal = new JSONObject(nutritionJsonStr);

        if(jsonGlobal.has(CAL)) nutrition.calorie = jsonGlobal.getInt(CAL);
        if(jsonGlobal.has(WEIGHT)) nutrition.totalweight = jsonGlobal.getInt(WEIGHT);

        JSONObject jsonSub=null;
        if(jsonGlobal.has(NUTRITIONS)) {
            jsonSub = jsonGlobal.getJSONObject(NUTRITIONS);

            JSONObject jsonTmp=null;
            if(jsonSub.has(FAT)){
                jsonTmp = jsonSub.getJSONObject(FAT);
                nutrition.fat = (float) jsonTmp.getDouble(QUANTITY);

            }
            if(jsonSub.has(CARBS)){
                jsonTmp = jsonSub.getJSONObject(CARBS);
                nutrition.carbs = (float)jsonTmp.getDouble(QUANTITY);
            }
            if(jsonSub.has(PROTEIN)){
                jsonTmp = jsonSub.getJSONObject(PROTEIN);
                nutrition.protein = (float) jsonTmp.getDouble(QUANTITY);
            }
        }

        return nutrition;
    }

    //////////////////////////////////////////////////////////////////////////
    // EDAMAM
    //////////////////////////////////////////////////////////////////////////
    //Using Edamam to analyze the nutrition
    // curl "https://api.edamam.com/api/nutrition-data?app_id=${YOUR_APP_ID}&app_key=${YOUR_APP_KEY}&ingr=1%20large%20apple"
    private static final String STATIC_EDAMAM_URL =
            "https://api.edamam.com/api/nutrition-data";
    static final String PARAM_APP_ID = "app_id";
    static final String PARAM_APP_KEY = "app_key";
    static final String PARAM_INGR = "ingr";

    public static URL buildUrl(Context context, String query){
        Uri buildUri = Uri.parse(STATIC_EDAMAM_URL).buildUpon()
                .appendQueryParameter(PARAM_APP_ID, context.getString(R.string.edamam_app_id))
                .appendQueryParameter(PARAM_APP_KEY, context.getString(R.string.edamam_app_key))
                .appendQueryParameter(PARAM_INGR, query)
                .build();
        URL url=null;
        try{
            url = new URL(buildUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        String method = "GET";
        urlConnection.setRequestMethod(method);
        try{
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            boolean hasInput = scanner.hasNext();
            if(hasInput){
                String results = new String();
                while(scanner.hasNext()){
                    results +=scanner.next();
                }
                return results;
            }else{
                return null;
            }
        }finally {
            urlConnection.disconnect();
        }
    }
}
