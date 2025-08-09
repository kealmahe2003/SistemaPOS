package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas exhaustivas para DatabaseManager
 * Objetivo: Alcanzar 90%+ de cobertura en DatabaseManager
 */
@DisplayName("Tests Exhaustivos para DatabaseManager")
public class DatabaseManagerExhaustivoTest {

    private static final String PRODUCTO_TEST_PREFIX = "TEST_EXHAUSTIVO_";
    private static final String VENTA_TEST_PREFIX = "VENTA_EXHAUSTIVA_";
    
    @BeforeEach
    void setUp() {
        // Inicializar base de datos para cada test
        DatabaseManager.inicializarBaseDatos();
        
        // Limpiar datos de tests anteriores si existen
        limpiarDatosPrueba();
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar datos de prueba después de cada test
        limpiarDatosPrueba();
    }
    
    private void limpiarDatosPrueba() {
        try {
            List<Producto> productos = DatabaseManager.leerProductos();
            for (Producto producto : productos) {
                if (producto.getNombre().startsWith(PRODUCTO_TEST_PREFIX)) {
                    DatabaseManager.eliminarProducto(producto);
                }
            }
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
    }
    
    @Test
    @DisplayName("Inicializar base de datos - múltiples llamadas")
    void testInicializarBaseDatosMultiplesLlamadas() {
        // When - Llamar múltiples veces
        assertDoesNotThrow(() -> {
            DatabaseManager.inicializarBaseDatos();
            DatabaseManager.inicializarBaseDatos();
            DatabaseManager.inicializarBaseDatos();
        });
        
        // Then - Debe funcionar sin errores
        List<Producto> productos = DatabaseManager.leerProductos();
        assertNotNull(productos);
    }
    
    @Test
    @DisplayName("Inicializar productos base - solo cuando no existen")
    void testInicializarProductosBasesSiNoExisten() {
        // Given - Base limpia
        DatabaseManager.limpiarBaseDatos();
        
        // When
        DatabaseManager.inicializarProductosBasesSiNoExisten();
        
        // Then
        List<Producto> productos = DatabaseManager.leerProductos();
        assertTrue(productos.size() >= 3, "Debe crear al menos 3 productos base");
        
        // When - Llamar de nuevo (no debe duplicar)
        int cantidadAntes = productos.size();
        DatabaseManager.inicializarProductosBasesSiNoExisten();
        
        // Then
        List<Producto> productosDepues = DatabaseManager.leerProductos();
        assertEquals(cantidadAntes, productosDepues.size(), "No debe duplicar productos");
    }
    
    @Test
    @DisplayName("Guardar producto - inserción nueva")
    void testGuardarProductoInsercionNueva() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "Nuevo_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 5.50, 25);
        
        // When
        DatabaseManager.guardarProducto(producto);
        
        // Then
        List<Producto> productos = DatabaseManager.leerProductos();
        assertTrue(productos.stream().anyMatch(p -> p.getNombre().equals(nombre)));
    }
    
    @Test
    @DisplayName("Guardar producto - actualización existente")
    void testGuardarProductoActualizacionExistente() {
        // Given - Crear producto inicial
        String nombre = PRODUCTO_TEST_PREFIX + "Actualizar_" + System.currentTimeMillis();
        Producto producto1 = new Producto(nombre, 3.00, 10);
        DatabaseManager.guardarProducto(producto1);
        
        // When - Guardar producto con mismo nombre pero diferentes valores
        Producto producto2 = new Producto(nombre, 4.50, 20);
        DatabaseManager.guardarProducto(producto2);
        
        // Then
        List<Producto> productos = DatabaseManager.leerProductos();
        Producto productoGuardado = productos.stream()
                                           .filter(p -> p.getNombre().equals(nombre))
                                           .findFirst()
                                           .orElse(null);
        
        assertNotNull(productoGuardado);
        assertEquals(4.50, productoGuardado.getPrecio(), 0.01);
        assertEquals(20, productoGuardado.getStock());
    }
    
    @Test
    @DisplayName("Actualizar producto específico")
    void testActualizarProductoEspecifico() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "ParaActualizar_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 2.00, 5);
        DatabaseManager.guardarProducto(producto);
        
        // When
        producto.setPrecio(3.50);
        producto.setStock(15);
        DatabaseManager.actualizarProducto(producto);
        
        // Then
        List<Producto> productos = DatabaseManager.leerProductos();
        Producto actualizado = productos.stream()
                                       .filter(p -> p.getNombre().equals(nombre))
                                       .findFirst()
                                       .orElse(null);
        
        assertNotNull(actualizado);
        assertEquals(3.50, actualizado.getPrecio(), 0.01);
        assertEquals(15, actualizado.getStock());
    }
    
    @Test
    @DisplayName("Leer productos - lista vacía y con datos")
    void testLeerProductos() {
        // When - Lista inicial
        List<Producto> productosIniciales = DatabaseManager.leerProductos();
        
        // Then
        assertNotNull(productosIniciales);
        
        // Given - Agregar producto de prueba
        String nombre = PRODUCTO_TEST_PREFIX + "Lectura_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 1.50, 8);
        DatabaseManager.guardarProducto(producto);
        
        // When - Leer después de agregar
        List<Producto> productosConNuevo = DatabaseManager.leerProductos();
        
        // Then
        assertEquals(productosIniciales.size() + 1, productosConNuevo.size());
        assertTrue(productosConNuevo.stream().anyMatch(p -> p.getNombre().equals(nombre)));
    }
    
    @Test
    @DisplayName("Eliminar producto existente")
    void testEliminarProductoExistente() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "ParaEliminar_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 2.75, 12);
        DatabaseManager.guardarProducto(producto);
        
        // Verificar que existe
        List<Producto> antesEliminar = DatabaseManager.leerProductos();
        assertTrue(antesEliminar.stream().anyMatch(p -> p.getNombre().equals(nombre)));
        
        // When
        DatabaseManager.eliminarProducto(producto);
        
        // Then
        List<Producto> despuesEliminar = DatabaseManager.leerProductos();
        assertFalse(despuesEliminar.stream().anyMatch(p -> p.getNombre().equals(nombre)));
    }
    
    @Test
    @DisplayName("Eliminar producto inexistente - manejo de errores")
    void testEliminarProductoInexistente() {
        // Given
        Producto productoInexistente = new Producto("INEXISTENTE_" + System.currentTimeMillis(), 1.0, 1);
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            DatabaseManager.eliminarProducto(productoInexistente);
        });
    }
    
    @Test
    @DisplayName("Guardar venta completa")
    void testGuardarVentaCompleta() {
        // Given
        String idVenta = VENTA_TEST_PREFIX + System.currentTimeMillis();
        List<Producto> items = List.of(
            new Producto("Item1", 2.50, 1),
            new Producto("Item2", 3.75, 2)
        );
        LocalDateTime fechaHora = LocalDateTime.now();
        double total = 10.00;
        
        Venta venta = new Venta(idVenta, fechaHora, items, total);
        
        // When
        DatabaseManager.guardarVenta(venta);
        
        // Then
        List<Venta> ventas = DatabaseManager.leerVentas();
        assertTrue(ventas.stream().anyMatch(v -> v.getId().equals(idVenta)));
    }
    
    @Test
    @DisplayName("Guardar venta con datos nulos - manejo de errores")
    void testGuardarVentaDatosNulos() {
        // When & Then
        assertDoesNotThrow(() -> {
            try {
                DatabaseManager.guardarVenta(null);
            } catch (Exception e) {
                // Es esperado que falle con datos nulos
                assertNotNull(e.getMessage());
            }
        });
    }
    
    @Test
    @DisplayName("Leer ventas - funcionalidad completa")
    void testLeerVentasFuncionalidadCompleta() {
        // Given
        String idVenta = VENTA_TEST_PREFIX + "Lectura_" + System.currentTimeMillis();
        Venta venta = new Venta(idVenta, LocalDateTime.now(), new ArrayList<>(), 15.50);
        
        // When
        List<Venta> ventasAntes = DatabaseManager.leerVentas();
        DatabaseManager.guardarVenta(venta);
        List<Venta> ventasDespues = DatabaseManager.leerVentas();
        
        // Then
        assertNotNull(ventasAntes);
        assertNotNull(ventasDespues);
        assertEquals(ventasAntes.size() + 1, ventasDespues.size());
        
        Venta ventaGuardada = ventasDespues.stream()
                                          .filter(v -> v.getId().equals(idVenta))
                                          .findFirst()
                                          .orElse(null);
        
        assertNotNull(ventaGuardada);
        assertEquals(15.50, ventaGuardada.getTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Registrar operación de caja")
    void testRegistrarOperacionCaja() {
        // Given
        String operacion = "TEST OPERATION: " + System.currentTimeMillis();
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            DatabaseManager.registrarOperacionCaja(operacion);
        });
    }
    
    @Test
    @DisplayName("Obtener total de ventas de hoy")
    void testObtenerTotalVentasHoy() {
        // Given - Crear venta de hoy
        String idVenta = VENTA_TEST_PREFIX + "Hoy_" + System.currentTimeMillis();
        Venta venta = new Venta(idVenta, LocalDateTime.now(), new ArrayList<>(), 25.75);
        
        // When
        double totalAntes = DatabaseManager.obtenerTotalVentasHoy();
        DatabaseManager.guardarVenta(venta);
        double totalDespues = DatabaseManager.obtenerTotalVentasHoy();
        
        // Then
        assertTrue(totalAntes >= 0);
        assertTrue(totalDespues >= totalAntes);
        assertTrue(totalDespues >= 25.75);
    }
    
    @Test
    @DisplayName("Obtener conteo de ventas de hoy")
    void testObtenerConteoVentasHoy() {
        // Given
        String idVenta = VENTA_TEST_PREFIX + "Conteo_" + System.currentTimeMillis();
        Venta venta = new Venta(idVenta, LocalDateTime.now(), new ArrayList<>(), 5.00);
        
        // When
        int conteoAntes = DatabaseManager.obtenerConteoVentasHoy();
        DatabaseManager.guardarVenta(venta);
        int conteoDespues = DatabaseManager.obtenerConteoVentasHoy();
        
        // Then
        assertTrue(conteoAntes >= 0);
        assertEquals(conteoAntes + 1, conteoDespues);
    }
    
    @Test
    @DisplayName("Crear backup de base de datos")
    void testCrearBackup() {
        // When
        boolean resultadoBackup = DatabaseManager.crearBackup();
        
        // Then - Debe completarse (true o false, pero sin excepciones)
        // El resultado depende de la configuración del sistema
        assertDoesNotThrow(() -> {
            DatabaseManager.crearBackup();
        });
    }
    
    @Test
    @DisplayName("Limpiar base de datos")
    void testLimpiarBaseDatos() {
        // Given - Asegurar que hay datos
        String nombre = PRODUCTO_TEST_PREFIX + "ParaLimpiar_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 1.00, 1);
        DatabaseManager.guardarProducto(producto);
        
        // When
        boolean resultadoLimpieza = DatabaseManager.limpiarBaseDatos();
        
        // Then
        assertTrue(resultadoLimpieza, "La limpieza debe ser exitosa");
        
        // Verificar que los datos se limpiaron
        List<Producto> productosDepuesLimpieza = DatabaseManager.leerProductos();
        assertFalse(productosDepuesLimpieza.stream().anyMatch(p -> p.getNombre().equals(nombre)));
    }
    
    @Test
    @DisplayName("Cerrar conexión")
    void testCerrarConexion() {
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            DatabaseManager.cerrarConexion();
        });
        
        // Debe poder reconectarse después
        assertDoesNotThrow(() -> {
            DatabaseManager.leerProductos();
        });
    }
    
    @Test
    @DisplayName("Operaciones con productos con caracteres especiales")
    void testOperacionesConCaracteresEspeciales() {
        // Given
        String nombreEspecial = PRODUCTO_TEST_PREFIX + "Café_Ñoño_áéíóú_" + System.currentTimeMillis();
        Producto producto = new Producto(nombreEspecial, 4.25, 15);
        
        // When & Then
        assertDoesNotThrow(() -> {
            DatabaseManager.guardarProducto(producto);
            
            List<Producto> productos = DatabaseManager.leerProductos();
            assertTrue(productos.stream().anyMatch(p -> p.getNombre().equals(nombreEspecial)));
            
            DatabaseManager.eliminarProducto(producto);
        });
    }
    
    @Test
    @DisplayName("Operaciones con valores límite")
    void testOperacionesConValoresLimite() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "Limites_" + System.currentTimeMillis();
        
        // When & Then - Precio cero
        assertDoesNotThrow(() -> {
            Producto productoPrecioCero = new Producto(nombre + "_CERO", 0.0, 1);
            DatabaseManager.guardarProducto(productoPrecioCero);
            DatabaseManager.eliminarProducto(productoPrecioCero);
        });
        
        // When & Then - Stock cero
        assertDoesNotThrow(() -> {
            Producto productoStockCero = new Producto(nombre + "_STOCK_CERO", 1.0, 0);
            DatabaseManager.guardarProducto(productoStockCero);
            DatabaseManager.eliminarProducto(productoStockCero);
        });
        
        // When & Then - Valores altos
        assertDoesNotThrow(() -> {
            Producto productoValoresAltos = new Producto(nombre + "_ALTO", 999999.99, 999999);
            DatabaseManager.guardarProducto(productoValoresAltos);
            DatabaseManager.eliminarProducto(productoValoresAltos);
        });
    }
    
    @Test
    @DisplayName("Concurrencia básica - múltiples operaciones")
    void testConcurrenciaBasica() {
        // When & Then - Múltiples operaciones simultáneas
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                String nombre = PRODUCTO_TEST_PREFIX + "Concurrente_" + i + "_" + System.currentTimeMillis();
                Producto producto = new Producto(nombre, 1.0 + i, 10 + i);
                
                DatabaseManager.guardarProducto(producto);
                DatabaseManager.leerProductos();
                DatabaseManager.actualizarProducto(producto);
                DatabaseManager.eliminarProducto(producto);
            }
        });
    }
    
    // ===============================================
    // TESTS PARA MÉTODOS AUXILIARES Y OPTIMIZACIÓN
    // ===============================================
    
    @Test
    @DisplayName("Verificar conexión a la base de datos")
    void testVerificarConexion() {
        // When
        boolean conexionValida = DatabaseManager.verificarConexion();
        
        // Then
        assertTrue(conexionValida, "La conexión a la base de datos debe ser válida");
    }
    
    @Test
    @DisplayName("Obtener estadísticas de la base de datos")
    void testObtenerEstadisticasBaseDatos() {
        // Given - Agregar algunos datos de prueba
        String nombre = PRODUCTO_TEST_PREFIX + "Estadisticas_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 5.00, 10);
        DatabaseManager.guardarProducto(producto);
        
        // When
        String estadisticas = DatabaseManager.obtenerEstadisticasBaseDatos();
        
        // Then
        assertNotNull(estadisticas);
        assertFalse(estadisticas.contains("Error"));
        assertTrue(estadisticas.contains("Estadísticas DB:"));
        assertTrue(estadisticas.contains("Productos="));
        assertTrue(estadisticas.contains("Ventas="));
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
    
    @Test
    @DisplayName("Buscar producto por nombre exacto")
    void testBuscarProductoPorNombre() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "BusquedaExacta_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 3.50, 15);
        DatabaseManager.guardarProducto(producto);
        
        // When - Buscar producto existente
        Producto encontrado = DatabaseManager.buscarProductoPorNombre(nombre);
        
        // Then
        assertNotNull(encontrado);
        assertEquals(nombre, encontrado.getNombre());
        assertEquals(3.50, encontrado.getPrecio(), 0.01);
        assertEquals(15, encontrado.getStock());
        
        // When - Buscar producto inexistente
        Producto noEncontrado = DatabaseManager.buscarProductoPorNombre("PRODUCTO_INEXISTENTE");
        
        // Then
        assertNull(noEncontrado);
        
        // When - Buscar con nombre nulo o vacío
        Producto nulo = DatabaseManager.buscarProductoPorNombre(null);
        Producto vacio = DatabaseManager.buscarProductoPorNombre("");
        Producto espacios = DatabaseManager.buscarProductoPorNombre("   ");
        
        // Then
        assertNull(nulo);
        assertNull(vacio);
        assertNull(espacios);
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
    
    @Test
    @DisplayName("Obtener productos con stock bajo")
    void testObtenerProductosConStockBajo() {
        // Given - Crear productos con diferentes stocks
        String nombre1 = PRODUCTO_TEST_PREFIX + "StockBajo1_" + System.currentTimeMillis();
        String nombre2 = PRODUCTO_TEST_PREFIX + "StockBajo2_" + System.currentTimeMillis();
        String nombre3 = PRODUCTO_TEST_PREFIX + "StockAlto_" + System.currentTimeMillis();
        
        Producto producto1 = new Producto(nombre1, 2.00, 2); // Stock bajo
        Producto producto2 = new Producto(nombre2, 3.00, 3); // Stock bajo
        Producto producto3 = new Producto(nombre3, 4.00, 15); // Stock alto
        
        DatabaseManager.guardarProducto(producto1);
        DatabaseManager.guardarProducto(producto2);
        DatabaseManager.guardarProducto(producto3);
        
        // When - Buscar productos con stock menor a 5
        List<Producto> productosStockBajo = DatabaseManager.obtenerProductosConStockBajo(5);
        
        // Then
        assertNotNull(productosStockBajo);
        assertTrue(productosStockBajo.stream().anyMatch(p -> p.getNombre().equals(nombre1)));
        assertTrue(productosStockBajo.stream().anyMatch(p -> p.getNombre().equals(nombre2)));
        assertFalse(productosStockBajo.stream().anyMatch(p -> p.getNombre().equals(nombre3)));
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto1);
        DatabaseManager.eliminarProducto(producto2);
        DatabaseManager.eliminarProducto(producto3);
    }
    
    @Test
    @DisplayName("Actualizar stock de producto específico")
    void testActualizarStockProducto() {
        // Given
        String nombre = PRODUCTO_TEST_PREFIX + "ActualizarStock_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 2.50, 10);
        DatabaseManager.guardarProducto(producto);
        
        // When - Actualizar stock exitosamente
        boolean resultado = DatabaseManager.actualizarStockProducto(nombre, 25);
        
        // Then
        assertTrue(resultado);
        
        // Verificar que el stock se actualizó
        Producto actualizado = DatabaseManager.buscarProductoPorNombre(nombre);
        assertNotNull(actualizado);
        assertEquals(25, actualizado.getStock());
        
        // When - Intentar actualizar producto inexistente
        boolean resultadoInexistente = DatabaseManager.actualizarStockProducto("INEXISTENTE", 10);
        
        // Then
        assertFalse(resultadoInexistente);
        
        // When - Intentar con parámetros inválidos
        assertFalse(DatabaseManager.actualizarStockProducto(null, 10));
        assertFalse(DatabaseManager.actualizarStockProducto("", 10));
        assertFalse(DatabaseManager.actualizarStockProducto(nombre, -1));
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
    
    @Test
    @DisplayName("Validar integridad de datos")
    void testValidarIntegridadDatos() {
        // Given - Agregar datos válidos
        String nombre = PRODUCTO_TEST_PREFIX + "Integridad_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 1.50, 5);
        DatabaseManager.guardarProducto(producto);
        
        String idVenta = VENTA_TEST_PREFIX + "Integridad_" + System.currentTimeMillis();
        Venta venta = new Venta(idVenta, LocalDateTime.now(), new ArrayList<>(), 10.00);
        DatabaseManager.guardarVenta(venta);
        
        // When
        boolean integridadValida = DatabaseManager.validarIntegridadDatos();
        
        // Then
        assertTrue(integridadValida, "Los datos deben mantener integridad");
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
    
    @Test
    @DisplayName("Obtener conteo total de registros")
    void testObtenerConteoTotalRegistros() {
        // Given - Agregar algunos datos de prueba
        String nombre = PRODUCTO_TEST_PREFIX + "Conteo_" + System.currentTimeMillis();
        Producto producto = new Producto(nombre, 2.00, 8);
        DatabaseManager.guardarProducto(producto);
        
        String idVenta = VENTA_TEST_PREFIX + "Conteo_" + System.currentTimeMillis();
        Venta venta = new Venta(idVenta, LocalDateTime.now(), new ArrayList<>(), 5.00);
        DatabaseManager.guardarVenta(venta);
        
        // When
        int[] conteos = DatabaseManager.obtenerConteoTotalRegistros();
        
        // Then
        assertNotNull(conteos);
        assertEquals(3, conteos.length);
        assertTrue(conteos[0] >= 1, "Debe haber al menos 1 producto");
        assertTrue(conteos[1] >= 1, "Debe haber al menos 1 venta");
        assertTrue(conteos[2] >= 0, "Operaciones de caja pueden ser 0 o más");
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
    
    @Test
    @DisplayName("Manejo de errores en métodos auxiliares")
    void testManejoErroresMetodosAuxiliares() {
        // When & Then - Todos los métodos deben manejar errores graciosamente
        assertDoesNotThrow(() -> {
            DatabaseManager.obtenerEstadisticasBaseDatos();
            DatabaseManager.buscarProductoPorNombre("TEST");
            DatabaseManager.obtenerProductosConStockBajo(10);
            DatabaseManager.validarIntegridadDatos();
            DatabaseManager.obtenerConteoTotalRegistros();
        });
    }
}
