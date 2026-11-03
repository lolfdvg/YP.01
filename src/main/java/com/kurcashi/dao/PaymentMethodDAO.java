package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.PaymentMethod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDAO {

    public List<PaymentMethod> getAllActive() {
        List<PaymentMethod> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_methods WHERE is_active = true ORDER BY method_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error loading payment methods: " + e.getMessage());
        }
        return list;
    }

    public List<PaymentMethod> getAll() {
        List<PaymentMethod> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_methods ORDER BY method_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error loading payment methods: " + e.getMessage());
        }
        return list;
    }

    public PaymentMethod getByCode(String code) {
        String sql = "SELECT * FROM payment_methods WHERE method_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("Error loading payment method: " + e.getMessage());
        }
        return null;
    }

    public boolean addPaymentMethod(PaymentMethod m) {
        String sql = "INSERT INTO payment_methods (method_code, method_name, is_active, description) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMethodCode());
            ps.setString(2, m.getMethodName());
            ps.setBoolean(3, m.isActive());
            ps.setString(4, m.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding payment method: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePaymentMethod(PaymentMethod m) {
        String sql = "UPDATE payment_methods SET method_name=?, is_active=?, description=? WHERE method_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMethodName());
            ps.setBoolean(2, m.isActive());
            ps.setString(3, m.getDescription());
            ps.setInt(4, m.getMethodId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment method: " + e.getMessage());
            return false;
        }
    }

    private PaymentMethod map(ResultSet rs) throws SQLException {
        return new PaymentMethod(
                rs.getInt("method_id"),
                rs.getString("method_code"),
                rs.getString("method_name"),
                rs.getBoolean("is_active"),
                rs.getString("description")
        );
    }
}