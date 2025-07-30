package com.cafeteriapos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Map;
import java.util.Objects;

public class LoginController {
    @FXML private VBox rootContainer;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        // Limpiar estilos de error previos
        userField.getStyleClass().remove("error-border");
        passwordField.getStyleClass().remove("error-border");
        
        // Validaciones
        String user = userField.getText().trim();
        String password = passwordField.getText().trim();
        boolean isValid = true;

        if (user.isEmpty()) {
            userField.getStyleClass().add("error-border");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordField.getStyleClass().add("error-border");
            isValid = false;
        }

        if (!isValid) {
            mostrarAlerta("Error", "Complete todos los campos");
            return;
        }

        if (AuthService.validarCredenciales(user, password)) {
            abrirDashboard();
            cerrarVentanaActual();
        } else {
            passwordField.getStyleClass().add("error-border");
            mostrarAlerta("Error", "Credenciales incorrectas");
        }
    }

    private void abrirDashboard() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/com/cafeteriapos/views/MainView.fxml")
            ));
            
            Stage stage = new Stage();
            stage.setTitle("Dashboard - Sistema POS");
            stage.setScene(new Scene(root));
            
            // Configuración de tamaño
            stage.setMinWidth(1024);
            stage.setMinHeight(768);
            stage.setMaximized(true);
            
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error Crítico", 
                "No se pudo iniciar el sistema: " + e.getMessage());
        }
    }

    private void cerrarVentanaActual() {
        ((Stage) userField.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        // Estilo CSS para alertas
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            Objects.requireNonNull(
                getClass().getResource("/styles/login.css")
            ).toExternalForm()
        );
        dialogPane.getStyleClass().add("alert-dialog");
        
        alert.showAndWait();
    }

    // Servicio de autenticación (puede moverse a paquete 'services')
    private static class AuthService {
        private static final Map<String, String> USUARIOS = Map.of(
            "admin", "admin123",
            "cajero", "cajero456"
        );

        public static boolean validarCredenciales(String usuario, String contrasena) {
            return USUARIOS.getOrDefault(usuario, "").equals(contrasena);
        }
    }
}