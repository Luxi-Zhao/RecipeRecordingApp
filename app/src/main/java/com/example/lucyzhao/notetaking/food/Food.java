package com.example.lucyzhao.notetaking.food;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.lucyzhao.notetaking.Utils;

import java.io.IOException;
import java.io.Serializable;

import static com.example.lucyzhao.notetaking.Utils.DEFAULT_PICTURE_PATH;

/**
 * Created by LucyZhao on 2016/10/29.
 */
public class Food implements Serializable {
    private String title;
    private String imageUriString;
    private final int id;

    public Food(String title, String imageUriString, Context context) {
        this.title = title;
        this.imageUriString = imageUriString;

        /* initialize a unique id for this food */
        int lastIdNumber = Utils.getLastFoodId(context);
        this.id = lastIdNumber + 1;
        Utils.setLastFoodId(context, this.id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUriString() {
        return imageUriString;
    }

    public void setImageUriString(String imageUriString) {
        this.imageUriString = imageUriString;
    }

    public int getId() {
        return id;
    }

}
