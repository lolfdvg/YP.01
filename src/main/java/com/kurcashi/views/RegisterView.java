package com.kurcashi.views;

import com.example.kurcashi.BaitDesireApp;
import com.kurcashi.dao.UserDAO;
import com.kurcashi.models.User;
import com.kurcashi.models.Address;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegisterView {

    private final BaitDesireApp app;
    private final UserDAO userDAO;

    private TextField usernameField;
    private TextField phoneField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Label errorLabel;

    public RegisterView(BaitDesireApp app) {
        this.app = app;
        this.userDAO = new UserDAO();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getStyleClass().add("profile-root");
        mainContainer.setPadding(new Insets(15));

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
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getStyleClass().add("auth-card");
        centerBox.setMaxHeight(Region.USE_PREF_SIZE);
        return centerBox;
    }

    private VBox createTitleLabels() {
        Label titleLabel = new Label("🐟 BAIT & DESIRE");
        titleLabel.getStyleClass().add("auth-title");

        Label subtitleLabel = new Label("Присоединитесь к BAIT & DESIRE");
        subtitleLabel.getStyleClass().add("auth-subtitle");

        VBox titleBox = new VBox(3);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        return titleBox;
    }

    private VBox createForm() {
        VBox formBox = new VBox(8);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(5, 0, 5, 0));

        usernameField = createUsernameField();
        phoneField = createPhoneField();
        emailField = createEmailField();
        passwordField = createPasswordField();
        confirmPasswordField = createConfirmPasswordField();
        errorLabel = createErrorLabel();

        formBox.getChildren().addAll(
                createLabeledField("Логин:", usernameField),
                createLabeledField("Номер телефона:", phoneField),
                createLabeledField("Email:", emailField),
                createLabeledField("Пароль:", passwordField),
                createLabeledField("Повторите пароль:", confirmPasswordField),
                errorLabel
        );
        return formBox;
    }

    private VBox createLabeledField(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("auth-label");

        VBox box = new VBox(2);
        box.getChildren().addAll(label, field);
        return box;
    }

    private TextField createUsernameField() {
        TextField field = new TextField();
        field.setPromptText("Введите логин (мин. 3 символа)");
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
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
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                String digitsOnly = newValue.replaceAll("\\D", "");
                if (digitsOnly.length() > 11) {
                    digitsOnly = digitsOnly.substring(0, 11);
                }
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
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
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
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && newValue.contains(" ")) field.setText(newValue.replace(" ", ""));
        });
        return field;
    }

    private PasswordField createConfirmPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Повторите пароль");
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((obs, old, newValue) -> {
            if (newValue != null && newValue.contains(" ")) field.setText(newValue.replace(" ", ""));
        });
        return field;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("form-error-label");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setVisible(false);
        return label;
    }

    private Button createRegisterButton() {
        Button button = new Button("Зарегистрироваться");
        button.getStyleClass().add("btn-auth-primary");
        button.setOnAction(e -> handleRegistration());
        return button;
    }

    private Button createBackButton() {
        Button button = new Button("← Назад к каталогу");
        button.getStyleClass().add("btn-auth-secondary");
        button.setOnAction(e -> app.showGuestCatalogView());
        return button;
    }

    private Hyperlink createLoginLink() {
        Hyperlink link = new Hyperlink("Уже есть аккаунт? Войдите!");
        link.getStyleClass().add("auth-link");
        link.setOnAction(e -> app.showLoginView());
        return link;
    }

    private void handleRegistration() {
        String username = usernameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        errorLabel.setVisible(false);

        if (!validateFields(username, phone, email, password, confirm)) {
            return;
        }

        String cleanPhone = phone.replaceAll("\\D", "");
        if (userDAO.isUserExists(email, cleanPhone)) {
            showError("❌ Пользователь с таким email или телефоном уже существует");
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
            showError("❌ Ошибка регистрации. Попробуйте позже.");
        }
    }

    private boolean validateFields(String username, String phone, String email, String password, String confirm) {
        if (username.isEmpty()) { showError("❌ Введите логин"); return false; }
        if (username.length() < 3) { showError("❌ Логин должен содержать минимум 3 символа"); return false; }

        String cleanPhone = phone.replaceAll("\\D", "");
        if (cleanPhone.isEmpty()) { showError("❌ Введите номер телефона"); return false; }
        if (cleanPhone.length() < 10) { showError("❌ Введите полный номер телефона"); return false; }

        if (email.isEmpty()) { showError("❌ Введите email"); return false; }
        if (!email.contains("@") || !email.contains(".")) { showError("❌ Введите корректный email"); return false; }

        if (password.isEmpty()) { showError("❌ Введите пароль"); return false; }
        if (password.length() < 6) { showError("❌ Пароль должен содержать минимум 6 символов"); return false; }
        if (password.length() > 12) { showError("❌ Пароль не должен превышать 12 символов"); return false; }
        if (password.contains(" ")) { showError("❌ Пароль не должен содержать пробелы"); return false; }

        if (!password.equals(confirm)) { showError("❌ Пароли не совпадают"); return false; }

        return true;
    }

    private void showError(String message) {
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