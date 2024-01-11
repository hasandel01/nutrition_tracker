package com.example.nutritiontracker.hdr;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.List;

public class HdrProcessor {

    // Adjust the weights based on the importance of each exposure
    private static final float WEIGHT_LOW = 0.25f;
    private static final float WEIGHT_NORMAL = 0.5f;
    private static final float WEIGHT_HIGH = 0.25f;

    public static Bitmap mergeExposures(List<Bitmap> exposures) {
        if (exposures == null || exposures.size() < 2) {
            return null;
        }

        // Assuming all exposures have the same dimensions
        int width = exposures.get(0).getWidth();
        int height = exposures.get(0).getHeight();

        // Create an empty bitmap to hold the final HDR image
        Bitmap hdrImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Loop through each pixel in the images and merge them
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int mergedPixel = mergePixels(exposures, x, y);
                hdrImage.setPixel(x, y, mergedPixel);
            }
        }

        return hdrImage;
    }

    private static int mergePixels(List<Bitmap> exposures, int x, int y) {
        // Initialize merged color channels
        int mergedRed = 0;
        int mergedGreen = 0;
        int mergedBlue = 0;

        // Iterate through exposures and merge color channels
        for (Bitmap exposure : exposures) {
            int pixel = exposure.getPixel(x, y);

            // Extract color channels
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            // Apply weights and accumulate values
            mergedRed += red * getExposureWeight(exposure);
            mergedGreen += green * getExposureWeight(exposure);
            mergedBlue += blue * getExposureWeight(exposure);
        }

        // Normalize values to 0-255 range
        mergedRed /= exposures.size();
        mergedGreen /= exposures.size();
        mergedBlue /= exposures.size();

        // Combine color channels into a single pixel
        return Color.rgb(mergedRed, mergedGreen, mergedBlue);
    }

    private static float getExposureWeight(Bitmap exposure) {
        // Adjust weights based on the importance of each exposure
        float exposureValue = calculateExposureValue(exposure);

        if (exposureValue < 0.33f) {
            return WEIGHT_LOW;
        } else if (exposureValue < 0.66f) {
            return WEIGHT_NORMAL;
        } else {
            return WEIGHT_HIGH;
        }
    }

    private static float calculateExposureValue(Bitmap exposure) {
        // You may implement a method to calculate exposure value based on the image histogram or metadata
        // For simplicity, returning a constant value in this example
        return 0.5f;
    }
}
