package com.example.nutritiontracker.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;


public class UserProfile implements Serializable {
    private String name;
    private String gender;
    private int age;
    private double height; // in meters
    private double weight; // in kilograms
    private double dailyCalorieIntake;

    private double activityFactor;


    public UserProfile() {
        // Default constructor required for Firebase
    }

    public UserProfile(String name, String gender, int age, double height, double weight, double activityFactor) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.activityFactor = activityFactor;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setDailyCalorieIntake(double dailyCalorieIntake) {
        this.dailyCalorieIntake = dailyCalorieIntake;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public double getDailyCalorieIntake() {
        return dailyCalorieIntake;
    }

    // Getter and Setter for activity factor
    public double getActivityFactor() {
        return activityFactor;
    }

    public void setActivityFactor(double activityFactor) {
        this.activityFactor = activityFactor;
    }


    // Method to calculate BMR using Mifflin-St Jeor formula
    public double calculateBMR() {
        if (gender.equals("Male")) {
            return (10 * weight)  + (6.25 * height) - (5 * age) + 5;
        } else {
            return (10 * weight)  + (6.25 * height) - (5 * age) - 161;
        }
    }

    // Method to calculate daily calorie intake based on BMR and activity factor
    public void calculateDailyCalorieIntake() {
        this.dailyCalorieIntake = calculateBMR() * activityFactor;
    }

    // Method to save user profile to Firebase
    public void saveToFirebase(UserProfile userProfile) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("UserProfiles").child(userId);
        // Assuming you have corresponding keys in the Firebase database for each field
        userRef.child("name").setValue(userProfile.name);
        userRef.child("age").setValue(userProfile.age);
        userRef.child("height").setValue(userProfile.height);
        userRef.child("weight").setValue(userProfile.weight);
        userRef.child("gender").setValue(userProfile.gender);
        userRef.child("activityFactor").setValue(userProfile.activityFactor);
        userRef.child("dailyCalorieIntake").setValue(userProfile.dailyCalorieIntake);

    }
}
