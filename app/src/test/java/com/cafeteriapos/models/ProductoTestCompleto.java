package com.cafeteriapos.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas mejorada para el modelo Producto
 * Incluye más casos de prueba y validaciones
 */
@DisplayName("Tests completos para Producto")
class ProductoTestCompleto {
    
    private Producto producto;
    
    @BeforeEach
    void setUp() {
        producto = new Producto("Café Americano", 2.50, 10);
    }
    
    @Test
    @DisplayName("Crear producto con constructor de 3 parámetros")
    void testCrearProductoCompleto() {
        // Given
        String nombre = "Espresso";
        double precio = 3.00;
        int stock = 15;
        
        // When
        Producto nuevoProducto = new Producto(nombre, precio, stock);
        
        // Then
        assertEquals(nombre, nuevoProducto.getNombre());
        assertEquals(precio, nuevoProducto.getPrecio(), 0.01);
        assertEquals(stock, nuevoProducto.getStock());
    }
    
    @Test
    @DisplayName("Crear producto y modificar stock después")
    void testCrearProductoYModificarStock() {
        // Given
        String nombre = "Cappuccino";
        double precio = 3.25;
        int stockInicial = 0;
        int stockFinal = 10;
        
        // When
        Producto nuevoProducto = new Producto(nombre, precio, stockInicial);
        nuevoProducto.setStock(stockFinal);
        
        // Then
        assertEquals(nombre, nuevoProducto.getNombre());
        assertEquals(precio, nuevoProducto.getPrecio(), 0.01);
        assertEquals(stockFinal, nuevoProducto.getStock());
    }
    
    @Test
    @DisplayName("Modificar stock del producto")
    void testModificarStock() {
        // Given
        int nuevoStock = 25;
        
        // When
        producto.setStock(nuevoStock);
        
        // Then
        assertEquals(nuevoStock, producto.getStock());
    }
    
    @Test
    @DisplayName("Modificar precio del producto")
    void testModificarPrecio() {
        // Given
        double nuevoPrecio = 2.75;
        
        // When
        producto.setPrecio(nuevoPrecio);
        
        // Then
        assertEquals(nuevoPrecio, producto.getPrecio(), 0.01);
    }
    
    @Test
    @DisplayName("Modificar nombre del producto")
    void testModificarNombre() {
        // Given
        String nuevoNombre = "Café Colombiano";
        
        // When
        producto.setNombre(nuevoNombre);
        
        // Then
        assertEquals(nuevoNombre, producto.getNombre());
    }
    
    @Test
    @DisplayName("Producto con valores límite - precio muy alto")
    void testProductoPrecioAlto() {
        // Given
        double precioAlto = 999.99;
        
        // When
        producto.setPrecio(precioAlto);
        
        // Then
        assertEquals(precioAlto, producto.getPrecio(), 0.01);
    }
    
    @Test
    @DisplayName("Producto con valores límite - precio cero")
    void testProductoPrecioCero() {
        // Given
        double precioCero = 0.0;
        
        // When
        producto.setPrecio(precioCero);
        
        // Then
        assertEquals(precioCero, producto.getPrecio(), 0.01);
    }
    
    @Test
    @DisplayName("Producto con stock cero")
    void testProductoStockCero() {
        // When
        producto.setStock(0);
        
        // Then
        assertEquals(0, producto.getStock());
    }
    
    @Test
    @DisplayName("Producto con stock alto")
    void testProductoStockAlto() {
        // Given
        int stockAlto = 1000;
        
        // When
        producto.setStock(stockAlto);
        
        // Then
        assertEquals(stockAlto, producto.getStock());
    }
    
    @Test
    @DisplayName("Comparar productos por propiedades")
    void testCompararProductos() {
        // Given
        Producto producto1 = new Producto("Latte", 3.50, 8);
        Producto producto2 = new Producto("Latte", 3.50, 8);
        Producto producto3 = new Producto("Mocha", 4.00, 5);
        
        // Then - Comparar productos idénticos
        assertEquals(producto1.getNombre(), producto2.getNombre());
        assertEquals(producto1.getPrecio(), producto2.getPrecio(), 0.01);
        assertEquals(producto1.getStock(), producto2.getStock());
        
        // Comparar productos diferentes
        assertNotEquals(producto1.getNombre(), producto3.getNombre());
        assertNotEquals(producto1.getPrecio(), producto3.getPrecio(), 0.01);
        assertNotEquals(producto1.getStock(), producto3.getStock());
    }
    
    @Test
    @DisplayName("Producto con nombre vacío")
    void testProductoNombreVacio() {
        // When
        producto.setNombre("");
        
        // Then
        assertEquals("", producto.getNombre());
    }
    
    @Test
    @DisplayName("Producto con nombre nulo")
    void testProductoNombreNulo() {
        // When
        producto.setNombre(null);
        
        // Then
        assertNull(producto.getNombre());
    }
    
    @Test
    @DisplayName("Producto con nombre muy largo")
    void testProductoNombreLargo() {
        // Given
        String nombreLargo = "Este es un nombre de producto extremadamente largo que podría causar problemas en algunos sistemas";
        
        // When
        producto.setNombre(nombreLargo);
        
        // Then
        assertEquals(nombreLargo, producto.getNombre());
    }
    
    @Test
    @DisplayName("Múltiples modificaciones consecutivas")
    void testModificacionesConsecutivas() {
        // When
        producto.setNombre("Nuevo Nombre");
        producto.setPrecio(5.00);
        producto.setStock(50);
        
        // Then
        assertEquals("Nuevo Nombre", producto.getNombre());
        assertEquals(5.00, producto.getPrecio(), 0.01);
        assertEquals(50, producto.getStock());
    }
}
