package com.example.nutritiontracker.models;

public class FoodInfo {
    private String name;
    private double calories;
    private double servingSizeG;
    private double fatTotalG;
    private double fatSaturatedG;
    private double proteinG;
    private int sodiumMg;
    private int potassiumMg;
    private int cholesterolMg;
    private double carbohydratesTotalG;
    private double fiberG;
    private double sugarG;

    // Constructors, getters, and setters
    // Constructor
    public FoodInfo(String name, double calories, double servingSizeG, double fatTotalG, double fatSaturatedG,
                    double proteinG, int sodiumMg, int potassiumMg, int cholesterolMg, double carbohydratesTotalG,
                    double fiberG, double sugarG) {
        this.name = name;
        this.calories = calories;
        this.servingSizeG = servingSizeG;
        this.fatTotalG = fatTotalG;
        this.fatSaturatedG = fatSaturatedG;
        this.proteinG = proteinG;
        this.sodiumMg = sodiumMg;
        this.potassiumMg = potassiumMg;
        this.cholesterolMg = cholesterolMg;
        this.carbohydratesTotalG = carbohydratesTotalG;
        this.fiberG = fiberG;
        this.sugarG = sugarG;
    }

    // Getters and setters for each field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getServingSizeG() {
        return servingSizeG;
    }

    public void setServingSizeG(double servingSizeG) {
        this.servingSizeG = servingSizeG;
    }

    public double getFatTotalG() {
        return fatTotalG;
    }

    public void setFatTotalG(double fatTotalG) {
        this.fatTotalG = fatTotalG;
    }

    public double getFatSaturatedG() {
        return fatSaturatedG;
    }

    public void setFatSaturatedG(double fatSaturatedG) {
        this.fatSaturatedG = fatSaturatedG;
    }

    public double getProteinG() {
        return proteinG;
    }

    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    public int getSodiumMg() {
        return sodiumMg;
    }

    public void setSodiumMg(int sodiumMg) {
        this.sodiumMg = sodiumMg;
    }

    public int getPotassiumMg() {
        return potassiumMg;
    }

    public void setPotassiumMg(int potassiumMg) {
        this.potassiumMg = potassiumMg;
    }

    public int getCholesterolMg() {
        return cholesterolMg;
    }

    public void setCholesterolMg(int cholesterolMg) {
        this.cholesterolMg = cholesterolMg;
    }

    public double getCarbohydratesTotalG() {
        return carbohydratesTotalG;
    }

    public void setCarbohydratesTotalG(double carbohydratesTotalG) {
        this.carbohydratesTotalG = carbohydratesTotalG;
    }

    public double getFiberG() {
        return fiberG;
    }

    public void setFiberG(double fiberG) {
        this.fiberG = fiberG;
    }

    public double getSugarG() {
        return sugarG;
    }

    public void setSugarG(double sugarG) {
        this.sugarG = sugarG;
    }
}
