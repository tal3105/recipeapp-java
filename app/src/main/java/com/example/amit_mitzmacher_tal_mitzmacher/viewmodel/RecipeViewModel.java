package com.example.amit_mitzmacher_tal_mitzmacher.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.example.amit_mitzmacher_tal_mitzmacher.data.RecipeRepository;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {
    private final RecipeRepository repository;

    // רשימת המתכונים המוצגת כרגע (בבית, בחיפוש או במועדפים)
    private final MutableLiveData<List<Recipe>> currentRecipes = new MutableLiveData<>();
    private String lastSearchQuery = "";

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        // טעינה ראשונית של כל המתכונים של המשתמש המחובר
        loadAllRecipes();
    }

    // פעולות בסיסיות - ה-Repository כבר דואג להוסיף את ה-userId
    public void insert(Recipe recipe) { repository.insert(recipe); }
    public void update(Recipe recipe) { repository.update(recipe); }
    public void delete(Recipe recipe) { repository.delete(recipe); }

    // בדיקה אם מתכון API קיים אצל המשתמש
    public LiveData<Recipe> getRecipeByApiId(String apiId) {
        return repository.getRecipeByApiId(apiId);
    }

    public LiveData<Recipe> getRecipeById(int id) {
        return repository.getRecipeById(id);
    }

    public LiveData<List<Recipe>> getCurrentRecipes() {
        return currentRecipes;
    }

    // --- טעינה וסינון נתונים ---

    public void loadAllRecipes() {
        // ה-Repository מחזיר רק מתכונים של המשתמש הנוכחי
        repository.getAllRecipes().observeForever(currentRecipes::setValue);
    }

    public void searchLocal(String query) {
        repository.searchRecipesLocal(query).observeForever(currentRecipes::setValue);
    }

    public void searchApi(String query) {
        repository.searchRecipesApi(query).observeForever(recipes -> {
            if (recipes != null) {
                currentRecipes.setValue(recipes);
            }
        });
    }

    public void filterByCategory(String category) {
        if (category.equals("All") || category.isEmpty()) {
            loadAllRecipes();
        } else {
            repository.getRecipesByCategory(category).observeForever(currentRecipes::setValue);
        }
    }

    public void filterByFavorites() {
        repository.getFavoriteRecipes().observeForever(currentRecipes::setValue);
    }

    public void toggleFavorite(Recipe recipe) {
        recipe.setFavorite(!recipe.isFavorite());
        repository.upsert(recipe);
    }

    // --- קטגוריות ופרטי API ---

    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    public LiveData<Recipe> getRecipeDetailsFromApi(String apiId) {
        return repository.getRecipeDetailsFromApi(apiId);
    }

    public String getLastSearchQuery() { return lastSearchQuery; }
    public void setLastSearchQuery(String query) { this.lastSearchQuery = query; }
}