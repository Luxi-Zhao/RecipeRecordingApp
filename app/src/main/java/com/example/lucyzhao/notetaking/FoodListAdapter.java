package com.example.lucyzhao.notetaking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2016/11/12.
 */
public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {

    private static ArrayList<Food> foodList;
    private Context associatedActivityContext;

    public FoodListAdapter(ArrayList<Food> food_List, Context associatedActivityContext) {
        foodList = food_List;
        this.associatedActivityContext = associatedActivityContext;
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public FoodListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item, null);

        return new ViewHolder(itemLayoutView);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        viewHolder.title.setText(foodList.get(position).getTitle());
        viewHolder.foodImage.setImageBitmap(foodList.get(position).getImage(associatedActivityContext));

    }

    /**
     * Replace the inner list with a new list
     * @param newFoodList
     */
    public void updateInnerList(ArrayList<Food> newFoodList){
        foodList = newFoodList;
        this.notifyDataSetChanged();
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView title;
        public ImageView foodImage;
        private final Context context;
        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            title = (TextView) itemLayoutView.findViewById(R.id.title);
            foodImage = (ImageView) itemLayoutView.findViewById(R.id.foodpicture);
            context = itemLayoutView.getContext();
            itemLayoutView.setClickable(true);
            itemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, RecipePageActivity.class);

            intent.putExtra(Utils.EXTRA_CLICKING_POSITION, this.getAdapterPosition());
            intent.putExtra(Utils.EXTRA_FOOD_OBJECT, foodList.get(this.getAdapterPosition()));

            context.startActivity(intent);
        }

    }


    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
