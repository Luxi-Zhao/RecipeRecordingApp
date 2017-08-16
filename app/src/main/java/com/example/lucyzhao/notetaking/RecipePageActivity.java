package com.example.lucyzhao.notetaking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Stack;

import static com.example.lucyzhao.notetaking.Utils.CHILD_PATH_INGREDIENTS;
import static com.example.lucyzhao.notetaking.Utils.CHILD_PATH_PROCEDURE;
import static com.example.lucyzhao.notetaking.Utils.LIST_FILE_NAME;
import static com.example.lucyzhao.notetaking.Utils.getCachedFoodList;

public class RecipePageActivity extends AppCompatActivity {
    private static final String TAG = RecipePageActivity.class.getSimpleName();

    EditText titleEt;
    EditText ingredientsEditText;
    EditText procedureEditText;
    private String file_name;
    private String initial_title;

    private float density;
    /* ingredient fields */
    private int ingredient_prevNumOfLines = 1;
    private Stack<Integer> ingredient_bulletIds = new Stack<>();
    /* procedure fields */
    private int procedure_prevNumOfLines = 1;
    private Stack<Integer> procedure_bulletIds = new Stack<>();

    private int clickingPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

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
        file_name = Integer.toString(food.getId());
        initial_title = title;
        titleEt = (EditText) findViewById(R.id.single_page_title);
        titleEt.setText(title);

        /*--------------set note page picture --------------*/
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        foodPic.setImageBitmap(food.getImage(this));

        /* -------------find EditText contents -------------*/
        procedureEditText = (EditText) findViewById(R.id.procedure_edit_text);
        ingredientsEditText = (EditText) findViewById(R.id.ingredients_edit_text);
        final RelativeLayout ingredientsRL = (RelativeLayout) findViewById(R.id.ingredients_relative_layout);
        final RelativeLayout procedureRL = (RelativeLayout) findViewById(R.id.procedure_relative_layout);
        //read information saved in internal storage and displays it
        displayInfo();

        /* ---------------TODO delete test display all files-------------*/
        File file = getFilesDir();
        File[] listOfFiles = file.listFiles();
        for(File f : listOfFiles){
            Log.v("internal str files", f.getAbsolutePath());
        }
        /* ---------------test display all files-------------*/


        /* ----------deal with ingredients edit text----------*/
        density = getApplicationContext().getResources().getDisplayMetrics().density;
        ingredient_bulletIds.push(R.id.first_bullet);

        ingredientsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int numOfLines = ingredientsEditText.getLineCount();
                Log.v("line num", numOfLines+" lines");
                if(numOfLines > ingredient_prevNumOfLines){
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                   // lp.setMarginStart((int)(10*density));
                    lp.addRule(RelativeLayout.BELOW, ingredient_bulletIds.peek());
                    lp.addRule(RelativeLayout.ALIGN_START, ingredient_bulletIds.peek());
                    lp.width = (int)(10*density);
                    lp.height = (int)(10*density);
                    lp.topMargin = (int)(ingredientsEditText.getTextSize() + ingredientsEditText.getLineSpacingExtra() - lp.height/2 -8 );

                    ImageView bullet = new ImageView(RecipePageActivity.this);
                    bullet.setBackgroundResource(R.drawable.dot);
                    bullet.setLayoutParams(lp);
                    int lastBulletId = View.generateViewId();
                    ingredient_bulletIds.push(lastBulletId);
                    bullet.setId(lastBulletId);
                    ingredientsRL.addView(bullet);
                }
                else if(numOfLines < ingredient_prevNumOfLines){
                    View bullet = ingredientsRL.findViewById(ingredient_bulletIds.peek());
                    ingredientsRL.removeView(bullet);
                    ingredient_bulletIds.pop();
                }
                ingredient_prevNumOfLines = numOfLines;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        procedure_bulletIds.push(R.id.second_bullet);
        procedureEditText.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int numOfLines = procedureEditText.getLineCount();
                Log.v("line num", numOfLines+" lines");
                if(numOfLines > procedure_prevNumOfLines){
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    // lp.setMarginStart((int)(10*density));
                    lp.addRule(RelativeLayout.BELOW, procedure_bulletIds.peek());
                    lp.addRule(RelativeLayout.ALIGN_START, procedure_bulletIds.peek());
                    lp.width = (int)(10*density);
                    lp.height = (int)(10*density);
                    lp.topMargin = (int)(procedureEditText.getTextSize() + procedureEditText.getLineSpacingExtra() - lp.height/2 -8 );

                    ImageView bullet = new ImageView(RecipePageActivity.this);
                    bullet.setBackgroundResource(R.drawable.dot);
                    bullet.setLayoutParams(lp);
                    int lastBulletId = View.generateViewId();
                    procedure_bulletIds.push(lastBulletId);
                    bullet.setId(lastBulletId);
                    procedureRL.addView(bullet);
                }
                else if(numOfLines < procedure_prevNumOfLines){
                    View bullet = procedureRL.findViewById(procedure_bulletIds.peek());
                    procedureRL.removeView(bullet);
                    procedure_bulletIds.pop();
                }
                procedure_prevNumOfLines = numOfLines;

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.v(TAG, "in onStop");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.v(TAG, "in onRestart");
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
        retrieveFromFolder(ingredientsEditText, CHILD_PATH_INGREDIENTS);
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
            File file = new File(getFilesDir().getPath() + "/" + file_name + folder_name);
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
            FileInputStream fis = new FileInputStream (new File(getFilesDir().getPath() + "/" + file_name + folder_name));
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


}
