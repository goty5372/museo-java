# 🏛️ Sistema de Administración de Museos

Sistema de escritorio en Java para gestionar colecciones de arte, obras, artistas, salas de exposición y recorridos guiados. Desarrollado con principios de Programación Orientada a Objetos (POO): herencia, polimorfismo, encapsulamiento y composición.

---

## 📋 Descripción

La aplicación permite administrar múltiples museos con sus respectivas salas, registrar obras de arte con seguimiento de conservación, trazar el historial de traslados entre salas y planificar recorridos guiados. Al iniciar, carga automáticamente datos de ejemplo basados en el **Museo Nacional de Colombia** y el **Museo del Oro**.

---

## ✨ Funcionalidades

| Módulo | Descripción |
|---|---|
| 🏛️ **Museos** | Registrar museos con nombre, descripción, coordenadas GPS y página web |
| 🚪 **Salas** | Crear salas de exposición con capacidad máxima, asociadas a un museo |
| 🎨 **Obras** | Registrar obras con artista (o anónimas), categoría, fechas y porcentaje de conservación |
| 👤 **Artistas** | Registrar artistas con nombre, nacionalidad y biografía |
| 🏷️ **Categorías** | Clasificar obras por tipo (Pintura, Escultura, Orfebrería, etc.) |
| 🔄 **Traslados** | Mover obras entre salas con registro histórico automático |
| 🗺️ **Recorridos** | Crear recorridos guiados con itinerario ordenado de salas, fechas y estado |
| 📊 **Reportes** | Listados de obras, promedios de conservación, historial de traslados, estado general |

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
└── src/
    ├── app/
    │   └── Main.java                   # Menú de consola e interacción con el usuario
    ├── modelo/
    │   ├── Museo.java                  # Entidad Museo (contiene salas)
    │   ├── SalaExposicion.java         # Sala dentro de un museo
    │   ├── Obra.java                   # Obra de arte con historial de traslados
    │   ├── Artista.java                # Artista autor de la obra
    │   ├── Categoria.java              # Categoría de la obra
    │   ├── RecorridoGuiado.java        # Recorrido con itinerario de salas
    │   ├── DetalleRecorridoSala.java   # Relación ordenada recorrido ↔ sala
    │   ├── HistorialExhibicion.java    # Registro de estancia de una obra en una sala
    │   ├── EstadoConservacion.java     # Enum con estados y recomendaciones
    │   ├── Fechas.java                 # Formateadores de fecha reutilizables
    │   └── Reportable.java             # Interfaz para generar reportes (polimorfismo)
    └── servicio/
        └── SistemaMuseoService.java    # Lógica del negocio, búsqueda y reportes
```

---

## 🧱 Diseño Orientado a Objetos

```
Reportable (interfaz)
 ├── Museo          → compone SalaExposicion[]
 ├── Obra           → asocia Artista, Categoria, SalaExposicion
 │                    contiene HistorialExhibicion[]
 └── RecorridoGuiado → compone DetalleRecorridoSala[]
                        (cada detalle referencia una SalaExposicion)

EstadoConservacion (enum con método abstracto)
 ├── OPTIMO
 ├── REGULAR
 └── CRITICO
```

- **Composición**: `Museo` → `SalaExposicion`; `RecorridoGuiado` → `DetalleRecorridoSala`
- **Polimorfismo**: `Reportable.imprimirReporte()` es implementado por `Museo`, `Obra` y `RecorridoGuiado`
- **Encapsulamiento**: validaciones en cada setter (nombres no vacíos, fechas coherentes, porcentaje 0-100, coordenadas válidas)
- **Enum con comportamiento**: `EstadoConservacion` define un método abstracto `getRecomendacion()` sobrescrito en cada constante

---

## 🚀 Cómo ejecutar

### Requisitos
- Java 11 o superior

### Compilar y correr desde la raíz del proyecto

```bash
# Compilar
javac -d out -sourcepath src src/app/Main.java

# Ejecutar
java -cp out app.Main
```

> Al iniciar, el sistema carga automáticamente datos de ejemplo para que puedas probar todas las funciones sin necesidad de ingresar datos manualmente.

---

## 📂 Menú principal

```
============================================
   SISTEMA DE ADMINISTRACION DE MUSEOS
============================================

------------- MENU PRINCIPAL -------------
1. Registrar museo
2. Registrar sala de exposicion
3. Registrar categoria
4. Registrar artista
5. Registrar obra
6. Mover obra de sala
7. Crear recorrido guiado
8. Cambiar orden de un recorrido
9. Ver listados
10. Reportes
0. Salir
```

### Reportes disponibles
1. Obras con artista, categoría, sala y museo
2. Promedio de conservación por sala
3. Itinerario de un recorrido guiado
4. Historial de salas de una obra
5. Obras por estado de conservación
6. Reporte general (museos, obras y recorridos)

---

## 🗃️ Datos de ejemplo incluidos

**Museos:** Museo Nacional de Colombia · Museo del Oro  
**Artistas:** Fernando Botero · Alejandro Obregón · Débora Arango  
**Obras:** Violencia, Masacre del 9 de abril, Mona Lisa (Botero), Mujer reclinada, Poporo Quimbaya, Balsa Muisca, Pectoral Calima  
**Recorridos:** Arte colombiano del siglo XX · Ruta del oro prehispánico  

---

## 📄 Licencia

Proyecto académico de libre uso.
