package com.example.nutritiontracker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.nutritiontracker.hdr.ExposureCalculator;
import com.example.nutritiontracker.hdr.HdrProcessor;
import com.example.nutritiontracker.models.FoodInfo;
import com.example.nutritiontracker.models.FoodItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CameraActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB3ko6UXDKy569IsC3MxbeYd9POFQWmnq0";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static ListView listView;
    private ImageView mMainImage;
    private static ApiNinjasAPI nutritionixAPI;
    private static final String BASE_URL = "https://api.api-ninjas.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        nutritionixAPI = retrofit.create(ApiNinjasAPI.class);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
            builder
                    .setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
        });

        mMainImage = findViewById(R.id.main_image);
        listView = findViewById(R.id.foodListView);
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = createUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    private Uri createUri() {
        File imageFile = new File(getApplicationContext().getFilesDir(),"camera_photo.jpg");
        return FileProvider.getUriForFile(getApplicationContext(),"com.example.cse464.fileProvider",imageFile); // Match authority here
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = createUri();
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                // Convert Bitmap to 2D array
                int[][] pixelArray = convertBitmapTo2DArray(bitmap);

                // Apply Laplacian filter
                Bitmap laplacianBitmap = applyLaplacianFilter(pixelArray);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<CameraActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(CameraActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response, CameraActivity.this);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

    }

    private void callCloudVision(final Bitmap bitmap) {

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    // Inside your class
    private static String convertResponseToString(BatchAnnotateImagesResponse response, Activity activity) {
        StringBuilder message = new StringBuilder("It is likely to be one of these below:\n\n");
        List<FoodItem> foodItemList = new ArrayList<>();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        AtomicInteger successfulCalls = new AtomicInteger(0);
        int totalCalls = labels != null ? labels.size() : 0;

        if (labels != null) {
            for (EntityAnnotation label : labels) {

                Call<List<FoodInfo>> call = nutritionixAPI.getFoodInfo(label.getDescription(),"Q27eDkP4m8MTODd5pQyOHzm8PbmLZGxqgYbBeBsp");

                call.enqueue(new Callback<List<FoodInfo>>() {
                    @Override
                    public void onResponse(Call<List<FoodInfo>> call, Response<List<FoodInfo>> response) {

                        if (response.isSuccessful()) {
                            List<FoodInfo> foodInfoList = response.body();
                            if (foodInfoList != null && !foodInfoList.isEmpty()) {
                                FoodInfo foodInfo = foodInfoList.get(0);
                                if (foodInfo != null) {

                                    double calories = foodInfo.getCalories();
                                    String successMessage = label.getDescription() + " " + calories + "\n";
                                    message.append(successMessage);
                                    String foodName = label.getDescription();
                                    // Create a FoodItem object and add it to the list
                                    FoodItem foodItem = new FoodItem(foodName, calories, 0);
                                    foodItemList.add(foodItem);
                                }

                                Log.d("ERROR3", "ERROR1");
                            }

                        }
                        // Increment the successful calls counter atomically
                        if (successfulCalls.incrementAndGet() == totalCalls) {
                            updateUIWithMessage(activity, foodItemList);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FoodInfo>> call, Throwable t) {
                        message.append("Failed to get information for: ").append(label.getDescription()).append("\n");

                        // Increment the successful calls counter atomically
                        if (successfulCalls.incrementAndGet() == totalCalls) {
                            updateUIWithMessage(activity, foodItemList);
                        }
                    }
                });
            }
        } else {
            message.append("nothing");
        }


        return message.toString();
    }

    private static void updateUIWithMessage(Activity activity, List<FoodItem> foodItemList) {
        if (activity != null && !activity.isFinishing()) {
            listView = activity.findViewById(R.id.foodListView);
            ArrayList<String> labelsAndCalories = new ArrayList<>();
            for (FoodItem foodItem : foodItemList) {
                labelsAndCalories.add(foodItem.getName() + ": " + foodItem.getCalories() + " calories. (100gr)");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, labelsAndCalories);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = labelsAndCalories.get(position);
                String[] info = selectedItem.split(": ");

                // Prompt the user for the count using an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Enter grams ");
                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_NUMBER); // Set input type to number
                builder.setView(input);

                builder.setPositiveButton("OK", (dialog, which) -> {
                    String countStr = input.getText().toString();
                    if (!TextUtils.isEmpty(countStr)) {
                        int count = Integer.parseInt(countStr);

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            String foodName = info[0];
                            String[] cal = info[1].split(" ");

                            HashMap<String, Object> foodItems = new HashMap<>();
                            foodItems.put("name", foodName);
                            foodItems.put("calories", Double.valueOf(cal[0]));
                            foodItems.put("count", count); // Add the count to the HashMap

                            // Creating a reference specific to the authenticated user's ID and the current date
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("UserProfiles").child(userId).child(currentDate);

                            // Pushing the food items under the user's reference for the current date
                            userRef.push().setValue(foodItems)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Data saved successfully
                                            Toast.makeText(activity, "Food details saved!", Toast.LENGTH_SHORT).show();

                                            // Start the Summary activity after saving the data
                                            Intent intent = new Intent(activity, Summary.class);
                                            activity.startActivity(intent);
                                            activity.finish(); // Finish the current activity if needed
                                        } else {
                                            // Failed to save data
                                            Toast.makeText(activity, "Failed to save food details", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // User is not authenticated
                            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle empty count input
                        Toast.makeText(activity, "Please enter a count", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                builder.show();
            });
        }
    }

    private int[][] convertBitmapTo2DArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[][] pixelArray = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelArray[x][y] = bitmap.getPixel(x, y);
            }
        }

        return pixelArray;
    }

    private Bitmap applyLaplacianFilter(int[][] pixelArray) {
        int width = pixelArray.length;
        int height = pixelArray[0].length;

        int[][] laplacianFilter = {
                {0, -1, 0},
                {-1, 4, -1},
                {0, -1, 0}
        };

        int[][] resultArrayR = new int[width][height];
        int[][] resultArrayG = new int[width][height];
        int[][] resultArrayB = new int[width][height];

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int sumR = 0, sumG = 0, sumB = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        sumR += Color.red(pixelArray[x + i][y + j]) * laplacianFilter[i + 1][j + 1];
                        sumG += Color.green(pixelArray[x + i][y + j]) * laplacianFilter[i + 1][j + 1];
                        sumB += Color.blue(pixelArray[x + i][y + j]) * laplacianFilter[i + 1][j + 1];
                    }
                }
                // Ensure the result is clamped to the valid color range (0 to 255)
                resultArrayR[x][y] = Math.max(0, Math.min(255, sumR + Color.red(pixelArray[x][y])));
                resultArrayG[x][y] = Math.max(0, Math.min(255, sumG + Color.green(pixelArray[x][y])));
                resultArrayB[x][y] = Math.max(0, Math.min(255, sumB + Color.blue(pixelArray[x][y])));
            }
        }

        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Apply the enhanced pixel values to the result Bitmap
                resultBitmap.setPixel(x, y, Color.rgb(resultArrayR[x][y], resultArrayG[x][y], resultArrayB[x][y]));
            }
        }

        return resultBitmap;
    }

    private Bitmap applyGaussianFilter(int[][] pixelArray) {
        int width = pixelArray.length;
        int height = pixelArray[0].length;

        // Gaussian kernel
        double[][] kernel = {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };

        int[][] resultArrayR = new int[width][height];
        int[][] resultArrayG = new int[width][height];
        int[][] resultArrayB = new int[width][height];

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                double sumR = 0, sumG = 0, sumB = 0;
                double sumWeight = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        double weight = kernel[i + 1][j + 1];
                        sumR += Color.red(pixelArray[x + i][y + j]) * weight;
                        sumG += Color.green(pixelArray[x + i][y + j]) * weight;
                        sumB += Color.blue(pixelArray[x + i][y + j]) * weight;
                        sumWeight += weight;
                    }
                }

                // Ensure the result is clamped to the valid color range (0 to 255)
                resultArrayR[x][y] = (int) Math.max(0, Math.min(255, sumR / sumWeight));
                resultArrayG[x][y] = (int) Math.max(0, Math.min(255, sumG / sumWeight));
                resultArrayB[x][y] = (int) Math.max(0, Math.min(255, sumB / sumWeight));
            }
        }

        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Apply the enhanced pixel values to the result Bitmap
                resultBitmap.setPixel(x, y, Color.rgb(resultArrayR[x][y], resultArrayG[x][y], resultArrayB[x][y]));
            }
        }

        return resultBitmap;
    }




}
