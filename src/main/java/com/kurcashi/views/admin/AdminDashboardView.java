package com.kurcashi.views.admin;

import com.kurcashi.dao.*;
import com.kurcashi.models.*;
import com.example.kurcashi.BaitDesireApp;
import com.kurcashi.utils.CustomAlert;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardView {

    private final Admin admin;
    private final BaitDesireApp app;
    private final AdminDAO adminDAO;
    private final TagDAO tagDAO;
    private final RecipeDAO recipeDAO;
    private final RecommendationDAO recommendationDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final AnalyticsDAO analyticsDAO;
    private BorderPane view;
    private StackPane contentArea;

    public AdminDashboardView(Admin admin, BaitDesireApp app) {
        this.admin = admin;
        this.app = app;
        this.adminDAO = new AdminDAO();
        this.tagDAO = new TagDAO();
        this.recipeDAO = new RecipeDAO();
        this.recommendationDAO = new RecommendationDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.analyticsDAO = new AnalyticsDAO();
        createView();
    }

    private void createView() {
        view = new BorderPane();
        view.setStyle("-fx-background-color: transparent;");

        if (app.isDarkTheme()) {
            view.getStyleClass().add("dark-theme");
        }

        VBox sidebar = new VBox(5);
        sidebar.getStyleClass().add("admin-sidebar");
        sidebar.setFillWidth(true);

        VBox logoBox = new VBox(3);
        Label logo = new Label("🐟 BAIT & DESIRE");
        logo.getStyleClass().add("admin-sidebar-logo");
        Label subLogo = new Label("ADMIN PANEL");
        subLogo.getStyleClass().add("admin-sidebar-sub");
        logoBox.getChildren().addAll(logo, subLogo);

        Button btnStats = createSidebarButton("📊 Статистика и аналитика");
        Button btnOrders = createSidebarButton("📦 Заказы");
        Button btnProducts = createSidebarButton("🐟 Товары");
        Button btnCategories = createSidebarButton("📁 Категории");
        Button btnUsers = createSidebarButton("👥 Пользователи");
        Button btnRecipes = createSidebarButton("🍳 Рецепты");
        Button btnRecs = createSidebarButton("🍷 Рекомендации");
        Button btnPayments = createSidebarButton("💳 Способы оплаты");
        Button btnTags = createSidebarButton("🏷️ Теги");
        Button btnLogs = createSidebarButton("📝 Журнал действий");
        Button btnBack = createSidebarButton("↩ Вернуться в магазин");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, btnStats, btnOrders, btnProducts, btnCategories, btnUsers,
                btnRecipes, btnRecs, btnPayments, btnTags, btnLogs, spacer, btnBack);

        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        contentArea = new StackPane();
        contentArea.setStyle("-fx-padding: 30; -fx-background-color: transparent;");

        btnStats.setOnAction(e -> contentArea.getChildren().setAll(createStatsTab()));
        btnOrders.setOnAction(e -> contentArea.getChildren().setAll(createOrdersTab()));
        btnProducts.setOnAction(e -> contentArea.getChildren().setAll(createProductsTab()));
        btnCategories.setOnAction(e -> contentArea.getChildren().setAll(createCategoriesTab()));
        btnUsers.setOnAction(e -> contentArea.getChildren().setAll(createUsersTab()));
        btnRecipes.setOnAction(e -> contentArea.getChildren().setAll(createRecipesTab()));
        btnRecs.setOnAction(e -> contentArea.getChildren().setAll(createRecommendationsTab()));
        btnPayments.setOnAction(e -> contentArea.getChildren().setAll(createPaymentMethodsTab()));
        btnTags.setOnAction(e -> contentArea.getChildren().setAll(createTagsTab()));
        btnLogs.setOnAction(e -> contentArea.getChildren().setAll(createLogsTab()));

        btnBack.setOnAction(e -> {
            User u = new com.kurcashi.dao.UserDAO().getUserById(admin.getAdminId());
            if (u == null) {
                u = new User();
                u.setUserId(admin.getAdminId());
                u.setUsername(admin.getUsername());
                u.setEmail(admin.getEmail());
            }
            u.setAdmin(true);
            app.showCatalogView(u);
        });

        contentArea.getChildren().add(createStatsTab());
        view.setLeft(sidebarScroll);
        view.setCenter(contentArea);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        return btn;
    }

    private VBox createStatsTab() {
        VBox container = new VBox(30);

        AdminDAO.AdminStats stats = adminDAO.getStats();
        Label titleStats = new Label("Обзор магазина");
        titleStats.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        HBox statsGrid = new HBox(20);
        statsGrid.getChildren().addAll(
                createStatCard("Пользователи", String.valueOf(stats.getUserCount()), "#e6a3b8"),
                createStatCard("Заказы", String.valueOf(stats.getOrderCount()), "#8db0e6"),
                createStatCard("Товары", String.valueOf(stats.getProductCount()), "#a3e6b0"),
                createStatCard("Выручка", String.format("%.0f ₽", stats.getTotalRevenue()), "#e6cba3")
        );

        Label titleAnalytics = new Label("Топ товаров за последние 30 дней");
        titleAnalytics.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        TableView<SalesAnalytics> analyticsTable = new TableView<>();
        analyticsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        analyticsTable.setPrefHeight(400);

        TableColumn<SalesAnalytics, String> colName = new TableColumn<>("Товар");
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<SalesAnalytics, Integer> colOrders = new TableColumn<>("Кол-во заказов");
        colOrders.setCellValueFactory(new PropertyValueFactory<>("ordersCount"));

        TableColumn<SalesAnalytics, Double> colQty = new TableColumn<>("Продано (кг)");
        colQty.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));

        TableColumn<SalesAnalytics, Double> colRevenue = new TableColumn<>("Выручка (₽)");
        colRevenue.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));

        analyticsTable.getColumns().addAll(colName, colOrders, colQty, colRevenue);

        List<SalesAnalytics> topProducts = analyticsDAO.getTopProducts(LocalDate.now().minusDays(30), LocalDate.now(), 10);
        analyticsTable.getItems().addAll(topProducts);

        container.getChildren().addAll(titleStats, statsGrid, titleAnalytics, analyticsTable);
        return container;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + app.getCurrentBgCard() + "; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        card.setPrefWidth(200);
        Label t = new Label(title); t.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + "; -fx-font-size: 14px;");
        Label v = new Label(value); v.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        card.getChildren().addAll(t, v);
        return card;
    }

    private VBox createOrdersTab() {
        VBox container = new VBox(20);
        Label title = new Label("Управление заказами");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Order, Integer> colId = new TableColumn<>("№ Заказа");
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        TableColumn<Order, Integer> colCustomer = new TableColumn<>("ID Клиента");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<Order, Double> colSum = new TableColumn<>("Сумма");
        colSum.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        TableColumn<Order, String> colStatus = new TableColumn<>("Статус");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Order, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList("PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"));
            private final Button btn = new Button("Обновить");
            {
                btn.getStyleClass().add("table-edit-btn");
                btn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    adminDAO.updateOrderStatus(order.getOrderId(), combo.getValue(), admin.getAdminId());
                    table.getItems().setAll(adminDAO.getAllOrders());
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Order order = getTableView().getItems().get(getIndex());
                    combo.setValue(order.getStatus());
                    setGraphic(new HBox(10, combo, btn));
                }
            }
        });

        table.getColumns().addAll(colId, colCustomer, colSum, colStatus, colAction);
        table.getItems().addAll(adminDAO.getAllOrders());
        container.getChildren().addAll(title, table);
        return container;
    }

    private VBox createProductsTab() {
        VBox container = new VBox(20);
        HBox header = new HBox(20); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Управление товарами"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        Button addBtn = new Button("+ Добавить товар"); addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showProductDialog(null));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, addBtn);

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Product, String> colName = new TableColumn<>("Название"); colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, Double> colPrice = new TableColumn<>("Цена"); colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<Product, Integer> colStock = new TableColumn<>("Остаток"); colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        TableColumn<Product, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button delBtn = new Button("Удал.");
            {
                editBtn.getStyleClass().add("table-edit-btn");
                delBtn.getStyleClass().add("table-del-btn");
                editBtn.setOnAction(e -> showProductDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    if (showConfirm("Удалить " + p.getName() + "?")) {
                        adminDAO.deleteProduct(p.getId(), admin.getAdminId());
                        table.getItems().setAll(adminDAO.getAllProducts());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colName, colPrice, colStock, colAction);
        table.getItems().addAll(adminDAO.getAllProducts());
        container.getChildren().addAll(header, table);
        return container;
    }

    private VBox createCategoriesTab() {
        VBox container = new VBox(20);
        HBox header = new HBox(20); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Категории"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        Button addBtn = new Button("+ Добавить"); addBtn.getStyleClass().add("btn-primary"); addBtn.setOnAction(e -> showCategoryDialog(null));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, addBtn);

        TableView<Category> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Category, String> colName = new TableColumn<>("Название"); colName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        TableColumn<Category, String> colDesc = new TableColumn<>("Описание"); colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Category, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button delBtn = new Button("Удал.");
            {
                editBtn.getStyleClass().add("table-edit-btn");
                delBtn.getStyleClass().add("table-del-btn");
                editBtn.setOnAction(e -> showCategoryDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Category c = getTableView().getItems().get(getIndex());
                    if (showConfirm("Удалить категорию " + c.getCategoryName() + "?")) {
                        adminDAO.deleteCategory(c.getCategoryId(), admin.getAdminId());
                        table.getItems().setAll(adminDAO.getAllCategories());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colName, colDesc, colAction);
        table.getItems().addAll(adminDAO.getAllCategories());
        container.getChildren().addAll(header, table);
        return container;
    }

    private VBox createUsersTab() {
        VBox container = new VBox(20);
        Label title = new Label("Пользователи и роли"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        Map<Integer, String> roleMap = loadAdminRoleMap();

        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, String> colName = new TableColumn<>("Имя"); colName.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<User, String> colEmail = new TableColumn<>("Email"); colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<User, Integer> colBonus = new TableColumn<>("Бонусы"); colBonus.setCellValueFactory(new PropertyValueFactory<>("bonusPoints"));

        TableColumn<User, String> colRole = new TableColumn<>("Роль в приложении");
        colRole.setCellValueFactory(cell -> {
            User u = cell.getValue();
            String role = roleMap.get(u.getUserId());
            return new SimpleStringProperty(getRoleDisplayName(role));
        });

        TableColumn<User, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button roleBtn = new Button("Роль");
            private final Button bonusBtn = new Button("Бонусы");
            {
                roleBtn.getStyleClass().add("table-edit-btn"); roleBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5;");
                bonusBtn.getStyleClass().add("table-edit-btn"); bonusBtn.setStyle("-fx-font-size: 10px; -fx-padding: 5;");
                roleBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    showManageRoleDialog(u);
                });
                bonusBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    showBonusDialog(u);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, bonusBtn, roleBtn));
            }
        });

        table.getColumns().addAll(colName, colEmail, colBonus, colRole, colAction);
        table.getItems().addAll(adminDAO.getAllUsers());
        container.getChildren().addAll(title, table);
        return container;
    }

    private void showManageRoleDialog(User user) {
        Map<Integer, String> currentRoleMap = loadAdminRoleMap();
        String currentRole = currentRoleMap.get(user.getUserId());
        boolean isAdmin = currentRole != null;

        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(450);

        Label header = new Label("Пользователь: " + user.getUsername());
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        Label currentRoleLabel = new Label("Текущая роль: " + getRoleDisplayName(currentRole));
        currentRoleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        Label chooseLabel = new Label("Выберите роль:");
        chooseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + app.getCurrentTextPrimary() + "; -fx-font-weight: bold;");

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList(
                "SUPER_ADMIN", "ADMIN", "MANAGER", "CONTENT_MANAGER"));
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : getRoleDisplayName(item));
            }
        });
        roleCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : getRoleDisplayName(item));
            }
        });
        roleCombo.setValue(isAdmin ? currentRole : "ADMIN");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button(isAdmin ? "Изменить роль" : "Назначить");
        save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            String newRole = roleCombo.getValue();
            if (newRole != null) {
                adminDAO.assignAdminRole(user.getUserId(), newRole, admin.getAdminId());
                contentArea.getChildren().setAll(createUsersTab());
            }
            stage.close();
        });

        btnBox.getChildren().addAll(cancel, save);

        if (isAdmin) {
            Button revokeBtn = new Button("Снять роль");
            revokeBtn.setStyle("-fx-background-color: " + app.getCurrentError() + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            revokeBtn.setOnAction(e -> {
                if (showConfirm("Снять роль администратора с " + user.getUsername() + "?")) {
                    adminDAO.revokeAdminRole(user.getUserId(), admin.getAdminId());
                    contentArea.getChildren().setAll(createUsersTab());
                    stage.close();
                }
            });
            btnBox.getChildren().add(0, revokeBtn);
        }

        box.getChildren().addAll(header, currentRoleLabel, new Separator(), chooseLabel, roleCombo, btnBox);
        finishDialog(stage, box, "Управление ролью");
        stage.show();
    }

    private Map<Integer, String> loadAdminRoleMap() {
        Map<Integer, String> map = new HashMap<>();
        try {
            for (Admin a : adminDAO.getAllAdmins()) {
                map.put(a.getAdminId(), a.getRole());
            }
        } catch (Exception ignored) { }
        return map;
    }

    private String getRoleDisplayName(String role) {
        if (role == null || role.isEmpty()) return "Пользователь";
        switch (role) {
            case "SUPER_ADMIN": return "Главный администратор";
            case "ADMIN": return "Администратор";
            case "MANAGER": return "Менеджер";
            case "CONTENT_MANAGER": return "Контент-менеджер";
            default: return role;
        }
    }

    private VBox createTagsTab() {
        VBox container = new VBox(20);
        HBox header = new HBox(20); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Теги поиска"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        Button addBtn = new Button("+ Добавить"); addBtn.getStyleClass().add("btn-primary"); addBtn.setOnAction(e -> showTagDialog(null));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, addBtn);

        TableView<Tag> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Tag, String> colName = new TableColumn<>("Тег"); colName.setCellValueFactory(new PropertyValueFactory<>("tagName"));

        TableColumn<Tag, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button delBtn = new Button("Удал.");
            {
                editBtn.getStyleClass().add("table-edit-btn");
                delBtn.getStyleClass().add("table-del-btn");
                editBtn.setOnAction(e -> showTagDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Tag t = getTableView().getItems().get(getIndex());
                    if (showConfirm("Удалить тег " + t.getTagName() + "?")) {
                        tagDAO.deleteTag(t.getTagId());
                        contentArea.getChildren().setAll(createTagsTab());
                        app.refreshTags();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colName, colAction);
        table.getItems().addAll(tagDAO.getAllTags());
        container.getChildren().addAll(header, table);
        return container;
    }

    private VBox createRecipesTab() {
        VBox container = new VBox(20);
        HBox header = new HBox(20); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Рецепты"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        Button addBtn = new Button("+ Добавить рецепт"); addBtn.getStyleClass().add("btn-primary"); addBtn.setOnAction(e -> showRecipeAdminDialog(null));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, addBtn);

        TableView<Recipe> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Recipe, String> colTitle = new TableColumn<>("Название"); colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Recipe, Integer> colTime = new TableColumn<>("Время (мин)"); colTime.setCellValueFactory(new PropertyValueFactory<>("cookingTime"));

        TableColumn<Recipe, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button delBtn = new Button("Удал.");
            {
                editBtn.getStyleClass().add("table-edit-btn");
                delBtn.getStyleClass().add("table-del-btn");
                editBtn.setOnAction(e -> showRecipeAdminDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Recipe r = getTableView().getItems().get(getIndex());
                    if (showConfirm("Удалить рецепт " + r.getTitle() + "?")) {
                        recipeDAO.deleteRecipe(r.getRecipeId());
                        contentArea.getChildren().setAll(createRecipesTab());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn));
            }
        });

        List<Recipe> allRecipes = new ArrayList<>();
        for (Product p : adminDAO.getAllProducts()) allRecipes.addAll(recipeDAO.getRecipesByProductId(p.getId()));

        table.getColumns().addAll(colTitle, colTime, colAction);
        table.getItems().addAll(allRecipes);
        container.getChildren().addAll(header, table);
        return container;
    }

    private VBox createRecommendationsTab() {
        VBox container = new VBox(20);
        HBox header = new HBox(20); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Рекомендации к товарам"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        Button addBtn = new Button("+ Добавить"); addBtn.getStyleClass().add("btn-primary"); addBtn.setOnAction(e -> showRecommendationDialog(null));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, addBtn);

        List<Product> allProducts = adminDAO.getAllProducts();
        Map<Integer, String> productNames = new HashMap<>();
        for (Product p : allProducts) productNames.put(p.getId(), p.getName());

        List<Recommendation> allRecs = new ArrayList<>();
        for (Product p : allProducts) {
            allRecs.addAll(recommendationDAO.getByProductId(p.getId()));
        }

        TableView<Recommendation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Recommendation, String> colProduct = new TableColumn<>("Основной товар");
        colProduct.setCellValueFactory(cell -> {
            int pid = cell.getValue().getProductId();
            return new SimpleStringProperty(productNames.getOrDefault(pid, "ID: " + pid));
        });

        TableColumn<Recommendation, String> colRecProduct = new TableColumn<>("Рекомендуемый товар");
        colRecProduct.setCellValueFactory(new PropertyValueFactory<>("recommendedProductName"));

        TableColumn<Recommendation, String> colComment = new TableColumn<>("Комментарий");
        colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));

        TableColumn<Recommendation, Void> colAction = new TableColumn<>("Действие");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Ред.");
            private final Button delBtn = new Button("Удал.");
            {
                editBtn.getStyleClass().add("table-edit-btn");
                delBtn.getStyleClass().add("table-del-btn");
                editBtn.setOnAction(e -> showRecommendationDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Recommendation r = getTableView().getItems().get(getIndex());
                    if (showConfirm("Удалить рекомендацию?")) {
                        recommendationDAO.deleteRecommendation(r.getRecommendationId());
                        contentArea.getChildren().setAll(createRecommendationsTab());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(5, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(colProduct, colRecProduct, colComment, colAction);
        table.getItems().addAll(allRecs);
        container.getChildren().addAll(header, table);
        return container;
    }

    private VBox createPaymentMethodsTab() {
        VBox container = new VBox(20);
        Label title = new Label("Способы оплаты"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        TableView<PaymentMethod> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(table, Priority.ALWAYS);
        TableColumn<PaymentMethod, String> colName = new TableColumn<>("Название"); colName.setCellValueFactory(new PropertyValueFactory<>("methodName"));
        TableColumn<PaymentMethod, String> colCode = new TableColumn<>("Код"); colCode.setCellValueFactory(new PropertyValueFactory<>("methodCode"));

        TableColumn<PaymentMethod, Void> colAction = new TableColumn<>("Статус");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button toggle = new Button();
            {
                toggle.getStyleClass().add("table-edit-btn");
                toggle.setOnAction(e -> {
                    PaymentMethod m = getTableView().getItems().get(getIndex());
                    m.setActive(!m.isActive());
                    paymentMethodDAO.updatePaymentMethod(m);
                    table.getItems().setAll(paymentMethodDAO.getAll());
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    PaymentMethod m = getTableView().getItems().get(getIndex());
                    toggle.setText(m.isActive() ? "Отключить" : "Включить");
                    setGraphic(toggle);
                }
            }
        });

        table.getColumns().addAll(colName, colCode, colAction);
        table.getItems().addAll(paymentMethodDAO.getAll());
        container.getChildren().addAll(title, table);
        return container;
    }

    private VBox createLogsTab() {
        VBox container = new VBox(20);
        Label title = new Label("Журнал действий"); title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        TextArea logArea = new TextArea(); logArea.setEditable(false); VBox.setVgrow(logArea, Priority.ALWAYS);
        for (AdminDAO.AdminLog log : adminDAO.getAdminLogs(50)) {
            logArea.appendText(log.getCreatedAt() + " | " + log.getAdminUsername() + " | " + log.getDetails() + "\n");
        }
        container.getChildren().addAll(title, logArea);
        return container;
    }

    // ==================== ДИАЛОГИ ====================

    private void showProductDialog(Product product) {
        boolean isNew = (product == null);
        Product p = isNew ? new Product() : product;
        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(500);

        TextField nameField = new TextField(p.getName());
        TextField priceField = new TextField(String.valueOf(p.getPrice()));
        TextField stockField = new TextField(String.valueOf(p.getStockQuantity()));
        TextArea descField = new TextArea(p.getDescription());
        descField.setPrefRowCount(3);
        descField.setWrapText(true);

        // ВЫБОР ИЗОБРАЖЕНИЯ
        TextField imageField = new TextField(p.getImageUrl());
        imageField.setPromptText("Имя файла");
        imageField.setEditable(false); // Запрещаем менять вручную, только через кнопку
        imageField.setMaxWidth(Double.MAX_VALUE);

        Button chooseImageBtn = new Button("Выбрать");
        chooseImageBtn.getStyleClass().add("btn-primary");
        chooseImageBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 12;");

        HBox imageBox = new HBox(10, imageField, chooseImageBtn);
        HBox.setHgrow(imageField, Priority.ALWAYS);

        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение товара");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    // Создаем папки, если их нет
                    File destDir = new File("src/main/resources/images/");
                    if (!destDir.exists()) destDir.mkdirs();
                    File targetDir = new File("target/classes/images/");
                    if (!targetDir.exists()) targetDir.mkdirs();

                    // Генерируем уникальное имя файла
                    String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                    String uniqueName = "product_" + System.currentTimeMillis() + ext;

                    // Копируем в исходники и в target (чтобы появилось сразу без пересборки)
                    Files.copy(selectedFile.toPath(), new File(destDir, uniqueName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(selectedFile.toPath(), new File(targetDir, uniqueName).toPath(), StandardCopyOption.REPLACE_EXISTING);

                    imageField.setText(uniqueName);
                } catch (IOException ex) {
                    CustomAlert.show(app.getPrimaryStage(), "Ошибка", "Не удалось сохранить изображение: " + ex.getMessage(), CustomAlert.AlertType.ERROR);
                }
            }
        });

        // ВЫБОР КАТЕГОРИИ
        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.getItems().addAll(adminDAO.getAllCategories());
        categoryCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Category c) { return c == null ? "" : c.getCategoryName(); }
            @Override public Category fromString(String s) { return null; }
        });
        if (!isNew && p.getCategoryId() > 0) {
            categoryCombo.getItems().stream()
                    .filter(c -> c.getCategoryId() == p.getCategoryId())
                    .findFirst().ifPresent(categoryCombo::setValue);
        }

        nameField.setMaxWidth(Double.MAX_VALUE);
        priceField.setMaxWidth(Double.MAX_VALUE);
        stockField.setMaxWidth(Double.MAX_VALUE);
        descField.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить");
        save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            try {
                p.setName(nameField.getText().trim());
                p.setPrice(Double.parseDouble(priceField.getText()));
                p.setStockQuantity(Integer.parseInt(stockField.getText()));
                p.setDescription(descField.getText().trim());
                p.setImageUrl(imageField.getText().trim());

                if (categoryCombo.getValue() != null) {
                    p.setCategoryId(categoryCombo.getValue().getCategoryId());
                } else {
                    p.setCategoryId(0);
                }

                if (isNew) {
                    adminDAO.addProduct(p, admin.getAdminId());
                } else {
                    adminDAO.updateProduct(p, admin.getAdminId());
                }

                stage.close();
                contentArea.getChildren().setAll(createProductsTab());
                // Обновляем каталог в главном окне, чтобы новые товары сразу появились
                app.updateProductsGrid();

            } catch (NumberFormatException ex) {
                CustomAlert.show(app.getPrimaryStage(), "Ошибка", "Проверьте правильность ввода цены и остатка", CustomAlert.AlertType.ERROR);
            }
        });
        btnBox.getChildren().addAll(cancel, save);

        VBox form = new VBox(10,
                new Label("Название:"), nameField,
                new Label("Цена (₽/кг):"), priceField,
                new Label("Остаток (кг):"), stockField,
                new Label("Категория:"), categoryCombo,
                new Label("Описание:"), descField,
                new Label("Изображение:"), imageBox
        );

        box.getChildren().addAll(form, btnBox);
        finishDialog(stage, box, isNew ? "Добавление товара" : "Редактирование товара");
        stage.show();
    }

    private void showCategoryDialog(Category category) {
        boolean isNew = (category == null);
        Category cat = isNew ? new Category(0, "", "") : category;

        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(400);

        TextField nameField = new TextField(cat.getCategoryName());
        TextArea descField = new TextArea(cat.getDescription());
        descField.setPrefRowCount(3);
        nameField.setMaxWidth(Double.MAX_VALUE);
        descField.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            cat.setCategoryName(nameField.getText());
            cat.setDescription(descField.getText());
            if (isNew) adminDAO.addCategory(cat, admin.getAdminId()); else adminDAO.updateCategory(cat, admin.getAdminId());
            stage.close(); contentArea.getChildren().setAll(createCategoriesTab());
        });
        btnBox.getChildren().addAll(cancel, save);

        box.getChildren().addAll(new Label("Название:"), nameField, new Label("Описание:"), descField, btnBox);
        finishDialog(stage, box, isNew ? "Добавление категории" : "Редактирование категории");
        stage.show();
    }

    private void showTagDialog(Tag tag) {
        boolean isNew = (tag == null);
        Tag t = isNew ? new Tag("", 0) : tag;

        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(300);

        TextField nameField = new TextField(t.getTagName());
        TextField orderField = new TextField(String.valueOf(t.getDisplayOrder()));
        nameField.setMaxWidth(Double.MAX_VALUE);
        orderField.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            t.setTagName(nameField.getText());
            t.setDisplayOrder(Integer.parseInt(orderField.getText()));
            if (isNew) tagDAO.addTag(t); else tagDAO.updateTag(t);
            stage.close(); contentArea.getChildren().setAll(createTagsTab());
            app.refreshTags();
        });
        btnBox.getChildren().addAll(cancel, save);

        box.getChildren().addAll(new Label("Название тега:"), nameField, new Label("Порядок:"), orderField, btnBox);
        finishDialog(stage, box, isNew ? "Добавление тега" : "Редактирование тега");
        stage.show();
    }

    private void showBonusDialog(User user) {
        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(300);

        TextField bonusField = new TextField(String.valueOf(user.getBonusPoints()));
        bonusField.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            try {
                adminDAO.updateUserBonusPoints(user.getUserId(), Integer.parseInt(bonusField.getText()), admin.getAdminId());
                contentArea.getChildren().setAll(createUsersTab());
                stage.close();
            } catch (Exception ignored) {}
        });
        btnBox.getChildren().addAll(cancel, save);

        box.getChildren().addAll(new Label("Изменить бонусы для " + user.getUsername() + ":"), bonusField, btnBox);
        finishDialog(stage, box, "Бонусы пользователя");
        stage.show();
    }

    private void showRecommendationDialog(Recommendation existing) {
        boolean isNew = (existing == null);
        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(450);

        List<Product> allProducts = adminDAO.getAllProducts();

        ComboBox<Product> prodCombo = new ComboBox<>();
        prodCombo.getItems().addAll(allProducts);
        prodCombo.setMaxWidth(Double.MAX_VALUE);
        prodCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName(); }
            @Override public Product fromString(String s) { return null; }
        });

        ComboBox<Product> recCombo = new ComboBox<>();
        recCombo.getItems().addAll(allProducts);
        recCombo.setMaxWidth(Double.MAX_VALUE);
        recCombo.setConverter(prodCombo.getConverter());

        TextField comment = new TextField();
        comment.setMaxWidth(Double.MAX_VALUE);

        if (!isNew) {
            for (Product p : allProducts) {
                if (p.getId() == existing.getProductId()) prodCombo.setValue(p);
                if (p.getId() == existing.getRecommendedProductId()) recCombo.setValue(p);
            }
            comment.setText(existing.getComment());
        }

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            if (prodCombo.getValue() != null && recCombo.getValue() != null) {
                if (!isNew) {
                    recommendationDAO.deleteRecommendation(existing.getRecommendationId());
                }
                recommendationDAO.addRecommendation(prodCombo.getValue().getId(), recCombo.getValue().getId(), comment.getText());
                stage.close(); contentArea.getChildren().setAll(createRecommendationsTab());
            }
        });
        btnBox.getChildren().addAll(cancel, save);

        box.getChildren().addAll(
                new Label("Основной товар:"), prodCombo,
                new Label("Рекомендуем:"), recCombo,
                new Label("Комментарий:"), comment,
                btnBox);
        finishDialog(stage, box, isNew ? "Новая рекомендация" : "Редактирование рекомендации");
        stage.show();
    }

    private void showRecipeAdminDialog(Recipe existing) {
        boolean isNew = existing == null;
        Recipe recipe = isNew ? new Recipe() : existing;
        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(600);

        List<Product> allProducts = adminDAO.getAllProducts();

        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.getItems().addAll(allProducts);
        productCombo.setMaxWidth(Double.MAX_VALUE);
        productCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName(); }
            @Override public Product fromString(String s) { return null; }
        });

        if (!isNew) {
            for (Product p : allProducts) {
                if (p.getId() == recipe.getProductId()) {
                    productCombo.setValue(p);
                    break;
                }
            }
        }

        TextField titleField = new TextField(recipe.getTitle());
        titleField.setMaxWidth(Double.MAX_VALUE);
        TextArea descField = new TextArea(recipe.getDescription());
        descField.setPrefRowCount(2);
        descField.setMaxWidth(Double.MAX_VALUE);
        TextArea stepsField = new TextArea(recipe.getSteps());
        stepsField.setPrefRowCount(3);
        stepsField.setMaxWidth(Double.MAX_VALUE);
        TextField timeField = new TextField(String.valueOf(recipe.getCookingTime()));
        timeField.setMaxWidth(Double.MAX_VALUE);

        List<RecipeIngredient> tempIngredients = new ArrayList<>(recipe.getIngredients());
        VBox ingBox = new VBox(5);
        Runnable[] renderIngRef = new Runnable[1];
        Runnable renderIng = () -> {
            ingBox.getChildren().clear();
            for (int i = 0; i < tempIngredients.size(); i++) {
                final int idx = i;
                RecipeIngredient ri = tempIngredients.get(i);
                HBox h = new HBox(10);
                h.setAlignment(Pos.CENTER_LEFT);
                Product p = allProducts.stream().filter(x -> x.getId() == ri.getProductId()).findFirst().orElse(null);
                Button del = new Button("X"); del.getStyleClass().add("table-del-btn");
                del.setOnAction(e -> { tempIngredients.remove(idx); renderIngRef[0].run(); });
                h.getChildren().addAll(new Label(p != null ? p.getName() : "?"), new Label(ri.getQuantity() + " кг"), del);
                ingBox.getChildren().add(h);
            }
        };
        renderIngRef[0] = renderIng;
        renderIng.run();

        ComboBox<Product> ingProduct = new ComboBox<>();
        ingProduct.getItems().addAll(allProducts);
        ingProduct.setConverter(productCombo.getConverter());
        HBox.setHgrow(ingProduct, Priority.ALWAYS);

        TextField ingQty = new TextField("1"); ingQty.setPrefWidth(60);
        Button addIng = new Button("+"); addIng.getStyleClass().add("btn-primary"); addIng.setOnAction(e -> {
            if (ingProduct.getValue() != null) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setProductId(ingProduct.getValue().getId());
                ri.setQuantity(Double.parseDouble(ingQty.getText()));
                tempIngredients.add(ri);
                renderIngRef[0].run();
            }
        });

        VBox form = new VBox(10,
                new Label("Товар:"), productCombo,
                new Label("Название:"), titleField,
                new Label("Описание:"), descField,
                new Label("Шаги:"), stepsField,
                new Label("Время (мин):"), timeField,
                new Label("Ингредиенты:"), ingBox, new HBox(10, ingProduct, ingQty, addIng)
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Сохранить"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            recipe.setProductId(productCombo.getValue() != null ? productCombo.getValue().getId() : 0);
            recipe.setTitle(titleField.getText());
            recipe.setDescription(descField.getText());
            recipe.setSteps(stepsField.getText());
            recipe.setCookingTime(Integer.parseInt(timeField.getText()));
            recipe.setIngredients(tempIngredients);
            if (isNew) recipeDAO.addRecipe(recipe, admin.getAdminId()); else recipeDAO.updateRecipe(recipe);
            stage.close(); contentArea.getChildren().setAll(createRecipesTab());
        });
        btnBox.getChildren().addAll(cancel, save);

        box.getChildren().addAll(scroll, btnBox);
        finishDialog(stage, box, isNew ? "Новый рецепт" : "Редактирование рецепта");
        stage.show();
    }

    // ==================== КАСТОМНЫЕ ОКНА И ШАПКИ ====================

    private Stage createDialogStage() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(app.getPrimaryStage());
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }

    private VBox createBaseDialogBox(double width) {
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(20, 25, 20, 25));
        contentBox.setPrefWidth(width);
        return contentBox;
    }

    private HBox createDialogTitleBar(Stage stage, String titleText) {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 10, 10, 20));
        titleBar.setStyle("-fx-background-color: " + app.getCurrentBgCard() +
                "; -fx-background-radius: 10 10 0 0; -fx-border-color: transparent transparent " +
                app.getCurrentBorderColor() + " transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        HBox.setHgrow(title, Priority.ALWAYS);

        final double[] xOffset = {0};
        final double[] yOffset = {0};
        titleBar.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });

        titleBar.getChildren().add(title);
        return titleBar;
    }

    private void finishDialog(Stage stage, VBox contentBox, String title) {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + app.getCurrentBgCard() +
                "; -fx-background-radius: 10; -fx-border-color: " + app.getCurrentBorderColor() +
                "; -fx-border-radius: 10; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        if (app.isDarkTheme()) {
            root.getStyleClass().add("dark-theme");
        }

        HBox titleBar = createDialogTitleBar(stage, title);
        root.getChildren().addAll(titleBar, contentBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        URL css = getClass().getResource("/styles/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private boolean showConfirm(String msg) {
        Stage stage = createDialogStage();
        VBox box = createBaseDialogBox(400);
        Label lbl = new Label(msg);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(15, 0, 0, 0));

        Button no = new Button("Отмена");
        no.getStyleClass().add("btn-secondary");
        no.setOnAction(e -> stage.close());

        Button yes = new Button("Да");
        yes.getStyleClass().add("btn-primary");

        final boolean[] result = {false};
        yes.setOnAction(e -> { result[0] = true; stage.close(); });

        btnBox.getChildren().addAll(no, yes);
        box.getChildren().addAll(lbl, btnBox);

        finishDialog(stage, box, "Подтверждение");
        stage.showAndWait();
        return result[0];
    }

    public BorderPane getView() { return view; }
}