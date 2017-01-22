package com.example.lucyzhao.notetaking;

import java.io.Serializable;

/**
 * Created by LucyZhao on 2016/10/29.
 */
public class Food implements Serializable{
    private final String title;

    public Food( String title ) {
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
