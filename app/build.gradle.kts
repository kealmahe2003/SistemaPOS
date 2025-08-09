plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.sonarqube") version "4.4.1.3373"
    id("jacoco")
}

repositories {
    mavenCentral()
}

dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-controls:21.0.2")
    implementation("org.openjfx:javafx-fxml:21.0.2")
    implementation("org.openjfx:javafx-graphics:21.0.2")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    
    // H2 Database (modo PostgreSQL)
    implementation("com.h2database:h2:2.2.224")
    implementation("org.postgresql:postgresql:42.7.1") // Para sintaxis PostgreSQL completa
    
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
    testImplementation("org.testfx:testfx-junit5:4.0.16-alpha")
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

application {
    mainClass.set("com.cafeteriapos.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED"
    )
}

// Configuración mejorada para recursos
sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
            includes.add("**/*")
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    
    processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    
    from(sourceSets.main.get().resources.srcDirs) {
        include("**/*")
        // Mantener la estructura de paquetes exacta
        exclude("**/*.java", "**/*.kt")
    }
    
    // Verificación obligatoria
    doLast {
        val outputDir = destinationDir
        logger.lifecycle("=== VERIFICACIÓN DE RECURSOS ===")
        logger.lifecycle("Recursos copiados a: $outputDir")
        
        val criticalFiles = listOf(
            "com/cafeteriapos/views/VentasView.fxml",
            "com/cafeteriapos/views/LoginView.fxml"
        )
        
        criticalFiles.forEach { path ->
            val file = File(outputDir, path)
            logger.lifecycle("${if (file.exists()) "✓" else "✗"} $path")
            if (!file.exists()) {
                throw GradleException("Archivo crítico no encontrado: $path")
            }
        }
    }
}
    
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy(jacocoTestReport)
        systemProperty("testfx.robot", "glass")
        systemProperty("testfx.headless", "true")
        systemProperty("prism.order", "sw")
        systemProperty("prism.text", "t2k")
    }
    
    jar {
        manifest {
            attributes(
                "Main-Class" to application.mainClass.get(),
                "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
            )
        }
        
        // Incluir todos los recursos manteniendo la estructura
        from(sourceSets.main.get().output)
        
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

jacoco {
    toolVersion = "0.8.11"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "kealmahe2003_SistemaPOS")
        property("sonar.organization", "kealmahe2003")
        property("sonar.projectName", "SistemaPOS")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", 
            layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath)
        property("sonar.junit.reportPaths", 
            layout.buildDirectory.dir("test-results/test").get().asFile.absolutePath)
        property("sonar.sourceEncoding", "UTF-8")
        
        // Configuración específica para tests
        property("sonar.tests", "src/test/java")
        property("sonar.test.inclusions", "**/*Test.java,**/*Tests.java")
        
        // Exclusiones para archivos que no necesitan cobertura
        property("sonar.coverage.exclusions", 
            "**/Main.java," +
            "**/Application.java," +
            "**/*Controller.java," +
            "**/views/**," +
            "**/*.fxml," +
            "**/*.css"
        )
        
        // Configuración adicional para mejorar la detección
        property("sonar.java.source", "21")
        property("sonar.java.target", "21")
        property("sonar.java.binaries", layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath)
        property("sonar.java.test.binaries", layout.buildDirectory.dir("classes/java/test").get().asFile.absolutePath)
    }
}

// Tarea para verificar estructura de recursos
tasks.register("verifyResources") {
    // Agrega esta línea para deshabilitar el caché de configuración para esta tarea
    notCompatibleWithConfigurationCache("Esta tarea accede directamente al filesystem")
    
    doLast {
        val resources = sourceSets.main.get().resources
        logger.lifecycle("Directorios de recursos: ${resources.srcDirs}")
        
        resources.srcDirs.forEach { dir ->
            logger.lifecycle("\nContenido de ${dir}:")
            fileTree(dir).visit {
                logger.lifecycle("- ${relativePath}")
            }
        }
        
        val outputDir = sourceSets.main.get().output.resourcesDir
        logger.lifecycle("\nRecursos en output (${outputDir}):")
        fileTree(outputDir ?: file("build/resources/main")).visit {
            logger.lifecycle("- ${relativePath}")
        }
    }
}
