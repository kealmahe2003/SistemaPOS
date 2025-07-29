plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.sonarqube") version "4.4.1.3373"  // Sintaxis correcta para Kotlin DSL
    id("jacoco")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.cafeteriapos.Main")
}

// Configuración adicional para JaCoCo (necesaria para SonarQube)
jacoco {
    toolVersion = "0.8.10" // Asegúrate de que coincida con la versión del plugin
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // Ejecuta jacocoTestReport después de test
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Asegura que test se ejecute primero
    reports {
        xml.required.set(true) // SonarQube necesita el reporte en XML
        html.required.set(true) // Opcional: Reporte HTML legible
    }
}