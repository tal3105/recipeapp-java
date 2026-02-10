package com.example.amit_mitzmacher_tal_mitzmacher.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.amit_mitzmacher_tal_mitzmacher.data.network.RecipeApiResponse;
import com.example.amit_mitzmacher_tal_mitzmacher.data.network.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {
    private final RecipeDao recipeDao;
    private final ExecutorService executorService;

    public RecipeRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        recipeDao = db.recipeDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // --- תיקון קריטי: הגנה מפני Null וערך ריק ---
    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return "GUEST"; // מחזירים ערך ברירת מחדל במקום "" כדי למנוע בעיות ב-Room
    }

    public void insert(Recipe recipe) {
        String uid = getCurrentUserId();
        recipe.setUserId(uid);
        executorService.execute(() -> recipeDao.insert(recipe));
    }

    public void update(Recipe recipe) {
        recipe.setUserId(getCurrentUserId());
        executorService.execute(() -> recipeDao.update(recipe));
    }

    public void delete(Recipe recipe) {
        executorService.execute(() -> recipeDao.delete(recipe));
    }

    public void upsert(Recipe recipe) {
        executorService.execute(() -> {
            recipe.setUserId(getCurrentUserId());
            long id = recipeDao.insert(recipe);
            if (id == -1) {
                recipeDao.update(recipe);
            }
        });
    }

    // --- שאילתות עם הגנה ---
    public LiveData<List<Recipe>> getAllRecipes() {
        String uid = getCurrentUserId();
        // אם המשתמש עוד לא "מוכן", נחזיר רשימה ריקה זמנית כדי למנוע קריסה
        if (uid.equals("GUEST")) {
            return new MutableLiveData<>(new ArrayList<>());
        }
        return recipeDao.getAllRecipes(uid);
    }

    public LiveData<Recipe> getRecipeByApiId(String apiId) {
        return recipeDao.getRecipeByApiId(apiId, getCurrentUserId());
    }

    public LiveData<List<String>> getAllCategories() {
        return recipeDao.getAllCategories(getCurrentUserId());
    }

    public LiveData<List<Recipe>> getRecipesByCategory(String category) {
        return recipeDao.getRecipesByCategory(getCurrentUserId(), category);
    }

    public LiveData<List<Recipe>> getFavoriteRecipes() {
        return recipeDao.getFavoriteRecipes(getCurrentUserId());
    }

    public LiveData<List<Recipe>> searchRecipesLocal(String query) {
        return recipeDao.findRecipesByIngredient(getCurrentUserId(), query);
    }

    public LiveData<Recipe> getRecipeById(int id) {
        return recipeDao.getRecipeById(id);
    }

    // --- API Methods ---
    public LiveData<List<Recipe>> searchRecipesApi(String query) {
        MutableLiveData<List<Recipe>> apiResults = new MutableLiveData<>();
        RetrofitClient.getRecipeService().searchRecipes(query)
                .enqueue(new Callback<RecipeApiResponse>() {
                    @Override
                    public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            apiResults.setValue(response.body().getRecipes());
                        } else {
                            apiResults.setValue(null);
                        }
                    }
                    @Override
                    public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                        apiResults.setValue(null);
                    }
                });
        return apiResults;
    }

    public LiveData<Recipe> getRecipeDetailsFromApi(String apiId) {
        MutableLiveData<Recipe> recipeData = new MutableLiveData<>();
        RetrofitClient.getRecipeService().getRecipeDetails(apiId).enqueue(new Callback<RecipeApiResponse>() {
            @Override
            public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getRecipes() != null) {
                    recipeData.setValue(response.body().getRecipes().get(0));
                }
            }
            @Override
            public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                recipeData.setValue(null);
            }
        });
        return recipeData;
    }
}