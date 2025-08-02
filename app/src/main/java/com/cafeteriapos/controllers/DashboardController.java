package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para la vista del Dashboard con estadísticas de ventas
 * Maneja gráficos, métricas y análisis de datos de ventas
 */
public class DashboardController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // === FXML COMPONENTS ===
    
    // Métricas
    @FXML private Label lblVentasHoy;
    // @FXML private Label lblCantidadVentasHoy; // No existe en FXML actual
    @FXML private Label lblProductoTop;
    // @FXML private Label lblVentasProductoTop; // No existe en FXML actual
    // @FXML private Label lblTotalMensual; // No existe en FXML actual
    // @FXML private Label lblPromedioMensual; // No existe en FXML actual
    // @FXML private Label lblUltimaActualizacion; // No existe en FXML actual
    
    // Gráficos
    @FXML private BarChart<String, Number> chartVentasDiarias;
    @FXML private CategoryAxis xAxisDias;
    @FXML private NumberAxis yAxisVentas;
    @FXML private PieChart chartProductosTop;
    
    // Controles
    @FXML private ComboBox<String> comboPeriodo;
    
    // === DATA STORAGE ===
    private List<Venta> ventasData;
    private List<Producto> productosData;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando Dashboard Controller");
        
        // Configurar gráficos
        configurarGraficos();
        
        // Cargar datos iniciales
        cargarDatos();
        
        // Actualizar vista
        actualizarDatos();
        
        logger.info("Dashboard Controller inicializado exitosamente");
    }
    
    /**
     * Configura las propiedades básicas de los gráficos
     */
    private void configurarGraficos() {
        // Configurar BarChart
        yAxisVentas.setLabel("Ingresos ($)");
        xAxisDias.setLabel("Días");
        chartVentasDiarias.setTitle("Ventas Diarias");
        chartVentasDiarias.setLegendVisible(false);
        
        // Configurar PieChart
        chartProductosTop.setTitle("Productos Más Vendidos");
        chartProductosTop.setLegendVisible(true);
        
        logger.debug("Gráficos configurados correctamente");
    }
    
    /**
     * Carga los datos desde Excel Manager
     */
    private void cargarDatos() {
        try {
            logger.info("Cargando datos para dashboard...");
            
            // Cargar productos
            productosData = ExcelManager.leerProductos();
            logger.debug("Productos cargados: {}", productosData.size());
            
            // Cargar ventas reales desde Excel
            ventasData = ExcelManager.leerVentas();
            logger.debug("Ventas cargadas: {}", ventasData.size());
            
            // Si no hay ventas, simular algunas para demostración
            if (ventasData.isEmpty()) {
                logger.info("No hay ventas reales, generando datos de ejemplo...");
                ventasData = cargarVentasSimuladas();
            }
            
        } catch (Exception e) {
            logger.error("Error cargando datos para dashboard: {}", e.getMessage());
            // Inicializar listas vacías para evitar errores
            productosData = new ArrayList<>();
            ventasData = new ArrayList<>();
        }
    }
    
    /**
     * Método para generar datos de ventas de ejemplo cuando no hay ventas reales
     * Usado solo para demostración cuando el Excel no tiene ventas registradas
     */
    private List<Venta> cargarVentasSimuladas() {
        List<Venta> ventas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        
        // Simular 7 días de ventas
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.minusDays(i);
            
            // Simular 2-5 ventas por día
            int ventasPorDia = 2 + (int)(Math.random() * 4);
            for (int j = 0; j < ventasPorDia; j++) {
                Venta venta = new Venta();
                venta.setFecha(fecha);
                venta.setTotal(20.0 + (Math.random() * 80.0)); // $20-100
                ventas.add(venta);
            }
        }
        
        return ventas;
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
            
            // Actualizar métricas
            actualizarMetricas();
            
            // Actualizar gráficos
            actualizarGraficoVentasDiarias();
            actualizarGraficoProductosTop();
            
            // Actualizar timestamp
            // lblUltimaActualizacion.setText("Ultima actualizacion: " + 
            //     LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            logger.info("Dashboard actualizado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error actualizando dashboard: {}", e.getMessage());
        }
    }
    
    /**
     * Actualiza las métricas numéricas del dashboard
     */
    private void actualizarMetricas() {
        LocalDate hoy = LocalDate.now();
        
        // Ventas de hoy
        List<Venta> ventasHoy = ventasData.stream()
            .filter(v -> v.getFecha().equals(hoy))
            .collect(Collectors.toList());
        
        double totalHoy = ventasHoy.stream()
            .mapToDouble(Venta::getTotal)
            .sum();
        
        lblVentasHoy.setText(String.format("$%.2f", totalHoy));
        // lblCantidadVentasHoy.setText(ventasHoy.size() + " transacciones");
        
        // Producto más vendido (simulado por ahora)
        if (!productosData.isEmpty()) {
            Producto productoTop = productosData.get(0);
            lblProductoTop.setText(productoTop.getNombre());
            // lblVentasProductoTop.setText("5 vendidos"); // Simulado
        }
        
        // Total mensual
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        List<Venta> ventasMes = ventasData.stream()
            .filter(v -> !v.getFecha().isBefore(inicioMes))
            .collect(Collectors.toList());
        
        double totalMes = ventasMes.stream()
            .mapToDouble(Venta::getTotal)
            .sum();
        
        double promedioMes = ventasMes.isEmpty() ? 0 : totalMes / hoy.getDayOfMonth();
        
        // lblTotalMensual.setText(String.format("$%.2f", totalMes));
        // lblPromedioMensual.setText(String.format("Promedio: $%.2f/día", promedioMes));
        
        logger.debug("Métricas actualizadas: Hoy=${}, Mes=${}", totalHoy, totalMes);
    }
    
    /**
     * Actualiza el gráfico de barras de ventas diarias
     */
    private void actualizarGraficoVentasDiarias() {
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
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Por ahora simulamos datos de productos más vendidos
        if (!productosData.isEmpty()) {
            for (int i = 0; i < Math.min(5, productosData.size()); i++) {
                Producto producto = productosData.get(i);
                // Simular ventas (en producción será calculado desde datos reales)
                double ventas = 10 + (Math.random() * 20);
                pieChartData.add(new PieChart.Data(producto.getNombre(), ventas));
            }
        } else {
            // Datos por defecto si no hay productos
            pieChartData.add(new PieChart.Data("Sin datos", 1));
        }
        
        chartProductosTop.setData(pieChartData);
        
        logger.debug("Gráfico de productos top actualizado con {} productos", pieChartData.size());
    }
    
    /**
     * Maneja el cambio de período en el ComboBox
     */
    @FXML
    public void cambiarPeriodo() {
        String periodo = comboPeriodo.getValue();
        logger.info("Cambiando período a: {}", periodo);
        
        // TODO: Implementar lógica para diferentes períodos
        // Por ahora solo actualizamos los datos
        actualizarDatos();
    }
}
