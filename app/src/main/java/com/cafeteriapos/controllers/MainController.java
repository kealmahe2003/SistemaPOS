package com.cafeteriapos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.Objects;

import com.cafeteriapos.utils.CajaManager;

public class MainController {
    @FXML private StackPane contenidoPane;

    @FXML
    private void abrirVenta() {
        cargarVista("/com/cafeteriapos/views/VentasView.fxml");
    }

    @FXML
    private void abrirProductos() {
        cargarVista("/com/cafeteriapos/views/ProductosView.fxml");
    }

    @FXML
    private void abrirEstadisticas() {
        cargarVista("/com/cafeteriapos/views/EstadisticasView.fxml");
    }

    @FXML
    private void cerrarSesion() {
        mostrarConfirmacion("Cerrar sesión", 
            "¿Está seguro de cerrar la sesión?", () -> {
                CajaManager.registrarCierre(calcularMontoCierre()); // Nuevo
                cerrarAplicacion();
            });
    }

    private double calcularMontoCierre() {
        // Lógica para calcular el monto final (ej: sumar ventas del día)
        return 1500.00; // Valor de ejemplo
    }

    private void cargarVista(String fxmlPath) {
        try {
            Node vista = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource(fxmlPath)
            ));
            contenidoPane.getChildren().setAll(vista);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", 
                "No se pudo cargar la vista", fxmlPath + " no encontrada");
        }
    }

    private void mostrarConfirmacion(String titulo, String mensaje, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    private void cerrarAplicacion() {
        System.exit(0);
        // Alternativa para volver al login:
        // ((Stage) contenidoPane.getScene().getWindow()).close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, 
                             String header, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}