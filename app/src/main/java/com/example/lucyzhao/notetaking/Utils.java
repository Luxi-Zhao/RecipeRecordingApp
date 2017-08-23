package com.example.lucyzhao.notetaking;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
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


    /**
     * give imageUri another value instead of Uri.fromFile(photoFile) because
     * file: // can no longer be used
     */
    public static Uri createUri(DialogFragment fragment){
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        //insert values into a table at EXTERNAL_CONTENT_URI and get the URI of the newly
        //created row
        return fragment.getActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * checks if a procedure text is a uri
     * @param str
     * @return
     */
    public static boolean isUri(String str){
        if(str.startsWith("file") || str.startsWith("content"))
            return true;
        else return false;
    }


}
