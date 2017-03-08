package io.krumbs.sdk.starter.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by baconleung on 3/6/17.
 */

public class FoodlogDbHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "foodlog.db";

    private static final int DATABASE_VERSION = 3;

    public FoodlogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FOODLOG_TABLE =
                "CREATE TABLE " + DbContract.FoodlogEntry.TABLE_NAME + " (" +

                        DbContract.FoodlogEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DbContract.FoodlogEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                        DbContract.FoodlogEntry.COLUMN_FOODLOG_ID + " INTEGER, " +
                        DbContract.FoodlogEntry.COLUMN_IMAGE_URI + " TEXT, " +
                        DbContract.FoodlogEntry.COLUMN_EVENT + " TEXT, " +
                        //Food
                        DbContract.FoodlogEntry.COLUMN_FOOD_CALORIE + " INTEGER, " +
                        DbContract.FoodlogEntry.COLUMN_FOOD_CARBOHYDRATES + " REAL, " +
                        DbContract.FoodlogEntry.COLUMN_FOOD_FAT + " REAL, " +
                        DbContract.FoodlogEntry.COLUMN_FOOD_PROTEIN + " REAL, " +
                        DbContract.FoodlogEntry.COLUMN_FOOD_TOTAL_WEIGHT + " INTEGER)" ;

        db.execSQL(SQL_CREATE_FOODLOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.FoodlogEntry.TABLE_NAME);
        onCreate(db);
    }
}
