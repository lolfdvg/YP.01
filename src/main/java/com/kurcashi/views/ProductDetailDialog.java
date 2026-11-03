package com.kurcashi.views;

import com.kurcashi.dao.RecipeDAO;
import com.kurcashi.dao.RecommendationDAO;
import com.kurcashi.dao.ReviewDAO;
import com.kurcashi.dao.CartDAO;
import com.kurcashi.models.Product;
import com.kurcashi.models.Recipe;
import com.kurcashi.models.RecipeIngredient;
import com.kurcashi.models.Recommendation;
import com.kurcashi.models.Review;
import com.kurcashi.models.User;
import com.kurcashi.utils.CustomAlert;
import com.example.kurcashi.BaitDesireApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDetailDialog {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailDialog.class.getName());
    private static final String REVIEW_IMAGES_DIR = "src/main/resources/review_images/";
    private static final String TARGET_REVIEW_IMAGES_DIR = "target/classes/review_images/";

    private final Product product;
    private final User currentUser;
    private final ReviewDAO reviewDAO;
    private final RecipeDAO recipeDAO;
    private final RecommendationDAO recommendationDAO;
    private final CartDAO cartDAO;
    private final Runnable onCartUpdated;
    private final Stage parentStage;
    private final BaitDesireApp app;

    private Stage dialogStage;
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
    private Label detailsRatingLabel;

    public ProductDetailDialog(Product product, User user, Runnable onCartUpdated, Stage parentStage, BaitDesireApp app) {
        this.product = product;
        this.currentUser = user;
        this.reviewDAO = new ReviewDAO();
        this.recipeDAO = new RecipeDAO();
        this.recommendationDAO = new RecommendationDAO();
        this.cartDAO = new CartDAO();
        this.onCartUpdated = onCartUpdated;
        this.parentStage = parentStage;
        this.app = app;
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + app.getCurrentBgCard() +
                "; -fx-background-radius: 15; -fx-border-color: " + app.getCurrentBorderColor() +
                "; -fx-border-radius: 15; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        HBox titleBar = createTitleBar();
        root.getChildren().add(titleBar);

        TabPane tabPane = createTabPane();
        tabPane.getStyleClass().add("dialog-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 700, 750);
        scene.setFill(Color.TRANSPARENT);
        URL css = getClass().getResource("/styles/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        dialogStage.setScene(scene);
        if (parentStage != null) {
            dialogStage.setOnShown(e -> {
                dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - 700) / 2);
                dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - 750) / 2);
            });
        }
        dialogStage.showAndWait();
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(15, 20, 15, 20));
        titleBar.setStyle("-fx-background-color: " + app.getCurrentBgLight() +
                "; -fx-background-radius: 15 15 0 0; -fx-border-color: transparent transparent " +
                app.getCurrentBorderColor() + " transparent; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(product.getName());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> dialogStage.close());

        final double[] xOffset = {0};
        final double[] yOffset = {0};
        titleBar.setOnMousePressed(event -> { xOffset[0] = event.getSceneX(); yOffset[0] = event.getSceneY(); });
        titleBar.setOnMouseDragged(event -> {
            dialogStage.setX(event.getScreenX() - xOffset[0]);
            dialogStage.setY(event.getScreenY() - yOffset[0]);
        });

        titleBar.getChildren().addAll(titleLabel, spacer, closeBtn);
        return titleBar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent; -fx-padding: 10 20 20 20;");

        boolean isGuest = (currentUser == null || currentUser.getUserId() <= 0);

        Tab detailsTab = new Tab("📋 Описание и покупка");
        detailsTab.setContent(createDetailsTab());

        Tab culinaryTab = new Tab("🍳 Рецепты и сочетания");
        culinaryTab.setContent(createCulinaryTab());

        Tab reviewsTab = new Tab("⭐ Отзывы");
        reviewsTab.setContent(isGuest ? createReviewsTabForGuest() : createReviewsTab());

        tabPane.getTabs().addAll(detailsTab, culinaryTab, reviewsTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == reviewsTab) loadReviews();
        });
        return tabPane;
    }

    private VBox createDetailsTab() {
        VBox container = new VBox(15);

        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.TOP_LEFT);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(300, 250);
        imageContainer.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-background-radius: 12; -fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 12;");
        ImageView iv = new ImageView(loadProductImage(product.getImageUrl()));
        iv.setFitWidth(300); iv.setFitHeight(250); iv.setPreserveRatio(true);
        imageContainer.getChildren().add(iv);

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(product.getFormattedPrice());
        price.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentAccentDark() + ";");

        Label stock = new Label(product.getStockQuantity() > 0 ? "В наличии: " + product.getStockQuantity() + " кг" : "❌ Нет в наличии");
        stock.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (product.getStockQuantity() > 0 ? app.getCurrentSuccess() : app.getCurrentError()) + ";");

        detailsRatingLabel = new Label("⭐ Рейтинг: " + String.format("%.1f", product.getRating()));
        detailsRatingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        infoBox.getChildren().addAll(price, stock, detailsRatingLabel);

        topBox.getChildren().addAll(imageContainer, infoBox);

        Label descTitle = new Label("Описание товара");
        descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        TextArea desc = new TextArea(product.getDescription());
        desc.setEditable(false);
        desc.setWrapText(true);
        desc.setPrefHeight(100);
        desc.setStyle("-fx-background-color: transparent; -fx-control-inner-background: " + app.getCurrentBgLight() + "; -fx-text-fill: " + app.getCurrentTextPrimary() + "; -fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 8;");

        VBox buyBox = new VBox(15);
        buyBox.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 12;");

        Label qtyLbl = new Label("Количество (кг):");
        qtyLbl.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + "; -fx-font-weight: bold;");

        double maxWeight = product.getStockQuantity() > 0 ? product.getStockQuantity() : 1000.0;
        Spinner<Double> spinner = new Spinner<>(0.1, maxWeight, 1.0);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);

        HBox btnControls = createSpinnerButtons(spinner, maxWeight);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentAccentDark() + ";");
        totalLabel.setText(String.format("Итого: %.2f ₽", product.getPrice()));

        spinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) totalLabel.setText(String.format("Итого: %.2f ₽", product.getPrice() * newV));
        });

        Button add = new Button("Добавить в корзину");
        add.getStyleClass().add("btn-primary");
        add.setStyle("-fx-font-size: 16px; -fx-padding: 12 30;");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(product.getStockQuantity() <= 0);
        add.setOnAction(e -> {
            app.addProductToCart(product, spinner.getValue());
            app.updateCartCountPublic();
            CustomAlert.show(dialogStage, "Успех", String.format("Добавлено %.2f кг в корзину", spinner.getValue()), CustomAlert.AlertType.INFO);
            dialogStage.close();
        });

        HBox qtyRow = new HBox(15, qtyLbl, spinner, btnControls);
        qtyRow.setAlignment(Pos.CENTER_LEFT);
        buyBox.getChildren().addAll(qtyRow, totalLabel, add);

        container.getChildren().addAll(topBox, descTitle, desc, buyBox);
        return container;
    }

    private HBox createSpinnerButtons(Spinner<Double> spinner, double maxWeight) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER);

        Button minus1 = new Button("-1");
        Button minus05 = new Button("-0.5");
        Button plus05 = new Button("+0.5");
        Button plus1 = new Button("+1");

        String btnStyle = "-fx-background-color: white; -fx-text-fill: " + app.getCurrentTextPrimary() + "; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-color: " + app.getCurrentBorderColor() + "; -fx-padding: 5 10;";
        minus1.setStyle(btnStyle); minus05.setStyle(btnStyle); plus05.setStyle(btnStyle); plus1.setStyle(btnStyle);

        minus1.setOnAction(e -> adjustSpinner(spinner, -1.0, maxWeight));
        minus05.setOnAction(e -> adjustSpinner(spinner, -0.5, maxWeight));
        plus05.setOnAction(e -> adjustSpinner(spinner, 0.5, maxWeight));
        plus1.setOnAction(e -> adjustSpinner(spinner, 1.0, maxWeight));

        box.getChildren().addAll(minus1, minus05, plus05, plus1);
        return box;
    }

    private void adjustSpinner(Spinner<Double> spinner, double delta, double maxWeight) {
        double newVal = spinner.getValue() + delta;
        if (newVal >= 0.1 && newVal <= maxWeight) spinner.getValueFactory().setValue(newVal);
    }

    private VBox createCulinaryTab() {
        VBox container = new VBox(20);

        Label recTitle = new Label("🍷 С чем подавать");
        recTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        VBox recBox = new VBox(10);
        List<Recommendation> recs = recommendationDAO.getByProductId(product.getId());
        if (recs.isEmpty()) {
            recBox.getChildren().add(new Label("Нет рекомендаций"));
        } else {
            for (Recommendation r : recs) {
                HBox recCard = new HBox(15);
                recCard.setAlignment(Pos.CENTER_LEFT);
                recCard.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-padding: 10 15; -fx-background-radius: 10;");

                VBox info = new VBox(5);
                Label name = new Label(r.getRecommendedProductName());
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
                Label comment = new Label(r.getComment());
                comment.setStyle("-fx-font-size: 12px; -fx-text-fill: " + app.getCurrentTextSecondary() + ";");
                info.getChildren().addAll(name, comment);

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

                Button addBtn = new Button("+ В корзину");
                addBtn.getStyleClass().add("btn-primary");
                addBtn.setDisable(r.getRecommendedProductStock() <= 0);
                addBtn.setOnAction(e -> {
                    if (currentUser != null) {
                        cartDAO.addToCart(currentUser.getUserId(), r.getRecommendedProductId(), 1);
                        if (onCartUpdated != null) onCartUpdated.run();
                        CustomAlert.show(dialogStage, "Добавлено", "Товар добавлен", CustomAlert.AlertType.INFO);
                    }
                });
                recCard.getChildren().addAll(info, sp, addBtn);
                recBox.getChildren().add(recCard);
            }
        }

        Label recTitle2 = new Label("🍳 Рецепты с этим продуктом");
        recTitle2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        VBox recipesBox = new VBox(10);
        List<Recipe> recipes = recipeDAO.getRecipesByProductId(product.getId());
        if (recipes.isEmpty()) {
            recipesBox.getChildren().add(new Label("Пока нет рецептов"));
        } else {
            for (Recipe r : recipes) {
                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-padding: 15; -fx-background-radius: 10;");

                Label title = new Label("🍽 " + r.getTitle());
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

                Label meta = new Label("⏱ " + r.getCookingTime() + " мин • 🎯 " + r.getDifficultyText());
                meta.setStyle("-fx-font-size: 12px; -fx-text-fill: " + app.getCurrentTextSecondary() + ";");

                Label ingTitle = new Label("Ингредиенты:");
                ingTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

                VBox ingBox = new VBox(4);
                for (RecipeIngredient ri : r.getIngredients()) {
                    Label ing = new Label("• " + ri.getProductName() + " — " + ri.getQuantity() + " " + ri.getUnit()
                            + (ri.getStockQuantity() <= 0 ? "  ❌ нет в наличии" : ""));
                    ing.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (ri.getStockQuantity() <= 0 ? app.getCurrentError() : app.getCurrentTextPrimary()) + ";");
                    ingBox.getChildren().add(ing);
                }

                Label stepsLabel = new Label(r.getSteps());
                stepsLabel.setWrapText(true);
                stepsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");
                TitledPane stepsPane = new TitledPane("Приготовление", stepsLabel);
                stepsPane.setExpanded(false);

                Button orderBtn = new Button("Заказать все ингредиенты");
                orderBtn.getStyleClass().add("btn-primary");
                orderBtn.setOnAction(e -> orderFromRecipe(r));

                card.getChildren().addAll(title, meta, ingTitle, ingBox, stepsPane, orderBtn);
                recipesBox.getChildren().add(card);
            }
        }

        container.getChildren().addAll(recTitle, recBox, new Separator(), recTitle2, recipesBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    private void orderFromRecipe(Recipe recipe) {
        if (currentUser == null || currentUser.getUserId() <= 0) {
            app.showCustomLoginPrompt("Войдите, чтобы заказать продукты");
            return;
        }

        int added = 0;
        StringBuilder missing = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri.getStockQuantity() <= 0) {
                missing.append("• ").append(ri.getProductName()).append("\n");
                continue;
            }
            int qty = (int) Math.max(1, Math.ceil(ri.getQuantity()));
            if (cartDAO.addToCart(currentUser.getUserId(), ri.getProductId(), qty)) added++;
            else missing.append("• ").append(ri.getProductName()).append("\n");
        }

        String msg = "Добавлено ингредиентов: " + added;
        if (missing.length() > 0) msg += "\n\nНе добавлены (нет в наличии):\n" + missing;
        CustomAlert.show(dialogStage, "Рецепт", msg, CustomAlert.AlertType.INFO);
        if (onCartUpdated != null) onCartUpdated.run();
    }

    private VBox createReviewsTabForGuest() {
        VBox c = new VBox(15);
        c.setPadding(new Insets(10));

        reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(5));
        ScrollPane scrollPane = new ScrollPane(reviewsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox paginationBox = createPaginationControls();

        VBox guestBox = new VBox(15);
        guestBox.setAlignment(Pos.CENTER);
        guestBox.setPadding(new Insets(20));
        guestBox.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-background-radius: 10; -fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 10;");

        Label lbl = new Label("🔒 Войдите, чтобы оставить свой отзыв");
        lbl.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + "; -fx-font-size: 14px;");

        Button loginBtn = new Button("Войти");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setOnAction(e -> { dialogStage.close(); app.showLoginView(); });

        guestBox.getChildren().addAll(lbl, loginBtn);

        c.getChildren().addAll(scrollPane, paginationBox, guestBox);
        loadReviews();
        return c;
    }

    private VBox createReviewsTab() {
        VBox c = new VBox(15);
        reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(5));

        ScrollPane scrollPane = new ScrollPane(reviewsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox paginationBox = createPaginationControls();
        VBox addBox = createAddReviewBox();

        c.getChildren().addAll(scrollPane, paginationBox, addBox);
        loadReviews();
        return c;
    }

    private VBox createAddReviewBox() {
        VBox addBox = new VBox(10);
        addBox.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-padding: 15; -fx-background-radius: 10;");

        starRatingBox = new HBox(5);
        createStarRatingInput();

        TextArea comment = new TextArea();
        comment.setPromptText("Ваш отзыв...");
        comment.setWrapText(true);
        comment.setPrefHeight(80);

        HBox imageUploadBox = createImageUploadBox();
        Button submit = createSubmitButton(comment, imageUploadBox);

        addBox.getChildren().addAll(new Label("Ваша оценка:"), starRatingBox, comment, imageUploadBox, submit);
        return addBox;
    }

    private HBox createImageUploadBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Фото:");
        lbl.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Путь к изображению");
        imagePathField.setPrefWidth(200);
        imagePathField.setEditable(false);

        Button chooseBtn = new Button("Выбрать");
        chooseBtn.getStyleClass().add("btn-primary");
        chooseBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");

        final String[] selectedImagePath = {null};
        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(dialogStage);
            if (file != null) {
                String destPath = copyReviewImage(file);
                if (destPath != null) {
                    selectedImagePath[0] = destPath;
                    imagePathField.setText(destPath);
                }
            }
        });
        box.getChildren().addAll(lbl, imagePathField, chooseBtn);
        box.setUserData(selectedImagePath);
        return box;
    }

    private String copyReviewImage(File sourceFile) {
        try {
            ensureReviewImagesDirectoryExists();
            String ext = sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'));
            String uniqueName = "review_" + System.currentTimeMillis() + ext;

            Files.copy(sourceFile.toPath(), new File(REVIEW_IMAGES_DIR, uniqueName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourceFile.toPath(), new File(TARGET_REVIEW_IMAGES_DIR, uniqueName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return uniqueName;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to copy review image", ex);
            return null;
        }
    }

    private void ensureReviewImagesDirectoryExists() {
        new File(REVIEW_IMAGES_DIR).mkdirs();
        new File(TARGET_REVIEW_IMAGES_DIR).mkdirs();
    }

    private Button createSubmitButton(TextArea commentArea, HBox imageUploadBox) {
        Button submit = new Button("Отправить отзыв");
        submit.getStyleClass().add("btn-primary");
        submit.setPrefWidth(200);

        boolean hasReviewed = reviewDAO.hasUserReviewed(product.getId(), currentUser.getUserId());
        if (hasReviewed) {
            submit.setDisable(true);
            submit.setText("✓ Вы уже оставили отзыв");
            submit.setStyle("-fx-background-color: " + app.getCurrentSuccess() + "; -fx-text-fill: white;");
            return submit;
        }

        submit.setOnAction(e -> {
            if (selectedRating == 0) {
                CustomAlert.show(dialogStage, "Ошибка", "Поставьте оценку", CustomAlert.AlertType.ERROR);
                return;
            }
            if (commentArea.getText().trim().isEmpty()) {
                CustomAlert.show(dialogStage, "Ошибка", "Напишите отзыв", CustomAlert.AlertType.ERROR);
                return;
            }
            String imagePath = ((String[]) imageUploadBox.getUserData())[0];
            Review review = new Review(product.getId(), currentUser.getUserId(), currentUser.getUsername(), selectedRating, commentArea.getText().trim());
            review.setImagePath(imagePath);

            if (reviewDAO.addReview(review)) {
                CustomAlert.show(dialogStage, "Успех", "Отзыв добавлен", CustomAlert.AlertType.INFO);
                loadReviews();
                commentArea.clear();

                double newAvg = reviewDAO.getAverageRating(product.getId());
                product.setRating(newAvg);
                product.setAverageRating(newAvg);

                if (detailsRatingLabel != null) {
                    detailsRatingLabel.setText("⭐ Рейтинг: " + String.format("%.1f", product.getRating()));
                }

                app.updateProductsGrid();

                submit.setDisable(true);
                submit.setText("✓ Вы уже оставили отзыв");
                submit.setStyle("-fx-background-color: " + app.getCurrentSuccess() + "; -fx-text-fill: white;");
            }
        });
        return submit;
    }

    private HBox createPaginationControls() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);

        prevButton = new Button("◀ Предыдущие");
        prevButton.getStyleClass().add("btn-primary");
        prevButton.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");

        pageInfoLabel = new Label();
        pageInfoLabel.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        nextButton = new Button("Следующие ▶");
        nextButton.getStyleClass().add("btn-primary");
        nextButton.setStyle("-fx-font-size: 12px; -fx-padding: 6 12;");

        prevButton.setOnAction(e -> { if (currentPage > 0) { currentPage--; displayCurrentPageReviews(); } });
        nextButton.setOnAction(e -> {
            int maxPages = (int) Math.ceil((double) totalReviews / reviewsPerPage);
            if (currentPage < maxPages - 1) { currentPage++; displayCurrentPageReviews(); }
        });

        box.getChildren().addAll(prevButton, pageInfoLabel, nextButton);
        return box;
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
            Label empty = new Label("Пока нет отзывов");
            empty.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + ";");
            reviewsList.getChildren().add(empty);
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
        }
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + app.getCurrentBgLight() + "; -fx-background-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(review.getUserName());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        Label date = new Label(review.getCreatedAt().toString().substring(0, 10));
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(name, createStarsDisplay(review.getRating()), spacer, date);

        Label comment = new Label(review.getComment());
        comment.setWrapText(true);
        comment.setStyle("-fx-font-size: 13px; -fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        card.getChildren().addAll(header, comment);

        if (review.getImagePath() != null && !review.getImagePath().isEmpty()) {
            ImageView iv = new ImageView();
            iv.setFitWidth(100); iv.setFitHeight(100); iv.setPreserveRatio(true);
            try {
                InputStream stream = getClass().getResourceAsStream("/review_images/" + review.getImagePath());
                if (stream != null) iv.setImage(new Image(stream));
                else {
                    File file = new File("src/main/resources/review_images/" + review.getImagePath());
                    if (file.exists()) iv.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception ignored) {}
            card.getChildren().add(iv);
        }
        return card;
    }

    private void createStarRatingInput() {
        starRatingBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            final int v = i;
            Label star = new Label("☆");
            star.setStyle("-fx-text-fill: #d9b3c2; -fx-font-size: 24px; -fx-cursor: hand;");
            star.setOnMouseClicked(e -> {
                selectedRating = v;
                for (int j = 0; j < 5; j++) {
                    Label s = (Label) starRatingBox.getChildren().get(j);
                    s.setText(j < v ? "★" : "☆");
                    s.setStyle("-fx-text-fill: " + (j < v ? "#f4b942" : "#d9b3c2") + "; -fx-font-size: 24px; -fx-cursor: hand;");
                }
            });
            starRatingBox.getChildren().add(star);
        }
    }

    private HBox createStarsDisplay(double rating) {
        HBox box = new HBox(2);
        int full = (int) Math.clamp(Math.round(rating), 0, 5);
        for (int i = 0; i < full; i++) {
            Label s = new Label("★"); s.setStyle("-fx-text-fill: #f4b942; -fx-font-size: 14px;");
            box.getChildren().add(s);
        }
        for (int i = full; i < 5; i++) {
            Label s = new Label("☆"); s.setStyle("-fx-text-fill: #d9b3c2; -fx-font-size: 14px;");
            box.getChildren().add(s);
        }
        return box;
    }

    private Image loadProductImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                InputStream stream = getClass().getResourceAsStream("/images/" + imageUrl);
                if (stream != null) {
                    Image img = new Image(stream);
                    if (!img.isError()) return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}