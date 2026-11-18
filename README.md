# Sistema de Administración de Museos - API REST en Quarkus y MySQL (XAMPP)

## Descripción general

El **Sistema de Administración de Museos** es una aplicación backend y API REST desarrollada para la gestión integral de museos, salas de exposición, colecciones de obras de arte, artistas, categorías, historial de traslados y recorridos guiados, inspirada en instituciones culturales como el **Museo Nacional de Colombia** y el **Museo del Oro**.

El sistema permite registrar, consultar y administrar toda la información museográfica mediante servicios web RESTful construidos con **Quarkus 3.6**, **RESTEasy Reactive (JAX-RS)**, inyección de dependencias **CDI (ArC)** y una base de datos en **MySQL / MariaDB (XAMPP)**, permitiendo que los datos y cambios realizados desde la API se almacenen y actualicen en la base de datos. Además, incorpora documentación interactiva mediante **Swagger UI (OpenAPI)** y aplica principios fundamentales de **Programación Orientada a Objetos (POO)**.

---

## Objetivo del proyecto

El objetivo principal del proyecto es construir un sistema estructurado y escalable que permita centralizar y organizar la información de colecciones culturales y museográficas, facilitando el control del inventario de obras, su ubicación por salas, el seguimiento de su estado de conservación y la planeación de recorridos guiados.

El sistema busca simular una herramienta institucional donde los datos puedan ser consultados, registrados, validados y reportados de manera organizada y persistente en **MySQL (XAMPP)**.

---

## Funcionalidades principales

### Gestión de Museos

El módulo de museos permite administrar las sedes o instituciones registradas en el sistema, incluyendo:

- Identificador único autogenerado.
- Nombre oficial y descripción institucional.
- Coordenadas geográficas validadas (latitud entre `-90` y `90`, longitud entre `-180` y `180`).
- Enlace al sitio web oficial.
- Lista de salas de exposición asociadas y cálculo automático de la capacidad total de aforo del museo.

### Salas de Exposición

La sección de salas permite organizar los espacios físicos dentro de cada museo:

- Registro de salas vinculadas directamente a un museo existente (relación de composición).
- Validación para evitar nombres de salas duplicados dentro del mismo museo.
- Control de capacidad máxima de personas por sala (mayor a cero).

### Obras de Arte e Historial de Exhibición

El módulo de obras permite gestionar el catálogo artístico y arqueológico junto con su trazabilidad:

- Nombre, descripción, URL de imagen, categoría y artista (con soporte para obras de autor **Anónimo**).
- Validación de coherencia cronológica entre fecha de creación y fecha de adquisición.
- Registro automático del primer ingreso a sala en el historial de exhibición.
- Traslado de obras entre salas (`PUT /obras/{id}/mover`), cerrando automáticamente el periodo de exhibición anterior y abriendo uno nuevo con fecha y hora tanto en el sistema como en MySQL.

### Artistas y Categorías

Permite catalogar a los autores y clasificar las piezas de la colección:

- **Artistas:** registro de nombre completo, nacionalidad y biografía.
- **Categorías:** clasificación temática o técnica (por ejemplo: *Pintura*, *Escultura*, *Orfebrería prehispánica*), evitando duplicidad de nombres.

### Recorridos Guiados

El sistema permite planificar itinerarios culturales para visitantes:

- Nombre, descripción, página web, fecha de inicio y fecha de finalización validada.
- Cálculo automático del estado del recorrido según la fecha actual:
  - **Próximo:** antes de la fecha de inicio.
  - **Activo:** durante el rango de fechas programado.
  - **Finalizado:** después de la fecha de cierre.
- Administración dinámica del itinerario de salas: agregar salas, cambiar el orden de visita y retirar salas renumerando automáticamente las posiciones.

### Estados de Conservación

Cada obra cuenta con un porcentaje de conservación (`0` a `100`) a partir del cual el sistema determina automáticamente su estado y emite una recomendación técnica:

| Rango | Estado | Recomendación |
|---|---|---|
| `80% – 100%` | **Óptimo** | Puede seguir en exhibición normalmente |
| `60% – 79%` | **Regular** | Programar una revisión en los próximos meses |
| `0% – 59%` | **Crítico** | Retirar a restauración lo antes posible |

### Reportes Administrativos

El módulo de reportes consolida indicadores clave del sistema:

- Listado detallado de obras con su artista, categoría, sala actual, museo, porcentaje y estado de conservación.
- Promedio de conservación calculado por cada sala de exposición que contiene obras.
- Agrupación de obras clasificadas por estado de conservación (`Óptimo`, `Regular`, `Crítico`) junto con su recomendación.
- Reporte general polimórfico que unifica museos, obras y recorridos guiados bajo la interfaz `Reportable`.

---

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 17+ | Lenguaje principal del proyecto |
| Quarkus 3.6.0 | Framework principal para el desarrollo del backend |
| MySQL / MariaDB (XAMPP) | Motor de base de datos relacional |
| SQL | Creación, consulta y manipulación de datos |
| RESTEasy Reactive | Exposición de endpoints RESTful con soporte JSON (Jackson) |
| Quarkus ArC (CDI) | Inyección de dependencias y gestión del ciclo de vida (`@ApplicationScoped`) |
| SmallRye OpenAPI | Generación de especificación OpenAPI y consola interactiva Swagger UI |
| Maven | Gestión del proyecto, compilación y dependencias |
| Visual Studio Code | Editor de código utilizado |
| GitHub | Publicación y control de versiones |

---

## Librerías y dependencias principales

El proyecto utiliza Maven para administrar las dependencias necesarias.

Dependencias principales:

- `quarkus-resteasy-reactive-jackson`
- `quarkus-arc`
- `quarkus-smallrye-openapi`
- `mysql-connector-j` (MySQL JDBC Driver)
- `quarkus-maven-plugin`
- `maven-compiler-plugin`

Estas dependencias se encuentran configuradas en el archivo:

```text
pom.xml
```

---

## Motor de base de datos

El proyecto utiliza **MySQL / MariaDB (XAMPP)** como motor de base de datos.

Nombre de la base de datos:

```sql
sistema_museos
```

La base de datos contiene las siguientes tablas:

- `museo` (Museos registrados y su ubicación geográfica).
- `sala_exposicion` (Salas asociadas a cada museo y su capacidad).
- `categoria` (Categorías de las piezas de arte).
- `artista` (Autores registrados en el sistema).
- `obra` (Catálogo de obras con porcentaje y estado de conservación).
- `historial_exhibicion` (Trazabilidad de entradas y salidas de obras por sala).
- `recorrido_guiado` (Recorridos programados con fechas de inicio y fin).
- `detalle_recorrido_sala` (Orden de visita de salas dentro de cada recorrido).

El script SQL para **XAMPP / phpMyAdmin** se encuentra en la carpeta:

```text
database/museo_xampp_mysql.sql
```

---

## Estructura del proyecto

```text
museo-java/
├── database/
│   └── museo_xampp_mysql.sql
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── config/
│       │   │   └── ConexionBD.java
│       │   │
│       │   ├── dto/
│       │   │   ├── AgregarSalaRequest.java
│       │   │   ├── CambiarOrdenRequest.java
│       │   │   ├── CrearRecorridoRequest.java
│       │   │   ├── MoverObraRequest.java
│       │   │   ├── RegistrarArtistaRequest.java
│       │   │   ├── RegistrarCategoriaRequest.java
│       │   │   ├── RegistrarMuseoRequest.java
│       │   │   ├── RegistrarObraRequest.java
│       │   │   └── RegistrarSalaRequest.java
│       │   │
│       │   ├── modelo/
│       │   │   ├── Artista.java
│       │   │   ├── Categoria.java
│       │   │   ├── DetalleRecorridoSala.java
│       │   │   ├── EstadoConservacion.java
│       │   │   ├── Fechas.java
│       │   │   ├── HistorialExhibicion.java
│       │   │   ├── Museo.java
│       │   │   ├── Obra.java
│       │   │   ├── RecorridoGuiado.java
│       │   │   ├── Reportable.java
│       │   │   └── SalaExposicion.java
│       │   │
│       │   ├── resource/
│       │   │   ├── ArtistaResource.java
│       │   │   ├── CategoriaResource.java
│       │   │   ├── ErrorMapper.java
│       │   │   ├── MuseoResource.java
│       │   │   ├── ObraResource.java
│       │   │   ├── RecorridoResource.java
│       │   │   ├── ReporteResource.java
│       │   │   └── SalaResource.java
│       │   │
│       │   └── servicio/
│       │       └── SistemaMuseoService.java
│       │
│       └── resources/
│           └── application.properties
│
├── iniciar.bat
├── pom.xml
└── README.md
```

---

## Endpoints de la API REST

| Módulo | Método y Ruta | Descripción |
|---|---|---|
| **Museos** | `GET /museos` | Lista todos los museos registrados |
| **Museos** | `GET /museos/{id}` | Consulta un museo por su identificador |
| **Museos** | `POST /museos` | Registra un nuevo museo |
| **Salas** | `GET /salas` | Lista todas las salas de exposición |
| **Salas** | `GET /salas/{id}` | Consulta una sala por su identificador |
| **Salas** | `POST /salas` | Registra una nueva sala dentro de un museo |
| **Obras** | `GET /obras` | Lista todas las obras registradas |
| **Obras** | `GET /obras/{id}` | Consulta el detalle e historial de una obra |
| **Obras** | `POST /obras` | Registra una nueva obra en una sala |
| **Obras** | `PUT /obras/{id}/mover` | Traslada una obra a otra sala y actualiza su historial |
| **Artistas** | `GET /artistas` | Lista todos los artistas |
| **Artistas** | `GET /artistas/{id}` | Consulta un artista por su identificador |
| **Artistas** | `POST /artistas` | Registra un nuevo artista |
| **Categorías** | `GET /categorias` | Lista todas las categorías |
| **Categorías** | `GET /categorias/{id}` | Consulta una categoría por su identificador |
| **Categorías** | `POST /categorias` | Registra una nueva categoría |
| **Recorridos** | `GET /recorridos` | Lista todos los recorridos guiados |
| **Recorridos** | `GET /recorridos/{id}` | Consulta un recorrido y sus salas en orden |
| **Recorridos** | `POST /recorridos` | Crea un nuevo recorrido guiado |
| **Recorridos** | `POST /recorridos/{id}/salas` | Agrega una sala al final del recorrido |
| **Recorridos** | `PUT /recorridos/{id}/salas/orden` | Cambia la posición de una sala en el recorrido |
| **Recorridos** | `DELETE /recorridos/{id}/salas/{posicion}` | Retira una sala del recorrido según su posición |
| **Reportes** | `GET /reportes/obras` | Resumen general de obras, ubicación y conservación |
| **Reportes** | `GET /reportes/conservacion` | Promedio de porcentaje de conservación por sala |
| **Reportes** | `GET /reportes/estados` | Obras agrupadas por estado (`Óptimo`, `Regular`, `Crítico`) |
| **Reportes** | `GET /reportes/general` | Reporte polimórfico de museos, obras y recorridos |

---

## Requisitos previos

Antes de ejecutar el proyecto, se recomienda tener instalado:

- **Java JDK 17** o superior.
- **Apache Maven 3.8+**.
- **XAMPP** (con los servicios **Apache** y **MySQL** activos).
- **Visual Studio Code** o un IDE compatible con Java.

---

## Cómo ejecutar el proyecto

### 1. Descargar o clonar el repositorio

Si el proyecto está en GitHub, se puede clonar con:

```bash
git clone https://github.com/Jeizxon/Museo_quarkus.git
```

Luego entrar a la carpeta del proyecto:

```bash
cd Museo_quarkus
```

También se puede descargar el repositorio como archivo `.zip` desde GitHub y descomprimirlo.

---

### 2. Iniciar XAMPP y cargar la base de datos en phpMyAdmin

1. Abrir el **XAMPP Control Panel** e iniciar los módulos **Apache** y **MySQL** (*Start*).
2. Entrar desde el navegador a **phpMyAdmin**:

```text
http://localhost/phpmyadmin
```

3. Ir a la pestaña **SQL** o **Importar**, abrir el archivo:

```text
database/museo_xampp_mysql.sql
```

4. Ejecutar el script para crear automáticamente la base de datos `sistema_museos` con sus 8 tablas y los registros iniciales de prueba.

---

### 3. Configurar la conexión a MySQL (XAMPP)

Abrir el archivo:

```text
src/main/java/config/ConexionBD.java
```

La aplicación está configurada por defecto para conectarse al servidor MySQL de XAMPP en el puerto `3306` con el usuario `root` sin contraseña:

```java
jdbc:mysql://localhost:3306/sistema_museos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

También se puede personalizar la conexión sin editar código mediante variables de entorno:

```text
MUSEO_DB_URL
MUSEO_DB_USER
MUSEO_DB_PASSWORD
```

---

### 4. Ejecutar el proyecto

Desde la terminal ubicada en la carpeta raíz del proyecto, ejecutar:

```bash
mvn quarkus:dev
```

O en Windows haciendo doble clic directamente sobre el archivo:

```text
iniciar.bat
```

---

### 5. Consultar la API y Swagger UI

Una vez iniciada la aplicación, se puede acceder desde el navegador a las siguientes rutas:

| Recurso | URL |
|---|---|
| Consola interactiva **Swagger UI** | `http://localhost:8080/swagger-ui` |
| Panel **phpMyAdmin (XAMPP)** | `http://localhost/phpmyadmin` |
| URL Base de la API | `http://localhost:8080` |
| Especificación OpenAPI | `http://localhost:8080/q/openapi` |

---

## Datos de prueba incluidos

Tanto el script `database/museo_xampp_mysql.sql` como el sistema incluyen los siguientes datos iniciales:

| ID | Obra | Artista | Categoría | Sala Actual | Conservación | Estado |
|---|---|---|---|---|---|---|
| `1` | Violencia | Alejandro Obregón | Pintura | Sala Arte Moderno | `92%` | Óptimo |
| `2` | Masacre del 9 de abril | Débora Arango | Pintura | Sala Memoria y Nación | `58%` | Crítico |
| `3` | Mona Lisa | Fernando Botero | Pintura | Sala de Exposiciones Temporales | `88%` | Óptimo |
| `4` | Mujer reclinada | Fernando Botero | Escultura | Sala Arte Moderno | `95%` | Óptimo |
| `5` | Poporo Quimbaya | Anónimo | Orfebrería prehispánica | Sala El Trabajo de los Metales | `97%` | Óptimo |
| `6` | Balsa Muisca | Anónimo | Orfebrería prehispánica | Sala La Ofrenda | `90%` | Óptimo |
| `7` | Pectoral Calima | Anónimo | Orfebrería prehispánica | Sala Cosmología y Simbolismo | `47%` | Crítico |

---

## Notas importantes

- Se recomienda tener **MySQL activo en XAMPP** antes de registrar o trasladar obras para que los cambios se reflejen en tiempo real en `sistema_museos` desde **phpMyAdmin**.
- La base de datos se llama `sistema_museos`.
- Todas las validaciones de negocio devuelven un mensaje claro en formato `{"error": "..."}` con código HTTP `400 Bad Request`.
- La interfaz visual recomendada para probar todos los endpoints frente al docente es **Swagger UI** (`http://localhost:8080/swagger-ui`).

---

## Posibles errores comunes

### Error de conexión a MySQL en XAMPP

Verificar:

- Que el servicio **MySQL** esté en verde (*Running*) en el panel de control de **XAMPP**.
- Que la base de datos `sistema_museos` exista en `http://localhost/phpmyadmin`.
- Que el puerto de MySQL sea `3306` y el usuario sea `root` en `ConexionBD.java`.

### El puerto 8080 ya está en uso

Verificar que no haya otra instancia corriendo en el puerto `8080`, o cambiar el puerto en `src/main/resources/application.properties`:

```properties
quarkus.http.port=8081
```

---

## Estado del proyecto

Proyecto académico.

La API REST en Quarkus se encuentra conectada a MySQL (XAMPP) y permite consultar, registrar, trasladar y generar reportes sobre museos, salas de exposición, artistas, categorías, obras de arte y recorridos guiados.

---
