package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.amit_mitzmacher_tal_mitzmacher.R;
import com.example.amit_mitzmacher_tal_mitzmacher.data.Recipe;
import com.example.amit_mitzmacher_tal_mitzmacher.data.utils.TranslationHelper; // ייבוא הסוכן
import com.example.amit_mitzmacher_tal_mitzmacher.databinding.RecipeItemBinding;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe);
        void onFavoriteClick(Recipe recipe);
    }

    public RecipeAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecipeItemBinding binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        // שמירת ה-ID הנוכחי על ה-View כדי למנוע בלבול בתרגום בזמן גלילה
        holder.itemView.setTag(recipe.getTitle());

        // טקסט זמני עד לסיום התרגום
        holder.binding.tvRecipeTitle.setText("מתרגם...");
        holder.binding.tvRecipeCategory.setText("...");

        // תרגום כותרת המתכון
        TranslationHelper.translate(recipe.getTitle(), translatedTitle -> {
            // בדיקה שהתא עדיין מציג את אותו מתכון (מניעת באגים בגלילה)
            if (holder.itemView.getTag().equals(recipe.getTitle())) {
                holder.binding.tvRecipeTitle.setText(translatedTitle);
            }
        });

        // תרגום הקטגוריה
        TranslationHelper.translate(recipe.getCategory(), translatedCategory -> {
            if (holder.itemView.getTag().equals(recipe.getTitle())) {
                holder.binding.tvRecipeCategory.setText(translatedCategory);
            }
        });

        // הצגת תמונה (API או Base64 מקומי)
        if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImage())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .centerCrop()
                    .into(holder.binding.ivRecipeImage);
        } else if (recipe.getImagePath() != null && !recipe.getImagePath().isEmpty()) {
            Bitmap bitmap = stringToBitmap(recipe.getImagePath());
            if (bitmap != null) {
                holder.binding.ivRecipeImage.setImageBitmap(bitmap);
            } else {
                holder.binding.ivRecipeImage.setImageResource(R.drawable.placeholder_food);
            }
        } else {
            holder.binding.ivRecipeImage.setImageResource(R.drawable.placeholder_food);
        }

        // ניהול אייקון המועדפים
        updateFavoriteIcon(holder.binding, recipe.isFavorite());

        holder.binding.btnFavorite.setOnClickListener(v -> {
            listener.onFavoriteClick(recipe);
        });

        // לחיצה על כל הפריט למעבר לפרטים
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(recipe);
        });
    }

    private void updateFavoriteIcon(RecipeItemBinding binding, boolean isFav) {
        binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateList(List<Recipe> newList) {
        this.recipes = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    private Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final RecipeItemBinding binding;
        public RecipeViewHolder(RecipeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}