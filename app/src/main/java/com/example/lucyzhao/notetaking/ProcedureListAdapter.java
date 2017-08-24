package com.example.lucyzhao.notetaking;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by LucyZhao on 2017/8/20.
 */

public class ProcedureListAdapter extends RecyclerView.Adapter<ProcedureListAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {
    private ArrayList<String> procedureList;
    private static final String TAG = ProcedureListAdapter.class.getSimpleName();

    public ProcedureListAdapter(ArrayList<String> procedure_list) {
        this.procedureList = procedure_list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflates the layout for a single item created in res
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_list_item_procedure, null);

        // instantiates a new view created from the single item layout
        return new ProcedureListAdapter.ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String text = procedureList.get(position);
        if (Utils.isUri(text)) {
            Glide.with(holder.pImage.getContext())
                    .load(text)
                    .centerCrop()
                    .into(holder.pImage);
            holder.setIsPic(true);
        } else {
            holder.setIsPic(false);
            holder.pText.setText(procedureList.get(position));
        }
        holder.isInEditMode(false);
    }

    @Override
    public int getItemCount() {
        return procedureList.size();
    }

    /**
     * Source: https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
     *
     * @param fromPosition
     * @param toPosition
     */
    @Override
    public void onItemMove(RecyclerView.ViewHolder holder, int fromPosition, int toPosition) {
        ((ProcedureListAdapter.ViewHolder) holder).isInEditMode(false);

        if (fromPosition > toPosition) {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(procedureList, i, i - 1);
            }
        } else {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(procedureList, i, i + 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        procedureList.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        CardView pCardView;
        TextView pText;
        EditText pEditText;
        Button okButton;

        ImageView pImage;

        private boolean isPic = false;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            pCardView = (CardView) itemLayoutView.findViewById(R.id.procedure_card_view);
            pText = (TextView) itemLayoutView.findViewById(R.id.procedure_text);
            pEditText = (EditText) itemLayoutView.findViewById(R.id.procedure_edit_text);
            okButton = (Button) itemLayoutView.findViewById(R.id.procedure_edit_ok);
            pImage = (ImageView) itemLayoutView.findViewById(R.id.procedure_image);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pEditText.getText().toString().isEmpty()) {
                        Toast.makeText(v.getContext(), "procedure cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        procedureList.remove(getAdapterPosition());
                        procedureList.add(getAdapterPosition(), pEditText.getText().toString());
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });

            itemLayoutView.setClickable(true);
            itemLayoutView.setOnClickListener(this);
            itemLayoutView.setOnLongClickListener(this);
        }

        public void setIsPic(boolean isPic) {
            this.isPic = isPic;
        }


        @Override
        public void onClick(View v) {
            isInEditMode(false);
        }

        @Override
        public boolean onLongClick(View v) {
            Log.v(TAG, "in onLongClick");
            isInEditMode(true);
            return true;
        }

        /**
         * Helper method that designates what the app should do if
         * the user wishes to edit a procedure item
         *
         * @param inEditMode
         */
        private void isInEditMode(boolean inEditMode) {
            if (isPic) {
                pImage.setVisibility(View.VISIBLE);
                pText.setVisibility(View.GONE);
                pEditText.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
            } else {
                pImage.setVisibility(View.GONE);
                if (inEditMode) {
                    pText.setVisibility(View.INVISIBLE);
                    pEditText.setText(pText.getText().toString());
                    pEditText.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);
                } else {
                    pText.setVisibility(View.VISIBLE);
                    pEditText.setVisibility(View.GONE);
                    okButton.setVisibility(View.GONE);
                }
            }
        }

    }
}
