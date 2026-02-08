package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.amit_mitzmacher_tal_mitzmacher.R;
import com.example.amit_mitzmacher_tal_mitzmacher.viewmodel.RecipeViewModel;
import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.example.amit_mitzmacher_tal_mitzmacher.databinding.FragmentAddRecipeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {

    private FragmentAddRecipeBinding binding;
    private RecipeViewModel recipeViewModel;
    private Bitmap capturedImage = null;
    private int recipeId = -1;
    private Recipe existingRecipe;
    private String currentPhotoTarget = "DISH";

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data");
                        updateUIWithImage();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                        capturedImage = BitmapFactory.decodeStream(inputStream);
                        updateUIWithImage();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        // חשוב: שימוש ב-requireActivity() כדי שה-ViewModel יהיה משותף לכל האפליקציה
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSpinner();
        setupToggleLogic();

        if (getArguments() != null) {
            recipeId = getArguments().getInt("recipeId", -1);
            if (recipeId != -1) loadExistingRecipe();
        }

        binding.btnCaptureImageMeal.setOnClickListener(v -> { currentPhotoTarget = "DISH"; showImageSourceDialog(); });
        binding.btnCaptureImage.setOnClickListener(v -> { currentPhotoTarget = "RECIPE"; showImageSourceDialog(); });
        binding.btnRemoveDishPhoto.setOnClickListener(v -> removeImage());
        binding.btnRemoveRecipePhoto.setOnClickListener(v -> removeImage());
        binding.btnSaveRecipe.setOnClickListener(v -> saveOrUpdateRecipe());
    }

    private void loadExistingRecipe() {
        recipeViewModel.getRecipeById(recipeId).observe(getViewLifecycleOwner(), recipe -> {
            if (recipe != null) {
                existingRecipe = recipe;
                updateUIWithExistingData();
            }
        });
    }

    private void updateUIWithExistingData() {
        binding.etTitle.setText(existingRecipe.getTitle());
        binding.etIngredients.setText(existingRecipe.getIngredients());
        binding.etInstructions.setText(existingRecipe.getInstructions());

        ArrayAdapter adapter = (ArrayAdapter) binding.spinnerCategory.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(existingRecipe.getCategory());
            binding.spinnerCategory.setSelection(position);
        }

        if (existingRecipe.getImagePath() != null && !existingRecipe.getImagePath().isEmpty()) {
            capturedImage = stringToBitmap(existingRecipe.getImagePath());
            updateUIWithImage();
        }
    }

    private void saveOrUpdateRecipe() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = binding.spinnerCategory.getSelectedItemPosition();
        if (selectedPosition == 0) {
            Toast.makeText(requireContext(), R.string.error_category_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String category = binding.spinnerCategory.getSelectedItem().toString();
        String ingredients = binding.etIngredients.getText().toString().trim();
        String instructions = binding.etInstructions.getText().toString().trim();
        String imageStr = (capturedImage != null) ? bitmapToString(capturedImage) : "";

        // קבלת ה-UID של המשתמש המחובר כרגע
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (recipeId == -1) {
            // יצירת מתכון חדש עם ה-userId
            Recipe newRecipe = new Recipe(title, category, ingredients, instructions, imageStr, currentUserId);
            recipeViewModel.insert(newRecipe);
        } else {
            // עדכון מתכון קיים
            existingRecipe.setTitle(title);
            existingRecipe.setCategory(category);
            existingRecipe.setIngredients(ingredients);
            existingRecipe.setInstructions(instructions);
            existingRecipe.setImagePath(imageStr);
            existingRecipe.setUserId(currentUserId); // וידוא שה-ID נשמר בעדכון
            recipeViewModel.update(existingRecipe);
        }

        Toast.makeText(requireContext(), R.string.recipe_saved_success, Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.recipe_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupToggleLogic() {
        ColorStateList selector = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}},
                new int[]{Color.parseColor("#8DC63F"), Color.TRANSPARENT}
        );
        binding.btnModeText.setBackgroundTintList(selector);
        binding.btnModeImage.setBackgroundTintList(selector);
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnModeText) {
                    binding.containerText.setVisibility(View.VISIBLE);
                    binding.containerImage.setVisibility(View.GONE);
                } else {
                    binding.containerText.setVisibility(View.GONE);
                    binding.containerImage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateUIWithImage() {
        if (capturedImage != null) {
            if ("DISH".equals(currentPhotoTarget)) {
                binding.ivDishPhoto.setImageBitmap(capturedImage);
                binding.btnRemoveDishPhoto.setVisibility(View.VISIBLE);
            } else {
                binding.ivRecipeImage.setImageBitmap(capturedImage);
                binding.btnRemoveRecipePhoto.setVisibility(View.VISIBLE);
            }
        }
    }

    private void removeImage() {
        capturedImage = null;
        binding.ivDishPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.ivRecipeImage.setImageResource(android.R.drawable.ic_menu_camera);
        binding.btnRemoveDishPhoto.setVisibility(View.GONE);
        binding.btnRemoveRecipePhoto.setVisibility(View.GONE);
    }

    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) { return null; }
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else { galleryLauncher.launch("image/*"); }
                }).show();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}