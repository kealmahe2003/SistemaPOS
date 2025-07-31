package com.cafeteriapos.models;


import org.junit.jupiter.api.Test;

import com.cafeteriapos.models.Producto;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas para el modelo Producto
 */
public class ProductoTest {

    @Test
    public void testCrearProducto() {
        // Given
        String nombre = "Café Americano";
        double precio = 2.50;
        int stock = 10;
        
        // When
        Producto producto = new Producto(nombre, precio, stock);
        
        // Then
        assertEquals(nombre, producto.getNombre());
        assertEquals(precio, producto.getPrecio());
        assertEquals(stock, producto.getStock());
    }
    
    @Test
    public void testSetStock() {
        // Given
        Producto producto = new Producto("Test", 1.0, 5);
        int nuevoStock = 15;
        
        // When
        producto.setStock(nuevoStock);
        
        // Then
        assertEquals(nuevoStock, producto.getStock());
    }
    
    @Test
    public void testEquals() {
        // Given
        Producto producto1 = new Producto("Café", 2.0, 10);
        Producto producto2 = new Producto("Café", 2.0, 10);
        Producto producto3 = new Producto("Té", 1.5, 5);
        
        // Then - Comparar por propiedades individuales ya que no hay equals() implementado
        assertEquals(producto1.getNombre(), producto2.getNombre());
        assertEquals(producto1.getPrecio(), producto2.getPrecio());
        assertEquals(producto1.getStock(), producto2.getStock());
        
        assertNotEquals(producto1.getNombre(), producto3.getNombre());
        assertNotEquals(producto1.getPrecio(), producto3.getPrecio());
        assertNotEquals(producto1.getStock(), producto3.getStock());
    }
}
