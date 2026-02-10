package com.example.amit_mitzmacher_tal_mitzmacher.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static Retrofit retrofit = null;

    public static RecipeService getRecipeService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
<<<<<<< HEAD
        // התיקון כאן: משתמשים ב-create ולא ב-getItem
=======

>>>>>>> d5c6860ed25c87fa55c0c3d03a0613ad37fdc9e1
        return retrofit.create(RecipeService.class);
    }
}