package com.cafeteriapos.performance;

import com.cafeteriapos.cache.DashboardCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Procesador en segundo plano para tareas de alto rendimiento
 * Maneja actualizaciones de caché, limpieza automática y análisis en tiempo real
 */
public class BackgroundProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(BackgroundProcessor.class);
    
    // === CONFIGURACIÓN ===
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 6;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;
    
    // === INTERVALOS DE TAREAS (en minutos) ===
    private static final long CACHE_REFRESH_INTERVAL = 5;
    private static final long ANALYTICS_INTERVAL = 10;
    private static final long CLEANUP_INTERVAL = 15;
    private static final long HEALTH_CHECK_INTERVAL = 2;
    
    // === EJECUTORES ===
    private final ThreadPoolExecutor mainExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final CompletionService<TaskResult> completionService;
    
    // === DEPENDENCIAS ===
    private final DashboardCacheManager cacheManager;
    private final DatabaseQueryOptimizer queryOptimizer;
    
    // === ESTADO ===
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicLong totalTasksExecuted = new AtomicLong(0);
    private final Map<String, TaskStatistics> taskStats = new ConcurrentHashMap<>();
    
    // === INSTANCIA SINGLETON ===
    private static volatile BackgroundProcessor instance;
    private static final Object LOCK = new Object();
    
    private BackgroundProcessor() {
        // Thread pool personalizado con manejo de cola
        this.mainExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new CustomThreadFactory("BackgroundProcessor"),
            new ThreadPoolExecutor.CallerRunsPolicy() // Fallback policy
        );
        
        this.scheduledExecutor = Executors.newScheduledThreadPool(3, 
            new CustomThreadFactory("BGProcessor-Scheduled"));
        
        this.completionService = new ExecutorCompletionService<>(mainExecutor);
        
        this.cacheManager = DashboardCacheManager.getInstance();
        this.queryOptimizer = DatabaseQueryOptimizer.getInstance();
        
        logger.info("BackgroundProcessor inicializado con configuración optimizada");
    }
    
    public static BackgroundProcessor getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new BackgroundProcessor();
                }
            }
        }
        return instance;
    }
    
    // === CONTROL DEL PROCESADOR ===
    
    /**
     * Inicia el procesador en segundo plano
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info("Iniciando BackgroundProcessor...");
            
            schedulePeriodicTasks();
            startTaskMonitoring();
            
            logger.info("BackgroundProcessor iniciado exitosamente");
        }
    }
    
    /**
     * Detiene el procesador
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Deteniendo BackgroundProcessor...");
            
            scheduledExecutor.shutdown();
            mainExecutor.shutdown();
            
            try {
                if (!mainExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    mainExecutor.shutdownNow();
                }
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
                
                logger.info("BackgroundProcessor detenido exitosamente");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupción durante el cierre del BackgroundProcessor");
            }
        }
    }
    
    /**
     * Verifica si está ejecutándose
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    // === TAREAS PROGRAMADAS ===
    
    private void schedulePeriodicTasks() {
        // Tarea 1: Actualización automática de caché
        scheduledExecutor.scheduleAtFixedRate(
            this::refreshCacheTask,
            CACHE_REFRESH_INTERVAL,
            CACHE_REFRESH_INTERVAL,
            TimeUnit.MINUTES
        );
        
        // Tarea 2: Análisis de tendencias
        scheduledExecutor.scheduleAtFixedRate(
            this::analyticsTask,
            ANALYTICS_INTERVAL,
            ANALYTICS_INTERVAL,
            TimeUnit.MINUTES
        );
        
        // Tarea 3: Limpieza automática
        scheduledExecutor.scheduleAtFixedRate(
            this::cleanupTask,
            CLEANUP_INTERVAL,
            CLEANUP_INTERVAL,
            TimeUnit.MINUTES
        );
        
        // Tarea 4: Health check
        scheduledExecutor.scheduleAtFixedRate(
            this::healthCheckTask,
            HEALTH_CHECK_INTERVAL,
            HEALTH_CHECK_INTERVAL,
            TimeUnit.MINUTES
        );
        
        logger.info("Tareas periódicas programadas exitosamente");
    }
    
    // === IMPLEMENTACIÓN DE TAREAS ===
    
    private void refreshCacheTask() {
        submitTask("CacheRefresh", () -> {
            try {
                logger.debug("Iniciando actualización automática de caché...");
                
                // Precargar datos frecuentemente usados
                queryOptimizer.warmupCache();
                
                // Estadísticas de caché
                var stats = cacheManager.getAllStats();
                logger.debug("Caché actualizado - Hit Rate: {:.2f}%", 
                    ((double) stats.getTotalHits() / 
                     (stats.getTotalHits() + stats.getTotalMisses())) * 100);
                
                return new TaskResult("CacheRefresh", true, "Caché actualizado exitosamente");
                
            } catch (Exception e) {
                logger.error("Error en actualización de caché", e);
                return new TaskResult("CacheRefresh", false, "Error: " + e.getMessage());
            }
        });
    }
    
    private void analyticsTask() {
        submitTask("Analytics", () -> {
            try {
                logger.debug("Ejecutando análisis de tendencias...");
                
                // Calcular métricas avanzadas de manera asíncrona
                CompletableFuture<Map<String, Object>> metricsFuture = 
                    queryOptimizer.calcularMetricasDashboard();
                
                Map<String, Object> metrics = metricsFuture.get(30, TimeUnit.SECONDS);
                
                // Analizar tendencias
                analyzeTrends(metrics);
                
                // Detectar anomalías
                detectAnomalies(metrics);
                
                return new TaskResult("Analytics", true, 
                    "Análisis completado - " + metrics.size() + " métricas procesadas");
                
            } catch (Exception e) {
                logger.error("Error en análisis de tendencias", e);
                return new TaskResult("Analytics", false, "Error: " + e.getMessage());
            }
        });
    }
    
    private void cleanupTask() {
        submitTask("Cleanup", () -> {
            try {
                logger.debug("Ejecutando limpieza automática...");
                
                // Limpiar caché expirado (ya manejado automáticamente, pero forzamos)
                Runtime runtime = Runtime.getRuntime();
                long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
                
                // Ejecutar garbage collection si es necesario
                if (memoryBefore > runtime.totalMemory() * 0.8) {
                    System.gc();
                    logger.debug("Garbage collection ejecutado debido a alta utilización de memoria");
                }
                
                long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                long memoryFreed = memoryBefore - memoryAfter;
                
                return new TaskResult("Cleanup", true, 
                    String.format("Limpieza completada - Memoria liberada: %d MB", 
                        memoryFreed / (1024 * 1024)));
                
            } catch (Exception e) {
                logger.error("Error en limpieza automática", e);
                return new TaskResult("Cleanup", false, "Error: " + e.getMessage());
            }
        });
    }
    
    private void healthCheckTask() {
        submitTask("HealthCheck", () -> {
            try {
                Map<String, Object> healthData = new HashMap<>();
                
                // Verificar estado del executor
                healthData.put("activeThreads", mainExecutor.getActiveCount());
                healthData.put("queueSize", mainExecutor.getQueue().size());
                healthData.put("completedTasks", mainExecutor.getCompletedTaskCount());
                
                // Verificar memoria
                Runtime runtime = Runtime.getRuntime();
                healthData.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
                healthData.put("memoryTotal", runtime.totalMemory());
                healthData.put("memoryMax", runtime.maxMemory());
                
                // Verificar estadísticas de caché
                var cacheStats = cacheManager.getAllStats();
                healthData.put("cacheSize", cacheStats.getTotalSize());
                healthData.put("cacheHitRate", 
                    (double) cacheStats.getTotalHits() / 
                    (cacheStats.getTotalHits() + cacheStats.getTotalMisses()));
                
                // Log periódico de salud
                if (totalTasksExecuted.get() % 10 == 0) {
                    logger.info("Health Check - Threads: {}, Queue: {}, Memory: {} MB", 
                        healthData.get("activeThreads"),
                        healthData.get("queueSize"),
                        (Long) healthData.get("memoryUsed") / (1024 * 1024));
                }
                
                return new TaskResult("HealthCheck", true, "Sistema saludable");
                
            } catch (Exception e) {
                logger.error("Error en health check", e);
                return new TaskResult("HealthCheck", false, "Error: " + e.getMessage());
            }
        });
    }
    
    // === ANÁLISIS AVANZADO ===
    
    private void analyzeTrends(Map<String, Object> metrics) {
        try {
            // Análisis de tendencias de ventas
            Number ventasHoy = (Number) metrics.get("ventasHoy");
            Number ventasSemana = (Number) metrics.get("ventasSemana");
            
            if (ventasHoy != null && ventasSemana != null) {
                double promedioSemanal = ventasSemana.doubleValue() / 7.0;
                double tendencia = (ventasHoy.doubleValue() / promedioSemanal - 1.0) * 100;
                
                if (Math.abs(tendencia) > 20) {
                    logger.info("Tendencia significativa detectada: {:.1f}% vs promedio semanal", 
                        tendencia);
                }
            }
            
            // Análisis de productos
            String productoMasVendido = (String) metrics.get("productoMasVendido");
            if (productoMasVendido != null && !"N/A".equals(productoMasVendido)) {
                logger.debug("Producto top: {}", productoMasVendido);
            }
            
        } catch (Exception e) {
            logger.debug("Error en análisis de tendencias: {}", e.getMessage());
        }
    }
    
    private void detectAnomalies(Map<String, Object> metrics) {
        try {
            // Detectar anomalías en ingresos
            Number ingresoHoy = (Number) metrics.get("ingresoHoy");
            Number ingresoSemana = (Number) metrics.get("ingresoSemana");
            
            if (ingresoHoy != null && ingresoSemana != null) {
                double promedioIngreso = ingresoSemana.doubleValue() / 7.0;
                double desviacion = Math.abs(ingresoHoy.doubleValue() - promedioIngreso) / promedioIngreso;
                
                if (desviacion > 0.5) { // 50% de desviación
                    logger.warn("Anomalía en ingresos detectada: ${:.2f} vs promedio ${:.2f}", 
                        ingresoHoy.doubleValue(), promedioIngreso);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error en detección de anomalías: {}", e.getMessage());
        }
    }
    
    // === GESTIÓN DE TAREAS ===
    
    public void submitTask(String taskName, Callable<TaskResult> task) {
        if (!isRunning.get()) {
            logger.warn("Intento de enviar tarea '{}' con procesador detenido", taskName);
            return;
        }
        
        activeTasks.incrementAndGet();
        totalTasksExecuted.incrementAndGet();
        
        completionService.submit(() -> {
            long startTime = System.currentTimeMillis();
            try {
                TaskResult result = task.call();
                long executionTime = System.currentTimeMillis() - startTime;
                
                recordTaskStatistics(taskName, executionTime, true);
                
                if (result.isSuccess()) {
                    logger.debug("Tarea '{}' completada exitosamente en {}ms: {}", 
                        taskName, executionTime, result.getMessage());
                } else {
                    logger.warn("Tarea '{}' falló en {}ms: {}", 
                        taskName, executionTime, result.getMessage());
                }
                
                return result;
                
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                recordTaskStatistics(taskName, executionTime, false);
                
                logger.error("Error ejecutando tarea '{}'", taskName, e);
                return new TaskResult(taskName, false, "Excepción: " + e.getMessage());
                
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }
    
    private void startTaskMonitoring() {
        CompletableFuture.runAsync(() -> {
            while (isRunning.get()) {
                try {
                    Future<TaskResult> completedTask = completionService.poll(1, TimeUnit.SECONDS);
                    if (completedTask != null) {
                        // Tarea completada procesada en submitTask
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.debug("Error en monitoreo de tareas: {}", e.getMessage());
                }
            }
        }, mainExecutor);
    }
    
    private void recordTaskStatistics(String taskName, long executionTime, boolean success) {
        taskStats.compute(taskName, (key, stats) -> {
            if (stats == null) {
                stats = new TaskStatistics(taskName);
            }
            stats.record(executionTime, success);
            return stats;
        });
    }
    
    // === ESTADÍSTICAS Y MONITOREO ===
    
    public BackgroundProcessorStats getStats() {
        return new BackgroundProcessorStats(
            isRunning.get(),
            activeTasks.get(),
            totalTasksExecuted.get(),
            mainExecutor.getActiveCount(),
            mainExecutor.getQueue().size(),
            mainExecutor.getCompletedTaskCount(),
            new HashMap<>(taskStats)
        );
    }
    
    public void printStats() {
        BackgroundProcessorStats stats = getStats();
        logger.info("=== ESTADÍSTICAS BACKGROUND PROCESSOR ===");
        logger.info("Estado: {}", stats.isRunning() ? "EJECUTÁNDOSE" : "DETENIDO");
        logger.info("Tareas activas: {}", stats.getActiveTasks());
        logger.info("Total ejecutadas: {}", stats.getTotalTasksExecuted());
        logger.info("Hilos activos: {}", stats.getActiveThreads());
        logger.info("Cola de espera: {}", stats.getQueueSize());
        
        logger.info("Estadísticas por tarea:");
        for (TaskStatistics taskStat : stats.getTaskStatistics().values()) {
            logger.info("  {}: {} ejecuciones, promedio {}ms, éxito {:.1f}%",
                taskStat.getTaskName(),
                taskStat.getExecutionCount(),
                taskStat.getAverageExecutionTime(),
                taskStat.getSuccessRate() * 100);
        }
    }
    
    // === CLASES INTERNAS ===
    
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
    
    public static class TaskResult {
        private final String taskName;
        private final boolean success;
        private final String message;
        private final LocalDateTime timestamp;
        
        public TaskResult(String taskName, boolean success, String message) {
            this.taskName = taskName;
            this.success = success;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public String getTaskName() { return taskName; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    private static class TaskStatistics {
        private final String taskName;
        private long executionCount = 0;
        private long totalExecutionTime = 0;
        private long successCount = 0;
        private LocalDateTime lastExecution;
        
        public TaskStatistics(String taskName) {
            this.taskName = taskName;
        }
        
        public synchronized void record(long executionTime, boolean success) {
            executionCount++;
            totalExecutionTime += executionTime;
            if (success) successCount++;
            lastExecution = LocalDateTime.now();
        }
        
        // Getters
        public String getTaskName() { return taskName; }
        public long getExecutionCount() { return executionCount; }
        public double getAverageExecutionTime() { 
            return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0.0; 
        }
        public double getSuccessRate() { 
            return executionCount > 0 ? (double) successCount / executionCount : 0.0; 
        }
        public LocalDateTime getLastExecution() { return lastExecution; }
    }
    
    public static class BackgroundProcessorStats {
        private final boolean running;
        private final int activeTasks;
        private final long totalTasksExecuted;
        private final int activeThreads;
        private final int queueSize;
        private final long completedTasks;
        private final Map<String, TaskStatistics> taskStatistics;
        
        public BackgroundProcessorStats(boolean running, int activeTasks, long totalTasksExecuted,
                                      int activeThreads, int queueSize, long completedTasks,
                                      Map<String, TaskStatistics> taskStatistics) {
            this.running = running;
            this.activeTasks = activeTasks;
            this.totalTasksExecuted = totalTasksExecuted;
            this.activeThreads = activeThreads;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
            this.taskStatistics = taskStatistics;
        }
        
        // Getters
        public boolean isRunning() { return running; }
        public int getActiveTasks() { return activeTasks; }
        public long getTotalTasksExecuted() { return totalTasksExecuted; }
        public int getActiveThreads() { return activeThreads; }
        public int getQueueSize() { return queueSize; }
        public long getCompletedTasks() { return completedTasks; }
        public Map<String, TaskStatistics> getTaskStatistics() { return taskStatistics; }
    }
}
