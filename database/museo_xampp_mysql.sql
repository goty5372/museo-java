-- =====================================================================
-- Base de datos: sistema_museos (Compatible con XAMPP / MySQL / MariaDB)
-- Sistema de Administracion de Museos
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sistema_museos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE sistema_museos;

DROP TABLE IF EXISTS detalle_recorrido_sala;
DROP TABLE IF EXISTS recorrido_guiado;
DROP TABLE IF EXISTS historial_exhibicion;
DROP TABLE IF EXISTS obra;
DROP TABLE IF EXISTS artista;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS sala_exposicion;
DROP TABLE IF EXISTS museo;

-- 1. Tabla museo
CREATE TABLE museo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    latitud DOUBLE,
    longitud DOUBLE,
    url_pagina VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabla sala_exposicion
CREATE TABLE sala_exposicion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_museo INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    capacidad INT NOT NULL,
    CONSTRAINT fk_sala_museo FOREIGN KEY (id_museo)
        REFERENCES museo(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabla categoria
CREATE TABLE categoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabla artista
CREATE TABLE artista (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nacionalidad VARCHAR(100),
    biografia TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Tabla obra
CREATE TABLE obra (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    url_imagen VARCHAR(255),
    fecha_creacion DATE NULL,
    fecha_adquisicion DATE NULL,
    porcentaje_conservacion INT NOT NULL,
    estado_conservacion VARCHAR(30) NOT NULL,
    id_categoria INT NOT NULL,
    id_artista INT NULL,
    id_sala_actual INT NOT NULL,
    CONSTRAINT fk_obra_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id),
    CONSTRAINT fk_obra_artista FOREIGN KEY (id_artista)
        REFERENCES artista(id) ON DELETE SET NULL,
    CONSTRAINT fk_obra_sala FOREIGN KEY (id_sala_actual)
        REFERENCES sala_exposicion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Tabla historial_exhibicion
CREATE TABLE historial_exhibicion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_obra INT NOT NULL,
    id_sala INT NOT NULL,
    fecha_entrada DATETIME NOT NULL,
    fecha_salida DATETIME NULL,
    CONSTRAINT fk_historial_obra FOREIGN KEY (id_obra)
        REFERENCES obra(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_sala FOREIGN KEY (id_sala)
        REFERENCES sala_exposicion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Tabla recorrido_guiado
CREATE TABLE recorrido_guiado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    url_pagina VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Tabla detalle_recorrido_sala
CREATE TABLE detalle_recorrido_sala (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_recorrido INT NOT NULL,
    id_sala INT NOT NULL,
    numero_orden INT NOT NULL,
    CONSTRAINT fk_detalle_recorrido FOREIGN KEY (id_recorrido)
        REFERENCES recorrido_guiado(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_sala FOREIGN KEY (id_sala)
        REFERENCES sala_exposicion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Datos iniciales de prueba
-- =====================================================================

INSERT INTO museo (id, nombre, descripcion, latitud, longitud, url_pagina) VALUES
(1, 'Museo Nacional de Colombia', 'Museo fundado en 1823 en Bogota, con colecciones de arte, historia y arqueologia', 4.615556, -74.068889, 'https://www.museonacional.gov.co'),
(2, 'Museo del Oro', 'Museo del Banco de la Republica con piezas de orfebreria prehispanica', 4.601944, -74.071944, 'https://www.banrepcultural.org/bogota/museo-del-oro');

INSERT INTO sala_exposicion (id, id_museo, nombre, descripcion, capacidad) VALUES
(1, 1, 'Sala Arte Moderno', 'Pintura y escultura colombiana del siglo XX', 60),
(2, 1, 'Sala Memoria y Nacion', 'Obras sobre la historia politica y social del pais', 80),
(3, 1, 'Sala de Exposiciones Temporales', 'Muestras que cambian por temporada', 120),
(4, 2, 'Sala El Trabajo de los Metales', 'Tecnicas de orfebreria prehispanica', 70),
(5, 2, 'Sala Cosmologia y Simbolismo', 'Piezas relacionadas con rituales y chamanismo', 90),
(6, 2, 'Sala La Ofrenda', 'Ofrendas votivas', 40);

INSERT INTO categoria (id, nombre, descripcion) VALUES
(1, 'Pintura', 'Oleos, acuarelas y tecnicas mixtas'),
(2, 'Escultura', 'Obras en bronce, piedra o madera'),
(3, 'Orfebreria prehispanica', 'Piezas de oro y tumbaga anteriores a 1492');

INSERT INTO artista (id, nombre, nacionalidad, biografia) VALUES
(1, 'Fernando Botero', 'Colombiana', 'Pintor y escultor nacido en Medellin (1932-2023)'),
(2, 'Alejandro Obregon', 'Colombiana', 'Pintor nacido en Barcelona (1920-1992)'),
(3, 'Debora Arango', 'Colombiana', 'Pintora antioquena (1907-2005)');

INSERT INTO obra (id, nombre, descripcion, url_imagen, fecha_creacion, fecha_adquisicion, porcentaje_conservacion, estado_conservacion, id_categoria, id_artista, id_sala_actual) VALUES
(1, 'Violencia', 'Oleo sobre lienzo', '', '1962-01-01', '1963-05-14', 92, 'Optimo', 1, 2, 1),
(2, 'Masacre del 9 de abril', 'Oleo sobre el Bogotazo de 1948', '', '1948-01-01', '1985-09-20', 58, 'Critico', 1, 3, 2),
(3, 'Mona Lisa', 'Version de Botero del cuadro de Leonardo', '', '1978-01-01', '2000-11-01', 88, 'Optimo', 1, 1, 3),
(4, 'Mujer reclinada', 'Escultura en bronce', '', '1993-01-01', '2001-03-15', 95, 'Optimo', 2, 1, 1),
(5, 'Poporo Quimbaya', 'Recipiente de oro, aprox. 500 a.C. - 600 d.C.', '', NULL, '1939-12-22', 97, 'Optimo', 3, NULL, 4),
(6, 'Balsa Muisca', 'Pieza de tumbaga encontrada en Pasca, Cundinamarca', '', NULL, '1969-03-01', 90, 'Optimo', 3, NULL, 6),
(7, 'Pectoral Calima', 'Pectoral con rostro humano, tiene corrosion en los bordes', '', NULL, '1958-07-10', 47, 'Critico', 3, NULL, 5);

INSERT INTO historial_exhibicion (id_obra, id_sala, fecha_entrada, fecha_salida) VALUES
(1, 1, '2018-03-01 09:00:00', NULL),
(2, 3, '2019-04-09 09:00:00', '2019-11-05 09:00:00'),
(2, 2, '2019-11-05 09:00:00', NULL),
(3, 1, '2017-02-01 09:00:00', '2026-01-15 09:00:00'),
(3, 3, '2026-01-15 09:00:00', NULL),
(4, 3, '2021-06-01 09:00:00', '2022-01-10 09:00:00'),
(4, 1, '2022-01-10 09:00:00', NULL),
(5, 5, '2015-08-01 09:00:00', '2020-03-02 09:00:00'),
(5, 4, '2020-03-02 09:00:00', NULL),
(6, 6, '2008-05-01 09:00:00', NULL),
(7, 4, '2012-01-15 09:00:00', '2024-07-08 09:00:00'),
(7, 5, '2024-07-08 09:00:00', NULL);

INSERT INTO recorrido_guiado (id, nombre, descripcion, fecha_inicio, fecha_fin, url_pagina) VALUES
(1, 'Arte colombiano del siglo XX', 'Recorrido por las obras de Botero, Obregon y Arango', '2026-12-01 09:00:00', '2027-02-28 17:00:00', 'https://example.org/recorridos/arte-siglo-xx'),
(2, 'Ruta del oro prehispanico', 'Recorrido por la orfebreria y los rituales prehispanicos', '2026-11-20 10:00:00', '2027-01-31 16:00:00', 'https://example.org/recorridos/ruta-del-oro');

INSERT INTO detalle_recorrido_sala (id_recorrido, id_sala, numero_orden) VALUES
(1, 3, 1),
(1, 1, 2),
(1, 2, 3),
(2, 4, 1),
(2, 5, 2),
(2, 6, 3);
