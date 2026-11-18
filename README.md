# 🏛️ Sistema de Administración de Museos

API REST en **Quarkus 3.6** para gestionar colecciones de arte, obras, artistas, salas de exposición y recorridos guiados. Desarrollado con principios de Programación Orientada a Objetos (POO): herencia, polimorfismo, encapsulamiento y composición.

---

## 🚀 Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| **Quarkus** | 3.6.0 | Framework principal |
| **RESTEasy Reactive** | — | Endpoints REST (JAX-RS) |
| **CDI (ArC)** | — | Inyección de dependencias |
| **SmallRye OpenAPI** | — | Documentación Swagger UI |
| **Java** | 17 | Lenguaje |
| **Maven** | 3.8+ | Gestión de dependencias |

---

## ✨ Funcionalidades

| Módulo | Endpoints |
|---|---|
| 🏛️ **Museos** | `GET /museos` · `GET /museos/{id}` · `POST /museos` |
| 🚪 **Salas** | `GET /salas` · `GET /salas/{id}` · `POST /salas` |
| 🎨 **Obras** | `GET /obras` · `GET /obras/{id}` · `POST /obras` · `PUT /obras/{id}/mover` |
| 👤 **Artistas** | `GET /artistas` · `GET /artistas/{id}` · `POST /artistas` |
| 🏷️ **Categorías** | `GET /categorias` · `GET /categorias/{id}` · `POST /categorias` |
| 🗺️ **Recorridos** | `GET /recorridos` · `GET /recorridos/{id}` · `POST /recorridos` · `POST /recorridos/{id}/salas` · `PUT /recorridos/{id}/salas/orden` · `DELETE /recorridos/{id}/salas/{posicion}` |
| 📊 **Reportes** | `GET /reportes/obras` · `GET /reportes/conservacion` · `GET /reportes/estados` · `GET /reportes/general` |

---

## 🔎 Estados de Conservación

El estado de cada obra se determina automáticamente según su porcentaje de conservación:

| Rango | Estado | Recomendación |
|---|---|---|
| 80% – 100% | 🟢 Óptimo | Puede seguir en exhibición normalmente |
| 60% – 79% | 🟡 Regular | Programar una revisión en los próximos meses |
| 0% – 59% | 🔴 Crítico | Retirar a restauración lo antes posible |

---

## 🗂️ Estructura del Proyecto

```
museo-java/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── modelo/
    │   │   ├── Museo.java                  # Entidad Museo (contiene salas)
    │   │   ├── SalaExposicion.java         # Sala dentro de un museo
    │   │   ├── Obra.java                   # Obra de arte con historial de traslados
    │   │   ├── Artista.java                # Artista autor de la obra
    │   │   ├── Categoria.java              # Categoría de la obra
    │   │   ├── RecorridoGuiado.java        # Recorrido con itinerario de salas
    │   │   ├── DetalleRecorridoSala.java   # Relación ordenada recorrido ↔ sala
    │   │   ├── HistorialExhibicion.java    # Registro de estancia de una obra en una sala
    │   │   ├── EstadoConservacion.java     # Enum con estados y recomendaciones
    │   │   ├── Fechas.java                 # Formateadores de fecha reutilizables
    │   │   └── Reportable.java             # Interfaz para reportes (polimorfismo)
    │   ├── servicio/
    │   │   └── SistemaMuseoService.java    # @ApplicationScoped — lógica del negocio
    │   ├── resource/
    │   │   ├── MuseoResource.java          # REST /museos
    │   │   ├── SalaResource.java           # REST /salas
    │   │   ├── ObraResource.java           # REST /obras
    │   │   ├── ArtistaResource.java        # REST /artistas
    │   │   ├── CategoriaResource.java      # REST /categorias
    │   │   ├── RecorridoResource.java      # REST /recorridos
    │   │   ├── ReporteResource.java        # REST /reportes
    │   │   └── ErrorMapper.java            # 400 para IllegalArgumentException
    │   └── dto/
    │       ├── RegistrarMuseoRequest.java
    │       ├── RegistrarSalaRequest.java
    │       ├── RegistrarObraRequest.java
    │       ├── RegistrarArtistaRequest.java
    │       ├── RegistrarCategoriaRequest.java
    │       ├── MoverObraRequest.java
    │       ├── CrearRecorridoRequest.java
    │       ├── AgregarSalaRequest.java
    │       └── CambiarOrdenRequest.java
    └── resources/
        └── application.properties
```

---

## 🧱 Diseño Orientado a Objetos

```
Reportable (interfaz)
 ├── Museo           → @ApplicationScoped via SistemaMuseoService
 │    └── compone SalaExposicion[]
 ├── Obra            → asocia Artista, Categoria, SalaExposicion
 │    └── contiene HistorialExhibicion[]
 └── RecorridoGuiado → compone DetalleRecorridoSala[]

EstadoConservacion (enum con método abstracto)
 ├── OPTIMO · REGULAR · CRITICO
```

- **CDI**: `SistemaMuseoService` es `@ApplicationScoped`; los recursos lo reciben con `@Inject`
- **Polimorfismo**: `Reportable.imprimirReporte()` implementado por `Museo`, `Obra` y `RecorridoGuiado`
- **Encapsulamiento**: validaciones en cada setter (nombres no vacíos, fechas coherentes, porcentaje 0–100, coordenadas válidas)
- **Enum con comportamiento**: `EstadoConservacion` define un método abstracto `getRecomendacion()` sobrescrito en cada constante

---

## ▶️ Cómo ejecutar

### Requisitos
- Java 17+
- Maven 3.8+

### Modo desarrollo (hot-reload)
```bash
mvn quarkus:dev
```

### Compilar y ejecutar
```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

### Accesos
| Recurso | URL |
|---|---|
| API base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui |
| OpenAPI spec | http://localhost:8080/q/openapi |

> Al arrancar, el sistema carga automáticamente datos de ejemplo (Museo Nacional de Colombia, Museo del Oro, 7 obras, 2 recorridos) mediante `@PostConstruct`.

---

## 📡 Ejemplos de uso

### Registrar un museo
```http
POST /museos
Content-Type: application/json

{
  "nombre": "Museo de Arte Moderno",
  "descripcion": "MAMBO de Bogotá",
  "latitud": 4.6097,
  "longitud": -74.0817,
  "urlPagina": "https://www.mambogota.com"
}
```

### Registrar una obra
```http
POST /obras
Content-Type: application/json

{
  "nombre": "El Abrazo",
  "descripcion": "Óleo sobre lienzo",
  "porcentajeConservacion": 85,
  "idCategoria": 1,
  "idArtista": 1,
  "idSala": 1
}
```

### Mover una obra de sala
```http
PUT /obras/1/mover
Content-Type: application/json

{
  "idSala": 3,
  "fecha": "2026-11-18T10:00:00"
}
```

### Reporte de conservación por sala
```http
GET /reportes/conservacion
```

---

## 🗃️ Datos de ejemplo incluidos

**Museos:** Museo Nacional de Colombia · Museo del Oro  
**Artistas:** Fernando Botero · Alejandro Obregón · Débora Arango  
**Obras:** Violencia, Masacre del 9 de abril, Mona Lisa (Botero), Mujer reclinada, Poporo Quimbaya, Balsa Muisca, Pectoral Calima  
**Recorridos:** Arte colombiano del siglo XX · Ruta del oro prehispánico  

---

## 📄 Licencia

Proyecto académico de libre uso.
