package com.example.searcher;

public class SearchResult {
    private final String id;
    private final String title;

    public SearchResult(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        return title + " (ID: " + id + ")";
    }
}
