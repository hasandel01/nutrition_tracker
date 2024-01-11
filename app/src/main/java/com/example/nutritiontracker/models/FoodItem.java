package com.example.nutritiontracker.models;

public class FoodItem {
    private String name;
    private double calories;
    private int count;  // Changed the count to int

    public FoodItem() {
        this.name = "name";
        this.calories = 0.0;
        this.count = 0;
    }

    public FoodItem(String foodName, double calories, int count) {
        this.name = foodName;
        this.calories = calories;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public int getCount() {  // Adjusted the getter method
        return count;
    }
}
