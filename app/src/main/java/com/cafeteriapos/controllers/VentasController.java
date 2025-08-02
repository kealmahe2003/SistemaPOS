package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.CajaManager;
import com.cafeteriapos.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VentasController {

    // Componentes UI
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, Double> columnaPrecio;
    @FXML private TableColumn<Producto, Integer> columnaStock;
    @FXML private TableView<ItemCarrito> tablaCarrito;
    @FXML private TableColumn<ItemCarrito, String> colCarritoNombre;
    @FXML private TableColumn<ItemCarrito, Integer> colCarritoCantidad;
    @FXML private TableColumn<ItemCarrito, Double> colCarritoSubtotal;
    @FXML private Label lblTotal;
    @FXML private TextField tfBusqueda;
    @FXML private Spinner<Integer> spinnerCantidad;
    @FXML private Spinner<Integer> spinnerEliminar;

    // Datos
    private final ObservableList<ItemCarrito> carrito = FXCollections.observableArrayList();
    private final FilteredList<Producto> productosFiltrados;
    private final ObservableList<Producto> productosDisponibles;

    public VentasController() {
        productosDisponibles = FXCollections.observableArrayList(cargarProductosSafely());
        productosFiltrados = new FilteredList<>(productosDisponibles);
    }

    private List<Producto> cargarProductosSafely() {
        try {
            List<Producto> productos = ExcelManager.leerProductos();
            
            // Si no hay productos, crear algunos en memoria sin guardar en Excel por ahora
            if (productos.isEmpty()) {
                System.out.println("No se encontraron productos. Usando productos de ejemplo en memoria...");
                return crearProductosDeEjemploEnMemoria();
            }
            
            return productos;
        } catch (Exception e) {
            System.err.println("Error al cargar productos desde Excel: " + e.getMessage());
            System.err.println("Usando productos de ejemplo en memoria...");
            // Retornar productos de ejemplo en memoria si hay error
            return crearProductosDeEjemploEnMemoria();
        }
    }

    private List<Producto> crearProductosDeEjemploEnMemoria() {
        List<Producto> productos = new ArrayList<>();
        productos.add(new Producto("Café Americano", 2.50, 50));
        productos.add(new Producto("Café con Leche", 3.00, 45));
        productos.add(new Producto("Croissant", 1.75, 20));
        productos.add(new Producto("Muffin de Chocolate", 2.25, 15));
        productos.add(new Producto("Sandwich Mixto", 4.50, 10));
        System.out.println("Productos de ejemplo cargados en memoria.");
        return productos;
    }

    @FXML
    public void initialize() {
        configurarTablaProductos();
        configurarTablaCarrito();
        configurarSpinners();
        cargarProductos();
        configurarBusqueda();
        aplicarEstilosCSS();
    }

    private void aplicarEstilosCSS() {
        try {
            // Intentar aplicar el CSS de ventas
            String cssPath = getClass().getResource("/styles/ventas.css") != null 
                ? getClass().getResource("/styles/ventas.css").toExternalForm()
                : null;
            
            if (cssPath != null && tablaProductos.getScene() != null) {
                tablaProductos.getScene().getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el CSS: " + e.getMessage());
            // Continuar sin CSS, la funcionalidad no se ve afectada
        }
    }

    private void configurarTablaProductos() {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnaStock.setCellFactory(new StockCellFactory());
        tablaProductos.setItems(productosFiltrados);
    }

    private void configurarTablaCarrito() {
        colCarritoNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCarritoCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarritoSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaCarrito.setItems(carrito);
    }

    private void configurarSpinners() {
        // Configurar spinner para agregar productos
        SpinnerValueFactory<Integer> valueFactoryAgregar = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerCantidad.setValueFactory(valueFactoryAgregar);
        spinnerCantidad.setEditable(true);

        // Configurar spinner para eliminar productos
        SpinnerValueFactory<Integer> valueFactoryEliminar = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerEliminar.setValueFactory(valueFactoryEliminar);
        spinnerEliminar.setEditable(true);

        // Actualizar el máximo del spinner de eliminar cuando se selecciona un item del carrito
        tablaCarrito.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                SpinnerValueFactory<Integer> factory = 
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, newVal.getCantidad(), 1);
                spinnerEliminar.setValueFactory(factory);
            }
        });
    }

    private void cargarProductos() {
        productosDisponibles.setAll(ExcelManager.leerProductos());
    }

    private void configurarBusqueda() {
        tfBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            productosFiltrados.setPredicate(producto -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return producto.getNombre().toLowerCase().contains(newVal.toLowerCase());
            });
        });
    }

    @FXML
    private void agregarAlCarrito() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        
        if (seleccionado == null) {
            mostrarAlerta("Error", "Seleccione un producto primero");
            return;
        }

        int cantidadDeseada = spinnerCantidad.getValue();
        
        if (seleccionado.getStock() < cantidadDeseada) {
            mostrarAlerta("Sin stock", String.format(
                "No hay suficiente stock. Disponible: %d, Solicitado: %d", 
                seleccionado.getStock(), cantidadDeseada));
            return;
        }

        // Buscar si ya existe en carrito
        ItemCarrito itemExistente = carrito.stream()
            .filter(item -> item.getProducto().getNombre().equals(seleccionado.getNombre()))
            .findFirst()
            .orElse(null);

        if (itemExistente != null) {
            int nuevaCantidad = itemExistente.getCantidad() + cantidadDeseada;
            if (seleccionado.getStock() < nuevaCantidad) {
                mostrarAlerta("Sin stock", String.format(
                    "No hay suficiente stock. Ya tienes %d en el carrito, stock disponible: %d", 
                    itemExistente.getCantidad(), seleccionado.getStock()));
                return;
            }
            itemExistente.setCantidad(nuevaCantidad);
        } else {
            carrito.add(new ItemCarrito(seleccionado, cantidadDeseada));
        }

        // Resetear spinner
        spinnerCantidad.getValueFactory().setValue(1);
        
        actualizarTotal();
        tablaCarrito.refresh();
        
        mostrarAlerta("Éxito", String.format("Se agregaron %d unidades de %s al carrito", 
            cantidadDeseada, seleccionado.getNombre()));
    }

    @FXML
    private void removerDelCarrito() {
        ItemCarrito seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        
        if (seleccionado == null) {
            mostrarAlerta("Error", "Seleccione un item del carrito primero");
            return;
        }

        int cantidadAEliminar = spinnerEliminar.getValue();
        
        if (cantidadAEliminar >= seleccionado.getCantidad()) {
            // Eliminar completamente el item
            carrito.remove(seleccionado);
            mostrarAlerta("Éxito", String.format("Se eliminó %s del carrito completamente", 
                seleccionado.getNombreProducto()));
        } else {
            // Reducir la cantidad
            seleccionado.setCantidad(seleccionado.getCantidad() - cantidadAEliminar);
            mostrarAlerta("Éxito", String.format("Se eliminaron %d unidades de %s del carrito", 
                cantidadAEliminar, seleccionado.getNombreProducto()));
        }
        
        // Resetear spinner
        spinnerEliminar.getValueFactory().setValue(1);
        
        actualizarTotal();
        tablaCarrito.refresh();
    }

    @FXML
    private void finalizarVenta() {
        if (carrito.isEmpty()) {
            mostrarAlerta("Error", "El carrito está vacío");
            return;
        }

        if (!validarStockDisponible()) {
            mostrarAlerta("Error", "No hay suficiente stock para algunos productos");
            return;
        }

        Venta venta = crearVenta();
        registrarVenta(venta);
    }

    private boolean validarStockDisponible() {
        return carrito.stream().allMatch(item -> 
            item.getProducto().getStock() >= item.getCantidad()
        );
    }

    private Venta crearVenta() {
        // Crear una lista de productos que representen los items vendidos
        // En esta lista, el "stock" del producto representa la cantidad vendida
        ObservableList<Producto> productosVenta = FXCollections.observableArrayList();
        
        carrito.forEach(item -> {
            // Crear un producto donde el "stock" representa la cantidad vendida
            // Esto es solo para el registro de la venta, no afecta el inventario real
            Producto productoVenta = new Producto(
                item.getProducto().getNombre(), 
                item.getProducto().getPrecio(), 
                item.getCantidad() // La cantidad vendida se guarda como "stock" en el registro de venta
            );
            productosVenta.add(productoVenta);
        });

        return new Venta(
            generarIdVenta(),
            LocalDateTime.now(),
            productosVenta,
            calcularTotal()
        );
    }

    private void registrarVenta(Venta venta) {
        try {
            // Actualizar stocks de los productos originales en la lista
            carrito.forEach(itemCarrito -> {
                // Buscar el producto original en la lista de productos disponibles
                Producto productoOriginal = productosDisponibles.stream()
                    .filter(p -> p.getNombre().equals(itemCarrito.getProducto().getNombre()))
                    .findFirst()
                    .orElse(null);
                
                if (productoOriginal != null) {
                    // Actualizar el stock del producto original
                    int nuevoStock = productoOriginal.getStock() - itemCarrito.getCantidad();
                    productoOriginal.setStock(Math.max(0, nuevoStock)); // Asegurar que no sea negativo
                    
                    // Guardar el producto actualizado en Excel
                    ExcelManager.actualizarProducto(productoOriginal);
                }
            });

            // Guardar la venta
            ExcelManager.guardarVenta(venta);
            CajaManager.registrarVenta(venta.getId(), venta.getTotal());
            
            mostrarAlerta("Éxito", String.format(
                "Venta registrada\nID: %s\nTotal: $%.2f", 
                venta.getId(), venta.getTotal()));
                
            limpiarCarrito();
            
            // Refrescar la tabla para mostrar los nuevos stocks
            tablaProductos.refresh();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo registrar: " + e.getMessage());
        }
    }

    private double calcularTotal() {
        return carrito.stream()
            .mapToDouble(ItemCarrito::getSubtotal)
            .sum();
    }

    private void actualizarTotal() {
        lblTotal.setText(String.format("$%.2f", calcularTotal()));
    }

    @FXML
    private void limpiarCarrito() {
        carrito.clear();
        actualizarTotal();
    }

    private String generarIdVenta() {
        return "V-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna para items del carrito
    public static class ItemCarrito {
        private final Producto producto;
        private int cantidad;

        public ItemCarrito(Producto producto, int cantidad) {
            this.producto = new Producto(producto.getNombre(), producto.getPrecio(), producto.getStock()); // Copia defensiva
            this.cantidad = cantidad;
        }

        // Getters
        public String getNombreProducto() { return producto.getNombre(); }
        public double getSubtotal() { return producto.getPrecio() * cantidad; }
        public Producto getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }

    // Factory para celdas de stock
    private static class StockCellFactory implements Callback<TableColumn<Producto, Integer>, TableCell<Producto, Integer>> {
        @Override
        public TableCell<Producto, Integer> call(TableColumn<Producto, Integer> param) {
            return new TableCell<>() {
                @Override
                protected void updateItem(Integer stock, boolean empty) {
                    super.updateItem(stock, empty);
                    if (empty || stock == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(stock.toString());
                        if (stock < 3) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else if (stock < 10) {
                            setStyle("-fx-text-fill: orange;");
                        }
                    }
                }
            };
        }
    }
}