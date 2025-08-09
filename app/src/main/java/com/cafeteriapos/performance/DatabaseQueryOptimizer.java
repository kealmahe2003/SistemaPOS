package com.cafeteriapos.performance;

import com.cafeteriapos.cache.DashboardCacheManager;
import com.cafeteriapos.controllers.DashboardController;
import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Optimizador avanzado para consultas H2 Database con cache inteligente
 * Minimiza las consultas SQL y optimiza el acceso a datos
 */
public class DatabaseQueryOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryOptimizer.class);
    private static volatile DatabaseQueryOptimizer instance;
    
    // Cache para datos frecuentemente accedidos
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    // Configuración del cache (30 segundos para datos frecuentes)
    private static final long CACHE_TTL_MS = 30_000;
    private static final long STATS_CACHE_TTL_MS = 60_000; // 1 minuto para estadísticas
    
    // Pool de hilos para operaciones asíncronas
    private final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "DatabaseQueryOptimizer-" + System.currentTimeMillis());
        t.setDaemon(true);
        return t;
    });
    
    // Cache manager para integración
    private DashboardCacheManager cacheManager;
    
    // Métricas de performance
    private final Map<String, Long> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> totalExecutionTime = new ConcurrentHashMap<>();
    
    private DatabaseQueryOptimizer() {
        logger.info("DatabaseQueryOptimizer inicializado");
    }
    
    public static DatabaseQueryOptimizer getInstance() {
        if (instance == null) {
            synchronized (DatabaseQueryOptimizer.class) {
                if (instance == null) {
                    instance = new DatabaseQueryOptimizer();
                }
            }
        }
        return instance;
    }
    
    /**
     * Integra con el cache manager del dashboard
     */
    public void integrarConCacheManager(DashboardCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        logger.debug("Integración con DashboardCacheManager establecida");
    }
    
    /**
     * Obtiene productos de forma optimizada con cache
     */
    public CompletableFuture<List<Producto>> getProductosOptimized() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String cacheKey = "productos_all";
            
            try {
                // Verificar cache primero
                List<Producto> cached = getCachedData(cacheKey);
                if (cached != null) {
                    recordMetric("productos_cache_hit", startTime);
                    return cached;
                }
                
                // Cache miss - obtener de H2 Database
                List<Producto> productos = DatabaseManager.leerProductos();
                
                // Guardar en cache
                putCachedData(cacheKey, productos);
                
                recordMetric("productos_database_query", startTime);
                logger.debug("Productos cargados desde H2 Database: {}", productos.size());
                
                return productos;
                
            } catch (Exception e) {
                logger.error("Error en getProductosOptimized: {}", e.getMessage());
                recordMetric("productos_error", startTime);
                return new ArrayList<>();
            }
        }, executor);
    }
    
    /**
     * Obtiene ventas de forma optimizada con cache
     */
    public CompletableFuture<List<Venta>> getVentasOptimized() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String cacheKey = "ventas_all";
            
            try {
                // Verificar cache primero
                List<Venta> cached = getCachedData(cacheKey);
                if (cached != null) {
                    recordMetric("ventas_cache_hit", startTime);
                    return cached;
                }
                
                // Cache miss - obtener de H2 Database
                List<Venta> ventas = DatabaseManager.leerVentas();
                
                // Guardar en cache
                putCachedData(cacheKey, ventas);
                
                recordMetric("ventas_database_query", startTime);
                logger.debug("Ventas cargadas desde H2 Database: {}", ventas.size());
                
                return ventas;
                
            } catch (Exception e) {
                logger.error("Error en getVentasOptimized: {}", e.getMessage());
                recordMetric("ventas_error", startTime);
                return new ArrayList<>();
            }
        }, executor);
    }
    
    /**
     * Obtiene un hash rápido de los datos para detectar cambios sin cargar todo
     */
    public CompletableFuture<Long> getDataHashOptimized() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String cacheKey = "data_hash";
            
            try {
                // Cache muy corto para hash (5 segundos)
                Long cached = getCachedData(cacheKey, 5000);
                if (cached != null) {
                    recordMetric("data_hash_cache_hit", startTime);
                    return cached;
                }
                
                // Calcular hash basado en conteos rápidos
                long ventasCount = DatabaseManager.leerVentas().size();
                long productosCount = DatabaseManager.leerProductos().size();
                long hash = ventasCount * 31 + productosCount * 17 + System.currentTimeMillis() / 60000; // Cambiar cada minuto
                
                putCachedData(cacheKey, hash, 5000);
                recordMetric("data_hash_calculated", startTime);
                
                return hash;
                
            } catch (Exception e) {
                logger.error("Error calculando hash de datos: {}", e.getMessage());
                recordMetric("data_hash_error", startTime);
                return 0L;
            }
        }, executor);
    }
    
    /**
     * Calcula métricas del dashboard de forma optimizada
     */
    public CompletableFuture<Map<String, Object>> calcularMetricasDashboard() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String cacheKey = "dashboard_metrics";
            
            try {
                // Verificar cache de estadísticas (TTL más largo)
                Map<String, Object> cached = getCachedData(cacheKey, STATS_CACHE_TTL_MS);
                if (cached != null) {
                    recordMetric("dashboard_metrics_cache_hit", startTime);
                    return cached;
                }
                
                Map<String, Object> metrics = new HashMap<>();
                
                // Usar métodos optimizados de DatabaseManager
                double totalVentasHoy = DatabaseManager.obtenerTotalVentasHoy();
                long cantidadVentasHoy = DatabaseManager.obtenerConteoVentasHoy();
                
                // Calcular total ventas del mes filtrando ventas
                List<Venta> todasVentas = DatabaseManager.leerVentas();
                LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
                double totalVentasMes = todasVentas.stream()
                    .filter(v -> !v.getFechaHora().toLocalDate().isBefore(inicioMes))
                    .mapToDouble(Venta::getTotal)
                    .sum();
                
                metrics.put("totalVentasHoy", totalVentasHoy);
                metrics.put("cantidadVentasHoy", cantidadVentasHoy);
                metrics.put("totalVentasMes", totalVentasMes);
                metrics.put("promedioVentaDiaria", cantidadVentasHoy > 0 ? totalVentasHoy / cantidadVentasHoy : 0.0);
                
                // Obtener datos adicionales de productos
                List<Producto> productos = DatabaseManager.leerProductos();
                metrics.put("totalProductos", productos.size());
                metrics.put("stockTotal", productos.stream().mapToInt(Producto::getStock).sum());
                
                // Productos con stock bajo (menos de 10)
                long productosStockBajo = productos.stream()
                    .filter(p -> p.getStock() < 10)
                    .count();
                metrics.put("productosStockBajo", productosStockBajo);
                
                // Guardar en cache con TTL más largo
                putCachedData(cacheKey, metrics, STATS_CACHE_TTL_MS);
                
                recordMetric("dashboard_metrics_calculated", startTime);
                logger.debug("Métricas dashboard calculadas: {} elementos", metrics.size());
                
                return metrics;
                
            } catch (Exception e) {
                logger.error("Error calculando métricas dashboard: {}", e.getMessage());
                recordMetric("dashboard_metrics_error", startTime);
                return new HashMap<>();
            }
        }, executor);
    }
    
    /**
     * Obtiene ventas filtradas por fecha de forma optimizada
     */
    public CompletableFuture<List<Venta>> getVentasPorFecha(LocalDate fecha) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String cacheKey = "ventas_fecha_" + fecha.toString();
            
            try {
                // Verificar cache
                List<Venta> cached = getCachedData(cacheKey);
                if (cached != null) {
                    recordMetric("ventas_por_fecha_cache_hit", startTime);
                    return cached;
                }
                
                // Obtener todas las ventas y filtrar (para este caso simple)
                // En una implementación más avanzada, se haría la consulta SQL directamente
                List<Venta> todasVentas = DatabaseManager.leerVentas();
                List<Venta> ventasFiltradas = todasVentas.stream()
                    .filter(v -> v.getFechaHora().toLocalDate().equals(fecha))
                    .collect(Collectors.toList());
                
                // Guardar en cache
                putCachedData(cacheKey, ventasFiltradas);
                
                recordMetric("ventas_por_fecha_calculated", startTime);
                logger.debug("Ventas filtradas por fecha {}: {}", fecha, ventasFiltradas.size());
                
                return ventasFiltradas;
                
            } catch (Exception e) {
                logger.error("Error obteniendo ventas por fecha: {}", e.getMessage());
                recordMetric("ventas_por_fecha_error", startTime);
                return new ArrayList<>();
            }
        }, executor);
    }
    
    /**
     * Obtiene ventas del período actual (hoy, semana, mes) de forma optimizada
     */
    public CompletableFuture<Map<String, List<Venta>>> getVentasPeriodoOptimized() {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                Map<String, List<Venta>> resultado = new HashMap<>();
                LocalDate hoy = LocalDate.now();
                
                // Obtener todas las ventas una sola vez
                List<Venta> todasVentas = getVentasOptimized().join();
                
                // Filtrar por períodos en paralelo
                CompletableFuture<List<Venta>> ventasHoyFuture = CompletableFuture.supplyAsync(() -> 
                    filtrarVentasPorPeriodo(todasVentas, hoy, hoy));
                
                CompletableFuture<List<Venta>> ventasSemanaFuture = CompletableFuture.supplyAsync(() -> 
                    filtrarVentasPorPeriodo(todasVentas, hoy.minusDays(7), hoy));
                
                CompletableFuture<List<Venta>> ventasMesFuture = CompletableFuture.supplyAsync(() -> 
                    filtrarVentasPorPeriodo(todasVentas, hoy.minusDays(30), hoy));
                
                // Esperar resultados
                resultado.put("hoy", ventasHoyFuture.join());
                resultado.put("semana", ventasSemanaFuture.join());
                resultado.put("mes", ventasMesFuture.join());
                
                recordMetric("ventas_periodo_calculated", startTime);
                logger.info("Ventas por período cargadas - Hoy: {}, Semana: {}, Mes: {}", 
                    resultado.get("hoy").size(), 
                    resultado.get("semana").size(), 
                    resultado.get("mes").size());
                
                return resultado;
                
            } catch (Exception e) {
                logger.error("Error al obtener ventas por período optimizado", e);
                recordMetric("ventas_periodo_error", startTime);
                return new HashMap<>();
            }
        }, executor);
    }
    
    /**
     * Invalida el cache cuando se realizan cambios
     */
    public void invalidateCache() {
        cache.clear();
        cacheTimestamps.clear();
        logger.debug("Cache invalidado completamente");
        
        // Invalidar también cache del manager si existe
        if (cacheManager != null) {
            cacheManager.invalidateAll();
        }
    }
    
    /**
     * Invalida solo el cache de un tipo específico
     */
    public void invalidateCache(String type) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(type));
        cacheTimestamps.entrySet().removeIf(entry -> entry.getKey().startsWith(type));
        logger.debug("Cache invalidado para tipo: {}", type);
    }
    
    /**
     * Notifica cuando se guarda una nueva venta (para invalidar cache)
     */
    public void onVentaSaved() {
        CompletableFuture.runAsync(() -> {
            try {
                // Invalidar caches relacionados con ventas
                invalidateCache("ventas");
                invalidateCache("dashboard_metrics");
                
                // Notificar al dashboard para actualización en tiempo real
                try {
                    DashboardController.notificarNuevaVenta();
                    logger.debug("Notificación enviada al dashboard para actualización inmediata");
                } catch (Exception e) {
                    logger.warn("Error notificando al dashboard: {}", e.getMessage());
                }
                
                // Pre-cargar datos actualizados en segundo plano
                getVentasOptimized().join();
                getProductosOptimized().join();
                logger.info("Datos re-cargados exitosamente después de nueva venta");
            } catch (Exception e) {
                logger.warn("Error pre-cargando datos después de nueva venta: {}", e.getMessage());
            }
        }, executor);
    }
    
    /**
     * Precarga cache con datos frecuentemente usados
     */
    public void warmupCache() {
        logger.info("Iniciando precarga de caché H2 Database...");
        
        CompletableFuture.allOf(
            getVentasOptimized(),
            getProductosOptimized(),
            calcularMetricasDashboard()
        ).thenRun(() -> logger.info("Precarga de caché H2 completada exitosamente"))
        .exceptionally(throwable -> {
            logger.error("Error durante precarga de caché H2", throwable);
            return null;
        });
    }
    
    /**
     * Cierra el optimizador y libera recursos
     */
    public void shutdown() {
        executor.shutdown();
        cache.clear();
        cacheTimestamps.clear();
        logger.info("DatabaseQueryOptimizer cerrado exitosamente");
    }
    
    // === MÉTODOS PRIVADOS DE CACHE ===
    
    @SuppressWarnings("unchecked")
    private <T> T getCachedData(String key) {
        return getCachedData(key, CACHE_TTL_MS);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getCachedData(String key, long ttl) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < ttl) {
            return (T) cache.get(key);
        }
        
        // Cache expirado, remover
        cache.remove(key);
        cacheTimestamps.remove(key);
        return null;
    }
    
    private void putCachedData(String key, Object data) {
        putCachedData(key, data, CACHE_TTL_MS);
    }
    
    private void putCachedData(String key, Object data, long ttl) {
        cache.put(key, data);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }
    
    // === MÉTODOS DE UTILIDADES ===
    
    private List<Venta> filtrarVentasPorPeriodo(List<Venta> ventas, LocalDate inicio, LocalDate fin) {
        return ventas.stream()
            .filter(venta -> {
                LocalDate fechaVenta = venta.getFechaHora().toLocalDate();
                return !fechaVenta.isBefore(inicio) && !fechaVenta.isAfter(fin);
            })
            .collect(Collectors.toList());
    }
    
    // === MÉTODOS DE MÉTRICAS ===
    
    private void recordMetric(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        operationCounts.merge(operation, 1L, Long::sum);
        totalExecutionTime.merge(operation, duration, Long::sum);
    }
    
    /**
     * Obtiene estadísticas de performance
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("operationCounts", new HashMap<>(operationCounts));
        stats.put("totalExecutionTime", new HashMap<>(totalExecutionTime));
        stats.put("cacheSize", cache.size());
        
        // Calcular promedios
        Map<String, Double> averageTimes = new HashMap<>();
        for (String operation : operationCounts.keySet()) {
            long totalTime = totalExecutionTime.getOrDefault(operation, 0L);
            long count = operationCounts.get(operation);
            averageTimes.put(operation, count > 0 ? (double) totalTime / count : 0.0);
        }
        stats.put("averageExecutionTime", averageTimes);
        
        // Integrar estadísticas del cache manager si existe
        if (cacheManager != null) {
            try {
                DashboardCacheManager.DashboardCacheStats cacheStats = cacheManager.getAllStats();
                stats.put("dashboardCacheHitRate", String.format("%.2f%%", 
                    ((double) cacheStats.getTotalHits() / 
                     (cacheStats.getTotalHits() + cacheStats.getTotalMisses())) * 100));
                stats.put("dashboardCacheSize", cacheStats.getTotalSize());
            } catch (Exception e) {
                logger.debug("Error obteniendo stats del dashboard cache: {}", e.getMessage());
            }
        }
        
        return stats;
    }
    
    /**
     * Imprime estadísticas de performance
     */
    public void printStats() {
        logger.info("=== DatabaseQueryOptimizer Performance Stats ===");
        logger.info("Cache size: {}", cache.size());
        
        operationCounts.forEach((operation, count) -> {
            long totalTime = totalExecutionTime.getOrDefault(operation, 0L);
            double avgTime = count > 0 ? (double) totalTime / count : 0.0;
            logger.info("{}: {} calls, avg time: {:.2f}ms", operation, count, avgTime);
        });
        
        if (cacheManager != null) {
            try {
                DashboardCacheManager.DashboardCacheStats cacheStats = cacheManager.getAllStats();
                double hitRate = ((double) cacheStats.getTotalHits() / 
                    (cacheStats.getTotalHits() + cacheStats.getTotalMisses())) * 100;
                logger.info("Dashboard Cache Hit Rate: {:.2f}%", hitRate);
            } catch (Exception e) {
                logger.debug("Error mostrando stats del dashboard cache: {}", e.getMessage());
            }
        }
    }
}
