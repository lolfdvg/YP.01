package com.example.kurcashi;

import javafx.application.Application;
import com.kurcashi.utils.CustomAlert;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import com.kurcashi.views.LoginView;
import com.kurcashi.views.RegisterView;
import com.kurcashi.views.CartView;
import com.kurcashi.views.ProductDetailDialog;
import com.kurcashi.views.ProfileView;
import com.kurcashi.views.CategorySidebar;
import com.kurcashi.views.ContactFooter;
import com.kurcashi.views.admin.AdminDashboardView;
import com.kurcashi.models.User;
import com.kurcashi.models.Admin;
import com.kurcashi.models.Address;
import com.kurcashi.models.Product;
import com.kurcashi.models.Category;
import com.kurcashi.models.Contact;
import com.kurcashi.models.Tag;
import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.dao.AddressDAO;
import com.kurcashi.dao.CartDAO;
import com.kurcashi.dao.PaymentDAO;
import com.kurcashi.dao.CategoryDAO;
import com.kurcashi.dao.ContactDAO;
import com.kurcashi.dao.RecentlyViewedDAO;
import com.kurcashi.dao.TagDAO;
import com.kurcashi.models.PaymentInfo;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaitDesireApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(BaitDesireApp.class.getName());

    private static final String STYLE_BG_TRANSPARENT = "-fx-background-color: transparent;";
    private static final String STYLE_BG_CARD_PREFIX = "-fx-background-color: ";
    private static final String STYLE_TEXT_FILL_PREFIX = "-fx-text-fill: ";
    private static final String STYLE_FONT_SIZE_14 = "; -fx-font-size: 14px;";
    private static final String STYLE_FONT_SIZE_12 = "; -fx-font-size: 12px;";
    private static final String STYLE_FONT_SIZE_20_BOLD = "; -fx-font-size: 20px; -fx-font-weight: bold;";
    private static final String STYLE_BORDER_COLOR_PREFIX = "; -fx-border-color: ";
    private static final String DARK_THEME_PREF_KEY = "dark_theme";
    private static final String STYLE_MENU_ITEM = "menu-item";
    private static final String LABEL_REGISTRATION = "Регистрация";

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
    private GridPane productsGrid;
    private TextField searchField;
    private CategorySidebar categorySidebar;
    private ContactFooter contactFooter;

    private ObservableList<Product> recentlyViewedProducts;
    private VBox recentlyViewedSection;

    private List<Category> cachedCategories;
    private List<Contact> cachedContacts;
    private List<Tag> searchTags;

    private Label cartCountLabel;
    private boolean isDarkTheme = true;
    private String guestId;
    private static final String GUEST_ID_KEY = "guest_id";

    // Цвета для светлой темы
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

    // Цвета для тёмной темы
    public static final String BG_LIGHT_DARK = "#0a0a0a";
    public static final String BG_CARD_DARK = "#1a1a1a";
    public static final String BG_DARKER_DARK = "#2a2a2a";
    public static final String TEXT_PRIMARY_DARK = "#ffffff";
    public static final String TEXT_SECONDARY_DARK = "#aaaaaa";
    public static final String ACCENT_DARK_THEME = "#ff69b4";
    public static final String ACCENT_DARKER = "#ff1493";
    public static final String SUCCESS_DARK = "#ff69b4";
    public static final String ERROR_DARK = "#ff4444";
    public static final String BORDER_COLOR_DARK = "#444444";

    private static final String RESOURCES_IMAGES_PATH = "/images/";
    private static final String PROJECT_IMAGES_PATH = "src/main/resources/images/";
    private static final String TARGET_IMAGES_PATH = "target/classes/images/";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BAIT & DESIRE - Магазин рыбы");

        initDAOs();
        initGuestSession();
        loadCachedData();

        recentlyViewedProducts = FXCollections.observableArrayList();

        rootPane = new StackPane();
        mainLayout = new BorderPane();

        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
        isDarkTheme = prefs.getBoolean(DARK_THEME_PREF_KEY, true);

        MenuBar menuBar = createGuestMenuBar();
        mainLayout.setTop(menuBar);
        mainLayout.setCenter(rootPane);

        loginView = new LoginView(this);
        registerView = new RegisterView(this);

        showGuestCatalogView();

        Scene scene = createMainScene();
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        primaryStage.setOnCloseRequest(e -> {
            java.util.prefs.Preferences prefs2 = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
            prefs2.putBoolean(DARK_THEME_PREF_KEY, isDarkTheme);
        });

        ensureImagesDirectoryExists();
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
        guestId = prefs.get(GUEST_ID_KEY, null);
        if (guestId == null || guestId.isEmpty()) {
            guestId = UUID.randomUUID().toString();
            prefs.put(GUEST_ID_KEY, guestId);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO guest_carts (guest_id) VALUES (?) ON CONFLICT DO NOTHING")) {
                pstmt.setString(1, guestId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to create guest cart", e);
            }
        } else {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO guest_carts (guest_id) VALUES (?) ON CONFLICT DO NOTHING")) {
                pstmt.setString(1, guestId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to ensure guest cart exists", e);
            }
        }
    }

    private Scene createMainScene() {
        Scene scene = new Scene(mainLayout, 1280, 800);
        try {
            URL menuCssUrl = getClass().getResource("/css/menu.css");
            if (menuCssUrl != null) {
                scene.getStylesheets().add(menuCssUrl.toExternalForm());
                LOGGER.info("menu.css loaded successfully");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load menu.css", e);
        }
        try {
            URL styleCssUrl = getClass().getResource("/css/style.css");
            if (styleCssUrl != null) {
                scene.getStylesheets().add(styleCssUrl.toExternalForm());
                LOGGER.info("style.css loaded successfully");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load style.css", e);
        }
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F11) toggleFullScreen();
        });
        return scene;
    }

    // ==================== ГОСТЕВАЯ СЕССИЯ ====================
    public int getEffectiveUserId() {
        if (currentUser != null && currentUser.getUserId() > 0) {
            return currentUser.getUserId();
        }
        return -Math.abs(guestId.hashCode());
    }

    public boolean isUserLoggedIn() {
        return currentUser != null && currentUser.getUserId() > 0;
    }

    public void showCustomLoginPrompt(String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setTitle("");

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setPadding(new Insets(25));
        dialogBox.setPrefWidth(400);
        dialogBox.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + "; -fx-background-radius: 15; " +
                "-fx-border-color: " + getCurrentAccent() + "; -fx-border-radius: 15; -fx-border-width: 2;");

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20.0);
        dropShadow.setOffsetX(5.0);
        dropShadow.setOffsetY(5.0);
        dropShadow.setColor(Color.color(0, 0, 0, isDarkTheme ? 0.8 : 0.4));
        dialogBox.setEffect(dropShadow);

        Label titleLabel = new Label("Требуется авторизация");
        titleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Войти");
        updateButtonStyle(loginButton);
        loginButton.setPrefWidth(100);
        loginButton.setOnAction(e -> {
            dialogStage.close();
            showLoginView();
        });

        Button registerButton = new Button("Регистрация");
        updateButtonStyle(registerButton);
        registerButton.setPrefWidth(100);
        registerButton.setOnAction(e -> {
            dialogStage.close();
            showRegisterView();
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14 + "; -fx-cursor: hand;");
        cancelButton.setOnMouseEntered(ev -> cancelButton.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + getCurrentError() + STYLE_FONT_SIZE_14 + "; -fx-cursor: hand;"));
        cancelButton.setOnMouseExited(ev -> cancelButton.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14 + "; -fx-cursor: hand;"));
        cancelButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(loginButton, registerButton, cancelButton);
        dialogBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(null);
        dialogStage.setScene(scene);
        dialogStage.setOnShown(e -> {
            if (primaryStage != null) {
                dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - dialogBox.getPrefWidth()) / 2);
                dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 250) / 2);
            } else {
                dialogStage.centerOnScreen();
            }
        });
        dialogStage.showAndWait();
    }

    public void migrateGuestData(String guestId, int newUserId) {
        // 1. Создаём корзину для пользователя, если её нет
        String ensureCartSql = "INSERT INTO carts (user_id, created_at, updated_at) " +
                "SELECT ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                "WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ensureCartSql)) {
            pstmt.setInt(1, newUserId);
            pstmt.setInt(2, newUserId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to ensure user cart", e);
            return;
        }

        // 2. Переносим товары из guest_cart_items в cart_items
        String migrateCartSql = "INSERT INTO cart_items (cart_id, product_id, quantity, added_at, updated_at) " +
                "SELECT (SELECT cart_id FROM carts WHERE user_id = ?), product_id, quantity, added_at, CURRENT_TIMESTAMP " +
                "FROM guest_cart_items WHERE guest_id = ? " +
                "ON CONFLICT (cart_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(migrateCartSql)) {
            pstmt.setInt(1, newUserId);
            pstmt.setString(2, guestId);
            int rows = pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Migrated {0} cart items", rows);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to migrate cart items", e);
        }

        // 3. Переносим историю просмотров
        String migrateRecentlySql = "INSERT INTO recently_viewed (user_id, product_id, viewed_at) " +
                "SELECT ?, product_id, viewed_at FROM guest_recently_viewed WHERE guest_id = ? " +
                "ON CONFLICT (user_id, product_id) DO UPDATE SET viewed_at = EXCLUDED.viewed_at";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(migrateRecentlySql)) {
            pstmt.setInt(1, newUserId);
            pstmt.setString(2, guestId);
            int rows = pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Migrated {0} recently viewed items", rows);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to migrate recently viewed", e);
        }
    }

    public String getGuestId() { return guestId; }

    // ==================== ОБЩИЕ НАСТРОЙКИ ====================
    private void setupCatalogComponents() {
        if (cachedCategories == null) cachedCategories = categoryDAO.getAllCategories();
        if (cachedContacts == null) cachedContacts = contactDAO.getAllActiveContacts();
        if (searchTags == null) searchTags = tagDAO.getAllActiveTags();

        List<Product> products = loadProductsFromDatabase();
        ObservableList<Product> allProducts = FXCollections.observableArrayList(products);
        filteredProducts = new FilteredList<>(allProducts, p -> true);

        productsGrid = new GridPane();
        productsGrid.setPadding(new Insets(20));
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.TOP_CENTER);
        productsGrid.setStyle(STYLE_BG_TRANSPARENT);

        categorySidebar = new CategorySidebar(cachedCategories, category -> {
            filteredProducts.setPredicate(category == null ? p -> true : p -> p.getCategoryId() == category.getCategoryId());
            updateProductsGrid();
        }, this);

        contactFooter = new ContactFooter(cachedContacts, this);
    }

    private VBox createCatalogContent(VBox header) {
        HBox contentContainer = new HBox(20);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setAlignment(Pos.TOP_LEFT);
        contentContainer.setStyle(STYLE_BG_TRANSPARENT);
        contentContainer.getChildren().add(categorySidebar.getView());

        VBox productsContainer = new VBox(10);
        productsContainer.setAlignment(Pos.TOP_CENTER);
        productsContainer.setStyle(STYLE_BG_TRANSPARENT);

        if (recentlyViewedSection != null) productsContainer.getChildren().add(recentlyViewedSection);
        productsContainer.getChildren().add(productsGrid);
        contentContainer.getChildren().add(productsContainer);
        HBox.setHgrow(productsContainer, Priority.ALWAYS);

        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(header, contentContainer);
        mainContent.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + ";");

        VBox fullContent = new VBox();
        fullContent.getChildren().addAll(mainContent, contactFooter.getView());
        fullContent.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + ";");

        ScrollPane scrollPane = new ScrollPane(fullContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + "; -fx-background: " + getCurrentBgLight() + ";");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return new VBox(scrollPane);
    }

    // ==================== ГОСТЕВОЙ РЕЖИМ ====================
    public void showGuestCatalogView() {
        BorderPane catalogLayout = new BorderPane();
        catalogLayout.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + ";");

        setupCatalogComponents();

        VBox header = createGuestHeader();
        recentlyViewedSection = new VBox();
        recentlyViewedSection.setVisible(false);

        loadGuestRecentlyViewed();

        updateProductsGrid();

        VBox catalogContent = createCatalogContent(header);
        catalogLayout.setCenter(catalogContent);
        rootPane.getChildren().clear();
        rootPane.getChildren().add(catalogLayout);

        updateMainMenuBarStyle();
        updateCartCount();
    }

    private VBox createGuestHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20, 30, 20, 30));
        headerBox.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + STYLE_BORDER_COLOR_PREFIX + getCurrentBorderColor() + "; -fx-border-width: 0 0 1 0;");
        headerBox.setMaxWidth(Double.MAX_VALUE);

        Label appTitleLabel = new Label("BAIT & DESIRE");
        appTitleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 36px; -fx-font-weight: bold;");
        appTitleLabel.setAlignment(Pos.CENTER);
        HBox titleBox = new HBox(appTitleLabel);
        titleBox.setAlignment(Pos.CENTER);

        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_RIGHT);
        headerTop.setSpacing(15);

        Button loginButton = new Button("Войти");
        updateButtonStyle(loginButton);
        loginButton.setOnAction(e -> showLoginView());

        Button registerButton = new Button(LABEL_REGISTRATION);
        updateButtonStyle(registerButton);
        registerButton.setOnAction(e -> showRegisterView());

        Button cartButton = new Button("Корзина");
        updateButtonStyle(cartButton);
        cartCountLabel = new Label();
        cartCountLabel.setStyle("-fx-background-color: " + getCurrentAccent() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 2 5;" +
                "-fx-background-radius: 10;" +
                "-fx-min-width: 18; -fx-min-height: 18;" +
                "-fx-alignment: center;");
        cartCountLabel.setVisible(false);
        StackPane cartStack = new StackPane(cartButton, cartCountLabel);
        StackPane.setAlignment(cartCountLabel, Pos.TOP_RIGHT);
        cartCountLabel.setTranslateX(-5);
        cartCountLabel.setTranslateY(-5);
        cartButton.setOnAction(e -> {
            CartView cartView = new CartView(null, null, null, this::updateCartCount, this);
            cartView.show(primaryStage);
        });

        headerTop.getChildren().addAll(loginButton, registerButton, cartStack);

        HBox searchBox = createSearchBoxCommon();
        HBox tagsBox = createTagsPanel();

        Label welcomeLabel = new Label("Добро пожаловать в BAIT & DESIRE!");
        welcomeLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label guestInfoLabel = new Label("Вы просматриваете каталог как гость. Для покупок войдите или зарегистрируйтесь.");
        guestInfoLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        guestInfoLabel.setWrapText(true);

        Label catalogTitle = new Label("Наша свежая рыба");
        catalogTitle.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        catalogTitle.setPadding(new Insets(10, 0, 0, 0));

        headerBox.getChildren().addAll(titleBox, headerTop, searchBox, tagsBox, welcomeLabel, guestInfoLabel, catalogTitle);
        return headerBox;
    }

    private void loadGuestRecentlyViewed() {
        if (recentlyViewedProducts == null) recentlyViewedProducts = FXCollections.observableArrayList();
        recentlyViewedProducts.clear();
        String sql = "SELECT p.product_id, p.product_name, p.price_per_kg, p.rating, p.description, " +
                "p.quantity_in_stock, p.image_url, p.category_id, c.category_name " +
                "FROM guest_recently_viewed grv " +
                "JOIN products p ON grv.product_id = p.product_id " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE grv.guest_id = ? " +
                "ORDER BY grv.viewed_at DESC LIMIT 8";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guestId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price_per_kg"),
                        rs.getDouble("rating"),
                        rs.getString("description"),
                        rs.getInt("quantity_in_stock"),
                        rs.getString("image_url"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        0.0
                );
                recentlyViewedProducts.add(product);
            }
            refreshRecentlyViewedSection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load guest recently viewed", e);
        }
    }

    // ==================== АВТОРИЗОВАННЫЙ ПОЛЬЗОВАТЕЛЬ ====================
    public void showCatalogView(User user, Address address) {
        showCatalogWithAddress(user, address);
    }

    public void showCatalogView(User user) {
        this.currentUser = user;
        loadRecentlyViewedFromDatabase();
        Address savedAddress = addressDAO.getDefaultAddress(user.getUserId());
        if (savedAddress == null) {
            showAddressDialog(user);
        } else {
            showCatalogWithAddress(user, savedAddress);
        }
    }

    public void showCatalogWithAddress(User user, Address address) {
        if (user == null || user.getUserId() <= 0) {
            showLoginView();
            return;
        }

        this.currentUser = user;

        BorderPane catalogLayout = new BorderPane();
        catalogLayout.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + ";");

        setupCatalogComponents();

        VBox header = createHeader(user, address);
        recentlyViewedSection = createRecentlyViewedSection();
        updateProductsGrid();

        VBox catalogContent = createCatalogContent(header);
        catalogLayout.setCenter(catalogContent);
        rootPane.getChildren().clear();
        rootPane.getChildren().add(catalogLayout);

        updateMainMenuBarStyle();
    }

    private VBox createHeader(User user, Address address) {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20, 30, 20, 30));
        headerBox.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + STYLE_BORDER_COLOR_PREFIX + getCurrentBorderColor() + "; -fx-border-width: 0 0 1 0;");
        headerBox.setMaxWidth(Double.MAX_VALUE);

        Label appTitleLabel = new Label("BAIT & DESIRE");
        appTitleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 36px; -fx-font-weight: bold;");
        appTitleLabel.setAlignment(Pos.CENTER);
        HBox titleBox = new HBox(appTitleLabel);
        titleBox.setAlignment(Pos.CENTER);

        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_RIGHT);
        headerTop.setSpacing(15);
        Button profileButton = new Button("Профиль");
        updateButtonStyle(profileButton);
        profileButton.setOnAction(e -> showProfileView());

        Button cartButton = new Button("Корзина");
        updateButtonStyle(cartButton);

        cartCountLabel = new Label();
        cartCountLabel.setStyle("-fx-background-color: " + getCurrentAccent() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 2 5;" +
                "-fx-background-radius: 10;" +
                "-fx-min-width: 18; -fx-min-height: 18;" +
                "-fx-alignment: center;");
        cartCountLabel.setVisible(false);

        StackPane cartStack = new StackPane(cartButton, cartCountLabel);
        StackPane.setAlignment(cartCountLabel, Pos.TOP_RIGHT);
        cartCountLabel.setTranslateX(-5);
        cartCountLabel.setTranslateY(-5);

        cartButton.setOnAction(e -> {
            PaymentInfo paymentInfo = paymentDAO.getPaymentInfo(user.getUserId());
            CartView cartView = new CartView(user, address, paymentInfo, this::updateCartCount, this);
            cartView.show(primaryStage);
        });
        updateCartCount();

        Button logoutButton = new Button("Выйти");
        updateButtonStyle(logoutButton);
        logoutButton.setOnAction(e -> {
            currentUser = null;
            showLoginView();
        });

        if (user.isAdmin()) {
            Button adminButton = new Button("Админ панель");
            updateButtonStyle(adminButton);
            adminButton.setOnAction(e -> {
                Admin adminObj = new Admin();
                adminObj.setAdminId(user.getUserId());
                adminObj.setUsername(user.getUsername());
                adminObj.setEmail(user.getEmail());
                adminObj.setFullName(user.getUsername());
                adminObj.setRole("ADMIN");
                adminObj.setActive(true);
                adminObj.setAdmin(true);
                showAdminDashboard(adminObj);
            });
            headerTop.getChildren().addAll(profileButton, cartStack, adminButton, logoutButton);
        } else {
            headerTop.getChildren().addAll(profileButton, cartStack, logoutButton);
        }

        HBox searchBox = createSearchBoxCommon();
        HBox tagsBox = createTagsPanel();

        Label welcomeLabel = new Label("Добро пожаловать, " + user.getUsername() + "!");
        welcomeLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label addressLabel = new Label("Адрес доставки: " + (address != null ? address.getFullAddress() : "Не указан"));
        addressLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        addressLabel.setWrapText(true);

        PaymentInfo paymentInfo = paymentDAO.getPaymentInfo(user.getUserId());
        Label cardLabel;
        if (paymentInfo != null && paymentInfo.getCardNumber() != null && !paymentInfo.getCardNumber().isEmpty()) {
            String last4 = paymentInfo.getCardNumber().substring(paymentInfo.getCardNumber().length() - 4);
            cardLabel = new Label("Карта: ****" + last4);
            cardLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        } else {
            cardLabel = new Label("Данные карты не добавлены");
            cardLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentError() + STYLE_FONT_SIZE_12);
        }

        Label catalogTitle = new Label("Наша свежая рыба");
        catalogTitle.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        catalogTitle.setPadding(new Insets(10, 0, 0, 0));

        headerBox.getChildren().addAll(titleBox, headerTop, searchBox, tagsBox, welcomeLabel, addressLabel, cardLabel, catalogTitle);
        return headerBox;
    }

    private HBox createSearchBoxCommon() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(10, 0, 10, 0));

        searchField = new TextField();
        searchField.setPromptText("Поиск по названию, категории или ингредиентам...");
        updateTextFieldStyle(searchField);
        searchField.setPrefWidth(450);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProducts(newValue));
        searchContainer.getChildren().add(searchField);
        return searchContainer;
    }

    // ==================== ПАНЕЛЬ ТЕГОВ ====================
    private HBox createTagsPanel() {
        HBox tagsBox = new HBox(10);
        tagsBox.setAlignment(Pos.CENTER_LEFT);
        tagsBox.setPadding(new Insets(5, 0, 15, 0));
        tagsBox.setStyle(STYLE_BG_TRANSPARENT);

        for (Tag tag : searchTags) {
            Button tagButton = new Button(tag.getTagName());
            String normalStyle = STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + ";" +
                    STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + ";" +
                    "-fx-font-size: 11px; -fx-padding: 5 12;" +
                    "-fx-background-radius: 20; -fx-border-radius: 20;" +
                    STYLE_BORDER_COLOR_PREFIX + getCurrentBorderColor() + ";" +
                    "-fx-border-width: 1; -fx-cursor: hand;";
            String hoverStyle = STYLE_BG_CARD_PREFIX + getCurrentAccent() + ";" +
                    STYLE_TEXT_FILL_PREFIX + "white; -fx-font-size: 11px; -fx-padding: 5 12;" +
                    "-fx-background-radius: 20; -fx-border-radius: 20;" +
                    STYLE_BORDER_COLOR_PREFIX + getCurrentAccent() + ";" +
                    "-fx-border-width: 1; -fx-cursor: hand;";
            tagButton.setStyle(normalStyle);
            tagButton.setMinHeight(32);
            tagButton.setMaxHeight(32);
            HBox.setHgrow(tagButton, Priority.NEVER);
            tagButton.setOnMouseEntered(e -> tagButton.setStyle(hoverStyle));
            tagButton.setOnMouseExited(e -> tagButton.setStyle(normalStyle));
            tagButton.setOnAction(e -> searchField.setText(tag.getTagName()));
            tagsBox.getChildren().add(tagButton);
        }
        return tagsBox;
    }

    // ==================== ОСНОВНЫЕ МЕТОДЫ ====================
    private void updateProductsGrid() {
        if (productsGrid == null) return;
        productsGrid.getChildren().clear();

        int maxCols = 4;
        int col = 0;
        int row = 0;
        for (Product product : filteredProducts) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, col, row);
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }

        if (filteredProducts.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(100));
            Label noProductsLabel = new Label("Ничего не найдено\nПопробуйте изменить запрос поиска");
            noProductsLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentError() + STYLE_FONT_SIZE_20_BOLD);
            noProductsLabel.setAlignment(Pos.CENTER);
            productsGrid.add(emptyBox, 0, 0);
            GridPane.setColumnSpan(emptyBox, maxCols);
        }
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredProducts.setPredicate(p -> true);
        } else {
            String lowerCaseSearch = searchText.toLowerCase().trim();
            filteredProducts.setPredicate(product ->
                    product.getName().toLowerCase().contains(lowerCaseSearch) ||
                            (product.getCategoryName() != null && product.getCategoryName().toLowerCase().contains(lowerCaseSearch)) ||
                            (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerCaseSearch))
            );
        }
        updateProductsGrid();
    }

    private List<Product> loadProductsFromDatabase() {
        List<Product> products = new ArrayList<>();
        String query = """
            SELECT p.product_id, p.product_name, p.price_per_kg, p.rating,
                   p.description, p.quantity_in_stock, p.image_url, p.category_id,
                   c.category_name,
                   COALESCE(avg_r.avg_rating, 0) as avg_rating
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            LEFT JOIN (
                SELECT product_id, AVG(rating) as avg_rating
                FROM reviews
                GROUP BY product_id
            ) avg_r ON p.product_id = avg_r.product_id
            ORDER BY p.product_id
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price_per_kg"),
                        rs.getDouble("rating"),
                        rs.getString("description"),
                        rs.getInt("quantity_in_stock"),
                        rs.getString("image_url"),
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getDouble("avg_rating")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading products", e);
        }
        return products;
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-border-color: " + getCurrentBorderColor() + "; -fx-border-radius: 15; -fx-border-width: 1;");
        card.setMinWidth(280);
        card.setCursor(javafx.scene.Cursor.HAND);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), card);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        card.setOnMouseEntered(e -> {
            scaleUp.playFromStart();
            card.setEffect(new DropShadow(20, Color.color(0, 0, 0, 0.3)));
            card.toFront();
        });
        card.setOnMouseExited(e -> {
            scaleDown.playFromStart();
            card.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.1)));
        });

        final User currentUserForCard = this.currentUser;
        final boolean isGuest = (currentUserForCard == null || currentUserForCard.getUserId() <= 0);

        card.setOnMouseClicked(e -> {
            addToRecentlyViewed(product);
            ProductDetailDialog detailDialog = new ProductDetailDialog(product, currentUserForCard, () -> {
                if (!isGuest) { updateCartCount(); updateProductsGrid(); }
            }, primaryStage, this);
            detailDialog.show();
        });

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(240);
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + "; -fx-background-radius: 10; -fx-border-color: " + getCurrentAccent() + "; -fx-border-radius: 10;");

        ImageView imageView = new ImageView(loadProductImage(product.getImageUrl()));
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageContainer.getChildren().add(imageView);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER);
        int fullStars = (int) Math.round(product.getAverageRating());
        for (int i = 0; i < fullStars; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-text-fill: #ff69b4; -fx-font-size: 14px;");
            ratingBox.getChildren().add(star);
        }
        for (int i = fullStars; i < 5; i++) {
            Label star = new Label("☆");
            star.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
            ratingBox.getChildren().add(star);
        }
        Label ratingLabel = new Label(String.format(" (%.1f)", product.getAverageRating()));
        ratingLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        ratingBox.getChildren().add(ratingLabel);

        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentAccent() + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label stockLabel = new Label(product.getStockQuantity() > 0 ? "В наличии: " + product.getStockQuantity() + " кг" : "Нет в наличии");
        stockLabel.setStyle(STYLE_TEXT_FILL_PREFIX + (product.getStockQuantity() > 0 ? getCurrentSuccess() : getCurrentError()) + STYLE_FONT_SIZE_12);

        Button addButton = new Button("В корзину");
        addButton.setPrefWidth(200);

        if (product.getStockQuantity() <= 0) {
            addButton.setDisable(true);
            addButton.setText("Нет в наличии");
            addButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #999999; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8;");
        } else {
            updateButtonStyle(addButton);
            addButton.setOnAction(e -> {
                addToCart(product, 1.0);
                CustomAlert.show(primaryStage, "Добавлено", "Товар добавлен в корзину", CustomAlert.AlertType.INFO);
            });
        }

        card.getChildren().addAll(imageContainer, nameLabel, ratingBox, priceLabel, stockLabel, addButton);
        if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
            Label catLabel = new Label(product.getCategoryName());
            catLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + "; -fx-font-size: 10px;");
            card.getChildren().add(1, catLabel);
        }
        return card;
    }

    private void addToCart(Product product, double kg) {
        int quantityUnits = (int) Math.ceil(kg);
        if (isUserLoggedIn()) {
            cartDAO.addToCart(currentUser.getUserId(), product.getId(), quantityUnits);
        } else {
            // Гость: проверка существования и обновление/вставка без ON CONFLICT
            String checkSql = "SELECT quantity FROM guest_cart_items WHERE guest_id = ? AND product_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, guestId);
                checkStmt.setInt(2, product.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int existingQty = rs.getInt("quantity");
                    String updateSql = "UPDATE guest_cart_items SET quantity = ? WHERE guest_id = ? AND product_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, existingQty + quantityUnits);
                        updateStmt.setString(2, guestId);
                        updateStmt.setInt(3, product.getId());
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO guest_cart_items (guest_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, guestId);
                        insertStmt.setInt(2, product.getId());
                        insertStmt.setInt(3, quantityUnits);
                        insertStmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to add to guest cart", e);
            }
        }
        updateCartCount();
    }

    // ==================== НЕДАВНО ПРОСМОТРЕННЫЕ ====================
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

    private void addToRecentlyViewed(Product product) {
        if (product == null) return;
        Platform.runLater(() -> {
            if (isUserLoggedIn()) {
                recentlyViewedDAO.addRecentlyViewed(currentUser.getUserId(), product.getId());
                List<Product> products = recentlyViewedDAO.getRecentlyViewed(currentUser.getUserId(), 8);
                recentlyViewedProducts.clear();
                recentlyViewedProducts.addAll(products);
            } else {
                String sql = "INSERT INTO guest_recently_viewed (guest_id, product_id) VALUES (?, ?) " +
                        "ON CONFLICT (guest_id, product_id) DO UPDATE SET viewed_at = CURRENT_TIMESTAMP";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, guestId);
                    pstmt.setInt(2, product.getId());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to add guest recently viewed", e);
                }
                recentlyViewedProducts.removeIf(p -> p.getId() == product.getId());
                recentlyViewedProducts.add(0, product);
                if (recentlyViewedProducts.size() > 8) {
                    recentlyViewedProducts.remove(8, recentlyViewedProducts.size());
                }
            }
            refreshRecentlyViewedSection();
        });
    }

    private void refreshRecentlyViewedSection() {
        if (recentlyViewedSection == null) return;
        recentlyViewedSection.getChildren().clear();

        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        Label sectionTitle = new Label("🕐 Недавно просмотренные");
        sectionTitle.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + STYLE_FONT_SIZE_20_BOLD);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isUserLoggedIn() && !recentlyViewedProducts.isEmpty()) {
            Button clearButton = new Button("Очистить историю");
            clearButton.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + getCurrentError() + "; -fx-font-size: 12px; -fx-cursor: hand; -fx-underline: true;");
            clearButton.setOnAction(e -> clearRecentlyViewed());
            titleBox.getChildren().add(clearButton);
        }
        titleBox.getChildren().add(0, sectionTitle);
        titleBox.getChildren().add(1, spacer);

        if (recentlyViewedProducts.isEmpty()) {
            Label emptyLabel = new Label("Нет недавно просмотренных товаров");
            emptyLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
            emptyLabel.setPadding(new Insets(20));
            emptyLabel.setAlignment(Pos.CENTER);
            VBox emptyBox = new VBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            recentlyViewedSection.getChildren().addAll(titleBox, emptyBox);
            return;
        }

        HBox recentlyViewedContainer = new HBox(15);
        recentlyViewedContainer.setAlignment(Pos.CENTER_LEFT);
        recentlyViewedContainer.setPadding(new Insets(10, 0, 10, 0));

        for (Product product : recentlyViewedProducts) {
            VBox smallCard = createSmallProductCard(product);
            recentlyViewedContainer.getChildren().add(smallCard);
        }

        ScrollPane scrollPane = new ScrollPane(recentlyViewedContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT + " -fx-background: transparent;");
        scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        recentlyViewedSection.getChildren().addAll(titleBox, scrollPane);
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

    private VBox createSmallProductCard(Product product) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setPrefWidth(150);
        card.setMinWidth(150);
        card.setMaxWidth(150);
        card.setPrefHeight(160);
        card.setMinHeight(160);
        card.setMaxHeight(160);
        card.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: " + getCurrentBorderColor() + "; -fx-border-radius: 10; -fx-border-width: 1;");
        card.setCursor(javafx.scene.Cursor.HAND);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), card);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        card.setOnMouseEntered(e -> {
            scaleUp.playFromStart();
            card.setEffect(new DropShadow(15, Color.color(0, 0, 0, 0.25)));
        });
        card.setOnMouseExited(e -> {
            scaleDown.playFromStart();
            card.setEffect(new DropShadow(5, Color.color(0, 0, 0, 0.1)));
        });
        card.setOnMouseClicked(e -> {
            addToRecentlyViewed(product);
            ProductDetailDialog detailDialog = new ProductDetailDialog(product, currentUser, () -> {
                updateCartCount();
                updateProductsGrid();
            }, primaryStage, this);
            detailDialog.show();
        });

        Button removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 2 5; -fx-background-radius: 10; -fx-font-weight: bold;");
        if (isUserLoggedIn()) {
            removeButton.setOnAction(e -> {
                recentlyViewedDAO.removeFromRecentlyViewed(currentUser.getUserId(), product.getId());
                recentlyViewedProducts.remove(product);
                refreshRecentlyViewedSection();
                e.consume();
            });
        } else {
            removeButton.setOnAction(e -> {
                String sql = "DELETE FROM guest_recently_viewed WHERE guest_id = ? AND product_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, guestId);
                    pstmt.setInt(2, product.getId());
                    pstmt.executeUpdate();
                    recentlyViewedProducts.remove(product);
                    refreshRecentlyViewedSection();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to remove guest recently viewed", ex);
                }
                e.consume();
            });
        }

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(130);
        imageContainer.setPrefHeight(90);
        imageContainer.setMinWidth(130);
        imageContainer.setMinHeight(90);
        imageContainer.setMaxWidth(130);
        imageContainer.setMaxHeight(90);
        imageContainer.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + "; -fx-background-radius: 8;");

        ImageView imageView = new ImageView(loadProductImage(product.getImageUrl()));
        imageView.setFitWidth(130);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageContainer.getChildren().add(imageView);

        StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
        removeButton.setTranslateX(5);
        removeButton.setTranslateY(-5);
        imageContainer.getChildren().add(removeButton);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(130);
        nameLabel.setPrefHeight(40);

        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentAccent() + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel);
        return card;
    }

    private VBox createRecentlyViewedSection() {
        recentlyViewedSection = new VBox(10);
        recentlyViewedSection.setPadding(new Insets(10, 20, 20, 20));
        recentlyViewedSection.setStyle(STYLE_BG_TRANSPARENT);
        recentlyViewedSection.setVisible(true);
        refreshRecentlyViewedSection();
        return recentlyViewedSection;
    }

    // ==================== ОСТАЛЬНЫЕ МЕТОДЫ ====================
    private void updateCartCount() {
        if (cartCountLabel == null) return;
        if (isUserLoggedIn()) {
            int count = cartDAO.getCartItemCount(currentUser.getUserId());
            Platform.runLater(() -> {
                if (count > 0) {
                    cartCountLabel.setText(String.valueOf(count));
                    cartCountLabel.setVisible(true);
                } else {
                    cartCountLabel.setVisible(false);
                }
            });
        } else {
            String sql = "SELECT SUM(quantity) FROM guest_cart_items WHERE guest_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, guestId);
                ResultSet rs = pstmt.executeQuery();
                int count = rs.next() ? rs.getInt(1) : 0;
                Platform.runLater(() -> {
                    if (count > 0) {
                        cartCountLabel.setText(String.valueOf(count));
                        cartCountLabel.setVisible(true);
                    } else {
                        cartCountLabel.setVisible(false);
                    }
                });
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to get guest cart count", e);
            }
        }
    }

    private void showAddressDialog(User user) {
        com.kurcashi.views.AddressDialog addressDialog = new com.kurcashi.views.AddressDialog(user, primaryStage, this);
        Address newAddress = addressDialog.showAndWait();
        if (newAddress != null) {
            showCatalogWithAddress(user, newAddress);
        } else {
            showCatalogWithAddress(user, addressDAO.getDefaultAddress(user.getUserId()));
        }
    }

    private void showProfileView() {
        if (!isUserLoggedIn()) {
            showCustomLoginPrompt("Для просмотра профиля необходимо войти.");
            return;
        }
        ProfileView profileView = new ProfileView(currentUser, paymentDAO, () -> {
            if (currentUser != null) showCatalogView(currentUser);
            else showGuestCatalogView();
        }, primaryStage, this);
        BorderPane profileLayout = new BorderPane();
        profileLayout.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgLight() + ";");
        profileLayout.setCenter(profileView.getView());
        rootPane.getChildren().clear();
        rootPane.getChildren().add(profileLayout);
    }

    public void showLoginView() {
        loginView = new LoginView(this);
        rootPane.getChildren().clear();
        rootPane.getChildren().add(loginView.getView());
    }

    public void showRegisterView() {
        registerView = new RegisterView(this);
        rootPane.getChildren().clear();
        rootPane.getChildren().add(registerView.getView());
    }

    public void showAdminDashboard(Admin admin) {
        AdminDashboardView dashboard = new AdminDashboardView(admin, this);
        rootPane.getChildren().clear();
        rootPane.getChildren().add(dashboard.getView());
    }

    public void updateTextAreaStyle(TextArea textArea) {
        if (isDarkTheme) {
            textArea.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + "; " +
                    "-fx-text-fill: #000000; " +
                    "-fx-prompt-text-fill: " + getCurrentTextSecondary() + "; " +
                    STYLE_BORDER_COLOR_PREFIX + getCurrentBorderColor() + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 10; " +
                    "-fx-font-size: 13px;");
        } else {
            textArea.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + "; " +
                    STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; " +
                    "-fx-prompt-text-fill: " + getCurrentTextSecondary() + "; " +
                    STYLE_BORDER_COLOR_PREFIX + getCurrentBorderColor() + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 10; " +
                    "-fx-font-size: 13px;");
        }
    }

    public void updateDisabledButtonStyle(Button button) {
        button.setStyle("-fx-background-color: #555555; -fx-text-fill: #999999; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: default;");
    }

    private MenuBar createGuestMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");
        updateMenuBarStyle(menuBar);

        Menu viewMenu = new Menu("Вид");
        viewMenu.getStyleClass().add("menu");

        MenuItem fullScreenItem = new MenuItem("Полноэкранный режим (F11)");
        fullScreenItem.getStyleClass().add(STYLE_MENU_ITEM);
        fullScreenItem.setOnAction(e -> toggleFullScreen());

        MenuItem windowModeItem = new MenuItem("Оконный режим");
        windowModeItem.getStyleClass().add(STYLE_MENU_ITEM);
        windowModeItem.setOnAction(e -> { if (isFullScreen) toggleFullScreen(); });

        MenuItem increaseSizeItem = new MenuItem("Увеличить размер окна");
        increaseSizeItem.getStyleClass().add(STYLE_MENU_ITEM);
        increaseSizeItem.setOnAction(e -> {
            primaryStage.setWidth(primaryStage.getWidth() + 100);
            primaryStage.setHeight(primaryStage.getHeight() + 100);
        });

        MenuItem decreaseSizeItem = new MenuItem("Уменьшить размер окна");
        decreaseSizeItem.getStyleClass().add(STYLE_MENU_ITEM);
        decreaseSizeItem.setOnAction(e -> {
            if (primaryStage.getWidth() > 1000 && primaryStage.getHeight() > 700) {
                primaryStage.setWidth(primaryStage.getWidth() - 100);
                primaryStage.setHeight(primaryStage.getHeight() - 100);
            }
        });

        MenuItem themeToggleItem = new MenuItem(isDarkTheme ? "☀️ Светлая тема" : "🌙 Темная тема");
        themeToggleItem.getStyleClass().add(STYLE_MENU_ITEM);
        themeToggleItem.setOnAction(e -> toggleTheme());
        themeToggleItem.setId("themeToggle");

        viewMenu.getItems().addAll(fullScreenItem, windowModeItem,
                new SeparatorMenuItem(),
                increaseSizeItem, decreaseSizeItem,
                new SeparatorMenuItem(),
                themeToggleItem);

        Menu navigationMenu = new Menu("Навигация");
        navigationMenu.getStyleClass().add("menu");

        MenuItem loginItem = new MenuItem("Вход");
        loginItem.getStyleClass().add(STYLE_MENU_ITEM);
        loginItem.setOnAction(e -> showLoginView());

        MenuItem registerItem = new MenuItem(LABEL_REGISTRATION);
        registerItem.getStyleClass().add(STYLE_MENU_ITEM);
        registerItem.setOnAction(e -> showRegisterView());

        navigationMenu.getItems().addAll(loginItem, registerItem);

        Menu helpMenu = new Menu("Помощь");
        helpMenu.getStyleClass().add("menu");

        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.getStyleClass().add(STYLE_MENU_ITEM);
        aboutItem.setOnAction(e -> showCustomAboutDialog());

        MenuItem shortcutsItem = new MenuItem("Горячие клавиши");
        shortcutsItem.getStyleClass().add(STYLE_MENU_ITEM);
        shortcutsItem.setOnAction(e -> showCustomShortcutsDialog());

        helpMenu.getItems().addAll(aboutItem, shortcutsItem);

        menuBar.getMenus().addAll(viewMenu, navigationMenu, helpMenu);
        return menuBar;
    }

    private void updateMainMenuBarStyle() {
        if (mainLayout.getTop() instanceof MenuBar menuBar) {
            updateMenuBarStyle(menuBar);
        }
    }

    private void updateMenuBarStyle(MenuBar menuBar) {
        if (isDarkTheme) {
            menuBar.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #333333;");
        } else {
            menuBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ffcce0;");
        }
    }

    private void updateThemeToggleMenuItem() {
        if (mainLayout.getTop() instanceof MenuBar menuBar) {
            Platform.runLater(() -> {
                for (Menu menu : menuBar.getMenus()) {
                    if ("Вид".equals(menu.getText())) {
                        for (MenuItem item : menu.getItems()) {
                            if ("themeToggle".equals(item.getId())) {
                                item.setText(isDarkTheme ? "☀️ Светлая тема" : "🌙 Темная тема");
                                break;
                            }
                        }
                        break;
                    }
                }
            });
        }
    }

    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        updateThemeToggleMenuItem();
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(BaitDesireApp.class);
        prefs.putBoolean(DARK_THEME_PREF_KEY, isDarkTheme);
        updateMainMenuBarStyle();
        refreshCurrentView();
    }

    private void refreshCurrentView() {
        if (isUserLoggedIn()) {
            showCatalogView(currentUser);
        } else {
            showGuestCatalogView();
        }
    }

    public void refreshTags() {
        searchTags = tagDAO.getAllActiveTags();
        refreshCurrentView();
    }

    private void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        primaryStage.setFullScreen(isFullScreen);
    }

    private void showCustomAboutDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setTitle("");

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        dialogBox.setPadding(new Insets(25));
        dialogBox.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + "; -fx-background-radius: 15; -fx-border-color: " + getCurrentBorderColor() + "; -fx-border-radius: 15; -fx-border-width: 2;");
        dialogBox.setMaxWidth(400);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20.0);
        dropShadow.setOffsetX(5.0);
        dropShadow.setOffsetY(5.0);
        dropShadow.setColor(Color.color(0, 0, 0, isDarkTheme ? 0.8 : 0.4));
        dialogBox.setEffect(dropShadow);

        Label titleLabel = new Label("О программе");
        titleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label contentLabel = new Label("BAIT & DESIRE - Магазин рыбы\n\nВерсия 2.0\n\nРазработано для управления учетными записями пользователей.\n\nГорячие клавиши:\nF11 - Переключение полноэкранного режима\nAlt+F4 - Выход из программы\n\nВ меню 'Вид' доступно переключение темной/светлой темы");
        contentLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        contentLabel.setWrapText(true);

        Button closeButton = new Button("Закрыть");
        closeButton.setPrefWidth(150);
        updateButtonStyle(closeButton);
        closeButton.setOnAction(e -> dialogStage.close());

        VBox buttonBox = new VBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(titleLabel, contentLabel, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(null);
        dialogStage.setScene(scene);
        if (primaryStage != null) {
            dialogStage.setOnShown(e -> {
                dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - dialogBox.getPrefWidth()) / 2);
                dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 300) / 2);
            });
        }
        dialogStage.showAndWait();
    }

    private void showCustomShortcutsDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setTitle("");

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        dialogBox.setPadding(new Insets(25));
        dialogBox.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgCard() + "; -fx-background-radius: 15; -fx-border-color: " + getCurrentBorderColor() + "; -fx-border-radius: 15; -fx-border-width: 2;");
        dialogBox.setMaxWidth(400);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20.0);
        dropShadow.setOffsetX(5.0);
        dropShadow.setOffsetY(5.0);
        dropShadow.setColor(Color.color(0, 0, 0, isDarkTheme ? 0.8 : 0.4));
        dialogBox.setEffect(dropShadow);

        Label titleLabel = new Label("Горячие клавиши");
        titleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label contentLabel = new Label("F11 - Переключение полноэкранного/оконного режима\nAlt+F4 - Выход из программы\n\nВ меню 'Вид' доступны дополнительные опции:\n• Полноэкранный режим\n• Оконный режим\n• Увеличить размер окна\n• Уменьшить размер окна\n• Переключение светлой/темной темы");
        contentLabel.setStyle(STYLE_TEXT_FILL_PREFIX + getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        contentLabel.setWrapText(true);

        Button closeButton = new Button("Закрыть");
        closeButton.setPrefWidth(150);
        updateButtonStyle(closeButton);
        closeButton.setOnAction(e -> dialogStage.close());

        VBox buttonBox = new VBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogBox.getChildren().addAll(titleLabel, contentLabel, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(null);
        dialogStage.setScene(scene);
        if (primaryStage != null) {
            dialogStage.setOnShown(e -> {
                dialogStage.setX(primaryStage.getX() + (primaryStage.getWidth() - dialogBox.getPrefWidth()) / 2);
                dialogStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 300) / 2);
            });
        }
        dialogStage.showAndWait();
    }

    public void updateButtonStyle(Button button) {
        button.setStyle("-fx-background-color: " + getCurrentAccent() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + getCurrentAccentDark() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + getCurrentAccent() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;"));
    }

    public void updateTextFieldStyle(TextField textField) {
        textField.setStyle(STYLE_BG_CARD_PREFIX + getCurrentBgDarker() + "; " + STYLE_TEXT_FILL_PREFIX + getCurrentTextPrimary() + "; -fx-prompt-text-fill: " + getCurrentTextSecondary() + "; -fx-border-color: " + getCurrentBorderColor() + "; -fx-border-radius: 25; -fx-background-radius: 25; -fx-padding: 12 20; -fx-font-size: 14px;");
    }

    private Image loadProductImage(String imageUrl) {
        final int W = 240;
        final int H = 160;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                InputStream stream = getClass().getResourceAsStream(RESOURCES_IMAGES_PATH + imageUrl);
                if (stream != null) {
                    Image img = new Image(stream, W, H, false, true);
                    if (!img.isError()) {
                        stream.close();
                        return img;
                    }
                    stream.close();
                }
            } catch (Exception ignored) {}
        }
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web(getCurrentBgDarker()));
        gc.fillRect(0, 0, W, H);
        gc.setFill(Color.web(getCurrentTextSecondary()));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText("🐟", W / 2.0 - 35, H / 2.0 + 15);
        return canvas.snapshot(null, null);
    }

    private void ensureImagesDirectoryExists() {
        try {
            File imagesDir = new File(PROJECT_IMAGES_PATH);
            if (!imagesDir.exists()) imagesDir.mkdirs();
            File targetImagesDir = new File(TARGET_IMAGES_PATH);
            if (!targetImagesDir.exists()) targetImagesDir.mkdirs();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create images directory", e);
        }
    }

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

    public void addProductToCart(Product product, double kg) {
        addToCart(product, kg);
    }

    public void updateCartCountPublic() {
        updateCartCount();
    }

    public static void main(String[] args) { launch(args); }
}