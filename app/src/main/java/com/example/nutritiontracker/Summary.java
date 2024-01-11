package com.example.nutritiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutritiontracker.adapters.FoodItemAdapter;
import com.example.nutritiontracker.models.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Summary extends AppCompatActivity {

    ImageView log_out;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    private TextView dailyIntakeMaintain;

    private TextView dailyIntakeWeightLoss;

    // Track currently displayed date
    private TextView monthDayText;

    private Calendar displayedDate;

    ListView dailyList;

    ImageView nextButton;

    FirebaseAuth mAuth;

    TextView totalCalories;

    private List<FoodItem> foodItemList; // Declare foodItemList as a field

    private double dailyCalorieIntake = 0.0; // Declare as a field

    private ValueEventListener userFoodListener; // Declare a field for the ValueEventListener

    private double totalCaloriesX = 0.0;

    private ImageView exclamationMark;

    ImageView camera;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("UserProfiles")
                    .child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // UserProfile data exists
                        UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            if (userProfile.getAge() == 0 || userProfile.getWeight() == 0 || userProfile.getHeight() == 0) {
                                // Navigate to UserInfoActivity to fill missing info
                                Intent intent = new Intent(Summary.this, UserInfoActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else {
                        // New user, navigate to UserInfoActivity to fill out the form
                        Intent intent = new Intent(Summary.this, UserInfoActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled
                }
            });
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        monthDayText = findViewById(R.id.monthDayText);
        log_out = findViewById(R.id.log_out);
        dailyList = findViewById(R.id.dailyList);
        mAuth = FirebaseAuth.getInstance();
        dailyIntakeMaintain = findViewById(R.id.dailyIntakeMaintain);
        dailyIntakeWeightLoss = findViewById(R.id.dailyIntakeWeightLoss);
        nextButton = findViewById(R.id.nextButton);
        nextButton.setVisibility(View.GONE); // Set initial visibility to GONE
        totalCalories = findViewById(R.id.totalDailyCalories);
        exclamationMark = findViewById(R.id.exclamationMark);
        exclamationMark.setVisibility(View.GONE);
        camera = findViewById(R.id.camera);
        exclamationMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a message when the user taps on the image
                Toast.makeText(getApplicationContext(), "You exceeded your recommended daily calorie intake for this day.", Toast.LENGTH_SHORT).show();
            }
        });

        foodItemList = new ArrayList<>(); // Initialize foodItemList here
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("UserProfiles").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        dailyCalorieIntake = userProfile.getDailyCalorieIntake();
                        dailyIntakeMaintain.setText("To maintain your weight: " + dailyCalorieIntake);
                        dailyIntakeWeightLoss.setText("To lose weight healthily: " + (dailyCalorieIntake - 500));
                    }
                } else {
                    // Handle if user profile doesn't exist
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });


        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate.getTime());


        DatabaseReference userFoodRef = FirebaseDatabase.getInstance().getReference().child("UserProfiles").child(userId).child(formattedDate);

        // Remove previous listener to prevent duplicates
        if (userFoodListener != null) {
            userFoodRef.removeEventListener(userFoodListener);
        }

        userFoodListener = userFoodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<FoodItem> foodItemList = new ArrayList<>();
                double totalCalories = 0.0;

                if (dataSnapshot.exists()) {

                    for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                        FoodItem foodItem = foodSnapshot.getValue(FoodItem.class);
                        if (foodItem != null) {
                            String name = foodItem.getName();
                            Double calories = foodItem.getCalories();
                            int count = foodItem.getCount();

                            foodItemList.add(new FoodItem(name,calories,count));

                            FoodItemAdapter adapter = new FoodItemAdapter(Summary.this,R.layout.food_item_row, foodItemList);
                            dailyList.setAdapter(adapter);
                        }
                    }
                } else {
                    // Handle if user profile or date node doesn't exist
                }

                for (FoodItem foodItem: foodItemList) {
                    totalCalories += foodItem.getCalories()*foodItem.getCount();
                }

                updateTotalCalories(totalCalories);
                updateVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }

        });

        // Initialize displayedDate to today's date
        displayedDate = Calendar.getInstance();
        // Display events for today initially
        updateDateText(); // Update the date text initially

        if(firebaseUser == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void updateTotalCalories(Double calories) {
        totalCalories.setText("TOTAL CALORIES: " + calories + " kcal.");
    }

    // Function to update the displayed date with day and date
    private void updateDateText() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(displayedDate.getTime());
        monthDayText.setText(formattedDate);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat dateFormatDatabase = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDateDatabase = dateFormatDatabase.format(displayedDate.getTime());

        DatabaseReference userFoodRef = FirebaseDatabase.getInstance().getReference().child("UserProfiles").child(userId).child(formattedDateDatabase);
        List<FoodItem> foodItemList = new ArrayList<>();

        userFoodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                totalCaloriesX = 0.0;
                foodItemList.clear();

                if (dataSnapshot.exists()) {

                    for (DataSnapshot foodSnapshot : dataSnapshot.getChildren()) {
                        FoodItem foodItem = foodSnapshot.getValue(FoodItem.class);
                        if (foodItem != null) {
                            String name = foodItem.getName();
                            Double calories = foodItem.getCalories();
                            int count = foodItem.getCount();

                            foodItemList.add(new FoodItem(name,calories,count));
                        }
                    }

                } else {
                    // Handle if user profile or date node doesn't exist
                }
                
                FoodItemAdapter adapter = new FoodItemAdapter(Summary.this, R.layout.food_item_row, foodItemList);
                dailyList.setAdapter(adapter);

                for (FoodItem foodItem: foodItemList) {
                    totalCaloriesX += foodItem.getCalories()*(foodItem.getCount()/100.0) ;
                }

                updateTotalCalories(totalCaloriesX);
                updateVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }


        });

    }

    public void previousDayAction(View view){
        // Move to previous day and fetch events
        displayedDate.add(Calendar.DAY_OF_MONTH, -1);
        updateDateText(); // Update the date text after changing the date
        // Re-check and update visibility of nextButton
        updateNextButtonVisibility();
    }

    public void nextDayAction(View view) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Move to next day and fetch events
        displayedDate.add(Calendar.DAY_OF_MONTH, 1);

        // Check if the displayed date is today's date
        if (displayedDate.after(today)) {
            // If displayed date is after today, reset it to today
            displayedDate = (Calendar) today.clone();
        }

        updateDateText(); // Update the date text after changing the date

        updateNextButtonVisibility();
    }

    public void cameraAction(View view) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
        finish();
    }

    public void editUserInfo(View view) {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        startActivity(intent);
        finish();
    }

    public void summaryAction(View view) {
        Intent intent = new Intent(getApplicationContext(), Summary.class);
        startActivity(intent);
        finish();
    }

    private void updateNextButtonVisibility() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Check if the displayed date is today's date
        if (displayedDate.equals(today)) {
            // Disable the "next" button
            nextButton.setVisibility(View.GONE);
        } else {
            // Enable the "next" button
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateVisibility() {

        if (totalCaloriesX > dailyCalorieIntake) {
            // Disable the "next" button
            exclamationMark.setVisibility(View.VISIBLE);
        } else {
            // Enable the "next" button
            exclamationMark.setVisibility(View.GONE);
        }
    }
}