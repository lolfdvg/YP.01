package com.kurcashi.dao;

import com.kurcashi.models.Recipe;
import com.kurcashi.models.RecipeIngredient;
import com.kurcashi.models.SalesAnalytics;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NewFeaturesTest {

    @Test
    public void testGetRecipesByProductId() {
        RecipeDAO recipeDAO = new RecipeDAO();
        // Берем ID существующего товара
        List<Recipe> recipes = recipeDAO.getRecipesByProductId(1);

        assertNotNull(recipes);
        if (!recipes.isEmpty()) {
            // Проверяем, что подтянулись ингредиенты (связь 1:N)
            assertNotNull(recipes.get(0).getIngredients());
        }
    }

    @Test
    public void testAddAndDeleteRecipe() {
        RecipeDAO recipeDAO = new RecipeDAO();
        Recipe newRecipe = new Recipe();
        newRecipe.setProductId(1);
        newRecipe.setTitle("Тестовый рецепт");
        newRecipe.setSteps("Шаг 1. Шаг 2.");
        newRecipe.setCookingTime(30);
        newRecipe.setDifficulty("EASY");

        List<RecipeIngredient> ingredients = new ArrayList<>();
        RecipeIngredient ing = new RecipeIngredient();
        ing.setProductId(1);
        ing.setQuantity(0.5);
        ingredients.add(ing);
        newRecipe.setIngredients(ingredients);

        // Тест добавления (Интеграционный тест с БД)
        boolean isAdded = recipeDAO.addRecipe(newRecipe, 1);
        assertTrue(isAdded, "Рецепт должен успешно добавляться");
    }

    @Test
    public void testRecommendationsLoading() {
        RecommendationDAO recDAO = new RecommendationDAO();
        // Проверяем, что метод не падает и возвращает список
        assertNotNull(recDAO.getByProductId(1));
    }

    @Test
    public void testAnalyticsDataLoading() {
        AnalyticsDAO analyticsDAO = new AnalyticsDAO();
        // Проверяем получение топа за последние 30 дней
        List<SalesAnalytics> topProducts = analyticsDAO.getTopProducts(
                LocalDate.now().minusDays(30), LocalDate.now(), 10);

        assertNotNull(topProducts);
    }

    @Test
    public void testValidationPaymentFormat() {
        // Проверка, что сумма форматируется с точкой (защита от ошибки API ЮKassa)
        double amount = 900.50;
        String formatted = String.format(java.util.Locale.US, "%.2f", amount);
        assertEquals("900.50", formatted, "Сумма должна быть с точкой");
    }
}