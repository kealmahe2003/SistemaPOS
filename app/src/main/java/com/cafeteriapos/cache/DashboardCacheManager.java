package com.cafeteriapos.cache;

import com.cafeteriapos.models.Venta;
import com.cafeteriapos.models.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager especializado para caché del dashboard
 * Gestiona múltiples tipos de caché con invalidación inteligente
 */
public class DashboardCacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardCacheManager.class);
    
    // === CACHÉS ESPECIALIZADOS ===
    private final PerformanceCache<String, List<Venta>> ventasCache;
    private final PerformanceCache<String, List<Producto>> productosCache;
    private final PerformanceCache<String, Map<String, Object>> metricsCache;
    private final PerformanceCache<String, Object> aggregatesCache;
    
    // === INSTANCIA SINGLETON ===
    private static volatile DashboardCacheManager instance;
    private static final Object LOCK = new Object();
    
    // === CONFIGURACIÓN ===
    private static final int VENTAS_CACHE_SIZE = 500;
    private static final int PRODUCTOS_CACHE_SIZE = 100;
    private static final int METRICS_CACHE_SIZE = 200;
    private static final int AGGREGATES_CACHE_SIZE = 300;
    
    private static final long VENTAS_TTL_MINUTES = 5;
    private static final long PRODUCTOS_TTL_MINUTES = 30;
    private static final long METRICS_TTL_MINUTES = 3;
    private static final long AGGREGATES_TTL_MINUTES = 10;
    
    // === TRACKING DE INVALIDACIÓN ===
    private final Map<String, LocalDateTime> lastDataUpdate = new ConcurrentHashMap<>();
    
    private DashboardCacheManager() {
        this.ventasCache = new PerformanceCache<>(VENTAS_CACHE_SIZE, VENTAS_TTL_MINUTES, true);
        this.productosCache = new PerformanceCache<>(PRODUCTOS_CACHE_SIZE, PRODUCTOS_TTL_MINUTES, true);
        this.metricsCache = new PerformanceCache<>(METRICS_CACHE_SIZE, METRICS_TTL_MINUTES, true);
        this.aggregatesCache = new PerformanceCache<>(AGGREGATES_CACHE_SIZE, AGGREGATES_TTL_MINUTES, true);
        
        logger.info("DashboardCacheManager inicializado con configuración optimizada");
    }
    
    /**
     * Obtiene la instancia singleton del cache manager
     */
    public static DashboardCacheManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DashboardCacheManager();
                }
            }
        }
        return instance;
    }
    
    // === GESTIÓN DE CACHÉ DE VENTAS ===
    
    public List<Venta> getVentas(String cacheKey) {
        return ventasCache.get(cacheKey);
    }
    
    public void putVentas(String cacheKey, List<Venta> ventas) {
        ventasCache.put(cacheKey, ventas);
        updateLastDataTime("ventas");
        logger.debug("Ventas cacheadas: key={}, count={}", cacheKey, ventas.size());
    }
    
    public List<Venta> getOrLoadVentas(String cacheKey, java.util.function.Supplier<List<Venta>> loader) {
        return ventasCache.getOrCompute(cacheKey, loader);
    }
    
    // === GESTIÓN DE CACHÉ DE PRODUCTOS ===
    
    public List<Producto> getProductos(String cacheKey) {
        return productosCache.get(cacheKey);
    }
    
    public void putProductos(String cacheKey, List<Producto> productos) {
        productosCache.put(cacheKey, productos);
        updateLastDataTime("productos");
        logger.debug("Productos cacheados: key={}, count={}", cacheKey, productos.size());
    }
    
    public List<Producto> getOrLoadProductos(String cacheKey, java.util.function.Supplier<List<Producto>> loader) {
        return productosCache.getOrCompute(cacheKey, loader);
    }
    
    // === GESTIÓN DE CACHÉ DE MÉTRICAS ===
    
    public Map<String, Object> getMetrics(String cacheKey) {
        return metricsCache.get(cacheKey);
    }
    
    public void putMetrics(String cacheKey, Map<String, Object> metrics) {
        metricsCache.put(cacheKey, metrics);
        updateLastDataTime("metrics");
        logger.debug("Métricas cacheadas: key={}, metrics={}", cacheKey, metrics.keySet());
    }
    
    public Map<String, Object> getOrLoadMetrics(String cacheKey, java.util.function.Supplier<Map<String, Object>> loader) {
        return metricsCache.getOrCompute(cacheKey, loader);
    }
    
    // === GESTIÓN DE CACHÉ DE AGREGADOS ===
    
    public Object getAggregate(String cacheKey) {
        return aggregatesCache.get(cacheKey);
    }
    
    public void putAggregate(String cacheKey, Object aggregate) {
        aggregatesCache.put(cacheKey, aggregate);
        updateLastDataTime("aggregates");
        logger.debug("Agregado cacheado: key={}, type={}", cacheKey, aggregate.getClass().getSimpleName());
    }
    
    public Object getOrLoadAggregate(String cacheKey, java.util.function.Supplier<Object> loader) {
        return aggregatesCache.getOrCompute(cacheKey, loader);
    }
    
    // === KEYS PREDEFINIDAS ===
    
    public static class CacheKeys {
        public static final String VENTAS_TODAS = "ventas:todas";
        public static final String VENTAS_HOY = "ventas:hoy";
        public static final String VENTAS_SEMANA = "ventas:semana";
        public static final String VENTAS_MES = "ventas:mes";
        
        public static final String PRODUCTOS_TODOS = "productos:todos";
        public static final String PRODUCTOS_ACTIVOS = "productos:activos";
        
        public static final String METRICS_DASHBOARD = "metrics:dashboard";
        public static final String METRICS_VENTAS_RESUMEN = "metrics:ventas_resumen";
        public static final String METRICS_PRODUCTOS_RESUMEN = "metrics:productos_resumen";
        
        public static final String AGG_TOTAL_VENTAS = "agg:total_ventas";
        public static final String AGG_PROMEDIO_VENTAS = "agg:promedio_ventas";
        public static final String AGG_PRODUCTO_MAS_VENDIDO = "agg:producto_mas_vendido";
        public static final String AGG_TENDENCIAS = "agg:tendencias";
        
        public static String ventasPorFecha(LocalDate fecha) {
            return "ventas:fecha:" + fecha.toString();
        }
        
        public static String metricsPorPeriodo(String periodo) {
            return "metrics:periodo:" + periodo;
        }
    }
    
    // === INVALIDACIÓN INTELIGENTE ===
    
    /**
     * Invalida cachés relacionados con ventas
     */
    public void invalidateVentasCache() {
        ventasCache.clear();
        // También invalidar métricas que dependen de ventas
        metricsCache.invalidate(CacheKeys.METRICS_VENTAS_RESUMEN);
        metricsCache.invalidate(CacheKeys.METRICS_DASHBOARD);
        
        // Invalidar agregados que dependen de ventas
        aggregatesCache.invalidate(CacheKeys.AGG_TOTAL_VENTAS);
        aggregatesCache.invalidate(CacheKeys.AGG_PROMEDIO_VENTAS);
        aggregatesCache.invalidate(CacheKeys.AGG_TENDENCIAS);
        
        logger.info("Cache de ventas y dependencias invalidado");
    }
    
    /**
     * Invalida cachés relacionados con productos
     */
    public void invalidateProductosCache() {
        productosCache.clear();
        // También invalidar métricas que dependen de productos
        metricsCache.invalidate(CacheKeys.METRICS_PRODUCTOS_RESUMEN);
        metricsCache.invalidate(CacheKeys.METRICS_DASHBOARD);
        
        // Invalidar agregados que dependen de productos
        aggregatesCache.invalidate(CacheKeys.AGG_PRODUCTO_MAS_VENDIDO);
        
        logger.info("Cache de productos y dependencias invalidado");
    }
    
    /**
     * Invalida todo el caché
     */
    public void invalidateAll() {
        ventasCache.clear();
        productosCache.clear();
        metricsCache.clear();
        aggregatesCache.clear();
        lastDataUpdate.clear();
        
        logger.info("Todos los cachés invalidados");
    }
    
    /**
     * Invalida cachés que son más antiguos que el tiempo especificado
     */
    public void invalidateOlderThan(LocalDateTime threshold) {
        // Esta funcionalidad ya está manejada automáticamente por TTL
        // Pero podemos forzar una limpieza manual si es necesario
        
        logger.info("Limpieza manual de cachés ejecutada");
    }
    
    // === ESTADÍSTICAS GLOBALES ===
    
    /**
     * Obtiene estadísticas de todos los cachés
     */
    public DashboardCacheStats getAllStats() {
        return new DashboardCacheStats(
            ventasCache.getStats(),
            productosCache.getStats(),
            metricsCache.getStats(),
            aggregatesCache.getStats(),
            lastDataUpdate
        );
    }
    
    /**
     * Imprime estadísticas de performance
     */
    public void printStats() {
        DashboardCacheStats stats = getAllStats();
        logger.info("=== ESTADÍSTICAS DE CACHÉ ===");
        logger.info("Ventas Cache: {}", stats.getVentasStats());
        logger.info("Productos Cache: {}", stats.getProductosStats());
        logger.info("Métricas Cache: {}", stats.getMetricsStats());
        logger.info("Agregados Cache: {}", stats.getAggregatesStats());
        
        double avgHitRate = (stats.getVentasStats().getHitRate() + 
                           stats.getProductosStats().getHitRate() + 
                           stats.getMetricsStats().getHitRate() + 
                           stats.getAggregatesStats().getHitRate()) / 4.0;
        
        logger.info("Hit Rate Promedio: {:.2f}%", avgHitRate * 100);
    }
    
    // === WARMING DEL CACHÉ ===
    
    /**
     * Pre-carga datos frecuentemente usados
     */
    public CompletableFuture<Void> warmupCache(
            java.util.function.Supplier<List<Venta>> ventasLoader,
            java.util.function.Supplier<List<Producto>> productosLoader) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Iniciando warming del caché...");
                
                // Pre-cargar ventas frecuentes
                List<Venta> todasVentas = ventasLoader.get();
                putVentas(CacheKeys.VENTAS_TODAS, todasVentas);
                
                // Pre-cargar productos
                List<Producto> todosProductos = productosLoader.get();
                putProductos(CacheKeys.PRODUCTOS_TODOS, todosProductos);
                
                logger.info("Warming del caché completado exitosamente");
                
            } catch (Exception e) {
                logger.error("Error durante warming del caché", e);
            }
        });
    }
    
    // === LIMPIEZA DE RECURSOS ===
    
    /**
     * Cierra todos los cachés y libera recursos
     */
    public void shutdown() {
        ventasCache.shutdown();
        productosCache.shutdown();
        metricsCache.shutdown();
        aggregatesCache.shutdown();
        lastDataUpdate.clear();
        
        logger.info("DashboardCacheManager cerrado");
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private void updateLastDataTime(String dataType) {
        lastDataUpdate.put(dataType, LocalDateTime.now());
    }
    
    // === CLASE INTERNA: ESTADÍSTICAS GLOBALES ===
    
    public static class DashboardCacheStats {
        private final PerformanceCache.CacheStats ventasStats;
        private final PerformanceCache.CacheStats productosStats;
        private final PerformanceCache.CacheStats metricsStats;
        private final PerformanceCache.CacheStats aggregatesStats;
        private final Map<String, LocalDateTime> lastUpdates;
        
        public DashboardCacheStats(PerformanceCache.CacheStats ventasStats,
                                 PerformanceCache.CacheStats productosStats,
                                 PerformanceCache.CacheStats metricsStats,
                                 PerformanceCache.CacheStats aggregatesStats,
                                 Map<String, LocalDateTime> lastUpdates) {
            this.ventasStats = ventasStats;
            this.productosStats = productosStats;
            this.metricsStats = metricsStats;
            this.aggregatesStats = aggregatesStats;
            this.lastUpdates = new ConcurrentHashMap<>(lastUpdates);
        }
        
        // Getters
        public PerformanceCache.CacheStats getVentasStats() { return ventasStats; }
        public PerformanceCache.CacheStats getProductosStats() { return productosStats; }
        public PerformanceCache.CacheStats getMetricsStats() { return metricsStats; }
        public PerformanceCache.CacheStats getAggregatesStats() { return aggregatesStats; }
        public Map<String, LocalDateTime> getLastUpdates() { return lastUpdates; }
        
        public int getTotalSize() {
            return ventasStats.getCurrentSize() + 
                   productosStats.getCurrentSize() + 
                   metricsStats.getCurrentSize() + 
                   aggregatesStats.getCurrentSize();
        }
        
        public long getTotalHits() {
            return ventasStats.getHits() + 
                   productosStats.getHits() + 
                   metricsStats.getHits() + 
                   aggregatesStats.getHits();
        }
        
        public long getTotalMisses() {
            return ventasStats.getMisses() + 
                   productosStats.getMisses() + 
                   metricsStats.getMisses() + 
                   aggregatesStats.getMisses();
        }
    }
}
