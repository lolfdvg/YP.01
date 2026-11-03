package com.kurcashi.dao;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.SalesAnalytics;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    /** Топ популярных товаров за период */
    public List<SalesAnalytics> getTopProducts(LocalDate from, LocalDate to, int limit) {
        List<SalesAnalytics> list = new ArrayList<>();
        String sql = "SELECT product_id, SUM(orders_count) AS orders_count, " +
                "SUM(total_quantity) AS total_quantity, SUM(total_revenue) AS total_revenue, " +
                "SUM(bonus_write_offs) AS bonus_write_offs " +
                "FROM sales_analytics WHERE record_date BETWEEN ? AND ? " +
                "GROUP BY product_id ORDER BY total_quantity DESC NULLS LAST LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SalesAnalytics a = new SalesAnalytics();
                a.setProductId(rs.getInt("product_id"));
                a.setOrdersCount(rs.getInt("orders_count"));
                a.setTotalQuantity(rs.getDouble("total_quantity"));
                a.setTotalRevenue(rs.getDouble("total_revenue"));
                a.setBonusWriteOffs(rs.getInt("bonus_write_offs"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Error analytics top: " + e.getMessage());
        }
        // Подтягиваем названия
        for (SalesAnalytics a : list) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT product_name FROM products WHERE product_id=?")) {
                ps.setInt(1, a.getProductId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) a.setProductName(rs.getString(1));
            } catch (SQLException ignored) {}
        }
        return list;
    }

    /** Распределение по способам оплаты */
    public List<Object[]> getPaymentsDistribution(LocalDate from, LocalDate to) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT pm.method_name, COUNT(op.order_payment_id) AS cnt, " +
                "COALESCE(SUM(op.amount),0) AS total " +
                "FROM order_payments op JOIN payment_methods pm ON op.method_id = pm.method_id " +
                "WHERE op.paid_at BETWEEN ? AND ? " +
                "GROUP BY pm.method_name ORDER BY cnt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
            }
        } catch (SQLException e) {
            System.err.println("Error analytics payments: " + e.getMessage());
        }
        return list;
    }

    /** Суммарные списания бонусов за период */
    public int getTotalBonusWriteOffs(LocalDate from, LocalDate to) {
        String sql = "SELECT COALESCE(SUM(bonus_write_offs),0) FROM sales_analytics " +
                "WHERE record_date BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error analytics bonus: " + e.getMessage());
        }
        return 0;
    }

    /** Запись аналитики при оформлении заказа */
    public boolean recordSale(Connection conn, LocalDate date, int productId, int qty, double amount,
                              int bonusWriteOff, int paymentMethodId) {
        String sql = "INSERT INTO sales_analytics " +
                "(record_date, product_id, orders_count, total_quantity, total_revenue, bonus_write_offs, payment_method_id) " +
                "VALUES (?,?,1,?,?,?,?) " +
                "ON CONFLICT (record_date, product_id, payment_method_id) DO UPDATE SET " +
                "orders_count = sales_analytics.orders_count + 1, " +
                "total_quantity = sales_analytics.total_quantity + EXCLUDED.total_quantity, " +
                "total_revenue = sales_analytics.total_revenue + EXCLUDED.total_revenue, " +
                "bonus_write_offs = sales_analytics.bonus_write_offs + EXCLUDED.bonus_write_offs";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, productId);
            ps.setInt(3, qty);
            ps.setDouble(4, amount);
            ps.setInt(5, bonusWriteOff);
            ps.setInt(6, paymentMethodId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording sale: " + e.getMessage());
            return false;
        }
    }
}