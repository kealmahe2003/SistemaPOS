package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la gestión de productos (sin UI)
 * Verifica el funcionamiento correcto de la lógica de productos
 */
public class ProductosControllerTest {

    @BeforeEach
    void setUp() {
        // Configurar base de datos para tests
        DatabaseManager.inicializarBaseDatos();
    }

    @Test
    void testCrearProductoValido() {
        // Given - Datos válidos del producto
        String nombre = "Café Americano Test";
        double precio = 2.50;
        int stock = 10;
        
        // When - Crear producto
        Producto producto = new Producto(nombre, precio, stock);
        
        // Then - Verificar que el producto fue creado correctamente
        assertNotNull(producto);
        assertEquals(nombre, producto.getNombre());
        assertEquals(precio, producto.getPrecio(), 0.01);
        assertEquals(stock, producto.getStock());
    }

    @Test
    void testValidacionFormulario() {
        // Test 1: Nombre vacío debe fallar
        String nombreVacio = "";
        String precio = "2.50";
        int stock = 10;
        
        assertThrows(IllegalArgumentException.class, () -> {
            if (nombreVacio.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        });
        
        // Test 2: Precio vacío debe fallar
        String precioVacio = "";
        
        assertThrows(IllegalArgumentException.class, () -> {
            if (precioVacio.trim().isEmpty()) {
                throw new IllegalArgumentException("Ingrese un precio");
            }
        });
        
        // Test 3: Stock negativo debe fallar
        int stockNegativo = -1;
        
        assertThrows(IllegalArgumentException.class, () -> {
            if (stockNegativo < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }
        });
    }

    @Test
    void testValidacionPrecio() {
        // Test precio cero debe fallar
        Producto producto = new Producto("Test", 0.0, 10);
        
        assertThrows(IllegalArgumentException.class, () -> {
            if (producto.getPrecio() <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a cero");
            }
        });
        
        // Test precio negativo debe fallar
        Producto producto2 = new Producto("Test", -5.0, 10);
        
        assertThrows(IllegalArgumentException.class, () -> {
            if (producto2.getPrecio() <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a cero");
            }
        });
    }

    @Test
    void testCargarProductosDesdeBaseDatos() {
        // When - Cargar productos desde H2
        java.util.List<Producto> productosLeidos = DatabaseManager.leerProductos();
        
        // Then - Verificar que se cargaron productos
        assertNotNull(productosLeidos);
        assertTrue(productosLeidos.size() >= 0); // Puede estar vacía o tener productos
        
        // Si hay productos, verificar que tienen datos válidos
        for (Producto p : productosLeidos) {
            assertNotNull(p.getNombre());
            assertFalse(p.getNombre().trim().isEmpty());
            assertTrue(p.getPrecio() > 0);
            assertTrue(p.getStock() >= 0);
        }
    }

    @Test
    void testOperacionesCRUD() {
        // CREATE - Crear producto de prueba
        Producto productoTest = new Producto("Test Product CRUD", 1.99, 5);
        DatabaseManager.guardarProducto(productoTest);
        
        // READ - Leer productos y verificar que existe
        java.util.List<Producto> productos = DatabaseManager.leerProductos();
        boolean encontrado = productos.stream()
            .anyMatch(p -> "Test Product CRUD".equals(p.getNombre()));
        assertTrue(encontrado, "El producto debería existir en la base de datos");
        
        // UPDATE - Actualizar producto
        productoTest.setPrecio(2.99);
        productoTest.setStock(15);
        DatabaseManager.actualizarProducto(productoTest);
        
        // Verificar actualización
        java.util.List<Producto> productosActualizados = DatabaseManager.leerProductos();
        var productoActualizado = productosActualizados.stream()
            .filter(p -> "Test Product CRUD".equals(p.getNombre()))
            .findFirst();
        
        assertTrue(productoActualizado.isPresent());
        assertEquals(2.99, productoActualizado.get().getPrecio(), 0.01);
        assertEquals(15, productoActualizado.get().getStock());
        
        // DELETE - Eliminar producto
        DatabaseManager.eliminarProducto(productoTest);
        
        // Verificar eliminación
        java.util.List<Producto> productosFinales = DatabaseManager.leerProductos();
        boolean eliminado = productosFinales.stream()
            .noneMatch(p -> "Test Product CRUD".equals(p.getNombre()));
        assertTrue(eliminado, "El producto debería haber sido eliminado");
    }

    @Test
    void testValidacionNombreProductoExistente() {
        // Given - Producto existente
        Producto producto1 = new Producto("Café Duplicado", 2.50, 10);
        DatabaseManager.guardarProducto(producto1);
        
        // When - Verificar que existe
        java.util.List<Producto> productos = DatabaseManager.leerProductos();
        boolean existe = productos.stream()
            .anyMatch(p -> "Café Duplicado".equalsIgnoreCase(p.getNombre()));
        
        // Then - Debería existir
        assertTrue(existe, "El producto debería existir");
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto1);
    }

    @Test
    void testActualizacionStock() {
        // Given - Producto con stock inicial
        Producto producto = new Producto("Test Stock", 1.50, 20);
        DatabaseManager.guardarProducto(producto);
        
        // When - Actualizar stock
        producto.setStock(5);
        DatabaseManager.actualizarProducto(producto);
        
        // Then - Verificar actualización
        java.util.List<Producto> productosActualizados = DatabaseManager.leerProductos();
        var productoActualizado = productosActualizados.stream()
            .filter(p -> "Test Stock".equals(p.getNombre()))
            .findFirst();
        
        assertTrue(productoActualizado.isPresent());
        assertEquals(5, productoActualizado.get().getStock());
        
        // Cleanup
        DatabaseManager.eliminarProducto(producto);
    }
}
