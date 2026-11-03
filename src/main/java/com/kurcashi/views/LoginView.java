package com.kurcashi.views;

import com.kurcashi.database.DatabaseConnection;
import com.kurcashi.models.User;
import com.kurcashi.utils.PasswordUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.example.kurcashi.BaitDesireApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginView {

    private static final Logger LOGGER = Logger.getLogger(LoginView.class.getName());

    private final BaitDesireApp app;
    private VBox view;
    private TextField usernameField;
    private PasswordField passwordField;
    private CheckBox rememberCheckBox;
    private Label errorLabel;

    public LoginView(BaitDesireApp app) {
        this.app = app;
        createView();
    }

    private void createView() {
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("profile-root");

        VBox card = createCard();
        view.getChildren().add(card);
    }

    private VBox createCard() {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("auth-card");
        card.setMaxHeight(Region.USE_PREF_SIZE); // Чтобы не растягивалось на весь экран

        card.getChildren().addAll(createTitleBox(), createInputBox(), createButtonBox(), createRegisterBox());
        return card;
    }

    private VBox createTitleBox() {
        Label titleLabel = new Label("🐟 BAIT & DESIRE");
        titleLabel.getStyleClass().add("auth-title");

        Label subtitleLabel = new Label("Вход в аккаунт");
        subtitleLabel.getStyleClass().add("auth-subtitle");

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        return titleBox;
    }

    private VBox createInputBox() {
        VBox inputBox = new VBox(15);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(20, 0, 20, 0));

        usernameField = createUsernameField();
        passwordField = createPasswordField();
        rememberCheckBox = createRememberCheckBox();
        errorLabel = createErrorLabel();

        inputBox.getChildren().addAll(
                createLabeledField("Имя пользователя", usernameField),
                createLabeledField("Пароль", passwordField),
                rememberCheckBox,
                errorLabel
        );
        return inputBox;
    }

    private VBox createLabeledField(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("auth-label");
        VBox box = new VBox(5);
        box.getChildren().addAll(label, field);
        return box;
    }

    private TextField createUsernameField() {
        TextField field = new TextField();
        field.setPromptText("Введите имя пользователя");
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                String filtered = newVal.replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9]", "");
                if (!filtered.equals(newVal)) field.setText(filtered);
            }
        });
        return field;
    }

    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Введите пароль");
        field.getStyleClass().add("auth-field");
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.contains(" ")) field.setText(newVal.replace(" ", ""));
        });
        return field;
    }

    private CheckBox createRememberCheckBox() {
        CheckBox cb = new CheckBox("Запомнить пароль");
        cb.getStyleClass().add("custom-checkbox");
        return cb;
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

    private VBox createButtonBox() {
        Button loginButton = new Button("Войти");
        loginButton.getStyleClass().add("btn-auth-primary");
        loginButton.setOnAction(e -> handleLogin());

        Button backButton = new Button("← Назад к каталогу");
        backButton.getStyleClass().add("btn-auth-secondary");
        backButton.setOnAction(e -> app.showGuestCatalogView());

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, backButton);
        return buttonBox;
    }

    private HBox createRegisterBox() {
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccountLabel = new Label("Нет аккаунта?");
        noAccountLabel.getStyleClass().add("auth-label");

        Hyperlink registerLink = new Hyperlink("Зарегистрироваться");
        registerLink.getStyleClass().add("auth-link");
        registerLink.setOnAction(e -> app.showRegisterView());

        registerBox.getChildren().addAll(noAccountLabel, registerLink);
        return registerBox;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setVisible(false);
        errorLabel.setText("");

        if (!validateInput(username, password)) return;

        User user = authenticateUser(username, password, rememberCheckBox.isSelected());

        if (user != null) {
            app.migrateGuestData(app.getGuestId(), user.getUserId());
            if (user.isAdmin()) {
                com.kurcashi.models.Admin admin = createAdminFromUser(user);
                app.showAdminDashboard(admin);
            } else {
                app.showCatalogView(user);
            }
        } else {
            showError("❌ Неверное имя пользователя или пароль");
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("❌ Пожалуйста, заполните все поля");
            return false;
        }
        if (username.length() < 3) {
            showError("❌ Имя пользователя должно содержать минимум 3 символа");
            return false;
        }
        if (password.length() < 6) {
            showError("❌ Пароль должен содержать минимум 6 символов");
            return false;
        }
        if (password.length() > 12) {
            showError("❌ Пароль не должен превышать 12 символов");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private User authenticateUser(String username, String password, boolean rememberPassword) {
        String query = "SELECT customer_id, first_name, last_name, email, phone, password_hash, bonus_points, registration_date, is_admin FROM customers WHERE first_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("customer_id"));
                    user.setUsername(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name") != null ? rs.getString("last_name") : "");
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setPassword(password);
                    user.setBonusPoints(rs.getInt("bonus_points"));
                    user.setRegistrationDate(rs.getTimestamp("registration_date"));
                    user.setAdmin(rs.getBoolean("is_admin"));

                    fetchAdminRole(user, conn);
                    handleSession(user, password, rememberPassword);
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при аутентификации", e);
            showError("❌ Ошибка подключения к базе данных");
        }
        return null;
    }

    private void fetchAdminRole(User user, Connection conn) {
        if (!user.isAdmin()) return;
        String roleQuery = "SELECT role FROM admins WHERE admin_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(roleQuery)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setAdminRole(rs.getString("role"));
            } else {
                createAdminRecord(user, conn);
                try (PreparedStatement stmt2 = conn.prepareStatement("SELECT role FROM admins WHERE admin_id = ?")) {
                    stmt2.setInt(1, user.getUserId());
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) user.setAdminRole(rs2.getString("role"));
                    else user.setAdminRole("ADMIN");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Не удалось получить роль администратора", e);
        }
    }

    private void createAdminRecord(User user, Connection conn) {
        String countSql = "SELECT COUNT(*) FROM admins";
        boolean isFirstAdmin = false;
        try (java.sql.Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) isFirstAdmin = true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Не удалось проверить количество админов", e);
        }

        String role = isFirstAdmin ? "SUPER_ADMIN" : "ADMIN";
        String insertSql = "INSERT INTO admins (admin_id, username, email, role, is_active, created_at) VALUES (?, ?, ?, ?, true, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, user.getUserId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Не удалось создать запись администратора", e);
        }
    }

    private void handleSession(User user, String password, boolean rememberPassword) {
        if (rememberPassword) {
            saveSession(user, password);
        } else {
            clearSession(user.getUserId());
        }
    }

    private void saveSession(User user, String password) {
        String sql = """
            INSERT INTO saved_sessions (customer_id, email, phone, password, saved_at, last_used)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (customer_id) DO UPDATE SET
                email = EXCLUDED.email,
                phone = EXCLUDED.phone,
                password = EXCLUDED.password,
                last_used = CURRENT_TIMESTAMP
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getUserId());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Ошибка сохранения сессии", e);
        }
    }

    private void clearSession(int userId) {
        String sql = "DELETE FROM saved_sessions WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Ошибка очистки сессии", e);
        }
    }

    private com.kurcashi.models.Admin createAdminFromUser(User user) {
        com.kurcashi.models.Admin admin = new com.kurcashi.models.Admin();
        admin.setAdminId(user.getUserId());
        admin.setUsername(user.getUsername());
        admin.setEmail(user.getEmail());
        admin.setFullName(user.getUsername());
        String role = user.getAdminRole();
        admin.setRole(role != null && !role.isEmpty() ? role : "ADMIN");
        admin.setActive(true);
        admin.setAdmin(true);
        admin.setPhone(user.getPhone());
        return admin;
    }

    public VBox getView() {
        return view;
    }
}