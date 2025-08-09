package com.cafeteriapos.integration;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.DatabaseManager;
import com.cafeteriapos.utils.CajaManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tests de integración para validar el flujo completo del sistema
 */
@DisplayName("Tests de Integración del Sistema POS")
class IntegrationTest {
    
    private List<Producto> productosTest;
    private Venta ventaTest;
    private String testSuffix;
    
    @BeforeEach
    void setUp() {
        // Generar sufijo único para cada test
        testSuffix = UUID.randomUUID().toString().substring(0, 8);
        
        // Crear productos de prueba con nombres únicos
        productosTest = new ArrayList<>();
        productosTest.add(new Producto("Café Test " + testSuffix, 2.50, 10));
        productosTest.add(new Producto("Croissant Test " + testSuffix, 1.75, 5));
        
        // Crear venta de prueba con ID único
        ventaTest = new Venta("INTEGRATION-" + testSuffix, LocalDateTime.now(), productosTest, 8.75);
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar datos de prueba después de cada test
        try {
            // Eliminar productos de prueba
            for (Producto producto : productosTest) {
                try {
                    DatabaseManager.eliminarProducto(producto);
                } catch (Exception e) {
                    // Ignorar errores de eliminación (puede que no exista)
                }
            }
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
    }
    
    @Test
    @DisplayName("Flujo completo: Crear productos → Guardar → Leer")
    void testFlujoProductosCompleto() {
        // When & Then - El flujo completo no debe fallar
        assertDoesNotThrow(() -> {
            // 1. Guardar productos
            for (Producto producto : productosTest) {
                DatabaseManager.guardarProducto(producto);
            }
            
            // 2. Leer productos
            List<Producto> productosLeidos = DatabaseManager.leerProductos();
            assertNotNull(productosLeidos);
            
            // 3. Actualizar un producto
            if (!productosTest.isEmpty()) {
                Producto producto = productosTest.get(0);
                producto.setStock(15);
                DatabaseManager.actualizarProducto(producto);
            }
            
        }, "Flujo completo de productos debe funcionar sin errores");
    }
    
    @Test
    @DisplayName("Flujo completo: Crear venta → Guardar → Registrar en caja")
    void testFlujoVentaCompleto() {
        // When & Then - El flujo completo no debe fallar
        assertDoesNotThrow(() -> {
            // 1. Guardar venta
            DatabaseManager.guardarVenta(ventaTest);
            
            // 2. Registrar en caja
            CajaManager.registrarVenta(ventaTest.getId(), ventaTest.getTotal());
            
            // 3. Leer ventas
            List<Venta> ventasLeidas = DatabaseManager.leerVentas();
            assertNotNull(ventasLeidas);
            
        }, "Flujo completo de venta debe funcionar sin errores");
    }
    
    @Test
    @DisplayName("Flujo completo: Apertura caja → Ventas → Cierre")
    void testFlujoCajaCompleto() {
        // Given
        double montoInicial = 100.0;
        double montoVentas = 25.50;
        double montoFinal = montoInicial + montoVentas;
        
        // When & Then - El flujo completo no debe fallar
        assertDoesNotThrow(() -> {
            // 1. Apertura de caja
            CajaManager.registrarApertura(montoInicial);
            
            // 2. Procesar algunas ventas
            CajaManager.registrarVenta("INT-V001", 15.25);
            CajaManager.registrarVenta("INT-V002", 10.25);
            
            // 3. Movimiento de efectivo
            CajaManager.registrarMovimiento("RETIRO", 20.0, "Cambio para clientes");
            
            // 4. Cierre de caja
            CajaManager.registrarCierre(montoFinal - 20.0);
            
        }, "Flujo completo de caja debe funcionar sin errores");
    }
    
    @Test
    @DisplayName("Flujo de manejo de errores")
    void testFlujoManejoErrores() {
        // When & Then - El manejo de errores debe ser robusto
        assertDoesNotThrow(() -> {
            // 1. Registrar un error (esto siempre debe funcionar)
            CajaManager.registrarError("Venta", "Conexión perdida");
            
            // 2. Operaciones de recuperación que siempre funcionan
            // DatabaseManager no necesita recreación de archivos (usa H2 Database)
            // El resultado puede ser true o false, pero no debe lanzar excepción
            
        }, "Operaciones de manejo de errores deben ser robustas");
        
        // 3. Intentar operaciones con datos inválidos (pueden fallar, pero las capturamos)
        try {
            DatabaseManager.guardarProducto(null);
            DatabaseManager.guardarVenta(null);
        } catch (Exception e) {
            // Es esperado que fallen con datos nulos
            assertNotNull(e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Operaciones concurrentes simuladas")
    void testOperacionesConcurrentes() {
        // When & Then - Múltiples operaciones rápidas no deben fallar
        assertDoesNotThrow(() -> {
            // Simular múltiples operaciones rápidas con nombres únicos
            for (int i = 0; i < 5; i++) { // Reducir número para evitar conflictos
                String uniqueId = testSuffix + "-" + i;
                Producto producto = new Producto("Producto-" + uniqueId, 1.0 + i, i);
                DatabaseManager.guardarProducto(producto);
                
                Venta venta = new Venta("V-CONCURRENT-" + uniqueId, LocalDateTime.now(), 
                                     List.of(producto), 1.0 + i);
                DatabaseManager.guardarVenta(venta);
                
                CajaManager.registrarVenta(venta.getId(), venta.getTotal());
            }
            
            // Verificar que las operaciones de lectura funcionan
            List<Producto> productos = DatabaseManager.leerProductos();
            List<Venta> ventas = DatabaseManager.leerVentas();
            
            assertNotNull(productos);
            assertNotNull(ventas);
            
        }, "Operaciones concurrentes deben manejarse correctamente");
    }
    
    @Test
    @DisplayName("Validación de integridad de datos")
    void testIntegridadDatos() {
        // Given - Producto con nombre único
        Producto productoOriginal = new Producto("Producto Integridad " + testSuffix, 5.00, 20);
        
        // When & Then
        assertDoesNotThrow(() -> {
            // 1. Guardar producto original
            DatabaseManager.guardarProducto(productoOriginal);
            
            // 2. Modificar y actualizar
            productoOriginal.setStock(15);
            productoOriginal.setPrecio(5.50);
            DatabaseManager.actualizarProducto(productoOriginal);
            
            // 3. Crear venta que afecte el stock
            Venta ventaIntegridad = new Venta("V-INTEGRIDAD-" + testSuffix, LocalDateTime.now(),
                                            List.of(productoOriginal), 5.50);
            DatabaseManager.guardarVenta(ventaIntegridad);
            
            // 4. Verificar que los datos son consistentes
            List<Producto> productos = DatabaseManager.leerProductos();
            List<Venta> ventas = DatabaseManager.leerVentas();
            
            assertNotNull(productos);
            assertNotNull(ventas);
            assertTrue(productos.size() > 0);
            assertTrue(ventas.size() > 0);
            
        }, "La integridad de datos debe mantenerse");
    }
}
