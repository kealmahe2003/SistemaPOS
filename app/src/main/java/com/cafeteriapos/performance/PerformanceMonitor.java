package com.cafeteriapos.performance;

import com.cafeteriapos.cache.DashboardCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitor de performance para el sistema POS
 * Recolecta y analiza métricas de rendimiento en tiempo real
 */
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    // === CONTADORES DE PERFORMANCE ===
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong successfulQueries = new AtomicLong(0);
    private final AtomicLong failedQueries = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    // === MÉTRICAS DE CACHÉ ===
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // === MÉTRICAS DE MEMORIA ===
    private volatile long lastMemoryCheck = 0;
    private volatile double maxMemoryUsage = 0.0;
    private volatile double averageMemoryUsage = 0.0;
    private final AtomicLong memoryChecks = new AtomicLong(0);
    
    // === CONFIGURACIÓN ===
    private final LocalDateTime startTime;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // === INSTANCIA SINGLETON ===
    private static volatile PerformanceMonitor instance;
    private static final Object LOCK = new Object();
    
    private PerformanceMonitor() {
        this.startTime = LocalDateTime.now();
        logger.info("PerformanceMonitor iniciado en: {}", startTime.format(FORMATTER));
    }
    
    public static PerformanceMonitor getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new PerformanceMonitor();
                }
            }
        }
        return instance;
    }
    
    // === REGISTRO DE MÉTRICAS ===
    
    /**
     * Registra una consulta ejecutada
     */
    public void recordQuery(long responseTimeMs, boolean success) {
        totalQueries.incrementAndGet();
        totalResponseTime.addAndGet(responseTimeMs);
        
        if (success) {
            successfulQueries.incrementAndGet();
        } else {
            failedQueries.incrementAndGet();
        }
        
        // Log si la consulta es muy lenta
        if (responseTimeMs > 1000) {
            logger.warn("Consulta lenta detectada: {}ms", responseTimeMs);
        }
    }
    
    /**
     * Registra un hit de caché
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    /**
     * Registra un miss de caché
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    /**
     * Actualiza estadísticas de memoria
     */
    public void updateMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
        
        // Actualizar máximo
        if (memoryUsagePercent > maxMemoryUsage) {
            maxMemoryUsage = memoryUsagePercent;
        }
        
        // Actualizar promedio
        long checks = memoryChecks.incrementAndGet();
        averageMemoryUsage = ((averageMemoryUsage * (checks - 1)) + memoryUsagePercent) / checks;
        
        lastMemoryCheck = System.currentTimeMillis();
        
        // Alerta si la memoria está muy alta
        if (memoryUsagePercent > 85.0) {
            logger.warn("Uso de memoria alto: {:.1f}%", memoryUsagePercent);
        }
    }
    
    // === ANÁLISIS DE PERFORMANCE ===
    
    /**
     * Obtiene métricas completas del sistema
     */
    public PerformanceMetrics getMetrics() {
        updateMemoryStats(); // Actualizar memoria antes de generar reporte
        
        long totalQueries = this.totalQueries.get();
        double averageResponseTime = totalQueries > 0 ? 
            (double) totalResponseTime.get() / totalQueries : 0.0;
        
        double successRate = totalQueries > 0 ? 
            (double) successfulQueries.get() / totalQueries * 100 : 0.0;
        
        long totalCacheRequests = cacheHits.get() + cacheMisses.get();
        double cacheHitRate = totalCacheRequests > 0 ? 
            (double) cacheHits.get() / totalCacheRequests * 100 : 0.0;
        
        // Obtener estadísticas de caché del manager
        DashboardCacheManager.DashboardCacheStats cacheStats = null;
        try {
            cacheStats = DashboardCacheManager.getInstance().getAllStats();
        } catch (Exception e) {
            logger.debug("Error obteniendo estadísticas de caché: {}", e.getMessage());
        }
        
        return new PerformanceMetrics(
            startTime,
            LocalDateTime.now(),
            totalQueries,
            successfulQueries.get(),
            failedQueries.get(),
            averageResponseTime,
            successRate,
            cacheHitRate,
            maxMemoryUsage,
            averageMemoryUsage,
            cacheStats
        );
    }
    
    /**
     * Genera reporte de performance para logging
     */
    public void logPerformanceReport() {
        PerformanceMetrics metrics = getMetrics();
        
        logger.info("=== REPORTE DE PERFORMANCE ===");
        logger.info("Tiempo de funcionamiento: {} minutos", metrics.getUptimeMinutes());
        logger.info("Consultas totales: {}", metrics.getTotalQueries());
        logger.info("Tasa de éxito: {:.1f}%", metrics.getSuccessRate());
        logger.info("Tiempo promedio de respuesta: {:.2f}ms", metrics.getAverageResponseTime());
        logger.info("Hit rate de caché: {:.1f}%", metrics.getCacheHitRate());
        logger.info("Uso máximo de memoria: {:.1f}%", metrics.getMaxMemoryUsage());
        logger.info("Uso promedio de memoria: {:.1f}%", metrics.getAverageMemoryUsage());
        
        if (metrics.getCacheStats() != null) {
            logger.info("Tamaño total de caché: {}", metrics.getCacheStats().getTotalSize());
        }
    }
    
    /**
     * Obtiene métricas como Map para dashboard
     */
    public Map<String, Object> getMetricsAsMap() {
        PerformanceMetrics metrics = getMetrics();
        Map<String, Object> map = new HashMap<>();
        
        map.put("uptime_minutes", metrics.getUptimeMinutes());
        map.put("total_queries", metrics.getTotalQueries());
        map.put("success_rate", metrics.getSuccessRate());
        map.put("avg_response_time", metrics.getAverageResponseTime());
        map.put("cache_hit_rate", metrics.getCacheHitRate());
        map.put("max_memory_usage", metrics.getMaxMemoryUsage());
        map.put("avg_memory_usage", metrics.getAverageMemoryUsage());
        map.put("start_time", metrics.getStartTime().format(FORMATTER));
        map.put("current_time", metrics.getCurrentTime().format(FORMATTER));
        
        return map;
    }
    
    /**
     * Detecta problemas de performance
     */
    public PerformanceIssues analyzeIssues() {
        PerformanceMetrics metrics = getMetrics();
        PerformanceIssues issues = new PerformanceIssues();
        
        // Verificar tiempo de respuesta alto
        if (metrics.getAverageResponseTime() > 500) {
            issues.addIssue("HIGH_RESPONSE_TIME", 
                String.format("Tiempo de respuesta promedio alto: %.2fms", metrics.getAverageResponseTime()));
        }
        
        // Verificar baja tasa de éxito
        if (metrics.getSuccessRate() < 95.0 && metrics.getTotalQueries() > 10) {
            issues.addIssue("LOW_SUCCESS_RATE", 
                String.format("Baja tasa de éxito: %.1f%%", metrics.getSuccessRate()));
        }
        
        // Verificar bajo hit rate de caché
        if (metrics.getCacheHitRate() < 60.0 && (cacheHits.get() + cacheMisses.get()) > 50) {
            issues.addIssue("LOW_CACHE_HIT_RATE", 
                String.format("Bajo hit rate de caché: %.1f%%", metrics.getCacheHitRate()));
        }
        
        // Verificar uso alto de memoria
        if (metrics.getMaxMemoryUsage() > 90.0) {
            issues.addIssue("HIGH_MEMORY_USAGE", 
                String.format("Uso alto de memoria: %.1f%%", metrics.getMaxMemoryUsage()));
        }
        
        return issues;
    }
    
    /**
     * Resetea todas las estadísticas
     */
    public void reset() {
        totalQueries.set(0);
        successfulQueries.set(0);
        failedQueries.set(0);
        totalResponseTime.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        memoryChecks.set(0);
        maxMemoryUsage = 0.0;
        averageMemoryUsage = 0.0;
        
        logger.info("Estadísticas de performance reseteadas");
    }
    
    // === CLASES INTERNAS ===
    
    public static class PerformanceMetrics {
        private final LocalDateTime startTime;
        private final LocalDateTime currentTime;
        private final long totalQueries;
        private final long successfulQueries;
        private final long failedQueries;
        private final double averageResponseTime;
        private final double successRate;
        private final double cacheHitRate;
        private final double maxMemoryUsage;
        private final double averageMemoryUsage;
        private final DashboardCacheManager.DashboardCacheStats cacheStats;
        
        public PerformanceMetrics(LocalDateTime startTime, LocalDateTime currentTime,
                                long totalQueries, long successfulQueries, long failedQueries,
                                double averageResponseTime, double successRate, double cacheHitRate,
                                double maxMemoryUsage, double averageMemoryUsage,
                                DashboardCacheManager.DashboardCacheStats cacheStats) {
            this.startTime = startTime;
            this.currentTime = currentTime;
            this.totalQueries = totalQueries;
            this.successfulQueries = successfulQueries;
            this.failedQueries = failedQueries;
            this.averageResponseTime = averageResponseTime;
            this.successRate = successRate;
            this.cacheHitRate = cacheHitRate;
            this.maxMemoryUsage = maxMemoryUsage;
            this.averageMemoryUsage = averageMemoryUsage;
            this.cacheStats = cacheStats;
        }
        
        public long getUptimeMinutes() {
            return java.time.Duration.between(startTime, currentTime).toMinutes();
        }
        
        // Getters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getCurrentTime() { return currentTime; }
        public long getTotalQueries() { return totalQueries; }
        public long getSuccessfulQueries() { return successfulQueries; }
        public long getFailedQueries() { return failedQueries; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public double getSuccessRate() { return successRate; }
        public double getCacheHitRate() { return cacheHitRate; }
        public double getMaxMemoryUsage() { return maxMemoryUsage; }
        public double getAverageMemoryUsage() { return averageMemoryUsage; }
        public DashboardCacheManager.DashboardCacheStats getCacheStats() { return cacheStats; }
    }
    
    public static class PerformanceIssues {
        private final Map<String, String> issues = new HashMap<>();
        
        public void addIssue(String type, String description) {
            issues.put(type, description);
        }
        
        public boolean hasIssues() {
            return !issues.isEmpty();
        }
        
        public Map<String, String> getIssues() {
            return new HashMap<>(issues);
        }
        
        public void logIssues() {
            if (hasIssues()) {
                logger.warn("=== PROBLEMAS DE PERFORMANCE DETECTADOS ===");
                issues.forEach((type, description) -> 
                    logger.warn("{}: {}", type, description));
            }
        }
    }
}
