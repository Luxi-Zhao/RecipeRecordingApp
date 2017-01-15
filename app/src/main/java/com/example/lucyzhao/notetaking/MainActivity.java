package com.example.lucyzhao.notetaking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // this is data for recycler view
    ArrayList<Food> foodList = new ArrayList<>();
    MyAdapter myAdapter = new MyAdapter(foodList);
    EditText newNoteName;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public final static String EXTRA_TITLE = "com.example.lucyzhao.notetaking.TITLE";
    public final static String EXTRA_PIC = "com.example.lucyzhao.notetaking.PIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        foodList.add(new Food("food1"));
        foodList.add(new Food("food2"));
        foodList.add(new Food("food5"));
        foodList.add(new Food("food6"));

        // 2. set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 3. create an adapter
        myAdapter = new MyAdapter(foodList);
        // 4. set adapter
        recyclerView.setAdapter(myAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void addNewNote(View view) {
        final NewNoteDialogFragment dialogFragment = new NewNoteDialogFragment();
        dialogFragment.show(getSupportFragmentManager(),"newNoteDialogFragment");
    }

    private void onDialogPositiveClick(){
        foodList.add(new Food(newNoteName.getText().toString()));
        myAdapter.notifyItemInserted(myAdapter.getItemCount()-1);
    }

    public static class NewNoteDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View textEntryView = inflater.inflate(R.layout.new_note_dialog_fragment, null);
            builder.setView(textEntryView);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity)getActivity()).newNoteName
                                    = (EditText) textEntryView.findViewById(R.id.new_note_name);
                            //transfer control back to the Fragment's host
                            ((MainActivity) getActivity()).onDialogPositiveClick();
                            Toast.makeText(getContext(),"created!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NewNoteDialogFragment.this.getDialog().cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
