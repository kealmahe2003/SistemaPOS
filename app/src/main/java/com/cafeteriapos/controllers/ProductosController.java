package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.utils.DatabaseManager;
import com.cafeteriapos.performance.DatabaseQueryOptimizer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProductosController {
    private static final Logger logger = LoggerFactory.getLogger(ProductosController.class);
    
    // Sistema de actualización automática
    private ScheduledExecutorService actualizacionService;
    private DatabaseQueryOptimizer optimizer;

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
        inicializarSistemaActualizacion();
        cargarProductos();
    }
    
    /**
     * Inicializa el sistema de actualización automática de productos
     */
    private void inicializarSistemaActualizacion() {
        try {
            // Obtener instancia del optimizer
            optimizer = DatabaseQueryOptimizer.getInstance();
            
            // Configurar actualización automática cada 10 segundos
            actualizacionService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ProductosController-Updater");
                t.setDaemon(true);
                return t;
            });
            
            // Verificar cambios cada 10 segundos
            actualizacionService.scheduleAtFixedRate(this::verificarCambiosEnProductos, 
                                                   10, 10, TimeUnit.SECONDS);
            
            logger.info("Sistema de actualización automática de productos iniciado");
            
        } catch (Exception e) {
            logger.error("Error al inicializar sistema de actualización: {}", e.getMessage());
        }
    }
    
    /**
     * Verifica si hay cambios en los productos y recarga si es necesario
     */
    private void verificarCambiosEnProductos() {
        try {
            // Usar DatabaseManager para obtener productos actuales desde H2 (1-5ms)
            List<Producto> productosActuales = DatabaseManager.leerProductos();
            
            // Comparar con los productos en memoria
            if (productosActuales.size() != productos.size() || 
                !productosActuales.equals(productos)) {
                
                logger.info("Cambios detectados en productos, recargando datos...");
                
                // Actualizar en el hilo de JavaFX
                Platform.runLater(() -> {
                    productos.setAll(productosActuales);
                    logger.info("Productos actualizados: {} productos cargados", productosActuales.size());
                });
            }
            
        } catch (Exception e) {
            logger.debug("Error en verificación de cambios: {}", e.getMessage());
        }
    }

    /**
     * Método para recrear el archivo Excel en caso de corrupción extrema
     */
    @FXML
    private void crearBackupBaseDatos() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Crear Backup Base de Datos");
        confirmacion.setHeaderText("¿Crear backup de la base de datos?");
        confirmacion.setContentText(
            "Esta acción creará un backup completo de la base de datos H2.\n" +
            "El backup se guardará en la carpeta data.\n\n" +
            "¿Desea continuar?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseManager.crearBackup()) {
                    mostrarAlerta("Éxito", 
                        "Backup de base de datos creado exitosamente.\n" +
                        "El archivo de backup se encuentra en la carpeta data.");
                    
                    // Recargar productos después del backup
                    cargarProductos();
                } else {
                    mostrarError("Error", 
                        "No se pudo crear el backup de la base de datos.\n" +
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
            List<Producto> productosLeidos = DatabaseManager.leerProductos();
            productos.setAll(productosLeidos);
            
            if (productosLeidos.isEmpty()) {
                mostrarAlerta("Información", 
                    "No se encontraron productos en la base de datos.\n" +
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
            DatabaseManager.guardarProducto(producto);
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
                        DatabaseManager.eliminarProducto(seleccionado);
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
                DatabaseManager.actualizarProducto(seleccionado); // Usar método actualizar
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
    
    /**
     * Método de limpieza cuando se destruye el controller
     */
    public void shutdown() {
        if (actualizacionService != null && !actualizacionService.isShutdown()) {
            actualizacionService.shutdown();
            try {
                if (!actualizacionService.awaitTermination(2, TimeUnit.SECONDS)) {
                    actualizacionService.shutdownNow();
                }
                logger.info("Sistema de actualización de productos detenido correctamente");
            } catch (InterruptedException e) {
                actualizacionService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}