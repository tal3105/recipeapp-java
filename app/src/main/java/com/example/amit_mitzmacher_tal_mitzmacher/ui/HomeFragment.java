package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.amit_mitzmacher_tal_mitzmacher.R;
import com.example.amit_mitzmacher_tal_mitzmacher.data.utils.TranslationHelper;
import com.example.amit_mitzmacher_tal_mitzmacher.viewmodel.RecipeViewModel;
import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.example.amit_mitzmacher_tal_mitzmacher.databinding.FragmentHomeBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecipeAdapter adapter;
    private RecipeViewModel recipeViewModel;
    private boolean isRestoringSearch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.ExitApp))
                        .setMessage(getString(R.string.ExitAppAlert))
                        .setNegativeButton(getString(R.string.ExitAppCancel), (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(getString(R.string.ExitAppOut), (dialog, which) -> requireActivity().finish())
                        .create()
                        .show();
            }
        });

        setupRecyclerView();
        setupCategoryFilters();
        setupSearch();

        recipeViewModel.getCurrentRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null) {
                adapter.updateList(recipes);
                binding.tvNoRecipes.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        binding.fabAddRecipe.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_addRecipeFragment));

        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_logInFragment);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recipeViewModel.getLastSearchQuery().isEmpty()) {
            recipeViewModel.loadAllRecipes();
        }
    }

    private void setupSearch() {
        String lastQuery = recipeViewModel.getLastSearchQuery();

        if (lastQuery != null && !lastQuery.isEmpty()) {
            isRestoringSearch = true;
            binding.searchView.setQuery(lastQuery, false);
            binding.searchView.clearFocus();
            new Handler(Looper.getMainLooper()).postDelayed(() -> isRestoringSearch = false, 500);
        }

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !isRestoringSearch) {
                    recipeViewModel.setLastSearchQuery(query);

                    TranslationHelper.translateToEnglish(query, translatedQuery -> {
                        recipeViewModel.searchApi(translatedQuery);
                    });
                    binding.searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (isRestoringSearch) return true;

                if (newText.isEmpty()) {
                    recipeViewModel.setLastSearchQuery("");
                    recipeViewModel.loadAllRecipes();
                } else {
                    TranslationHelper.translateToEnglish(newText, translatedQuery -> {
                        if (!isRestoringSearch) {
                            recipeViewModel.searchLocal(translatedQuery);
                        }
                    });
                }
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recipe recipe) {
                Bundle args = new Bundle();
                if (recipe.getApiId() != null && !recipe.getApiId().isEmpty()) {
                    args.putString("apiId", recipe.getApiId());
                } else {
                    args.putInt("recipeId", recipe.getId());
                }
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_recipeDetailsFragment, args);
            }

            @Override
            public void onFavoriteClick(Recipe recipe) {
                recipeViewModel.toggleFavorite(recipe);
                String message = recipe.isFavorite() ?
                        getString(R.string.added_to_favorites) :
                        getString(R.string.removed_from_favorites);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                if (recipe.getApiId() != null && !recipe.getApiId().isEmpty()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> forceSelectAllChip(), 300);
                }
            }
        });
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecipes.setAdapter(adapter);
    }

    private void forceSelectAllChip() {
        binding.searchView.setQuery("", false);
        binding.searchView.clearFocus();
        recipeViewModel.setLastSearchQuery("");

        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            View child = binding.chipGroupCategories.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getTag() != null && chip.getTag().toString().equalsIgnoreCase("All")) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void setupCategoryFilters() {
        recipeViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                binding.chipGroupCategories.removeAllViews();
                addChipToGroup(getString(R.string.chipAll), "All", recipeViewModel.getLastSearchQuery().isEmpty());
                addChipToGroup(getString(R.string.chipFavorite), "Favorites ⭐", false);

                for (String cat : categories) {
                    if (cat != null && !cat.isEmpty() && !cat.equals("Choose Category")) {
                        TranslationHelper.translate(cat, translatedCat -> {
                            if (binding != null) addChipToGroup(translatedCat, cat, false);
                        });
                    }
                }
            }
        });
    }

    private void addChipToGroup(String displayName, String originalName, boolean isChecked) {
        Chip chip = new Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle);
        chip.setText(displayName);
        chip.setTag(originalName);
        chip.setCheckable(true);
        chip.setChecked(isChecked);

        int greenColor = Color.parseColor("#8DC63F");
        chip.setChipStrokeColor(ColorStateList.valueOf(greenColor));
        chip.setChipStrokeWidth(3f);
        int[][] states = new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked} };
        chip.setChipBackgroundColor(new ColorStateList(states, new int[]{greenColor, Color.WHITE}));
        chip.setTextColor(new ColorStateList(states, new int[]{Color.WHITE, greenColor}));

        chip.setOnCheckedChangeListener((v, checked) -> {
            if (checked) {
                if (originalName.equals("All")) {
                    recipeViewModel.setLastSearchQuery("");
                    recipeViewModel.loadAllRecipes();
                } else if (originalName.equals("Favorites ⭐")) {
                    recipeViewModel.filterByFavorites();
                } else {
                    recipeViewModel.filterByCategory(originalName);
                }
                if (!originalName.equals("All")) binding.searchView.setQuery("", false);
            }
        });
        binding.chipGroupCategories.addView(chip);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}