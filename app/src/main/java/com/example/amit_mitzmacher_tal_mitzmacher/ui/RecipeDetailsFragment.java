package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.amit_mitzmacher_tal_mitzmacher.R;
import com.example.amit_mitzmacher_tal_mitzmacher.data.utils.TranslationHelper;
import com.example.amit_mitzmacher_tal_mitzmacher.viewmodel.RecipeViewModel;
import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.example.amit_mitzmacher_tal_mitzmacher.databinding.FragmentRecipeDetailsBinding;
import java.util.List;

public class RecipeDetailsFragment extends Fragment {

    private FragmentRecipeDetailsBinding binding;
    private RecipeViewModel recipeViewModel;
    private Recipe currentRecipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.contentLayout.setVisibility(View.GONE);

        if (getArguments() != null) {
            int localId = getArguments().getInt("recipeId", -1);
            String apiId = getArguments().getString("apiId", null);
            if (localId != -1) observeLocalRecipe(localId);
            else if (apiId != null) observeApiRecipe(apiId);
        }
        setupButtons();
    }

    private void observeLocalRecipe(int id) {
        binding.progressBar.setVisibility(View.VISIBLE);
        recipeViewModel.getRecipeById(id).observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                currentRecipe = recipe;
                displayRecipe(currentRecipe, false, null);
                binding.progressBar.setVisibility(View.GONE);
                binding.contentLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void observeApiRecipe(String apiId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        recipeViewModel.getRecipeDetailsFromApi(apiId).observe(getViewLifecycleOwner(), apiRecipe -> {
            if (apiRecipe != null) {
                recipeViewModel.getRecipeByApiId(apiId).observe(getViewLifecycleOwner(), localRecipe -> {
                    currentRecipe = (localRecipe != null) ? localRecipe : apiRecipe;
                    displayRecipe(currentRecipe, (localRecipe == null), apiRecipe);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                });
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRecipe(Recipe recipe, boolean isFromApi, Recipe apiFallback) {
        // 1. כותרת - תרגום אוטומטי
        TranslationHelper.translate(recipe.getTitle(), translated -> {
            if (binding != null) binding.tvDetailsTitle.setText(translated);
        });

        // 2. קטגוריה - הצגת הקטגוריה המקורית ותרגומה (בלי צורך ב-strings.xml)
        String cat = (recipe.getCategory() != null) ? recipe.getCategory() : (isFromApi ? "Web" : "General");
        TranslationHelper.translate(cat, translated -> {
            if (binding != null) binding.tvDetailsCategory.setText(translated);
        });

        // 3. מצרכים
        setupIngredientsDisplay(recipe, apiFallback);

        // 4. הוראות הכנה
        String instructions = recipe.getInstructions();
        if ((instructions == null || instructions.trim().isEmpty()) && apiFallback != null) {
            instructions = apiFallback.getInstructions();
        }

        if (instructions != null && !instructions.trim().isEmpty()) {
            binding.layoutInstructionsContainer.setVisibility(View.VISIBLE);
            TranslationHelper.translate(instructions, translated -> {
                if (binding != null) binding.tvDetailsInstructions.setText(translated);
            });
        } else {
            binding.layoutInstructionsContainer.setVisibility(View.GONE);
        }

        handleImageDisplay(recipe, isFromApi);

        // שליטה בכפתורים
        binding.wrapperEdit.setVisibility(isFromApi ? View.GONE : View.VISIBLE);
        binding.wrapperDelete.setVisibility(recipe.getId() > 0 ? View.VISIBLE : View.GONE);
    }

    private void setupIngredientsDisplay(Recipe recipe, Recipe apiFallback) {
        List<Recipe.ExtendedIngredient> ingredientsList = recipe.getExtendedIngredients();
        if ((ingredientsList == null || ingredientsList.isEmpty()) && apiFallback != null) {
            ingredientsList = apiFallback.getExtendedIngredients();
        }

        binding.tvDetailsIngredients.setText(""); // איפוס הטקסט

        if (ingredientsList != null && !ingredientsList.isEmpty()) {
            binding.layoutIngredientsContainer.setVisibility(View.VISIBLE);
            for (Recipe.ExtendedIngredient ing : ingredientsList) {

                // בניית הטקסט: כמות + שם (למשל: "2 cups Flour")
                // השתמש ב-getName() וב-getMeasure() כפי שהגדרנו ב-Recipe
                String measure = (ing.getMeasure() != null) ? ing.getMeasure() : "";
                String fullIngredientText = measure + " " + ing.getOriginal();

                TranslationHelper.translate(fullIngredientText.trim(), translated -> {
                    if (binding != null) {
                        binding.tvDetailsIngredients.append("• " + translated + "\n");
                    }
                });
            }
        } else if (recipe.getIngredients() != null && !recipe.getIngredients().trim().isEmpty()) {
            // טיפול במקרה של טקסט חופשי (מתכונים שהמשתמש הזין ידנית)
            TranslationHelper.translate(recipe.getIngredients(), translated -> {
                if (binding != null) {
                    binding.layoutIngredientsContainer.setVisibility(View.VISIBLE);
                    binding.tvDetailsIngredients.setText(translated);
                }
            });
        } else {
            binding.layoutIngredientsContainer.setVisibility(View.GONE);
        }
    }

    private void handleImageDisplay(Recipe recipe, boolean isFromApi) {
        binding.cardImage.setVisibility(View.VISIBLE);
        String apiImg = recipe.getImage();
        String localImg = recipe.getImagePath();

        if (apiImg != null && !apiImg.isEmpty()) {
            Glide.with(this).load(apiImg).into(binding.imgDetails);
            binding.imgDetails.setOnClickListener(v -> showFullScreenImage(apiImg, null));
        } else if (localImg != null && !localImg.isEmpty()) {
            Bitmap b = stringToBitmap(localImg);
            if (b != null) {
                binding.imgDetails.setImageBitmap(b);
                binding.imgDetails.setOnClickListener(v -> showFullScreenImage(null, b));
            }
        } else {
            binding.cardImage.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        binding.btnEditRecipe.setOnClickListener(v -> {
            if (currentRecipe != null) {
                Bundle args = new Bundle();
                args.putInt("recipeId", currentRecipe.getId());
                Navigation.findNavController(v).navigate(R.id.action_recipeDetailsFragment_to_addRecipeFragment, args);
            }
        });
        binding.btnDeleteRecipe.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("מחיקת מתכון")
                    .setMessage("האם אתה בטוח שברצונך למחוק את המתכון?")
                    .setPositiveButton("כן", (dialog, which) -> deleteRecipe())
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

    private void deleteRecipe() {
        if (currentRecipe != null) {
            recipeViewModel.delete(currentRecipe);
            Toast.makeText(getContext(), "המתכון נמחק", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    private void showFullScreenImage(String url, Bitmap bitmap) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).create();
        ImageView iv = new ImageView(requireContext());
        iv.setBackgroundColor(Color.BLACK);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (url != null) Glide.with(this).load(url).into(iv);
        else if (bitmap != null) iv.setImageBitmap(bitmap);

        dialog.show();
        dialog.setContentView(iv);
        iv.setOnClickListener(v -> dialog.dismiss());
    }

    private Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) { return null; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}