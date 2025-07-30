package com.cafeteriapos.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Maneja el registro de operaciones de caja (apertura, cierre, ventas).
 * Integrado con ExcelManager para persistencia en Excel.
 */
public class CajaManager {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra la apertura de caja con un monto inicial.
     */
    public static void registrarApertura(double montoInicial) {
        String registro = String.format(
            "[APERTURA] Monto inicial: $%.2f - %s",
            montoInicial,
            LocalDateTime.now().format(DATE_FORMATTER)
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    /**
     * Registra el cierre de caja con un monto final.
     */
    public static void registrarCierre(double montoFinal) {
        String registro = String.format(
            "[CIERRE] Monto final: $%.2f - %s",
            montoFinal,
            LocalDateTime.now().format(DATE_FORMATTER)
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    /**
     * Registra una venta en la caja.
     */
    public static void registrarVenta(String idVenta, double monto) {
        String registro = String.format(
            "[VENTA] ID: %s | Monto: $%.2f - %s",
            idVenta,
            monto,
            LocalDateTime.now().format(DATE_FORMATTER)
        );
        ExcelManager.registrarOperacionCaja(registro);
    }

    /**
     * Registra un movimiento especial (retiro/ingreso de efectivo).
     */
    public static void registrarMovimiento(String tipo, double monto, String motivo) {
        String registro = String.format(
            "[%s] Monto: $%.2f | Motivo: %s - %s",
            tipo.toUpperCase(),
            monto,
            motivo,
            LocalDateTime.now().format(DATE_FORMATTER)
        );
        ExcelManager.registrarOperacionCaja(registro);
    }
}