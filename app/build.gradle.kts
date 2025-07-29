plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id "org.sonarqube" version "4.4.1.3373"  // Plugin de SonarQube
    id "jacoco"  version "0.8.10"  // Plugin de JaCoCo para cobertura de código
}

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