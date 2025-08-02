package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProductosController {
    private static final Logger logger = LoggerFactory.getLogger(ProductosController.class);

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

    /**
     * Método para recrear el archivo Excel en caso de corrupción extrema
     */
    @FXML
    private void recrearArchivoExcel() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Recrear Archivo Excel");
        confirmacion.setHeaderText("¿Recrear archivo Excel?");
        confirmacion.setContentText(
            "Esta acción creará un archivo Excel completamente nuevo.\n" +
            "El archivo actual será respaldado.\n\n" +
            "¿Desea continuar?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (ExcelManager.forzarRecreacionArchivo()) {
                    mostrarAlerta("Éxito", 
                        "Archivo Excel recreado exitosamente.\n" +
                        "El archivo anterior fue respaldado.\n" +
                        "Puede comenzar a agregar productos nuevamente.");
                    
                    // Limpiar la lista actual y recargar
                    productos.clear();
                    cargarProductos();
                    limpiarFormulario();
                } else {
                    mostrarError("Error", 
                        "No se pudo recrear el archivo Excel.\n" +
                        "Verifique los permisos del directorio.");
                }
            }
        });
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
        try {
            List<Producto> productosLeidos = ExcelManager.leerProductos();
            productos.setAll(productosLeidos);
            
            if (productosLeidos.isEmpty()) {
                mostrarAlerta("Información", 
                    "No se encontraron productos en el archivo.\n" +
                    "Puede comenzar agregando nuevos productos.");
            } else {
                logger.info("Productos cargados: {}", productosLeidos.size());
            }
            
        } catch (Exception e) {
            mostrarError("Error al cargar productos", 
                "Error al cargar productos: " + e.getMessage() + "\n" +
                "La aplicación continuará con una lista vacía.\n" +
                "Puede agregar nuevos productos normalmente.");
            
            // Asegurar que la lista esté inicializada aunque haya error
            productos.clear();
        }
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

    @FXML
    private void eliminarProducto() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText(
                "¿Está seguro de que desea eliminar el producto: " + seleccionado.getNombre() + "?\n\n" +
                "Esta acción no se puede deshacer.");

            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        productos.remove(seleccionado);
                        ExcelManager.eliminarProducto(seleccionado);
                        mostrarAlerta("Éxito", "Producto eliminado correctamente");
                        limpiarFormulario();
                    } catch (Exception e) {
                        mostrarError("Error", "No se pudo eliminar el producto: " + e.getMessage());
                        // Revertir el cambio en la lista local
                        productos.add(seleccionado);
                    }
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
                validarFormulario(); // Validar antes de actualizar
                
                seleccionado.setNombre(tfNombre.getText().trim());
                seleccionado.setPrecio(Double.parseDouble(tfPrecio.getText()));
                seleccionado.setStock(spinnerStock.getValue());
                
                tablaProductos.refresh();
                ExcelManager.actualizarProducto(seleccionado); // Usar método actualizar
                limpiarFormulario();
                
                mostrarAlerta("Éxito", "Producto actualizado correctamente");
                
            } catch (NumberFormatException e) {
                mostrarError("Error", "El precio debe ser un número válido");
            } catch (Exception e) {
                mostrarError("Error", "Error al actualizar producto: " + e.getMessage());
            }
        } else {
            mostrarError("Error", "Seleccione un producto para actualizar");
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