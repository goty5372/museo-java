# Sistema de Administración de Museos - API REST en Quarkus

## Descripción general

El **Sistema de Administración de Museos** es una aplicación backend y API REST desarrollada para la gestión integral de museos, salas de exposición, colecciones de obras de arte, artistas, categorías, historial de traslados y recorridos guiados, inspirada en instituciones culturales como el **Museo Nacional de Colombia** y el **Museo del Oro**.

El sistema permite registrar, consultar y administrar toda la información museográfica mediante servicios web RESTful construidos con **Quarkus 3.6**, **RESTEasy Reactive (JAX-RS)** e inyección de dependencias **CDI (ArC)**. Además, incorpora documentación interactiva mediante **Swagger UI (OpenAPI)** y aplica principios fundamentales de **Programación Orientada a Objetos (POO)** como encapsulamiento, composición, asociación y polimorfismo.

---

## Objetivo del proyecto

El objetivo principal del proyecto es construir un sistema estructurado y escalable que permita centralizar y organizar la información de colecciones culturales y museográficas, facilitando el control del inventario de obras, su ubicación por salas, el seguimiento de su estado de conservación y la planeación de recorridos guiados.

El sistema busca simular una herramienta institucional donde los datos puedan ser consultados, registrados, validados y reportados de manera organizada a través de endpoints HTTP estandarizados en formato JSON.

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
- Traslado de obras entre salas (`PUT /obras/{id}/mover`), cerrando automáticamente el periodo de exhibición anterior y abriendo uno nuevo con fecha y hora.

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
| Java 17 | Lenguaje principal del proyecto |
| Quarkus 3.6.0 | Framework principal para el desarrollo del backend |
| RESTEasy Reactive | Exposición de endpoints RESTful con soporte JSON (Jackson) |
| Quarkus ArC (CDI) | Inyección de dependencias y gestión del ciclo de vida (`@ApplicationScoped`) |
| SmallRye OpenAPI | Generación de especificación OpenAPI y consola interactiva Swagger UI |
| Maven | Gestión del proyecto, compilación y dependencias |
| Visual Studio Code / IDE Java | Entorno de desarrollo utilizado |
| GitHub | Publicación y control de versiones |

---

## Librerías y dependencias principales

El proyecto utiliza Maven para administrar las dependencias y plugins necesarios.

Dependencias principales:

- `quarkus-resteasy-reactive-jackson` (Servicios REST y serialización JSON)
- `quarkus-arc` (Contenedor CDI para inyección de dependencias)
- `quarkus-smallrye-openapi` (Documentación OpenAPI y Swagger UI)
- `quarkus-maven-plugin` (Ejecución en modo desarrollo con *hot-reload* y empaquetado)
- `maven-compiler-plugin` (Compilación configurada para Java 17 con soporte de parámetros)

Estas dependencias se encuentran configuradas en el archivo:

```text
pom.xml
```

---

## Gestión de datos y diseño orientado a objetos

El proyecto centraliza la lógica de negocio y el almacenamiento en memoria dentro del servicio `@ApplicationScoped`:

```text
src/main/java/servicio/SistemaMuseoService.java
```

Al iniciar la aplicación, el método anotado con `@PostConstruct` (`cargarDatosDePrueba()`) inicializa automáticamente el catálogo institucional con museos, salas, categorías, artistas, obras, traslados históricos y recorridos guiados listos para ser consultados.

Principios de **Programación Orientada a Objetos (POO)** aplicados en el modelo:

- **Composición:** `Museo` contiene y gestiona su colección de `SalaExposicion`; `RecorridoGuiado` administra su lista ordenada de `DetalleRecorridoSala`; `Obra` gestiona su lista de `HistorialExhibicion`.
- **Asociación:** `Obra` se relaciona con `Artista`, `Categoria` y `SalaExposicion`.
- **Polimorfismo:** La interfaz `Reportable` define el contrato `generarReporte()`, implementado de forma específica por `Museo`, `Obra` y `RecorridoGuiado`.
- **Encapsulamiento y validaciones:** Todas las entidades validan la integridad de sus datos en constructores y *setters*, lanzando `IllegalArgumentException` que `ErrorMapper` transforma automáticamente en respuestas HTTP `400 Bad Request` en formato JSON.
- **Enumeraciones con comportamiento:** `EstadoConservacion` declara el método abstracto `getRecomendacion()`, sobrescrito individualmente por cada constante (`OPTIMO`, `REGULAR`, `CRITICO`).

---

## Estructura del proyecto

```text
museo-java/
├── src/
│   └── main/
│       ├── java/
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

## Ejemplos de uso de la API

### Registrar un museo (`POST /museos`)

```json
{
  "nombre": "Museo de Arte Moderno de Bogotá",
  "descripcion": "Espacio cultural dedicado al arte moderno y contemporáneo",
  "latitud": 4.6097,
  "longitud": -74.0817,
  "urlPagina": "https://www.mambogota.com"
}
```

### Registrar una obra (`POST /obras`)

```json
{
  "nombre": "El Abrazo",
  "descripcion": "Óleo sobre lienzo",
  "urlImagen": "https://example.org/imagenes/el-abrazo.jpg",
  "fechaCreacion": "1980-05-10",
  "fechaAdquisicion": "1995-08-20",
  "porcentajeConservacion": 85,
  "idCategoria": 1,
  "idArtista": 1,
  "idSala": 1
}
```

### Mover una obra de sala (`PUT /obras/1/mover`)

```json
{
  "idSala": 3,
  "fecha": "2026-11-18T10:00:00"
}
```

---

## Requisitos previos

Antes de ejecutar el proyecto, se recomienda tener instalado:

- **Java JDK 17** o superior.
- **Apache Maven 3.8+**.
- **Visual Studio Code**, IntelliJ IDEA o un IDE compatible con proyectos Maven y Java.
- Navegador web, **Postman**, **Insomnia** o `curl` para probar los endpoints REST.

---

## Cómo ejecutar el proyecto

### 1. Descargar o clonar el repositorio

Si el proyecto está en GitHub, se puede clonar con:

```bash
git clone https://github.com/goty5372/museo-java.git
```

Luego entrar a la carpeta del proyecto:

```bash
cd museo-java
```

También se puede descargar el repositorio como archivo `.zip` desde GitHub y descomprimirlo.

---

### 2. Verificar la configuración de la aplicación

Abrir el archivo:

```text
src/main/resources/application.properties
```

La aplicación está configurada por defecto para ejecutarse en el puerto `8080` y habilitar siempre la interfaz de Swagger UI:

```properties
quarkus.application.name=Sistema de Administracion de Museos
quarkus.http.port=8080
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
```

---

### 3. Ejecutar el proyecto en modo desarrollo

Desde la terminal ubicada en la carpeta raíz del proyecto, ejecutar:

```bash
mvn quarkus:dev
```

Este comando compila el proyecto, carga los datos iniciales de prueba y activa la recarga en caliente (*hot-reload*) ante cualquier cambio en el código.

---

### 4. Compilar y ejecutar como paquete JAR (opcional)

Si se desea generar el paquete ejecutable de la aplicación:

```bash
mvn clean package
```

Y luego iniciarlo con:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

---

### 5. Consultar la aplicación y Swagger UI

Una vez iniciada la aplicación, se puede acceder desde el navegador a las siguientes rutas:

| Recurso | URL |
|---|---|
| URL Base de la API | `http://localhost:8080` |
| Consola interactiva Swagger UI | `http://localhost:8080/swagger-ui` |
| Especificación OpenAPI | `http://localhost:8080/q/openapi` |

---

## Datos de prueba incluidos

Al iniciar el servidor, el sistema precarga automáticamente los siguientes registros de prueba:

### Museos y Salas

| ID Museo | Museo | Salas Registradas |
|---|---|---|
| `1` | **Museo Nacional de Colombia** | 1. Sala Arte Moderno (60 personas)<br>2. Sala Memoria y Nación (80 personas)<br>3. Sala de Exposiciones Temporales (120 personas) |
| `2` | **Museo del Oro** | 4. Sala El Trabajo de los Metales (70 personas)<br>5. Sala Cosmología y Simbolismo (90 personas)<br>6. Sala La Ofrenda (40 personas) |

### Obras precargadas

| ID | Obra | Artista | Categoría | Conservación | Estado |
|---|---|---|---|---|---|
| `1` | Violencia | Alejandro Obregón | Pintura | `92%` | Óptimo |
| `2` | Masacre del 9 de abril | Débora Arango | Pintura | `58%` | Crítico |
| `3` | Mona Lisa | Fernando Botero | Pintura | `88%` | Óptimo |
| `4` | Mujer reclinada | Fernando Botero | Escultura | `95%` | Óptimo |
| `5` | Poporo Quimbaya | Anónimo | Orfebrería prehispánica | `97%` | Óptimo |
| `6` | Balsa Muisca | Anónimo | Orfebrería prehispánica | `90%` | Óptimo |
| `7` | Pectoral Calima | Anónimo | Orfebrería prehispánica | `47%` | Crítico |

### Recorridos guiados precargados

- **Arte colombiano del siglo XX:** recorre *Sala de Exposiciones Temporales* → *Sala Arte Moderno* → *Sala Memoria y Nación*.
- **Ruta del oro prehispánico:** recorre *Sala El Trabajo de los Metales* → *Sala Cosmología y Simbolismo* → *Sala La Ofrenda*.

---

## Notas importantes

- No se requiere configurar un motor de base de datos externo para ejecutar el proyecto, ya que `SistemaMuseoService` gestiona el estado en memoria durante la ejecución y precarga los datos con `@PostConstruct`.
- Todas las validaciones de negocio devuelven un mensaje claro en formato `{"error": "..."}` con código de estado HTTP `400 Bad Request`.
- Cuando se registra una obra con `idArtista` en `null`, el sistema la cataloga automáticamente como obra de autor **Anónimo**.
- La interfaz visual recomendada para probar todos los endpoints sin herramientas adicionales es **Swagger UI** (`http://localhost:8080/swagger-ui`).

---

## Posibles errores comunes

### El puerto 8080 ya está en uso

Verificar que no haya otra instancia de Quarkus u otro servicio corriendo en el puerto `8080`, o modificar la propiedad en `src/main/resources/application.properties`:

```properties
quarkus.http.port=8081
```

### Maven no se reconoce como comando

Instalar Apache Maven o verificar que la carpeta `bin` de Maven y la variable `JAVA_HOME` (apuntando a JDK 17+) estén agregadas al `PATH` del sistema.

---

## Estado del proyecto

Proyecto académico.

La API REST se encuentra completamente funcional e integrada con Quarkus, permitiendo registrar, consultar, trasladar y generar reportes sobre museos, salas de exposición, artistas, categorías, obras de arte y recorridos guiados.

---
