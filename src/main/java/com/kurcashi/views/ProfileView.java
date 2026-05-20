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
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileView {

    // Константы для дублированных строковых литералов
    private static final String STYLE_BG_CARD = "-fx-background-color: ";
    private static final String STYLE_BG_RADIUS_12 = "; -fx-background-radius: 12; ";
    private static final String STYLE_EFFECT_SHADOW = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 3); ";
    private static final String STYLE_BORDER = "-fx-border-color: ";
    private static final String STYLE_BORDER_RADIUS_12 = "; -fx-border-radius: 12; -fx-border-width: 1;";
    private static final String STYLE_TEXT_FILL = "-fx-text-fill: ";
    private static final String STYLE_FONT_SIZE_11 = "; -fx-font-size: 11px;";
    private static final String STYLE_FONT_SIZE_12 = "; -fx-font-size: 12px;";
    private static final String STYLE_FONT_SIZE_13 = "; -fx-font-size: 13px;";
    private static final String STYLE_FONT_SIZE_14 = "; -fx-font-size: 14px;";
    private static final String STYLE_FONT_SIZE_16 = "; -fx-font-size: 16px;";
    private static final String STYLE_FONT_SIZE_20 = "; -fx-font-size: 20px;";
    private static final String STYLE_FONT_WEIGHT_BOLD = "; -fx-font-weight: bold;";
    private static final String STYLE_PADDING_6_12 = "; -fx-padding: 6 12;";
    private static final String STYLE_BG_RADIUS_6 = "; -fx-background-radius: 6;";
    private static final String STYLE_CURSOR_HAND = "; -fx-cursor: hand;";
    private static final String STYLE_BG_TRANSPARENT_BOTH = "-fx-background-color: transparent; -fx-background: transparent;";
    private static final String STYLE_TEXT_FILL_WHITE = STYLE_TEXT_FILL + "white;";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String MESSAGE_SUCCESS = "Успех";
    private static final String MESSAGE_ERROR = "Ошибка";
    private static final String DEFAULT_ADDRESS_TEXT = "Адрес не указан";
    private static final String NO_ORDERS_TEXT = "У вас пока нет заказов";

    private static final Logger logger = Logger.getLogger(ProfileView.class.getName());

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

    private GridPane ordersGrid;
    private ScrollPane ordersScrollPane;

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
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");

        Label titleLabel = new Label("👤 Профиль пользователя");
        titleLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + "; -fx-font-size: 28px; " + STYLE_FONT_WEIGHT_BOLD);

        VBox profileCard = createProfileCard();
        VBox bonusCard = createBonusCard();
        VBox passwordCard = createPasswordCard();
        VBox addressCard = createAddressCard();
        VBox paymentCard = createPaymentCard();
        VBox ordersCard = createOrdersCard();

        Button backButton = new Button("← Назад к каталогу");
        app.updateButtonStyle(backButton);
        backButton.setOnAction(e -> {
            if (onProfileUpdated != null) {
                onProfileUpdated.run();
            }
        });

        HBox buttonBox = new HBox(backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        content.getChildren().addAll(titleLabel, profileCard, bonusCard, passwordCard, addressCard, paymentCard, ordersCard, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(700);
        scrollPane.getStyleClass().add("edge-to-edge");

        view = new VBox(scrollPane);
        view.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + ";");
    }

    private VBox createBonusCard() {
        VBox card = createCard("🎁 Бонусная программа");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label bonusLabel = new Label("💰 Доступно бонусов:");
        bonusLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);

        Label bonusValueLabel = new Label(String.valueOf(user.getBonusPoints()));
        bonusValueLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_20 + STYLE_FONT_WEIGHT_BOLD);

        Label bonusRubLabel = new Label("баллов (1 балл = 1 рубль)");
        bonusRubLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);

        HBox bonusBox = new HBox(10);
        bonusBox.setAlignment(Pos.CENTER_LEFT);
        bonusBox.getChildren().addAll(bonusLabel, bonusValueLabel, bonusRubLabel);

        Label infoLabel = new Label("✨ За каждый заказ вы получаете 10% бонусами от суммы покупки!");
        infoLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        infoLabel.setWrapText(true);

        grid.add(bonusBox, 0, 0);
        grid.add(infoLabel, 0, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(100);
        grid.getColumnConstraints().add(col1);

        card.getChildren().addAll(grid);
        return card;
    }

    private VBox createProfileCard() {
        VBox card = createCard("Личные данные");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label nameLabel = new Label("Имя:");
        nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        TextField nameField = new TextField(user.getUsername());
        app.updateTextFieldStyle(nameField);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        TextField emailField = new TextField(user.getEmail());
        app.updateTextFieldStyle(emailField);

        Label phoneLabel = new Label("Телефон:");
        phoneLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        TextField phoneField = new TextField(user.getPhone());
        app.updateTextFieldStyle(phoneField);

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(phoneLabel, 0, 2);
        grid.add(phoneField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);

        Button saveButton = new Button("💾 Сохранить изменения");
        app.updateButtonStyle(saveButton);
        saveButton.setStyle(saveButton.getStyle() + "-fx-background-color: " + app.getCurrentSuccess() + ";");
        saveButton.setOnAction(e -> {
            String newName = nameField.getText();
            String newEmail = emailField.getText();
            String newPhone = phoneField.getText();

            if (newName != null && !newName.trim().isEmpty()) {
                user.setUsername(newName);
            }
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                user.setEmail(newEmail);
            }
            if (newPhone != null && !newPhone.trim().isEmpty()) {
                user.setPhone(newPhone);
            }

            if (user.saveToDatabase()) {
                CustomAlert.show(null, MESSAGE_SUCCESS, "Данные профиля успешно обновлены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(null, MESSAGE_ERROR, "Не удалось обновить данные профиля", CustomAlert.AlertType.ERROR);
            }
        });

        card.getChildren().addAll(grid, saveButton);
        return card;
    }

    private VBox createPasswordCard() {
        VBox card = createCard("🔒 Смена пароля");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label oldPasswordLabel = new Label("Старый пароль:");
        oldPasswordLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Введите текущий пароль");
        app.updateTextFieldStyle(oldPasswordField);

        Label newPasswordLabel = new Label("Новый пароль:");
        newPasswordLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("От 6 до 12 символов");
        app.updateTextFieldStyle(newPasswordField);

        Label confirmPasswordLabel = new Label("Подтвердите пароль:");
        confirmPasswordLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Повторите новый пароль");
        app.updateTextFieldStyle(confirmPasswordField);

        Label errorLabel = new Label();
        errorLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_12);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        grid.add(oldPasswordLabel, 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(newPasswordLabel, 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(confirmPasswordLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);

        Button changePasswordButton = createChangePasswordButton(oldPasswordField, newPasswordField, confirmPasswordField, errorLabel);

        VBox passwordContent = new VBox(10);
        passwordContent.getChildren().addAll(grid, errorLabel, changePasswordButton);

        card.getChildren().add(passwordContent);
        return card;
    }

    private Button createChangePasswordButton(PasswordField oldPasswordField, PasswordField newPasswordField,
                                              PasswordField confirmPasswordField, Label errorLabel) {
        Button changePasswordButton = new Button("🔑 Сменить пароль");
        app.updateButtonStyle(changePasswordButton);
        changePasswordButton.setOnAction(e -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            errorLabel.setVisible(false);
            errorLabel.setText("");

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError(errorLabel, "❌ Пожалуйста, заполните все поля");
                return;
            }
            if (newPassword.contains(" ")) {
                showError(errorLabel, "❌ Пароль не должен содержать пробелы");
                return;
            }
            if (newPassword.length() < 6) {
                showError(errorLabel, "❌ Новый пароль должен содержать минимум 6 символов");
                return;
            }
            if (newPassword.length() > 12) {
                showError(errorLabel, "❌ Новый пароль не должен превышать 12 символов");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showError(errorLabel, "❌ Новый пароль и подтверждение не совпадают");
                return;
            }
            if (!verifyOldPassword(oldPassword)) {
                showError(errorLabel, "❌ Неверный текущий пароль");
                return;
            }

            if (changePassword(newPassword)) {
                CustomAlert.show(null, MESSAGE_SUCCESS, "Пароль успешно изменен!", CustomAlert.AlertType.SUCCESS);
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                errorLabel.setText("");
                errorLabel.setVisible(false);
            } else {
                showError(errorLabel, "❌ Ошибка при смене пароля. Попробуйте позже.");
            }
        });
        return changePasswordButton;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

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
            logger.log(Level.SEVERE, "Error verifying old password: {0}", e.getMessage());
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
            logger.log(Level.SEVERE, "Error changing password: {0}", e.getMessage());
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
            logger.log(Level.WARNING, "Error updating saved session: {0}", e.getMessage());
        }
    }

    private VBox createAddressCard() {
        VBox card = createCard("📍 Адрес доставки");

        Address address = addressDAO.getDefaultAddress(user.getUserId());

        Label addressLabel = new Label();
        addressLabel.setWrapText(true);
        addressLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);

        if (address != null) {
            addressLabel.setText("Текущий адрес:\n" + address.getFullAddress());
        } else {
            addressLabel.setText(DEFAULT_ADDRESS_TEXT);
        }

        Button editAddressButton = new Button("✏️ Редактировать адрес");
        app.updateButtonStyle(editAddressButton);
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
        grid.setPadding(new Insets(10));

        Label cardNumberLabel = new Label("Номер карты:");
        cardNumberLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("XXXX XXXX XXXX XXXX");
        app.updateTextFieldStyle(cardNumberField);

        Label expiryLabel = new Label("Срок действия:");
        expiryLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        app.updateTextFieldStyle(expiryField);

        Label cvvLabel = new Label("CVV:");
        cvvLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_WEIGHT_BOLD);
        PasswordField cvvField = new PasswordField();
        cvvField.setPromptText("XXX");
        app.updateTextFieldStyle(cvvField);

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
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);

        Label validationLabel = new Label();
        validationLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_11);
        validationLabel.setWrapText(true);
        validationLabel.setVisible(false);

        Button saveCardButton = new Button("💳 Сохранить данные карты");
        app.updateButtonStyle(saveCardButton);
        saveCardButton.setOnAction(e -> {
            String cardNumber = cardNumberField.getText().replaceAll("[\\s-]", "");
            String expiryDate = expiryField.getText();
            String cvv = cvvField.getText();

            validationLabel.setVisible(true);

            if (!PaymentInfo.isValidCardNumber(cardNumber)) {
                validationLabel.setText("❌ Неверный номер карты");
                validationLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_11);
                return;
            }
            if (!PaymentInfo.isValidExpiryDate(expiryDate)) {
                validationLabel.setText("❌ Неверный срок действия");
                validationLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_11);
                return;
            }
            if (!PaymentInfo.isValidCVV(cvv)) {
                validationLabel.setText("❌ Неверный CVV");
                validationLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_11);
                return;
            }

            validationLabel.setText("✓ Данные карты прошли проверку");
            validationLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentSuccess() + STYLE_FONT_SIZE_11);

            PaymentInfo newPaymentInfo = new PaymentInfo(user.getUserId(), cardNumber, expiryDate, cvv);
            if (paymentDAO.savePaymentInfo(newPaymentInfo)) {
                CustomAlert.show(null, MESSAGE_SUCCESS, "Данные карты успешно сохранены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(null, MESSAGE_ERROR, "Не удалось сохранить данные карты", CustomAlert.AlertType.ERROR);
            }
        });

        Button deleteCardButton = new Button("🗑️ Удалить данные карты");
        deleteCardButton.setStyle("-fx-background-color: " + app.getCurrentError() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_14 + STYLE_FONT_WEIGHT_BOLD + "; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        deleteCardButton.setOnAction(e -> {
            if (paymentDAO.deletePaymentInfo(user.getUserId())) {
                cardNumberField.clear();
                expiryField.clear();
                cvvField.clear();
                validationLabel.setText("");
                validationLabel.setVisible(false);
                CustomAlert.show(null, MESSAGE_SUCCESS, "Данные карты удалены", CustomAlert.AlertType.INFO);
            } else {
                CustomAlert.show(null, MESSAGE_ERROR, "Не удалось удалить данные карты", CustomAlert.AlertType.ERROR);
            }
        });

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(grid, validationLabel);

        HBox buttonBox = new HBox(10, saveCardButton, deleteCardButton);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(infoBox, buttonBox);
        return card;
    }

    private VBox createOrdersCard() {
        VBox card = createCard("📦 История заказов");

        ordersGrid = new GridPane();
        ordersGrid.setPadding(new Insets(10));
        ordersGrid.setHgap(20);
        ordersGrid.setVgap(20);
        ordersGrid.setAlignment(Pos.TOP_CENTER);
        ordersGrid.setStyle(STYLE_BG_TRANSPARENT_BOTH);

        ordersScrollPane = new ScrollPane(ordersGrid);
        ordersScrollPane.setFitToWidth(true);
        ordersScrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);
        ordersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ordersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ordersScrollPane.setPrefHeight(300);

        refreshOrdersGrid();

        Label countLabel = new Label();
        countLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12 + "; -fx-padding: 5 0 0 0;");

        card.getChildren().addAll(ordersScrollPane, countLabel);
        return card;
    }

    private void refreshOrdersGrid() {
        if (ordersGrid == null) return;
        ordersGrid.getChildren().clear();

        List<Order> orders = orderDAO.getOrdersByUserId(user.getUserId());

        VBox parentCard = (VBox) ordersScrollPane.getParent();
        if (parentCard != null && parentCard.getChildren().size() > 1) {
            Label countLabel = (Label) parentCard.getChildren().get(1);
            countLabel.setText(String.format("Всего заказов: %d", orders.size()));
        }

        if (orders.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            Label emptyLabel = new Label(NO_ORDERS_TEXT);
            emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + "; -fx-font-size: 16px;");
            emptyBox.getChildren().add(emptyLabel);
            ordersGrid.add(emptyBox, 0, 0);
            GridPane.setColumnSpan(emptyBox, 3);
            return;
        }

        int col = 0;
        int row = 0;
        final int maxCols = 2;
        for (Order order : orders) {
            VBox card = createOrderCard(order);
            ordersGrid.add(card, col, row);
            col++;
            if (col == maxCols) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + STYLE_BG_RADIUS_12 +
                STYLE_EFFECT_SHADOW +
                STYLE_BORDER + app.getCurrentBorderColor() + STYLE_BORDER_RADIUS_12);
        card.setPrefWidth(350);
        card.setMinWidth(350);
        card.setMaxWidth(350);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label orderIdLabel = new Label(String.format("Заказ №%d", order.getOrderId()));
        orderIdLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_16 + STYLE_FONT_WEIGHT_BOLD);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().toString().substring(0, 16) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        headerBox.getChildren().addAll(orderIdLabel, spacer, dateLabel);

        Label statusLabel = createStatusLabel(order);
        Label amountLabel = new Label(String.format("💰 Сумма: %.2f ₽", order.getTotalAmount()));
        amountLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_14 + STYLE_FONT_WEIGHT_BOLD);
        Label paymentLabel = new Label("💳 Оплата: " + (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD") ? "Карта" : "Наличные"));
        paymentLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        String addressText = (order.getAddress() != null && !order.getAddress().isEmpty()) ? order.getAddress() : DEFAULT_ADDRESS_TEXT;
        Label addressLabel = new Label("📍 Адрес: " + addressText);
        addressLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
        addressLabel.setWrapText(true);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        Button detailsButton = new Button("📋 Детали");
        Button repeatButton = new Button("🔄 Повторить");
        detailsButton.setStyle("-fx-background-color: " + app.getCurrentAccent() + STYLE_TEXT_FILL_WHITE + STYLE_FONT_SIZE_12 + STYLE_PADDING_6_12 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND);
        repeatButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + app.getCurrentAccent() + STYLE_FONT_SIZE_12 + STYLE_PADDING_6_12 + STYLE_BG_RADIUS_6 + STYLE_CURSOR_HAND + " -fx-border-color: " + app.getCurrentAccent() + "; -fx-border-radius: 6; -fx-border-width: 1;");
        detailsButton.setOnAction(e -> showOrderDetails(order));
        repeatButton.setOnAction(e -> repeatOrder(order));
        buttonsBox.getChildren().addAll(detailsButton, repeatButton);

        card.getChildren().addAll(headerBox, statusLabel, amountLabel, paymentLabel, addressLabel, buttonsBox);
        return card;
    }

    private Label createStatusLabel(Order order) {
        String statusText = getStatusText(order.getStatus());
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

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "⏳ Ожидает оплаты";
            case "PAID": return "✅ Оплачен";
            case "SHIPPED": return "🚚 Отправлен";
            case STATUS_DELIVERED: return "📦 Доставлен";
            case STATUS_CANCELLED: return "❌ Отменен";
            default: return status;
        }
    }

    private void repeatOrder(Order order) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("=== НАЧАЛО ПОВТОРЕНИЯ ЗАКАЗА ===");
            logger.info(String.format("Order ID: %d", order != null ? order.getOrderId() : 0));
        }

        if (order == null) {
            logger.severe("ERROR: Order is null!");
            CustomAlert.show(parentStage, MESSAGE_ERROR, "Заказ не найден", CustomAlert.AlertType.ERROR);
            return;
        }

        String sql = "SELECT product_id, product_name, quantity FROM order_items WHERE order_id = ?";
        try (Connection conn = com.kurcashi.database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Executing SQL: " + sql);
            }
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
                CustomAlert.show(parentStage, MESSAGE_SUCCESS, message, CustomAlert.AlertType.SUCCESS);
            } else {
                CustomAlert.show(parentStage, MESSAGE_ERROR, "Ни один товар не добавлен. Нет в наличии: " + String.join(", ", unavailableProducts), CustomAlert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error in repeatOrder: {0}", e.getMessage());
            CustomAlert.show(parentStage, MESSAGE_ERROR, "Ошибка при добавлении товаров в корзину: " + e.getMessage(), CustomAlert.AlertType.ERROR);
        }
    }

    private void showOrderDetails(Order order) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setTitle("");

        applyBlurEffect();
        dialogStage.setOnCloseRequest(e -> removeBlurEffect());
        dialogStage.setOnHidden(e -> removeBlurEffect());

        VBox dialogBox = new VBox(15);
        dialogBox.setPadding(new Insets(20));
        dialogBox.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + "; -fx-background-radius: 15;" +
                "-fx-border-color: " + app.getCurrentAccent() + "; -fx-border-radius: 15; -fx-border-width: 2;");
        dialogBox.setPrefWidth(550);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(0, 0, 5, 0));
        Label titleLabel = new Label(String.format("Детали заказа №%d", order.getOrderId()));
        titleLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + "; -fx-font-size: 20px; " + STYLE_FONT_WEIGHT_BOLD);
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + app.getCurrentTextSecondary() + "; -fx-font-size: 18px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> dialogStage.close());
        titleBar.getChildren().addAll(titleLabel, closeButton);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().toString().substring(0, 19) : "";
        Label dateLabel = new Label("📅 Дата: " + dateStr);
        dateLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        Label statusLabel = new Label("📌 Статус: " + getStatusText(order.getStatus()));
        statusLabel.setStyle(STYLE_TEXT_FILL + getStatusColor(order) + STYLE_FONT_SIZE_14 + STYLE_FONT_WEIGHT_BOLD);
        Label amountLabel = new Label(String.format("💰 Сумма: %.2f ₽", order.getTotalAmount()));
        amountLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_16 + STYLE_FONT_WEIGHT_BOLD);
        String paymentText = (order.getPaymentMethod() != null && order.getPaymentMethod().equals("CARD")) ? "Банковской картой" : "Наличными";
        Label paymentLabel = new Label("💳 Оплата: " + paymentText);
        paymentLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        String addressText = (order.getAddress() != null && !order.getAddress().isEmpty()) ? order.getAddress() : "Не указан";
        Label addressLabel = new Label("📍 Адрес доставки: " + addressText);
        addressLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_14);
        addressLabel.setWrapText(true);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + app.getCurrentBorderColor() + ";");
        Label itemsTitle = new Label("🛒 Товары в заказе:");
        itemsTitle.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_14 + STYLE_FONT_WEIGHT_BOLD);

        VBox itemsList = new VBox(8);
        itemsList.setPadding(new Insets(10));
        itemsList.setStyle(STYLE_BG_CARD + app.getCurrentBgLight() + "; -fx-background-radius: 8;");
        loadOrderItems(order.getOrderId(), itemsList);
        ScrollPane scrollPane = new ScrollPane(itemsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle(STYLE_BG_TRANSPARENT_BOTH);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        Button repeatButton = new Button("🔄 Повторить заказ");
        repeatButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + app.getCurrentAccent() + STYLE_FONT_SIZE_14 + STYLE_FONT_WEIGHT_BOLD + "; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: " + app.getCurrentAccent() + "; -fx-border-radius: 8; -fx-border-width: 1;");
        repeatButton.setOnAction(e -> {
            repeatOrder(order);
            dialogStage.close();
        });
        Button closeButton2 = new Button("Закрыть");
        app.updateButtonStyle(closeButton2);
        closeButton2.setOnAction(e -> dialogStage.close());
        buttonBox.getChildren().addAll(repeatButton, closeButton2);

        dialogBox.getChildren().addAll(titleBar, dateLabel, statusLabel, amountLabel, paymentLabel, addressLabel, separator, itemsTitle, scrollPane, buttonBox);
        Scene scene = new Scene(dialogBox);
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
                nameLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_13);
                nameLabel.setPrefWidth(200);
                Label qtyLabel = new Label(quantity + " кг");
                qtyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_13);
                qtyLabel.setPrefWidth(80);
                qtyLabel.setAlignment(Pos.CENTER);
                Label priceLabel = new Label(String.format("%.2f ₽/кг", price));
                priceLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_13);
                priceLabel.setPrefWidth(100);
                priceLabel.setAlignment(Pos.CENTER_RIGHT);
                Label totalLabel = new Label(String.format("%.2f ₽", total));
                totalLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentAccentDark() + STYLE_FONT_SIZE_13 + STYLE_FONT_WEIGHT_BOLD);
                totalLabel.setPrefWidth(100);
                totalLabel.setAlignment(Pos.CENTER_RIGHT);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                itemBox.getChildren().addAll(nameLabel, spacer, qtyLabel, priceLabel, totalLabel);
                container.getChildren().add(itemBox);
            }

            if (container.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Нет информации о товарах");
                emptyLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextSecondary() + STYLE_FONT_SIZE_12);
                emptyLabel.setAlignment(Pos.CENTER);
                container.getChildren().add(emptyLabel);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading order items: {0}", e.getMessage());
            Label errorLabel = new Label("Ошибка загрузки данных");
            errorLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentError() + STYLE_FONT_SIZE_12);
            container.getChildren().add(errorLabel);
        }
    }

    private VBox createCard(String title) {
        VBox card = new VBox(15);
        card.setStyle(STYLE_BG_CARD + app.getCurrentBgCard() + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 15; -fx-border-width: 1;");
        card.setPadding(new Insets(20));
        card.setMaxWidth(800);
        card.setPrefWidth(800);
        card.setMinWidth(600);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(STYLE_TEXT_FILL + app.getCurrentTextPrimary() + STYLE_FONT_SIZE_20 + STYLE_FONT_WEIGHT_BOLD);
        card.getChildren().add(titleLabel);
        return card;
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

    public VBox getView() {
        return view;
    }
}