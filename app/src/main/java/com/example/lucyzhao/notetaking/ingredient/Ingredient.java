package com.example.lucyzhao.notetaking.ingredient;

import java.io.Serializable;

/**
 * Created by LucyZhao on 2017/8/16.
 */

public class Ingredient implements Serializable {
    private final String name;
    private final float amount;
    private final String unit;

    public Ingredient(String name, float amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public float getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return name + " " + amount + " " + unit;
    }
}
