package com.example.amit_mitzmacher_tal_mitzmacher.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Recipe recipe);

    @Delete
    void delete(Recipe recipe);

    @Update
    void update(Recipe recipe);

    // Retrieving all recipes for a specific user
    @Query("SELECT * FROM recipes WHERE userId = :userId")
    LiveData<List<Recipe>> getAllRecipes(String userId);

    // Checking if a recipe from the API already exists for the current user
    @Query("SELECT * FROM recipes WHERE apiId = :apiId AND userId = :userId LIMIT 1")
    LiveData<Recipe> getRecipeByApiId(String apiId, String userId);

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    LiveData<Recipe> getRecipeById(int id);

    // Search for recipes by ingredient for a specific user
    @Query("SELECT * FROM recipes WHERE userId = :userId AND ingredients LIKE '%' || :ingredient || '%'")
    LiveData<List<Recipe>> findRecipesByIngredient(String userId, String ingredient);

    // Retrieving recipes by category for a specific user
    @Query("SELECT * FROM recipes WHERE userId = :userId AND category = :category")
    LiveData<List<Recipe>> getRecipesByCategory(String userId, String category);

    // Retrieving the list of categories used by the current user
    @Query("SELECT DISTINCT category FROM recipes WHERE userId = :userId")
    LiveData<List<String>> getAllCategories(String userId);

    // Retrieve only the current user's favorites
    @Query("SELECT * FROM recipes WHERE userId = :userId AND isFavorite = 1")
    LiveData<List<Recipe>> getFavoriteRecipes(String userId);
}