package com.example.lucyzhao.notetaking;




import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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


import java.io.File;
import java.util.ArrayList;

public class RecipePageActivity extends AppCompatActivity {
    private static final String TAG = RecipePageActivity.class.getSimpleName();

    EditText titleEt;
    TextView titleTv;
    Button titleOk;

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
        titleRL.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
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
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        Glide.with(this)
                .load(food.getImageUriString())
                .centerCrop()
                .into(foodPic);

        //foodPic.setImageBitmap(food.getImage(this, false));


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


        /* -------- setting up procedure list recycler view ---------*/

        ArrayList newPrList = Utils.getList(this, folder_name, Utils.CHILD_PATH_PROCEDURE);

        if(newPrList != null && !newPrList.isEmpty() && newPrList.get(0) instanceof String){
            prList = (ArrayList<String>) newPrList;
        }

        prListAdapter = new ProcedureListAdapter(prList);

        RecyclerView prRecyclerView = (RecyclerView) findViewById(R.id.procedure_recyclerView);
        prRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        prRecyclerView.setAdapter(prListAdapter);

        /* -------- setting up add procedure button -----------------*/
        addProcedureBtn = (ToggleButton) findViewById(R.id.add_procedure_button);
        addProcedureBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
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
                }
                else{
                    getSupportFragmentManager().popBackStack(); //TODO there maybe more fragments
                    //TODO we'll consider that later
                }
            }
        });

        /*--------- setting up ingredient list recycler view --------*/
        ArrayList newIngList = Utils.getList(this, folder_name, Utils.CHILD_PATH_INGREDIENTS);

        if(newIngList != null && !newIngList.isEmpty() && newIngList.get(0) instanceof Ingredient){
            ingList = (ArrayList<Ingredient>) newIngList;
        }

        ingListAdapter = new IngredientsListAdapter(ingList);

        RecyclerView ingRecyclerView = (RecyclerView) findViewById(R.id.ingredients_recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this){
//            @Override
//            public boolean canScrollVertically() {
//                return false;
//            }  //disable recycler view scrolling
//        });
        ingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ingRecyclerView.setAdapter(ingListAdapter);

        ingRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        /* --------- setting up add ingredient button ---------------*/
        addIngredientBtn = (ToggleButton) findViewById(R.id.add_ingredient_button);
        addIngredientBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment = new NewIngredientFragment();
                    //fragment.setEnterTransition(new Slide(Gravity.RIGHT));

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
    protected void onStop(){
        super.onStop();
        Log.v(TAG, "in onStop");
        Utils.saveList(this, ingList, folder_name, Utils.CHILD_PATH_INGREDIENTS);
        Utils.saveList(this, prList, folder_name, Utils.CHILD_PATH_PROCEDURE);
    }


    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG, "in onRestart");
    }

    private void addNewIngredient(Ingredient ingredient){
        Log.v(TAG, "adding new ingredient " + ingredient.toString());
        ingList.add(ingredient);
        ingListAdapter.notifyItemInserted(ingListAdapter.getItemCount() - 1);
        Log.v(TAG, "notified item inserted at pos: " + (ingListAdapter.getItemCount()-1));
    }

    private void addNewProcedure(String text){
        Log.v(TAG, "adding new procedure " + text);
        prList.add(text);
        prListAdapter.notifyItemInserted(prListAdapter.getItemCount() - 1);
        Log.v(TAG, "notified item inserted at pos: " + (prListAdapter.getItemCount()-1));
    }


    /**
     * save user input
     * TODO move the logic to onStop
     * TODO edit this logic so that it only shows edit text when the user clicks on it
     */
    private void saveTitleToMem(){

        // save food list in case of title change
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

    private void titleInEditMode(boolean inEditMode){
        if(inEditMode){
            titleEt.setText(titleTv.getText().toString());
            titleEt.setVisibility(View.VISIBLE);
            titleOk.setVisibility(View.VISIBLE);
            titleTv.setVisibility(View.INVISIBLE);
        }
        else {
            titleEt.setVisibility(View.GONE);
            titleOk.setVisibility(View.INVISIBLE);
            titleTv.setVisibility(View.VISIBLE);
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

    public static class NewProcedureFragment extends Fragment{
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
                    ((RecipePageActivity) getActivity()).addNewProcedure(text.getText().toString());
                    ((RecipePageActivity) getActivity()).addProcedureBtn.toggle();
                }
            });
            return fragmentView;
        }
    }

}
