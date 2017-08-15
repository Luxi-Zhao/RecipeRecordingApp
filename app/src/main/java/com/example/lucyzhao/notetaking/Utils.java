package com.example.lucyzhao.notetaking;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by LucyZhao on 2017/8/15.
 */

public class Utils {
    public static final String LIST_FILE_NAME = "food_list";
    public static final String CHILD_PATH_INGREDIENTS = "_ingredients";
    public static final String CHILD_PATH_PROCEDURE = "_procedure";

    /**
     * Rotate the bitmap by certain degrees
     * source
     * http://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
     * Users should only take pictures in portrait EDIT: landscape is ok on a real device
     * @param bm
     * @return
     */
    public static Bitmap processBitmap(Bitmap bm, int rotationInDegrees){

        Matrix matrix = new Matrix();
        //rotate the matrix around a pivot point
        matrix.setRotate(rotationInDegrees, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        int newSize;
        if(bm.getHeight() > bm.getWidth())
            newSize = bm.getWidth();
        else newSize = bm.getHeight();
        Bitmap resizedBitmap = Bitmap.createBitmap(rotatedBitmap, 0,0, newSize, newSize);
        return resizedBitmap;
    }

    public static Bitmap processBitmap(Bitmap bm){
        int newSize;
        if(bm.getHeight() > bm.getWidth())
            newSize = bm.getWidth();
        else newSize = bm.getHeight();
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0,0, newSize, newSize);
        return resizedBitmap;
    }
}
