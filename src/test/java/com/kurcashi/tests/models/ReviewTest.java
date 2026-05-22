package com.kurcashi.tests.models;

import com.kurcashi.models.Review;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void testReviewWithImage() {
        Review review = new Review();
        review.setReviewId(1);
        review.setProductId(10);
        review.setUserId(5);
        review.setUserName("Тестовый пользователь");
        review.setRating(5);
        review.setComment("Отличный товар!");
        review.setImagePath("review_20260520_001.jpg");

        assertEquals(1, review.getReviewId());
        assertEquals(10, review.getProductId());
        assertEquals(5, review.getUserId());
        assertEquals("Тестовый пользователь", review.getUserName());
        assertEquals(5, review.getRating());
        assertEquals("Отличный товар!", review.getComment());
        assertEquals("review_20260520_001.jpg", review.getImagePath());
    }

    @Test
    void testReviewWithoutImage() {
        Review review = new Review(10, 5, "Пользователь", 4, "Хорошо");
        assertNull(review.getImagePath());
    }
}