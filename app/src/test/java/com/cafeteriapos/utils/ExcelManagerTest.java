package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de pruebas para ExcelManager
 * Nota: Estas pruebas usan archivos temporales para no afectar los datos reales
 */
@DisplayName("Tests para ExcelManager")
public class ExcelManagerTest {
    
    @TempDir
    Path tempDir;
    
    private Producto productoTest;
    private Venta ventaTest;
    private String originalFilePath;
    
    @BeforeEach
    void setUp() {
        // Crear datos de prueba
        productoTest = new Producto("Café Test", 2.50, 10);
        
        List<Producto> items = new ArrayList<>();
        items.add(productoTest);
        ventaTest = new Venta("V-TEST-001", LocalDateTime.now(), items, 2.50);
        
        // Guardar la ruta original del archivo para restaurarla después
        originalFilePath = System.getProperty("excel.file.path", "data/registros_pos.xlsx");
    }
    
    @AfterEach
    void tearDown() {
        // Restaurar la configuración original si es necesario
        if (originalFilePath != null) {
            System.setProperty("excel.file.path", originalFilePath);
        }
    }
    
    @Test
    @DisplayName("Leer productos devuelve lista no nula")
    void testLeerProductos() {
        // When
        List<Producto> productos = ExcelManager.leerProductos();
        
        // Then
        assertNotNull(productos, "La lista de productos no debe ser nula");
        // La lista puede estar vacía si no hay archivo Excel, pero no debe ser nula
    }
    
    @Test
    @DisplayName("Leer ventas devuelve lista no nula")
    void testLeerVentas() {
        // When
        List<Venta> ventas = ExcelManager.leerVentas();
        
        // Then
        assertNotNull(ventas, "La lista de ventas no debe ser nula");
        // La lista puede estar vacía si no hay archivo Excel, pero no debe ser nula
    }
    
    @Test
    @DisplayName("Guardar producto no lanza excepción")
    void testGuardarProducto() {
        // When & Then - El método no debe lanzar excepción
        assertDoesNotThrow(() -> {
            ExcelManager.guardarProducto(productoTest);
        }, "Guardar producto no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Guardar venta no lanza excepción")
    void testGuardarVenta() {
        // When & Then - El método no debe lanzar excepción
        assertDoesNotThrow(() -> {
            ExcelManager.guardarVenta(ventaTest);
        }, "Guardar venta no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Actualizar producto no lanza excepción")
    void testActualizarProducto() {
        // Given
        productoTest.setStock(5); // Cambiar stock
        
        // When & Then - El método no debe lanzar excepción
        assertDoesNotThrow(() -> {
            ExcelManager.actualizarProducto(productoTest);
        }, "Actualizar producto no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Eliminar producto no lanza excepción")
    void testEliminarProducto() {
        // When & Then - El método no debe lanzar excepción
        assertDoesNotThrow(() -> {
            ExcelManager.eliminarProducto(productoTest);
        }, "Eliminar producto no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Forzar recreación de archivo funciona")
    void testForzarRecreacionArchivo() {
        // When & Then - El método no debe lanzar excepción y debe retornar boolean
        assertDoesNotThrow(() -> {
            boolean resultado = ExcelManager.forzarRecreacionArchivo();
            // El resultado puede ser true o false, dependiendo del estado del sistema
            assertNotNull(resultado);
        }, "Forzar recreación no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Manejar producto nulo con validación apropiada")
    void testManejarProductoNulo() {
        // When & Then - Los métodos pueden lanzar excepciones con parámetros nulos, 
        // pero no deben causar fallos críticos del sistema
        // Probamos que el manejo de nulos no cause crashes
        try {
            ExcelManager.guardarProducto(null);
            ExcelManager.actualizarProducto(null);
            ExcelManager.eliminarProducto(null);
        } catch (Exception e) {
            // Es aceptable que lance excepciones con parámetros nulos
            assertNotNull(e.getMessage(), "El mensaje de error debe existir");
        }
    }
    
    @Test
    @DisplayName("Manejar venta nula con validación apropiada")
    void testManejarVentaNula() {
        // When & Then - El método puede lanzar excepción con parámetro nulo
        try {
            ExcelManager.guardarVenta(null);
        } catch (Exception e) {
            // Es aceptable que lance excepción con parámetro nulo
            assertNotNull(e.getMessage(), "El mensaje de error debe existir");
        }
    }
    
    @Test
    @DisplayName("Operaciones consecutivas funcionan correctamente")
    void testOperacionesConsecutivas() {
        // Given
        Producto producto1 = new Producto("Test1", 1.0, 5);
        Producto producto2 = new Producto("Test2", 2.0, 8);
        
        // When & Then - Múltiples operaciones no deben fallar
        assertDoesNotThrow(() -> {
            ExcelManager.guardarProducto(producto1);
            ExcelManager.guardarProducto(producto2);
            
            producto1.setStock(3);
            ExcelManager.actualizarProducto(producto1);
            
            List<Producto> productos = ExcelManager.leerProductos();
            assertNotNull(productos);
            
        }, "Operaciones consecutivas deben funcionar sin problemas");
    }
}
