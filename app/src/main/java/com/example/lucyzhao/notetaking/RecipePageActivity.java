package com.example.lucyzhao.notetaking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RecipePageActivity extends AppCompatActivity {
    EditText editText;
    private String file_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        Intent intent = getIntent();
        String title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        file_name = title;
        TextView titleTv = (TextView) findViewById(R.id.single_page_title);
        titleTv.setText(title);

        Bundle extras = getIntent().getExtras();
        Bitmap bmp = extras.getParcelable(MainActivity.EXTRA_PIC);
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        foodPic.setImageBitmap(bmp);

        editText = (EditText) findViewById(R.id.user_input);
        //read information saved in internal storage and displays it
        String savedInfo;
        try {
            FileInputStream fis = openFileInput(file_name);
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

    /**
     * save user input 
     * @param view
     */
    public void saveInfo(View view){

        String info = editText.getText().toString();
        try {
            FileOutputStream fos = openFileOutput(file_name,MODE_PRIVATE);
            fos.write(info.getBytes());
            fos.close();
            //displays a message saying information saved
            Toast.makeText(getApplicationContext(),"input saved",Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
