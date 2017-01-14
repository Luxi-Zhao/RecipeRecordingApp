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
import android.widget.ImageView;
import android.widget.TextView;

public class RecipePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page);

        Intent intent = getIntent();
        String title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        TextView titleTv = (TextView) findViewById(R.id.single_page_title);
        titleTv.setText(title);

        Bundle extras = getIntent().getExtras();
        Bitmap bmp = extras.getParcelable(MainActivity.EXTRA_PIC);
        ImageView foodPic = (ImageView) findViewById(R.id.single_page_foodpicture);
        foodPic.setImageBitmap(bmp);

    }

}
