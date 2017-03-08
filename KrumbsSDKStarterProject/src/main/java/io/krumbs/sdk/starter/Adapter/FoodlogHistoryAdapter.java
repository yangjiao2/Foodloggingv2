package io.krumbs.sdk.starter.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.krumbs.sdk.starter.Data.DbContract;
import io.krumbs.sdk.starter.R;
import io.krumbs.sdk.starter.Utilities.Nutrition;

/**
 * Created by baconleung on 3/6/17.
 */

public class FoodlogHistoryAdapter extends RecyclerView.Adapter<FoodlogHistoryAdapter.ViewHolder>{

    // Holds on to the cursor to display the foodlog history
    private Cursor mCursor;

    /*
    * Constructor using the context and db cursor
    *  @param context the calling context/activity
    *  @param cursor the db cursor with macro nutrition data to display
    * */
    public FoodlogHistoryAdapter(Cursor cursor){
        this.mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foodlog_history,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Move the mCursor to the postion of the item to be displayed
        if(!mCursor.moveToPosition(position))
            return ; //bail if returned null

        // Update the view holder with the information needed to display
        holder.mTVCalorieValue.setText(String.valueOf(mCursor.getInt(
                                mCursor.getColumnIndex(DbContract.FoodlogEntry.COLUMN_FOOD_CALORIE))));
        holder.mTVCarbsValue.setText(String.valueOf(mCursor.getFloat(
                                mCursor.getColumnIndex(DbContract.FoodlogEntry.COLUMN_FOOD_CARBOHYDRATES))));
        holder.mTVFatValue.setText(String.valueOf(mCursor.getFloat(
                                mCursor.getColumnIndex(DbContract.FoodlogEntry.COLUMN_FOOD_FAT))));
        holder.mTVProteinValue.setText(String.valueOf(mCursor.getFloat(
                                mCursor.getColumnIndex(DbContract.FoodlogEntry.COLUMN_FOOD_PROTEIN))));
        holder.mTVWeightValue.setText(String.valueOf(mCursor.getInt(
                mCursor.getColumnIndex(DbContract.FoodlogEntry.COLUMN_FOOD_TOTAL_WEIGHT))));
        //Retrieve the id from the cursor and set the tage of the itemview in the holder to the id
        long id = mCursor.getLong(mCursor.getColumnIndex(DbContract.FoodlogEntry._ID));
        holder.itemView.setTag(id);




    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /**
     * Swaps the Cursor currently held in the adapter with a new one
     * and triggers a UI refresh
     *
     * @param newCursor the new cursor that will replace the existing one
     */
    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }


    //////////////////////////////////////////////////////////////////////////
    // Class ViewHolder
    //////////////////////////////////////////////////////////////////////////
    public static final class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTVCalorieValue;
        private TextView mTVWeightValue;
        private TextView mTVFatValue;
        private TextView mTVProteinValue;
        private TextView mTVCarbsValue;

        public ViewHolder(View v){
            super(v);
            mTVCalorieValue = (TextView) v.findViewById(R.id.tv_calorie_value);
            mTVWeightValue= (TextView) v.findViewById(R.id.tv_weight_value);
            mTVFatValue= (TextView) v.findViewById(R.id.tv_fat_value);
            mTVProteinValue= (TextView) v.findViewById(R.id.tv_protein_value);
            mTVCarbsValue= (TextView) v.findViewById(R.id.tv_carbs_value);
         }
    }
}
