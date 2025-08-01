package com.cafeteriapos.utils;

import com.cafeteriapos.models.Producto;
import com.cafeteriapos.models.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {
    private static final Logger logger = LoggerFactory.getLogger(ExcelManager.class);
    private static final String FILE_PATH = "data/registros_pos.xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Constantes para nombres de hojas
    private static final String PRODUCTOS_SHEET = "Productos";
    private static final String VENTAS_SHEET = "Ventas";
    private static final String REGISTRO_CAJA_SHEET = "RegistroCaja";
    
    // Constantes para mensajes de log
    private static final String BACKUP_MESSAGE = "Archivo corrupto respaldado como: ";
    private static final String RECREATED_MESSAGE = "Archivo Excel recreado exitosamente en: ";


    public static void guardarVenta(Venta venta) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = getOrCreateSheet(workbook, VENTAS_SHEET);
            
            // Crear headers si es la primera vez
            if (sheet.getLastRowNum() == 0) {
                crearHeadersVenta(sheet);
            }
            
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(venta.getId());
            row.createCell(1).setCellValue(venta.getFechaHora().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(venta.getTotal());
            
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al guardar venta", e);
        }
    }

    private static void crearHeadersVenta(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID Venta");
        header.createCell(1).setCellValue("Fecha/Hora");
        header.createCell(2).setCellValue("Total");
    }

    public static void guardarProducto(Producto producto) {
        try {
            Workbook workbook = getOrCreateWorkbook();
            Sheet sheet = getOrCreateSheet(workbook, PRODUCTOS_SHEET);
            
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
            logger.error("Error al guardar producto: {}", e.getMessage());
            logger.debug("Stack trace completo:", e);
            
            // Si falla, intentar recrear el archivo desde cero
            try {
                logger.info("Intentando recrear archivo Excel...");
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
                logger.info("Producto guardado en archivo recreado");
            } catch (Exception fallbackException) {
                logger.error("Error crítico al guardar producto: {}", fallbackException.getMessage());
                logger.debug("Stack trace crítico:", fallbackException);
            }
        }
    }

    public static List<Producto> leerProductos() {
        List<Producto> productos = new ArrayList<>();
        
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet("Productos");
            if (sheet == null || sheet.getLastRowNum() == 0) {
                // No hay datos, retornar lista vacía
                logger.info("No se encontraron productos en Excel. Retornando lista vacía.");
                return productos;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // Saltar filas vacías
                
                try {
                    // Verificar que las celdas existan y tengan contenido
                    Cell nombreCell = row.getCell(0);
                    Cell precioCell = row.getCell(1);
                    Cell stockCell = row.getCell(2);
                    
                    if (nombreCell == null || precioCell == null || stockCell == null) {
                        logger.debug("Fila {} tiene celdas vacías, saltando...", i);
                        continue;
                    }
                    
                    String nombre = nombreCell.getStringCellValue();
                    double precio = precioCell.getNumericCellValue();
                    int stock = (int) stockCell.getNumericCellValue();
                    
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        productos.add(new Producto(nombre.trim(), precio, stock));
                    }
                    
                } catch (Exception rowException) {
                    logger.warn("Error procesando fila {}: {}", i, rowException.getMessage());
                    // Continuar con la siguiente fila en lugar de fallar completamente
                }
            }
        } catch (Exception e) {
            logger.error("Error crítico al leer productos: {}", e.getMessage());
            logger.info("Intentando recuperar desde archivo backup o crear archivo limpio...");
            
            // Intentar recuperación completa
            try {
                recuperarArchivoExcel();
                // Intentar leer otra vez después de la recuperación
                return leerProductosSeguro();
            } catch (Exception recoveryException) {
                logger.error("No se pudo recuperar el archivo Excel: {}", recoveryException.getMessage());
                logger.info("Retornando lista vacía para permitir que la aplicación continúe.");
            }
        }
        
        logger.info("Productos cargados exitosamente: {}", productos.size());
        return productos;
    }
    
    /**
     * Lee todas las ventas desde el archivo Excel
     * @return Lista de ventas registradas
     */
    public static List<Venta> leerVentas() {
        List<Venta> ventas = new ArrayList<>();
        
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet(VENTAS_SHEET);
            if (sheet == null || sheet.getLastRowNum() == 0) {
                logger.info("No se encontraron ventas en Excel. Retornando lista vacía.");
                return ventas;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Cell idCell = row.getCell(0);
                    Cell fechaCell = row.getCell(1);
                    Cell totalCell = row.getCell(2);
                    
                    if (idCell == null || fechaCell == null || totalCell == null) {
                        logger.debug("Fila {} de ventas tiene celdas vacías, saltando...", i);
                        continue;
                    }
                    
                    String id = idCell.getStringCellValue();
                    String fechaStr = fechaCell.getStringCellValue();
                    double total = totalCell.getNumericCellValue();
                    
                    // Parsear fecha
                    LocalDateTime fecha = LocalDateTime.parse(fechaStr, DATE_FORMATTER);
                    
                    // Crear venta (sin items por ahora, solo datos básicos)
                    Venta venta = new Venta(id, fecha, new ArrayList<>(), total);
                    ventas.add(venta);
                    
                } catch (Exception rowException) {
                    logger.warn("Error procesando fila {} de ventas: {}", i, rowException.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error al leer ventas: {}", e.getMessage());
            logger.info("Retornando lista vacía de ventas.");
        }
        
        logger.info("Ventas cargadas exitosamente: {}", ventas.size());
        return ventas;
    }
    
    /**
     * Método seguro para leer productos después de recuperación
     */
    private static List<Producto> leerProductosSeguro() {
        List<Producto> productos = new ArrayList<>();
        try (Workbook workbook = createNewWorkbook()) {
            // En este punto tenemos un archivo limpio, retornamos lista vacía
            logger.info("Archivo Excel recuperado. Lista de productos vacía - listo para agregar nuevos productos.");
        } catch (Exception e) {
            logger.error("Error incluso con archivo limpio: {}", e.getMessage());
        }
        return productos;
    }
    
    /**
     * Método para recuperar archivo Excel corrupto
     */
    private static void recuperarArchivoExcel() throws IOException {
        File file = new File(FILE_PATH);
        
        // Crear backup del archivo corrupto
        if (file.exists()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupFile = new File(FILE_PATH + ".corrupted." + timestamp);
            if (file.renameTo(backupFile)) {
                logger.info("Archivo corrupto respaldado como: {}", backupFile.getName());
            }
        }
        
        // Crear archivo completamente nuevo
        try (Workbook newWorkbook = createNewWorkbook()) {
            // El archivo se guarda automáticamente en createNewWorkbook()
            logger.info("Archivo Excel recreado exitosamente en: {}", FILE_PATH);
        }
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
                if (row != null && row.getCell(0) != null && 
                    row.getCell(0).getStringCellValue().equals(producto.getNombre())) {
                    sheet.removeRow(row);
                    break;
                }
            }
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al eliminar producto", e);
        }
    }

    public static void actualizarProducto(Producto producto) {
        try (Workbook workbook = getOrCreateWorkbook()) {
            Sheet sheet = workbook.getSheet("Productos");
            if (sheet == null) {
                // Si no existe la hoja, crear y agregar el producto
                guardarProducto(producto);
                return;
            }

            boolean encontrado = false;
            // Buscar el producto existente y actualizarlo
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null && 
                    row.getCell(0).getStringCellValue().equals(producto.getNombre())) {
                    // Actualizar las celdas existentes
                    row.getCell(1).setCellValue(producto.getPrecio());
                    row.getCell(2).setCellValue(producto.getStock());
                    encontrado = true;
                    break;
                }
            }
            
            // Si no se encontró, agregar como nuevo producto
            if (!encontrado) {
                Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
                newRow.createCell(0).setCellValue(producto.getNombre());
                newRow.createCell(1).setCellValue(producto.getPrecio());
                newRow.createCell(2).setCellValue(producto.getStock());
            }
            
            saveWorkbook(workbook);
        } catch (IOException e) {
            handleError("Error al actualizar producto", e);
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
            logger.warn("Archivo Excel corrupto, creando uno nuevo: {}", e.getMessage());
            // Hacer backup del archivo corrupto
            File backupFile = new File(FILE_PATH + ".backup." + System.currentTimeMillis());
            if (file.exists()) {
                file.renameTo(backupFile);
                logger.info("Archivo corrupto respaldado como: {}", backupFile.getName());
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
            logger.info("Archivo corrupto respaldado como: {}", backupFile.getName());
        }
    }

    /**
     * Método para forzar la recreación completa del archivo Excel
     * Útil cuando el archivo está completamente corrupto
     */
    public static boolean forzarRecreacionArchivo() {
        try {
            File file = new File(FILE_PATH);
            
            // Hacer backup si existe
            if (file.exists()) {
                String timestamp = String.valueOf(System.currentTimeMillis());
                File backupFile = new File(FILE_PATH + ".backup." + timestamp);
                if (file.renameTo(backupFile)) {
                    logger.info("Archivo respaldado como: {}", backupFile.getName());
                }
            }
            
            // Crear archivo completamente nuevo
            try (Workbook newWorkbook = createNewWorkbook()) {
                logger.info("Archivo Excel recreado exitosamente en: {}", FILE_PATH);
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Error al forzar recreación del archivo: {}", e.getMessage());
            return false;
        }
    }

    private static void handleError(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage());
        logger.debug("Stack trace completo:", e);
    }
}