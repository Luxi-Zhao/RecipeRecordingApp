package com.example.lucyzhao.notetaking;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

import static com.example.lucyzhao.notetaking.MainActivity.NewNoteDialogFragment.DEFAULT_PICTURE_PATH;
import static com.example.lucyzhao.notetaking.MainActivity.NewNoteDialogFragment.processBitmap;
import static java.security.AccessController.getContext;

/**
 * Created by LucyZhao on 2016/10/29.
 */
 public class Food implements Serializable{
    private final String title;
    private final String imageUriString;

    public Food( String title, String imageUriString ) {
        this.title = title;
        this.imageUriString = imageUriString;
    }

    public String getTitle(){
        return title;
    }

    public String getImageUriString() { return imageUriString; }

    /**
     * similar to setImagePreview in MainActivity
     * convert a Uri string to a Bitmap
     * Process the Bitmap to make it point to the right direction
     * if the picture is taken from the camera; if it's the default
     * picture, use it directly
     * @param context The context in which this method is used
     * @return
     */
    public Bitmap getImage(Context context) {
        Uri imageUri = Uri.parse(imageUriString);
        Bitmap originalBitmap = null;
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), imageUri);
        } catch (IOException e) {
            Log.v("IOException","bitmap cannot be created from uri");
            e.printStackTrace();
        }
        if(imageUriString.equals(DEFAULT_PICTURE_PATH))
            return originalBitmap;
        else return processBitmap(originalBitmap, 90);
    }
}
