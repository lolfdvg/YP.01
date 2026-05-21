package com.kurcashi.views;

import com.example.kurcashi.BaitDesireApp;
import com.kurcashi.dao.UserDAO;
import com.kurcashi.models.User;
import com.kurcashi.models.Address;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RegisterView {

    private static final String STYLE_TEXT_FILL_WHITE = "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12;";
    private static final String STYLE_BG_BUTTON_BACK = "-fx-background-color: " + BaitDesireApp.TEXT_SECONDARY + ";" + STYLE_TEXT_FILL_WHITE;
    private static final String STYLE_BG_BUTTON_BACK_HOVER = "-fx-background-color: " + BaitDesireApp.TEXT_PRIMARY + ";" + STYLE_TEXT_FILL_WHITE;
    private static final String FONT_ARIAL = "Arial";

    private final BaitDesireApp app;
    private final UserDAO userDAO;

    public RegisterView(BaitDesireApp app) {
        this.app = app;
        this.userDAO = new UserDAO();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: " + app.getCurrentBgLight() + ";");

        VBox centerBox = createCenterBox();
        centerBox.getChildren().addAll(
                createTitleLabels(),
                createForm(),
                createRegisterButton(),
                createBackButton(),
                createLoginLink()
        );

        mainContainer.getChildren().add(centerBox);
        return mainContainer;
    }

    private VBox createCenterBox() {
        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(400);
        centerBox.setPadding(new Insets(40));
        centerBox.setStyle("-fx-background-color: " + app.getCurrentBgCard() + "; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-border-color: " + app.getCurrentBorderColor() + "; -fx-border-radius: 10; -fx-border-width: 1;");
        return centerBox;
    }

    private VBox createTitleLabels() {
        Label titleLabel = new Label("🐟 BAIT & DESIRE");
        titleLabel.setFont(Font.font(FONT_ARIAL, FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + ";");

        Label subtitleLabel = new Label("Присоединитесь к BAIT & DESIRE");
        subtitleLabel.setFont(Font.font(FONT_ARIAL, FontWeight.NORMAL, 16));
        subtitleLabel.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + ";");

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        return titleBox;
    }

    private VBox createForm() {
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(10, 0, 10, 0));

        TextField usernameField = createUsernameField();
        TextField phoneField = createPhoneField();
        TextField emailField = createEmailField();
        PasswordField passwordField = createPasswordField();
        PasswordField confirmPasswordField = createConfirmPasswordField();
        Label errorLabel = createErrorLabel();

        formBox.getChildren().addAll(
                createLabeledField("Логин:", usernameField),
                createLabeledField("Номер телефона:", phoneField),
                createLabeledField("Email:", emailField),
                createLabeledField("Пароль:", passwordField),
                createLabeledField("Повторите пароль:", confirmPasswordField),
                errorLabel
        );
        formBox.setUserData(errorLabel);
        return formBox;
    }

    private VBox createLabeledField(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setFont(Font.font(FONT_ARIAL, FontWeight.NORMAL, 14));
        label.setStyle("-fx-text-fill: " + app.getCurrentTextPrimary() + ";");
        label.setPadding(new Insets(5, 0, 0, 0));

        VBox box = new VBox(5);
        box.getChildren().addAll(label, field);
        return box;
    }

    private TextField createUsernameField() {
        TextField field = new TextField();
        field.setPromptText("Введите логин (мин. 3 символа)");
        field.setPrefWidth(300);
        app.updateTextFieldStyle(field);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                String filtered = newValue.replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9]", "");
                if (!filtered.equals(newValue)) field.setText(filtered);
            }
        });
        return field;
    }

    private TextField createPhoneField() {
        TextField field = new TextField();
        field.setPromptText("+7 (___) ___-__-__");
        field.setPrefWidth(300);
        app.updateTextFieldStyle(field);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                String digitsOnly = newValue.replaceAll("\\D", "");
                if (digitsOnly.length() > 11) digitsOnly = digitsOnly.substring(0, 11);
                String formatted = formatPhone(digitsOnly);
                if (!formatted.equals(field.getText())) {
                    field.setText(formatted);
                    field.positionCaret(formatted.length());
                }
            }
        });
        return field;
    }

    private TextField createEmailField() {
        TextField field = new TextField();
        field.setPromptText("example@mail.com");
        field.setPrefWidth(300);
        app.updateTextFieldStyle(field);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                String filtered = newValue.replaceAll("[^a-zA-Z0-9@._\\-]", "");
                if (!filtered.equals(newValue)) field.setText(filtered);
            }
        });
        return field;
    }

    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("От 6 до 12 символов");
        field.setPrefWidth(300);
        app.updateTextFieldStyle(field);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && newValue.contains(" ")) field.setText(newValue.replace(" ", ""));
        });
        return field;
    }

    private PasswordField createConfirmPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Повторите пароль");
        field.setPrefWidth(300);
        app.updateTextFieldStyle(field);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && newValue.contains(" ")) field.setText(newValue.replace(" ", ""));
        });
        return field;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: " + app.getCurrentError() + "; -fx-font-size: 12px; -fx-padding: 5 0;");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(350);
        label.setVisible(false);
        return label;
    }

    private Button createRegisterButton() {
        Button button = new Button("Зарегистрироваться");
        button.setPrefWidth(300);
        app.updateButtonStyle(button);
        button.setOnAction(e -> handleRegistration(button.getScene().getRoot()));
        return button;
    }

    private Button createBackButton() {
        Button button = new Button("← Назад к каталогу");
        button.setPrefWidth(300);
        button.setStyle(STYLE_BG_BUTTON_BACK + "-fx-background-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle(STYLE_BG_BUTTON_BACK_HOVER + "-fx-background-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle(STYLE_BG_BUTTON_BACK + "-fx-background-radius: 5;"));
        button.setOnAction(e -> app.showGuestCatalogView());
        return button;
    }

    private Hyperlink createLoginLink() {
        Hyperlink link = new Hyperlink("Уже есть аккаунт? Войдите!");
        link.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + "; -fx-border-color: transparent; -fx-font-size: 14px;");
        link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill: " + app.getCurrentAccentDark() + "; -fx-border-color: transparent; -fx-font-size: 14px; -fx-underline: true;"));
        link.setOnMouseExited(e -> link.setStyle("-fx-text-fill: " + app.getCurrentTextSecondary() + "; -fx-border-color: transparent; -fx-font-size: 14px;"));
        link.setOnAction(e -> app.showLoginView());
        return link;
    }

    private void handleRegistration(javafx.scene.Node root) {
        VBox formBox = (VBox) ((VBox) ((VBox) root.getScene().getRoot()).getChildren().get(0)).getChildren().get(2); // костыль, лучше искать по id
        // Но для простоты – найдём элементы по их позициям. Лучше – задать id.
        // Упростим: получим поля из formBox
        VBox form = (VBox) ((VBox) ((VBox) root.getScene().getRoot()).getChildren().get(0)).getChildren().get(2);
        TextField usernameField = (TextField) ((VBox) form.getChildren().get(0)).getChildren().get(1);
        TextField phoneField = (TextField) ((VBox) form.getChildren().get(1)).getChildren().get(1);
        TextField emailField = (TextField) ((VBox) form.getChildren().get(2)).getChildren().get(1);
        PasswordField passwordField = (PasswordField) ((VBox) form.getChildren().get(3)).getChildren().get(1);
        PasswordField confirmPasswordField = (PasswordField) ((VBox) form.getChildren().get(4)).getChildren().get(1);
        Label errorLabel = (Label) form.getChildren().get(5);

        String username = usernameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        errorLabel.setVisible(false);

        if (!validateFields(username, phone, email, password, confirm, errorLabel)) return;

        String cleanPhone = phone.replaceAll("\\D", "");
        if (userDAO.isUserExists(email, cleanPhone)) {
            showError(errorLabel, "❌ Пользователь с таким email или телефоном уже существует");
            return;
        }

        User user = new User(username, email, cleanPhone, password);
        if (userDAO.registerUser(user)) {
            User registeredUser = userDAO.loginUser(email, cleanPhone, password);
            if (registeredUser != null) {
                app.migrateGuestData(app.getGuestId(), registeredUser.getUserId());
                AddressDialog addressDialog = new AddressDialog(registeredUser, app.getPrimaryStage(), app);
                Address savedAddress = addressDialog.showAndWait();
                app.showCatalogView(registeredUser, savedAddress);
            } else {
                app.showLoginView();
            }
        } else {
            showError(errorLabel, "❌ Ошибка регистрации. Попробуйте позже.");
        }
    }

    private boolean validateFields(String username, String phone, String email, String password, String confirm, Label errorLabel) {
        if (username.isEmpty()) { showError(errorLabel, "❌ Введите логин"); return false; }
        if (username.length() < 3) { showError(errorLabel, "❌ Логин должен содержать минимум 3 символа"); return false; }

        String cleanPhone = phone.replaceAll("\\D", "");
        if (cleanPhone.isEmpty()) { showError(errorLabel, "❌ Введите номер телефона"); return false; }
        if (cleanPhone.length() < 10) { showError(errorLabel, "❌ Введите полный номер телефона"); return false; }

        if (email.isEmpty()) { showError(errorLabel, "❌ Введите email"); return false; }
        if (!email.contains("@") || !email.contains(".")) { showError(errorLabel, "❌ Введите корректный email (должен содержать @ и .)"); return false; }

        if (password.isEmpty()) { showError(errorLabel, "❌ Введите пароль"); return false; }
        if (password.length() < 6) { showError(errorLabel, "❌ Пароль должен содержать минимум 6 символов"); return false; }
        if (password.length() > 12) { showError(errorLabel, "❌ Пароль не должен превышать 12 символов"); return false; }
        if (password.contains(" ")) { showError(errorLabel, "❌ Пароль не должен содержать пробелы"); return false; }

        if (!password.equals(confirm)) { showError(errorLabel, "❌ Пароли не совпадают"); return false; }

        return true;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private String formatPhone(String digits) {
        if (digits == null || digits.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        if (digits.length() >= 1) result.append("+7");
        if (digits.length() >= 2) {
            result.append(" (");
            int end = Math.min(4, digits.length());
            result.append(digits, 1, end);
            if (digits.length() >= 4) result.append(")");
        }
        if (digits.length() >= 5) {
            result.append(" ");
            int end = Math.min(7, digits.length());
            result.append(digits, 4, end);
        }
        if (digits.length() >= 8) {
            result.append("-");
            int end = Math.min(9, digits.length());
            result.append(digits, 7, end);
        }
        if (digits.length() >= 10) {
            result.append("-");
            int end = Math.min(11, digits.length());
            result.append(digits, 9, end);
        }
        return result.toString();
    }
}