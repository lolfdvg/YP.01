package com.kurcashi.views;

import com.kurcashi.dao.ReviewDAO;
import com.kurcashi.models.Product;
import com.kurcashi.models.Review;
import com.kurcashi.models.User;
import com.example.kurcashi.BaitDesireApp;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDetailDialog {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailDialog.class.getName());

    // Константы стилей
    private static final String STYLE_BG_CARD_PREFIX = "-fx-background-color: ";
    private static final String STYLE_TEXT_FILL_PREFIX = "-fx-text-fill: ";
    private static final String STYLE_FONT_SIZE_20_BOLD = "; -fx-font-size: 20px; -fx-font-weight: bold;";
    private static final String STYLE_FONT_SIZE_24_BOLD = "; -fx-font-size: 24px; -fx-font-weight: bold;";
    private static final String STYLE_FONT_SIZE_14_BOLD = "; -fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String STYLE_FONT_SIZE_14 = "; -fx-font-size: 14px;";
    private static final String STYLE_FONT_SIZE_13 = "; -fx-font-size: 13px;";
    private static final String STYLE_FONT_SIZE_12 = "; -fx-font-size: 12px;";
    private static final String STYLE_FONT_SIZE_11 = "; -fx-font-size: 11px;";
    private static final String STYLE_FONT_WEIGHT_BOLD = "; -fx-font-weight: bold;";
    private static final String STYLE_BG_RADIUS_12 = "; -fx-background-radius: 12;";
    private static final String STYLE_BORDER_RADIUS_12 = "; -fx-border-radius: 12; -fx-border-width: 1;";
    private static final String STYLE_BORDER_RADIUS_10 = "; -fx-border-radius: 10;";
    private static final String STYLE_BG_TRANSPARENT = "-fx-background-color: transparent;";
    private static final String STYLE_CURSOR_HAND = "; -fx-cursor: hand;";
    private static final String STYLE_PADDING_10_20 = "; -fx-padding: 10 20;";
    private static final String STYLE_BUTTON_PRIMARY = STYLE_BG_CARD_PREFIX + "%s;" + STYLE_TEXT_FILL_PREFIX + "white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;";
    private static final String STYLE_BUTTON_DISABLED = "-fx-background-color: #cccccc; -fx-text-fill: #999999; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8;";
    private static final String STYLE_STAR_ACTIVE = "-fx-text-fill: #f4b942; -fx-font-size: 28px; -fx-cursor: hand;";
    private static final String STYLE_STAR_INACTIVE = "-fx-text-fill: #d9b3c2; -fx-font-size: 28px; -fx-cursor: hand;";
    private static final String STYLE_BG_CARD = STYLE_BG_CARD_PREFIX + "%s;" + STYLE_BG_RADIUS_12 + STYLE_BORDER_RADIUS_12;
    private static final String ERROR_TITLE = "Ошибка";
    private static final String REVIEW_IMAGES_DIR = "src/main/resources/review_images/";
    private static final String TARGET_REVIEW_IMAGES_DIR = "target/classes/review_images/";
    private static final String STYLE_BUTTON_ADD_TO_CART = "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;";

    private final Product product;
    private final User currentUser;
    private final ReviewDAO reviewDAO;
    private final Runnable onCartUpdated;
    private final Stage parentStage;
    private final BaitDesireApp app;

    private Stage dialogStage;
    private Scene parentScene;
    private Effect originalEffect;
    private VBox reviewsList;
    private HBox starRatingBox;
    private int selectedRating = 0;
    private int currentPage = 0;
    private final int reviewsPerPage = 3;
    private int totalReviews;
    private Label pageInfoLabel;
    private Button prevButton;
    private Button nextButton;
    private List<Review> allReviews;

    public ProductDetailDialog(Product product, User user, Runnable onCartUpdated, Stage parentStage, BaitDesireApp app) {
        this.product = product;
        this.currentUser = user;
        this.reviewDAO = new ReviewDAO();
        this.onCartUpdated = onCartUpdated;
        this.parentStage = parentStage;
        this.app = app;
        if (parentStage != null && parentStage.getScene() != null) {
            this.parentScene = parentStage.getScene();
        }
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setTitle("");

        applyBlurEffect();
        dialogStage.setOnCloseRequest(e -> removeBlurEffect());
        dialogStage.setOnHidden(e -> removeBlurEffect());

        VBox mainContainer = createMainContainer();
        HBox titleBar = createTitleBar();
        mainContainer.getChildren().add(titleBar);

        TabPane tabPane = createTabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainContainer.getChildren().add(tabPane);

        Scene scene = new Scene(mainContainer, 600, 700);
        dialogStage.setScene(scene);
        positionDialog();
        dialogStage.showAndWait();
    }

    private VBox createMainContainer() {
        VBox container = new VBox(0);
        container.setStyle(String.format(STYLE_BG_CARD, app.getCurrentBgLight())
                + "; -fx-border-color: " + app.getCurrentAccent() + "; -fx-border-width: 2;");
        return container;
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(15, 20, 15, 20));
        titleBar.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + "; -fx-background-radius: 15 15 0 0;");

        Label titleLabel = new Label(product.getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + ";");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER);

        Button closeButton = createCloseButton();
        titleBar.getChildren().addAll(titleLabel, closeButton);
        return titleBar;
    }

    private Button createCloseButton() {
        Button button = new Button("✕");
        button.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + "; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + app.getCurrentAccent() + "; -fx-text-fill: white; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle(STYLE_BG_TRANSPARENT + STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + "; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 5;"));
        button.setOnAction(e -> dialogStage.close());
        return button;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle(STYLE_BG_TRANSPARENT);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        boolean isGuest = (currentUser == null || currentUser.getUserId() <= 0);

        Tab detailsTab = new Tab("📋 Описание");
        detailsTab.setContent(createDetailsTab());
        customizeTab(detailsTab);

        Tab reviewsTab = new Tab("⭐ Отзывы");
        reviewsTab.setContent(isGuest ? createReviewsTabForGuest() : createReviewsTab());
        customizeTab(reviewsTab);

        Tab addToCartTab = new Tab("🛒 В корзину");
        addToCartTab.setContent(createAddToCartTab());
        customizeTab(addToCartTab);

        tabPane.getTabs().addAll(detailsTab, reviewsTab, addToCartTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateTabStyle(tabPane);
            if (newTab == reviewsTab) loadReviews();
        });
        updateTabStyle(tabPane);
        return tabPane;
    }

    private void customizeTab(Tab tab) {
        tab.setStyle("-fx-background-color: " + app.getCurrentBgCard() + "; -fx-background-radius: 10 10 0 0; -fx-padding: 8 20; -fx-font-size: 13px;");
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

    private void positionDialog() {
        if (parentStage != null) {
            dialogStage.setOnShown(e -> {
                dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - 600) / 2);
                dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - 700) / 2);
            });
        }
    }

    // ==================== ВКЛАДКА "ОПИСАНИЕ" ====================
    private VBox createDetailsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgLight() + ";");

        StackPane imageContainer = createImageContainer();
        HBox ratingBox = createRatingDisplay();
        Label priceLabel = createPriceLabel();
        Label stockLabel = createStockLabel();
        Label descTitle = new Label("Описание:");
        descTitle.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);

        TextArea descriptionArea = new TextArea(product.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        app.updateTextAreaStyle(descriptionArea);
        descriptionArea.setPrefHeight(120);

        container.getChildren().addAll(imageContainer, ratingBox, priceLabel, stockLabel, descTitle, descriptionArea);
        return container;
    }

    private StackPane createImageContainer() {
        final int W = 250;
        final int H = 180;
        StackPane container = new StackPane();
        container.setPrefWidth(W);
        container.setPrefHeight(H);
        container.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + STYLE_BORDER_RADIUS_10 + "; -fx-border-color: " + app.getCurrentAccent() + STYLE_BORDER_RADIUS_10);
        container.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView(loadProductImage(product.getImageUrl()));
        imageView.setFitWidth(W);
        imageView.setFitHeight(H);
        imageView.setPreserveRatio(true);
        container.getChildren().add(imageView);
        return container;
    }

    private HBox createRatingDisplay() {
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        double avgRating = reviewDAO.getAverageRating(product.getId());
        HBox starsBox = createStarsDisplay(avgRating);
        Label ratingValueLabel = new Label(String.format("%.1f", avgRating));
        ratingValueLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);
        totalReviews = reviewDAO.getReviewsByProductId(product.getId()).size();
        Label reviewCountLabel = new Label("(" + totalReviews + " отзывов)");
        reviewCountLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        ratingBox.getChildren().addAll(starsBox, ratingValueLabel, reviewCountLabel);
        return ratingBox;
    }

    private Label createPriceLabel() {
        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentAccentDark() + STYLE_FONT_SIZE_24_BOLD);
        return priceLabel;
    }

    private Label createStockLabel() {
        Label stockLabel = new Label();
        if (product.getStockQuantity() > 0) {
            stockLabel.setText("📦 В наличии: " + product.getStockQuantity() + " кг");
            stockLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentSuccess() + STYLE_FONT_SIZE_12);
        } else {
            stockLabel.setText("❌ Нет в наличии");
            stockLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentError() + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD);
        }
        stockLabel.setPadding(new Insets(5, 0, 0, 0));
        return stockLabel;
    }

    // ==================== ВКЛАДКА "ОТЗЫВЫ" ДЛЯ ГОСТЯ ====================
    private VBox createReviewsTabForGuest() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgLight() + ";");

        HBox headerBox = createRatingHeader();
        reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(5));
        ScrollPane scrollPane = createScrollPane(reviewsList, 300);
        HBox paginationBox = createPaginationControls();
        paginationBox.setVisible(false);
        loadReviews();

        VBox guestInfoBox = createGuestInfoBox();
        container.getChildren().addAll(headerBox, scrollPane, paginationBox, guestInfoBox);
        return container;
    }

    // ==================== ВКЛАДКА "ОТЗЫВЫ" ДЛЯ АВТОРИЗОВАННОГО ПОЛЬЗОВАТЕЛЯ ====================
    private VBox createReviewsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgLight() + ";");

        HBox headerBox = createRatingHeader();
        reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(5));
        ScrollPane scrollPane = createScrollPane(reviewsList, 280);
        HBox paginationBox = createPaginationControls();
        VBox addReviewBox = createAddReviewBox();

        container.getChildren().addAll(headerBox, scrollPane, paginationBox, addReviewBox);
        loadReviews();
        return container;
    }

    private HBox createRatingHeader() {
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        double avgRating = reviewDAO.getAverageRating(product.getId());
        Label avgRatingLabel = new Label(String.format("%.1f", avgRating));
        avgRatingLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_24_BOLD);
        HBox starsBox = createStarsDisplay(avgRating);
        headerBox.getChildren().addAll(avgRatingLabel, starsBox);
        return headerBox;
    }

    private VBox createGuestInfoBox() {
        VBox guestBox = new VBox(12);
        guestBox.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + STYLE_BG_RADIUS_12);
        guestBox.setPadding(new Insets(15));
        guestBox.setAlignment(Pos.CENTER);
        Label guestTitle = new Label("🔒 Оставить отзыв могут только авторизованные пользователи");
        guestTitle.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);
        guestTitle.setWrapText(true);
        guestTitle.setAlignment(Pos.CENTER);
        HBox guestButtons = new HBox(10);
        guestButtons.setAlignment(Pos.CENTER);
        Button loginButton = new Button("🔐 Войти");
        loginButton.setStyle(String.format(STYLE_BUTTON_PRIMARY, app.getCurrentAccent()));
        loginButton.setOnAction(e -> {
            dialogStage.close();
            app.showLoginView();
        });
        Button registerButton = new Button("📝 Регистрация");
        registerButton.setStyle(String.format(STYLE_BUTTON_PRIMARY, app.getCurrentAccentDark()));
        registerButton.setOnAction(e -> {
            dialogStage.close();
            app.showRegisterView();
        });
        guestButtons.getChildren().addAll(loginButton, registerButton);
        guestBox.getChildren().addAll(guestTitle, guestButtons);
        return guestBox;
    }

    private VBox createAddReviewBox() {
        VBox addReviewBox = new VBox(12);
        addReviewBox.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + STYLE_BG_RADIUS_12);
        addReviewBox.setPadding(new Insets(15));
        Label addReviewTitle = new Label("Оставить отзыв");
        addReviewTitle.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);

        HBox ratingInputBox = new HBox(8);
        ratingInputBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Ваша оценка:");
        ratingLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_13);
        starRatingBox = new HBox(5);
        starRatingBox.setAlignment(Pos.CENTER_LEFT);
        createStarRatingInput();
        ratingInputBox.getChildren().addAll(ratingLabel, starRatingBox);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Напишите ваш отзыв...");
        commentArea.setWrapText(true);
        commentArea.setPrefHeight(80);
        app.updateTextAreaStyle(commentArea);

        HBox imageUploadBox = createImageUploadBox();
        Button submitButton = createSubmitButton(commentArea, imageUploadBox);

        addReviewBox.getChildren().addAll(addReviewTitle, ratingInputBox, commentArea, imageUploadBox, submitButton);
        return addReviewBox;
    }

    private HBox createImageUploadBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label imageLabel = new Label("Фото (необязательно):");
        imageLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_13);
        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Путь к изображению");
        imagePathField.setPrefWidth(200);
        imagePathField.setEditable(false);
        app.updateTextFieldStyle(imagePathField);
        Button chooseButton = new Button("Выбрать файл");
        chooseButton.setStyle(String.format(STYLE_BUTTON_PRIMARY, app.getCurrentAccent()));
        final String[] selectedImagePath = {null};
        chooseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение для отзыва");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialogStage);
            if (selectedFile != null) {
                String destPath = copyReviewImage(selectedFile);
                if (destPath != null) {
                    selectedImagePath[0] = destPath;
                    imagePathField.setText(destPath);
                }
            }
        });
        box.getChildren().addAll(imageLabel, imagePathField, chooseButton);
        box.setUserData(selectedImagePath);
        return box;
    }

    private String copyReviewImage(File sourceFile) {
        try {
            ensureReviewImagesDirectoryExists();
            String ext = "";
            String fileName = sourceFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) ext = fileName.substring(dotIndex);
            String uniqueName = "review_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            File destFile = new File(REVIEW_IMAGES_DIR, uniqueName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File targetDest = new File(TARGET_REVIEW_IMAGES_DIR, uniqueName);
            if (!targetDest.getParentFile().exists() && !targetDest.getParentFile().mkdirs()) {
                LOGGER.warning("Failed to create target review images directory");
            } else {
                Files.copy(sourceFile.toPath(), targetDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueName;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to copy review image", ex);
            return null;
        }
    }

    private void ensureReviewImagesDirectoryExists() {
        File dir = new File(REVIEW_IMAGES_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.warning("Failed to create review images directory: " + REVIEW_IMAGES_DIR);
        }
        File targetDir = new File(TARGET_REVIEW_IMAGES_DIR);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            LOGGER.warning("Failed to create target review images directory: " + TARGET_REVIEW_IMAGES_DIR);
        }
    }

    private Button createSubmitButton(TextArea commentArea, HBox imageUploadBox) {
        Button submitButton = new Button("Отправить отзыв");
        submitButton.setPrefWidth(200);
        submitButton.setStyle(String.format(STYLE_BUTTON_PRIMARY, app.getCurrentAccent()));

        boolean hasReviewed = reviewDAO.hasUserReviewed(product.getId(), currentUser.getUserId());
        if (hasReviewed) {
            submitButton.setDisable(true);
            submitButton.setText("✓ Вы уже оставили отзыв");
            submitButton.setStyle("-fx-background-color: " + app.getCurrentSuccess() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
            return submitButton;
        }

        submitButton.setOnAction(e -> {
            if (selectedRating == 0) {
                showAlert(ERROR_TITLE, "Пожалуйста, поставьте оценку");
                return;
            }
            String comment = commentArea.getText().trim();
            if (comment.isEmpty()) {
                showAlert(ERROR_TITLE, "Пожалуйста, напишите отзыв");
                return;
            }

            String imagePath = null;
            Object userData = imageUploadBox.getUserData();
            if (userData instanceof String[]) {
                imagePath = ((String[]) userData)[0];
            }

            Review review = new Review(product.getId(), currentUser.getUserId(), currentUser.getUsername(), selectedRating, comment);
            review.setImagePath(imagePath);
            if (reviewDAO.addReview(review)) {
                showAlert("Успех", "Ваш отзыв добавлен!");
                loadReviews();
                commentArea.clear();
                selectedRating = 0;
                createStarRatingInput();
                submitButton.setDisable(true);
                submitButton.setText("✓ Вы уже оставили отзыв");
                submitButton.setStyle("-fx-background-color: " + app.getCurrentSuccess() + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
                if (onCartUpdated != null) onCartUpdated.run();
            } else {
                showAlert(ERROR_TITLE, "Не удалось добавить отзыв");
            }
        });
        return submitButton;
    }

    private HBox createPaginationControls() {
        HBox paginationBox = new HBox(15);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setPadding(new Insets(10, 0, 0, 0));
        prevButton = new Button("◀ Предыдущие");
        nextButton = new Button("Следующие ▶");
        pageInfoLabel = new Label();
        String buttonStyle = String.format(STYLE_BUTTON_PRIMARY, app.getCurrentAccent());
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        prevButton.setOnAction(e -> { if (currentPage > 0) { currentPage--; displayCurrentPageReviews(); } });
        nextButton.setOnAction(e -> {
            int maxPages = (int) Math.ceil((double) totalReviews / reviewsPerPage);
            if (currentPage < maxPages - 1) { currentPage++; displayCurrentPageReviews(); }
        });
        paginationBox.getChildren().addAll(prevButton, pageInfoLabel, nextButton);
        return paginationBox;
    }

    private void loadReviews() {
        allReviews = reviewDAO.getReviewsByProductId(product.getId());
        totalReviews = allReviews.size();
        currentPage = 0;
        displayCurrentPageReviews();
    }

    private void displayCurrentPageReviews() {
        if (reviewsList == null) return;
        reviewsList.getChildren().clear();
        if (allReviews == null || allReviews.isEmpty()) {
            reviewsList.getChildren().add(createEmptyReviewsBox());
            if (prevButton != null) {
                prevButton.setDisable(true);
                nextButton.setDisable(true);
                pageInfoLabel.setText("0 из 0");
            }
            return;
        }
        int start = currentPage * reviewsPerPage;
        int end = Math.min(start + reviewsPerPage, totalReviews);
        for (int i = start; i < end; i++) {
            reviewsList.getChildren().add(createReviewCard(allReviews.get(i)));
        }
        int maxPages = (int) Math.ceil((double) totalReviews / reviewsPerPage);
        if (prevButton != null) {
            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= maxPages - 1);
            pageInfoLabel.setText((currentPage + 1) + " из " + Math.max(1, maxPages));
            pageInfoLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        }
    }

    private VBox createEmptyReviewsBox() {
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(30));
        Label emptyLabel = new Label("📝 Пока нет отзывов");
        emptyLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        Label inviteLabel = new Label("Будьте первым, кто оставит отзыв!");
        inviteLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        emptyBox.getChildren().addAll(emptyLabel, inviteLabel);
        return emptyBox;
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + STYLE_BG_RADIUS_12);
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label userNameLabel = new Label(review.getUserName());
        userNameLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14_BOLD);
        HBox starsBox = createStarsDisplay(review.getRating());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label dateLabel = new Label(review.getCreatedAt().toString().substring(0, 10));
        dateLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_11);
        header.getChildren().addAll(userNameLabel, starsBox, spacer, dateLabel);
        Label commentLabel = new Label(review.getComment());
        commentLabel.setStyle(STYLE_TEXT_FILL_PREFIX + (app.isDarkTheme() ? "#000000" : app.getCurrentTextPrimary()) + STYLE_FONT_SIZE_12);
        commentLabel.setWrapText(true);
        card.getChildren().addAll(header, commentLabel);

        if (review.getImagePath() != null && !review.getImagePath().isEmpty()) {
            ImageView reviewImage = new ImageView();
            reviewImage.setFitWidth(120);
            reviewImage.setFitHeight(120);
            reviewImage.setPreserveRatio(true);
            try {
                InputStream stream = getClass().getResourceAsStream("/review_images/" + review.getImagePath());
                if (stream != null) {
                    reviewImage.setImage(new Image(stream));
                } else {
                    File file = new File("src/main/resources/review_images/" + review.getImagePath());
                    if (file.exists()) {
                        reviewImage.setImage(new Image(file.toURI().toString()));
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load review image", e);
            }
            card.getChildren().add(reviewImage);
        }
        return card;
    }

    // ==================== ВКЛАДКА "В КОРЗИНУ" ====================
    private VBox createAddToCartTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        container.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgLight() + ";");

        Label titleLabel = new Label("Добавить в корзину");
        titleLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_20_BOLD);

        Label priceLabel = createPriceLabel();

        HBox quantityBox = new HBox(15);
        quantityBox.setAlignment(Pos.CENTER);
        Label quantityLabel = new Label("Количество (кг):");
        quantityLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14);

        double maxWeight = product.getStockQuantity() > 0 ? product.getStockQuantity() : 1000.0;
        Spinner<Double> weightSpinner = new Spinner<>(0.1, maxWeight, 1.0);
        weightSpinner.setEditable(true);
        weightSpinner.setPrefWidth(120);
        weightSpinner.setStyle(STYLE_BG_CARD_PREFIX + app.getCurrentBgLight() + "; -fx-border-color: " + app.getCurrentAccent() + STYLE_BORDER_RADIUS_10);
        weightSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override public String toString(Double object) { return object == null ? "0.0" : String.format("%.2f", object); }
            @Override public Double fromString(String string) {
                try { double value = Double.parseDouble(string.replace(',', '.')); return Math.clamp(value, 0.1, maxWeight); }
                catch (NumberFormatException e) { return 1.0; }
            }
        });

        HBox buttonControls = createSpinnerButtons(weightSpinner, maxWeight);
        Label totalLabel = new Label();
        totalLabel.setStyle(STYLE_TEXT_FILL_PREFIX + app.getCurrentAccentDark() + STYLE_FONT_SIZE_20_BOLD);
        totalLabel.setPadding(new Insets(10, 0, 0, 0));
        weightSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) totalLabel.setText(String.format("Итого: %.2f руб", product.getPrice() * newVal));
        });
        totalLabel.setText(String.format("Итого: %.2f руб", product.getPrice()));

        Button addButton = createAddToCartButton(weightSpinner);
        VBox quantityControlBox = new VBox(12);
        quantityControlBox.setAlignment(Pos.CENTER);
        quantityControlBox.getChildren().addAll(quantityLabel, weightSpinner, buttonControls, totalLabel, addButton);
        container.getChildren().addAll(titleLabel, priceLabel, quantityControlBox);
        return container;
    }

    private HBox createSpinnerButtons(Spinner<Double> spinner, double maxWeight) {
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER);
        Button minus1Button = new Button("-1 кг");
        Button minus05Button = new Button("-0.5 кг");
        Button plus05Button = new Button("+0.5 кг");
        Button plus1Button = new Button("+1 кг");
        String btnStyle = STYLE_BG_CARD_PREFIX + app.getCurrentBgCard() + ";" + STYLE_TEXT_FILL_PREFIX + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_12 + STYLE_FONT_WEIGHT_BOLD + STYLE_PADDING_10_20 + STYLE_BG_RADIUS_12 + STYLE_CURSOR_HAND + "; -fx-border-color: " + app.getCurrentAccent() + STYLE_BORDER_RADIUS_12;
        minus1Button.setStyle(btnStyle);
        minus05Button.setStyle(btnStyle);
        plus05Button.setStyle(btnStyle);
        plus1Button.setStyle(btnStyle);
        minus1Button.setOnAction(e -> adjustSpinner(spinner, -1.0, maxWeight));
        minus05Button.setOnAction(e -> adjustSpinner(spinner, -0.5, maxWeight));
        plus05Button.setOnAction(e -> adjustSpinner(spinner, 0.5, maxWeight));
        plus1Button.setOnAction(e -> adjustSpinner(spinner, 1.0, maxWeight));
        controls.getChildren().addAll(minus1Button, minus05Button, plus05Button, plus1Button);
        return controls;
    }

    private void adjustSpinner(Spinner<Double> spinner, double delta, double maxWeight) {
        double newVal = spinner.getValue() + delta;
        if (newVal >= 0.1 && newVal <= maxWeight) {
            spinner.getValueFactory().setValue(newVal);
        }
    }

    private Button createAddToCartButton(Spinner<Double> weightSpinner) {
        Button addButton = new Button("🛒 Добавить в корзину");
        addButton.setPrefWidth(250);
        addButton.setStyle(String.format(STYLE_BUTTON_ADD_TO_CART, app.getCurrentAccent()));
        addButton.setOnMouseEntered(e -> addButton.setStyle(String.format(STYLE_BUTTON_ADD_TO_CART, app.getCurrentAccentDark())));
        addButton.setOnMouseExited(e -> addButton.setStyle(String.format(STYLE_BUTTON_ADD_TO_CART, app.getCurrentAccent())));

        if (product.getStockQuantity() <= 0) {
            addButton.setDisable(true);
            addButton.setText("❌ Нет в наличии");
            addButton.setStyle(STYLE_BUTTON_DISABLED);
        } else {
            addButton.setOnAction(e -> {
                double kg = weightSpinner.getValue();
                app.addProductToCart(product, kg);
                app.updateCartCountPublic();
                showAlert("Успех", String.format("Добавлено %.2f кг товара \"%s\" в корзину", kg, product.getName()));
            });
        }
        return addButton;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    private void createStarRatingInput() {
        starRatingBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int starValue = i;
            Label starLabel = new Label("☆");
            starLabel.setStyle(STYLE_STAR_INACTIVE);
            starLabel.setOnMouseEntered(e -> updateStarsPreview(starValue));
            starLabel.setOnMouseExited(e -> resetStarsToSelected());
            starLabel.setOnMouseClicked(e -> {
                selectedRating = starValue;
                resetStarsToSelected();
            });
            starRatingBox.getChildren().add(starLabel);
        }
    }

    private void updateStarsPreview(int upTo) {
        for (int j = 1; j <= 5; j++) {
            Label s = (Label) starRatingBox.getChildren().get(j - 1);
            if (j <= upTo) {
                s.setText("★");
                s.setStyle(STYLE_STAR_ACTIVE);
            } else {
                s.setText("☆");
                s.setStyle(STYLE_STAR_INACTIVE);
            }
        }
    }

    private void resetStarsToSelected() {
        for (int j = 1; j <= 5; j++) {
            Label s = (Label) starRatingBox.getChildren().get(j - 1);
            if (j <= selectedRating) {
                s.setText("★");
                s.setStyle(STYLE_STAR_ACTIVE);
            } else {
                s.setText("☆");
                s.setStyle(STYLE_STAR_INACTIVE);
            }
        }
    }

    private HBox createStarsDisplay(double rating) {
        HBox starsBox = new HBox(3);
        starsBox.setAlignment(Pos.CENTER_LEFT);
        int fullStars = (int) Math.clamp(Math.round(rating), 0, 5);
        for (int i = 0; i < fullStars; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-text-fill: #f4b942; -fx-font-size: 16px;");
            starsBox.getChildren().add(star);
        }
        for (int i = fullStars; i < 5; i++) {
            Label star = new Label("☆");
            star.setStyle("-fx-text-fill: #d9b3c2; -fx-font-size: 16px;");
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private ScrollPane createScrollPane(VBox content, double prefHeight) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(prefHeight);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private void showAlert(String title, String message) {
        com.kurcashi.utils.CustomAlert.show(dialogStage, title, message, com.kurcashi.utils.CustomAlert.AlertType.INFO);
    }

    private Image loadProductImage(String imageUrl) {
        final int W = 250;
        final int H = 180;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                InputStream stream = getClass().getResourceAsStream("/images/" + imageUrl);
                if (stream != null) {
                    Image img = new Image(stream, W, H, false, true);
                    if (!img.isError()) return img;
                    stream.close();
                }
            } catch (Exception ignored) {
                // ignored
            }
        }
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web(app.getCurrentBgDarker()));
        gc.fillRect(0, 0, W, H);
        gc.setFill(Color.web(app.getCurrentTextSecondary()));
        gc.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 48));
        gc.fillText("🐟", (double) W / 2 - 35, (double) H / 2 + 15);
        return canvas.snapshot(null, null);
    }

    private void applyBlurEffect() {
        if (parentScene != null && parentScene.getRoot() != null) {
            originalEffect = parentScene.getRoot().getEffect();
            BoxBlur blurEffect = new BoxBlur(15, 15, 3);
            parentScene.getRoot().setEffect(blurEffect);
        }
    }

    private void removeBlurEffect() {
        if (parentScene != null && parentScene.getRoot() != null) {
            parentScene.getRoot().setEffect(originalEffect);
        }
    }
}