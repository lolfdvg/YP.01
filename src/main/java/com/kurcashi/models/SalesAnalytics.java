package com.kurcashi.models;

import java.time.LocalDate;

public class SalesAnalytics {
    private int recordId;
    private LocalDate recordDate;
    private int productId;
    private String productName;
    private int ordersCount;
    private double totalQuantity;
    private double totalRevenue;
    private int bonusWriteOffs;
    private int paymentMethodId;
    private String paymentMethodName;

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getOrdersCount() { return ordersCount; }
    public void setOrdersCount(int ordersCount) { this.ordersCount = ordersCount; }
    public double getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(double totalQuantity) { this.totalQuantity = totalQuantity; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getBonusWriteOffs() { return bonusWriteOffs; }
    public void setBonusWriteOffs(int bonusWriteOffs) { this.bonusWriteOffs = bonusWriteOffs; }
    public int getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public String getPaymentMethodName() { return paymentMethodName; }
    public void setPaymentMethodName(String paymentMethodName) { this.paymentMethodName = paymentMethodName; }
}