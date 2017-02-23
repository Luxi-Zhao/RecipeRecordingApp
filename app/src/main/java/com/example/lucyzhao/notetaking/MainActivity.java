package com.example.lucyzhao.notetaking;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    // this is data for recycler view
    ArrayList<Food> foodList = new ArrayList<>();
    MyAdapter myAdapter;
    EditText newNoteName;
    private Uri imageUri;
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

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // create an adapter
        myAdapter = new MyAdapter(foodList, getApplicationContext());
        // set adapter
        recyclerView.setAdapter(myAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStop(){
        super.onStop();
        /* serialize the arraylist */
        saveFoodList();
    }

    private void saveFoodList() {
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

    /**
     * Callback for the add new note floating action button
     * @param view
     */
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
            foodList.add(new Food(newNoteName.getText().toString(), imageUri.toString()));
            myAdapter.notifyItemInserted(myAdapter.getItemCount() - 1);
            Toast.makeText(getApplicationContext(),"created!",Toast.LENGTH_SHORT).show();
        }
        return nameExists;
    }

    public static class NewNoteDialogFragment extends DialogFragment {
        static final int MY_PERMISSIONS_REQUEST_CAMERA = 5656;
        static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 5757;
        static final String DEFAULT_PICTURE_PATH = "android.resource://com.example.lucyzhao.notetaking/drawable/foodpic2";
        private View fragmentView;
        static final int TAKE_PICTURE = 115;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog;
            // Pass null as the parent view because its going in the dialog layout
            fragmentView = inflater.inflate(R.layout.new_note_dialog_fragment, null);
            builder.setView(fragmentView);

            /* initialize imageUri with the default picture in case the user doesn't want to use
              a custom picture
             */
            ((MainActivity) getActivity()).imageUri = Uri.parse(DEFAULT_PICTURE_PATH);

            Button okButton = (Button) fragmentView.findViewById(R.id.ok_button);
            Button cancelButton = (Button) fragmentView.findViewById(R.id.cancel_button);
            okButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    ((MainActivity) getActivity()).newNoteName
                            = (EditText) fragmentView.findViewById(R.id.new_note_name);
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

            tryTakingPicture();

            return builder.create();
        }

        private void tryTakingPicture(){
            FloatingActionButton cameraButton = (FloatingActionButton) fragmentView.findViewById(R.id.camera_button);
            cameraButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //this will request camera permission, which will try to take a picture
                    requestUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });
        }

        private void doTakingPicture(){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((MainActivity)getActivity()).imageUri = createUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, ((MainActivity)getActivity()).imageUri);
            startActivityForResult(intent, TAKE_PICTURE);
        }

        /**
         * give imageUri another value instead of Uri.fromFile(photoFile) because
         * file: // can no longer be used
         */
        private Uri createUri(){
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            //insert values into a table at EXTERNAL_CONTENT_URI and get the URI of the newly
            //created row
            return getActivity().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }


        /**
         * Handling result from startActivityForResult
         * @param requestCode
         * @param resultCode
         * @param data
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.v("tag","ENTERING ON ACTIVITY RESULT");
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
                setPicturePreview();
            }

        }

        private void setPicturePreview(){
           // Uri selectedImageUri = imageUri;
            try {
                Log.v("tag","GETTING PICTURE");
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), ((MainActivity)getActivity()).imageUri);
                Bitmap processedBitmap = processBitmap(originalBitmap, 90);
                ImageView picPreview = (ImageView) fragmentView.findViewById(R.id.foodpicture_preview);
                picPreview.setImageBitmap(processedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * source
         * http://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
         * Users should only take pictures in portrait
         * @param bm
         * @return
         */
        public static Bitmap processBitmap(Bitmap bm, int rotationInDegrees){

            Matrix matrix = new Matrix();
            //rotate the matrix around a pivot point
            matrix.setRotate(rotationInDegrees, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            Bitmap resizedBitmap = Bitmap.createBitmap(rotatedBitmap, 0,0, bm.getHeight(), bm.getHeight());
            return resizedBitmap;
        }


        private void requestUserPermission(String devicePermission, int requestCode){
            Log.v("tag","entering requesting user permission" + devicePermission);

            if (ContextCompat.checkSelfPermission(getContext(),
                    devicePermission)
                    == PackageManager.PERMISSION_DENIED) {
                    // request the permission.
                    Log.v("tag","requesting permission");
                    requestPermissions(
                            new String[]{devicePermission},
                            requestCode);

            }
            else if(ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){
                doTakingPicture();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            Log.v("tag","entering result");
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_CAMERA: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("tag","trying to take picture");
                        // permission was granted, yay! Take the picture
                        doTakingPicture();
                    } else {
                        Log.v("tag","camera access permission denied");
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("tag","ask for camera permission");
                        // permission was granted, yay! Ask for camera permission
                        requestUserPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        Log.v("tag","external storage writing permission denied");
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }



    }

}
