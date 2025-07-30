package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelManager {
    // Configuración
    private static final String RUTA_ARCHIVO = "data/registros_pos.xlsx";
    private static final String HOJA_VENTAS = "Ventas";
    private static final String HOJA_PRODUCTOS = "Productos";

    /**
     * Registra una venta en el archivo Excel.
     */
    public static void guardarVenta(Venta venta) {
        try (Workbook workbook = cargarWorkbook()) {
            Sheet hoja = workbook.getSheet(HOJA_VENTAS);
            if (hoja == null) hoja = workbook.createSheet(HOJA_VENTAS);

            // Crear fila
            Row fila = hoja.createRow(hoja.getLastRowNum() + 1);
            
            // Escribir datos
            fila.createCell(0).setCellValue(venta.getId());
            fila.createCell(1).setCellValue(venta.getFecha().toString());
            fila.createCell(2).setCellValue(venta.getTotal());
            
            guardarWorkbook(workbook);
        } catch (IOException e) {
            manejarError("Error al guardar venta", e);
        }
    }

    /**
     * Guarda un producto en el archivo Excel.
     */
    public static void guardarProducto(Producto producto) {
        try (Workbook workbook = cargarWorkbook()) {
            Sheet hoja = workbook.getSheet(HOJA_PRODUCTOS);
            if (hoja == null) {
                hoja = workbook.createSheet(HOJA_PRODUCTOS);
                // Crear headers si es nueva hoja
                Row header = hoja.createRow(0);
                header.createCell(0).setCellValue("Nombre");
                header.createCell(1).setCellValue("Precio");
                header.createCell(2).setCellValue("Stock");
            }

            Row fila = hoja.createRow(hoja.getLastRowNum() + 1);
            fila.createCell(0).setCellValue(producto.getNombre());
            fila.createCell(1).setCellValue(producto.getPrecio());
            fila.createCell(2).setCellValue(producto.getStock());

            guardarWorkbook(workbook);
        } catch (IOException e) {
            manejarError("Error al guardar producto", e);
        }
    }

    /**
     * Lee todos los productos desde Excel.
     */
    public static List<Producto> leerProductos() {
        List<Producto> productos = new ArrayList<>();
        try (Workbook workbook = cargarWorkbook()) {
            Sheet hoja = workbook.getSheet(HOJA_PRODUCTOS);
            if (hoja == null) return productos;

            for (Row fila : hoja) {
                if (fila.getRowNum() == 0) continue; // Saltar header
                
                String nombre = fila.getCell(0).getStringCellValue();
                double precio = fila.getCell(1).getNumericCellValue();
                int stock = (int) fila.getCell(2).getNumericCellValue();
                
                productos.add(new Producto(nombre, precio, stock));
            }
        } catch (IOException e) {
            manejarError("Error al leer productos", e);
        }
        return productos;
    }

    // -- Métodos privados de apoyo -- //
    private static Workbook cargarWorkbook() throws IOException {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) {
            return new XSSFWorkbook(); // Crear nuevo si no existe
        }
        return WorkbookFactory.create(archivo);
    }

    private static void guardarWorkbook(Workbook workbook) throws IOException {
        File archivo = new File(RUTA_ARCHIVO);
        archivo.getParentFile().mkdirs(); // Crear directorio si no existe
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            workbook.write(fos);
        }
    }

    private static void manejarError(String mensaje, Exception e) {
        System.err.println(mensaje + ": " + e.getMessage());
        // En producción, usar Logger:
        // Logger.error(mensaje, e);
    }
}