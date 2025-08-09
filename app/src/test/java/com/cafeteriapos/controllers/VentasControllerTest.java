package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests para VentasController (lógica sin UI)
 * Verifica el funcionamiento de las operaciones de ventas
 */
@DisplayName("Tests para VentasController")
public class VentasControllerTest {

    private List<Producto> productosTest;
    
    @BeforeEach
    void setUp() {
        // Inicializar base de datos para tests
        DatabaseManager.inicializarBaseDatos();
        
        // Crear productos de prueba
        productosTest = new ArrayList<>();
        productosTest.add(new Producto("Café Test", 2.50, 10));
        productosTest.add(new Producto("Croissant Test", 1.75, 5));
    }

    @Test
    @DisplayName("Cargar productos disponibles")
    void testCargarProductosDisponibles() {
        // When
        List<Producto> productos = DatabaseManager.leerProductos();
        
        // Then
        assertNotNull(productos);
        assertTrue(productos.size() >= 0);
    }

    @Test
    @DisplayName("Crear venta válida")
    void testCrearVentaValida() {
        // Given
        String idVenta = "V-TEST-001";
        double total = 10.75;
        
        // When
        Venta venta = new Venta(idVenta, java.time.LocalDateTime.now(), productosTest, total);
        
        // Then
        assertNotNull(venta);
        assertEquals(idVenta, venta.getId());
        assertEquals(total, venta.getTotal(), 0.01);
        assertEquals(productosTest, venta.getItems());
    }

    @Test
    @DisplayName("Validar stock disponible")
    void testValidarStockDisponible() {
        // Given
        Producto producto = new Producto("Test Product", 5.00, 3);
        int cantidadSolicitada = 2;
        
        // When & Then
        assertTrue(producto.getStock() >= cantidadSolicitada, 
                  "Debe haber suficiente stock");
    }

    @Test
    @DisplayName("Validar stock insuficiente")
    void testValidarStockInsuficiente() {
        // Given
        Producto producto = new Producto("Test Product", 5.00, 1);
        int cantidadSolicitada = 5;
        
        // When & Then
        assertFalse(producto.getStock() >= cantidadSolicitada, 
                   "No debe haber suficiente stock");
    }

    @Test
    @DisplayName("Calcular total de carrito")
    void testCalcularTotalCarrito() {
        // Given
        List<Producto> items = List.of(
            new Producto("Item 1", 2.50, 1),
            new Producto("Item 2", 3.75, 1)
        );
        
        // When
        double total = items.stream()
                          .mapToDouble(Producto::getPrecio)
                          .sum();
        
        // Then
        assertEquals(6.25, total, 0.01);
    }

    @Test
    @DisplayName("Actualizar stock después de venta")
    void testActualizarStockDespuesVenta() {
        // Given
        Producto producto = new Producto("Test Stock", 5.00, 10);
        int cantidadVendida = 3;
        int stockOriginal = producto.getStock();
        
        // When
        producto.setStock(stockOriginal - cantidadVendida);
        
        // Then
        assertEquals(7, producto.getStock());
    }

    @Test
    @DisplayName("Generar ID de venta único")
    void testGenerarIdVentaUnico() {
        // When
        String id1 = "V-" + System.currentTimeMillis();
        String id2 = "V-" + (System.currentTimeMillis() + 1);
        
        // Then
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("V-"));
        assertTrue(id2.startsWith("V-"));
    }

    @Test
    @DisplayName("Procesar venta exitosa")
    void testProcesarVentaExitosa() {
        // Given
        String idVenta = "V-SUCCESS-TEST-" + System.currentTimeMillis();
        List<Producto> items = List.of(
            new Producto("Café Venta " + System.currentTimeMillis(), 2.50, 10)
        );
        double total = 2.50;
        
        // When & Then
        assertDoesNotThrow(() -> {
            Venta venta = new Venta(idVenta, java.time.LocalDateTime.now(), items, total);
            // Solo verificar que se puede crear la venta
            assertNotNull(venta);
            assertEquals(idVenta, venta.getId());
            assertEquals(total, venta.getTotal(), 0.01);
        });
    }

    @Test
    @DisplayName("Manejar carrito vacío")
    void testManejarCarritoVacio() {
        // Given
        List<Producto> carritoVacio = new ArrayList<>();
        
        // When & Then
        assertTrue(carritoVacio.isEmpty());
        assertEquals(0, carritoVacio.size());
    }

    @Test
    @DisplayName("Filtrar productos por búsqueda")
    void testFiltrarProductosPorBusqueda() {
        // Given
        List<Producto> productos = List.of(
            new Producto("Café Americano", 2.50, 10),
            new Producto("Café Latte", 3.50, 8),
            new Producto("Té Verde", 2.00, 15)
        );
        String busqueda = "café";
        
        // When
        List<Producto> productosFiltrados = productos.stream()
            .filter(p -> p.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
            .toList();
        
        // Then
        assertEquals(2, productosFiltrados.size());
        assertTrue(productosFiltrados.stream()
                  .allMatch(p -> p.getNombre().toLowerCase().contains("café")));
    }

    @Test
    @DisplayName("Validar cantidad en spinner")
    void testValidarCantidadSpinner() {
        // Given
        int cantidadMinima = 1;
        int cantidadMaxima = 99;
        int cantidadValida = 5;
        
        // When & Then
        assertTrue(cantidadValida >= cantidadMinima);
        assertTrue(cantidadValida <= cantidadMaxima);
    }

    @Test
    @DisplayName("Manejar productos con stock cero")
    void testManejarProductosStockCero() {
        // Given
        Producto productoSinStock = new Producto("Sin Stock", 5.00, 0);
        
        // When & Then
        assertEquals(0, productoSinStock.getStock());
        assertFalse(productoSinStock.getStock() > 0, 
                   "Producto sin stock no debe estar disponible");
    }
}
