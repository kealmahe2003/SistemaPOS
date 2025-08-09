package com.cafeteriapos.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sistema de caché avanzado con LRU, TTL y gestión automática de memoria
 * Optimizado para alto rendimiento y concurrencia
 */
public class PerformanceCache<K, V> {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceCache.class);
    
    // === CONFIGURACIÓN DEL CACHÉ ===
    private final int maxSize;
    private final long ttlMillis;
    private final boolean enableStats;
    
    // === ESTRUCTURAS DE DATOS THREAD-SAFE ===
    private final Map<K, CacheEntry<V>> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService cleanupExecutor;
    
    // === ESTADÍSTICAS DE PERFORMANCE ===
    private volatile long hits = 0;
    private volatile long misses = 0;
    private volatile long evictions = 0;
    private volatile long cleanups = 0;
    
    /**
     * Constructor del caché con configuración personalizada
     */
    public PerformanceCache(int maxSize, long ttlMinutes, boolean enableStats) {
        this.maxSize = maxSize;
        this.ttlMillis = ttlMinutes * 60 * 1000L;
        this.enableStats = enableStats;
        
        // LRU Cache thread-safe con orden de acceso
        this.cache = new ConcurrentHashMap<>(maxSize);
        
        // Limpieza automática cada 2 minutos
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PerformanceCache-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        startCleanupTask();
        
        logger.info("PerformanceCache iniciado: maxSize={}, ttl={}min, stats={}", 
            maxSize, ttlMinutes, enableStats);
    }
    
    /**
     * Constructor con valores por defecto
     */
    public PerformanceCache() {
        this(1000, 5, true);
    }
    
    /**
     * Obtiene un valor del caché
     */
    public V get(K key) {
        lock.readLock().lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            
            if (entry == null) {
                recordMiss();
                return null;
            }
            
            if (isExpired(entry)) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    // Double-check después de obtener write lock
                    entry = cache.get(key);
                    if (entry != null && isExpired(entry)) {
                        cache.remove(key);
                        recordMiss();
                        return null;
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            
            if (entry != null) {
                entry.updateAccessTime();
                recordHit();
                return entry.getValue();
            }
            
            recordMiss();
            return null;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Almacena un valor en el caché
     */
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            // Verificar si necesitamos hacer espacio
            if (cache.size() >= maxSize && !cache.containsKey(key)) {
                evictLRU();
            }
            
            CacheEntry<V> entry = new CacheEntry<>(value);
            cache.put(key, entry);
            
            logger.debug("Cache entry añadida: key={}, size={}/{}", key, cache.size(), maxSize);
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Método conveniente: obtiene o calcula el valor
     */
    public V getOrCompute(K key, java.util.function.Supplier<V> supplier) {
        V value = get(key);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                put(key, value);
            }
        }
        return value;
    }
    
    /**
     * Invalida una entrada específica
     */
    public void invalidate(K key) {
        lock.writeLock().lock();
        try {
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Limpia todo el caché
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            logger.info("Cache limpiado completamente");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtiene estadísticas del caché
     */
    public CacheStats getStats() {
        lock.readLock().lock();
        try {
            long totalRequests = hits + misses;
            double hitRate = totalRequests > 0 ? (double) hits / totalRequests : 0.0;
            
            return new CacheStats(
                cache.size(),
                maxSize,
                hits,
                misses,
                hitRate,
                evictions,
                cleanups
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Cierra el caché y libera recursos
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        clear();
        logger.info("PerformanceCache cerrado");
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupExpired();
            } catch (Exception e) {
                logger.error("Error en cleanup automático del caché", e);
            }
        }, 2, 2, TimeUnit.MINUTES);
    }
    
    private void cleanupExpired() {
        lock.writeLock().lock();
        try {
            int removedCount = 0;
            var iterator = cache.entrySet().iterator();
            
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (isExpired(entry.getValue())) {
                    iterator.remove();
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                cleanups++;
                logger.debug("Cleanup automático: {} entradas expiradas removidas", removedCount);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void evictLRU() {
        // Encontrar la entrada menos recientemente usada
        K lruKey = null;
        LocalDateTime oldestAccess = LocalDateTime.now();
        
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            LocalDateTime accessTime = entry.getValue().getLastAccessed();
            if (accessTime.isBefore(oldestAccess)) {
                oldestAccess = accessTime;
                lruKey = entry.getKey();
            }
        }
        
        if (lruKey != null) {
            cache.remove(lruKey);
            evictions++;
            logger.debug("Entrada LRU evicted: key={}", lruKey);
        }
    }
    
    private boolean isExpired(CacheEntry<V> entry) {
        return ChronoUnit.MILLIS.between(entry.getCreatedAt(), LocalDateTime.now()) > ttlMillis;
    }
    
    private void recordHit() {
        if (enableStats) {
            hits++;
        }
    }
    
    private void recordMiss() {
        if (enableStats) {
            misses++;
        }
    }
    
    // === CLASE INTERNA: ENTRADA DEL CACHÉ ===
    
    private static class CacheEntry<V> {
        private final V value;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime lastAccessed;
        
        public CacheEntry(V value) {
            this.value = value;
            this.createdAt = LocalDateTime.now();
            this.lastAccessed = createdAt;
        }
        
        public V getValue() {
            return value;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public LocalDateTime getLastAccessed() {
            return lastAccessed;
        }
        
        public void updateAccessTime() {
            this.lastAccessed = LocalDateTime.now();
        }
    }
    
    // === CLASE INTERNA: ESTADÍSTICAS ===
    
    public static class CacheStats {
        private final int currentSize;
        private final int maxSize;
        private final long hits;
        private final long misses;
        private final double hitRate;
        private final long evictions;
        private final long cleanups;
        
        public CacheStats(int currentSize, int maxSize, long hits, long misses, 
                         double hitRate, long evictions, long cleanups) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
            this.evictions = evictions;
            this.cleanups = cleanups;
        }
        
        // Getters
        public int getCurrentSize() { return currentSize; }
        public int getMaxSize() { return maxSize; }
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public double getHitRate() { return hitRate; }
        public long getEvictions() { return evictions; }
        public long getCleanups() { return cleanups; }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{size=%d/%d, hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, cleanups=%d}",
                currentSize, maxSize, hits, misses, hitRate * 100, evictions, cleanups
            );
        }
    }
}
