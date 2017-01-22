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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    // this is data for recycler view
    ArrayList<Food> foodList = new ArrayList<>();
    MyAdapter myAdapter = new MyAdapter(foodList);
    EditText newNoteName;
    private static final String LIST_FILE_NAME = "food_list";
    public final static String EXTRA_TITLE = "com.example.lucyzhao.notetaking.TITLE";
    public final static String EXTRA_PIC = "com.example.lucyzhao.notetaking.PIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeFoodList();
        for(Food food : foodList){
            System.out.println(food.getTitle());
        }

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // create an adapter
        myAdapter = new MyAdapter(foodList);
        // set adapter
        recyclerView.setAdapter(myAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStop(){
        super.onStop();
        /* serialize the arraylist */
        try {
            FileOutputStream fos = openFileOutput(LIST_FILE_NAME, MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(foodList);
            objectOutputStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeFoodList() {
        try {
            FileInputStream fis = openFileInput(LIST_FILE_NAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(fis);
            foodList = (ArrayList<Food>) objectInputStream.readObject();
            objectInputStream.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void addNewNote(View view) {
        final NewNoteDialogFragment dialogFragment = new NewNoteDialogFragment();
        dialogFragment.show(getSupportFragmentManager(),"newNoteDialogFragment");
    }

    /**
     * Check if the input recipe name exists. If it does, prompt user to enter another name
     * If not, create a new note with this name
     * @return whether the input recipe name already exists
     */
    private boolean onDialogPositiveClick(){
        boolean nameExists = false;
        for(Food food : foodList) {
            Log.w("food is ", food.getTitle());
            if(food.getTitle().equals(newNoteName.getText().toString())) {
                Toast.makeText(getApplicationContext(),"recipe exists, change name", Toast.LENGTH_LONG).show();
                nameExists = true;
                break;
            }
        }
        if(!nameExists) {
            foodList.add(new Food(newNoteName.getText().toString()));
            myAdapter.notifyItemInserted(myAdapter.getItemCount() - 1);
            Toast.makeText(getApplicationContext(),"created!",Toast.LENGTH_SHORT).show();
        }
        return nameExists;
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

            Button okButton = (Button) textEntryView.findViewById(R.id.ok_button);
            Button cancelButton = (Button) textEntryView.findViewById(R.id.cancel_button);
            okButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    ((MainActivity) getActivity()).newNoteName
                            = (EditText) textEntryView.findViewById(R.id.new_note_name);
                    //transfer control back to the Fragment's host
                    if(!((MainActivity) getActivity()).onDialogPositiveClick()) {
                        dismiss();
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    dismiss();
                }
            });

            return builder.create();
        }
    }

}
