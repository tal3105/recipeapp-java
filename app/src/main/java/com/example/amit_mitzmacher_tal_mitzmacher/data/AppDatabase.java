package com.example.amit_mitzmacher_tal_mitzmacher.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Recipe.class, SearchHistory.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();
    private static volatile AppDatabase instance;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "recipe_db")

                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}