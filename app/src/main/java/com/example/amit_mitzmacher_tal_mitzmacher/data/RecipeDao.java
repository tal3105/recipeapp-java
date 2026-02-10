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

    // שליפת כל המתכונים של משתמש ספציפי
    @Query("SELECT * FROM recipes WHERE userId = :userId")
    LiveData<List<Recipe>> getAllRecipes(String userId);

    // בדיקה אם מתכון מה-API כבר קיים אצל המשתמש הנוכחי
    @Query("SELECT * FROM recipes WHERE apiId = :apiId AND userId = :userId LIMIT 1")
    LiveData<Recipe> getRecipeByApiId(String apiId, String userId);

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    LiveData<Recipe> getRecipeById(int id);

    // חיפוש מתכונים לפי מרכיב עבור משתמש ספציפי
    @Query("SELECT * FROM recipes WHERE userId = :userId AND ingredients LIKE '%' || :ingredient || '%'")
    LiveData<List<Recipe>> findRecipesByIngredient(String userId, String ingredient);

    // שליפת מתכונים לפי קטגוריה עבור משתמש ספציפי
    @Query("SELECT * FROM recipes WHERE userId = :userId AND category = :category")
    LiveData<List<Recipe>> getRecipesByCategory(String userId, String category);

    // שליפת רשימת הקטגוריות שהמשתמש הנוכחי השתמש בהן
    @Query("SELECT DISTINCT category FROM recipes WHERE userId = :userId")
    LiveData<List<String>> getAllCategories(String userId);

    // שליפת המועדפים של המשתמש הנוכחי בלבד
    @Query("SELECT * FROM recipes WHERE userId = :userId AND isFavorite = 1")
    LiveData<List<Recipe>> getFavoriteRecipes(String userId);
}