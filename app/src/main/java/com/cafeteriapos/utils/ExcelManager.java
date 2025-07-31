package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
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
        try {
            Workbook workbook = getOrCreateWorkbook();
            Sheet sheet = getOrCreateSheet(workbook, "Productos");
            
            if (sheet.getLastRowNum() == 0) {
                crearHeadersProducto(sheet);
            }
            
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(producto.getNombre());
            row.createCell(1).setCellValue(producto.getPrecio());
            row.createCell(2).setCellValue(producto.getStock());
            
            saveWorkbook(workbook);
            workbook.close(); // Cerrar explícitamente después de guardar
        } catch (Exception e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            e.printStackTrace();
            
            // Si falla, intentar recrear el archivo desde cero
            try {
                System.out.println("Intentando recrear archivo Excel...");
                recreateExcelFile();
                
                // Intentar guardar otra vez con archivo limpio
                Workbook newWorkbook = createNewWorkbook();
                Sheet sheet = getOrCreateSheet(newWorkbook, "Productos");
                
                Row row = sheet.createRow(1); // Primera fila después del header
                row.createCell(0).setCellValue(producto.getNombre());
                row.createCell(1).setCellValue(producto.getPrecio());
                row.createCell(2).setCellValue(producto.getStock());
                
                saveWorkbook(newWorkbook);
                newWorkbook.close();
                System.out.println("Producto guardado en archivo recreado");
            } catch (Exception fallbackException) {
                System.err.println("Error crítico al guardar producto: " + fallbackException.getMessage());
                fallbackException.printStackTrace();
            }
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
        if (!file.exists() || file.length() == 0) {
            return createNewWorkbook();
        }
        
        // Intentar leer el archivo existente
        try {
            return WorkbookFactory.create(file);
        } catch (Exception e) {
            // Si hay error al leer el archivo (corrupto, ZLIB, etc.), crear uno nuevo
            System.err.println("Archivo Excel corrupto, creando uno nuevo: " + e.getMessage());
            // Hacer backup del archivo corrupto
            File backupFile = new File(FILE_PATH + ".backup." + System.currentTimeMillis());
            if (file.exists()) {
                file.renameTo(backupFile);
                System.out.println("Archivo corrupto respaldado como: " + backupFile.getName());
            }
            return createNewWorkbook();
        }
    }
    
    private static Workbook createNewWorkbook() throws IOException {
        // Crear un nuevo workbook si el archivo no existe o está vacío
        Workbook newWorkbook = new XSSFWorkbook();
        
        // Crear sheet de Productos con headers
        Sheet productSheet = newWorkbook.createSheet("Productos");
        Row header = productSheet.createRow(0);
        header.createCell(0).setCellValue("Nombre");
        header.createCell(1).setCellValue("Precio");
        header.createCell(2).setCellValue("Stock");
        
        // Crear sheet de Ventas con headers
        Sheet ventasSheet = newWorkbook.createSheet("Ventas");
        Row ventasHeader = ventasSheet.createRow(0);
        ventasHeader.createCell(0).setCellValue("ID");
        ventasHeader.createCell(1).setCellValue("Fecha");
        ventasHeader.createCell(2).setCellValue("Total");
        ventasHeader.createCell(3).setCellValue("Detalle");
        
        // Guardar el archivo inmediatamente
        saveWorkbook(newWorkbook);
        return newWorkbook;
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
        
        // Asegurar que el archivo anterior se elimine completamente
        if (file.exists()) {
            file.delete();
        }
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            fos.flush(); // Asegurar que se escriba completamente
        }
    }

    private static void recreateExcelFile() throws IOException {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            // Hacer backup del archivo corrupto
            File backupFile = new File(FILE_PATH + ".corrupted." + System.currentTimeMillis());
            file.renameTo(backupFile);
            System.out.println("Archivo corrupto respaldado como: " + backupFile.getName());
        }
    }

    private static void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        // Logger.error(message, e); // Implementar en producción
    }
}