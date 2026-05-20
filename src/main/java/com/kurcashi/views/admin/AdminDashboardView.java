package com.kurcashi.views.admin;

import com.kurcashi.dao.AdminDAO;
import com.kurcashi.models.Admin;
import com.kurcashi.models.User;
import com.kurcashi.models.Product;
import com.kurcashi.models.Category;
import com.kurcashi.models.Order;
import com.example.kurcashi.BaitDesireApp;
import com.kurcashi.utils.CustomAlert;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AdminDashboardView {

    // Константы для дублированных строковых литералов
    private static final String STYLE_BG_CARD = "-fx-background-color: ";
    private static final String STYLE_BG_RADIUS_12 = "; -fx-background-radius: 12; ";
    private static final String STYLE_EFFECT_SHADOW = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4); ";
    private static final String STYLE_BORDER = "-fx-border-color: ";
    private static final String STYLE_BORDER_RADIUS_12 = "; -fx-border-radius: 12; -fx-border-width: 1;";
    private static final String STYLE_TEXT_FILL = "-fx-text-fill: ";
    private static final String STYLE_FONT_SIZE_12 = "; -fx-font-size: 12px;";
    private static final String STYLE_FONT_SIZE_14 = "; -fx-font-size: 14px;";
    private static final String STYLE_FONT_SIZE_18 = "; -fx-font-size: 18px;";
    private static final String STYLE_FONT_WEIGHT_BOLD = "; -fx-font-weight: bold;";
    private static final String STYLE_PADDING_5_10 = "; -fx-padding: 5 10;";
    private static final String STYLE_BG_RADIUS_6 = "; -fx-background-radius: 6;";
    private static final String STYLE_CURSOR_HAND = "; -fx-cursor: hand;";
    private static final String STYLE_BG_TRANSPARENT = "-fx-background-color: transparent;";
    private static final String STYLE_BG_TRANSPARENT_BOTH = "-fx-background-color: transparent; -fx-background: transparent;";
    private static final String STYLE_FONT_SIZE_16_BOLD = "; -fx-font-size: 16px; -fx-font-weight: bold;";
    private static final String STYLE_FONT_SIZE_14_BOLD = "; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String STYLE_TEXT_FILL_WHITE = "; -fx-text-fill: white;";
    private static final String STYLE_TITLE_LABEL = STYLE_TEXT_FILL + "%s" + "; -fx-font-size: 18px; -fx-font-weight: bold;";
    private static final String STYLE_CANCEL_BUTTON_BASE = "-fx-background-color: " + "%s" + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;";
    private static final String BUTTON_SAVE_TEXT = "Сохранить";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String NOT_SPECIFIED = "Не указан";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String IMAGES_DIR_PATH = "src/main/resources/images/";
    private static final String CONFIRM_DELETE_TITLE = "Подтверждение удаления";

    private final Admin admin;
    private final BaitDesireApp app;
    private final AdminDAO adminDAO;
    private BorderPane view;

    // Сетки для карточек
    private GridPane usersGrid;
    private GridPane productsGrid;
    private GridPane ordersGrid;
    private GridPane categoriesGrid;
    private GridPane usersForRolesGrid;
    private GridPane logsGrid;

    public AdminDashboardView(Admin admin, BaitDesireApp app) {
        this.admin = admin;
        this.app = app;
        this.adminDAO = new AdminDAO();
        createView();
    }

    private void createView() {
        view = new BorderPane();
        view.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        VBox topBar = createTopBar();
        view.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.setStyle(STYLE_BG_TRANSPARENT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab statsTab = new Tab("Статистика");
        statsTab.setContent(createStatsTab());
        customizeTab(statsTab);

        Tab usersTab = new Tab("Пользователи");
        usersTab.setContent(createUsersTab());
        customizeTab(usersTab);

        Tab productsTab = new Tab("Товары");
        productsTab.setContent(createProductsTab());
        customizeTab(productsTab);

        Tab categoriesTab = new Tab("Категории");
        categoriesTab.setContent(createCategoriesTab());
        customizeTab(categoriesTab);

        Tab ordersTab = new Tab("Заказы");
        ordersTab.setContent(createOrdersTab());
        customizeTab(ordersTab);

        Tab assignRolesTab = new Tab("Назначение ролей");
        assignRolesTab.setContent(createUsersForRolesTab());
        customizeTab(assignRolesTab);

        Tab logsTab = new Tab("Журнал действий");
        logsTab.setContent(createLogsTab());
        customizeTab(logsTab);

        tabPane.getTabs().addAll(statsTab, usersTab, productsTab, categoriesTab, ordersTab, assignRolesTab, logsTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateTabStyle(tabPane);
            if (newTab == usersTab && usersGrid.getChildren().isEmpty()) refreshUsersGrid();
            else if (newTab == productsTab && productsGrid.getChildren().isEmpty()) refreshProductsGrid();
            else if (newTab == categoriesTab && categoriesGrid.getChildren().isEmpty()) refreshCategoriesGrid();
            else if (newTab == ordersTab && ordersGrid.getChildren().isEmpty()) refreshOrdersGrid();
            else if (newTab == assignRolesTab && usersForRolesGrid.getChildren().isEmpty()) refreshUsersForRolesGrid();
            else if (newTab == logsTab && logsGrid.getChildren().isEmpty()) refreshLogsGrid();
        });

        view.setCenter(tabPane);
    }

    private void customizeTab(Tab tab) {
        String bgColor = app.getCurrentBgCard();
        String textColor = app.isDarkTheme() ? "#ffffff" : app.getCurrentTextPrimary();
        tab.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10 10 0 0; -fx-padding: 8 20; -fx-font-size: 13px; -fx-text-fill: " + textColor + ";");
    }

    private void updateTabStyle(TabPane tabPane) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.isSelected()) {
                tab.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-background-radius: 10 10 0 0; -fx-padding: 8 20; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                customizeTab(tab);
            }
        }
    }

    private VBox createTopBar() {
        VBox topBar = new VBox(10);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + ";" +
                "-fx-border-color: " + app.getCurrentBorderColor() + ";" +
                "-fx-border-width: 0 0 1 0;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Label titleLabel = new Label("Панель администратора");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String roleDisplay = admin != null ? admin.getRoleDisplayName() : "Администратор";
        Label adminLabel = new Label("Админ: " + (admin != null ? admin.getUsername() : "Unknown") + " (" + roleDisplay + ")");
        adminLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);

        Button logoutButton = new Button("Выйти");
        app.updateButtonStyle(logoutButton);
        logoutButton.setOnAction(e -> app.showLoginView());

        Button backToUserButton = new Button("Перейти в магазин");
        app.updateButtonStyle(backToUserButton);
        backToUserButton.setOnAction(e -> {
            User tempUser = new User();
            if (admin != null) {
                tempUser.setUserId(admin.getAdminId());
                tempUser.setUsername(admin.getUsername());
                tempUser.setAdmin(true);
            } else {
                tempUser.setUserId(1);
                tempUser.setUsername("User");
                tempUser.setAdmin(true);
            }
            com.kurcashi.dao.UserDAO userDAO = new com.kurcashi.dao.UserDAO();
            User existingUser = userDAO.getUserById(tempUser.getUserId());
            if (existingUser != null) {
                app.showCatalogView(existingUser);
            } else {
                app.showCatalogView(tempUser);
            }
        });

        header.getChildren().addAll(titleLabel, spacer, adminLabel, backToUserButton, logoutButton);
        topBar.getChildren().add(header);
        return topBar;
    }

    private VBox createStatsTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        AdminDAO.AdminStats stats = adminDAO.getStats();

        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setPadding(new Insets(20));

        VBox userCard = createStatCard("Пользователи", String.valueOf(stats.userCount));
        VBox orderCard = createStatCard("Заказы", String.valueOf(stats.orderCount));
        VBox productCard = createStatCard("Товары", String.valueOf(stats.productCount));
        VBox revenueCard = createStatCard("Выручка", String.format("%.2f ₽", stats.totalRevenue));
        VBox adminCard = createStatCard("Администраторы", String.valueOf(stats.adminCount));

        statsGrid.getChildren().addAll(userCard, orderCard, productCard, revenueCard, adminCard);
        container.getChildren().add(statsGrid);
        return container;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 15; -fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccent() + "; -fx-font-size: 28px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    // ==================== ПОЛЬЗОВАТЕЛИ ====================
    private VBox createUsersTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshUsersGrid());
        toolbar.getChildren().add(refreshButton);

        usersGrid = new GridPane();
        usersGrid.setPadding(new Insets(10));
        usersGrid.setHgap(20);
        usersGrid.setVgap(20);
        usersGrid.setAlignment(Pos.TOP_CENTER);
        usersGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(usersGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshUsersGrid() {
        usersGrid.getChildren().clear();
        List<User> users = adminDAO.getAllUsers();
        int col = 0;
        int row = 0;
        final int maxCols = 4;
        for (User user : users) {
            VBox card = createUserCard(user);
            usersGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (users.isEmpty()) {
            Label emptyLabel = new Label("Нет пользователей");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            usersGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);

        Label nameLabel = new Label(user.getUsername());
        nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
        Label emailLabel = new Label("📧 " + user.getEmail());
        emailLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        Label phoneLabel = new Label("📱 " + Objects.requireNonNullElse(user.getPhone(), NOT_SPECIFIED));
        phoneLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        Label bonusLabel = new Label("🎁 Бонусы: " + user.getBonusPoints());
        bonusLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label adminLabel = new Label(user.isAdmin() ? "👑 Администратор" : "👤 Пользователь");
        adminLabel.setStyle(STYLE_TEXT_FILL + (user.isAdmin() ? app.getCurrentSuccess() : app.getCurrentTextSecondary()) + STYLE_FONT_SIZE_12);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button deleteButton = new Button("Удалить");
        deleteButton.setStyle("-fx-background-color: " + app.getCurrentError() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        deleteButton.setOnAction(e -> deleteUser(user));
        Button bonusButton = new Button("Бонусы");
        bonusButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        bonusButton.setOnAction(e -> showBonusDialog(user));
        buttonsBox.getChildren().addAll(bonusButton, deleteButton);

        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, bonusLabel, adminLabel, buttonsBox);
        return card;
    }

    private void deleteUser(User user) {
        boolean confirmed = showCustomConfirm(CONFIRM_DELETE_TITLE, "Удалить пользователя " + user.getUsername() + "?");
        if (confirmed) {
            boolean success = adminDAO.deleteUser(user.getUserId(), admin.getAdminId());
            if (success) {
                refreshUsersGrid();
                refreshUsersForRolesGrid();
                showInfo("Пользователь удалён");
            } else {
                showError("Не удалось удалить пользователя");
            }
        }
    }

    private void showBonusDialog(User user) {
        Stage dialogStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(400);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Изменить бонусы");
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        Label infoLabel = new Label("Пользователь: " + user.getUsername() + "\nТекущие бонусы: " + user.getBonusPoints());
        infoLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.CENTER);

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        Label bonusLabel = new Label("Новое значение:");
        bonusLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14);
        TextField bonusField = new TextField();
        bonusField.setPromptText("Бонусы");
        bonusField.setPrefWidth(150);
        app.updateTextFieldStyle(bonusField);
        inputBox.getChildren().addAll(bonusLabel, bonusField);

        HBox buttonBox = createButtonBoxForDialog(() -> {
            try {
                int newBonus = Integer.parseInt(bonusField.getText().trim());
                if (newBonus >= 0) {
                    boolean success = adminDAO.updateUserBonusPoints(user.getUserId(), newBonus, admin.getAdminId());
                    if (success) {
                        refreshUsersGrid();
                        refreshUsersForRolesGrid();
                        dialogStage.close();
                        showInfo("Бонусы обновлены");
                    } else {
                        showError("Ошибка обновления бонусов");
                    }
                } else {
                    showError("Бонусы не могут быть отрицательными");
                }
            } catch (NumberFormatException ex) {
                showError("Введите корректное число");
            }
        }, dialogStage::close);

        dialogBox.getChildren().addAll(titleLabel, infoLabel, inputBox, buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    // ==================== ТОВАРЫ ====================
    private VBox createProductsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button addButton = new Button("Добавить товар");
        app.updateButtonStyle(addButton);
        addButton.setOnAction(e -> showProductDialog(null));
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshProductsGrid());
        toolbar.getChildren().addAll(addButton, refreshButton);

        productsGrid = new GridPane();
        productsGrid.setPadding(new Insets(10));
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.TOP_CENTER);
        productsGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(productsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshProductsGrid() {
        productsGrid.getChildren().clear();
        List<Product> products = adminDAO.getAllProducts();
        int col = 0;
        int row = 0;
        final int maxCols = 4;
        for (Product product : products) {
            VBox card = createProductCard(product);
            productsGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (products.isEmpty()) {
            Label emptyLabel = new Label("Нет товаров");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            productsGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
        nameLabel.setWrapText(true);
        Label priceLabel = new Label(String.format("💰 %.2f ₽/кг", product.getPrice()));
        priceLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_14_BOLD);
        Label stockLabel = new Label("📦 В наличии: " + product.getStockQuantity() + " кг");
        stockLabel.setStyle(STYLE_TEXT_FILL + (product.getStockQuantity() > 0 ? app.getCurrentSuccess() : app.getCurrentError()) + STYLE_FONT_SIZE_12);
        Label ratingLabel = new Label("⭐ Рейтинг: " + String.format("%.1f", product.getRating()));
        ratingLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button editButton = new Button("Ред.");
        editButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        editButton.setOnAction(e -> showProductDialog(product));
        Button deleteButton = new Button("Удал.");
        deleteButton.setStyle("-fx-background-color: " + app.getCurrentError() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        deleteButton.setOnAction(e -> deleteProduct(product));
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, ratingLabel, buttonsBox);
        return card;
    }

    // Вспомогательные методы для снижения сложности showProductDialog
    private GridPane createProductFormGrid(ComboBox<Category> categoryCombo,
                                           TextField nameField, TextField priceField, TextField stockField,
                                           TextArea descArea, HBox imageBox) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Настройка колонок: первая - фиксированная ширина для меток, вторая - растягивается
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setMaxWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createBoldLabel("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createBoldLabel("Цена (₽/кг):"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(createBoldLabel("Количество (кг):"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(createBoldLabel("Описание:"), 0, 3);
        grid.add(descArea, 1, 3);
        grid.add(createBoldLabel("Изображение:"), 0, 4);
        grid.add(imageBox, 1, 4);
        grid.add(createBoldLabel("Категория:"), 0, 5);
        grid.add(categoryCombo, 1, 5);

        return grid;
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        label.setWrapText(false);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }

    private HBox createImageUploadBox(TextField imageField, Stage ownerStage) {
        Button chooseImageButton = new Button("Выбрать изображение");
        chooseImageButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение товара");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.PNG", "*.JPG", "*.JPEG", "*.GIF")
            );
            File selectedFile = fileChooser.showOpenDialog(ownerStage);
            if (selectedFile != null) {
                String ext = "";
                String fileName = selectedFile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    ext = fileName.substring(dotIndex);
                }
                String uniqueName = "product_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
                File destDir = new File(IMAGES_DIR_PATH);
                if (!destDir.exists() && !destDir.mkdirs()) {
                    showError("Не удалось создать папку для изображений");
                    return;
                }
                File destFile = new File(destDir, uniqueName);
                try {
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imageField.setText(uniqueName);
                    showInfo("Изображение загружено: " + uniqueName);
                } catch (IOException ex) {
                    showError("Ошибка копирования файла: " + ex.getMessage());
                }
            }
        });
        imageField.setPrefWidth(250);
        HBox imageBox = new HBox(10, imageField, chooseImageButton);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        return imageBox;
    }

    private void showProductDialog(Product product) {
        boolean isNew = (product == null);
        final Product finalProduct;
        if (isNew) {
            Product newProduct = new Product();
            newProduct.setRating(0.0);
            newProduct.setStockQuantity(0);
            finalProduct = newProduct;
        } else {
            finalProduct = product;
        }

        Stage dialogStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(550);
        dialogBox.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label(isNew ? "Добавление товара" : "Редактирование товара");
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        TextField nameField = new TextField(finalProduct.getName());
        TextField priceField = new TextField(String.valueOf(finalProduct.getPrice()));
        TextField stockField = new TextField(String.valueOf(finalProduct.getStockQuantity()));
        TextArea descArea = new TextArea(finalProduct.getDescription());
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        TextField imageField = new TextField(finalProduct.getImageUrl());
        imageField.setPromptText("Имя файла изображения или путь");

        HBox imageBox = createImageUploadBox(imageField, dialogStage);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        List<Category> categories = adminDAO.getAllCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Category c) { return c != null ? c.getCategoryName() : ""; }
            @Override
            public Category fromString(String string) { return null; }
        });
        if (finalProduct.getCategoryId() > 0) {
            categories.stream().filter(c -> c.getCategoryId() == finalProduct.getCategoryId()).findFirst().ifPresent(categoryCombo::setValue);
        }

        GridPane formGrid = createProductFormGrid(categoryCombo, nameField, priceField, stockField, descArea, imageBox);
        dialogBox.getChildren().addAll(titleLabel, formGrid);

        HBox buttonBox = createButtonBoxForDialog(() -> {
            try {
                finalProduct.setName(nameField.getText().trim());
                finalProduct.setPrice(Double.parseDouble(priceField.getText()));
                finalProduct.setStockQuantity(Integer.parseInt(stockField.getText()));
                finalProduct.setDescription(descArea.getText().trim());
                finalProduct.setImageUrl(imageField.getText().trim());
                if (categoryCombo.getValue() != null) {
                    finalProduct.setCategoryId(categoryCombo.getValue().getCategoryId());
                }
                boolean success;
                if (isNew) {
                    success = adminDAO.addProduct(finalProduct, admin.getAdminId());
                } else {
                    success = adminDAO.updateProduct(finalProduct, admin.getAdminId());
                }
                if (success) {
                    refreshProductsGrid();
                    dialogStage.close();
                    showInfo(isNew ? "Товар добавлен" : "Товар обновлён");
                } else {
                    showError("Ошибка сохранения товара");
                }
            } catch (NumberFormatException ex) {
                showError("Неверный формат цены или количества");
            }
        }, dialogStage::close);

        dialogBox.getChildren().add(buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private void deleteProduct(Product product) {
        boolean confirmed = showCustomConfirm(CONFIRM_DELETE_TITLE, "Удалить товар \"" + product.getName() + "\"?");
        if (confirmed) {
            boolean success = adminDAO.deleteProduct(product.getId(), admin.getAdminId());
            if (success) {
                refreshProductsGrid();
                showInfo("Товар удалён");
            } else {
                showError("Не удалось удалить товар");
            }
        }
    }

    // ==================== ЗАКАЗЫ ====================
    private VBox createOrdersTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshOrdersGrid());
        toolbar.getChildren().add(refreshButton);

        ordersGrid = new GridPane();
        ordersGrid.setPadding(new Insets(10));
        ordersGrid.setHgap(20);
        ordersGrid.setVgap(20);
        ordersGrid.setAlignment(Pos.TOP_CENTER);
        ordersGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(ordersGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshOrdersGrid() {
        ordersGrid.getChildren().clear();
        List<Order> orders = adminDAO.getAllOrders();
        int col = 0;
        int row = 0;
        final int maxCols = 4;
        for (Order order : orders) {
            VBox card = createOrderCard(order);
            ordersGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (orders.isEmpty()) {
            Label emptyLabel = new Label("Нет заказов");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            ordersGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);

        Label idLabel = createOrderIdLabel(order);
        Label userLabel = createUserLabel(order);
        Label amountLabel = createAmountLabel(order);
        Label statusLabel = createStatusLabel(order);
        Label paymentLabel = createPaymentLabel(order);
        Label addressLabel = createAddressLabel(order);
        HBox statusBox = createStatusBox(order);

        card.getChildren().addAll(idLabel, userLabel, amountLabel, statusLabel, paymentLabel, addressLabel, statusBox);
        return card;
    }

    private Label createOrderIdLabel(Order order) {
        Label label = new Label("Заказ №" + order.getOrderId());
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
        return label;
    }

    private Label createUserLabel(Order order) {
        Label label = new Label("👤 Пользователь ID: " + order.getCustomerId());
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        return label;
    }

    private Label createAmountLabel(Order order) {
        Label label = new Label(String.format("💰 Сумма: %.2f ₽", order.getTotalAmount()));
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_14_BOLD);
        return label;
    }

    private Label createStatusLabel(Order order) {
        String statusText = switch (order.getStatus()) {
            case "PENDING" -> "⏳ Ожидает оплаты";
            case "PAID" -> "✅ Оплачен";
            case "SHIPPED" -> "🚚 Отправлен";
            case STATUS_DELIVERED -> "📦 Доставлен";
            case STATUS_CANCELLED -> "❌ Отменен";
            default -> order.getStatus();
        };
        Label statusLabel = new Label("📌 Статус: " + statusText);
        String statusColor = getStatusColor(order);
        statusLabel.setStyle(STYLE_TEXT_FILL + statusColor + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
        return statusLabel;
    }

    private String getStatusColor(Order order) {
        if (STATUS_DELIVERED.equals(order.getStatus())) {
            return app.getCurrentSuccess();
        } else if (STATUS_CANCELLED.equals(order.getStatus())) {
            return app.getCurrentError();
        } else {
            return app.getCurrentTextSecondary();
        }
    }

    private Label createPaymentLabel(Order order) {
        Label label = new Label("💳 Оплата: " + (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD") ? "Карта" : "Наличные"));
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        return label;
    }

    private Label createAddressLabel(Order order) {
        Label label = new Label("📍 Адрес: " + Objects.requireNonNullElse(order.getAddress(), NOT_SPECIFIED));
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        label.setWrapText(true);
        return label;
    }

    private HBox createStatusBox(Order order) {
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PENDING", "PAID", "SHIPPED", STATUS_DELIVERED, STATUS_CANCELLED);
        statusCombo.setValue(order.getStatus());
        statusCombo.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + "; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        statusCombo.setPrefWidth(120);

        Button updateButton = new Button(UPDATE_BUTTON_TEXT);
        updateButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        updateButton.setOnAction(e -> {
            adminDAO.updateOrderStatus(order.getOrderId(), statusCombo.getValue(), admin.getAdminId());
            refreshOrdersGrid();
            showInfo("Статус заказа обновлён");
        });

        HBox statusBox = new HBox(10, statusCombo, updateButton);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        return statusBox;
    }

    // ==================== КАТЕГОРИИ ====================
    private VBox createCategoriesTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button addButton = new Button("Добавить категорию");
        app.updateButtonStyle(addButton);
        addButton.setOnAction(e -> showCategoryDialog(null));
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshCategoriesGrid());
        toolbar.getChildren().addAll(addButton, refreshButton);

        categoriesGrid = new GridPane();
        categoriesGrid.setPadding(new Insets(10));
        categoriesGrid.setHgap(20);
        categoriesGrid.setVgap(20);
        categoriesGrid.setAlignment(Pos.TOP_CENTER);
        categoriesGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(categoriesGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshCategoriesGrid() {
        categoriesGrid.getChildren().clear();
        List<Category> categories = adminDAO.getAllCategories();
        int col = 0;
        int row = 0;
        final int maxCols = 4;
        for (Category cat : categories) {
            VBox card = createCategoryCard(cat);
            categoriesGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (categories.isEmpty()) {
            Label emptyLabel = new Label("Нет категорий");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            categoriesGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createCategoryCard(Category category) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);

        Label nameLabel = new Label("📁 " + category.getCategoryName());
        nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
        Label descLabel = new Label(Objects.requireNonNullElse(category.getDescription(), "Без описания"));
        descLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        descLabel.setWrapText(true);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button editButton = new Button("Ред.");
        editButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        editButton.setOnAction(e -> showCategoryDialog(category));
        Button deleteButton = new Button("Удал.");
        deleteButton.setStyle("-fx-background-color: " + app.getCurrentError() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        deleteButton.setOnAction(e -> deleteCategory(category));
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, descLabel, buttonsBox);
        return card;
    }

    private void showCategoryDialog(Category category) {
        boolean isNew = (category == null);
        final Category finalCategory;
        if (isNew) {
            finalCategory = new Category(0, "", "");
        } else {
            finalCategory = category;
        }

        Stage dialogStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(500);
        dialogBox.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label(isNew ? "Добавление категории" : "Редактирование категории");
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(80);
        col1.setPrefWidth(80);
        col1.setMaxWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(finalCategory.getCategoryName());
        TextArea descArea = new TextArea(finalCategory.getDescription());
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        grid.add(createBoldLabel("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createBoldLabel("Описание:"), 0, 1);
        grid.add(descArea, 1, 1);

        dialogBox.getChildren().addAll(titleLabel, grid);

        HBox buttonBox = createButtonBoxForDialog(() -> {
            finalCategory.setCategoryName(nameField.getText().trim());
            finalCategory.setDescription(descArea.getText().trim());
            boolean success;
            if (isNew) {
                success = adminDAO.addCategory(finalCategory, admin.getAdminId());
            } else {
                success = adminDAO.updateCategory(finalCategory, admin.getAdminId());
            }
            if (success) {
                refreshCategoriesGrid();
                dialogStage.close();
                showInfo(isNew ? "Категория добавлена" : "Категория обновлена");
            } else {
                showError("Ошибка сохранения категории");
            }
        }, dialogStage::close);

        dialogBox.getChildren().add(buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private void deleteCategory(Category category) {
        boolean confirmed = showCustomConfirm(CONFIRM_DELETE_TITLE, "Удалить категорию \"" + category.getCategoryName() + "\"?");
        if (confirmed) {
            boolean success = adminDAO.deleteCategory(category.getCategoryId(), admin.getAdminId());
            if (success) {
                refreshCategoriesGrid();
                showInfo("Категория удалена");
            } else {
                showError("Не удалось удалить категорию");
            }
        }
    }

    // ==================== НАЗНАЧЕНИЕ РОЛЕЙ ====================
    private VBox createUsersForRolesTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        boolean isSuperAdmin = admin != null && "SUPER_ADMIN".equals(admin.getRole());

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        if (!isSuperAdmin) {
            Label warningLabel = new Label("Только главный администратор может назначать роли");
            warningLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
            toolbar.getChildren().add(warningLabel);
        } else {
            Label infoLabel = new Label("Вы имеете права главного администратора");
            infoLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentSuccess() + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
            toolbar.getChildren().add(infoLabel);
        }
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshUsersForRolesGrid());
        toolbar.getChildren().add(refreshButton);

        usersForRolesGrid = new GridPane();
        usersForRolesGrid.setPadding(new Insets(10));
        usersForRolesGrid.setHgap(20);
        usersForRolesGrid.setVgap(20);
        usersForRolesGrid.setAlignment(Pos.TOP_CENTER);
        usersForRolesGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(usersForRolesGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshUsersForRolesGrid() {
        usersForRolesGrid.getChildren().clear();
        List<User> users = adminDAO.getAllUsers();
        int col = 0;
        int row = 0;
        final int maxCols = 4;
        for (User user : users) {
            VBox card = createUserRoleCard(user);
            usersForRolesGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (users.isEmpty()) {
            Label emptyLabel = new Label("Нет пользователей");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            usersForRolesGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createUserRoleCard(User user) {
        boolean isSuperAdmin = admin != null && "SUPER_ADMIN".equals(admin.getRole());
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);

        Label nameLabel = new Label(user.getUsername());
        nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
        Label emailLabel = new Label("📧 " + user.getEmail());
        emailLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        Label phoneLabel = new Label("📱 " + Objects.requireNonNullElse(user.getPhone(), NOT_SPECIFIED));
        phoneLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        Label bonusLabel = new Label("🎁 Бонусы: " + user.getBonusPoints());
        bonusLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label adminStatusLabel = new Label(user.isAdmin() ? "👑 Администратор" : "👤 Пользователь");
        adminStatusLabel.setStyle(STYLE_TEXT_FILL + (user.isAdmin() ? app.getCurrentSuccess() : app.getCurrentTextSecondary()) + STYLE_FONT_SIZE_12);

        HBox controlsBox = createRoleControlsBox(user, isSuperAdmin);
        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, bonusLabel, adminStatusLabel, controlsBox);
        return card;
    }

    private HBox createRoleControlsBox(User user, boolean isSuperAdmin) {
        final User finalUser = user;
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        if (isSuperAdmin) {
            ComboBox<com.kurcashi.models.AdminRole> roleCombo = new ComboBox<>();
            List<com.kurcashi.models.AdminRole> roles = adminDAO.getAllRoles();
            roleCombo.setItems(FXCollections.observableArrayList(roles));
            roleCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(com.kurcashi.models.AdminRole role) {
                    return role != null ? role.getRoleName() : "";
                }
                @Override
                public com.kurcashi.models.AdminRole fromString(String string) {
                    return null;
                }
            });
            roleCombo.setPrefWidth(150);
            roleCombo.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + "; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
            if (!roles.isEmpty()) {
                roleCombo.setValue(roles.getFirst());
            }

            Button assignButton = new Button("Назначить");
            assignButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + "; -fx-padding: 5 12;" + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
            assignButton.setOnAction(e -> {
                if (roleCombo.getValue() != null) {
                    adminDAO.assignAdminRole(finalUser.getUserId(), roleCombo.getValue().getRoleCode(), admin.getAdminId());
                    refreshUsersForRolesGrid();
                    refreshUsersGrid();
                    showInfo("Роль назначена");
                }
            });

            Button revokeButton = new Button("Снять права");
            revokeButton.setStyle("-fx-background-color: " + app.getCurrentError() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + "; -fx-padding: 5 12;" + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
            revokeButton.setOnAction(e -> {
                adminDAO.revokeAdminRole(finalUser.getUserId(), admin.getAdminId());
                refreshUsersForRolesGrid();
                refreshUsersGrid();
                showInfo("Права сняты");
            });

            if (finalUser.isAdmin()) {
                assignButton.setDisable(true);
                roleCombo.setDisable(true);
                revokeButton.setDisable(false);
            } else {
                assignButton.setDisable(false);
                roleCombo.setDisable(false);
                revokeButton.setDisable(true);
            }
            controlsBox.getChildren().addAll(roleCombo, assignButton, revokeButton);
        } else {
            Label noPermLabel = new Label("Недостаточно прав");
            noPermLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_12);
            controlsBox.getChildren().add(noPermLabel);
        }
        return controlsBox;
    }

    // ==================== ЖУРНАЛ ДЕЙСТВИЙ ====================
    private VBox createLogsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshLogsGrid());
        toolbar.getChildren().add(refreshButton);

        logsGrid = new GridPane();
        logsGrid.setPadding(new Insets(10));
        logsGrid.setHgap(20);
        logsGrid.setVgap(20);
        logsGrid.setAlignment(Pos.TOP_CENTER);
        logsGrid.setStyle(STYLE_BG_TRANSPARENT);

        ScrollPane scrollPane = new ScrollPane(logsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(500);

        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshLogsGrid() {
        logsGrid.getChildren().clear();
        List<AdminDAO.AdminLog> logs = adminDAO.getAdminLogs(100);
        int col = 0;
        int row = 0;
        final int maxCols = 1;
        for (AdminDAO.AdminLog log : logs) {
            VBox card = createLogCard(log);
            logsGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
        if (logs.isEmpty()) {
            Label emptyLabel = new Label("Нет записей в журнале");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            logsGrid.add(emptyLabel, 0, 0);
        }
    }

    private VBox createLogCard(AdminDAO.AdminLog log) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(700);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + "; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2); " +
                STYLE_BORDER + app.getCurrentBorderColor() + "; -fx-border-radius: 10; -fx-border-width: 1;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label adminLabel = new Label("👤 " + (log.getAdminUsername() != null ? log.getAdminUsername() : "Unknown"));
        adminLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);
        Label actionLabel = new Label("[" + log.getActionType() + "]");
        actionLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccent() + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label dateLabel = new Label(log.getCreatedAt() != null ? log.getCreatedAt().toString().substring(0, 19) : "");
        dateLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + "; -fx-font-size: 11px;");
        header.getChildren().addAll(adminLabel, actionLabel, spacer, dateLabel);

        Label targetLabel = new Label("Объект: " + log.getTargetType() + " (ID: " + log.getTargetId() + ")");
        targetLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        Label detailsLabel = new Label("📝 " + (log.getDetails() != null ? log.getDetails() : "Нет подробностей"));
        detailsLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_12);
        detailsLabel.setWrapText(true);

        card.getChildren().addAll(header, targetLabel, detailsLabel);
        return card;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ДИАЛОГОВ ====================
    private Stage createDialogStage() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(app.getPrimaryStage());
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("");
        return stage;
    }

    private VBox createBaseDialogBox(double width) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(25));
        box.setPrefWidth(width);
        box.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + "; -fx-background-radius: 15; " +
                "-fx-border-color: " + app.getCurrentAccent() + "; -fx-border-radius: 15; -fx-border-width: 2;");
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20.0);
        dropShadow.setOffsetX(5.0);
        dropShadow.setOffsetY(5.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.3));
        box.setEffect(dropShadow);
        return box;
    }

    private HBox createButtonBoxForDialog(Runnable saveAction, Runnable closeAction) {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        Button saveButton = new Button(BUTTON_SAVE_TEXT);
        app.updateButtonStyle(saveButton);
        Button cancelButton = createCancelButton();
        cancelButton.setOnAction(e -> closeAction.run());
        saveButton.setOnAction(e -> saveAction.run());
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        return buttonBox;
    }

    private Button createCancelButton() {
        Button cancelButton = new Button("Отмена");
        String baseStyle = STYLE_CANCEL_BUTTON_BASE.formatted(app.getCurrentTextSecondary());
        String hoverStyle = STYLE_CANCEL_BUTTON_BASE.formatted(app.getCurrentTextPrimary());
        cancelButton.setStyle(baseStyle);
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(hoverStyle));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(baseStyle));
        return cancelButton;
    }

    private void finishDialog(Stage stage, VBox dialogBox) {
        Scene scene = new Scene(dialogBox);
        scene.setFill(null);
        stage.setScene(scene);
        centerStage(stage);
    }

    private void centerStage(Stage stage) {
        if (app.getPrimaryStage() != null) {
            stage.setOnShown(e -> {
                stage.setX(app.getPrimaryStage().getX() + (app.getPrimaryStage().getWidth() - stage.getWidth()) / 2);
                stage.setY(app.getPrimaryStage().getY() + (app.getPrimaryStage().getHeight() - stage.getHeight()) / 2);
            });
        } else {
            stage.centerOnScreen();
        }
    }

    private void showInfo(String message) {
        CustomAlert.show(app.getPrimaryStage(), "Информация", message, CustomAlert.AlertType.INFO);
    }

    private void showError(String message) {
        CustomAlert.show(app.getPrimaryStage(), "Ошибка", message, CustomAlert.AlertType.ERROR);
    }

    private boolean showCustomConfirm(String title, String message) {
        Stage confirmStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(400);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        Label messageLabel = new Label(message);
        messageLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        Button yesButton = new Button("Да");
        yesButton.setPrefWidth(100);
        app.updateButtonStyle(yesButton);
        Button noButton = createCancelButton();
        noButton.setText("Нет");
        noButton.setPrefWidth(100);

        final boolean[] result = {false};
        yesButton.setOnAction(e -> {
            result[0] = true;
            confirmStage.close();
        });
        noButton.setOnAction(e -> confirmStage.close());

        buttonBox.getChildren().addAll(yesButton, noButton);
        dialogBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);
        finishDialog(confirmStage, dialogBox);
        confirmStage.showAndWait();
        return result[0];
    }

    public BorderPane getView() {
        return view;
    }
}