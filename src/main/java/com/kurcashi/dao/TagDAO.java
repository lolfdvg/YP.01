package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.Tag;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    public List<Tag> getAllActiveTags() {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT tag_id, tag_name, display_order, is_active, created_at, updated_at " +
                "FROM tags WHERE is_active = true ORDER BY display_order";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tag tag = new Tag();
                tag.setTagId(rs.getInt("tag_id"));
                tag.setTagName(rs.getString("tag_name"));
                tag.setDisplayOrder(rs.getInt("display_order"));
                tag.setActive(rs.getBoolean("is_active"));
                tag.setCreatedAt(rs.getTimestamp("created_at"));
                tag.setUpdatedAt(rs.getTimestamp("updated_at"));
                tags.add(tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT tag_id, tag_name, display_order, is_active FROM tags ORDER BY display_order";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tag tag = new Tag();
                tag.setTagId(rs.getInt("tag_id"));
                tag.setTagName(rs.getString("tag_name"));
                tag.setDisplayOrder(rs.getInt("display_order"));
                tag.setActive(rs.getBoolean("is_active"));
                tags.add(tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public boolean addTag(Tag tag) {
        String sql = "INSERT INTO tags (tag_name, display_order, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag.getTagName());
            pstmt.setInt(2, tag.getDisplayOrder());
            pstmt.setBoolean(3, tag.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTag(Tag tag) {
        String sql = "UPDATE tags SET tag_name = ?, display_order = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE tag_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag.getTagName());
            pstmt.setInt(2, tag.getDisplayOrder());
            pstmt.setBoolean(3, tag.isActive());
            pstmt.setInt(4, tag.getTagId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTag(int tagId) {
        String sql = "DELETE FROM tags WHERE tag_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tagId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}