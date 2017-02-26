package com.example.lucyzhao.notetaking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class RecipePageActivity extends AppCompatActivity {
    EditText ingredientsEditText;
    EditText procedureEditText;
    private String file_name;
    private static final String CHILD_PATH_INGREDIENTS = "_ingredients";
    private static final String CHILD_PATH_PROCEDURE = "_procedure";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        /* ------------ set note page title ----------------*/
        Intent intent = getIntent();
        String title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        file_name = title;
        TextView titleTv = (TextView) findViewById(R.id.single_page_title);
        titleTv.setText(title);

        /*--------------set note page picture --------------*/
        Bundle extras = getIntent().getExtras();
        Bitmap bmp = extras.getParcelable(MainActivity.EXTRA_PIC);
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        foodPic.setImageBitmap(bmp);

        /* -------------find EditText contents -------------*/
        procedureEditText = (EditText) findViewById(R.id.procedure);
        ingredientsEditText = (EditText) findViewById(R.id.ingredients);

        //read information saved in internal storage and displays it
        displayInfo();

        /* ---------------test display all files-------------*/
        File file = getFilesDir();
        File[] listOfFiles = file.listFiles();
        for(File f : listOfFiles){
            Log.v("internal str files", f.getAbsolutePath());
        }
        /* ---------------test display all files-------------*/
    }

    /**
     * save user input 
     * @param view
     */
    public void saveInfo(View view){
        saveToFolder(ingredientsEditText, CHILD_PATH_INGREDIENTS);
        saveToFolder(procedureEditText, CHILD_PATH_PROCEDURE);
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
            FileOutputStream fos = openFileOutput(file_name + folder_name, MODE_PRIVATE);
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
            FileInputStream fis = openFileInput(file_name + folder_name);
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
