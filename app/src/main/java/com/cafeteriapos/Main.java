package com.cafeteriapos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.cafeteriapos.utils.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Iniciando Sistema POS con H2 Database en modo PostgreSQL...");
        
        try {
            // Inicializar base de datos H2
            DatabaseManager.inicializarBaseDatos();
            
            // Inicializar productos base si no existen
            DatabaseManager.inicializarProductosBasesSiNoExisten();
            
            logger.info("Sistema POS inicializado correctamente con H2 Database");
            
        } catch (Exception e) {
            logger.error("Error crítico inicializando sistema: {}", e.getMessage());
            throw e;
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cafeteriapos/views/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Inicio de Sesión - Sistema POS");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Shutdown hook para cerrar conexión H2 correctamente
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Cerrando Sistema POS...");
            DatabaseManager.cerrarConexion();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}