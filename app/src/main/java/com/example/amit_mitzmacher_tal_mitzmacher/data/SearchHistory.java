package com.example.amit_mitzmacher_tal_mitzmacher.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String queryText;
    public long timestamp;

    public SearchHistory(String queryText) {
        this.queryText = queryText;
        this.timestamp = System.currentTimeMillis();
    }
}