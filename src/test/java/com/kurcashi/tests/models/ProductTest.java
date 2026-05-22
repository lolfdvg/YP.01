package com.kurcashi.tests.models;

import com.kurcashi.models.Product;
import com.kurcashi.models.Tag;
import com.kurcashi.models.Review;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductConstructor() {
        Product product = new Product(1, "Лосось", 1200.0, 4.5, "Свежий лосось", 50, "losos.jpg");
        assertEquals(1, product.getId());
        assertEquals("Лосось", product.getName());
        assertEquals(1200.0, product.getPrice());
        assertEquals(4.5, product.getRating());
        assertEquals("Свежий лосось", product.getDescription());
        assertEquals(50, product.getStockQuantity());
        assertEquals("losos.jpg", product.getImageUrl());
    }

    @Test
    void testGetFormattedPrice() {
        Product product = new Product(1, "Лосось", 1250.50, 4.5, "", 50, "");
        assertEquals("1250,50 руб/кг", product.getFormattedPrice());
    }

    @Test
    void testSetAndGetCategory() {
        Product product = new Product();
        product.setCategoryId(5);
        product.setCategoryName("Рыба");
        assertEquals(5, product.getCategoryId());
        assertEquals("Рыба", product.getCategoryName());
    }
}

class TagTest {

    @Test
    void testTagConstructor() {
        Tag tag = new Tag("свежая", 1);
        assertEquals("свежая", tag.getTagName());
        assertEquals(1, tag.getDisplayOrder());
        assertTrue(tag.isActive());
    }

    @Test
    void testTagSetters() {
        Tag tag = new Tag();
        tag.setTagId(10);
        tag.setTagName("копчёная");
        tag.setDisplayOrder(2);
        tag.setActive(false);
        assertEquals(10, tag.getTagId());
        assertEquals("копчёная", tag.getTagName());
        assertEquals(2, tag.getDisplayOrder());
        assertFalse(tag.isActive());
    }
}