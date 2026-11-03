package com.kurcashi.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private int recipeId;
    private int productId;
    private String title;
    private String description;
    private String steps;
    private int cookingTime;
    private String difficulty; // EASY, MEDIUM, HARD
    private String imageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public String getDifficultyText() {
        return switch (difficulty == null ? "" : difficulty) {
            case "EASY" -> "Легко";
            case "MEDIUM" -> "Средне";
            case "HARD" -> "Сложно";
            default -> difficulty;
        };
    }
}