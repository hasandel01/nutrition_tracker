package com.example.nutritiontracker;

import com.example.nutritiontracker.models.FoodInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiNinjasAPI {
    @GET("v1/nutrition")
    Call<List<FoodInfo>> getFoodInfo(
            @Query("query") String query,
            @Query("x-api-key") String x_api_key
    );
}
