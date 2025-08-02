package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para el Dashboard moderno con estadísticas avanzadas
 * Maneja gráficos, métricas y análisis de datos de ventas con diseño moderno
 */
public class DashboardController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // === NUEVOS CAMPOS DEL DASHBOARD MODERNO ===
    
    // Tarjeta Ventas del Día
    @FXML private Label lblVentasHoy;
    @FXML private Label lblTransaccionesHoy;
    @FXML private Label lblPromedioVenta;
    
    // Tarjeta Producto Estrella
    @FXML private Label lblProductoTop;
    @FXML private Label lblCantidadTop;
    @FXML private Label lblIngresosTop;
    
    // Tarjeta Balance Mensual - Simplificada
    @FXML private Label lblIngresosHoy;
    @FXML private Label lblCostosHoy;
    
    // Gráficos principales
    @FXML private BarChart<String, Number> chartVentasDiarias;
    @FXML private CategoryAxis xAxisDias;
    @FXML private NumberAxis yAxisVentas;
    @FXML private PieChart chartProductosTop;
    
    // === DATA STORAGE ===
    private List<Venta> ventasData;
    private List<Producto> productosData;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando Dashboard Controller Moderno");
        
        // Configurar gráficos
        configurarGraficos();
        
        // Cargar datos iniciales
        cargarDatos();
        
        // Actualizar vista
        actualizarDatos();
        
        logger.info("Dashboard Controller Moderno inicializado exitosamente");
    }
    
    /**
     * Configura las propiedades básicas de los gráficos
     */
    private void configurarGraficos() {
        // Configurar BarChart principal
        if (yAxisVentas != null) {
            yAxisVentas.setLabel("Ingresos ($)");
        }
        if (xAxisDias != null) {
            xAxisDias.setLabel("Días");
        }
        if (chartVentasDiarias != null) {
            chartVentasDiarias.setLegendVisible(false);
            chartVentasDiarias.setAnimated(true);
        }
        
        // Configurar PieChart
        if (chartProductosTop != null) {
            chartProductosTop.setLegendVisible(true);
            chartProductosTop.setAnimated(true);
        }
        
        logger.debug("Gráficos configurados correctamente");
    }
    
    /**
     * Carga los datos reales desde Excel Manager
     */
    private void cargarDatos() {
        try {
            logger.info("Cargando datos reales para dashboard...");
            
            // Cargar productos reales
            productosData = ExcelManager.leerProductos();
            logger.debug("Productos cargados: {}", productosData.size());
            
            // Cargar ventas reales desde Excel
            ventasData = ExcelManager.leerVentas();
            logger.debug("Ventas cargadas: {}", ventasData.size());
            
            logger.info("Datos reales cargados exitosamente: {} productos, {} ventas", 
                productosData.size(), ventasData.size());
                
        } catch (Exception e) {
            logger.error("Error cargando datos reales: {}", e.getMessage());
            // Inicializar listas vacías para evitar errores
            productosData = new ArrayList<>();
            ventasData = new ArrayList<>();
        }
    }
    
    /**
     * Actualiza todos los datos y gráficos del dashboard
     */
    @FXML
    public void actualizarDatos() {
        logger.info("Actualizando datos del dashboard");
        
        try {
            // Recargar datos
            cargarDatos();
            
            // Actualizar métricas modernas
            actualizarMetricasModernas();
            
            // Actualizar gráficos
            actualizarGraficoVentasDiarias();
            actualizarGraficoProductosTop();
            
            logger.info("Dashboard actualizado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error actualizando dashboard: {}", e.getMessage());
        }
    }
    
    /**
     * Actualiza las métricas del dashboard moderno
     */
    private void actualizarMetricasModernas() {
        LocalDate hoy = LocalDate.now();
        
        // === TARJETA VENTAS DEL DÍA ===
        List<Venta> ventasHoy = ventasData.stream()
            .filter(v -> v.getFecha().equals(hoy))
            .collect(Collectors.toList());
        
        double totalHoy = ventasHoy.stream()
            .mapToDouble(Venta::getTotal)
            .sum();
        
        double promedioVenta = ventasHoy.isEmpty() ? 0 : totalHoy / ventasHoy.size();
        
        if (lblVentasHoy != null) {
            lblVentasHoy.setText(String.format("$%.2f", totalHoy));
        }
        if (lblTransaccionesHoy != null) {
            lblTransaccionesHoy.setText(ventasHoy.size() + " transacciones");
        }
        if (lblPromedioVenta != null) {
            lblPromedioVenta.setText(String.format("Promedio: $%.0f", promedioVenta));
        }
        
        // === TARJETA PRODUCTO ESTRELLA ===
        if (!productosData.isEmpty() && !ventasData.isEmpty()) {
            // Calcular el producto más vendido basado en datos reales
            Map<String, Integer> ventasPorProducto = new HashMap<>();
            Map<String, Double> ingresosPorProducto = new HashMap<>();
            
            // Procesar ventas reales para encontrar el producto más vendido
            for (Venta venta : ventasData) {
                if (venta.getItems() != null) {
                    for (Producto prod : venta.getItems()) {
                        String nombre = prod.getNombre();
                        ventasPorProducto.put(nombre, 
                            ventasPorProducto.getOrDefault(nombre, 0) + 1);
                        ingresosPorProducto.put(nombre,
                            ingresosPorProducto.getOrDefault(nombre, 0.0) + prod.getPrecio());
                    }
                }
            }
            
            if (!ventasPorProducto.isEmpty()) {
                // Encontrar el producto con más ventas
                Optional<Map.Entry<String, Integer>> productoTopEntry = ventasPorProducto.entrySet().stream()
                    .max(Map.Entry.comparingByValue());
                
                if (productoTopEntry.isPresent()) {
                    String productoTopNombre = productoTopEntry.get().getKey();
                    int cantidadVendida = ventasPorProducto.get(productoTopNombre);
                    double ingresosProducto = ingresosPorProducto.get(productoTopNombre);
                    
                    if (lblProductoTop != null) {
                        lblProductoTop.setText(productoTopNombre);
                    }
                    if (lblCantidadTop != null) {
                        lblCantidadTop.setText(String.valueOf(cantidadVendida));
                    }
                    if (lblIngresosTop != null) {
                        lblIngresosTop.setText(String.format("$%.0f", ingresosProducto));
                    }
                } else {
                    // Sin datos válidos
                    if (lblProductoTop != null) lblProductoTop.setText("Sin datos");
                    if (lblCantidadTop != null) lblCantidadTop.setText("0");
                    if (lblIngresosTop != null) lblIngresosTop.setText("$0");
                }
            } else {
                // Sin datos de ventas con productos
                if (lblProductoTop != null) lblProductoTop.setText("Sin ventas");
                if (lblCantidadTop != null) lblCantidadTop.setText("0");
                if (lblIngresosTop != null) lblIngresosTop.setText("$0");
            }
        } else {
            // Valores por defecto cuando no hay datos
            if (lblProductoTop != null) lblProductoTop.setText("Sin productos");
            if (lblCantidadTop != null) lblCantidadTop.setText("0");
            if (lblIngresosTop != null) lblIngresosTop.setText("$0");
        }
        
        // === TARJETA BALANCE MENSUAL ===
        double ingresosMensuales = ventasData.stream()
            .filter(v -> v.getFecha().getMonthValue() == hoy.getMonthValue())
            .mapToDouble(Venta::getTotal)
            .sum();
        
        // Simular costos (25% de los ingresos)
        double costosMensuales = ingresosMensuales * 0.25;
        
        if (lblIngresosHoy != null) {
            lblIngresosHoy.setText(String.format("$%.0f", ingresosMensuales));
        }
        if (lblCostosHoy != null) {
            lblCostosHoy.setText(String.format("$%.0f", costosMensuales));
        }
        
        logger.debug("Métricas modernas actualizadas: Hoy=${}", totalHoy);
    }
    
    /**
     * Actualiza el gráfico de barras de ventas diarias
     */
    private void actualizarGraficoVentasDiarias() {
        if (chartVentasDiarias == null) return;
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Diarias");
        
        // Agrupar ventas por día
        Map<LocalDate, Double> ventasPorDia = ventasData.stream()
            .collect(Collectors.groupingBy(
                Venta::getFecha,
                Collectors.summingDouble(Venta::getTotal)
            ));
        
        // Crear datos para últimos 7 días
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            String diaStr = fecha.format(formatter);
            double total = ventasPorDia.getOrDefault(fecha, 0.0);
            
            series.getData().add(new XYChart.Data<>(diaStr, total));
        }
        
        chartVentasDiarias.getData().clear();
        chartVentasDiarias.getData().add(series);
        
        logger.debug("Gráfico de ventas diarias actualizado con {} datos", series.getData().size());
    }
    
    /**
     * Actualiza el gráfico circular de productos más vendidos
     */
    private void actualizarGraficoProductosTop() {
        if (chartProductosTop == null) return;
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (!productosData.isEmpty() && !ventasData.isEmpty()) {
            // Calcular ventas reales por producto
            Map<String, Integer> ventasPorProducto = new HashMap<>();
            
            for (Venta venta : ventasData) {
                if (venta.getItems() != null) {
                    for (Producto prod : venta.getItems()) {
                        String nombre = prod.getNombre();
                        ventasPorProducto.put(nombre, 
                            ventasPorProducto.getOrDefault(nombre, 0) + 1);
                    }
                }
            }
            
            // Tomar los top 5 productos más vendidos
            ventasPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                });
        }
        
        if (pieChartData.isEmpty()) {
            // Datos por defecto si no hay ventas
            pieChartData.add(new PieChart.Data("Sin datos", 1));
        }
        
        chartProductosTop.setData(pieChartData);
        
        logger.debug("Gráfico de productos top actualizado con {} productos", pieChartData.size());
    }
    
}
