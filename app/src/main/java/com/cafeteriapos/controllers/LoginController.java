package com.cafeteriapos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.Objects;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String user = userField.getText().trim();
        String password = passwordField.getText().trim();

        if (AuthService.validarCredenciales(user, password)) {
            abrirDashboard();
            cerrarVentanaActual();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de autenticación", 
                "Credenciales incorrectas", "Verifique usuario y contraseña");
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
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", 
                "No se pudo cargar el dashboard", e.getMessage());
        }
    }

    private void cerrarVentanaActual() {
        Window window = userField.getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, 
                             String header, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    // Clase interna para manejo de autenticación (puede moverse a paquete 'services')
    private static class AuthService {
        private static final String USUARIO_VALIDO = "admin";
        private static final String CONTRASENA_VALIDA = "admin123";

        public static boolean validarCredenciales(String usuario, String contrasena) {
            return USUARIO_VALIDO.equals(usuario) && CONTRASENA_VALIDA.equals(contrasena);
        }
    }
}