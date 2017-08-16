package com.example.lucyzhao.notetaking;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static com.example.lucyzhao.notetaking.Utils.DEFAULT_PICTURE_PATH;


public class MainActivity extends AppCompatActivity {
    // this is data for recycler view, always represents the newest food list
    ArrayList<Food> foodList = new ArrayList<>();
    FoodListAdapter foodListAdapter;

    private boolean getCache = true;

    EditText newNoteName;
    private Uri imageUri;   //stores the uri of the last image saved to device
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get the cached foodlist
        ArrayList<Food> cachedFoodList = Utils.getCachedFoodList(getApplicationContext());
        if(cachedFoodList != null){
            foodList = cachedFoodList;
        }

        /*--------- setting up recycler view --------*/
        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodListAdapter = new FoodListAdapter(foodList, getApplicationContext());
        recyclerView.setAdapter(foodListAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        /*---------------detecting swipe motion--------------------*/
        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.Callback() {
                    private Paint paint = new Paint();
                    private Paint textPaint = new Paint();
                    private final Bitmap deleteIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), android.R.drawable.ic_menu_delete);

                    @Override
                    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        return makeFlag(ACTION_STATE_IDLE, LEFT) | makeFlag(ACTION_STATE_SWIPE, LEFT);
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false; //this method should never be called
                    }
                    /* when the user swipes a view, it gets deleted */
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int posSwiped = viewHolder.getAdapterPosition();
                        Log.v("onSwiped","you swiped it!");
                        Log.v("onSwiped","adapter position is " + posSwiped);

                        /* 1.delete the picture associated */
                        String uriString = foodList.get(posSwiped).getImageUriString();
                        deleteImageOnDevice(Uri.parse(uriString), getApplicationContext());

                        /*  2.remove the ingredient and procedure files from internal
                            storage
                            3.remove the swiped item from the adapter and its internal
                            list
                         */
                        if(!Utils.deleteFoodDir(getApplicationContext(), foodList.get(posSwiped).getId())){
                            Toast.makeText(getApplicationContext(), "Error: delete failed", Toast.LENGTH_LONG);
                        }
                        foodList.remove(posSwiped);
                        foodListAdapter.notifyItemRemoved(posSwiped);

                    }

                    @Override
                    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder){
                        return 0.4f;
                    }

                    @Override
                    public void onChildDraw(Canvas c,
                                            RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder,
                                            float dX,
                                            float dY,
                                            int actionState,
                                            boolean isCurrentlyActive){
                        super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
                        if(isCurrentlyActive && actionState == ACTION_STATE_SWIPE) {
                            paint.setColor(0xffff0000);
                            View view = viewHolder.itemView;
                            /* draw the red rectangle */
                            c.drawRect(view.getRight() + dX, view.getTop(), view.getRight(), view.getBottom(), paint);
                            /* draw the delete icon and "swipe to delete" text */
                            if (dX <= -200){
                                c.drawBitmap(deleteIcon, view.getRight() - deleteIcon.getWidth() - 50,
                                        view.getTop() + (view.getBottom() - view.getTop() - deleteIcon.getHeight()) / 2, paint);
                                textPaint.setColor(Color.WHITE);
                                textPaint.setStyle(Paint.Style.FILL);
                                int textSize = 50;
                                textPaint.setTextSize(textSize);
                                String text = "swipe to delete";
                                float textWidth = textPaint.measureText(text);
                                c.drawText(text,view.getRight() - textWidth - deleteIcon.getWidth() - 70,
                                        view.getTop() + (view.getBottom()-view.getTop())/2 + textSize/2, textPaint);
                            }
                        }
                    }


                });
        mIth.attachToRecyclerView(recyclerView);
        /*---------------end of detecting swipe motion--------------------*/

    }

    @Override
    protected void onStop(){
        super.onStop();
        /* serialize the arraylist */
        Log.v(TAG, "in onStop");
        Utils.saveFoodList(getApplicationContext(),foodList);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG, "in onRestart");
        if(getCache) {
            Log.v(TAG, "getting cache");
            ArrayList<Food> newFoodList = Utils.getCachedFoodList(getApplicationContext());
            foodListAdapter.updateInnerList(newFoodList);
            foodList = newFoodList;
        }
        else {
            Log.v(TAG, "do not get cache");
            getCache = true;
        }
    }


    /**
     * Delete the image saved in the gallery
     * @param uri of the image
     * @param context current context
     */
    private static void deleteImageOnDevice(Uri uri, Context context){
        Log.v(TAG, "deleting file" + uri.getPath());
        if (!uri.getPath().equals(Utils.DEFAULT_PICTURE_URI_PATH)){
            Log.v(TAG, "uri.getPath() is {" + uri.getPath() + "}");
            Log.v(TAG, "default picture uri path is {" + Utils.DEFAULT_PICTURE_URI_PATH + "}");
            context.getContentResolver().delete(uri, null, null);
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
            foodList.add(new Food(newNoteName.getText().toString(), imageUri.toString(), this));
            foodListAdapter.notifyItemInserted(foodListAdapter.getItemCount() - 1);
            Toast.makeText(getApplicationContext(),"created!",Toast.LENGTH_SHORT).show();
        }
        return nameExists;
    }

    public static class NewNoteDialogFragment extends DialogFragment {
        static final int MY_PERMISSIONS_REQUEST_CAMERA = 5656;
        static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 5757;
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

            FloatingActionButton cameraButton = (FloatingActionButton) fragmentView.findViewById(R.id.camera_button);
            Button okButton = (Button) fragmentView.findViewById(R.id.ok_button);
            Button cancelButton = (Button) fragmentView.findViewById(R.id.cancel_button);

            okButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    ((MainActivity) getActivity()).newNoteName
                            = (EditText) fragmentView.findViewById(R.id.new_note_name);
                    //transfer control back to the Fragment's host to check if the name
                    //already exits
                    if(!((MainActivity) getActivity()).onDialogPositiveClick()) {
                        dismiss();
                        //dismissDialog();
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    deleteImageOnDevice(((MainActivity)getActivity()).imageUri, getActivity());
                    dismiss();
                    //dismissDialog();
                }
            });

            cameraButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //this will request camera permission, which will try to take a picture
                    requestUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });

            return builder.create();
        }

        /**
         * Handles the effects of dismissing the dialog on MainActivity
         * (calling onRestart)
         * EDIT: dismissing the dialog does not call onRestart, returning
         * from picture taking does
         */
        private void dismissDialog(){
            Log.v(TAG, "in dismissing dialogue");
            ((MainActivity) getActivity()).getCache = false;
            Log.v(TAG, "dialog dismissing, getCache = " + ((MainActivity) getActivity()).getCache);
            dismiss();
        }

        private void takePicture(){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((MainActivity)getActivity()).imageUri = createUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, ((MainActivity)getActivity()).imageUri);
            startActivityForResult(intent, TAKE_PICTURE);

            ((MainActivity) getActivity()).getCache = false;
            Log.v(TAG, "leaving MainActivity, getCache = " + ((MainActivity) getActivity()).getCache);
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
            try {
                Log.v("tag","GETTING PICTURE");
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), ((MainActivity)getActivity()).imageUri);
                Log.v(TAG, ((MainActivity)getActivity()).imageUri.toString());
                /* TODO note: CHANGED FROM 90 TO 0 ON REAL DEVICE*/
                Bitmap processedBitmap = Utils.processBitmap(originalBitmap);
                ImageView picPreview = (ImageView) fragmentView.findViewById(R.id.foodpicture_preview);
                picPreview.setImageBitmap(processedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        /**
         * Helper method to request device permissions
         * If permission is denied, request again
         * If permission requested is camera and is already granted, take the picture
         * If permission requested is external storage and is already granted, check the camera
         * permission
         * @param devicePermission could be either camera permission or external storage permission
         * @param requestCode
         */
        private void requestUserPermission(String devicePermission, int requestCode){
            Log.v(TAG,"entering requesting user permission" + devicePermission);

            if (ContextCompat.checkSelfPermission(getContext(),
                    devicePermission)
                    == PackageManager.PERMISSION_DENIED) {
                    // try requesting the permission again.
                    Log.v(TAG,"requesting permission");
                    requestPermissions(
                            new String[]{devicePermission},
                            requestCode);

            }
            else if(ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){
                Log.v(TAG, "camera permission already granted");
                takePicture();
            }
            else if(ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                Log.v(TAG, "camera permission not granted, requesting. External S permission granted");
                requestUserPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
            }
            else
                Log.v(TAG, "error requesting permission");
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            Log.v(TAG,"entering result");
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_CAMERA: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("tag","trying to take picture");
                        // permission was granted, yay! Take the picture
                        takePicture();
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
