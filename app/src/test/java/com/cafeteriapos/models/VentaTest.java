package com.cafeteriapos.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de pruebas para el modelo Venta
 */
@DisplayName("Tests para Venta")
public class VentaTest {
    
    private Venta venta;
    private List<Producto> productos;
    private LocalDateTime fechaHora;
    
    @BeforeEach
    void setUp() {
        fechaHora = LocalDateTime.of(2025, 8, 2, 10, 30);
        productos = new ArrayList<>();
        productos.add(new Producto("Café Americano", 2.50, 5));
        productos.add(new Producto("Croissant", 1.75, 3));
        
        venta = new Venta("V001", fechaHora, productos, 15.25);
    }
    
    @Test
    @DisplayName("Crear venta con todos los parámetros")
    void testCrearVentaCompleta() {
        // Then
        assertEquals("V001", venta.getId());
        assertEquals(fechaHora, venta.getFechaHora());
        assertEquals(productos, venta.getItems());
        assertEquals(15.25, venta.getTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Crear venta con constructor vacío")
    void testCrearVentaVacia() {
        // When
        Venta nuevaVenta = new Venta();
        
        // Then
        assertEquals("", nuevaVenta.getId());
        assertNotNull(nuevaVenta.getFechaHora());
        assertNotNull(nuevaVenta.getItems());
        assertTrue(nuevaVenta.getItems().isEmpty());
        assertEquals(0.0, nuevaVenta.getTotal());
    }
    
    @Test
    @DisplayName("Obtener fecha desde LocalDateTime")
    void testGetFecha() {
        // When
        LocalDate fecha = venta.getFecha();
        
        // Then
        assertEquals(LocalDate.of(2025, 8, 2), fecha);
    }
    
    @Test
    @DisplayName("Establecer fecha convierte a LocalDateTime")
    void testSetFecha() {
        // Given
        LocalDate nuevaFecha = LocalDate.of(2025, 8, 3);
        
        // When
        venta.setFecha(nuevaFecha);
        
        // Then
        assertEquals(nuevaFecha, venta.getFecha());
        assertEquals(nuevaFecha.atStartOfDay(), venta.getFechaHora());
    }
    
    @Test
    @DisplayName("Modificar propiedades de la venta")
    void testModificarPropiedades() {
        // Given
        LocalDateTime nuevaFecha = LocalDateTime.of(2025, 8, 3, 15, 45);
        List<Producto> nuevosProductos = new ArrayList<>();
        nuevosProductos.add(new Producto("Té Verde", 2.00, 2));
        
        // When
        venta.setId("V999");
        venta.setFechaHora(nuevaFecha);
        venta.setItems(nuevosProductos);
        venta.setTotal(8.50);
        
        // Then
        assertEquals("V999", venta.getId());
        assertEquals(nuevaFecha, venta.getFechaHora());
        assertEquals(nuevosProductos, venta.getItems());
        assertEquals(8.50, venta.getTotal(), 0.01);
    }
    
    @Test
    @DisplayName("Venta con lista vacía de productos")
    void testVentaConListaVacia() {
        // Given
        List<Producto> listaVacia = new ArrayList<>();
        
        // When
        Venta ventaVacia = new Venta("V003", fechaHora, listaVacia, 0.0);
        
        // Then
        assertEquals("V003", ventaVacia.getId());
        assertTrue(ventaVacia.getItems().isEmpty());
        assertEquals(0.0, ventaVacia.getTotal());
    }
    
    @Test
    @DisplayName("Venta con valores límite")
    void testVentaValoresLimite() {
        // Given
        List<Producto> productosCaros = new ArrayList<>();
        productosCaros.add(new Producto("Producto Premium", 999.99, 1));
        
        // When
        Venta ventaCara = new Venta("V004", fechaHora, productosCaros, 999.99);
        
        // Then
        assertEquals(999.99, ventaCara.getTotal(), 0.01);
        assertEquals(1, ventaCara.getItems().size());
    }
}
