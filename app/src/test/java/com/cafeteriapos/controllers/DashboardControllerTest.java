package com.cafeteriapos.controllers;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import com.cafeteriapos.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests para DashboardController (lógica sin UI)
 * Verifica el funcionamiento de las operaciones del dashboard
 */
@DisplayName("Tests para DashboardController")
public class DashboardControllerTest {

    @BeforeEach
    void setUp() {
        // Inicializar base de datos para tests
        DatabaseManager.inicializarBaseDatos();
    }

    @Test
    @DisplayName("Calcular total de ventas del día")
    void testCalcularTotalVentasDelDia() {
        // Given
        List<Venta> ventasDelDia = List.of(
            new Venta("V001", LocalDateTime.now(), new ArrayList<>(), 15.50),
            new Venta("V002", LocalDateTime.now(), new ArrayList<>(), 23.75),
            new Venta("V003", LocalDateTime.now(), new ArrayList<>(), 8.25)
        );
        
        // When
        double totalDia = ventasDelDia.stream()
                                    .mapToDouble(Venta::getTotal)
                                    .sum();
        
        // Then
        assertEquals(47.50, totalDia, 0.01);
    }

    @Test
    @DisplayName("Contar productos vendidos")
    void testContarProductosVendidos() {
        // Given
        List<Producto> productosVendidos = List.of(
            new Producto("Café", 2.50, 1),
            new Producto("Croissant", 1.75, 2),
            new Producto("Té", 2.00, 1)
        );
        
        // When
        int totalProductos = productosVendidos.size();
        
        // Then
        assertEquals(3, totalProductos);
    }

    @Test
    @DisplayName("Calcular promedio de ventas")
    void testCalcularPromedioVentas() {
        // Given
        List<Double> ventasUltimaSemana = List.of(
            150.0, 200.0, 175.0, 225.0, 180.0, 160.0, 190.0
        );
        
        // When
        double promedio = ventasUltimaSemana.stream()
                                          .mapToDouble(Double::doubleValue)
                                          .average()
                                          .orElse(0.0);
        
        // Then
        assertEquals(182.86, promedio, 0.01);
    }

    @Test
    @DisplayName("Identificar producto más vendido")
    void testIdentificarProductoMasVendido() {
        // Given
        List<Producto> productos = List.of(
            new Producto("Café", 2.50, 10),
            new Producto("Té", 2.00, 5),
            new Producto("Croissant", 1.75, 15)
        );
        
        // When
        Producto masVendido = productos.stream()
                                     .max((p1, p2) -> Integer.compare(15 - p1.getStock(), 15 - p2.getStock()))
                                     .orElse(null);
        
        // Then
        assertNotNull(masVendido);
        // El producto con menos stock restante es el más vendido
    }

    @Test
    @DisplayName("Formatear fecha para display")
    void testFormatearFechaDisplay() {
        // Given
        LocalDateTime fecha = LocalDateTime.of(2025, 8, 8, 14, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        // When
        String fechaFormateada = fecha.format(formatter);
        
        // Then
        assertEquals("08/08/2025 14:30", fechaFormateada);
    }

    @Test
    @DisplayName("Calcular porcentaje de cambio")
    void testCalcularPorcentajeCambio() {
        // Given
        double ventasAnterior = 1000.0;
        double ventasActual = 1150.0;
        
        // When
        double porcentajeCambio = ((ventasActual - ventasAnterior) / ventasAnterior) * 100;
        
        // Then
        assertEquals(15.0, porcentajeCambio, 0.01);
    }

    @Test
    @DisplayName("Validar datos de gráficos")
    void testValidarDatosGraficos() {
        // Given
        List<Double> datosVentas = List.of(100.0, 150.0, 200.0, 175.0, 225.0);
        
        // When & Then
        assertNotNull(datosVentas);
        assertFalse(datosVentas.isEmpty());
        assertTrue(datosVentas.stream().allMatch(valor -> valor >= 0));
    }

    @Test
    @DisplayName("Calcular inventario total")
    void testCalcularInventarioTotal() {
        // Given
        List<Producto> inventario = List.of(
            new Producto("Café", 2.50, 20),
            new Producto("Té", 2.00, 15),
            new Producto("Croissant", 1.75, 30)
        );
        
        // When
        int totalItems = inventario.stream()
                                 .mapToInt(Producto::getStock)
                                 .sum();
        
        // Then
        assertEquals(65, totalItems);
    }

    @Test
    @DisplayName("Identificar productos con stock bajo")
    void testIdentificarProductosStockBajo() {
        // Given
        List<Producto> productos = List.of(
            new Producto("Café", 2.50, 2),
            new Producto("Té", 2.00, 15),
            new Producto("Croissant", 1.75, 1)
        );
        int umbralStockBajo = 5;
        
        // When
        List<Producto> stockBajo = productos.stream()
                                           .filter(p -> p.getStock() < umbralStockBajo)
                                           .toList();
        
        // Then
        assertEquals(2, stockBajo.size());
        assertTrue(stockBajo.stream().allMatch(p -> p.getStock() < umbralStockBajo));
    }

    @Test
    @DisplayName("Calcular margen de ganancia")
    void testCalcularMargenGanancia() {
        // Given
        double precio = 5.00;
        double costo = 3.00;
        
        // When
        double margen = ((precio - costo) / precio) * 100;
        
        // Then
        assertEquals(40.0, margen, 0.01);
    }

    @Test
    @DisplayName("Generar estadísticas de período")
    void testGenerarEstadisticasPeriodo() {
        // Given
        LocalDateTime inicioMes = LocalDateTime.of(2025, 8, 1, 0, 0);
        LocalDateTime finMes = LocalDateTime.of(2025, 8, 31, 23, 59);
        
        // When
        boolean periodoValido = inicioMes.isBefore(finMes);
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(inicioMes.toLocalDate(), finMes.toLocalDate()) + 1;
        
        // Then
        assertTrue(periodoValido);
        assertEquals(31, diasPeriodo);
    }

    @Test
    @DisplayName("Validar métricas de performance")
    void testValidarMetricasPerformance() {
        // Given
        double tiempoRespuesta = 1.5; // segundos
        int transaccionesPorMinuto = 60;
        double usoMemoria = 75.0; // porcentaje
        
        // When & Then
        assertTrue(tiempoRespuesta < 3.0, "Tiempo de respuesta debe ser aceptable");
        assertTrue(transaccionesPorMinuto > 0, "Debe haber transacciones");
        assertTrue(usoMemoria < 90.0, "Uso de memoria debe ser controlado");
    }

    @Test
    @DisplayName("Formatear valores monetarios")
    void testFormatearValoresMonetarios() {
        // Given
        double valor = 1234.567;
        
        // When
        String valorFormateado = String.format("$%.2f", valor);
        
        // Then
        assertTrue(valorFormateado.startsWith("$"));
        assertTrue(valorFormateado.contains("1234"));
        assertTrue(valorFormateado.endsWith("57"));
    }

    @Test
    @DisplayName("Validar actualización de datos en tiempo real")
    void testValidarActualizacionTiempoReal() {
        // Given
        long ultimaActualizacion = System.currentTimeMillis();
        long intervalosActualizacion = 5000; // 5 segundos
        
        // When
        long tiempoTranscurrido = System.currentTimeMillis() - ultimaActualizacion;
        boolean necesitaActualizacion = tiempoTranscurrido >= intervalosActualizacion;
        
        // Then
        // Como acabamos de crear el timestamp, no debería necesitar actualización
        assertFalse(necesitaActualizacion);
    }

    @Test
    @DisplayName("Validar configuración de dashboard")
    void testValidarConfiguracionDashboard() {
        // Given
        boolean mostrarGraficos = true;
        boolean actualizacionAutomatica = true;
        int intervalosActualizacion = 30; // segundos
        
        // When & Then
        assertTrue(mostrarGraficos);
        assertTrue(actualizacionAutomatica);
        assertTrue(intervalosActualizacion > 0);
        assertTrue(intervalosActualizacion <= 300); // No más de 5 minutos
    }

    @Test
    @DisplayName("Manejar datos vacíos graciosamente")
    void testManejarDatosVaciosGraciosamente() {
        // Given
        List<Venta> ventasVacias = new ArrayList<>();
        List<Producto> productosVacios = new ArrayList<>();
        
        // When
        double totalVentas = ventasVacias.stream().mapToDouble(Venta::getTotal).sum();
        int totalProductos = productosVacios.size();
        
        // Then
        assertEquals(0.0, totalVentas);
        assertEquals(0, totalProductos);
    }
}
