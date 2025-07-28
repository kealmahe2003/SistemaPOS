package com.cafeteriapos.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class CajaManager {

    public static void abrirCaja() {
        String archivo = "caja_apertura.txt";
        String contenido = "Caja abierta el: " + LocalDateTime.now();

        try (FileWriter writer = new FileWriter(archivo, true)) {
            writer.write(contenido + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}