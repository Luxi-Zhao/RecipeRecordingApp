package com.example.lucyzhao.notetaking;




import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.example.lucyzhao.notetaking.Utils.CHILD_PATH_INGREDIENTS;
import static com.example.lucyzhao.notetaking.Utils.CHILD_PATH_PROCEDURE;
import static com.example.lucyzhao.notetaking.Utils.dpToPx;

public class RecipePageActivity extends AppCompatActivity {
    private static final String TAG = RecipePageActivity.class.getSimpleName();

    EditText titleEt;
    EditText ingredientsEditText;
    EditText procedureEditText;

    ToggleButton addIngredientBtn;
    private String folder_name;
    private String initial_title;
    private int clickingPos = -1;

    private ArrayList<Ingredient> ingList = new ArrayList<>();
    private IngredientsListAdapter ingListAdapter;


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
        String title = food.getTitle();
        folder_name = Integer.toString(food.getId());
        initial_title = title;
        titleEt = (EditText) findViewById(R.id.single_page_title);
        titleEt.setText(title);

        /*--------------set note page picture --------------*/
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        foodPic.setImageBitmap(food.getImage(this));

        /* -------------find EditText contents -------------*/
        procedureEditText = (EditText) findViewById(R.id.procedure_edit_text);
        //ingredientsEditText = (EditText) findViewById(R.id.ingredients_edit_text);
        //read information saved in internal storage and displays it
        displayInfo();

        /* ---------------TODO delete test display all files-------------*/
        File file = getFilesDir();
        File[] listOfFiles = file.listFiles();
        for(File f : listOfFiles){
            Log.v(TAG, "internal files: " + f.getAbsolutePath());
            if(f.isDirectory()){
                for(File child : f.listFiles()){
                    Log.v(TAG, "child: " + child.getAbsolutePath());
                }
            }
        }
        /* ---------------test display all files-------------*/

        /*--------- setting up ingredient list recycler view --------*/
        ArrayList newIngList = Utils.getList(this, folder_name, Utils.CHILD_PATH_INGREDIENTS_TEMP);

        if(newIngList != null && !newIngList.isEmpty() && newIngList.get(0) instanceof Ingredient){
            ingList = (ArrayList<Ingredient>) newIngList;
        }

        ingListAdapter = new IngredientsListAdapter(ingList);

        RecyclerView recyclerView;
        recyclerView = (RecyclerView) findViewById(R.id.ingredients_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(ingListAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        /* --------- setting up add ingredient button ---------------*/
        addIngredientBtn = (ToggleButton) findViewById(R.id.add_ingredient_button);

//        addIngredientBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //addNewIngredient();
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                NewIngredientFragment fragment = new NewIngredientFragment();
//                fragment.setEnterTransition(new Slide(Gravity.RIGHT));
//                fragmentTransaction.add(R.id.add_ingredient_container, fragment);
//                fragmentTransaction.commit();
//            }
//
//        });
        addIngredientBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    //addNewIngredient();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment = new NewIngredientFragment();
                    //fragment.setEnterTransition(new Slide(Gravity.RIGHT));

                    fragmentTransaction.setCustomAnimations(
                            R.anim.enter_from_right,
                            R.anim.exit_from_left,
                            R.anim.enter_from_right,
                            R.anim.exit_from_left
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
    protected void onStop(){
        super.onStop();
        Log.v(TAG, "in onStop");
        Utils.saveList(this, ingList, folder_name, Utils.CHILD_PATH_INGREDIENTS_TEMP);
    }


    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG, "in onRestart");
    }

    private void addNewIngredient(Ingredient ingredient){
        ingList.add(ingredient);
        Log.v(TAG, "item count in list is: " + ingListAdapter.getItemCount());
        ingListAdapter.notifyItemInserted(ingListAdapter.getItemCount() - 1);
    }


    /**
     * save user input 
     * @param view
     */
    public void saveInfo(View view){
        saveToFolder(ingredientsEditText, CHILD_PATH_INGREDIENTS);
        saveToFolder(procedureEditText, CHILD_PATH_PROCEDURE);

        String cur_title = titleEt.getText().toString();
        if(!cur_title.equals(initial_title)){
           Log.v(TAG, "title is edited");
           ArrayList<Food> foodList = Utils.getCachedFoodList(this);
           if(foodList != null && clickingPos != -1){
               foodList.get(clickingPos).setTitle(cur_title);
               Utils.saveFoodList(getApplicationContext(),foodList);
               Log.v(TAG, "foodlist saved");
           }
           else{
               Log.v(TAG, "something's wrong");
           }
        }
        //displays a message saying information saved
        Toast.makeText(getApplicationContext(),"input saved",Toast.LENGTH_LONG).show();
    }

    public void displayInfo(){
//        retrieveFromFolder(ingredientsEditText, CHILD_PATH_INGREDIENTS);
        retrieveFromFolder(procedureEditText, CHILD_PATH_PROCEDURE);
    }

    /**
     * Helper method that converts an EditText to a string and saves the string to a folder
     * @param editText the EditText to retrieve the string from
     * @param folder_name the folder to save to
     */
    private void saveToFolder(EditText editText, String folder_name){
        String textToSave = editText.getText().toString();
        try {
            File file = new File(getFilesDir().getPath() + "/" + this.folder_name + folder_name);
            if(!file.exists()){
                boolean result = file.getParentFile().mkdirs();
                Log.v(TAG, Boolean.toString(result));
                boolean file_result = file.createNewFile();
                Log.v(TAG, "result for creating a new file: " + Boolean.toString(file_result));
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(textToSave.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method that retrieves a saved string from a folder and sets an EditText to
     * the retrieved string
     * @param editText the EditText to set
     * @param folder_name the folder to retrieve from
     */
    private void retrieveFromFolder(EditText editText, String folder_name) {
        String savedInfo;
        try {
            FileInputStream fis = new FileInputStream (new File(getFilesDir().getPath() + "/" + this.folder_name + folder_name));
            BufferedReader bufferedReader
                    = new BufferedReader(new InputStreamReader(fis));
            StringBuffer stringBuffer = new StringBuffer();
            while((savedInfo = bufferedReader.readLine()) != null ) {
                stringBuffer.append(savedInfo + "\n");
            }
            editText.setText(stringBuffer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class NewIngredientFragment extends Fragment{
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
                    int ingAmount = Integer.parseInt(amount.getText().toString());
                    String ingUnit = unit.getText().toString();
                    Ingredient ing = new Ingredient(ingName, ingAmount, ingUnit);
                    ((RecipePageActivity) getActivity()).addNewIngredient(ing);
                    ((RecipePageActivity) getActivity()).addIngredientBtn.toggle();
                }
            });
            return fragmentView;
        }
    }

}
