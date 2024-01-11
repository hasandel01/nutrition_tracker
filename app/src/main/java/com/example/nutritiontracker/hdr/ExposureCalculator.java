package com.example.nutritiontracker.hdr;

import android.graphics.Bitmap;
import android.graphics.Color;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ExposureCalculator {

    public static float calculateExposureValue(Bitmap bitmap) {
        if (bitmap == null) {
            return 0.5f; // Default value if bitmap is null
        }

        int[][] histograms = generateHistograms(bitmap);

        // Calculate the mean intensity for each channel
        float meanRed = calculateMeanIntensity(histograms[0]);
        float meanGreen = calculateMeanIntensity(histograms[1]);
        float meanBlue = calculateMeanIntensity(histograms[2]);

        // Normalize mean intensities to the range [0, 1]
        float normalizedMeanRed = meanRed / 255.0f;
        float normalizedMeanGreen = meanGreen / 255.0f;
        float normalizedMeanBlue = meanBlue / 255.0f;

        // Return the average normalized mean intensity as the exposure value
        return (normalizedMeanRed + normalizedMeanGreen + normalizedMeanBlue) / 3.0f;
    }

    private static int[][] generateHistograms(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[][] histograms = new int[3][256]; // Assuming 8-bit image and 3 color channels (RGB)

        // Iterate through each pixel and update the histograms
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);

                histograms[0][Color.red(pixel)]++;    // Red channel
                histograms[1][Color.green(pixel)]++;  // Green channel
                histograms[2][Color.blue(pixel)]++;   // Blue channel
            }
        }

        return histograms;
    }

    private static float calculateMeanIntensity(int[] histogram) {
        int totalPixels = 0;
        int sum = 0;

        // Calculate the sum of intensities and the total number of pixels
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
            totalPixels += histogram[i];
        }

        // Calculate the mean intensity
        return (float) sum / totalPixels;
    }
}

