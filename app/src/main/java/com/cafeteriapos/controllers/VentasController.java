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

        if (seleccionado.getStock() <= 0) {
            mostrarAlerta("Sin stock", "No hay suficiente stock de este producto");
            return;
        }

        // Buscar si ya existe en carrito
        ItemCarrito itemExistente = carrito.stream()
            .filter(item -> item.getProducto().equals(seleccionado))
            .findFirst()
            .orElse(null);

        if (itemExistente != null) {
            itemExistente.setCantidad(itemExistente.getCantidad() + 1);
        } else {
            carrito.add(new ItemCarrito(seleccionado, 1));
        }

        actualizarTotal();
        tablaCarrito.refresh();
    }

    @FXML
    private void removerDelCarrito() {
        ItemCarrito seleccionado = tablaCarrito.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (seleccionado.getCantidad() > 1) {
                seleccionado.setCantidad(seleccionado.getCantidad() - 1);
            } else {
                carrito.remove(seleccionado);
            }
            actualizarTotal();
        }
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
        ObservableList<ItemCarrito> itemsCopiados = FXCollections.observableArrayList();
        carrito.forEach(item -> itemsCopiados.add(new ItemCarrito(item.getProducto(), item.getCantidad())));
        
        // Convertir los items del carrito a una lista de productos o el tipo que espera Venta
        // Suponiendo que Venta espera List<Producto>:
        ObservableList<Producto> productosVenta = FXCollections.observableArrayList();
        itemsCopiados.forEach(item -> {
            Producto producto = new Producto(item.getProducto().getNombre(), item.getProducto().getPrecio(), item.getCantidad());
            productosVenta.add(producto);
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
            // Actualizar stocks
            venta.getItems().forEach(item -> {
                Producto p = item;
                p.setStock(p.getStock() - p.getStock()); // You may want to adjust this line depending on your logic
                ExcelManager.guardarProducto(p);
            });

            ExcelManager.guardarVenta(venta);
            CajaManager.registrarVenta(venta.getId(), venta.getTotal());
            
            mostrarAlerta("Éxito", String.format(
                "Venta registrada\nID: %s\nTotal: $%.2f", 
                venta.getId(), venta.getTotal()));
                
            limpiarCarrito();
            cargarProductos(); // Refrescar datos

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