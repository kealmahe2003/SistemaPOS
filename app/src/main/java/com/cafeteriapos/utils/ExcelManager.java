package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {
    private static final String FILE_PATH = "data/registros_pos.xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static void guardarVenta(Venta venta) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = getOrCreateSheet(workbook, "Ventas");
            
            // Crear headers si es la primera vez
            if (sheet.getLastRowNum() == 0) {
                crearHeadersVenta(sheet);
            }
            
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(venta.getId());
            row.createCell(1).setCellValue(venta.getFecha().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(venta.getTotal());
            
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al guardar venta", e);
        }
    }

    private static void crearHeadersVenta(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID Venta");
        header.createCell(1).setCellValue("Fecha");
        header.createCell(2).setCellValue("Total");
    }

    public static void guardarProducto(Producto producto) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = getOrCreateSheet(workbook, "Productos");
            
            if (sheet.getLastRowNum() == 0) {
                crearHeadersProducto(sheet);
            }
            
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(producto.getNombre());
            row.createCell(1).setCellValue(producto.getPrecio());
            row.createCell(2).setCellValue(producto.getStock());
            
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al guardar producto", e);
        }
    }

    public static List<Producto> leerProductos() {
        List<Producto> productos = new ArrayList<>();
        
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet("Productos");
            if (sheet == null) return productos;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                productos.add(new Producto(
                    row.getCell(0).getStringCellValue(),
                    row.getCell(1).getNumericCellValue(),
                    (int) row.getCell(2).getNumericCellValue()
                ));
            }
        } catch (IOException e) {
            handleError("Error al leer productos", e);
        }
        return productos;
    }

    private static void crearHeadersProducto(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Nombre");
        header.createCell(1).setCellValue("Precio");
        header.createCell(2).setCellValue("Stock");
    }

    public static void eliminarProducto(Producto producto) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet("Productos");
            if (sheet == null) return;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row.getCell(0).getStringCellValue().equals(producto.getNombre())) {
                    sheet.removeRow(row);
                    break;
                }
            }
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al eliminar producto", e);
        }
    }

    public static void registrarOperacionCaja(String operacion) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = getOrCreateSheet(workbook, "RegistroCaja");
            
            if (sheet.getLastRowNum() == 0) {
                crearHeadersCaja(sheet);
            }
            
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(LocalDateTime.now().format(DATE_FORMATTER));
            row.createCell(1).setCellValue(operacion);
            
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al registrar operación de caja", e);
        }
    }

    private static void crearHeadersCaja(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Fecha/Hora");
        header.createCell(1).setCellValue("Operación");
    }


    private static Workbook getOrCreateWorkbook() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new XSSFWorkbook();
        }
        return WorkbookFactory.create(file);
    }

    private static Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    private static void saveWorkbook(Workbook workbook) throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }

    private static void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        // Logger.error(message, e); // Implementar en producción
    }
}