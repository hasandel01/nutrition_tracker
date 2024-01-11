package com.example.nutritiontracker.models;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodResponse {
    @SerializedName("parsed")
    private List<ParsedFood> parsedFoods;

    @SerializedName("hints")
    private List<ParsedFood> hintFoods;
    public List<ParsedFood> getParsedFoods() {
        return parsedFoods;
    }

    public List<ParsedFood> getHintedFoods() {
        return hintFoods;
    }

    public static class ParsedFood {
        @SerializedName("food")
        private FoodInfo foodInfo;

        public FoodInfo getFoodInfo() {
            return foodInfo;
        }
    }

    public static class HintedFood {
        @SerializedName("food")
        private FoodInfo foodInfo;

        public FoodInfo getFoodInfo() {
            return foodInfo;
        }
    }


    public static class FoodInfo {
        @SerializedName("foodId")
        private String foodId;

        @SerializedName("label")
        private String label;

        @SerializedName("nutrients")
        private Nutrients nutrients;

        public Nutrients getNutrients() {
            return nutrients;
        }
    }

    public static class Nutrients {
        @SerializedName("ENERC_KCAL")
        private double calories;

        public double getCalories() {
            return calories;
        }
    }
}
