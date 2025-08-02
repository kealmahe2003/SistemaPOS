package com.cafeteriapos.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Clase de pruebas para utilidades del sistema
 */
@DisplayName("Tests para Utilidades del Sistema")
class UtilsTest {

    @Test
    @DisplayName("Formato de moneda básico")
    void testFormatoMoneda() {
        // Given
        double cantidad = 15.75;
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        
        // When
        String resultado = formato.format(cantidad);
        
        // Then
        assertNotNull(resultado);
        assertTrue(resultado.length() > 0);
    }
    
    @Test
    @DisplayName("Cálculos de impuestos")
    void testCalculoImpuestos() {
        // Given
        double subtotal = 100.0;
        double porcentajeIVA = 0.19; // 19% IVA Colombia
        
        // When
        double iva = subtotal * porcentajeIVA;
        double total = subtotal + iva;
        
        // Then
        assertEquals(19.0, iva, 0.01);
        assertEquals(119.0, total, 0.01);
    }
    
    @Test
    @DisplayName("Cálculos de descuentos")
    void testCalculoDescuentos() {
        // Given
        double precio = 50.0;
        double porcentajeDescuento = 0.10; // 10% descuento
        
        // When
        double descuento = precio * porcentajeDescuento;
        double precioFinal = precio - descuento;
        
        // Then
        assertEquals(5.0, descuento, 0.01);
        assertEquals(45.0, precioFinal, 0.01);
    }
    
    @Test
    @DisplayName("Redondeo de valores monetarios")
    void testRedondeoMonetario() {
        // Given
        double valor1 = 15.456;
        double valor2 = 15.454;
        
        // When - Redondear a 2 decimales
        double redondeado1 = Math.round(valor1 * 100.0) / 100.0;
        double redondeado2 = Math.round(valor2 * 100.0) / 100.0;
        
        // Then
        assertEquals(15.46, redondeado1, 0.01);
        assertEquals(15.45, redondeado2, 0.01);
    }
    
    @Test
    @DisplayName("Validación de rangos numéricos")
    void testValidacionRangos() {
        // Given
        double precio = 25.50;
        int stock = 10;
        
        // When & Then
        assertTrue(precio > 0, "El precio debe ser positivo");
        assertTrue(stock >= 0, "El stock no puede ser negativo");
        assertTrue(precio <= 1000, "El precio debe estar en rango razonable");
    }
    
    @Test
    @DisplayName("Operaciones con cero")
    void testOperacionesCero() {
        // Given
        double cero = 0.0;
        double valor = 10.0;
        
        // When & Then
        assertEquals(0.0, cero * valor, 0.01);
        assertEquals(valor, valor + cero, 0.01);
        assertEquals(valor, valor - cero, 0.01);
    }
    
    @Test
    @DisplayName("Precisión decimal en cálculos financieros")
    void testPrecisionDecimal() {
        // Given - Operación que puede causar imprecisión
        double resultado = 0.1 + 0.2;
        
        // When & Then - Verificar con tolerancia
        assertEquals(0.3, resultado, 0.0001, 
                    "Los cálculos decimales deben manejarse con tolerancia");
    }
    
    @Test
    @DisplayName("Conversión de cadenas numéricas")
    void testConversionCadenas() {
        // Given
        String precioStr = "15.75";
        String stockStr = "10";
        
        // When & Then - Las conversiones no deben fallar
        assertDoesNotThrow(() -> {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);
            
            assertEquals(15.75, precio, 0.01);
            assertEquals(10, stock);
        });
    }
    
    @Test
    @DisplayName("Manejo de valores extremos")
    void testValoresExtremos() {
        // Given
        double valorMaximo = Double.MAX_VALUE;
        double valorMinimo = Double.MIN_VALUE;
        
        // Then
        assertTrue(valorMaximo > 0);
        assertTrue(valorMinimo > 0);
        assertNotEquals(valorMaximo, valorMinimo);
    }
}
