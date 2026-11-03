package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.Recipe;
import com.kurcashi.models.RecipeIngredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {

    public List<Recipe> getRecipesByProductId(int productId) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM recipes WHERE product_id = ? ORDER BY title";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Recipe r = mapRecipe(rs);
                r.setIngredients(getIngredients(r.getRecipeId()));
                recipes.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error loading recipes: " + e.getMessage());
        }
        return recipes;
    }

    public Recipe getRecipeById(int recipeId) {
        String sql = "SELECT * FROM recipes WHERE recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Recipe r = mapRecipe(rs);
                r.setIngredients(getIngredients(recipeId));
                return r;
            }
        } catch (SQLException e) {
            System.err.println("Error loading recipe: " + e.getMessage());
        }
        return null;
    }

    public List<RecipeIngredient> getIngredients(int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        String sql = "SELECT ri.*, p.product_name, p.price_per_kg, p.quantity_in_stock " +
                "FROM recipe_ingredients ri JOIN products p ON ri.product_id = p.product_id " +
                "WHERE ri.recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setIngredientId(rs.getInt("ingredient_id"));
                ri.setRecipeId(rs.getInt("recipe_id"));
                ri.setProductId(rs.getInt("product_id"));
                ri.setProductName(rs.getString("product_name"));
                ri.setProductPrice(rs.getDouble("price_per_kg"));
                ri.setStockQuantity(rs.getInt("quantity_in_stock"));
                ri.setQuantity(rs.getDouble("quantity"));
                ri.setUnit(rs.getString("unit"));
                list.add(ri);
            }
        } catch (SQLException e) {
            System.err.println("Error loading ingredients: " + e.getMessage());
        }
        return list;
    }

    public boolean addRecipe(Recipe recipe, int adminId) {
        String sql = "INSERT INTO recipes (product_id, title, description, steps, cooking_time, difficulty, image_url) " +
                "VALUES (?,?,?,?,?,?,?) RETURNING recipe_id";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, recipe.getProductId());
                ps.setString(2, recipe.getTitle());
                ps.setString(3, recipe.getDescription());
                ps.setString(4, recipe.getSteps());
                ps.setInt(5, recipe.getCookingTime());
                ps.setString(6, recipe.getDifficulty());
                ps.setString(7, recipe.getImageUrl());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { conn.rollback(); return false; }
                int recipeId = rs.getInt(1);
                insertIngredients(conn, recipeId, recipe.getIngredients());
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("Error adding recipe: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRecipe(Recipe recipe) {
        String sql = "UPDATE recipes SET title=?, description=?, steps=?, cooking_time=?, difficulty=?, image_url=?, " +
                "updated_at=CURRENT_TIMESTAMP WHERE recipe_id=?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, recipe.getTitle());
                ps.setString(2, recipe.getDescription());
                ps.setString(3, recipe.getSteps());
                ps.setInt(4, recipe.getCookingTime());
                ps.setString(5, recipe.getDifficulty());
                ps.setString(6, recipe.getImageUrl());
                ps.setInt(7, recipe.getRecipeId());
                ps.executeUpdate();
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM recipe_ingredients WHERE recipe_id = ?")) {
                    del.setInt(1, recipe.getRecipeId());
                    del.executeUpdate();
                }
                insertIngredients(conn, recipe.getRecipeId(), recipe.getIngredients());
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("Error updating recipe: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRecipe(int recipeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM recipes WHERE recipe_id=?")) {
            ps.setInt(1, recipeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recipe: " + e.getMessage());
            return false;
        }
    }

    private void insertIngredients(Connection conn, int recipeId, List<RecipeIngredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) return;
        String sql = "INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (RecipeIngredient ri : ingredients) {
                ps.setInt(1, recipeId);
                ps.setInt(2, ri.getProductId());
                ps.setDouble(3, ri.getQuantity());
                ps.setString(4, ri.getUnit() != null ? ri.getUnit() : "кг");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Recipe mapRecipe(ResultSet rs) throws SQLException {
        Recipe r = new Recipe();
        r.setRecipeId(rs.getInt("recipe_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setSteps(rs.getString("steps"));
        r.setCookingTime(rs.getInt("cooking_time"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setImageUrl(rs.getString("image_url"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}