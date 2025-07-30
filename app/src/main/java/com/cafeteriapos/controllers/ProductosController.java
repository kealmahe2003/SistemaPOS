package com.cafeteriapos.controllers;

public class ProductosController {
    @FXML private TableView<Producto> tablaProductos;
    private ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabla();
        cargarProductos();
    }

    private void cargarProductos() {
        productos.setAll(ExcelManager.leerProductos()); // Integrar con Excel
    }
}
