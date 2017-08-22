package com.example.lucyzhao.notetaking;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

import static com.example.lucyzhao.notetaking.Utils.DEFAULT_PICTURE_PATH;

/**
 * Created by LucyZhao on 2016/10/29.
 */
 public class Food implements Serializable{
    private String title;
    private final String imageUriString;
    private final int id;

    public Food( String title, String imageUriString, Context context ) {
        this.title = title;
        this.imageUriString = imageUriString;

        /* initialize a unique id for this food */
        int lastIdNumber = Utils.getLastFoodId(context);
        this.id = lastIdNumber + 1;
        Utils.setLastFoodId(context, this.id);
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUriString() { return imageUriString; }

    public int getId() { return id; }

    /**
     * similar to setImagePreview in MainActivity
     * convert a Uri string to a Bitmap
     * Process the Bitmap to make it point to the right direction
     * if the picture is taken from the camera; if it's the default
     * picture, use it directly
     * TODO use glide
     * @param context The context in which this method is used
     * @return
     */
    public Bitmap getImage(Context context, boolean square) {
        Uri imageUri = Uri.parse(imageUriString);
        Bitmap originalBitmap = null;
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), imageUri);
        } catch (IOException e) {
            Log.v("IOException","bitmap cannot be created from uri");
            e.printStackTrace();
        }
        /* TODO note: CHANGED FROM 90 TO 0 ON REAL DEVICE*/
        if(imageUriString.equals(DEFAULT_PICTURE_PATH)){
            Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
            return Bitmap.createBitmap(1, 1, conf); // this creates a MUTABLE bitmap
        }
        else if(square){
            return Utils.processBitmap(originalBitmap);
        }
        else return originalBitmap;
    }
}
