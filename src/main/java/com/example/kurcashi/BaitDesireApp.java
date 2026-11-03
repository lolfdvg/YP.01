package com.example.kurcashi;

import javafx.application.Application;
import com.kurcashi.utils.CustomAlert;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.kurcashi.views.*;
import com.kurcashi.views.admin.AdminDashboardView;
import com.kurcashi.models.*;
import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.dao.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaitDesireApp extends Application {

    private static final String DARK_THEME_PREF_KEY = "dark_theme";

    private Stage primaryStage;
    private StackPane rootPane;
    private BorderPane mainLayout;
    private LoginView loginView;
    private RegisterView registerView;
    private boolean isFullScreen = false;

    private AddressDAO addressDAO;
    private CartDAO cartDAO;
    private PaymentDAO paymentDAO;
    private CategoryDAO categoryDAO;
    private ContactDAO contactDAO;
    private RecentlyViewedDAO recentlyViewedDAO;
    private TagDAO tagDAO;
    private User currentUser;

    private FilteredList<Product> filteredProducts;
    private FlowPane productsGrid;
    private TextField searchField;
    private CategorySidebar categorySidebar;

    private ObservableList<Product> recentlyViewedProducts;
    private VBox recentlyViewedSection;

    private List<Category> cachedCategories;
    private List<Contact> cachedContacts;
    private List<Tag> searchTags;

    private Label cartCountLabel;
    private boolean isDarkTheme = false;
    private String guestId;

    public static final String BG_LIGHT = "#fff5f9";
    public static final String BG_CARD = "#ffffff";
    public static final String BG_DARKER = "#ffe6f0";
    public static final String TEXT_PRIMARY = "#5a3e4a";
    public static final String TEXT_SECONDARY = "#b45f7a";
    public static final String ACCENT = "#e6a3b8";
    public static final String ACCENT_DARK = "#d48fb0";
    public static final String SUCCESS = "#7d9a7a";
    public static final String ERROR = "#c97b7b";
    public static final String BORDER_COLOR = "#ffcce0";

    public static final String BG_LIGHT_DARK = "#121212";
    public static final String BG_CARD_DARK = "#1e1e1e";
    public static final String BG_DARKER_DARK = "#2a2a2a";
    public static final String TEXT_PRIMARY_DARK = "#ffffff";
    public static final String TEXT_SECONDARY_DARK = "#aaaaaa";
    public static final String ACCENT_DARK_THEME = "#ff69b4";
    public static final String ACCENT_DARKER = "#ff1493";
    public static final String SUCCESS_DARK = "#ff69b4";
    public static final String ERROR_DARK = "#ff4444";
    public static final String BORDER_COLOR_DARK = "#444444";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BAIT & DESIRE");

        initDAOs();
        initGuestSession();
        loadCachedData();

        recentlyViewedProducts = FXCollections.observableArrayList();

        rootPane = new StackPane();
        mainLayout = new BorderPane();

        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
        isDarkTheme = prefs.getBoolean(DARK_THEME_PREF_KEY, false);

        if (isDarkTheme) mainLayout.getStyleClass().add("dark-theme");

        MenuBar menuBar = createGuestMenuBar();
        mainLayout.setTop(menuBar);
        mainLayout.setCenter(rootPane);

        showGuestCatalogView();

        Scene scene = new Scene(mainLayout, 1280, 800);
        try {
            URL css = getClass().getResource("/styles/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }

        scene.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.F11) toggleFullScreen(); });
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        primaryStage.setOnCloseRequest(e -> prefs.putBoolean(DARK_THEME_PREF_KEY, isDarkTheme));
        primaryStage.show();
    }

    private void initDAOs() {
        addressDAO = new AddressDAO();
        cartDAO = new CartDAO();
        paymentDAO = new PaymentDAO();
        categoryDAO = new CategoryDAO();
        contactDAO = new ContactDAO();
        recentlyViewedDAO = new RecentlyViewedDAO();
        tagDAO = new TagDAO();
    }

    private void loadCachedData() {
        cachedCategories = categoryDAO.getAllCategories();
        cachedContacts = contactDAO.getAllActiveContacts();
        searchTags = tagDAO.getAllActiveTags();
    }

    private void initGuestSession() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
        guestId = prefs.get("guest_id", UUID.randomUUID().toString());
        prefs.put("guest_id", guestId);
    }

    public boolean isUserLoggedIn() { return currentUser != null && currentUser.getUserId() > 0; }
    public String getGuestId() { return guestId; }

    private void setupCatalogComponents() {
        List<Product> products = loadProductsFromDatabase();
        ObservableList<Product> allProducts = FXCollections.observableArrayList(products);
        filteredProducts = new FilteredList<>(allProducts, p -> true);

        productsGrid = new FlowPane();
        productsGrid.setPadding(new Insets(30));
        productsGrid.setHgap(25);
        productsGrid.setVgap(25);
        productsGrid.setAlignment(Pos.TOP_LEFT);
        productsGrid.setStyle("-fx-background-color: transparent;");

        categorySidebar = new CategorySidebar(cachedCategories, category -> {
            filteredProducts.setPredicate(category == null ? p -> true : p -> p.getCategoryId() == category.getCategoryId());
            updateProductsGrid();
        }, this);
    }

    private VBox createCatalogContent(VBox header) {
        HBox contentContainer = new HBox(20);
        contentContainer.setPadding(new Insets(20));
        contentContainer.getChildren().add(categorySidebar.getView());

        VBox productsContainer = new VBox(10);
        if (recentlyViewedSection != null) productsContainer.getChildren().add(recentlyViewedSection);
        productsContainer.getChildren().add(productsGrid);

        ScrollPane scrollPane = new ScrollPane(productsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        contentContainer.getChildren().add(scrollPane);

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(header, contentContainer);
        return new VBox(mainContent);
    }

    public void showGuestCatalogView() {
        BorderPane catalogLayout = new BorderPane();
        catalogLayout.setStyle("-fx-background-color: " + getCurrentBgLight() + ";");
        setupCatalogComponents();
        VBox header = createHeader(null, null);
        recentlyViewedSection = new VBox();
        recentlyViewedSection.setVisible(false);
        loadGuestRecentlyViewed();
        updateProductsGrid();
        catalogLayout.setCenter(createCatalogContent(header));
        rootPane.getChildren().clear();
        rootPane.getChildren().add(catalogLayout);
    }

    public void showCatalogView(User user) {
        this.currentUser = user;
        loadRecentlyViewedFromDatabase();
        Address savedAddress = addressDAO.getDefaultAddress(user.getUserId());
        showCatalogWithAddress(user, savedAddress);
    }

    public void showCatalogView(User user, Address address) {
        showCatalogWithAddress(user, address);
    }

    public void showCatalogWithAddress(User user, Address address) {
        if (user == null || user.getUserId() <= 0) { showLoginView(); return; }
        this.currentUser = user;
        BorderPane catalogLayout = new BorderPane();
        catalogLayout.setStyle("-fx-background-color: " + getCurrentBgLight() + ";");
        setupCatalogComponents();
        VBox header = createHeader(user, address);
        recentlyViewedSection = createRecentlyViewedSection();
        updateProductsGrid();
        catalogLayout.setCenter(createCatalogContent(header));
        rootPane.getChildren().clear();
        rootPane.getChildren().add(catalogLayout);
    }

    private VBox createHeader(User user, Address address) {
        VBox headerBox = new VBox(10);
        headerBox.getStyleClass().add("header-bar");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label appTitleLabel = new Label("🐟 BAIT & DESIRE");
        appTitleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + getCurrentAccentDark() + ";");

        searchField = new TextField();
        searchField.setPromptText("Поиск рыбы и морепродуктов...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, newVal) -> filterProducts(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cartButton = new Button("Корзина");
        cartButton.getStyleClass().add("btn-cart");

        cartCountLabel = new Label("0");
        cartCountLabel.getStyleClass().add("cart-badge");
        cartCountLabel.setVisible(false);

        StackPane cartStack = new StackPane(cartButton, cartCountLabel);
        StackPane.setAlignment(cartCountLabel, Pos.TOP_RIGHT);
        cartCountLabel.setTranslateX(-5);
        cartCountLabel.setTranslateY(-5);
        cartButton.setOnAction(e -> openCart(user, address));

        topRow.getChildren().addAll(appTitleLabel, searchField, spacer);

        if (user != null) {
            if (user.isAdmin()) {
                Button adminBtn = new Button("Aдмин-панель");
                adminBtn.getStyleClass().add("btn-primary");
                adminBtn.setOnAction(e -> {
                    Admin adminUser = new Admin();
                    adminUser.setAdminId(user.getUserId());
                    adminUser.setUsername(user.getUsername());
                    adminUser.setEmail(user.getEmail());
                    adminUser.setAdmin(true);
                    adminUser.setRole(user.getAdminRole() != null ? user.getAdminRole() : "ADMIN");
                    showAdminDashboard(adminUser);
                });
                topRow.getChildren().add(adminBtn);
            }
            Button profileBtn = new Button("Профиль");
            profileBtn.getStyleClass().add("btn-primary");
            profileBtn.setOnAction(e -> showProfileView());

            Button logoutBtn = new Button("Выйти");
            logoutBtn.getStyleClass().add("btn-secondary");
            logoutBtn.setOnAction(e -> { currentUser = null; showGuestCatalogView(); });

            topRow.getChildren().addAll(profileBtn, logoutBtn);
        } else {
            Button loginBtn = new Button("Войти");
            loginBtn.getStyleClass().add("btn-primary");
            loginBtn.setOnAction(e -> showLoginView());
            topRow.getChildren().add(loginBtn);
        }

        topRow.getChildren().add(cartStack);

        HBox tagsBox = createTagsPanel();
        headerBox.getChildren().addAll(topRow, tagsBox);
        updateCartCount();
        return headerBox;
    }

    private HBox createTagsPanel() {
        HBox tagsBox = new HBox(10);
        tagsBox.setAlignment(Pos.CENTER_LEFT);
        tagsBox.setPadding(new Insets(5, 0, 5, 0));

        for (Tag tag : searchTags) {
            Button tagButton = new Button(tag.getTagName());
            tagButton.getStyleClass().add("tag-btn");
            tagButton.setOnAction(e -> searchField.setText(tag.getTagName()));
            tagsBox.getChildren().add(tagButton);
        }
        return tagsBox;
    }

    private void openCart(User user, Address address) {
        PaymentInfo paymentInfo = (user != null) ? paymentDAO.getPaymentInfo(user.getUserId()) : null;
        CartView cartView = new CartView(user, address, paymentInfo, this::updateCartCount, this);
        cartView.show(primaryStage);
    }

    public void updateProductsGrid() {
        if (productsGrid == null) return;
        productsGrid.getChildren().clear();
        for (Product product : filteredProducts) {
            productsGrid.getChildren().add(createProductCard(product));
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredProducts.setPredicate(p -> true);
        } else {
            String lower = searchText.toLowerCase().trim();
            filteredProducts.setPredicate(product -> product.getName().toLowerCase().contains(lower));
        }
        updateProductsGrid();
    }

    private List<Product> loadProductsFromDatabase() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.*, c.category_name FROM products p LEFT JOIN categories c ON p.category_id = c.category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(new Product(rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getDouble("price_per_kg"), rs.getDouble("rating"), rs.getString("description"),
                        rs.getInt("quantity_in_stock"), rs.getString("image_url"), rs.getInt("category_id"),
                        rs.getString("category_name"), rs.getDouble("rating")));
            }
        } catch (SQLException e) { }
        return products;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefSize(220, 160);
        ImageView imageView = new ImageView(loadProductImage(product.getImageUrl()));
        imageView.setFitWidth(220);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageContainer.getChildren().add(imageView);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);
        nameLabel.setMinHeight(40);
        nameLabel.setAlignment(Pos.CENTER);

        // ДОБАВЛЕНО ОТОБРАЖЕНИЕ КАТЕГОРИИ
        Label categoryLabel = new Label(product.getCategoryName() != null && !product.getCategoryName().isEmpty()
                ? product.getCategoryName() : "Без категории");
        categoryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getCurrentTextSecondary() + ";");

        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER);
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #f4b942; -fx-font-size: 14px;");
        Label ratingVal = new Label(String.format("%.1f", product.getRating()));
        ratingVal.setStyle("-fx-font-size: 13px; -fx-text-fill: " + getCurrentTextSecondary() + ";");
        ratingBox.getChildren().addAll(star, ratingVal);

        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.getStyleClass().add("product-price");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("В корзину");
        addButton.getStyleClass().add("btn-primary");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> {
            addToCart(product, 1.0);
            CustomAlert.show(primaryStage, "Добавлено", "Товар в корзине", CustomAlert.AlertType.INFO);
        });

        card.setOnMouseClicked(e -> {
            addToRecentlyViewed(product);
            ProductDetailDialog dialog = new ProductDetailDialog(product, currentUser, this::updateCartCount, primaryStage, this);
            dialog.show();
        });

        // CategoryLabel добавлен в card
        card.getChildren().addAll(imageContainer, nameLabel, categoryLabel, ratingBox, priceLabel, spacer, addButton);
        return card;
    }

    private Image loadProductImage(String imageUrl) {
        if (imageUrl != null) {
            try {
                InputStream stream = getClass().getResourceAsStream("/images/" + imageUrl);
                if (stream != null) return new Image(stream, 230, 150, false, true);
            } catch (Exception ignored) {}
        }
        return new Image("https://via.placeholder.com/230x150?text=No+Image");
    }

    private void loadRecentlyViewedFromDatabase() {
        if (currentUser != null && currentUser.getUserId() > 0) {
            List<Product> products = recentlyViewedDAO.getRecentlyViewed(currentUser.getUserId(), 8);
            Platform.runLater(() -> {
                recentlyViewedProducts.clear();
                recentlyViewedProducts.addAll(products);
                refreshRecentlyViewedSection();
            });
        }
    }

    private void loadGuestRecentlyViewed() {
        if (recentlyViewedProducts == null) recentlyViewedProducts = FXCollections.observableArrayList();
        recentlyViewedProducts.clear();
        String sql = "SELECT p.*, c.category_name FROM guest_recently_viewed grv JOIN products p ON grv.product_id = p.product_id LEFT JOIN categories c ON p.category_id = c.category_id WHERE grv.guest_id = ? ORDER BY grv.viewed_at DESC LIMIT 8";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guestId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                recentlyViewedProducts.add(new Product(rs.getInt("product_id"), rs.getString("product_name"), rs.getDouble("price_per_kg"), rs.getDouble("rating"), rs.getString("description"), rs.getInt("quantity_in_stock"), rs.getString("image_url"), rs.getInt("category_id"), rs.getString("category_name"), 0.0));
            }
            refreshRecentlyViewedSection();
        } catch (SQLException e) { }
    }

    private void addToRecentlyViewed(Product product) {
        Platform.runLater(() -> {
            if (isUserLoggedIn()) {
                recentlyViewedDAO.addRecentlyViewed(currentUser.getUserId(), product.getId());
                loadRecentlyViewedFromDatabase();
            } else {
                String sql = "INSERT INTO guest_recently_viewed (guest_id, product_id) VALUES (?, ?) ON CONFLICT (guest_id, product_id) DO UPDATE SET viewed_at = CURRENT_TIMESTAMP";
                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, guestId); pstmt.setInt(2, product.getId()); pstmt.executeUpdate();
                } catch (SQLException e) { }
                loadGuestRecentlyViewed();
            }
        });
    }

    private void refreshRecentlyViewedSection() {
        if (recentlyViewedSection == null) return;
        recentlyViewedSection.getChildren().clear();
        if (recentlyViewedProducts.isEmpty()) { recentlyViewedSection.setVisible(false); return; }

        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(10, 0, 5, 0));

        Label title = new Label("🕐 Недавно просмотренные");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getCurrentTextPrimary() + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleBox.getChildren().addAll(title, spacer);

        if (isUserLoggedIn()) {
            Button clearButton = new Button("Очистить историю");
            clearButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + getCurrentError() + "; -fx-font-size: 13px; -fx-cursor: hand; -fx-underline: true;");
            clearButton.setOnAction(e -> clearRecentlyViewed());
            titleBox.getChildren().add(clearButton);
        }

        FlowPane list = new FlowPane(15, 15);
        for (Product p : recentlyViewedProducts) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: " + getCurrentBgCard() + "; -fx-background-radius: 8; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-cursor: hand;");
            card.setPrefWidth(150);
            Label n = new Label(p.getName()); n.setWrapText(true); n.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getCurrentTextPrimary() + ";");
            Label pr = new Label(p.getFormattedPrice()); pr.setStyle("-fx-text-fill: " + getCurrentAccentDark() + ";");
            card.getChildren().addAll(n, pr);
            card.setOnMouseClicked(e -> {
                ProductDetailDialog dialog = new ProductDetailDialog(p, currentUser, this::updateCartCount, primaryStage, this);
                dialog.show();
            });
            list.getChildren().add(card);
        }
        recentlyViewedSection.getChildren().addAll(titleBox, list);
        recentlyViewedSection.setVisible(true);
    }

    private void clearRecentlyViewed() {
        if (isUserLoggedIn()) {
            recentlyViewedDAO.clearRecentlyViewed(currentUser.getUserId());
            recentlyViewedProducts.clear();
            refreshRecentlyViewedSection();
            CustomAlert.show(primaryStage, "Очищено", "История просмотров очищена", CustomAlert.AlertType.INFO);
        }
    }

    private VBox createRecentlyViewedSection() {
        recentlyViewedSection = new VBox(10);
        recentlyViewedSection.setPadding(new Insets(0, 30, 10, 30));
        refreshRecentlyViewedSection();
        return recentlyViewedSection;
    }

    private void addToCart(Product product, double kg) {
        addProductToCart(product, kg);
    }

    public void addProductToCart(Product product, double kg) {
        int quantityUnits = (int) Math.ceil(kg);
        if (isUserLoggedIn()) {
            cartDAO.addToCart(currentUser.getUserId(), product.getId(), quantityUnits);
        } else {
            String sql = "INSERT INTO guest_cart_items (guest_id, product_id, quantity) VALUES (?, ?, ?) ON CONFLICT (guest_id, product_id) DO UPDATE SET quantity = guest_cart_items.quantity + EXCLUDED.quantity";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, guestId); pstmt.setInt(2, product.getId()); pstmt.setInt(3, quantityUnits); pstmt.executeUpdate();
            } catch (SQLException e) {}
        }
        updateCartCount();
    }

    public void updateCartCount() {
        if (cartCountLabel == null) return;
        int count = 0;
        if (isUserLoggedIn()) {
            count = cartDAO.getCartItemCount(currentUser.getUserId());
        } else {
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT SUM(quantity) FROM guest_cart_items WHERE guest_id = ?")) {
                pstmt.setString(1, guestId); ResultSet rs = pstmt.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            } catch (SQLException e) {}
        }
        if (count > 0) { cartCountLabel.setText(String.valueOf(count)); cartCountLabel.setVisible(true); }
        else { cartCountLabel.setVisible(false); }
    }

    public void updateCartCountPublic() { updateCartCount(); }
    public void showLoginView() { loginView = new LoginView(this); rootPane.getChildren().clear(); rootPane.getChildren().add(loginView.getView()); }
    public void showRegisterView() { registerView = new RegisterView(this); rootPane.getChildren().clear(); rootPane.getChildren().add(registerView.getView()); }
    private void showProfileView() {
        ProfileView profileView = new ProfileView(currentUser, paymentDAO, () -> showCatalogView(currentUser), primaryStage, this);
        rootPane.getChildren().clear(); rootPane.getChildren().add(profileView.getView());
    }
    public void showAdminDashboard(Admin admin) {
        AdminDashboardView dashboard = new AdminDashboardView(admin, this);
        rootPane.getChildren().clear(); rootPane.getChildren().add(dashboard.getView());
    }

    private void toggleFullScreen() { isFullScreen = !isFullScreen; primaryStage.setFullScreen(isFullScreen); }

    public String getCurrentBgLight() { return isDarkTheme ? BG_LIGHT_DARK : BG_LIGHT; }
    public String getCurrentBgCard() { return isDarkTheme ? BG_CARD_DARK : BG_CARD; }
    public String getCurrentBgDarker() { return isDarkTheme ? BG_DARKER_DARK : BG_DARKER; }
    public String getCurrentTextPrimary() { return isDarkTheme ? TEXT_PRIMARY_DARK : TEXT_PRIMARY; }
    public String getCurrentTextSecondary() { return isDarkTheme ? TEXT_SECONDARY_DARK : TEXT_SECONDARY; }
    public String getCurrentAccent() { return isDarkTheme ? ACCENT_DARK_THEME : ACCENT; }
    public String getCurrentAccentDark() { return isDarkTheme ? ACCENT_DARKER : ACCENT_DARK; }
    public String getCurrentSuccess() { return isDarkTheme ? SUCCESS_DARK : SUCCESS; }
    public String getCurrentError() { return isDarkTheme ? ERROR_DARK : ERROR; }
    public String getCurrentBorderColor() { return isDarkTheme ? BORDER_COLOR_DARK : BORDER_COLOR; }
    public boolean isDarkTheme() { return isDarkTheme; }
    public Stage getPrimaryStage() { return primaryStage; }

    public void updateButtonStyle(Button b) { b.getStyleClass().add("btn-primary"); }
    public void updateTextFieldStyle(TextField t) {}
    public void updateTextAreaStyle(TextArea t) {}
    public void updateDisabledButtonStyle(Button b) { b.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #999999;"); }
    public void showCustomLoginPrompt(String message) { CustomAlert.show(primaryStage, "Авторизация", message, CustomAlert.AlertType.INFO); showLoginView(); }
    public void migrateGuestData(String guestId, int newUserId) {}
    public void refreshTags() { searchTags = tagDAO.getAllActiveTags(); }

    private MenuBar createGuestMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu viewMenu = new Menu("Вид");
        MenuItem fullScreen = new MenuItem("Полный экран (F11)");
        fullScreen.setOnAction(e -> toggleFullScreen());
        MenuItem themeToggle = new MenuItem(isDarkTheme ? "Светлая тема" : "Темная тема");
        themeToggle.setOnAction(e -> toggleTheme(themeToggle));
        viewMenu.getItems().addAll(fullScreen, new SeparatorMenuItem(), themeToggle);

        Menu help = new Menu("Помощь");
        MenuItem about = new MenuItem("О программе");
        about.setOnAction(e -> CustomAlert.show(primaryStage, "О программе", "BAIT & DESIRE v3.0", CustomAlert.AlertType.INFO));
        help.getItems().add(about);

        menuBar.getMenus().addAll(viewMenu, help);
        return menuBar;
    }

    private void toggleTheme(MenuItem item) {
        isDarkTheme = !isDarkTheme;
        item.setText(isDarkTheme ? "Светлая тема" : "Темная тема");

        if (isDarkTheme) {
            if (!mainLayout.getStyleClass().contains("dark-theme")) mainLayout.getStyleClass().add("dark-theme");
        } else {
            mainLayout.getStyleClass().remove("dark-theme");
        }

        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
        prefs.putBoolean(DARK_THEME_PREF_KEY, isDarkTheme);
        if (isUserLoggedIn()) showCatalogView(currentUser); else showGuestCatalogView();
    }

    public static void main(String[] args) { launch(args); }
}