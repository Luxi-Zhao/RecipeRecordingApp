package com.example.lucyzhao.notetaking;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static java.security.AccessController.getContext;

/**
 * Created by LucyZhao on 2017/8/15.
 */

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static final String EXTRA_CLICKING_POSITION = "clicking_pos";
    public static final String EXTRA_FOOD_OBJECT = "food_object";

    public static final String FOOD_LIST_KEY = "food_list";

    public static final String CHILD_PATH_INGREDIENTS = "ingredients";
    public static final String CHILD_PATH_PROCEDURE = "procedure";

    public static final String DEFAULT_PICTURE_PATH = "android.resource://com.example.lucyzhao.notetaking/drawable/foodpic2";
    public static final String DEFAULT_PICTURE_URI_PATH = "/drawable/foodpic2";
    public static final String ID_KEY = "id_key";

    public static final String ADD_INGREDIENT_FRAGMENT = "add_ingredient_frag";
    public static final String ADD_PROCEDURE_FRAGMENT = "add_proc_frag";


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

    public static void saveFoodList(Context context, ArrayList<Food> foodList) {
        try {
            FileOutputStream fos = context.openFileOutput(FOOD_LIST_KEY, MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(foodList);
            objectOutputStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Food> getCachedFoodList(Context context) {
        ArrayList<Food> foodList = null;
        try {
            FileInputStream fis = context.openFileInput(FOOD_LIST_KEY);
            ObjectInputStream objectInputStream = new ObjectInputStream(fis);
            foodList = (ArrayList<Food>) objectInputStream.readObject();
            objectInputStream.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return foodList;
    }

    /**
     *
     * @param c
     * @param list
     * @param foldername
     * @param filename
     */
    public static void saveList(Context c, ArrayList list, String foldername, String filename) {
        try {
            Log.v(TAG, "saving list");
            File file = new File(c.getFilesDir().getPath() + "/" + foldername + "/" + filename);
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file,false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(list);
            objectOutputStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Ingredient> getList(Context c, String foldername, String filename) {
        Log.v(TAG, "getting list");
        ArrayList list = null;
        try {
            FileInputStream fis = new FileInputStream (
                    new File(c.getFilesDir().getPath() + "/" + foldername + "/" + filename));
            ObjectInputStream objectInputStream = new ObjectInputStream(fis);
            list = (ArrayList) objectInputStream.readObject();
            objectInputStream.close();
            fis.close();
            Log.v(TAG, "fis closed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getLastFoodId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(ID_KEY, 0); //0 is the default value
    }

    public static void setLastFoodId(Context context, int newValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ID_KEY, newValue);
        editor.commit();
    }

    /**
     * Delete an entire food folder
     * @param context
     * @param foodId
     * @return
     */
    public static boolean deleteFoodDir(Context context, int foodId){

        File dir = new File(context.getFilesDir() + "/" + foodId);
        Log.v(TAG, "dir to delete is: " + dir.getAbsolutePath());
        if (dir.exists()) {
            deleteRecursive(dir);
        }
        Log.v(TAG, "is the dir deleted? " + !dir.exists());
        return !dir.exists();
    }

    /**
     * Source:
     * https://stackoverflow.com/questions/13410949/how-to-delete-folder-from-internal-storage-in-android
     * @param fileOrDirectory
     */
    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                Log.v(TAG, "child is: " + child.getPath());
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static int dpToPx(Context c, int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
