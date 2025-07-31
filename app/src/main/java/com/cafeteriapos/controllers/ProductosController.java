package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductosController {

    // Componentes UI
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, Double> columnaPrecio;
    @FXML private TableColumn<Producto, Integer> columnaStock;
    @FXML private TextField tfNombre;
    @FXML private TextField tfPrecio;
    @FXML private Spinner<Integer> spinnerStock;

    // Datos
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarValidaciones();
        configurarTabla();
        configurarSpinner();
        cargarProductos();
    }

    private void configurarValidaciones() {
        // Validación en tiempo real para el precio
        tfPrecio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                tfPrecio.setText(oldVal);
            }
        });

        // Validación para nombre (solo letras, números y espacios)
        tfNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ]*")) {
                tfNombre.setText(oldVal);
            }
        });
    }

    private void configurarTabla() {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        tablaProductos.setItems(productos);
    }

    private void configurarSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0);
        spinnerStock.setValueFactory(factory);
    }

    private void cargarProductos() {
        productos.setAll(ExcelManager.leerProductos());
    }

    @FXML
    private void agregarProducto() {
        try {
            validarFormulario();
            
            Producto producto = new Producto(
                tfNombre.getText().trim(),
                Double.parseDouble(tfPrecio.getText()),
                spinnerStock.getValue()
            );
            
            if (producto.getPrecio() <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a cero");
            }

            if (productoExiste(producto.getNombre())) {
                throw new IllegalStateException("El producto ya existe");
            }

            productos.add(producto);
            ExcelManager.guardarProducto(producto);
            limpiarFormulario();
            
        } catch (NumberFormatException e) {
            mostrarError("Formato inválido", "Ingrese un precio válido (ej: 5.99)");
        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    private boolean productoExiste(String nombre) {
        return productos.stream()
            .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
    }

    private void validarFormulario() {
        if (tfNombre.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        if (tfPrecio.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese un precio");
        }

        if (spinnerStock.getValue() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText(
                "¿Eliminar el producto: " + seleccionado.getNombre() + "?");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    productos.remove(seleccionado);
                    ExcelManager.eliminarProducto(seleccionado);
                }
            });
        } else {
            mostrarError("Error", "Seleccione un producto para eliminar");
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void actualizarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try {
                seleccionado.setNombre(tfNombre.getText().trim());
                seleccionado.setPrecio(Double.parseDouble(tfPrecio.getText()));
                seleccionado.setStock(spinnerStock.getValue());
                
                tablaProductos.refresh();
                ExcelManager.guardarProducto(seleccionado);
                limpiarFormulario();
                
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El precio debe ser un número válido");
            }
        }
    }

    @FXML
    private void seleccionarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            tfNombre.setText(seleccionado.getNombre());
            tfPrecio.setText(String.valueOf(seleccionado.getPrecio()));
            spinnerStock.getValueFactory().setValue(seleccionado.getStock());
        }
    }

    private void limpiarFormulario() {
        tfNombre.clear();
        tfPrecio.clear();
        spinnerStock.getValueFactory().setValue(0);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}