package com.example.lucyzhao.notetaking.ingredient;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.example.lucyzhao.notetaking.ItemTouchHelperAdapter;
import com.example.lucyzhao.notetaking.R;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/8/16.
 */

public class IngredientsListAdapter extends RecyclerView.Adapter<IngredientsListAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<Ingredient> ingredientList;

    public IngredientsListAdapter(ArrayList<Ingredient> ingredient_list) {
        this.ingredientList = ingredient_list;
    }


    /**
     * Create a new view when there is no recycled views available
     * Invoked if nothing is ever deleted from the list
     * (invoked by the layout manager)
     */
    @Override
    public IngredientsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // inflates the layout for a single item created in res
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item_ingredient, null);

        // instantiates a new view created from the single item layout
        return new IngredientsListAdapter.ViewHolder(itemLayoutView);
    }

    /**
     * Replace the contents of a recycled view with the new view
     * Invoked if items have been deleted from the list
     * (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(IngredientsListAdapter.ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        float amount = ingredientList.get(position).getAmount();
        String amountString;
        if (Math.floor(amount) == amount) {
            amountString = Integer.toString((int) amount);
        } else {
            amountString = Float.toString(amount);
        }
        viewHolder.name_tv.setText(ingredientList.get(position).getName());
        viewHolder.amount_tv.setText(amountString);
        viewHolder.unit_tv.setText(ingredientList.get(position).getUnit());

        // hides edit text for recycled views
        viewHolder.isInEditMode(false);

    }


    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    @Override
    public void onItemMove(RecyclerView.ViewHolder holder, int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {
        ingredientList.remove(position);
        notifyItemRemoved(position);
    }


    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final String TAG = ViewHolder.class.getSimpleName();
        public final EditText name;
        public EditText amount;
        public EditText unit;
        public Button edit_ok;

        public TextView name_tv;
        public TextView amount_tv;
        public TextView unit_tv;

        /**
         * Instantiates a new view
         *
         * @param itemLayoutView
         */
        public ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);
            Log.v(TAG, "instantiating a new viewholder");

            name = (EditText) itemLayoutView.findViewById(R.id.ingredient_name);
            amount = (EditText) itemLayoutView.findViewById(R.id.ingredient_amount);
            unit = (EditText) itemLayoutView.findViewById(R.id.ingredient_unit);
            edit_ok = (Button) itemLayoutView.findViewById(R.id.edit_ok);

            name_tv = (TextView) itemLayoutView.findViewById(R.id.ingredient_name_tv);
            amount_tv = (TextView) itemLayoutView.findViewById(R.id.ingredient_amount_tv);
            unit_tv = (TextView) itemLayoutView.findViewById(R.id.ingredient_unit_tv);


            // configures clicking events for recycler view
            itemLayoutView.setClickable(true);

            itemLayoutView.setOnClickListener(this);
            itemLayoutView.setOnLongClickListener(this);

            // configures clicking events for finishing an edit and deleting an item
            edit_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "edit ok on click");
                    String new_name = name.getText().toString();
                    String new_amount_str = amount.getText().toString();
                    String new_unit = unit.getText().toString();
                    if (new_name.isEmpty() || new_amount_str.isEmpty()) {
                        Toast.makeText(itemLayoutView.getContext(),
                                "name and amount cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        Ingredient new_ing = new Ingredient(new_name, Float.parseFloat(new_amount_str), new_unit);
                        ingredientList.remove(getAdapterPosition());
                        ingredientList.add(getAdapterPosition(), new_ing);
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });

        }

        @Override
        public void onClick(View view) {
            isInEditMode(false);
        }

        @Override
        public boolean onLongClick(View v) {
            Log.v(TAG, "onLongClick at adapter pos: " + getAdapterPosition() + "|| ingredient is: " + ingredientList.get(getAdapterPosition()).toString());
            isInEditMode(true);

            name.setText(name_tv.getText().toString());
            amount.setText(amount_tv.getText().toString());
            unit.setText(unit_tv.getText().toString());

            return true;
        }

        private void isInEditMode(boolean inEditMode) {
            if (inEditMode) {
                setTextViewVisibility(View.INVISIBLE);
                setEditModeVisibility(View.VISIBLE);
            } else {
                setTextViewVisibility(View.VISIBLE);
                setEditModeVisibility(View.GONE);
            }
        }

        private void setEditModeVisibility(int visibility) {
            name.setVisibility(visibility);
            amount.setVisibility(visibility);
            unit.setVisibility(visibility);
            edit_ok.setVisibility(visibility);
        }

        private void setTextViewVisibility(int visibility) {
            name_tv.setVisibility(visibility);
            amount_tv.setVisibility(visibility);
            unit_tv.setVisibility(visibility);
        }


    }


}
