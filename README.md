# Nutrition Tracker App

This repository contains the source code for the Nutrition Tracker Android app. The app uses the Google Vision API and API Ninjas to analyze and track nutritional information from images of food items.

## Project Overview

The Nutrition Tracker app is designed to help users monitor and track the nutritional content of their meals. Users can take pictures of their food, and the app uses the Google Vision API for image recognition to identify food items. The nutritional information is then fetched from the API Ninjas Nutrition API to provide users with detailed data on the calories, macronutrients, and other nutritional components of their meals.

## Technologies Used

- [Android Studio](https://developer.android.com/studio): The official integrated development environment for Android app development.
- [Google Vision API](https://cloud.google.com/vision): A powerful image analysis API that provides image recognition capabilities.
- [API Ninjas Nutrition API](https://apininjas.com/nutrition-api/): An API that offers detailed nutritional information for various food items.

## How I Built the Project

### 1. Setting Up the Android Project

I used Android Studio to create the Android app. The project structure includes activities, fragments, and the necessary resources for the user interface.

### 2. Integrating Google Vision API

I integrated the Google Vision API into the app to perform image recognition. This involved configuring the necessary API credentials, handling image capture or selection, and processing the results.

### 3. Fetching Nutritional Information from API Ninjas

I incorporated the API Ninjas Nutrition API to fetch detailed nutritional information based on the recognized food items. This involved making HTTP requests to the API and parsing the JSON responses.

### 4. User Interface Design

I designed the user interface to provide a seamless experience for users to capture images, view recognized items, and access detailed nutritional information.

## How to Run the Project

1. **Clone the Repository:**

    ```bash
    git clone https://github.com/hasandel01/nutrition_tracker.git
    cd nutrition-tracker
    ```

2. **Open in Android Studio:**

    - Open Android Studio.
    - Choose `File` > `Open` and select the `nutrition-tracker` folder.

3. **Configure API Keys:**

    - Obtain API keys for the Google Vision API and API Ninjas Nutrition API.
    - Update the API key placeholders in the project code with your actual API keys.

4. **Run the Application:**

    - Connect an Android device or use an emulator.
    - Click the "Run" button in Android Studio to build and run the app.

## Additional Notes

- Make sure to handle API keys securely and consider restricting their usage based on security best practices.
- Ensure that the app has the necessary permissions to access the device's camera for image capture.

## License

This project is licensed under the [MIT License](LICENSE).
