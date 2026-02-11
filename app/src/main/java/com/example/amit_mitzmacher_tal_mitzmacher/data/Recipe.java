package com.example.amit_mitzmacher_tal_mitzmacher.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // --- השדה החדש להפרדת משתמשים ---
    private String userId; // ה-UID של המשתמש מ-Firebase

    @SerializedName("idMeal")
    private String apiId;

    @SerializedName(value = "title", alternate = {"strMeal"})
    private String title;

    @SerializedName(value = "category", alternate = {"strCategory"})
    private String category;

    @SerializedName(value = "instructions", alternate = {"strInstructions"})
    private String instructions;

    @SerializedName(value = "image", alternate = {"strMealThumb"})
    private String image;

    private String imagePath;
    private String ingredients;
    private boolean isFavorite;

    // API fields remain with @Ignore because they are not saved to the DB directly
    @Ignore @SerializedName("strIngredient1") private String strIngredient1;
    @Ignore @SerializedName("strIngredient2") private String strIngredient2;
    @Ignore @SerializedName("strIngredient3") private String strIngredient3;
    @Ignore @SerializedName("strIngredient4") private String strIngredient4;
    @Ignore @SerializedName("strIngredient5") private String strIngredient5;
    @Ignore @SerializedName("strIngredient6") private String strIngredient6;
    @Ignore @SerializedName("strIngredient7") private String strIngredient7;
    @Ignore @SerializedName("strIngredient8") private String strIngredient8;
    @Ignore @SerializedName("strIngredient9") private String strIngredient9;
    @Ignore @SerializedName("strIngredient10") private String strIngredient10;

    @Ignore @SerializedName("strMeasure1") private String strMeasure1;
    @Ignore @SerializedName("strMeasure2") private String strMeasure2;
    @Ignore @SerializedName("strMeasure3") private String strMeasure3;
    @Ignore @SerializedName("strMeasure4") private String strMeasure4;
    @Ignore @SerializedName("strMeasure5") private String strMeasure5;
    @Ignore @SerializedName("strMeasure6") private String strMeasure6;
    @Ignore @SerializedName("strMeasure7") private String strMeasure7;
    @Ignore @SerializedName("strMeasure8") private String strMeasure8;
    @Ignore @SerializedName("strMeasure9") private String strMeasure9;
    @Ignore @SerializedName("strMeasure10") private String strMeasure10;

    @Ignore
    private List<ExtendedIngredient> extendedIngredients;

    public Recipe() {}

    @Ignore
    public Recipe(String title, String category, String ingredients, String instructions, String imagePath, String userId) {
        this.title = title;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.userId = userId;
        this.isFavorite = false;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getApiId() { return apiId; }
    public void setApiId(String apiId) { this.apiId = apiId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public List<ExtendedIngredient> getExtendedIngredients() {
        if (extendedIngredients == null) {
            extendedIngredients = new ArrayList<>();

            // Array of components
            String[] apiIngredients = {
                    strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
                    strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10
            };

            // Array of quantities
            String[] apiMeasures = {
                    strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
                    strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10
            };

            for (int i = 0; i < apiIngredients.length; i++) {
                String ing = apiIngredients[i];
                String measure = apiMeasures[i];

                if (ing != null && !ing.trim().isEmpty()) {
                    // Save both together in one object
                    extendedIngredients.add(new ExtendedIngredient(ing, measure));
                }
            }
        }
        return extendedIngredients;
    }

    public void setExtendedIngredients(List<ExtendedIngredient> extendedIngredients) {
        this.extendedIngredients = extendedIngredients;
    }

    public static class ExtendedIngredient {
        private String name;
        private String measure;

        public ExtendedIngredient(String name, String measure) {
            this.name = name;
            this.measure = measure;
        }

        public String getFullText() {
            if (measure == null || measure.trim().isEmpty()) return name;
            return measure.trim() + " " + name;
        }

        public String getOriginal() { return name; }
        public String getMeasure() { return measure; }
    }
}