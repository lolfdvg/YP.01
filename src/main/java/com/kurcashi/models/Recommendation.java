package com.kurcashi.models;

public class Recommendation {
    private int recommendationId;
    private int productId;
    private int recommendedProductId;
    private String comment;
    private String recommendedProductName;
    private double recommendedProductPrice;
    private String recommendedProductImageUrl;
    private int recommendedProductStock;
    private int recommendedProductCategoryId;

    public int getRecommendationId() { return recommendationId; }
    public void setRecommendationId(int recommendationId) { this.recommendationId = recommendationId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getRecommendedProductId() { return recommendedProductId; }
    public void setRecommendedProductId(int recommendedProductId) { this.recommendedProductId = recommendedProductId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getRecommendedProductName() { return recommendedProductName; }
    public void setRecommendedProductName(String v) { this.recommendedProductName = v; }
    public double getRecommendedProductPrice() { return recommendedProductPrice; }
    public void setRecommendedProductPrice(double v) { this.recommendedProductPrice = v; }
    public String getRecommendedProductImageUrl() { return recommendedProductImageUrl; }
    public void setRecommendedProductImageUrl(String v) { this.recommendedProductImageUrl = v; }
    public int getRecommendedProductStock() { return recommendedProductStock; }
    public void setRecommendedProductStock(int v) { this.recommendedProductStock = v; }
    public int getRecommendedProductCategoryId() { return recommendedProductCategoryId; }
    public void setRecommendedProductCategoryId(int v) { this.recommendedProductCategoryId = v; }
}