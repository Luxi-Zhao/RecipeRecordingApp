package com.example.lucyzhao.notetaking;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/8/16.
 */

public class IngredientsListAdapter extends RecyclerView.Adapter<IngredientsListAdapter.ViewHolder> {
    private ArrayList<Ingredient> ingredientList;

    public IngredientsListAdapter(ArrayList<Ingredient> ingredient_list){
        this.ingredientList = ingredient_list;
    }


    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public IngredientsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_single_list_item, null);

        return new IngredientsListAdapter.ViewHolder(itemLayoutView);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(IngredientsListAdapter.ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        viewHolder.name.setText(ingredientList.get(position).getName());
        viewHolder.amount.setText(Integer.toString(ingredientList.get(position).getAmount()));
        viewHolder.unit.setText(ingredientList.get(position).getUnit());
    }


    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public void updateInnerList(ArrayList<Ingredient> newIngList){
        this.ingredientList = newIngList;
        this.notifyDataSetChanged();
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public TextView amount;
        public TextView unit;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            name = (TextView) itemLayoutView.findViewById(R.id.ingredient_name);
            amount = (TextView) itemLayoutView.findViewById(R.id.ingredient_amount);
            unit = (TextView) itemLayoutView.findViewById(R.id.ingredient_unit);
        }

        @Override
        public void onClick(View view) {

        }

    }

}
