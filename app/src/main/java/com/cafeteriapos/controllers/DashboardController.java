package com.cafeteriapos.controllers;

import com.cafeteriapos.cache.DashboardCacheManager;
import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.performance.BackgroundProcessor;
import com.cafeteriapos.performance.DatabaseQueryOptimizer;
import com.cafeteriapos.utils.DatabaseManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    
    // Información del sistema
    @FXML private Label lblUltimaActualizacion;
    
    // Gráficos principales
    @FXML private BarChart<String, Number> chartVentasDiarias;
    @FXML private CategoryAxis xAxisDias;
    @FXML private NumberAxis yAxisVentas;
    @FXML private PieChart chartProductosTop;
    
    // Botones de acción
    @FXML private Button btnActualizar;
    
    // === DATA STORAGE ===
    private List<Venta> ventasData;
    private List<Producto> productosData;
    
    // === SISTEMAS DE PERFORMANCE AVANZADOS ===
    private DashboardCacheManager cacheManager;
    private DatabaseQueryOptimizer queryOptimizer;
    private BackgroundProcessor backgroundProcessor;
    
    // === CONFIGURACIÓN DE PERFORMANCE ===
    private static final long CACHE_DURATION = 5L * 60 * 1000; // 5 minutos en milisegundos
    private volatile boolean performanceSystemInitialized = false;
    
    // === TIMER PARA ACTUALIZACIÓN AUTOMÁTICA ===
    private Timeline autoUpdateTimeline;
    
    // === OPTIMIZACIÓN INTELIGENTE DE ACTUALIZACIONES ===
    private volatile long lastDataHash = 0;
    private volatile long lastUpdateTime = 0;
    private static final long MIN_UPDATE_INTERVAL = 2000; // 2 segundos mínimo entre actualizaciones
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inicializando Dashboard Controller Moderno");
        
        // === INICIALIZAR SISTEMAS DE PERFORMANCE ===
        initializePerformanceSystems();
        
        // Configurar gráficos
        configurarGraficos();
        
        // Cargar datos iniciales de manera optimizada
        cargarDatosOptimizado();
        
        // Iniciar actualización automática
        iniciarActualizacionAutomatica();
        
        // Actualizar vista con animaciones
        actualizarDatos();
        
        // Aplicar animaciones de entrada
        Platform.runLater(this::animarMetricas);
        
        logger.info("Dashboard Controller Moderno inicializado exitosamente");
    }
    
    /**
     * Inicializa los sistemas de performance avanzados
     */
    private void initializePerformanceSystems() {
        try {
            logger.info("Inicializando sistemas de performance...");
            
            // Obtener instancias de los sistemas de performance
            this.cacheManager = DashboardCacheManager.getInstance();
            this.queryOptimizer = DatabaseQueryOptimizer.getInstance();
            this.backgroundProcessor = BackgroundProcessor.getInstance();
            
            // Iniciar el procesador en segundo plano
            backgroundProcessor.start();
            
            // Precarga inicial del caché
            CompletableFuture.runAsync(() -> {
                queryOptimizer.warmupCache();
                logger.info("Cache inicial precargado exitosamente");
            }).exceptionally(throwable -> {
                logger.error("Error en precarga inicial del caché", throwable);
                return null;
            });
            
            performanceSystemInitialized = true;
            logger.info("Sistemas de performance inicializados exitosamente");
            
        } catch (Exception e) {
            logger.error("Error inicializando sistemas de performance", e);
            performanceSystemInitialized = false;
        }
    }
    
    /**
     * Carga datos de manera optimizada usando el sistema de performance
     */
    private void cargarDatosOptimizado() {
        if (performanceSystemInitialized) {
            // Usar el sistema optimizado
            CompletableFuture<List<Venta>> ventasFuture = queryOptimizer.getVentasOptimized();
            CompletableFuture<List<Producto>> productosFuture = queryOptimizer.getProductosOptimized();
            
            CompletableFuture.allOf(ventasFuture, productosFuture)
                .thenRun(() -> Platform.runLater(() -> {
                    try {
                        this.ventasData = ventasFuture.get();
                        this.productosData = productosFuture.get();
                        
                        logger.info("Datos cargados optimizadamente - Ventas: {}, Productos: {}", 
                            ventasData.size(), productosData.size());
                            
                    } catch (Exception e) {
                        logger.error("Error obteniendo datos optimizados", e);
                        // Fallback al método tradicional
                        cargarDatos();
                    }
                }))
                .exceptionally(throwable -> {
                    logger.error("Error en carga optimizada, usando método tradicional", throwable);
                    Platform.runLater(this::cargarDatos);
                    return null;
                });
        } else {
            // Fallback al método tradicional
            cargarDatos();
        }
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
     * Carga los datos reales desde Database Manager - ULTRA RÁPIDO H2
     */
    private void cargarDatos() {
        try {
            logger.info("Cargando datos reales para dashboard desde H2...");
            
            // Cargar productos reales desde H2 Database (1-5ms)
            productosData = DatabaseManager.leerProductos();
            logger.debug("Productos cargados desde H2: {}", productosData.size());
            
            // Cargar ventas reales desde H2 Database (1-5ms)
            ventasData = DatabaseManager.leerVentas();
            logger.debug("Ventas cargadas desde H2: {}", ventasData.size());
            
            logger.info("Datos reales cargados exitosamente desde H2: {} productos, {} ventas", 
                productosData.size(), ventasData.size());
                
        } catch (Exception e) {
            logger.error("Error cargando datos reales desde H2: {}", e.getMessage());
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
        
        // Animar botón de actualización
        if (btnActualizar != null) {
            animarBotonRefresh(btnActualizar);
        }
        
        try {
            // === FORZAR RECARGA COMPLETA SIEMPRE ===
            logger.info("Forzando recarga completa de datos desde Excel...");
            
            // Usar sistema optimizado si está disponible
            if (performanceSystemInitialized) {
                // INVALIDAR CACHE MANUALMENTE ANTES DE ACTUALIZAR
                queryOptimizer.invalidateCache();
                logger.debug("Cache invalidado manualmente antes de actualización");
                
                actualizarDatosOptimizado();
            } else {
                // Actualizar caché primero
                actualizarCache();
                
                // Recargar datos FORZADAMENTE desde Excel
                cargarDatos();
                
                // Actualizar métricas modernas
                actualizarMetricasModernas();
                
                // Actualizar gráficos
                actualizarGraficoVentasDiarias();
                actualizarGraficoProductosTop();
                
                // Actualizar timestamp
                actualizarTimestamp();
                
                // Verificar alertas
                verificarAlertas();
            }
            
            logger.info("Dashboard actualizado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error actualizando dashboard: {}", e.getMessage());
            mostrarAlerta("Error", "Error al actualizar dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Actualiza datos usando el sistema de performance optimizado CON TIMEOUTS CORTOS
     */
    private void actualizarDatosOptimizado() {
        logger.debug("Actualizando datos con sistema optimizado (timeouts cortos)...");
        
        // === FORZAR RECARGA COMPLETA PARA DATOS ACTUALIZADOS ===
        // Sincronizar datos en memoria para fallback CON RECARGA FORZADA
        CompletableFuture<List<Venta>> ventasFuture = queryOptimizer.getVentasOptimized();
        CompletableFuture<List<Producto>> productosFuture = queryOptimizer.getProductosOptimized();
        
        CompletableFuture.allOf(ventasFuture, productosFuture)
            .thenRun(() -> Platform.runLater(() -> {
                try {
                    List<Venta> nuevasVentas = ventasFuture.get();
                    List<Producto> nuevosProductos = productosFuture.get();
                    
                    // ACTUALIZAR DATOS EN MEMORIA INMEDIATAMENTE
                    this.ventasData = nuevasVentas;
                    this.productosData = nuevosProductos;
                    
                    logger.debug("Datos sincronizados para fallback - Ventas: {}, Productos: {}", 
                        ventasData.size(), productosData.size());
                        
                    // FORZAR ACTUALIZACIÓN INMEDIATA DE MÉTRICAS
                    actualizarMetricasModernas();
                    actualizarGraficoVentasDiarias();
                    actualizarGraficoProductosTop();
                    actualizarTimestamp();
                    verificarAlertas();
                    
                } catch (Exception e) {
                    logger.warn("Error sincronizando datos para fallback: {}", e.getMessage());
                }
            }));
        
        // Obtener métricas precalculadas del caché CON TIMEOUT EXTENDIDO (15 segundos)
        CompletableFuture<Map<String, Object>> metricsFuture = queryOptimizer.calcularMetricasDashboard()
            .orTimeout(15, java.util.concurrent.TimeUnit.SECONDS);
        
        metricsFuture.thenAccept(metrics -> Platform.runLater(() -> {
            try {
                // Actualizar métricas usando datos optimizados
                actualizarMetricasConDatos(metrics);
                
                // Actualizar gráficos de manera asíncrona
                CompletableFuture.runAsync(() -> {
                    Platform.runLater(() -> {
                        actualizarGraficoVentasDiarias();
                        actualizarGraficoProductosTop();
                    });
                });
                
                // Actualizar timestamp
                actualizarTimestamp();
                
                // Verificar alertas en segundo plano
                backgroundProcessor.submitTask("AlertCheck", () -> {
                    verificarAlertas();
                    return new BackgroundProcessor.TaskResult("AlertCheck", true, "Alertas verificadas");
                });
                
                logger.info("Dashboard actualizado con sistema optimizado");
                
            } catch (Exception e) {
                logger.error("Error en actualización optimizada", e);
                // Fallback al método tradicional
                actualizarMetricasModernas();
                actualizarGraficoVentasDiarias();
                actualizarGraficoProductosTop();
                actualizarTimestamp();
                verificarAlertas();
            }
        })).exceptionally(throwable -> {
            logger.warn("Timeout en métricas optimizadas (15s), recargando datos frescos: {}", throwable.getMessage());
            Platform.runLater(() -> {
                // Fallback con DATOS FRESCOS - forzar recarga completa
                try {
                    // Invalidar caché para forzar datos actualizados
                    queryOptimizer.invalidateCache();
                    
                    // Cargar datos actualizados
                    actualizarMetricasModernas();
                    actualizarGraficoVentasDiarias();
                    actualizarGraficoProductosTop();
                    actualizarTimestamp();
                    verificarAlertas();
                    
                    logger.info("Fallback ejecutado con datos actualizados");
                } catch (Exception e) {
                    logger.error("Error en fallback con datos frescos", e);
                    // Último recurso: usar datos en memoria pero invalidar caché para la próxima
                    queryOptimizer.invalidateCache();
                    actualizarMetricasModernas();
                    actualizarGraficoVentasDiarias();
                    actualizarGraficoProductosTop();
                    actualizarTimestamp();
                }
            });
            return null;
        });
    }
    
    /**
     * Actualiza métricas usando datos precalculados
     */
    private void actualizarMetricasConDatos(Map<String, Object> metrics) {
        // Tarjeta Ventas del Día
        if (lblVentasHoy != null) {
            Number ingresoHoy = (Number) metrics.get("ingresoHoy");
            lblVentasHoy.setText(String.format("$%.2f", ingresoHoy != null ? ingresoHoy.doubleValue() : 0.0));
        }
        
        if (lblTransaccionesHoy != null) {
            Number ventasHoy = (Number) metrics.get("ventasHoy");
            lblTransaccionesHoy.setText(String.valueOf(ventasHoy != null ? ventasHoy.intValue() : 0));
        }
        
        if (lblPromedioVenta != null) {
            Number ingresoHoy = (Number) metrics.get("ingresoHoy");
            Number ventasHoy = (Number) metrics.get("ventasHoy");
            
            double promedio = 0.0;
            if (ventasHoy != null && ventasHoy.intValue() > 0 && ingresoHoy != null) {
                promedio = ingresoHoy.doubleValue() / ventasHoy.doubleValue();
            }
            lblPromedioVenta.setText(String.format("$%.2f", promedio));
        }
        
        // Tarjeta Producto Estrella
        if (lblProductoTop != null) {
            String productoMasVendido = (String) metrics.get("productoMasVendido");
            lblProductoTop.setText(productoMasVendido != null ? productoMasVendido : "N/A");
        }
        
        // Tarjeta Balance
        if (lblIngresosHoy != null) {
            Number ingresoHoy = (Number) metrics.get("ingresoHoy");
            lblIngresosHoy.setText(String.format("$%.2f", ingresoHoy != null ? ingresoHoy.doubleValue() : 0.0));
        }
        
        logger.debug("Métricas actualizadas con datos optimizados");
    }
    
    /**
     * Actualiza las métricas del dashboard moderno de forma ASÍNCRONA
     */
    private void actualizarMetricasModernas() {
        LocalDate hoy = LocalDate.now();
        
        try {
            // CARGAR datos de forma ASÍNCRONA con timeout corto
            logger.debug("Cargando datos frescos para métricas modernas (async)...");
            
            queryOptimizer.getVentasOptimized()
                .orTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                .thenAccept(ventasFrescas -> {
                    Platform.runLater(() -> {
                        try {
                            actualizarMetricasConDatos(ventasFrescas, hoy);
                        } catch (Exception e) {
                            logger.error("Error actualizando métricas con datos", e);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    logger.warn("Timeout en carga de ventas (3s), usando datos de respaldo");
                    Platform.runLater(() -> {
                        try {
                            // Usar datos simples de respaldo
                            actualizarMetricasConDatosBasicos(hoy);
                        } catch (Exception e) {
                            logger.error("Error en datos de respaldo", e);
                        }
                    });
                    return null;
                });
                
        } catch (Exception e) {
            logger.error("Error iniciando carga de métricas", e);
            // Usar datos básicos inmediatamente
            actualizarMetricasConDatosBasicos(hoy);
        }
    }
    
    /**
     * Actualiza métricas con datos completos
     */
    private void actualizarMetricasConDatos(List<Venta> ventasFrescas, LocalDate hoy) {
        try {
            // === TARJETA VENTAS DEL DÍA ===
            List<Venta> ventasHoy = ventasFrescas.stream()
                .filter(v -> v.getFecha().equals(hoy))
                .toList();
            
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
            
            // Actualizar ventasData para mantener compatibilidad
            this.ventasData = ventasFrescas;
            
            // Actualizar otras métricas
            actualizarProductoEstrella();
            actualizarBalanceMensual();
            
            // Añadir análisis de tendencia
            analizarTendenciasVentas(totalHoy);
            
            logger.debug("Métricas modernas actualizadas: Hoy=${}, {} transacciones", totalHoy, ventasHoy.size());
            
        } catch (Exception e) {
            logger.error("Error actualizando métricas modernas con datos frescos", e);
            actualizarMetricasConDatosBasicos(hoy);
        }
    }
    
    /**
     * Actualiza métricas con datos básicos cuando falla la carga completa
     */
    private void actualizarMetricasConDatosBasicos(LocalDate hoy) {
        try {
            // Usar datos existentes si están disponibles
            if (ventasData != null && !ventasData.isEmpty()) {
                logger.warn("Usando datos existentes como fallback");
                
                List<Venta> ventasHoy = ventasData.stream()
                    .filter(v -> v.getFecha().equals(hoy))
                    .toList();
                
                double totalHoy = ventasHoy.stream()
                    .mapToDouble(Venta::getTotal)
                    .sum();
                
                if (lblVentasHoy != null) {
                    lblVentasHoy.setText(String.format("$%.2f", totalHoy));
                }
                if (lblTransaccionesHoy != null) {
                    lblTransaccionesHoy.setText(ventasHoy.size() + " transacciones");
                }
                if (lblPromedioVenta != null) {
                    double promedio = ventasHoy.isEmpty() ? 0 : totalHoy / ventasHoy.size();
                    lblPromedioVenta.setText(String.format("Promedio: $%.0f", promedio));
                }
                
                actualizarProductoEstrella();
                actualizarBalanceMensual();
                analizarTendenciasVentas(totalHoy);
            } else {
                // Sin datos disponibles - mostrar valores vacíos
                logger.warn("No hay datos disponibles, mostrando dashboard vacío");
                if (lblVentasHoy != null) lblVentasHoy.setText("$0.00");
                if (lblTransaccionesHoy != null) lblTransaccionesHoy.setText("0 transacciones");
                if (lblPromedioVenta != null) lblPromedioVenta.setText("Promedio: $0");
            }
        } catch (Exception e) {
            logger.error("Error en datos básicos de respaldo", e);
            // Valores por defecto seguros
            if (lblVentasHoy != null) lblVentasHoy.setText("Error");
            if (lblTransaccionesHoy != null) lblTransaccionesHoy.setText("Error");
            if (lblPromedioVenta != null) lblPromedioVenta.setText("Error");
        }
    }

    /**
     * Actualizar el producto estrella del dashboard
     */
    private void actualizarProductoEstrella() {
        try {
            if (productosData != null && !productosData.isEmpty() && 
                ventasData != null && !ventasData.isEmpty()) {
                
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
                        setProductoTopDefault();
                    }
                } else {
                    setProductoTopDefault();
                }
            } else {
                // Valores por defecto cuando no hay datos
                setProductoTopDefault();
            }
        } catch (Exception e) {
            logger.error("Error actualizando producto estrella", e);
            setProductoTopDefault();
        }
    }
    
    private void setProductoTopDefault() {
        if (lblProductoTop != null) lblProductoTop.setText("Sin datos");
        if (lblCantidadTop != null) lblCantidadTop.setText("0");
        if (lblIngresosTop != null) lblIngresosTop.setText("$0");
    }
    
    /**
     * Actualiza el balance mensual del dashboard
     */
    private void actualizarBalanceMensual() {
        try {
            LocalDate hoy = LocalDate.now();
            
            if (ventasData != null && !ventasData.isEmpty()) {
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
            } else {
                // Sin datos
                if (lblIngresosHoy != null) lblIngresosHoy.setText("$0");
                if (lblCostosHoy != null) lblCostosHoy.setText("$0");
            }
            
            logger.debug("Balance mensual actualizado exitosamente");
        } catch (Exception e) {
            logger.error("Error actualizando balance mensual", e);
            if (lblIngresosHoy != null) lblIngresosHoy.setText("Error");
            if (lblCostosHoy != null) lblCostosHoy.setText("Error");
        }
    }
    
    /**
     * Actualiza el gráfico de barras de ventas diarias
     */
    /**
     * Actualiza el gráfico de ventas diarias con animaciones suaves
     */
    private void actualizarGraficoVentasDiarias() {
        if (chartVentasDiarias == null) return;
        
        // === PROTECCIÓN CONTRA NULL ===
        if (ventasData == null) {
            logger.warn("ventasData es null en gráfico ventas diarias, usando datos vacíos");
            ventasData = new ArrayList<>();
        }
        
        // Crear nueva serie con animación
        XYChart.Series<String, Number> nuevaSerie = new XYChart.Series<>();
        nuevaSerie.setName("Ventas Diarias");
        
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
            
            nuevaSerie.getData().add(new XYChart.Data<>(diaStr, total));
        }
        
        // Actualización suave del gráfico
        Platform.runLater(() -> {
            try {
                chartVentasDiarias.getData().clear();
                chartVentasDiarias.getData().add(nuevaSerie);
                
                // Animación de aparición para cada punto
                for (XYChart.Data<String, Number> data : nuevaSerie.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setOpacity(0);
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), data.getNode());
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.play();
                    }
                }
                
                logger.debug("Gráfico de ventas diarias actualizado con {} datos", nuevaSerie.getData().size());
            } catch (Exception e) {
                logger.error("Error actualizando gráfico de ventas diarias", e);
            }
        });
    }
    
    /**
     * Actualiza el gráfico circular de productos más vendidos con animaciones
     */
    private void actualizarGraficoProductosTop() {
        if (chartProductosTop == null) return;
        
        // === PROTECCIÓN CONTRA NULL ===
        if (productosData == null) {
            logger.warn("productosData es null en gráfico productos, usando datos vacíos");
            productosData = new ArrayList<>();
        }
        if (ventasData == null) {
            logger.warn("ventasData es null en gráfico productos, usando datos vacíos");
            ventasData = new ArrayList<>();
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        if (!productosData.isEmpty() && !ventasData.isEmpty()) {
            // Calcular ventas reales por producto con mejor algoritmo
            Map<String, Integer> ventasPorProducto = new HashMap<>();
            
            for (Venta venta : ventasData) {
                if (venta.getItems() != null) {
                    for (Producto prod : venta.getItems()) {
                        if (prod.getNombre() != null && !prod.getNombre().trim().isEmpty()) {
                            String nombre = prod.getNombre().trim();
                            ventasPorProducto.put(nombre, 
                                ventasPorProducto.getOrDefault(nombre, 0) + 1);
                        }
                    }
                }
            }
            
            // Tomar los top 5 productos más vendidos
            ventasPorProducto.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Solo productos con ventas
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    String nombreCorto = entry.getKey().length() > 15 ? 
                        entry.getKey().substring(0, 12) + "..." : entry.getKey();
                    pieChartData.add(new PieChart.Data(nombreCorto + " (" + entry.getValue() + ")", entry.getValue()));
                });
        }
        
        if (pieChartData.isEmpty()) {
            // Datos por defecto si no hay ventas
            pieChartData.add(new PieChart.Data("Sin datos disponibles", 1));
        }
        
        // Actualización suave del gráfico
        Platform.runLater(() -> {
            try {
                // Guardar datos anteriores para comparación
                ObservableList<PieChart.Data> datosAnteriores = FXCollections.observableArrayList(chartProductosTop.getData());
                
                chartProductosTop.setData(pieChartData);
                
                // Animación solo si hay datos reales
                if (pieChartData.size() > 1 || !pieChartData.get(0).getName().contains("Sin datos")) {
                    chartProductosTop.setOpacity(0.8);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(800), chartProductosTop);
                    fadeIn.setFromValue(0.8);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
                
                logger.debug("Gráfico de productos top actualizado con {} productos", pieChartData.size());
            } catch (Exception e) {
                logger.error("Error actualizando gráfico de productos top", e);
            }
        });
    }
    
    // === PUNTO 1: FUNCIONALIDADES DE BOTONES ===
    
    /**
     * Exporta reporte completo a PDF/Excel
     */
    @FXML
    public void exportarReporte() {
        logger.info("Iniciando exportación de reporte...");
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt")
            );
            fileChooser.setInitialFileName("reporte_dashboard_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv");
            
            Stage stage = (Stage) lblVentasHoy.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                generarReporteCSV(file);
                mostrarAlerta("Éxito", "Reporte exportado exitosamente a: " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            logger.error("Error exportando reporte: {}", e.getMessage());
            mostrarAlerta("Error", "Error al exportar reporte: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Configura opciones del dashboard
     */
    @FXML
    public void configurarDashboard() {
        logger.info("Abriendo configuración del dashboard...");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración del Dashboard");
        alert.setHeaderText("Opciones de Configuración");
        alert.setContentText(
            "Próximamente disponible:\n\n" +
            "• Personalizar intervalos de actualización\n" +
            "• Configurar alertas automáticas\n" +
            "• Seleccionar métricas visibles\n" +
            "• Temas de color personalizados\n" +
            "• Exportación automática programada"
        );
        alert.showAndWait();
    }
    
    /**
     * Limpia completamente todos los datos de la base de datos
     */
    @FXML
    public void limpiarBaseDatos() {
        logger.info("Solicitando limpieza completa de la base de datos...");
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("⚠️ LIMPIAR BASE DE DATOS");
        confirmacion.setHeaderText("¿Estás seguro de esta acción?");
        confirmacion.setContentText(
            "Esta acción eliminará PERMANENTEMENTE:\n\n" +
            "✗ Todos los productos registrados\n" +
            "✗ Todas las ventas históricas\n" +
            "✗ Todos los movimientos de caja\n" +
            "✗ Todo el historial del sistema\n\n" +
            "⚠️ ESTA ACCIÓN NO SE PUEDE DESHACER ⚠️\n\n" +
            "¿Deseas continuar?"
        );
        
        // Personalizar botones
        confirmacion.getButtonTypes().setAll(
            new ButtonType("❌ Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE),
            new ButtonType("🗑️ SÍ, LIMPIAR TODO", ButtonBar.ButtonData.OK_DONE)
        );
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // Ejecutar limpieza en hilo de fondo
                Task<Boolean> limpiezaTask = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        updateMessage("Limpiando base de datos...");
                        return DatabaseManager.limpiarBaseDatos();
                    }
                    
                    @Override
                    protected void succeeded() {
                        Boolean exitoso = getValue();
                        if (exitoso) {
                            logger.info("Base de datos limpiada exitosamente");
                            
                            // Mostrar confirmación de éxito
                            Alert exito = new Alert(Alert.AlertType.INFORMATION);
                            exito.setTitle("✅ Limpieza Completada");
                            exito.setHeaderText("Base de datos limpiada exitosamente");
                            exito.setContentText(
                                "La base de datos ha sido completamente limpiada:\n\n" +
                                "✅ Todos los productos eliminados\n" +
                                "✅ Todas las ventas eliminadas\n" +
                                "✅ Historial de caja eliminado\n\n" +
                                "El sistema está listo para comenzar de nuevo."
                            );
                            exito.showAndWait();
                            
                            // Actualizar dashboard después de la limpieza
                            Platform.runLater(() -> {
                                invalidarCacheCompleto();
                                actualizarDatos();
                            });
                            
                        } else {
                            mostrarAlerta("Error en Limpieza", 
                                "No se pudo completar la limpieza de la base de datos.\n" +
                                "Consulte los logs para más detalles.", Alert.AlertType.ERROR);
                        }
                    }
                    
                    @Override
                    protected void failed() {
                        logger.error("Error durante la limpieza de la base de datos", getException());
                        mostrarAlerta("Error Crítico", 
                            "Error crítico durante la limpieza:\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    }
                };
                
                // Mostrar progreso
                Alert progreso = new Alert(Alert.AlertType.INFORMATION);
                progreso.setTitle("Limpiando...");
                progreso.setHeaderText("Limpieza en progreso");
                progreso.setContentText("Por favor espere mientras se limpia la base de datos...");
                progreso.getButtonTypes().clear(); // Sin botones mientras procesa
                
                // Ejecutar task en background
                Thread limpiezaThread = new Thread(limpiezaTask);
                limpiezaThread.setDaemon(true);
                limpiezaThread.start();
                
                // Cerrar diálogo de progreso cuando termine
                limpiezaTask.setOnSucceeded(e -> progreso.close());
                limpiezaTask.setOnFailed(e -> progreso.close());
                
                progreso.show();
            }
        });
    }
    
    /**
     * Invalida completamente el cache del sistema
     */
    private void invalidarCacheCompleto() {
        try {
            if (queryOptimizer != null) {
                queryOptimizer.invalidateCache();
                logger.debug("Cache del DatabaseQueryOptimizer invalidado");
            }
            // Note: DashboardCacheManager no tiene método limpiarTodo, 
            // pero se puede invalidar específicamente cada caché si fuera necesario
        } catch (Exception e) {
            logger.warn("Error invalidando cache: {}", e.getMessage());
        }
    }
    
    /**
     * Ver reporte completo de balance
     */
    @FXML
    public void verReporteCompleto() {
        logger.info("Mostrando reporte completo de balance...");
        generarReporteDetallado();
    }
    
    /**
     * Ver detalle de ventas del día
     */
    @FXML
    public void verDetalleVentas() {
        logger.info("Mostrando detalle de ventas del día...");
        generarDetalleVentasHoyAsync();
    }
    
    /**
     * Gestionar productos
     */
    @FXML
    public void gestionarProductos() {
        logger.info("Abriendo gestión de productos...");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gestión de Productos");
        alert.setHeaderText("Sistema de Gestión");
        alert.setContentText(
            "Funcionalidades disponibles:\n\n" +
            "• Ver inventario completo\n" +
            "• Productos de baja rotación\n" +
            "• Análisis de rentabilidad\n" +
            "• Recomendaciones de restock\n" +
            "• Histórico de productos estrella"
        );
        alert.showAndWait();
    }
    
    // === PUNTO 2: OPTIMIZACIONES DE PERFORMANCE ===
    
    /**
     * Actualiza caché usando el sistema optimizado
     */
    private void actualizarCache() {
        if (performanceSystemInitialized) {
            // El sistema optimizado maneja el caché automáticamente
            logger.debug("Cache manejado automáticamente por sistemas de performance");
        } else {
            // Fallback básico (solo para casos de emergencia)
            logger.debug("Usando sistema de cache básico (fallback)");
            cargarDatos();
        }
    }
    
    /**
     * Obtiene ventas rápidamente por fecha (OPTIMIZADO PARA VELOCIDAD)
     */
    private List<Venta> obtenerVentasCache(LocalDate fecha) {
        // === PROTECCIÓN CONTRA NULL ===
        if (ventasData == null) {
            logger.warn("ventasData es null en obtenerVentasCache, retornando lista vacía");
            return new ArrayList<>();
        }
        
        // SIEMPRE usar datos en memoria para consultas rápidas
        // El sistema optimizado es para cargas masivas, no consultas simples
        return ventasData.stream()
                .filter(venta -> venta.getFecha().equals(fecha))
                .toList();
    }
    
    /**
     * Inicia actualización automática cada 10 segundos (para detección rápida de cambios)
     */
    private void iniciarActualizacionAutomatica() {
        if (autoUpdateTimeline != null) {
            autoUpdateTimeline.stop();
        }
        
        autoUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), e -> {
                logger.debug("Actualización automática iniciada");
                // Verificar si necesitamos actualizar antes de hacer la carga completa
                actualizarDatosInteligenteAsync();
            })
        );
        autoUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        autoUpdateTimeline.play();
        
        logger.info("Sistema de actualización automática iniciado (cada 10 segundos)");
    }
    
    /**
     * Actualización inteligente que verifica cambios antes de actualizar UI
     */
    private void actualizarDatosInteligenteAsync() {
        long currentTime = System.currentTimeMillis();
        
        // Verificar tiempo mínimo entre actualizaciones
        if (currentTime - lastUpdateTime < MIN_UPDATE_INTERVAL) {
            logger.debug("Saltando actualización - muy pronto desde la última ({} ms)", 
                currentTime - lastUpdateTime);
            return;
        }
        
        // Verificar cambios en datos de manera asíncrona
        CompletableFuture.supplyAsync(() -> {
            // Calcular hash rápido de datos actuales
            long ventasCount = DatabaseManager.leerVentas().size();
            long productosCount = DatabaseManager.leerProductos().size();
            return ventasCount * 31 + productosCount * 17; // Hash simple pero efectivo
        }).thenAccept(newHash -> {
            if (newHash != lastDataHash) {
                logger.debug("Detectados cambios en datos (hash: {} -> {}), actualizando dashboard", 
                    lastDataHash, newHash);
                lastDataHash = newHash;
                lastUpdateTime = currentTime;
                Platform.runLater(this::actualizarDatos);
            } else {
                logger.debug("No hay cambios en datos, saltando actualización UI");
            }
        }).exceptionally(throwable -> {
            logger.error("Error en verificación inteligente de datos", throwable);
            // Fallback a actualización normal en caso de error
            Platform.runLater(this::actualizarDatos);
            return null;
        });
    }
    
    /**
     * Actualización inmediata del dashboard cuando se detectan nuevas ventas
     * Este método debe ser llamado desde el sistema de ventas
     */
    public void actualizarDashboardInmediatamente() {
        logger.info("Actualizando dashboard inmediatamente debido a nueva venta...");
        Platform.runLater(() -> {
            try {
                // Invalidar caché primero para forzar recarga desde Excel
                if (queryOptimizer != null) {
                    queryOptimizer.invalidateCache();
                }
                
                // Actualizar datos inmediatamente
                actualizarDatos();
                
                logger.info("Dashboard actualizado inmediatamente tras nueva venta");
            } catch (Exception e) {
                logger.error("Error en actualización inmediata del dashboard", e);
            }
        });
    }
    
    /**
     * Método estático para acceso desde otras clases
     */
    public static void notificarNuevaVenta() {
        // Este método puede ser llamado desde VentasController o DatabaseManager
        logger.info("Nueva venta detectada - solicitando actualización inmediata del dashboard");
        
        // Buscar instancia activa del dashboard y actualizar
        Platform.runLater(() -> {
            // La actualización se hará en el próximo ciclo de auto-refresh (ahora cada 10 segundos)
            logger.debug("Notificación de nueva venta procesada");
        });
    }
    
    // === PUNTO 3: MEJORAS DE UX/UI ===
    
    /**
     * Animación de carga para botón refresh
     */
    private void animarBotonRefresh(Button boton) {
        if (boton == null) return;
        
        // Animación de rotación
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), boton);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(2);
        
        // Cambiar texto temporalmente
        String textoOriginal = boton.getText();
        boton.setText("🔄 Actualizando...");
        boton.setDisable(true);
        
        rotateTransition.setOnFinished(e -> {
            boton.setText(textoOriginal);
            boton.setDisable(false);
        });
        
        rotateTransition.play();
    }
    
    /**
     * Animación de fade-in para métricas
     */
    private void animarMetricas() {
        List<Label> labels = Arrays.asList(
            lblVentasHoy, lblTransaccionesHoy, lblPromedioVenta,
            lblProductoTop, lblCantidadTop, lblIngresosTop,
            lblIngresosHoy, lblCostosHoy
        );
        
        Duration delay = Duration.ZERO;
        for (Label label : labels) {
            if (label != null) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), label);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setDelay(delay);
                fadeIn.play();
                
                delay = delay.add(Duration.millis(100));
            }
        }
    }
    
    /**
     * Analiza tendencias de ventas y actualiza información de balance
     */
    private void analizarTendenciasVentas(double ventasHoy) {
        try {
            LocalDate ayer = LocalDate.now().minusDays(1);
            List<Venta> ventasAyer = obtenerVentasCache(ayer);
            double totalAyer = ventasAyer.stream().mapToDouble(Venta::getTotal).sum();
            
            // Calcular variación porcentual
            double variacion = 0;
            String tendencia = "estable";
            
            if (totalAyer > 0) {
                variacion = ((ventasHoy - totalAyer) / totalAyer) * 100;
                if (variacion > 10) {
                    tendencia = "positiva ↗️";
                } else if (variacion < -10) {
                    tendencia = "negativa ↘️";
                } else {
                    tendencia = "estable ➡️";
                }
            }
            
            // Actualizar labels de balance con información de tendencia
            if (lblIngresosHoy != null) {
                lblIngresosHoy.setText(String.format("$%.2f", ventasHoy));
            }
            
            if (lblCostosHoy != null) {
                // Estimar costos como 60% de ingresos (estimación)
                double costosEstimados = ventasHoy * 0.6;
                lblCostosHoy.setText(String.format("$%.2f", costosEstimados));
            }
            
            logger.debug("Tendencia de ventas: {} ({}%)", tendencia, String.format("%.1f", variacion));
            
        } catch (Exception e) {
            logger.error("Error analizando tendencias: {}", e.getMessage());
        }
    }
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Genera reporte CSV detallado
     */
    private void generarReporteCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("REPORTE DASHBOARD - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            
            // Métricas generales
            writer.write("=== MÉTRICAS GENERALES ===\n");
            writer.write("Ventas Hoy," + (lblVentasHoy != null ? lblVentasHoy.getText() : "N/A") + "\n");
            writer.write("Transacciones," + (lblTransaccionesHoy != null ? lblTransaccionesHoy.getText() : "N/A") + "\n");
            writer.write("Promedio Venta," + (lblPromedioVenta != null ? lblPromedioVenta.getText() : "N/A") + "\n");
            writer.write("Producto Top," + (lblProductoTop != null ? lblProductoTop.getText() : "N/A") + "\n");
            
            // Detalle de ventas por día
            writer.write("\n=== VENTAS POR DÍA (Últimos 7 días) ===\n");
            writer.write("Fecha,Total Ventas,Número Transacciones\n");
            
            LocalDate hoy = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate fecha = hoy.minusDays(i);
                List<Venta> ventasDia = obtenerVentasCache(fecha);
                double totalDia = ventasDia.stream().mapToDouble(Venta::getTotal).sum();
                
                writer.write(String.format("%s,%.2f,%d\n", 
                    fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                    totalDia, 
                    ventasDia.size()));
            }
            
            writer.write("\n=== PRODUCTOS TOP ===\n");
            writer.write("Producto,Cantidad Vendida,Ingresos Generados\n");
            
            // Top productos de las ventas
            Map<String, Integer> ventasPorProducto = new HashMap<>();
            Map<String, Double> ingresosPorProducto = new HashMap<>();
            
            for (Venta venta : ventasData) {
                if (venta.getItems() != null) {
                    for (Producto producto : venta.getItems()) {
                        String nombre = producto.getNombre();
                        ventasPorProducto.put(nombre, ventasPorProducto.getOrDefault(nombre, 0) + 1);
                        ingresosPorProducto.put(nombre, 
                            ingresosPorProducto.getOrDefault(nombre, 0.0) + producto.getPrecio());
                    }
                }
            }
            
            ventasPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    try {
                        writer.write(String.format("%s,%d,%.2f\n", 
                            entry.getKey(), 
                            entry.getValue(),
                            ingresosPorProducto.getOrDefault(entry.getKey(), 0.0)));
                    } catch (IOException e) {
                        logger.error("Error escribiendo producto en CSV: {}", e.getMessage());
                    }
                });
        }
        
        logger.info("Reporte CSV generado exitosamente: {}", file.getAbsolutePath());
    }
    
    /**
     * Genera reporte detallado de balance de forma INMEDIATA (sin timeouts)
     */
    private void generarReporteDetallado() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        
        // OPTIMIZACIÓN: Usar datos en memoria directamente para velocidad máxima
        logger.info("Generando reporte usando datos en memoria para máxima velocidad...");
        
        List<Venta> todasVentas = ventasData != null ? 
            new ArrayList<>(ventasData) : 
            new ArrayList<>();
        
        mostrarReporteDetallado(hoy, inicioMes, todasVentas);
        logger.info("Reporte completo mostrado INSTANTÁNEAMENTE: {} ventas totales", todasVentas.size());
    }
    
    /**
     * Muestra el reporte detallado de balance
     */
    private void mostrarReporteDetallado(LocalDate hoy, LocalDate inicioMes, List<Venta> todasVentas) {
        List<Venta> ventasMes = todasVentas.stream()
            .filter(v -> !v.getFecha().isBefore(inicioMes))
            .toList();
        
        double totalIngresosMes = ventasMes.stream().mapToDouble(Venta::getTotal).sum();
        double promedioVentasDiarias = hoy.getDayOfMonth() > 0 ? totalIngresosMes / hoy.getDayOfMonth() : 0.0;
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reporte Completo de Balance");
        alert.setHeaderText("Balance Mensual Detallado");
        alert.setContentText(String.format(
            "📊 ANÁLISIS DEL MES ACTUAL\n\n" +
            "💰 Total Ingresos: $%.2f\n" +
            "📈 Promedio Diario: $%.2f\n" +
            "🛒 Total Transacciones: %d\n" +
            "📅 Días Transcurridos: %d\n" +
            "🎯 Proyección Mensual: $%.2f\n\n" +
            "📈 TENDENCIA: %s",
            totalIngresosMes,
            promedioVentasDiarias,
            ventasMes.size(),
            hoy.getDayOfMonth(),
            promedioVentasDiarias * 30,
            totalIngresosMes > 0 ? "Positiva ✅" : "Requiere Atención ⚠️"
        ));
        alert.showAndWait();
        
        logger.info("Reporte detallado mostrado exitosamente: {} transacciones del mes", ventasMes.size());
    }
    
    /**
     * Genera detalle de ventas del día de forma INMEDIATA (sin timeouts)
     */
    private void generarDetalleVentasHoyAsync() {
        LocalDate hoy = LocalDate.now();
        
        // OPTIMIZACIÓN: Usar datos en memoria directamente para velocidad máxima
        logger.info("Generando detalle de ventas usando datos en memoria para máxima velocidad...");
        
        List<Venta> ventasHoy = ventasData != null ? 
            ventasData.stream()
                .filter(venta -> venta.getFecha().equals(hoy))
                .toList() : 
            new ArrayList<>();
        
        mostrarDetalleVentas(hoy, ventasHoy);
        logger.info("Detalle de ventas mostrado INSTANTÁNEAMENTE: {} transacciones", ventasHoy.size());
    }
    
    /**
     * Muestra el detalle de ventas en un diálogo
     */
    private void mostrarDetalleVentas(LocalDate fecha, List<Venta> ventas) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("📅 VENTAS DEL DÍA - ").append(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        
        if (ventas.isEmpty()) {
            detalle.append("❌ No hay ventas registradas para hoy");
        } else {
            double totalDia = ventas.stream().mapToDouble(Venta::getTotal).sum();
            double promedio = totalDia / ventas.size();
            
            detalle.append(String.format("💰 Total del Día: $%.2f\n", totalDia));
            detalle.append(String.format("🛒 Transacciones: %d\n", ventas.size()));
            detalle.append(String.format("📊 Ticket Promedio: $%.2f\n\n", promedio));
            
            detalle.append("🕐 CRONOLOGÍA DE VENTAS:\n");
            ventas.stream()
                .sorted(Comparator.comparing(Venta::getFechaHora))
                .forEach(venta -> {
                    detalle.append(String.format("• %s - $%.2f (%d productos)\n",
                        venta.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")),
                        venta.getTotal(),
                        venta.getItems() != null ? venta.getItems().size() : 0));
                });
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle de Ventas");
        alert.setHeaderText("Ventas del Día Actual");
        alert.setContentText(detalle.toString());
        alert.showAndWait();
        
        logger.info("Detalle de ventas mostrado exitosamente: {} transacciones", ventas.size());
    }
    
    /**
     * Muestra alertas con estilo consistente
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Actualiza el timestamp de última actualización
     */
    private void actualizarTimestamp() {
        if (lblUltimaActualizacion != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lblUltimaActualizacion.setText("Última actualización: " + timestamp);
        }
    }
    
    /**
     * Verifica alertas automáticas del negocio
     */
    private void verificarAlertas() {
        try {
            LocalDate hoy = LocalDate.now();
            List<Venta> ventasHoy = obtenerVentasCache(hoy);
            double totalHoy = ventasHoy.stream().mapToDouble(Venta::getTotal).sum();
            
            // Alerta: Sin ventas en el día
            if (ventasHoy.isEmpty()) {
                logger.warn("⚠️ Alerta: No hay ventas registradas para hoy");
                return;
            }
            
            // Alerta: Ventas muy bajas
            double promedioSemanal = calcularPromedioVentasSemanal();
            if (totalHoy < promedioSemanal * 0.3) {
                logger.warn("⚠️ Alerta: Ventas del día están 70% por debajo del promedio semanal");
            }
            
            // Alerta: Día muy exitoso
            if (totalHoy > promedioSemanal * 2.0) {
                logger.info("🎉 ¡Excelente! Ventas del día superan el doble del promedio semanal");
            }
            
            // Alerta: Productos sin rotar
            verificarProductosSinRotacion();
            
        } catch (Exception e) {
            logger.error("Error verificando alertas: {}", e.getMessage());
        }
    }
    
    /**
     * Calcula promedio de ventas semanal
     */
    private double calcularPromedioVentasSemanal() {
        LocalDate hoy = LocalDate.now();
        double totalSemana = 0;
        int diasConVentas = 0;
        
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.minusDays(i);
            List<Venta> ventasDia = obtenerVentasCache(fecha);
            if (!ventasDia.isEmpty()) {
                totalSemana += ventasDia.stream().mapToDouble(Venta::getTotal).sum();
                diasConVentas++;
            }
        }
        
        return diasConVentas > 0 ? totalSemana / diasConVentas : 0;
    }
    
    /**
     * Verifica productos que no han tenido rotación
     */
    private void verificarProductosSinRotacion() {
        // === PROTECCIÓN CONTRA NULL ===
        if (productosData == null || ventasData == null) {
            logger.debug("Datos no disponibles para verificar rotación de productos");
            return;
        }
        
        Set<String> productosVendidos = ventasData.stream()
            .filter(v -> v.getItems() != null)
            .flatMap(v -> v.getItems().stream())
            .map(Producto::getNombre)
            .collect(Collectors.toSet());
        
        List<String> productosSinRotacion = productosData.stream()
            .map(Producto::getNombre)
            .filter(nombre -> !productosVendidos.contains(nombre))
            .toList();
        
        if (!productosSinRotacion.isEmpty()) {
            logger.warn("⚠️ Productos sin rotación: {}", String.join(", ", productosSinRotacion));
        }
    }
    
    /**
     * Limpia los recursos del sistema de performance
     * Debe ser llamado al cerrar la aplicación
     */
    public void cleanup() {
        try {
            logger.info("Limpiando recursos del Dashboard Controller...");
            
            // Detener timeline de actualización automática
            if (autoUpdateTimeline != null) {
                autoUpdateTimeline.stop();
            }
            
            // Cerrar sistemas de performance
            if (performanceSystemInitialized) {
                if (backgroundProcessor != null) {
                    backgroundProcessor.stop();
                }
                
                if (queryOptimizer != null) {
                    queryOptimizer.shutdown();
                }
                
                if (cacheManager != null) {
                    cacheManager.shutdown();
                }
            }
            
            // Imprimir estadísticas finales
            if (performanceSystemInitialized && backgroundProcessor != null) {
                backgroundProcessor.printStats();
                
                if (cacheManager != null) {
                    cacheManager.printStats();
                }
            }
            
            logger.info("Recursos del Dashboard Controller limpiados exitosamente");
            
        } catch (Exception e) {
            logger.error("Error durante limpieza de recursos", e);
        }
    }
    
}
