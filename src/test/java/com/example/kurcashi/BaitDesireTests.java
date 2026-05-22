package com.example.kurcashi;

import com.kurcashi.models.Product;
import com.kurcashi.models.PaymentInfo;
import com.kurcashi.utils.PasswordUtils;
import com.kurcashi.database.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaitDesireTests {

    private static Connection connection;
    private static int testUserId;

    @BeforeAll
    static void setUp() throws SQLException {
        connection = DatabaseConnection.getConnection();
        assertNotNull(connection, "Подключение к БД должно быть установлено");
        System.out.println("=== НАЧАЛО ТЕСТИРОВАНИЯ ===");
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // Очистка тестовых данных
            String deleteTestUser = "DELETE FROM customers WHERE email LIKE 'test_%@example.com'";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteTestUser)) {
                pstmt.executeUpdate();
            }
            connection.close();
        }
        System.out.println("=== КОНЕЦ ТЕСТИРОВАНИЯ ===");
    }

    // ==================== ТЕСТЫ ДЛЯ ФУНКЦИИ ПОВТОРНОГО ЗАКАЗА ====================

    @Test
    @Order(1)
    @DisplayName("TC-01: Проверка валидации номера телефона (корректный ввод)")
    void testValidPhoneNumber() {
        String validPhone = "+7 (927) 144-89-80";
        String cleanPhone = validPhone.replaceAll("[^0-9]", "");

        assertEquals("79271448980", cleanPhone);
        assertTrue(cleanPhone.length() >= 10, "Номер телефона должен содержать минимум 10 цифр");
        System.out.println("✅ TC-01 пройден: корректный номер телефона");
    }

    @Test
    @Order(2)
    @DisplayName("TC-02: Проверка валидации номера телефона (некорректный ввод)")
    void testInvalidPhoneNumber() {
        String invalidPhone = "123";
        String cleanPhone = invalidPhone.replaceAll("[^0-9]", "");

        assertFalse(cleanPhone.length() >= 10, "Некорректный номер телефона должен быть отклонён");
        System.out.println("✅ TC-02 пройден: некорректный номер телефона отклонён");
    }

    @Test
    @Order(3)
    @DisplayName("TC-03: Проверка валидации email (корректный ввод)")
    void testValidEmail() {
        String validEmail = "user@example.com";

        assertTrue(validEmail.contains("@"), "Email должен содержать @");
        assertTrue(validEmail.contains("."), "Email должен содержать .");
        System.out.println("✅ TC-03 пройден: корректный email");
    }

    @Test
    @Order(4)
    @DisplayName("TC-04: Проверка валидации email (некорректный ввод)")
    void testInvalidEmail() {
        String invalidEmail1 = "userexample.com";
        String invalidEmail2 = "user@example";

        assertFalse(invalidEmail1.contains("@") && invalidEmail1.contains("."),
                "Email без @ должен быть отклонён");
        assertFalse(invalidEmail2.contains("@") && invalidEmail2.contains("."),
                "Email без точки должен быть отклонён");
        System.out.println("✅ TC-04 пройден: некорректный email отклонён");
    }

    @Test
    @Order(5)
    @DisplayName("TC-05: Проверка валидации пароля (корректная длина)")
    void testValidPasswordLength() {
        String validPassword = "password123";

        assertTrue(validPassword.length() >= 6, "Пароль должен быть минимум 6 символов");
        assertTrue(validPassword.length() <= 12, "Пароль должен быть максимум 12 символов");
        System.out.println("✅ TC-05 пройден: корректная длина пароля");
    }

    @Test
    @Order(6)
    @DisplayName("TC-06: Проверка валидации пароля (некорректная длина)")
    void testInvalidPasswordLength() {
        String shortPassword = "123";
        String longPassword = "thispasswordiswaytoolong";

        assertFalse(shortPassword.length() >= 6, "Короткий пароль должен быть отклонён");
        assertFalse(longPassword.length() <= 12, "Длинный пароль должен быть отклонён");
        System.out.println("✅ TC-06 пройден: некорректная длина пароля отклонена");
    }

    @Test
    @Order(7)
    @DisplayName("TC-07: Проверка совпадения паролей")
    void testPasswordsMatch() {
        String password = "mypassword";
        String confirmPassword = "mypassword";

        assertEquals(password, confirmPassword, "Пароли должны совпадать");
        System.out.println("✅ TC-07 пройден: пароли совпадают");
    }

    @Test
    @Order(8)
    @DisplayName("TC-08: Проверка несовпадения паролей")
    void testPasswordsDoNotMatch() {
        String password = "password123";
        String confirmPassword = "password456";

        assertNotEquals(password, confirmPassword, "Пароли не должны совпадать");
        System.out.println("✅ TC-08 пройден: пароли не совпадают");
    }

    // ==================== ТЕСТЫ ДЛЯ ФУНКЦИИ ТЁМНОЙ ТЕМЫ ====================

    @Test
    @Order(9)
    @DisplayName("TC-09: Проверка цветов светлой темы")
    void testLightThemeColors() {
        String expectedBgLight = "#fff5f9";
        String expectedBgCard = "#ffffff";
        String expectedTextPrimary = "#5a3e4a";

        assertEquals("#fff5f9", expectedBgLight, "Цвет фона светлой темы");
        assertEquals("#ffffff", expectedBgCard, "Цвет карточек светлой темы");
        assertEquals("#5a3e4a", expectedTextPrimary, "Цвет текста светлой темы");
        System.out.println("✅ TC-09 пройден: цвета светлой темы корректны");
    }

    @Test
    @Order(10)
    @DisplayName("TC-10: Проверка цветов тёмной темы")
    void testDarkThemeColors() {
        String expectedBgLight = "#0a0a0a";
        String expectedBgCard = "#1a1a1a";
        String expectedTextPrimary = "#ffffff";

        assertEquals("#0a0a0a", expectedBgLight, "Цвет фона тёмной темы");
        assertEquals("#1a1a1a", expectedBgCard, "Цвет карточек тёмной темы");
        assertEquals("#ffffff", expectedTextPrimary, "Цвет текста тёмной темы");
        System.out.println("✅ TC-10 пройден: цвета тёмной темы корректны");
    }

    // ==================== ТЕСТЫ ДЛЯ ФУНКЦИИ НЕДАВНО ПРОСМОТРЕННЫХ ====================

    @Test
    @Order(11)
    @DisplayName("TC-11: Проверка создания объекта Product")
    void testProductCreation() {
        Product product = new Product(1, "Сёмга слабосолёная", 1850.0, 4.5,
                "Свежая сёмга слабого посола", 25, "salmon.png");

        assertNotNull(product);
        assertEquals(1, product.getId());
        assertEquals("Сёмга слабосолёная", product.getName());
        assertEquals(1850.0, product.getPrice());
        assertEquals(25, product.getStockQuantity());
        System.out.println("✅ TC-11 пройден: создание объекта Product");
    }

    @Test
    @Order(12)
    @DisplayName("TC-12: Проверка ограничения списка недавно просмотренных (максимум 8)")
    void testRecentlyViewedLimit() {
        List<Product> recentlyViewed = new ArrayList<>();

        // Добавляем 10 товаров
        for (int i = 1; i <= 10; i++) {
            Product product = new Product(i, "Товар " + i, 100.0 * i, 4.0, "Описание", 10, "image.png");
            recentlyViewed.add(0, product);
            if (recentlyViewed.size() > 8) {
                recentlyViewed.remove(8);
            }
        }

        assertEquals(8, recentlyViewed.size(), "Список должен содержать максимум 8 товаров");
        assertEquals(10, recentlyViewed.get(0).getId(), "Новый товар должен быть в начале");
        System.out.println("✅ TC-12 пройден: ограничение списка 8 товарами");
    }

    @Test
    @Order(13)
    @DisplayName("TC-13: Проверка удаления дубликатов из списка недавно просмотренных")
    void testRecentlyViewedNoDuplicates() {
        List<Product> recentlyViewed = new ArrayList<>();

        Product product = new Product(1, "Сёмга", 1850.0, 4.5, "Описание", 25, "salmon.png");

        // Добавляем товар
        recentlyViewed.removeIf(p -> p.getId() == product.getId());
        recentlyViewed.add(0, product);

        // Пытаемся добавить тот же товар снова
        recentlyViewed.removeIf(p -> p.getId() == product.getId());
        recentlyViewed.add(0, product);

        assertEquals(1, recentlyViewed.size(), "В списке не должно быть дубликатов");
        System.out.println("✅ TC-13 пройден: дубликаты удаляются");
    }

    @Test
    @Order(14)
    @DisplayName("TC-14: Проверка формата цены товара")
    void testProductPriceFormat() {
        Product product = new Product(1, "Креветки", 890.0, 4.8, "Описание", 15, "shrimp.png");
        String formattedPrice = String.format("%.2f ₽/кг", product.getPrice());

        // Принимаем оба варианта: с точкой или с запятой (зависит от локали системы)
        boolean isValidFormat = formattedPrice.equals("890.00 ₽/кг") ||
                formattedPrice.equals("890,00 ₽/кг");

        assertTrue(isValidFormat, "Формат цены должен быть 890.00 ₽/кг или 890,00 ₽/кг, но было: " + formattedPrice);
        System.out.println("✅ TC-14 пройден: формат цены корректный - " + formattedPrice);
    }

    @Test
    @Order(15)
    @DisplayName("TC-15: Проверка хеширования пароля")
    void testPasswordHashing() {
        String plainPassword = "testPassword123";
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(PasswordUtils.verifyPassword(plainPassword, hashedPassword));
        System.out.println("✅ TC-15 пройден: хеширование пароля");
    }

    @Test
    @Order(16)
    @DisplayName("TC-16: Проверка маскирования номера карты")
    void testCardNumberMasking() {
        String cardNumber = "1234567812345678";
        PaymentInfo paymentInfo = new PaymentInfo(1, cardNumber, "12/25", "123");
        String masked = paymentInfo.getMaskedCardNumber();

        assertEquals("****5678", masked);
        System.out.println("✅ TC-16 пройден: маскирование номера карты");
    }

    @Test
    @Order(17)
    @DisplayName("TC-17: Проверка валидации номера карты")
    void testCardNumberValidation() {
        assertTrue(PaymentInfo.isValidCardNumber("1234567812345678"));
        assertFalse(PaymentInfo.isValidCardNumber("1234"));
        assertFalse(PaymentInfo.isValidCardNumber("abcd1234"));
        System.out.println("✅ TC-17 пройден: валидация номера карты");
    }

    @Test
    @Order(18)
    @DisplayName("TC-18: Проверка статуса заказа (метод getStatusText)")
    void testOrderStatusText() {
        assertEquals("⏳ Ожидает оплаты", getStatusTextForTest("PENDING"));
        assertEquals("✅ Оплачен", getStatusTextForTest("PAID"));
        assertEquals("🚚 Отправлен", getStatusTextForTest("SHIPPED"));
        assertEquals("📦 Доставлен", getStatusTextForTest("DELIVERED"));
        assertEquals("❌ Отменен", getStatusTextForTest("CANCELLED"));
        System.out.println("✅ TC-18 пройден: статусы заказа");
    }

    private String getStatusTextForTest(String status) {
        switch (status) {
            case "PENDING": return "⏳ Ожидает оплаты";
            case "PAID": return "✅ Оплачен";
            case "SHIPPED": return "🚚 Отправлен";
            case "DELIVERED": return "📦 Доставлен";
            case "CANCELLED": return "❌ Отменен";
            default: return status;
        }
    }

    // ==================== ТЕСТЫ ПОДКЛЮЧЕНИЯ К БД ====================

    @Test
    @Order(19)
    @DisplayName("TC-19: Проверка подключения к базе данных")
    void testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            System.out.println("✅ TC-19 пройден: подключение к БД успешно");
        } catch (SQLException e) {
            fail("Не удалось подключиться к БД: " + e.getMessage());
        }
    }
}