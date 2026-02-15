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

    private final MutableLiveData<List<Recipe>> currentRecipes = new MutableLiveData<>();

    private String lastSearchQuery = "";

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        loadAllRecipes();
    }

    public void insert(Recipe recipe) { repository.insert(recipe); }
    public void update(Recipe recipe) { repository.update(recipe); }
    public void delete(Recipe recipe) { repository.delete(recipe); }

    public LiveData<Recipe> getRecipeByApiId(String apiId) {
        return repository.getRecipeByApiId(apiId);
    }

    public LiveData<Recipe> getRecipeById(int id) {
        return repository.getRecipeById(id);
    }

    public LiveData<List<Recipe>> getCurrentRecipes() {
        return currentRecipes;
    }
    public void loadAllRecipes() {
        repository.getAllRecipes().observeForever(recipes -> {
            currentRecipes.setValue(recipes);
        });
    }

    public void searchLocal(String query) {
        repository.searchRecipesLocal(query).observeForever(recipes -> {
            currentRecipes.setValue(recipes);
        });
    }

    public void searchApi(String query) {
        repository.searchRecipesApi(query).observeForever(recipes -> {
            if (recipes != null) {
                // Update our main LiveData so the Home screen refreshes
                currentRecipes.setValue(recipes);
            }
        });
    }

    // Filter recipes based on the selected Chip
    public void filterByCategory(String category) {
        if (category.equals("All") || category.isEmpty()) {
            loadAllRecipes();
        } else {
            repository.getRecipesByCategory(category).observeForever(recipes -> {
                currentRecipes.setValue(recipes);
            });
        }
    }

    public void filterByFavorites() {
        repository.getFavoriteRecipes().observeForever(recipes -> {
            currentRecipes.setValue(recipes);
        });
    }

    // flip the favorite status and update the database
    public void toggleFavorite(Recipe recipe) {
        recipe.setFavorite(!recipe.isFavorite());
        repository.upsert(recipe);
    }

    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    public LiveData<Recipe> getRecipeDetailsFromApi(String apiId) {
        return repository.getRecipeDetailsFromApi(apiId);
    }

    public String getLastSearchQuery() { return lastSearchQuery; }
    public void setLastSearchQuery(String query) { this.lastSearchQuery = query; }
}