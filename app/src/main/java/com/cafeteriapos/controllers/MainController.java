package com.cafeteriapos.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private StackPane contenidoPane;

    @FXML
    private void abrirVenta() {
        contenidoPane.getChildren().setAll(new Label("Aquí iría la interfaz de ventas."));
    }

    @FXML
    private void abrirProductos() {
        contenidoPane.getChildren().setAll(new Label("Aquí iría la interfaz de productos."));
    }

    @FXML
    private void abrirEstadisticas() {
        contenidoPane.getChildren().setAll(new Label("Aquí iría la interfaz de estadísticas."));
    }

    @FXML
    private void cerrarSesion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sesión cerrada");
        alert.setHeaderText(null);
        alert.setContentText("Has cerrado sesión.");
        alert.showAndWait();

        System.exit(0); // o regresar al login
    }
}