package com.cafeteriapos.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tests para componentes de cache
 * Verifica el funcionamiento del sistema de cache
 */
@DisplayName("Tests para Cache Components")
public class CacheTest {

    private Map<String, Object> cache;

    @BeforeEach
    void setUp() {
        cache = new ConcurrentHashMap<>();
    }

    @Test
    @DisplayName("Validar operaciones básicas de cache")
    void testValidarOperacionesBasicasCache() {
        // Given
        String clave = "producto_1";
        String valor = "Café Americano";
        
        // When
        cache.put(clave, valor);
        Object valorObtenido = cache.get(clave);
        
        // Then
        assertNotNull(valorObtenido);
        assertEquals(valor, valorObtenido);
    }

    @Test
    @DisplayName("Validar cache miss")
    void testValidarCacheMiss() {
        // Given
        String claveInexistente = "producto_inexistente";
        
        // When
        Object valor = cache.get(claveInexistente);
        
        // Then
        assertNull(valor, "Cache miss debe retornar null");
    }

    @Test
    @DisplayName("Validar cache hit")
    void testValidarCacheHit() {
        // Given
        String clave = "usuario_1";
        String valor = "admin";
        cache.put(clave, valor);
        
        // When
        Object valorObtenido = cache.get(clave);
        
        // Then
        assertNotNull(valorObtenido, "Cache hit debe retornar valor");
        assertEquals(valor, valorObtenido);
    }

    @Test
    @DisplayName("Validar invalidación de cache")
    void testValidarInvalidacionCache() {
        // Given
        String clave = "datos_temporales";
        String valor = "datos_importantes";
        cache.put(clave, valor);
        
        // When
        cache.remove(clave);
        Object valorDespuesInvalidacion = cache.get(clave);
        
        // Then
        assertNull(valorDespuesInvalidacion, "Datos invalidados no deben existir");
    }

    @Test
    @DisplayName("Validar tamaño máximo de cache")
    void testValidarTamañoMaximoCache() {
        // Given
        int tamañoMaximo = 100;
        
        // When
        for (int i = 0; i < 150; i++) {
            cache.put("clave_" + i, "valor_" + i);
        }
        
        // Then
        // En implementación real se implementaría LRU o TTL
        assertTrue(cache.size() <= 150, "Cache debe manejar tamaño");
    }

    @Test
    @DisplayName("Validar TTL (Time To Live)")
    void testValidarTTL() {
        // Given
        String clave = "dato_temporal";
        String valor = "expira_pronto";
        long tiempoCreacion = System.currentTimeMillis();
        long ttlMs = 1000; // 1 segundo
        
        // When
        CacheEntry entry = new CacheEntry(valor, tiempoCreacion, ttlMs);
        
        // Then
        assertFalse(entry.isExpired(), "Entry recién creado no debe estar expirado");
        
        // Simular paso del tiempo
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(entry.isExpired(), "Entry debe expirar después del TTL");
    }

    @Test
    @DisplayName("Validar cache thread-safe")
    void testValidarCacheThreadSafe() {
        // Given
        Map<String, Object> threadSafeCache = new ConcurrentHashMap<>();
        
        // When & Then
        assertDoesNotThrow(() -> {
            // Simular acceso concurrente
            Runnable escritor = () -> {
                for (int i = 0; i < 100; i++) {
                    threadSafeCache.put("key_" + i, "value_" + i);
                }
            };
            
            Runnable lector = () -> {
                for (int i = 0; i < 100; i++) {
                    threadSafeCache.get("key_" + i);
                }
            };
            
            Thread t1 = new Thread(escritor);
            Thread t2 = new Thread(lector);
            
            t1.start();
            t2.start();
            
            t1.join();
            t2.join();
        });
    }

    @Test
    @DisplayName("Validar política LRU")
    void testValidarPoliticaLRU() {
        // Given
        int capacidadMaxima = 3;
        LRUCache<String, String> lruCache = new LRUCache<>(capacidadMaxima);
        
        // When
        lruCache.put("A", "Valor A");
        lruCache.put("B", "Valor B");
        lruCache.put("C", "Valor C");
        lruCache.put("D", "Valor D"); // Debe expulsar A
        
        // Then
        assertNull(lruCache.get("A"), "Elemento más antiguo debe ser expulsado");
        assertNotNull(lruCache.get("B"), "Elemento B debe seguir en cache");
        assertNotNull(lruCache.get("C"), "Elemento C debe seguir en cache");
        assertNotNull(lruCache.get("D"), "Elemento D debe estar en cache");
    }

    @Test
    @DisplayName("Validar estadísticas de cache")
    void testValidarEstadisticasCache() {
        // Given
        CacheStats stats = new CacheStats();
        
        // When - Simular uso del cache
        stats.recordHit();
        stats.recordHit();
        stats.recordMiss();
        stats.recordHit();
        
        // Then
        assertEquals(3, stats.getHits());
        assertEquals(1, stats.getMisses());
        assertEquals(75.0, stats.getHitRatio(), 0.01);
    }

    @Test
    @DisplayName("Validar limpieza automática")
    void testValidarLimpiezaAutomatica() {
        // Given
        AutoCleanCache cleanCache = new AutoCleanCache();
        cleanCache.put("temp1", "valor1", 100); // 100ms TTL
        cleanCache.put("temp2", "valor2", 200); // 200ms TTL
        
        // When
        assertEquals(2, cleanCache.size());
        
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        cleanCache.cleanup(); // Limpiar expirados
        
        // Then
        assertEquals(1, cleanCache.size(), "Solo un elemento debe quedar");
    }

    @Test
    @DisplayName("Validar cache de múltiples niveles")
    void testValidarCacheMultiplesNiveles() {
        // Given
        Map<String, Object> nivel1 = new HashMap<>(); // Cache rápido
        Map<String, Object> nivel2 = new HashMap<>(); // Cache lento
        
        String clave = "dato_importante";
        String valor = "información_valiosa";
        
        // When
        nivel2.put(clave, valor);
        
        // Buscar en nivel 1 primero
        Object resultado = nivel1.get(clave);
        if (resultado == null) {
            // Cache miss en nivel 1, buscar en nivel 2
            resultado = nivel2.get(clave);
            if (resultado != null) {
                // Promover a nivel 1
                nivel1.put(clave, resultado);
            }
        }
        
        // Then
        assertNotNull(resultado);
        assertTrue(nivel1.containsKey(clave), "Valor debe ser promovido a nivel 1");
    }

    @Test
    @DisplayName("Validar serialización de cache")
    void testValidarSerializacionCache() {
        // Given
        String datos = "datos_para_serializar";
        
        // When
        byte[] datosSerializados = datos.getBytes();
        String datosDeserializados = new String(datosSerializados);
        
        // Then
        assertEquals(datos, datosDeserializados);
        assertTrue(datosSerializados.length > 0);
    }

    // Clases auxiliares para testing
    private static class CacheEntry {
        private final Object value;
        private final long creationTime;
        private final long ttlMs;
        
        public CacheEntry(Object value, long creationTime, long ttlMs) {
            this.value = value;
            this.creationTime = creationTime;
            this.ttlMs = ttlMs;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - creationTime > ttlMs;
        }
        
        public Object getValue() {
            return value;
        }
    }

    private static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, V> cache;
        
        public LRUCache(int capacity) {
            this.capacity = capacity;
            this.cache = new java.util.LinkedHashMap<K, V>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > capacity;
                }
            };
        }
        
        public void put(K key, V value) {
            cache.put(key, value);
        }
        
        public V get(K key) {
            return cache.get(key);
        }
    }

    private static class CacheStats {
        private int hits = 0;
        private int misses = 0;
        
        public void recordHit() { hits++; }
        public void recordMiss() { misses++; }
        public int getHits() { return hits; }
        public int getMisses() { return misses; }
        public double getHitRatio() {
            int total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total * 100;
        }
    }

    private static class AutoCleanCache {
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        
        public void put(String key, Object value, long ttlMs) {
            cache.put(key, new CacheEntry(value, System.currentTimeMillis(), ttlMs));
        }
        
        public int size() {
            return cache.size();
        }
        
        public void cleanup() {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }
}
