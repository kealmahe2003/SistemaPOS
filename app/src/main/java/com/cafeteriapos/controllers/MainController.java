package com.cafeteriapos.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class MainController {

    @FXML private StackPane contenidoPane;

    @FXML
    private void home() {
        cargarVista("/com/cafeteriapos/views/MainView.fxml");
    }

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

    private void cargarVista(String fxmlPath) {
        try {
            // Verificación adicional del recurso
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Archivo no encontrado: " + fxmlPath + 
                    "\nRuta absoluta: " + new File("src/main/resources" + fxmlPath).getAbsolutePath());
            }

            // Carga con verificación de existencia
            InputStream fxmlStream = resourceUrl.openStream();
            fxmlStream.close(); // Solo para verificar que se puede leer
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent vista = loader.load();
            
            contenidoPane.getChildren().setAll(vista);
        } catch (IOException e) {
            mostrarError("Error Crítico", 
                "No se pudo cargar:\n" + fxmlPath + 
                "\n\nError: " + e.getMessage() +
                "\n\nClassLoader: " + getClass().getClassLoader().getResource(fxmlPath));
            e.printStackTrace();
        }
    }   


    @FXML
    private void cerrarSesion() {
        boolean confirmacion = mostrarConfirmacion(
            "Cerrar Sesión", 
            "¿Está seguro que desea salir del sistema?"
        );
        
        if (confirmacion) {
            regresarALogin();
        }
    }

    @FXML
    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        // Aplicar estilos CSS
        aplicarEstilosAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstilosAlert(alert);
        alert.showAndWait();
    }

    @FXML
    private void aplicarEstilosAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(
                    getClass().getResource("/styles/main.css")
                ).toExternalForm()
            );
        } catch (NullPointerException e) {
            System.err.println("No se encontró el archivo CSS: " + e.getMessage());
        }
    }

    private void regresarALogin() {
        cerrarVentanaActual();
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(
                FXMLLoader.load(
                    Objects.requireNonNull(
                        getClass().getResource("/com/cafeteriapos/views/LoginView.fxml")
                    )
                )
            ));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error Crítico", "No se pudo cargar el login: " + e.getMessage());
            System.exit(1);
        }
    }

    private void cerrarVentanaActual() {
        ((Stage) contenidoPane.getScene().getWindow()).close();
    }
}