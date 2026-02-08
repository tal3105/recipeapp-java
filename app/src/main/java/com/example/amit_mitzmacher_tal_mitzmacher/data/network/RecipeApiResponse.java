package com.example.amit_mitzmacher_tal_mitzmacher.data.network;

import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecipeApiResponse {
    @SerializedName("meals")
    private List<Recipe> recipes;

    public List<Recipe> getRecipes() {
        return recipes;
    }
}