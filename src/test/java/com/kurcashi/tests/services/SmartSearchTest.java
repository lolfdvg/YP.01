package com.kurcashi.tests.services;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class SmartSearchTest {

    // Мок-класс для имитации товара
    static class TestProduct {
        String name;
        String category;
        String description;
        String ingredients;

        TestProduct(String name, String category, String description, String ingredients) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.ingredients = ingredients;
        }

        boolean matchesSearch(String query) {
            String lowerQuery = query.toLowerCase();
            return name.toLowerCase().contains(lowerQuery) ||
                    (category != null && category.toLowerCase().contains(lowerQuery)) ||
                    (description != null && description.toLowerCase().contains(lowerQuery)) ||
                    (ingredients != null && ingredients.toLowerCase().contains(lowerQuery));
        }
    }

    @Test
    void testSearchByName() {
        TestProduct product = new TestProduct("Лосось слабосолёный", "Рыба", "Свежий лосось", "лосось, соль");
        assertTrue(product.matchesSearch("Лосось"));
        assertTrue(product.matchesSearch("лосось"));
    }

    @Test
    void testSearchByCategory() {
        TestProduct product = new TestProduct("Форель", "Рыба", "Свежая форель", "форель");
        assertTrue(product.matchesSearch("Рыба"));
        assertTrue(product.matchesSearch("рыба"));
    }

    @Test
    void testSearchByDescription() {
        TestProduct product = new TestProduct("Осётр", "Рыба", "Свежайший осётр с Дальнего Востока", "осётр");
        assertTrue(product.matchesSearch("Свежайший"));
        assertTrue(product.matchesSearch("Дальнего Востока"));
    }

    @Test
    void testSearchByIngredients() {
        TestProduct product = new TestProduct("Сёмга", "Рыба", "Свежая сёмга", "сёмга, соль, перец, лимон");
        assertTrue(product.matchesSearch("перец"));
        assertTrue(product.matchesSearch("лимон"));
    }

    @Test
    void testSearchNoMatch() {
        TestProduct product = new TestProduct("Тунец", "Рыба", "Свежий тунец", "тунец");
        assertFalse(product.matchesSearch("лосось"));
        assertFalse(product.matchesSearch("креветка"));
    }

    @Test
    void testCaseInsensitiveSearch() {
        TestProduct product = new TestProduct("ЛОСОСЬ", "РЫБА", "СВЕЖИЙ ЛОСОСЬ", "ЛОСОСЬ");
        assertTrue(product.matchesSearch("лосось"));
        assertTrue(product.matchesSearch("ЛОСОСЬ"));
        assertTrue(product.matchesSearch("Лосось"));
    }
}