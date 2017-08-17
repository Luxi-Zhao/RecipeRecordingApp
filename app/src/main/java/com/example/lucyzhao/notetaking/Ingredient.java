package com.example.lucyzhao.notetaking;

import java.io.Serializable;

/**
 * Created by LucyZhao on 2017/8/16.
 */

public class Ingredient implements Serializable{
    private final String name;
    private final int amount;
    private final String unit;

    public Ingredient(String name, int amount, String unit){
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public String getName(){
        return name;
    }

    public int getAmount(){
        return amount;
    }

    public String getUnit(){
        return unit;
    }
}
