package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.Recommendation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDAO {

    public List<Recommendation> getByProductId(int productId) {
        List<Recommendation> list = new ArrayList<>();
        String sql = "SELECT r.*, p.product_name, p.price_per_kg, p.image_url, p.quantity_in_stock, p.category_id " +
                "FROM recommendations r JOIN products p ON r.recommended_product_id = p.product_id " +
                "WHERE r.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error loading recommendations: " + e.getMessage());
        }
        return list;
    }

    public boolean addRecommendation(int productId, int recommendedProductId, String comment) {
        String sql = "INSERT INTO recommendations (product_id, recommended_product_id, comment) " +
                "VALUES (?,?,?) ON CONFLICT (product_id, recommended_product_id) DO UPDATE SET comment = EXCLUDED.comment";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, recommendedProductId);
            ps.setString(3, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding recommendation: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRecommendation(int recommendationId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM recommendations WHERE recommendation_id=?")) {
            ps.setInt(1, recommendationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting recommendation: " + e.getMessage());
            return false;
        }
    }

    private Recommendation map(ResultSet rs) throws SQLException {
        Recommendation r = new Recommendation();
        r.setRecommendationId(rs.getInt("recommendation_id"));
        r.setProductId(rs.getInt("product_id"));
        r.setRecommendedProductId(rs.getInt("recommended_product_id"));
        r.setComment(rs.getString("comment"));
        r.setRecommendedProductName(rs.getString("product_name"));
        r.setRecommendedProductPrice(rs.getDouble("price_per_kg"));
        r.setRecommendedProductImageUrl(rs.getString("image_url"));
        r.setRecommendedProductStock(rs.getInt("quantity_in_stock"));
        r.setRecommendedProductCategoryId(rs.getInt("category_id"));
        return r;
    }
}