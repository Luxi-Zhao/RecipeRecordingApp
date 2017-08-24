package com.example.lucyzhao.notetaking;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.bumptech.glide.Glide;
import com.example.lucyzhao.notetaking.food.Food;
import com.example.lucyzhao.notetaking.ingredient.Ingredient;
import com.example.lucyzhao.notetaking.ingredient.IngredientsListAdapter;


import java.io.File;
import java.util.ArrayList;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;
import static com.example.lucyzhao.notetaking.Utils.DEFAULT_PICTURE_PATH;

public class RecipePageActivity extends AppCompatActivity {
    private static final String TAG = RecipePageActivity.class.getSimpleName();

    EditText titleEt;
    TextView titleTv;
    Button titleOk;

    ImageView foodPic;

    ToggleButton addIngredientBtn;
    ToggleButton addProcedureBtn;
    private String folder_name;
    private String initial_title;
    private int clickingPos = -1;

    private ArrayList<Ingredient> ingList = new ArrayList<>();
    private IngredientsListAdapter ingListAdapter;

    private ArrayList<String> prList = new ArrayList<>();
    private ProcedureListAdapter prListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);
        Log.v(TAG, "in onCreate");

        /* ------------ get intent --------------*/
        Intent intent = getIntent();
        /* ------- find out which item got clicked ----------*/
        clickingPos = intent.getIntExtra(Utils.EXTRA_CLICKING_POSITION, -1);
        /* ------- get the food object ---------------*/
        Food food = (Food) intent.getSerializableExtra(Utils.EXTRA_FOOD_OBJECT);
        Log.v(TAG, "food info: title " + food.getTitle());
        Log.v(TAG, "food info: id " + food.getId());

        /* ------------ set note page title ----------------*/
        final String title = food.getTitle();
        folder_name = Integer.toString(food.getId());
        initial_title = title;
        titleOk = (Button) findViewById(R.id.title_ok_button);
        titleEt = (EditText) findViewById(R.id.single_page_title);
        titleTv = (TextView) findViewById(R.id.single_page_title_tv);
        titleTv.setText(title);
        titleInEditMode(false);
        RelativeLayout titleRL = (RelativeLayout) findViewById(R.id.title_rl);
        titleRL.setClickable(true);
        titleRL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                titleInEditMode(true);
                return true;
            }
        });
        titleRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleInEditMode(false);
            }
        });
        titleOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTitleToMem();
                titleTv.setText(titleEt.getText().toString());
                titleInEditMode(false);
            }
        });

        /*--------------set note page picture --------------*/
        foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);

        if (food.getImageUriString().equals(DEFAULT_PICTURE_PATH)) {
            Bitmap.Config conf = Bitmap.Config.ARGB_4444;
            foodPic.setImageBitmap(Bitmap.createBitmap(1, 1, conf));  // this creates a MUTABLE bitmap
        } else {
            Glide.with(this)
                    .load(food.getImageUriString())
                    .centerCrop()
                    .into(foodPic);
        }

        foodPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                DialogFragment fragment = new ChangePicDialogFragment();
                fragment.show(fragmentManager, "change");
                return true;
            }
        });

        /* ---------------TODO delete test display all files-------------*/
        File file = getFilesDir();
        File[] listOfFiles = file.listFiles();
        for (File f : listOfFiles) {
            Log.v(TAG, "internal files: " + f.getAbsolutePath());
            if (f.isDirectory()) {
                for (File child : f.listFiles()) {
                    Log.v(TAG, "child: " + child.getAbsolutePath());
                }
            }
        }
        /* ---------------test display all files-------------*/


        /* -------- setting up procedure list recycler view ---------*/

        ArrayList newPrList = Utils.getList(this, folder_name, Utils.CHILD_PATH_PROCEDURE);

        if (newPrList != null && !newPrList.isEmpty() && newPrList.get(0) instanceof String) {
            prList = (ArrayList<String>) newPrList;
        }

        prListAdapter = new ProcedureListAdapter(prList);

        RecyclerView prRecyclerView = (RecyclerView) findViewById(R.id.procedure_recyclerView);
        prRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        prRecyclerView.setAdapter(prListAdapter);

        ItemTouchHelper.Callback callback =
                new mProcedureTouchHelperCallback(prListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(prRecyclerView);


        /* -------- setting up add procedure button -----------------*/
        addProcedureBtn = (ToggleButton) findViewById(R.id.add_procedure_button);
        addProcedureBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment = new NewProcedureFragment();

                    fragmentTransaction.setCustomAnimations(
                            R.anim.enter_from_right,
                            R.anim.exit_from_left,
                            R.anim.enter_from_right,
                            R.anim.exit_from_left
                    );
                    fragmentTransaction.add(R.id.add_procedure_container, fragment);
                    fragmentTransaction.addToBackStack(Utils.ADD_PROCEDURE_FRAGMENT);
                    fragmentTransaction.commit();
                } else {
                    getSupportFragmentManager().popBackStack(); //TODO there maybe more fragments
                    //TODO we'll consider that later
                }
            }
        });

        /*--------- setup add picture button ----------------------*/
        Button addPictureButton = (Button) findViewById(R.id.add_picture_button);
        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = new AddProcPicDialogFragment();
                fragment.show(getSupportFragmentManager(), "add pic dialog fragment");
            }
        });

        /*--------- setting up ingredient list recycler view --------*/
        ArrayList newIngList = Utils.getList(this, folder_name, Utils.CHILD_PATH_INGREDIENTS);

        if (newIngList != null && !newIngList.isEmpty() && newIngList.get(0) instanceof Ingredient) {
            ingList = (ArrayList<Ingredient>) newIngList;
        }

        ingListAdapter = new IngredientsListAdapter(ingList);

        RecyclerView ingRecyclerView = (RecyclerView) findViewById(R.id.ingredients_recyclerView);

        ingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ingRecyclerView.setAdapter(ingListAdapter);

        new ItemTouchHelper(new mIngTouchHelperCallback(ingListAdapter))
                .attachToRecyclerView(ingRecyclerView);

        /* --------- setting up add ingredient button ---------------*/
        addIngredientBtn = (ToggleButton) findViewById(R.id.add_ingredient_button);
        addIngredientBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment = new NewIngredientFragment();

                    fragmentTransaction.setCustomAnimations(
                            R.anim.enter_from_right,
                            android.R.anim.slide_out_right,
                            R.anim.enter_from_right,
                            android.R.anim.slide_out_right
                    );
                    fragmentTransaction.add(R.id.add_ingredient_container, fragment);
                    fragmentTransaction.addToBackStack(Utils.ADD_INGREDIENT_FRAGMENT);
                    fragmentTransaction.commit();

                } else {
                    // The toggle is disabled

                    getSupportFragmentManager().popBackStack(); //TODO there maybe more fragments
                    //TODO we'll consider that later
//                    Fragment fragment = getFragmentManager().findFragmentById(R.id.add_ingredient_container);
//                    getFragmentManager().beginTransaction().remove(fragment).commit();

                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "in onStop");
        Utils.saveList(this, ingList, folder_name, Utils.CHILD_PATH_INGREDIENTS);
        Utils.saveList(this, prList, folder_name, Utils.CHILD_PATH_PROCEDURE);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "in onRestart");
    }

    private void addNewIngredient(Ingredient ingredient) {
        Log.v(TAG, "adding new ingredient " + ingredient.toString());
        ingList.add(ingredient);
        ingListAdapter.notifyItemInserted(ingListAdapter.getItemCount() - 1);
        Log.v(TAG, "notified item inserted at pos: " + (ingListAdapter.getItemCount() - 1));
    }

    private void addNewProcedure(String text) {
        Log.v(TAG, "adding new procedure " + text);
        if (!prList.contains(text)) {
            prList.add(text);
            prListAdapter.notifyItemInserted(prListAdapter.getItemCount() - 1);
            Log.v(TAG, "notified item inserted at pos: " + (prListAdapter.getItemCount() - 1));
        } else {
            Toast.makeText(getApplicationContext(), R.string.procedure_duplicate, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * save user input
     * TODO move the logic to onStop
     */
    private void saveTitleToMem() {

        // save food list in case of title change
        String cur_title = titleEt.getText().toString();
        if (!cur_title.equals(initial_title)) {
            Log.v(TAG, "title is edited");
            ArrayList<Food> foodList = Utils.getCachedFoodList(this);
            if (foodList != null && clickingPos != -1) {
                foodList.get(clickingPos).setTitle(cur_title);
                Utils.saveFoodList(getApplicationContext(), foodList);
                Log.v(TAG, "foodlist saved");
            } else {
                Log.v(TAG, "something's wrong");
            }
        }
    }

    /**
     * save the new picture to memory
     *
     * @param imageUriString non-empty
     */
    private void savePicToMem(String imageUriString) {
        ArrayList<Food> foodList = Utils.getCachedFoodList(this);
        foodList.get(clickingPos).setImageUriString(imageUriString);
        Utils.saveFoodList(getApplicationContext(), foodList);
        Log.v(TAG, "new pic and foodlist saved");
    }


    private void titleInEditMode(boolean inEditMode) {
        if (inEditMode) {
            titleEt.setText(titleTv.getText().toString());
            titleEt.setVisibility(View.VISIBLE);
            titleOk.setVisibility(View.VISIBLE);
            titleTv.setVisibility(View.INVISIBLE);
        } else {
            titleEt.setVisibility(View.GONE);
            titleOk.setVisibility(View.INVISIBLE);
            titleTv.setVisibility(View.VISIBLE);
        }
    }


    public static class NewIngredientFragment extends Fragment {
        Button okButton;
        EditText name;
        EditText amount;
        EditText unit;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View fragmentView = inflater.inflate(R.layout.add_new_ingredient_fragment, container, false);
            name = (EditText) fragmentView.findViewById(R.id.new_name);
            amount = (EditText) fragmentView.findViewById(R.id.new_amount);
            unit = (EditText) fragmentView.findViewById(R.id.new_unit);

            okButton = (Button) fragmentView.findViewById(R.id.ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ingName = name.getText().toString();
                    String ingAmountStr = amount.getText().toString();
                    String ingUnit = unit.getText().toString();
                    if (ingName.isEmpty() || ingAmountStr.isEmpty()) {
                        Toast.makeText(getContext(),
                                R.string.name_amount_empty, Toast.LENGTH_SHORT).show();
                    } else {
                        Ingredient ing = new Ingredient(ingName, Float.parseFloat(ingAmountStr), ingUnit);
                        ((RecipePageActivity) getActivity()).addNewIngredient(ing);
                        ((RecipePageActivity) getActivity()).addIngredientBtn.toggle();
                    }

                }
            });
            return fragmentView;
        }
    }

    public static class NewProcedureFragment extends Fragment {
        Button okButton;
        EditText text;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View fragmentView = inflater.inflate(R.layout.add_new_procedure_fragment, container, false);
            text = (EditText) fragmentView.findViewById(R.id.new_procedure);

            okButton = (Button) fragmentView.findViewById(R.id.new_procedure_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text.getText().toString().isEmpty()) {
                        Toast.makeText(v.getContext(), R.string.procedure_empty, Toast.LENGTH_SHORT).show();
                    } else {
                        ((RecipePageActivity) getActivity()).addNewProcedure(text.getText().toString());
                        ((RecipePageActivity) getActivity()).addProcedureBtn.toggle();
                    }
                }
            });
            return fragmentView;
        }
    }

    public static class ChangePicDialogFragment extends DialogFragment {
        private static final int OPEN_GALLERY = 101;
        private static final int TAKE_PICTURE = 99;

        static final int MY_PERMISSIONS_REQUEST_CAMERA = 5655;
        static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 5753;

        private String newImageUri = "";    //stores the address of a newly taken photo

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog;
            // Pass null as the parent view because its going in the dialog layout
            View fragmentView = inflater.inflate(R.layout.change_recipe_pic_fragment, null);
            builder.setView(fragmentView);

            Button newPicButton = (Button) fragmentView.findViewById(R.id.change_camera);
            final Button newGalButton = (Button) fragmentView.findViewById(R.id.change_gallery);

            newPicButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });

            newGalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery();
                }
            });

            return builder.create();
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

        private void takePicture() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri newUri = Utils.createUri(this);
            newImageUri = newUri.toString();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
            startActivityForResult(intent, TAKE_PICTURE);
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
            Log.v(TAG, "ENTERING ON ACTIVITY RESULT change fragment");
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == OPEN_GALLERY && resultCode == Activity.RESULT_OK) {
                Log.v(TAG, "getting uri");
                Uri galleryImageUri = data.getData();
                Log.v(TAG, "image uri is: " + galleryImageUri.getPath());

                handleNewPic(galleryImageUri.toString());
            } else if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
                handleNewPic(newImageUri);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.v(TAG, "result cancelled");
            }

        }

        /**
         * Helper method to update the ImageView and save the new image uri to memory
         *
         * @param newUriString new uri string to hold the picture, should not be empty
         */
        private void handleNewPic(String newUriString) {
            ((RecipePageActivity) getActivity()).savePicToMem(newUriString);
            // set food pic to new pic
            Glide.with(getActivity())
                    .load(newUriString)
                    .centerCrop()
                    .into(((RecipePageActivity) getActivity()).foodPic);

            dismiss();
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
            Log.v(TAG, "entering requesting user permission" + devicePermission);

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
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
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
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }

    }

    public static class AddProcPicDialogFragment extends DialogFragment {
        private static final int OPEN_GALLERY = 100;
        private static final int TAKE_PICTURE = 98;

        static final int MY_PERMISSIONS_REQUEST_CAMERA = 1111;
        static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2222;

        private String newImageUri = "";    //stores the address of a newly taken photo

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog;
            // Pass null as the parent view because its going in the dialog layout
            View fragmentView = inflater.inflate(R.layout.change_recipe_pic_fragment, null);
            builder.setView(fragmentView);

            Button newPicButton = (Button) fragmentView.findViewById(R.id.change_camera);
            final Button newGalButton = (Button) fragmentView.findViewById(R.id.change_gallery);

            newGalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery();
                }
            });

            newPicButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });
            return builder.create();
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

        private void takePicture() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri newUri = Utils.createUri(this);
            newImageUri = newUri.toString();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
            startActivityForResult(intent, TAKE_PICTURE);
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
            Log.v(TAG, "ENTERING ON ACTIVITY RESULT change fragment");
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == OPEN_GALLERY && resultCode == Activity.RESULT_OK) {
                Log.v(TAG, "getting uri");
                Uri galleryImageUri = data.getData();
                Log.v(TAG, "image uri is: " + galleryImageUri.getPath());
                ((RecipePageActivity) getActivity()).addNewProcedure(galleryImageUri.toString());
                dismiss();

            } else if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
                Log.v(TAG, "image uri is: " + newImageUri);
                ((RecipePageActivity) getActivity()).addNewProcedure(newImageUri);
                dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.v(TAG, "result cancelled");
            }

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
            Log.v(TAG, "entering requesting user permission" + devicePermission);

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
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
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
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }


    }

    private static class mProcedureTouchHelperCallback extends ItemTouchHelper.Callback {
        private final ItemTouchHelperAdapter mAdapter;

        mProcedureTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(UP | DOWN, LEFT | RIGHT);
        }


        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    private static class mIngTouchHelperCallback extends ItemTouchHelper.Callback {
        private final ItemTouchHelperAdapter adapter;

        mIngTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ACTION_STATE_IDLE, LEFT | RIGHT) | makeFlag(ACTION_STATE_SWIPE, LEFT | RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            adapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
}
