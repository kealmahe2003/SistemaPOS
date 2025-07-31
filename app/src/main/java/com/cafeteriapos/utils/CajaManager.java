package com.cafeteriapos.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CajaManager {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void registrarApertura(double montoInicial) {
        String registro = String.format(
            "APERTURA - Monto inicial: $%.2f", 
            montoInicial
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    public static void registrarCierre(double montoFinal) {
        String registro = String.format(
            "CIERRE - Monto final: $%.2f", 
            montoFinal
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    public static void registrarVenta(String idVenta, double monto) {
        String registro = String.format(
            "VENTA - ID: %s | Monto: $%.2f", 
            idVenta, 
            monto
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    public static void registrarMovimiento(String tipo, double monto, String motivo) {
        String registro = String.format(
            "%s - Monto: $%.2f | Motivo: %s", 
            tipo.toUpperCase(), 
            monto, 
            motivo
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    public static void registrarError(String operacion, String mensajeError) {
        String registro = String.format(
            "ERROR - Operación: %s | Error: %s", 
            operacion, 
            mensajeError
        );
        ExcelManager.registrarOperacionCaja(registro);
    }
}