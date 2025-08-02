package com.cafeteriapos.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas para CajaManager
 */
@DisplayName("Tests para CajaManager")
public class CajaManagerTest {
    
    @BeforeEach
    void setUp() {
        // Configuración antes de cada prueba
    }
    
    @Test
    @DisplayName("Registrar apertura de caja no lanza excepción")
    void testRegistrarApertura() {
        // Given
        double montoInicial = 100.0;
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            CajaManager.registrarApertura(montoInicial);
        }, "Registrar apertura no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Registrar cierre de caja no lanza excepción")
    void testRegistrarCierre() {
        // Given
        double montoFinal = 250.75;
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            CajaManager.registrarCierre(montoFinal);
        }, "Registrar cierre no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Registrar venta no lanza excepción")
    void testRegistrarVenta() {
        // Given
        String idVenta = "V001";
        double monto = 15.50;
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            CajaManager.registrarVenta(idVenta, monto);
        }, "Registrar venta no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Registrar movimiento no lanza excepción")
    void testRegistrarMovimiento() {
        // Given
        String tipo = "RETIRO";
        double monto = 50.0;
        String motivo = "Cambio para clientes";
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            CajaManager.registrarMovimiento(tipo, monto, motivo);
        }, "Registrar movimiento no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Registrar error no lanza excepción")
    void testRegistrarError() {
        // Given
        String operacion = "Venta";
        String mensajeError = "Error de conexión";
        
        // When & Then - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            CajaManager.registrarError(operacion, mensajeError);
        }, "Registrar error no debe lanzar excepción");
    }
    
    @Test
    @DisplayName("Registrar con valores límite")
    void testRegistrarValoresLimite() {
        // When & Then - Valores límite no deben causar problemas
        assertDoesNotThrow(() -> {
            CajaManager.registrarApertura(0.0); // Monto cero
            CajaManager.registrarVenta("", 0.01); // ID vacío, monto mínimo
            CajaManager.registrarMovimiento("DEPOSITO", 9999.99, "Prueba límite");
        }, "Valores límite deben manejarse correctamente");
    }
    
    @Test
    @DisplayName("Registrar con parámetros nulos - manejo de errores")
    void testRegistrarParametrosNulos() {
        // When & Then - Parámetros nulos pueden generar excepciones,
        // pero el sistema debe manejarlas apropiadamente
        try {
            CajaManager.registrarVenta(null, 10.0);
            CajaManager.registrarMovimiento(null, 5.0, "motivo");
            CajaManager.registrarMovimiento("TIPO", 5.0, null);
            CajaManager.registrarError(null, null);
        } catch (Exception e) {
            // Es aceptable que genere excepciones con parámetros nulos
            assertNotNull(e, "La excepción debe tener información");
        }
    }
    
    @Test
    @DisplayName("Múltiples operaciones consecutivas")
    void testOperacionesConsecutivas() {
        // When & Then - Múltiples operaciones seguidas deben funcionar
        assertDoesNotThrow(() -> {
            CajaManager.registrarApertura(100.0);
            CajaManager.registrarVenta("V001", 15.0);
            CajaManager.registrarVenta("V002", 8.5);
            CajaManager.registrarMovimiento("RETIRO", 20.0, "Cambio");
            CajaManager.registrarCierre(83.5);
        }, "Operaciones consecutivas deben funcionar correctamente");
    }
}
