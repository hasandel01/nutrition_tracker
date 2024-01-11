package com.example.nutritiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutritiontracker.models.UserProfile;

public class UserInfoActivity extends AppCompatActivity {

    EditText editTextName, editTextHeight, editTextWeight, editTextAge;
    RadioGroup radioGroupGender;
    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        editTextName = findViewById(R.id.editTextName);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextAge = findViewById(R.id.editTextAge);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        SeekBar activitySeekBar = findViewById(R.id.activitySeekBar);
        TextView activityLevelTextView = findViewById(R.id.activityLevelTextView);

        activitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String[] activityLevels = {"Sedentary (little or none) ",
                                        "Lightly Active (1-2 days in a week)",
                                        "Moderately Active (3-4 days in a week)",
                                        "Very Active (6-7 days in a week)",
                                        "Extra Active (physical job & exercise daily"};

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String selectedActivity = activityLevels[progress];
                activityLevelTextView.setText(selectedActivity); // Show the selected activity level to the user
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });

        buttonSubmit.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String heightStr = editTextHeight.getText().toString().trim();
            String weightStr = editTextWeight.getText().toString().trim();
            String ageStr = editTextAge.getText().toString().trim();

            if (!name.isEmpty() && !heightStr.isEmpty() && !weightStr.isEmpty() && !ageStr.isEmpty()) {
                try {
                    float height = Float.parseFloat(heightStr);
                    float weight = Float.parseFloat(weightStr);
                    int age = Integer.parseInt(ageStr);

                    int progress = activitySeekBar.getProgress();
                    double activityFactor;
                    switch (progress) {
                        case 1:
                            activityFactor = 1.375; // Lightly Active
                            break;
                        case 2:
                            activityFactor = 1.55; // Moderately Active
                            break;
                        case 3:
                            activityFactor = 1.725; // Very Active
                            break;
                        case 4:
                            activityFactor = 1.9; // Extra Active
                            break;
                        default:
                            activityFactor = 1.2; // Default to Sedentary for safety
                            break;
                    }

                    // Get selected gender
                    int selectedId = radioGroupGender.getCheckedRadioButtonId();
                    RadioButton radioButton = findViewById(selectedId);
                    String gender = radioButton.getText().toString();

                    // Create UserProfile object and perform necessary actions
                    UserProfile userProfile = new UserProfile(name, gender, age, height, weight, activityFactor);

                    // Calculate daily calorie intake
                    userProfile.calculateDailyCalorieIntake();
                    // Save user profile to Firebase
                    userProfile.saveToFirebase(userProfile);

                    Intent intent = new Intent(getApplicationContext(), Summary.class);
                    startActivity(intent);
                    finish();

                } catch (NumberFormatException e) {
                    Toast.makeText(UserInfoActivity.this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UserInfoActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
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
}
