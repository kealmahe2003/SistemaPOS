# 🏪 Sistema POS para Cafetería ☕

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)](https://openjfx.io/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14.3-green.svg)](https://gradle.org/)
[![H2 Database](https://img.shields.io/badge/H2-2.2.224-red.svg)](https://www.h2database.com/)

Sistema de Punto de Venta profesional desarrollado en JavaFX para cafeterías pequeñas y medianas. Incluye gestión completa de productos, ventas, inventario y dashboard de estadísticas en tiempo real.

## ✨ **Características Principales**

- 🔐 **Sistema de login** con autenticación segura
- 📦 **Gestión de productos** con CRUD completo y alertas de stock
- 🛒 **Sistema de ventas** con carrito interactivo y búsqueda en tiempo real
- 📊 **Dashboard profesional** con 6 métricas clave y gráficos interactivos
- ⚡ **Alto rendimiento** con caché inteligente y actualización automática
- 💾 **Base de datos H2** con transacciones ACID y modo PostgreSQL

## 🏗️ **Tecnologías**

- **Frontend**: JavaFX 21.0.2 + FXML + CSS profesional
- **Backend**: Java 21 con patrón MVC
- **Base de Datos**: H2 Database v2.2.224 (modo PostgreSQL)
- **Build**: Gradle 8.14.3 multi-módulo
- **Testing**: JUnit 5 + JaCoCo

## 🚀 **Instalación**

```bash
# Clonar repositorio
git clone https://github.com/kealmahe2003/SistemaPOS.git
cd SistemaPOS

# Ejecutar aplicación
./gradlew run

# Ejecutar tests
./gradlew test
```

## 🎯 **Uso**

- **Usuario**: `admin`
- **Contraseña**: `admin123`

1. Login con credenciales
2. Gestionar productos desde el menú lateral
3. Realizar ventas con carrito interactivo
4. Monitorear estadísticas en dashboard
5. Exportar reportes

## 📊 **Dashboard Features**

### Métricas en Tiempo Real:
- 🟢 Ventas del día
- 🔵 Ingresos totales  
- 🟡 Número de transacciones
- 🟣 Promedio por venta
- 🔴 Producto estrella
- ⚫ Unidades vendidas

### Gráficos Interactivos:
- **BarChart**: Ventas por día (últimos 7 días)
- **PieChart**: Productos más vendidos

### Herramientas:
- Actualización automática cada 10 segundos
- Exportación de reportes
- Gestión directa de productos
- Limpieza de base de datos

## 📁 **Estructura**

```
SistemaPOS/
├── app/src/main/java/          # Código fuente
├── app/src/main/resources/     # FXML y CSS
├── app/src/test/java/         # Tests
├── utilities/                  # Módulo utilidades
└── data/                      # Base de datos H2
```

## 📄 **Licencia**

MIT License - Ver [LICENSE](LICENSE) para detalles.

## � **Contacto**

- **Desarrollador**: [kealmahe2003](https://github.com/kealmahe2003)
- **Reportar Issues**: [GitHub Issues](https://github.com/kealmahe2003/SistemaPOS/issues)

---

⭐ **Si te gusta el proyecto, dale una estrella en GitHub**

