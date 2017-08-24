package com.example.lucyzhao.notetaking;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.bumptech.glide.Glide;

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
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // get the cached foodlist
        ArrayList<Food> cachedFoodList = Utils.getCachedFoodList(getApplicationContext());
        if (cachedFoodList != null) {
            foodList = cachedFoodList;
        }

        /*--------- setting up recycler view --------*/
        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodListAdapter = new FoodListAdapter(foodList, getApplicationContext());
        recyclerView.setAdapter(foodListAdapter);

        /*---------------detecting swipe motion--------------------*/
        new ItemTouchHelper(new mFoodTouchHelperCallback(foodListAdapter, getApplicationContext()))
                .attachToRecyclerView(recyclerView);
        /*---------------end of detecting swipe motion--------------------*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        /* serialize the arraylist */
        Log.v(TAG, "in onStop");
        Utils.saveFoodList(getApplicationContext(), foodList);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "in onRestart");
        if (getCache) {
            Log.v(TAG, "getting cache");
            ArrayList<Food> newFoodList = Utils.getCachedFoodList(getApplicationContext());
            foodListAdapter.updateInnerList(newFoodList);
            foodList = newFoodList;
        } else {
            Log.v(TAG, "do not get cache");
            getCache = true;
        }
    }


    /**
     * Callback for the add new note floating action button
     *
     * @param view
     */
    public void addNewNote(View view) {
        final NewNoteDialogFragment dialogFragment = new NewNoteDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "newNoteDialogFragment");
    }

    /**
     * Check if the input recipe name exists. If it does, prompt user to enter another name
     * If not, create a new note with this name
     *
     * @return whether the input recipe name already exists
     */
    private boolean onDialogPositiveClick() {
        boolean nameExists = false;
        for (Food food : foodList) {
            Log.w("food is ", food.getTitle());
            if (food.getTitle().equals(newNoteName.getText().toString())) {
                Toast.makeText(getApplicationContext(), "recipe exists, change name", Toast.LENGTH_LONG).show();
                nameExists = true;
                break;
            }
        }
        if (!nameExists) {
            foodList.add(new Food(newNoteName.getText().toString(), imageUri.toString(), this));
            foodListAdapter.notifyItemInserted(foodListAdapter.getItemCount() - 1);
            Toast.makeText(getApplicationContext(), "created!", Toast.LENGTH_SHORT).show();
        }
        return nameExists;
    }


    public static class NewNoteDialogFragment extends DialogFragment {
        static final int MY_PERMISSIONS_REQUEST_CAMERA = 5656;
        static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 5757;
        private View fragmentView;
        private ImageView picPreview;
        static final int TAKE_PICTURE = 115;
        static final int OPEN_GALLERY = 110;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog;
            // Pass null as the parent view because its going in the dialog layout
            fragmentView = inflater.inflate(R.layout.add_new_note_dialog_fragment, null);
            builder.setView(fragmentView);

            /* initialize imageUri with the default picture in case the user doesn't want to use
              a custom picture
             */
            ((MainActivity) getActivity()).imageUri = Uri.parse(DEFAULT_PICTURE_PATH);

            picPreview = (ImageView) fragmentView.findViewById(R.id.foodpicture_preview);
            FloatingActionButton cameraButton = (FloatingActionButton) fragmentView.findViewById(R.id.camera_button);
            Button okButton = (Button) fragmentView.findViewById(R.id.ok_button);
            Button cancelButton = (Button) fragmentView.findViewById(R.id.cancel_button);
            FloatingActionButton galleryButton = (FloatingActionButton) fragmentView.findViewById(R.id.gallery_button);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).newNoteName
                            = (EditText) fragmentView.findViewById(R.id.new_note_name);
                    //transfer control back to the Fragment's host to check if the name
                    //already exits
                    if (!((MainActivity) getActivity()).onDialogPositiveClick()) {
                        dismiss();
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.deleteImageOnDevice(((MainActivity) getActivity()).imageUri, getActivity());
                    dismiss();
                }
            });

            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //this will request camera permission, which will try to take a picture
                    requestUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });

            galleryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery(); //TODO test valid
                }
            });


            return builder.create();
        }

        private void takePicture() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((MainActivity) getActivity()).imageUri = Utils.createUri(this);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, ((MainActivity) getActivity()).imageUri);
            startActivityForResult(intent, TAKE_PICTURE);

            ((MainActivity) getActivity()).getCache = false;
            Log.v(TAG, "leaving MainActivity, getCache = " + ((MainActivity) getActivity()).getCache);
        }

        /**
         * Source: http://androidbitmaps.blogspot.ca/2015/04/loading-images-in-android-part-iii-pick.html
         */
        private void openGallery() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), OPEN_GALLERY);
        }


        /**
         * Handling result from startActivityForResult
         *
         * @param requestCode
         * @param resultCode
         * @param data
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
                setPicturePreview();
            } else if (requestCode == OPEN_GALLERY && resultCode == Activity.RESULT_OK) {
                Uri galleryImageUri = data.getData();
                ((MainActivity) getActivity()).imageUri = galleryImageUri;
                Glide.with(this)
                        .load(galleryImageUri)
                        .centerCrop()
                        .into(picPreview);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                ((MainActivity) getActivity()).imageUri = Uri.parse(Utils.DEFAULT_PICTURE_PATH);
            }

        }

        private void setPicturePreview() {
            Log.v(TAG, "GETTING PICTURE: " + ((MainActivity) getActivity()).imageUri.toString());
            Glide.with(this)
                    .load(((MainActivity) getActivity()).imageUri)
                    .centerCrop()
                    .into(picPreview);
        }


        /**
         * Helper method to request device permissions
         * If permission is denied, request again
         * If permission requested is camera and is already granted, take the picture
         * If permission requested is external storage and is already granted, check the camera
         * permission
         *
         * @param devicePermission could be either camera permission or external storage permission
         * @param requestCode
         */
        private void requestUserPermission(String devicePermission, int requestCode) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    devicePermission)
                    == PackageManager.PERMISSION_DENIED) {
                // try requesting the permission again.
                Log.v(TAG, "requesting permission");
                if (Build.VERSION.SDK_INT >= 23)
                    requestPermissions(
                            new String[]{devicePermission},
                            requestCode);

            } else if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "camera permission already granted");
                takePicture();
            } else if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "camera permission not granted, requesting. External S permission granted");
                requestUserPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
            } else
                Log.v(TAG, "error requesting permission");
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[], int[] grantResults) {
            Log.v(TAG, "entering result");
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_CAMERA: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("tag", "trying to take picture");
                        // permission was granted, yay! Take the picture
                        takePicture();
                    } else {
                        Log.v("tag", "camera access permission denied");
                    }
                    return;
                }
                case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v("tag", "ask for camera permission");
                        // permission was granted, yay! Ask for camera permission
                        requestUserPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        Log.v("tag", "external storage writing permission denied");
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
            }
        }
    }

    private static class mFoodTouchHelperCallback extends ItemTouchHelper.Callback {

        private Paint paint = new Paint();
        private final Bitmap deleteIcon;
        private final Context context;

        private final ItemTouchHelperAdapter adapter;

        mFoodTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context) {
            this.context = context;
            this.adapter = adapter;
            this.deleteIcon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_delete);
        }

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
            adapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.7f;
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue * 100;
        }

        @Override
        public void onChildDraw(Canvas c,
                                RecyclerView recyclerView,
                                final RecyclerView.ViewHolder viewHolder,
                                float dX,
                                float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (isCurrentlyActive && actionState == ACTION_STATE_SWIPE) {
                paint.setColor(0xffffffff);
                if (Build.VERSION.SDK_INT >= 23)
                    paint.setColor(context.getResources().getColor(R.color.colorAccent, null));
                View view = viewHolder.itemView;

                // the background rectangle
                c.drawRect(view.getRight() + dX, view.getTop(), view.getRight(), view.getBottom(), paint);

                // scale the image according to dX
                int leftCoor = view.getRight() - 50 - deleteIcon.getWidth();
                int topCoor = view.getTop() + (view.getBottom() - view.getTop() - deleteIcon.getHeight()) / 2;
                int rightCoor = leftCoor + deleteIcon.getWidth();
                int bottomCoor = topCoor + deleteIcon.getHeight();

                float scaleThreshold = 250;
                float scaleFactor = Math.abs(dX) / scaleThreshold;

                float l_scaled = view.getRight() - 50 - deleteIcon.getWidth() * scaleFactor;
                float t_scaled = view.getTop() + (view.getBottom() - view.getTop() - deleteIcon.getHeight() * scaleFactor) / 2;
                float r_scaled = leftCoor + deleteIcon.getWidth() * scaleFactor;
                float b_scaled = topCoor + deleteIcon.getHeight() * scaleFactor;

                //animation from -150 horizontal displacement to
                //-250 displacement
                if (dX <= -150 && dX >= -scaleThreshold) {
                    c.drawBitmap(deleteIcon, null, new Rect((int) l_scaled, (int) t_scaled, (int) r_scaled, (int) b_scaled), paint);
                } else if (dX < -scaleThreshold) {
                    c.drawBitmap(deleteIcon, null,
                            new Rect(leftCoor, topCoor, rightCoor, bottomCoor), paint);
                }
            }
        }
    }

}
