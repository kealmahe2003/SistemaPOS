package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.CajaManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.util.UUID;

public class VentasController {
    // Componentes UI
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, Double> columnaPrecio;
    @FXML private Label lblTotal;
    @FXML private TextField tfBusqueda;
    
    // Datos
    private ObservableList<Producto> carrito = FXCollections.observableArrayList();
    private ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        configurarTabla();
        cargarProductosEjemplo(); // Reemplazar con tu lógica de carga real
    }
    
    private void configurarTabla() {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tablaProductos.setItems(productosDisponibles);
    }
    
    private void cargarProductosEjemplo() {
        productosDisponibles.addAll(
            new Producto("Café Americano", 2.50),
            new Producto("Capuchino", 3.00),
            new Producto("Sandwich", 4.50)
        );
    }
    
    @FXML
    private void agregarAlCarrito() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carrito.add(seleccionado);
            actualizarTotal();
        }
    }
    
    @FXML
    private void removerDelCarrito() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            carrito.remove(seleccionado);
            actualizarTotal();
        }
    }
    
    @FXML
    private void finalizarVenta() {
        if (!carrito.isEmpty()) {
            Venta nuevaVenta = new Venta(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                carrito,
                calcularTotal()
            );
            
            // Registrar en caja
            CajaManager.registrarVenta(
                nuevaVenta.getId(), 
                nuevaVenta.getTotal()
            );
            
            mostrarAlerta("Venta registrada", 
                "ID: " + nuevaVenta.getId() + 
                "\nTotal: $" + nuevaVenta.getTotal());
            
            carrito.clear();
            actualizarTotal();
        }
    }
    
    private double calcularTotal() {
        return carrito.stream()
            .mapToDouble(Producto::getPrecio)
            .sum();
    }
    
    private void actualizarTotal() {
        lblTotal.setText(String.format("Total: $%.2f", calcularTotal()));
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}