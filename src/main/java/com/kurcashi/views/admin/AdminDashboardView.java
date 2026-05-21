package com.kurcashi.views.admin;

import com.kurcashi.dao.AdminDAO;
import com.kurcashi.dao.TagDAO;
import com.kurcashi.models.Admin;
import com.kurcashi.models.Tag;
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
    private static final String BUTTON_EDIT_TEXT = "Ред.";
    private static final String BUTTON_DELETE_TEXT = "Удал.";
    private static final String LABEL_NAME = "Название:";
    private static final String LABEL_DESCRIPTION = "Описание:";
    private static final String LABEL_PRICE = "Цена (₽/кг):";
    private static final String LABEL_STOCK = "Количество (кг):";
    private static final String LABEL_IMAGE = "Изображение:";
    private static final String LABEL_CATEGORY = "Категория:";
    private static final String LABEL_ORDER = "Порядок:";

    private final Admin admin;
    private final BaitDesireApp app;
    private final AdminDAO adminDAO;
    private final TagDAO tagDAO;
    private BorderPane view;

    private GridPane usersGrid;
    private GridPane productsGrid;
    private GridPane ordersGrid;
    private GridPane categoriesGrid;
    private GridPane usersForRolesGrid;
    private GridPane logsGrid;
    private GridPane tagsGrid;

    public AdminDashboardView(Admin admin, BaitDesireApp app) {
        this.admin = admin;
        this.app = app;
        this.adminDAO = new AdminDAO();
        this.tagDAO = new TagDAO();
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

        Tab tagsTab = new Tab("Теги");
        tagsTab.setContent(createTagsTab());
        customizeTab(tagsTab);

        tabPane.getTabs().addAll(statsTab, usersTab, productsTab, categoriesTab, ordersTab, assignRolesTab, logsTab, tagsTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateTabStyle(tabPane);
            if (newTab == usersTab && usersGrid.getChildren().isEmpty()) refreshUsersGrid();
            else if (newTab == productsTab && productsGrid.getChildren().isEmpty()) refreshProductsGrid();
            else if (newTab == categoriesTab && categoriesGrid.getChildren().isEmpty()) refreshCategoriesGrid();
            else if (newTab == ordersTab && ordersGrid.getChildren().isEmpty()) refreshOrdersGrid();
            else if (newTab == assignRolesTab && usersForRolesGrid.getChildren().isEmpty()) refreshUsersForRolesGrid();
            else if (newTab == logsTab && logsGrid.getChildren().isEmpty()) refreshLogsGrid();
            else if (newTab == tagsTab && tagsGrid.getChildren().isEmpty()) refreshTagsGrid();
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
            app.showCatalogView(existingUser != null ? existingUser : tempUser);
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

        HBox toolbar = createRefreshToolbar(this::refreshUsersGrid);
        usersGrid = new GridPane();
        configureGridPane(usersGrid);

        ScrollPane scrollPane = createScrollPane(usersGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshUsersGrid() {
        clearAndFillGrid(usersGrid, adminDAO.getAllUsers(), this::createUserCard);
    }

    private VBox createUserCard(User user) {
        VBox card = createBaseCard(260);
        Label nameLabel = createLabel(user.getUsername(), STYLE_FONT_SIZE_16_BOLD, app.getCurrentTextPrimary());
        Label emailLabel = createLabel("📧 " + user.getEmail(), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label phoneLabel = createLabel("📱 " + Objects.requireNonNullElse(user.getPhone(), NOT_SPECIFIED), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label bonusLabel = createBoldLabel("🎁 Бонусы: " + user.getBonusPoints(), "13px", app.getCurrentAccentDark());
        Label adminLabel = createLabel(user.isAdmin() ? "👑 Администратор" : "👤 Пользователь", STYLE_FONT_SIZE_12,
                user.isAdmin() ? app.getCurrentSuccess() : app.getCurrentTextSecondary());

        HBox buttonsBox = createUserActionButtons(user);
        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, bonusLabel, adminLabel, buttonsBox);
        return card;
    }

    private HBox createUserActionButtons(User user) {
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button deleteButton = createColoredButton("Удалить", app.getCurrentError());
        deleteButton.setOnAction(e -> deleteUser(user));
        Button bonusButton = createColoredButton("Бонусы", app.getCurrentAccent());
        bonusButton.setOnAction(e -> showBonusDialog(user));
        buttonsBox.getChildren().addAll(bonusButton, deleteButton);
        return buttonsBox;
    }

    private void deleteUser(User user) {
        if (showCustomConfirm("Удалить пользователя " + user.getUsername() + "?")) {
            if (adminDAO.deleteUser(user.getUserId(), admin.getAdminId())) {
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

        HBox inputBox = createBonusInputBox();
        HBox buttonBox = createButtonBoxForDialog(() -> {
            TextField bonusField = (TextField) inputBox.getChildren().get(1);
            int newBonus = Integer.parseInt(bonusField.getText().trim());
            if (newBonus >= 0) {
                if (adminDAO.updateUserBonusPoints(user.getUserId(), newBonus, admin.getAdminId())) {
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
        }, dialogStage::close);

        dialogBox.getChildren().addAll(titleLabel, infoLabel, inputBox, buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private HBox createBonusInputBox() {
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        Label bonusLabel = new Label("Новое значение:");
        bonusLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14);
        TextField bonusField = new TextField();
        bonusField.setPromptText("Бонусы");
        bonusField.setPrefWidth(150);
        app.updateTextFieldStyle(bonusField);
        inputBox.getChildren().addAll(bonusLabel, bonusField);
        return inputBox;
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
        configureGridPane(productsGrid);
        ScrollPane scrollPane = createScrollPane(productsGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshProductsGrid() {
        clearAndFillGrid(productsGrid, adminDAO.getAllProducts(), this::createProductCard);
    }

    private VBox createProductCard(Product product) {
        VBox card = createBaseCard(260);
        Label nameLabel = createLabel(product.getName(), STYLE_FONT_SIZE_16_BOLD, app.getCurrentTextPrimary());
        nameLabel.setWrapText(true);
        Label priceLabel = createLabel(String.format("💰 %.2f ₽/кг", product.getPrice()), STYLE_FONT_SIZE_14_BOLD, app.getCurrentAccentDark());
        Label stockLabel = createLabel("📦 В наличии: " + product.getStockQuantity() + " кг", STYLE_FONT_SIZE_12,
                product.getStockQuantity() > 0 ? app.getCurrentSuccess() : app.getCurrentError());
        Label ratingLabel = createLabel("⭐ Рейтинг: " + String.format("%.1f", product.getRating()), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button editButton = createColoredButton(BUTTON_EDIT_TEXT, app.getCurrentAccent());
        editButton.setOnAction(e -> showProductDialog(product));
        Button deleteButton = createColoredButton(BUTTON_DELETE_TEXT, app.getCurrentError());
        deleteButton.setOnAction(e -> deleteProduct(product));
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, ratingLabel, buttonsBox);
        return card;
    }

    private void showProductDialog(Product product) {
        boolean isNew = (product == null);
        final Product finalProduct = (isNew) ? new Product() : product;
        if (isNew) {
            finalProduct.setRating(0.0);
            finalProduct.setStockQuantity(0);
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
        ComboBox<Category> categoryCombo = createCategoryCombo(finalProduct);

        GridPane formGrid = createProductFormGrid(categoryCombo, nameField, priceField, stockField, descArea, imageBox);
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
                boolean success = isNew ? adminDAO.addProduct(finalProduct, admin.getAdminId()) : adminDAO.updateProduct(finalProduct, admin.getAdminId());
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

        dialogBox.getChildren().addAll(titleLabel, formGrid, buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private void deleteProduct(Product product) {
        if (showCustomConfirm("Удалить товар \"" + product.getName() + "\"?")) {
            if (adminDAO.deleteProduct(product.getId(), admin.getAdminId())) {
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

        HBox toolbar = createRefreshToolbar(this::refreshOrdersGrid);
        ordersGrid = new GridPane();
        configureGridPane(ordersGrid);
        ScrollPane scrollPane = createScrollPane(ordersGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshOrdersGrid() {
        clearAndFillGrid(ordersGrid, adminDAO.getAllOrders(), this::createOrderCard);
    }

    private VBox createOrderCard(Order order) {
        VBox card = createBaseCard(280);
        Label idLabel = createOrderIdLabel(order);
        Label userLabel = createLabel("👤 Пользователь ID: " + order.getCustomerId(), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label amountLabel = createLabel(String.format("💰 Сумма: %.2f ₽", order.getTotalAmount()), STYLE_FONT_SIZE_14_BOLD, app.getCurrentAccentDark());
        Label statusLabel = createStatusLabel(order);
        Label paymentLabel = createLabel("💳 Оплата: " + (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD") ? "Карта" : "Наличные"), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label addressLabel = createLabel("📍 Адрес: " + Objects.requireNonNullElse(order.getAddress(), NOT_SPECIFIED), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        addressLabel.setWrapText(true);
        HBox statusBox = createOrderStatusBox(order);

        card.getChildren().addAll(idLabel, userLabel, amountLabel, statusLabel, paymentLabel, addressLabel, statusBox);
        return card;
    }

    private Label createOrderIdLabel(Order order) {
        Label label = new Label("Заказ №" + order.getOrderId());
        label.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16_BOLD);
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
        String statusColor = getStatusColor(order);
        Label statusLabel = new Label("📌 Статус: " + statusText);
        statusLabel.setStyle(STYLE_TEXT_FILL + statusColor + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
        return statusLabel;
    }

    private String getStatusColor(Order order) {
        if (STATUS_DELIVERED.equals(order.getStatus())) return app.getCurrentSuccess();
        if (STATUS_CANCELLED.equals(order.getStatus())) return app.getCurrentError();
        return app.getCurrentTextSecondary();
    }

    private HBox createOrderStatusBox(Order order) {
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PENDING", "PAID", "SHIPPED", STATUS_DELIVERED, STATUS_CANCELLED);
        statusCombo.setValue(order.getStatus());
        statusCombo.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + "; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        statusCombo.setPrefWidth(120);

        Button updateButton = createColoredButton(UPDATE_BUTTON_TEXT, app.getCurrentAccent());
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
        configureGridPane(categoriesGrid);
        ScrollPane scrollPane = createScrollPane(categoriesGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshCategoriesGrid() {
        clearAndFillGrid(categoriesGrid, adminDAO.getAllCategories(), this::createCategoryCard);
    }

    private VBox createCategoryCard(Category category) {
        VBox card = createBaseCard(260);
        Label nameLabel = createLabel("📁 " + category.getCategoryName(), STYLE_FONT_SIZE_16_BOLD, app.getCurrentTextPrimary());
        Label descLabel = createLabel(Objects.requireNonNullElse(category.getDescription(), "Без описания"), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        descLabel.setWrapText(true);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button editButton = createColoredButton(BUTTON_EDIT_TEXT, app.getCurrentAccent());
        editButton.setOnAction(e -> showCategoryDialog(category));
        Button deleteButton = createColoredButton(BUTTON_DELETE_TEXT, app.getCurrentError());
        deleteButton.setOnAction(e -> deleteCategory(category));
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, descLabel, buttonsBox);
        return card;
    }

    private void showCategoryDialog(Category category) {
        boolean isNew = (category == null);
        final Category finalCategory = (isNew) ? new Category(0, "", "") : category;

        Stage dialogStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(500);
        dialogBox.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label(isNew ? "Добавление категории" : "Редактирование категории");
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        GridPane grid = createCategoryFormGrid(finalCategory);
        HBox buttonBox = createButtonBoxForDialog(() -> {
            TextField nameField = (TextField) grid.lookup("#nameField");
            TextArea descArea = (TextArea) grid.lookup("#descArea");
            finalCategory.setCategoryName(nameField.getText().trim());
            finalCategory.setDescription(descArea.getText().trim());
            boolean success = isNew ? adminDAO.addCategory(finalCategory, admin.getAdminId()) : adminDAO.updateCategory(finalCategory, admin.getAdminId());
            if (success) {
                refreshCategoriesGrid();
                dialogStage.close();
                showInfo(isNew ? "Категория добавлена" : "Категория обновлена");
            } else {
                showError("Ошибка сохранения категории");
            }
        }, dialogStage::close);

        dialogBox.getChildren().addAll(titleLabel, grid, buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private void deleteCategory(Category category) {
        if (showCustomConfirm("Удалить категорию \"" + category.getCategoryName() + "\"?")) {
            if (adminDAO.deleteCategory(category.getCategoryId(), admin.getAdminId())) {
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
        HBox toolbar = createRoleToolbar(isSuperAdmin);
        usersForRolesGrid = new GridPane();
        configureGridPane(usersForRolesGrid);
        ScrollPane scrollPane = createScrollPane(usersForRolesGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshUsersForRolesGrid() {
        clearAndFillGrid(usersForRolesGrid, adminDAO.getAllUsers(), this::createUserRoleCard);
    }

    private VBox createUserRoleCard(User user) {
        boolean isSuperAdmin = admin != null && "SUPER_ADMIN".equals(admin.getRole());
        VBox card = createBaseCard(300);
        Label nameLabel = createLabel(user.getUsername(), STYLE_FONT_SIZE_16_BOLD, app.getCurrentTextPrimary());
        Label emailLabel = createLabel("📧 " + user.getEmail(), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label phoneLabel = createLabel("📱 " + Objects.requireNonNullElse(user.getPhone(), NOT_SPECIFIED), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label bonusLabel = createBoldLabel("🎁 Бонусы: " + user.getBonusPoints(), "13px", app.getCurrentAccentDark());
        Label adminStatusLabel = createLabel(user.isAdmin() ? "👑 Администратор" : "👤 Пользователь", STYLE_FONT_SIZE_12,
                user.isAdmin() ? app.getCurrentSuccess() : app.getCurrentTextSecondary());

        HBox controlsBox = createRoleControlsBox(user, isSuperAdmin);
        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, bonusLabel, adminStatusLabel, controlsBox);
        return card;
    }

    private HBox createRoleControlsBox(User user, boolean isSuperAdmin) {
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
            if (!roles.isEmpty()) roleCombo.setValue(roles.getFirst());

            Button assignButton = createColoredButton("Назначить", app.getCurrentAccent());
            assignButton.setOnAction(e -> {
                if (roleCombo.getValue() != null) {
                    adminDAO.assignAdminRole(user.getUserId(), roleCombo.getValue().getRoleCode(), admin.getAdminId());
                    refreshUsersForRolesGrid();
                    refreshUsersGrid();
                    showInfo("Роль назначена");
                }
            });

            Button revokeButton = createColoredButton("Снять права", app.getCurrentError());
            revokeButton.setOnAction(e -> {
                adminDAO.revokeAdminRole(user.getUserId(), admin.getAdminId());
                refreshUsersForRolesGrid();
                refreshUsersGrid();
                showInfo("Права сняты");
            });

            if (user.isAdmin()) {
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

    private HBox createRoleToolbar(boolean isSuperAdmin) {
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
        return toolbar;
    }

    // ==================== ЖУРНАЛ ДЕЙСТВИЙ ====================
    private VBox createLogsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = createRefreshToolbar(this::refreshLogsGrid);
        logsGrid = new GridPane();
        logsGrid.setPadding(new Insets(10));
        logsGrid.setHgap(20);
        logsGrid.setVgap(20);
        logsGrid.setAlignment(Pos.TOP_CENTER);
        logsGrid.setStyle(STYLE_BG_TRANSPARENT);
        ScrollPane scrollPane = createScrollPane(logsGrid);
        scrollPane.setPrefHeight(500);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshLogsGrid() {
        clearAndFillGrid(logsGrid, adminDAO.getAdminLogs(100), this::createLogCard);
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
        Label adminLabel = createLabel("👤 " + (log.getAdminUsername() != null ? log.getAdminUsername() : "Unknown"), STYLE_FONT_SIZE_14_BOLD, app.getCurrentTextPrimary());
        Label actionLabel = createLabel("[" + log.getActionType() + "]", STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD, app.getCurrentAccent());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label dateLabel = createLabel(log.getCreatedAt() != null ? log.getCreatedAt().toString().substring(0, 19) : "", "; -fx-font-size: 11px;", app.getCurrentTextSecondary());
        header.getChildren().addAll(adminLabel, actionLabel, spacer, dateLabel);

        Label targetLabel = createLabel("Объект: " + log.getTargetType() + " (ID: " + log.getTargetId() + ")", STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label detailsLabel = createLabel("📝 " + (log.getDetails() != null ? log.getDetails() : "Нет подробностей"), STYLE_FONT_SIZE_12, app.getCurrentTextPrimary());
        detailsLabel.setWrapText(true);

        card.getChildren().addAll(header, targetLabel, detailsLabel);
        return card;
    }

    // ==================== ТЕГИ ====================
    private VBox createTagsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button addButton = new Button("Добавить тег");
        app.updateButtonStyle(addButton);
        addButton.setOnAction(e -> showTagDialog(null));
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshTagsGrid());
        toolbar.getChildren().addAll(addButton, refreshButton);

        tagsGrid = new GridPane();
        configureGridPane(tagsGrid);
        ScrollPane scrollPane = createScrollPane(tagsGrid);
        container.getChildren().addAll(toolbar, scrollPane);
        return container;
    }

    private void refreshTagsGrid() {
        clearAndFillGrid(tagsGrid, tagDAO.getAllTags(), this::createTagCard);
    }

    private VBox createTagCard(Tag tag) {
        VBox card = createBaseCard(260);
        Label nameLabel = createLabel("🏷️ " + tag.getTagName(), STYLE_FONT_SIZE_16_BOLD, app.getCurrentTextPrimary());
        Label orderLabel = createLabel(LABEL_ORDER + " " + tag.getDisplayOrder(), STYLE_FONT_SIZE_12, app.getCurrentTextSecondary());
        Label activeLabel = createLabel(tag.isActive() ? "🟢 Активен" : "🔴 Неактивен", STYLE_FONT_SIZE_12,
                tag.isActive() ? app.getCurrentSuccess() : app.getCurrentError());

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        Button editButton = createColoredButton(BUTTON_EDIT_TEXT, app.getCurrentAccent());
        editButton.setOnAction(e -> showTagDialog(tag));
        Button deleteButton = createColoredButton(BUTTON_DELETE_TEXT, app.getCurrentError());
        deleteButton.setOnAction(e -> deleteTag(tag));
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameLabel, orderLabel, activeLabel, buttonsBox);
        return card;
    }

    private void showTagDialog(Tag tag) {
        boolean isNew = (tag == null);
        final Tag finalTag = (isNew) ? new Tag("", 0) : tag;

        Stage dialogStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(450);
        dialogBox.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label(isNew ? "Добавление тега" : "Редактирование тега");
        titleLabel.setStyle(String.format(STYLE_TITLE_LABEL, app.getCurrentTextPrimary()));

        GridPane grid = createTagFormGrid(finalTag);
        HBox buttonBox = createButtonBoxForDialog(() -> {
            TextField nameField = (TextField) grid.lookup("#tagNameField");
            TextField orderField = (TextField) grid.lookup("#tagOrderField");
            CheckBox activeCheck = (CheckBox) grid.lookup("#tagActiveCheck");

            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Название тега не может быть пустым");
                return;
            }
            try {
                int order = Integer.parseInt(orderField.getText());
                finalTag.setTagName(name);
                finalTag.setDisplayOrder(order);
                finalTag.setActive(activeCheck.isSelected());

                boolean success = isNew ? tagDAO.addTag(finalTag) : tagDAO.updateTag(finalTag);
                if (success) {
                    refreshTagsGrid();
                    dialogStage.close();
                    showInfo(isNew ? "Тег добавлен" : "Тег обновлён");
                    app.refreshTags();
                } else {
                    showError("Ошибка сохранения тега");
                }
            } catch (NumberFormatException ex) {
                showError("Порядок должен быть числом");
            }
        }, dialogStage::close);

        dialogBox.getChildren().addAll(titleLabel, grid, buttonBox);
        finishDialog(dialogStage, dialogBox);
        dialogStage.showAndWait();
    }

    private void deleteTag(Tag tag) {
        if (showCustomConfirm("Удалить тег \"" + tag.getTagName() + "\"?")) {
            if (tagDAO.deleteTag(tag.getTagId())) {
                refreshTagsGrid();
                showInfo("Тег удалён");
                app.refreshTags();
            } else {
                showError("Не удалось удалить тег");
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    private HBox createRefreshToolbar(Runnable refreshAction) {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        Button refreshButton = new Button(UPDATE_BUTTON_TEXT);
        app.updateButtonStyle(refreshButton);
        refreshButton.setOnAction(e -> refreshAction.run());
        toolbar.getChildren().add(refreshButton);
        return toolbar;
    }

    private <T> void clearAndFillGrid(GridPane grid, List<T> items, java.util.function.Function<T, VBox> cardCreator) {
        grid.getChildren().clear();
        int col = 0;
        int row = 0;
        int maxCols = (grid == logsGrid) ? 1 : 4;
        for (T item : items) {
            VBox card = cardCreator.apply(item);
            grid.add(card, col, row);
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
        if (items.isEmpty()) {
            Label emptyLabel = new Label("Нет данных");
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_18);
            grid.add(emptyLabel, 0, 0);
        }
    }

    private void configureGridPane(GridPane grid) {
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setStyle(STYLE_BG_TRANSPARENT);
    }

    private ScrollPane createScrollPane(GridPane grid) {
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox createBaseCard(double prefWidth) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(prefWidth);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);
        return card;
    }

    private Label createLabel(String text, String additionalStyle, String color) {
        Label label = new Label(text);
        label.setStyle(STYLE_TEXT_FILL + color + additionalStyle);
        return label;
    }

    // Перегрузка createBoldLabel с одним аргументом (использует стандартные размер и цвет)
    private Label createBoldLabel(String text) {
        return createBoldLabel(text, "14px", app.getCurrentTextPrimary());
    }

    private Label createBoldLabel(String text, String fontSize, String color) {
        Label label = new Label(text);
        label.setStyle(STYLE_TEXT_FILL + color + "; -fx-font-size: " + fontSize + STYLE_FONT_WEIGHT_BOLD);
        return label;
    }

    private Button createColoredButton(String text, String bgColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + bgColor + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        return button;
    }

    private GridPane createProductFormGrid(ComboBox<Category> categoryCombo, TextField nameField, TextField priceField,
                                           TextField stockField, TextArea descArea, HBox imageBox) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));
        ColumnConstraints col1 = new ColumnConstraints(100, 100, 120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createBoldLabel(LABEL_NAME), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createBoldLabel(LABEL_PRICE), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(createBoldLabel(LABEL_STOCK), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(createBoldLabel(LABEL_DESCRIPTION), 0, 3);
        grid.add(descArea, 1, 3);
        grid.add(createBoldLabel(LABEL_IMAGE), 0, 4);
        grid.add(imageBox, 1, 4);
        grid.add(createBoldLabel(LABEL_CATEGORY), 0, 5);
        grid.add(categoryCombo, 1, 5);
        return grid;
    }

    private GridPane createCategoryFormGrid(Category category) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));
        ColumnConstraints col1 = new ColumnConstraints(80, 80, 100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(category.getCategoryName());
        nameField.setId("nameField");
        TextArea descArea = new TextArea(category.getDescription());
        descArea.setId("descArea");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        grid.add(createBoldLabel(LABEL_NAME), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createBoldLabel(LABEL_DESCRIPTION), 0, 1);
        grid.add(descArea, 1, 1);
        return grid;
    }

    private GridPane createTagFormGrid(Tag tag) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));
        ColumnConstraints col1 = new ColumnConstraints(80, 80, 100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(tag.getTagName());
        nameField.setId("tagNameField");
        TextField orderField = new TextField(String.valueOf(tag.getDisplayOrder()));
        orderField.setId("tagOrderField");
        CheckBox activeCheck = new CheckBox("Активен");
        activeCheck.setId("tagActiveCheck");
        activeCheck.setSelected(tag.isActive());

        grid.add(createBoldLabel(LABEL_NAME), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createBoldLabel(LABEL_ORDER), 0, 1);
        grid.add(orderField, 1, 1);
        grid.add(activeCheck, 1, 2);
        return grid;
    }

    private ComboBox<Category> createCategoryCombo(Product product) {
        ComboBox<Category> combo = new ComboBox<>();
        List<Category> categories = adminDAO.getAllCategories();
        combo.setItems(FXCollections.observableArrayList(categories));
        combo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Category c) { return c != null ? c.getCategoryName() : ""; }
            @Override
            public Category fromString(String string) { return null; }
        });
        if (product.getCategoryId() > 0) {
            categories.stream().filter(c -> c.getCategoryId() == product.getCategoryId()).findFirst().ifPresent(combo::setValue);
        }
        return combo;
    }

    private HBox createImageUploadBox(TextField imageField, Stage ownerStage) {
        Button chooseImageButton = new Button("Выбрать изображение");
        chooseImageButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_5_10 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение товара");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(ownerStage);
            if (selectedFile != null) {
                String ext = "";
                String fileName = selectedFile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) ext = fileName.substring(dotIndex);
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
        return new HBox(10, imageField, chooseImageButton);
    }

    private Stage createDialogStage() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(app.getPrimaryStage());
        stage.initStyle(StageStyle.TRANSPARENT);
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

    private boolean showCustomConfirm(String message) {
        Stage confirmStage = createDialogStage();
        VBox dialogBox = createBaseDialogBox(400);
        dialogBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(CONFIRM_DELETE_TITLE);
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