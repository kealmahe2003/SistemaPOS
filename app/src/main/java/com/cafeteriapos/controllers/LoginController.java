package com.cafeteriapos.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML private TextField usuarioField;
    @FXML private PasswordField contraseñaField;

    @FXML
    private void handleLogin() {
        String usuario = usuarioField.getText();
        String contraseña = contraseñaField.getText();

        if ("admin".equals(usuario) && "admin".equals(contraseña)) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/cafeteriapos/views/MainView.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Sistema POS - Dashboard");
                stage.setScene(new javafx.scene.Scene(root));
                stage.show();

                // Cierra ventana de login
                ((javafx.stage.Stage) usuarioField.getScene().getWindow()).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Credenciales incorrectas");
            alert.setContentText("Por favor, inténtalo de nuevo.");
            alert.showAndWait();
        }
    }
}