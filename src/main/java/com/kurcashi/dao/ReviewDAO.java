package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewDAO {
    private static final Logger LOGGER = Logger.getLogger(ReviewDAO.class.getName());

    // Добавление отзыва с изображением
    public boolean addReview(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, user_name, rating, comment, image_path, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getProductId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setString(3, review.getUserName());
            pstmt.setInt(4, review.getRating());
            pstmt.setString(5, review.getComment());
            pstmt.setString(6, review.getImagePath());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) updateProductRating(conn, review.getProductId());
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding review", e);
            return false;
        }
    }

    // Получение всех отзывов по товару (с изображениями)
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, product_id, user_id, user_name, rating, comment, image_path, created_at " +
                "FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setProductId(rs.getInt("product_id"));
                review.setUserId(rs.getInt("user_id"));
                review.setUserName(rs.getString("user_name"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setImagePath(rs.getString("image_path"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reviews", e);
        }
        return reviews;
    }

    // Получение среднего рейтинга товара
    public double getAverageRating(int productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting average rating", e);
        }
        return 0.0;
    }

    // Проверка, оставлял ли пользователь отзыв на этот товар
    public boolean hasUserReviewed(int productId, int userId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking user review", e);
        }
        return false;
    }

    // Вспомогательный метод – обновление рейтинга товара в таблице products
    private void updateProductRating(Connection conn, int productId) {
        String sql = "UPDATE products SET rating = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE product_id = ?) WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to update product rating", e);
        }
    }
}