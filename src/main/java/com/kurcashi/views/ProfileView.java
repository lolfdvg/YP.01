package com.kurcashi.views;

import com.kurcashi.models.User;
import com.kurcashi.models.Address;
import com.kurcashi.models.PaymentInfo;
import com.kurcashi.models.Order;
import com.kurcashi.models.Product;
import com.kurcashi.dao.AddressDAO;
import com.kurcashi.dao.PaymentDAO;
import com.kurcashi.dao.OrderDAO;
import com.kurcashi.dao.CartDAO;
import com.kurcashi.dao.ProductDAO;
import com.kurcashi.utils.CustomAlert;
import com.kurcashi.utils.PasswordUtils;
import com.example.kurcashi.BaitDesireApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileView {

    private static final Logger LOGGER = Logger.getLogger(ProfileView.class.getName());
    private static final String LABEL_ERROR = "Ошибка";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private User user;
    private AddressDAO addressDAO;
    private PaymentDAO paymentDAO;
    private OrderDAO orderDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private Runnable onProfileUpdated;
    private VBox view;
    private Stage parentStage;
    private BaitDesireApp app;
    private Scene parentScene;
    private Effect originalEffect;

    private FlowPane ordersFlow;
    private Label ordersCountLabel;

    public ProfileView(User user, PaymentDAO paymentDAO, Runnable onProfileUpdated, Stage parentStage, BaitDesireApp app) {
        this.user = user;
        this.addressDAO = new AddressDAO();
        this.paymentDAO = paymentDAO;
        this.orderDAO = new OrderDAO();
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
        this.onProfileUpdated = onProfileUpdated;
        this.parentStage = parentStage;
        this.app = app;
        if (parentStage != null && parentStage.getScene() != null) {
            this.parentScene = parentStage.getScene();
        }
        createView();
    }

    private void createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25, 30, 30, 30));
        root.getStyleClass().add("profile-root");

        HBox header = createProfileHeader();
        TabPane tabs = createTabs();

        root.getChildren().addAll(header, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        ScrollPane scrollWrapper = new ScrollPane(root);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.getStyleClass().add("profile-scroll");
        scrollWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        view = new VBox(scrollWrapper);
        view.getStyleClass().add("profile-root");
    }

    // ==================== ШАПКА ====================

    private HBox createProfileHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.getStyleClass().add("profile-header");

        StackPane avatarBox = new StackPane();
        avatarBox.setPrefSize(70, 70);
        avatarBox.setMaxSize(70, 70);
        avatarBox.getStyleClass().add("profile-avatar");

        Label initial = new Label(
                user.getUsername() != null && !user.getUsername().isEmpty()
                        ? user.getUsername().substring(0, 1).toUpperCase()
                        : "?"
        );
        initial.getStyleClass().add("profile-avatar-initial");
        avatarBox.getChildren().add(initial);

        VBox nameBlock = new VBox(4);
        Label nameLabel = new Label(user.getUsername() + (user.getLastName() != null && !user.getLastName().isEmpty() ? " " + user.getLastName() : ""));
        nameLabel.getStyleClass().add("profile-name");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("profile-email");

        Label roleLabel = new Label(user.isAdmin() ? "⚙️ Администратор" : "👤 Покупатель");
        roleLabel.getStyleClass().add(user.isAdmin() ? "profile-role-admin" : "profile-role-user");

        nameBlock.getChildren().addAll(nameLabel, emailLabel, roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox bonusCard = new VBox(2);
        bonusCard.setAlignment(Pos.CENTER);
        bonusCard.setPadding(new Insets(10, 20, 10, 20));
        bonusCard.getStyleClass().add("profile-bonus-card");

        Label bonusIcon = new Label("🎁");
        bonusIcon.getStyleClass().add("profile-bonus-icon");

        Label bonusValue = new Label(String.valueOf(user.getBonusPoints()));
        bonusValue.getStyleClass().add("profile-bonus-value");

        Label bonusCaption = new Label("бонусов");
        bonusCaption.getStyleClass().add("profile-bonus-caption");

        bonusCard.getChildren().addAll(bonusIcon, bonusValue, bonusCaption);

        Button backButton = new Button("← Назад");
        backButton.getStyleClass().add("btn-primary");
        backButton.setOnAction(e -> {
            if (onProfileUpdated != null) onProfileUpdated.run();
        });

        header.getChildren().addAll(avatarBox, nameBlock, spacer, bonusCard, backButton);
        return header;
    }

    // ==================== ВКЛАДКИ ====================

    private TabPane createTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("dialog-tab-pane");

        Tab personalTab = new Tab("👤 Личные данные");
        personalTab.setContent(createPersonalTab());
        personalTab.setClosable(false);

        Tab deliveryTab = new Tab("Доставка и оплата");
        deliveryTab.setContent(createDeliveryTab());
        deliveryTab.setClosable(false);

        Tab ordersTab = new Tab("История заказов");
        ordersTab.setContent(createOrdersTab());
        ordersTab.setClosable(false);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == ordersTab) refreshOrdersGrid();
        });

        tabs.getTabs().addAll(personalTab, deliveryTab, ordersTab);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        return tabs;
    }

    private VBox createPersonalTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 5, 5, 5));

        HBox row = new HBox(20);
        VBox profileCard = createProfileCard();
        VBox passwordCard = createPasswordCard();
        HBox.setHgrow(profileCard, Priority.ALWAYS);
        HBox.setHgrow(passwordCard, Priority.ALWAYS);
        profileCard.setMaxWidth(Double.MAX_VALUE);
        passwordCard.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(profileCard, passwordCard);
        content.getChildren().add(row);
        return content;
    }

    private VBox createDeliveryTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 5, 5, 5));

        HBox row = new HBox(20);
        VBox addressCard = createAddressCard();
        VBox paymentCard = createPaymentCard();
        HBox.setHgrow(addressCard, Priority.ALWAYS);
        HBox.setHgrow(paymentCard, Priority.ALWAYS);
        addressCard.setMaxWidth(Double.MAX_VALUE);
        paymentCard.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(addressCard, paymentCard);
        content.getChildren().add(row);
        return content;
    }

    private VBox createOrdersTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 5, 5, 5));

        ordersCountLabel = new Label();
        ordersCountLabel.getStyleClass().add("profile-count");

        ordersFlow = new FlowPane(20, 20);
        ordersFlow.setAlignment(Pos.TOP_LEFT);

        ScrollPane ordersScrollPane = new ScrollPane(ordersFlow);
        ordersScrollPane.setFitToWidth(true);
        ordersScrollPane.getStyleClass().add("profile-scroll");
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(ordersScrollPane, Priority.ALWAYS);

        refreshOrdersGrid();

        content.getChildren().addAll(ordersCountLabel, ordersScrollPane);
        return content;
    }

    private void refreshOrdersGrid() {
        if (ordersFlow == null) return;
        ordersFlow.getChildren().clear();

        List<Order> orders = orderDAO.getOrdersByUserId(user.getUserId());
        if (ordersCountLabel != null) {
            ordersCountLabel.setText("Всего заказов: " + orders.size());
        }

        if (orders.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));

            Label icon = new Label("📦");
            icon.getStyleClass().add("empty-orders-icon");

            Label emptyLabel = new Label("У вас пока нет заказов");
            emptyLabel.getStyleClass().add("empty-orders-text");

            Button goShopping = new Button("Перейти к каталогу");
            goShopping.getStyleClass().add("btn-primary");
            goShopping.setOnAction(e -> {
                if (onProfileUpdated != null) onProfileUpdated.run();
            });

            emptyBox.getChildren().addAll(icon, emptyLabel, goShopping);
            ordersFlow.getChildren().add(emptyBox);
            return;
        }

        for (Order order : orders) {
            ordersFlow.getChildren().add(createOrderCard(order));
        }
    }

    // ==================== КАРТОЧКИ СЕКЦИЙ ====================

    private VBox createProfileCard() {
        VBox card = createCard("Личные данные");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        Label nameLabel = new Label("Имя:");
        nameLabel.getStyleClass().add("profile-form-label");
        TextField nameField = new TextField(user.getUsername());
        nameField.setMaxWidth(Double.MAX_VALUE);

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("profile-form-label");
        TextField emailField = new TextField(user.getEmail());
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label phoneLabel = new Label("Телефон:");
        phoneLabel.getStyleClass().add("profile-form-label");
        TextField phoneField = new TextField(user.getPhone());
        phoneField.setMaxWidth(Double.MAX_VALUE);

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(phoneLabel, 0, 2);
        grid.add(phoneField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(75);
        grid.getColumnConstraints().addAll(col1, col2);

        Button saveButton = new Button("Сохранить изменения");
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> {
            if (nameField.getText() != null && !nameField.getText().trim().isEmpty()) {
                user.setUsername(nameField.getText().trim());
            }
            if (emailField.getText() != null && !emailField.getText().trim().isEmpty()) {
                user.setEmail(emailField.getText().trim());
            }
            if (phoneField.getText() != null && !phoneField.getText().trim().isEmpty()) {
                user.setPhone(phoneField.getText().trim());
            }

            if (user.saveToDatabase()) {
                CustomAlert.show(parentStage, "Успех", "Данные профиля успешно обновлены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(parentStage, LABEL_ERROR, "Не удалось обновить данные профиля", CustomAlert.AlertType.ERROR);
            }
        });

        card.getChildren().addAll(grid, saveButton);
        return card;
    }

    private VBox createPasswordCard() {
        VBox card = createCard("Смена пароля");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        Label oldLabel = new Label("Текущий:");
        oldLabel.getStyleClass().add("profile-form-label");
        PasswordField oldField = new PasswordField();
        oldField.setPromptText("Введите текущий пароль");
        oldField.setMaxWidth(Double.MAX_VALUE);

        Label newLabel = new Label("Новый:");
        newLabel.getStyleClass().add("profile-form-label");
        PasswordField newField = new PasswordField();
        newField.setPromptText("От 6 до 12 символов");
        newField.setMaxWidth(Double.MAX_VALUE);

        Label confirmLabel = new Label("Повтор:");
        confirmLabel.getStyleClass().add("profile-form-label");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Повторите новый пароль");
        confirmField.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("form-error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        grid.add(oldLabel, 0, 0);
        grid.add(oldField, 1, 0);
        grid.add(newLabel, 0, 1);
        grid.add(newField, 1, 1);
        grid.add(confirmLabel, 0, 2);
        grid.add(confirmField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(75);
        grid.getColumnConstraints().addAll(col1, col2);

        Button changePasswordButton = new Button("Сменить пароль");
        changePasswordButton.getStyleClass().add("btn-primary");
        changePasswordButton.setMaxWidth(Double.MAX_VALUE);
        changePasswordButton.setOnAction(e -> {
            String oldPassword = oldField.getText();
            String newPassword = newField.getText();
            String confirmPassword = confirmField.getText();

            errorLabel.setVisible(false);
            errorLabel.setText("");

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("❌ Пожалуйста, заполните все поля");
                errorLabel.setVisible(true);
                return;
            }
            if (newPassword.contains(" ")) {
                errorLabel.setText("❌ Пароль не должен содержать пробелы");
                errorLabel.setVisible(true);
                return;
            }
            if (newPassword.length() < 6) {
                errorLabel.setText("❌ Новый пароль должен содержать минимум 6 символов");
                errorLabel.setVisible(true);
                return;
            }
            if (newPassword.length() > 12) {
                errorLabel.setText("❌ Новый пароль не должен превышать 12 символов");
                errorLabel.setVisible(true);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("❌ Новый пароль и подтверждение не совпадают");
                errorLabel.setVisible(true);
                return;
            }
            if (!verifyOldPassword(oldPassword)) {
                errorLabel.setText("❌ Неверный текущий пароль");
                errorLabel.setVisible(true);
                return;
            }
            if (changePassword(newPassword)) {
                CustomAlert.show(parentStage, "Успех", "Пароль успешно изменен!", CustomAlert.AlertType.SUCCESS);
                oldField.clear();
                newField.clear();
                confirmField.clear();
            } else {
                errorLabel.setText("❌ Ошибка при смене пароля. Попробуйте позже.");
                errorLabel.setVisible(true);
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(grid, errorLabel, changePasswordButton);
        card.getChildren().add(content);
        return card;
    }

    private VBox createAddressCard() {
        VBox card = createCard("Адрес доставки");
        Address address = addressDAO.getDefaultAddress(user.getUserId());

        Label addressLabel = new Label();
        addressLabel.setWrapText(true);
        addressLabel.getStyleClass().add("profile-hint");

        if (address != null) {
            addressLabel.setText(address.getFullAddress());
        } else {
            addressLabel.setText("Адрес пока не указан");
        }

        Button editAddressButton = new Button("Изменить адрес");
        editAddressButton.getStyleClass().add("btn-primary");
        editAddressButton.setMaxWidth(Double.MAX_VALUE);
        editAddressButton.setOnAction(e -> {
            AddressDialog addressDialog = new AddressDialog(user, parentStage != null ? parentStage : new Stage(), app);
            Address newAddress = addressDialog.showAndWait();
            if (newAddress != null && onProfileUpdated != null) {
                onProfileUpdated.run();
            }
        });

        card.getChildren().addAll(addressLabel, editAddressButton);
        return card;
    }

    private VBox createPaymentCard() {
        VBox card = createCard("💳 Данные карты");
        PaymentInfo paymentInfo = paymentDAO.getPaymentInfo(user.getUserId());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        Label cardNumberLabel = new Label("Номер:");
        cardNumberLabel.getStyleClass().add("profile-form-label");
        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("XXXX XXXX XXXX XXXX");
        cardNumberField.setMaxWidth(Double.MAX_VALUE);

        Label expiryLabel = new Label("Срок:");
        expiryLabel.getStyleClass().add("profile-form-label");
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        expiryField.setMaxWidth(Double.MAX_VALUE);

        Label cvvLabel = new Label("CVV:");
        cvvLabel.getStyleClass().add("profile-form-label");
        PasswordField cvvField = new PasswordField();
        cvvField.setPromptText("XXX");
        cvvField.setMaxWidth(Double.MAX_VALUE);

        if (paymentInfo != null) {
            cardNumberField.setText(PaymentInfo.formatCardNumber(paymentInfo.getCardNumber()));
            expiryField.setText(paymentInfo.getExpiryDate());
            cvvField.setText(paymentInfo.getCvv());
        }

        grid.add(cardNumberLabel, 0, 0);
        grid.add(cardNumberField, 1, 0);
        grid.add(expiryLabel, 0, 1);
        grid.add(expiryField, 1, 1);
        grid.add(cvvLabel, 0, 2);
        grid.add(cvvField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(80);
        grid.getColumnConstraints().addAll(col1, col2);

        Label validationLabel = new Label();
        validationLabel.getStyleClass().add("form-validation-label");
        validationLabel.setWrapText(true);
        validationLabel.setVisible(false);

        Button saveCardButton = new Button("Сохранить карту");
        saveCardButton.getStyleClass().add("btn-primary");
        saveCardButton.setMaxWidth(Double.MAX_VALUE);
        saveCardButton.setOnAction(e -> {
            String cardNumber = cardNumberField.getText().replaceAll("[\\s-]", "");
            String expiryDate = expiryField.getText();
            String cvv = cvvField.getText();

            validationLabel.setVisible(true);

            if (!PaymentInfo.isValidCardNumber(cardNumber)) {
                validationLabel.setText("❌ Неверный номер карты");
                validationLabel.getStyleClass().remove("form-success-label");
                if (!validationLabel.getStyleClass().contains("form-error-label")) {
                    validationLabel.getStyleClass().add("form-error-label");
                }
                return;
            }
            if (!PaymentInfo.isValidExpiryDate(expiryDate)) {
                validationLabel.setText("❌ Неверный срок действия");
                return;
            }
            if (!PaymentInfo.isValidCVV(cvv)) {
                validationLabel.setText("❌ Неверный CVV");
                return;
            }

            validationLabel.setText("✓ Данные карты прошли проверку");
            validationLabel.getStyleClass().remove("form-error-label");
            if (!validationLabel.getStyleClass().contains("form-success-label")) {
                validationLabel.getStyleClass().add("form-success-label");
            }

            PaymentInfo newPaymentInfo = new PaymentInfo(user.getUserId(), cardNumber, expiryDate, cvv);
            if (paymentDAO.savePaymentInfo(newPaymentInfo)) {
                CustomAlert.show(parentStage, "Успех", "Данные карты успешно сохранены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(parentStage, LABEL_ERROR, "Не удалось сохранить данные карты", CustomAlert.AlertType.ERROR);
            }
        });

        Button deleteCardButton = new Button("🗑️ Удалить");
        deleteCardButton.getStyleClass().add("btn-danger");
        deleteCardButton.setOnAction(e -> {
            if (paymentDAO.deletePaymentInfo(user.getUserId())) {
                cardNumberField.clear();
                expiryField.clear();
                cvvField.clear();
                validationLabel.setText("");
                validationLabel.setVisible(false);
                CustomAlert.show(parentStage, "Успех", "Данные карты удалены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(parentStage, LABEL_ERROR, "Не удалось удалить данные карты", CustomAlert.AlertType.ERROR);
            }
        });

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(grid, validationLabel);

        HBox buttonBox = new HBox(10);
        HBox.setHgrow(saveCardButton, Priority.ALWAYS);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveCardButton, deleteCardButton);

        card.getChildren().addAll(infoBox, buttonBox);
        return card;
    }

    // ==================== КАРТОЧКА ЗАКАЗА ====================

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.getStyleClass().add("order-card");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("Заказ №" + order.getOrderId());
        orderIdLabel.getStyleClass().add("order-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(order.getCreatedAt() != null ? order.getCreatedAt().toString().substring(0, 10) : "");
        dateLabel.getStyleClass().add("order-date");

        headerBox.getChildren().addAll(orderIdLabel, spacer, dateLabel);

        Label statusBadge = new Label(getStatusText(order.getStatus()));
        statusBadge.getStyleClass().addAll("order-status-badge", getStatusClass(order.getStatus()));

        Label amountLabel = new Label(String.format("%.2f ₽", order.getTotalAmount()));
        amountLabel.getStyleClass().add("order-amount");

        Label paymentLabel = new Label("💳 " + (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD") ? "Карта" : "Наличные"));
        paymentLabel.getStyleClass().add("order-payment");

        Label addressLabel = new Label("📍 " + (order.getAddress() != null && !order.getAddress().isEmpty() ? order.getAddress() : "не указан"));
        addressLabel.getStyleClass().add("order-address");
        addressLabel.setWrapText(true);
        addressLabel.setMaxWidth(280);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER);

        Button detailsButton = new Button("Детали");
        detailsButton.getStyleClass().addAll("btn-primary", "btn-small");
        HBox.setHgrow(detailsButton, Priority.ALWAYS);
        detailsButton.setMaxWidth(Double.MAX_VALUE);
        detailsButton.setOnAction(e -> showOrderDetails(order));

        Button repeatButton = new Button("Повторить");
        repeatButton.getStyleClass().add("btn-outline");
        HBox.setHgrow(repeatButton, Priority.ALWAYS);
        repeatButton.setMaxWidth(Double.MAX_VALUE);
        repeatButton.setOnAction(e -> repeatOrder(order));

        buttonsBox.getChildren().addAll(detailsButton, repeatButton);

        card.getChildren().addAll(headerBox, statusBadge, amountLabel, paymentLabel, addressLabel, buttonsBox);
        return card;
    }

    private String getStatusClass(String status) {
        return switch (status) {
            case "PENDING" -> "order-status-pending";
            case "PAID" -> "order-status-paid";
            case "SHIPPED" -> "order-status-shipped";
            case STATUS_DELIVERED -> "order-status-delivered";
            case STATUS_CANCELLED -> "order-status-cancelled";
            default -> "order-status-pending";
        };
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Ожидает оплаты";
            case "PAID" -> "Оплачен";
            case "SHIPPED" -> "Отправлен";
            case STATUS_DELIVERED -> "Доставлен";
            case STATUS_CANCELLED -> "Отменен";
            default -> status;
        };
    }

    // ==================== ПОВТОР ЗАКАЗА ====================

    private void repeatOrder(Order order) {
        LOGGER.info("=== НАЧАЛО ПОВТОРЕНИЯ ЗАКАЗА ===");
        if (order == null) {
            CustomAlert.show(parentStage, LABEL_ERROR, "Заказ не найден", CustomAlert.AlertType.ERROR);
            return;
        }

        String sql = "SELECT product_id, product_name, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, order.getOrderId());
            ResultSet rs = pstmt.executeQuery();
            int itemsAdded = 0;
            List<String> unavailableProducts = new ArrayList<>();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                String productName = rs.getString("product_name");

                Product product = productDAO.getProductById(productId);
                if (product != null && product.getStockQuantity() >= quantity) {
                    if (cartDAO.addToCart(user.getUserId(), productId, quantity)) {
                        itemsAdded++;
                    } else {
                        unavailableProducts.add(productName);
                    }
                } else {
                    unavailableProducts.add(productName);
                }
            }

            if (itemsAdded == 0 && unavailableProducts.isEmpty()) {
                CustomAlert.show(parentStage, "Внимание", "Заказ не содержит товаров для повторения", CustomAlert.AlertType.INFO);
            } else if (itemsAdded > 0) {
                String message = itemsAdded + " товар(ов) добавлен(ы) в корзину";
                if (!unavailableProducts.isEmpty()) {
                    message += "\nНет в наличии: " + String.join(", ", unavailableProducts);
                }
                CustomAlert.show(parentStage, "Успех", message, CustomAlert.AlertType.SUCCESS);
            } else {
                CustomAlert.show(parentStage, LABEL_ERROR, "Ни один товар не добавлен. Нет в наличии: " + String.join(", ", unavailableProducts), CustomAlert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error repeating order", e);
            CustomAlert.show(parentStage, LABEL_ERROR, "Ошибка при добавлении товаров в корзину: " + e.getMessage(), CustomAlert.AlertType.ERROR);
        }
    }

    // ==================== ДЕТАЛИ ЗАКАЗА ====================

    private void showOrderDetails(Order order) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        applyBlurEffect();
        dialogStage.setOnCloseRequest(e -> removeBlurEffect());
        dialogStage.setOnHidden(e -> removeBlurEffect());

        VBox dialogBox = new VBox(15);
        dialogBox.setPadding(new Insets(20));
        dialogBox.setPrefWidth(550);
        dialogBox.getStyleClass().add("order-detail-dialog");

        // Заголовок
        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Детали заказа №" + order.getOrderId());
        titleLabel.getStyleClass().add("order-detail-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button closeIcon = new Button("✕");
        closeIcon.getStyleClass().add("btn-icon-close");
        closeIcon.setOnAction(e -> dialogStage.close());

        titleBar.getChildren().addAll(titleLabel, closeIcon);

        // Информация
        Label dateLabel = new Label("Дата: " + (order.getCreatedAt() != null ? order.getCreatedAt().toString().substring(0, 19) : ""));
        dateLabel.getStyleClass().add("order-detail-info");

        Label statusLabel = new Label("Статус: " + getStatusText(order.getStatus()));
        statusLabel.getStyleClass().addAll("order-detail-status", getStatusClass(order.getStatus()));

        Label amountLabel = new Label("Сумма: " + String.format("%.2f ₽", order.getTotalAmount()));
        amountLabel.getStyleClass().add("order-detail-amount");

        Label paymentLabel = new Label("Оплата: " + (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD") ? "Банковской картой" : "Наличными"));
        paymentLabel.getStyleClass().add("order-detail-info");

        Label addressLabel = new Label("Адрес доставки: " + (order.getAddress() != null && !order.getAddress().isEmpty() ? order.getAddress() : "Не указан"));
        addressLabel.getStyleClass().add("order-detail-info");
        addressLabel.setWrapText(true);

        Separator separator = new Separator();

        Label itemsTitle = new Label("🛒 Товары в заказе:");
        itemsTitle.getStyleClass().add("order-detail-title");
        itemsTitle.setStyle(null); // очищаем inline, если был — используем класс
        itemsTitle.getStyleClass().remove("order-detail-title");
        itemsTitle.getStyleClass().add("profile-form-label");

        VBox itemsList = new VBox(8);
        itemsList.setPadding(new Insets(10));
        itemsList.getStyleClass().add("order-items-container");
        loadOrderItems(order.getOrderId(), itemsList);

        ScrollPane scrollPane = new ScrollPane(itemsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.getStyleClass().add("profile-scroll");

        // Кнопки
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button repeatButton = new Button("Повторить заказ");
        repeatButton.getStyleClass().add("btn-primary");
        repeatButton.setOnAction(e -> {
            repeatOrder(order);
            dialogStage.close();
        });

        Button closeButton = new Button("Закрыть");
        closeButton.getStyleClass().add("btn-outline-secondary");
        closeButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(repeatButton, closeButton);

        dialogBox.getChildren().addAll(titleBar, dateLabel, statusLabel, amountLabel, paymentLabel, addressLabel,
                separator, itemsTitle, scrollPane, buttonBox);

        Scene scene = new Scene(dialogBox);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        URL css = getClass().getResource("/styles/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        dialogStage.setScene(scene);
        if (parentStage != null) {
            dialogStage.setOnShown(e -> {
                dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - 550) / 2);
                dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - 600) / 2);
            });
        }
        dialogStage.showAndWait();
    }

    private void loadOrderItems(int orderId, VBox container) {
        String sql = "SELECT product_name, price, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                double total = price * quantity;

                HBox itemBox = new HBox(10);
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setPadding(new Insets(5, 10, 5, 10));

                Label nameLabel = new Label("🐟 " + productName);
                nameLabel.getStyleClass().add("order-item-name");
                nameLabel.setPrefWidth(200);

                Label qtyLabel = new Label(quantity + " кг");
                qtyLabel.getStyleClass().add("order-item-qty");
                qtyLabel.setPrefWidth(80);
                qtyLabel.setAlignment(Pos.CENTER);

                Label priceLabel = new Label(String.format("%.2f ₽/кг", price));
                priceLabel.getStyleClass().add("order-item-price");
                priceLabel.setPrefWidth(100);
                priceLabel.setAlignment(Pos.CENTER_RIGHT);

                Label totalLabel = new Label(String.format("%.2f ₽", total));
                totalLabel.getStyleClass().add("order-item-total");
                totalLabel.setPrefWidth(100);
                totalLabel.setAlignment(Pos.CENTER_RIGHT);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                itemBox.getChildren().addAll(nameLabel, spacer, qtyLabel, priceLabel, totalLabel);
                container.getChildren().add(itemBox);
            }
            if (container.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Нет информации о товарах");
                emptyLabel.getStyleClass().add("empty-order-items");
                emptyLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading order items", e);
            Label errorLabel = new Label("Ошибка загрузки данных");
            errorLabel.getStyleClass().add("form-error-label");
            container.getChildren().add(errorLabel);
        }
    }

    // ==================== ХЕЛПЕРЫ ====================

    private boolean verifyOldPassword(String oldPassword) {
        String query = "SELECT password_hash FROM customers WHERE customer_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, user.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return PasswordUtils.verifyPassword(oldPassword, storedHash);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verifying old password", e);
        }
        return false;
    }

    private boolean changePassword(String newPassword) {
        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        String query = "UPDATE customers SET password_hash = ? WHERE customer_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, user.getUserId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                user.setPassword(newPassword);
                updateSavedSession(newPassword);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error changing password", e);
        }
        return false;
    }

    private void updateSavedSession(String newPassword) {
        String query = "UPDATE saved_sessions SET password = ? WHERE customer_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, user.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating saved session", e);
        }
    }

    private void applyBlurEffect() {
        if (parentScene != null && parentScene.getRoot() != null) {
            originalEffect = parentScene.getRoot().getEffect();
            parentScene.getRoot().setEffect(new BoxBlur(15, 15, 3));
        }
    }

    private void removeBlurEffect() {
        if (parentScene != null && parentScene.getRoot() != null) {
            parentScene.getRoot().setEffect(originalEffect);
        }
    }

    private VBox createCard(String title) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("profile-section");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("profile-section-title");
        card.getChildren().add(titleLabel);
        return card;
    }

    public VBox getView() {
        return view;
    }
}