package com.example.lucyzhao.notetaking;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by LucyZhao on 2016/10/29.
 */
public class Food implements Serializable{
    private final String title;
    private final Bitmap image;

    public Food( String title, Bitmap image ) {
        this.title = title;
        this.image = image;
    }

    public String getTitle(){
        return title;
    }

    public Bitmap getImage() { return image; }
}
