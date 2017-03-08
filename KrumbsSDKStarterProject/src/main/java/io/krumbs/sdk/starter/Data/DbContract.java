package io.krumbs.sdk.starter.Data;

import android.provider.BaseColumns;

/**
 * Created by baconleung on 3/6/17.
 */

public class DbContract {

    public static final class FoodlogEntry implements BaseColumns {

        public static final String TABLE_NAME = "foodlog";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_FOODLOG_ID = "foodlog_id";
        //Event { breakfast, lunch, dinner, snap}
        public static final String COLUMN_EVENT = "event";
        public static final String COLUMN_IMAGE_URI = "image_uri";
        // Below, food data
        public static final String COLUMN_FOOD_TOTAL_WEIGHT = "total_weight";
        public static final String COLUMN_FOOD_CALORIE = "calorie";
        public static final String COLUMN_FOOD_FAT = "fat";
        public static final String COLUMN_FOOD_PROTEIN = "protein";
        public static final String COLUMN_FOOD_CARBOHYDRATES = "carbohydrates";

    }

}
