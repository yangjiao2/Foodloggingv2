package io.krumbs.sdk.starter.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import clarifai2.dto.prediction.Concept;
import io.krumbs.sdk.starter.R;

/**
 * Created by baconleung on 2/18/17.
 */

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    //List type, used to hold results received from Clarifai. The result is class CONCEPT from Clarifai.
    //CONCEPT type mainly has three elements, ID, NAME and VALUE. You can use concept.id(), concept.name(),
    // concept.value() to get these attributes. The VALUE is the probability, float type.
    private static final int limitedNumber = 10;
    private static List<Concept> concepts =new ArrayList<>();

    public List<Concept> getConcepts(){
        return concepts;
    }

    public IngredientAdapter setData(List<Concept> list){
        concepts=list;
        //Limiting the number of desplay item
        int times = list.size()-limitedNumber;
        for(int i=0;i<times;i++){
            concepts.remove(limitedNumber);
        }
        notifyDataSetChanged();
        return this;
    }

    //Bind the layout ITEM_INGREDIENT.XML to the viewholder. You can suppose Viewholder is the item in a view.
    // Lots of Viewholder make up a list, like a list contains lots of items(viewholder).
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient,parent,false));
    }

    //Combine the content in the concept with viewholder(item)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Concept concept = concepts.get(position);
        holder.ingredientName.setText(concept.name());
//        holder.ingredientProbability.setText(String.valueOf(concept.value()));
    }

    @Override
    public int getItemCount() {
        return concepts.size();
    }

    public void removeItem(int position){
        concepts.remove(position);
        notifyItemRemoved(position);
    }

    //////////////////////////////////////////////////////////////////////////
    // Class ViewHolder
    //////////////////////////////////////////////////////////////////////////
    public static final class ViewHolder extends RecyclerView.ViewHolder{
        public TextView ingredientName;
//        public TextView ingredientProbability;

        public ViewHolder(View v){
            super(v);
            ingredientName = (TextView) v.findViewById(R.id.tv_ingredient_name);
//            ingredientProbability = (TextView) v.findViewById(R.id.tv_ingredient_probability);
        }
    }
}
