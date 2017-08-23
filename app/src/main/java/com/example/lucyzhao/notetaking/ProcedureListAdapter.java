package com.example.lucyzhao.notetaking;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/8/20.
 */

public class ProcedureListAdapter extends RecyclerView.Adapter<ProcedureListAdapter.ViewHolder> {
    ArrayList<String> procedureList;

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
        holder.pText.setText(procedureList.get(position));
        holder.isInEditMode(false);
    }

    @Override
    public int getItemCount() {
        return procedureList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView pText;
        EditText pEditText;
        Button okButton;
        Button deleteButton;

        public ViewHolder(View itemLayoutView){
            super(itemLayoutView);
            pText = (TextView) itemLayoutView.findViewById(R.id.procedure_text);
            pEditText = (EditText) itemLayoutView.findViewById(R.id.procedure_edit_text);
            okButton = (Button) itemLayoutView.findViewById(R.id.procedure_edit_ok);
            deleteButton = (Button) itemLayoutView.findViewById(R.id.procedure_edit_delete);

            //isInEditMode(false);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(pEditText.getText().toString().isEmpty()){
                        Toast.makeText(v.getContext(), "procedure cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        procedureList.remove(getAdapterPosition());
                        procedureList.add(getAdapterPosition(), pEditText.getText().toString());
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    procedureList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            itemLayoutView.setClickable(true);
            itemLayoutView.setOnClickListener(this);
            itemLayoutView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            isInEditMode(false);
        }

        @Override
        public boolean onLongClick(View v) {
            isInEditMode(true);
            pEditText.setText(pText.getText().toString());
            return true;
        }

        private void isInEditMode(boolean inEditMode) {
            if (inEditMode) {
                pText.setVisibility(View.INVISIBLE);
                pEditText.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
            } else {
                pText.setVisibility(View.VISIBLE);
                pEditText.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
        }

    }
}
