package com.example.amit_mitzmacher_tal_mitzmacher.data.network;

import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeService {
    @GET("search.php")
    Call<RecipeApiResponse> searchRecipes(
            @Query("s") String query
    );
    @GET("lookup.php")
    Call<RecipeApiResponse> getRecipeDetails(
            @Query("i") String id
    );
    @GET("random.php")
    Call<RecipeApiResponse> getRandomRecipe();
}