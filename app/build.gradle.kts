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
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    
    // Apache POI (Excel)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5") {
        exclude(group = "com.github.virtuald", module = "curvesapi") // Evita conflicto de dependencias
    }
    
    // Test (opcional pero recomendado)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.cafeteriapos.Main")
}

// Configuración para JaCoCo (SonarQube)
jacoco {
    toolVersion = "0.8.11" // Versión compatible con SonarQube
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
    
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
    
    sonarqube {
        properties {
            property("sonar.projectKey", "kealmahe2003-SistemaPOS")
            property("sonar.projectName", "SistemaPOS")
            property("sonar.java.coveragePlugin", "jacoco")
            property("sonar.coverage.jacoco.xmlReportPaths", 
                layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath)
        }
    }
}