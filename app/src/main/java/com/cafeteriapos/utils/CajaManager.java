package com.cafeteriapos.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CajaManager {
    // Configuración centralizada
    private static final String DIRECTORIO_DATA = "data";
    private static final String ARCHIVO_REGISTRO = "registro_caja.log";
    private static final DateTimeFormatter FORMATO_FECHA = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra un evento en el archivo de log de la caja.
     * @param tipoEvento Ej: "APERTURA", "CIERRE", "VENTA"
     * @param mensaje Descripción detallada
     */
    public static void registrarEvento(String tipoEvento, String mensaje) {
        try {
            // Asegurar que el directorio existe
            Path directorio = Paths.get(DIRECTORIO_DATA);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // Formato del registro: [FECHA] TIPO_EVENTO - MENSAJE
            String registro = String.format("[%s] %s - %s%n",
                LocalDateTime.now().format(FORMATO_FECHA),
                tipoEvento,
                mensaje
            );

            // Escribir en archivo (append)
            Path archivo = directorio.resolve(ARCHIVO_REGISTRO);
            Files.writeString(
                archivo,
                registro,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            
        } catch (IOException e) {
            System.err.println("Error al registrar evento de caja: " + e.getMessage());
            // En producción, usar un logger (Log4j/SLF4J)
        }
    }

    // Métodos específicos para acciones comunes
    public static void registrarApertura(double montoInicial) {
        registrarEvento("APERTURA", String.format("Monto inicial: $%.2f", montoInicial));
    }

    public static void registrarCierre(double montoFinal) {
        registrarEvento("CIERRE", String.format("Monto final: $%.2f", montoFinal));
    }

    public static void registrarVenta(String idVenta, double monto) {
        registrarEvento("VENTA", String.format("ID: %s | Monto: $%.2f", idVenta, monto));
    }
}