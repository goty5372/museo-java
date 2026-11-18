// Datos de ejemplo, son los mismos que se insertan en museo.sql
// las fechas con hora estan en el formato del input datetime-local (AAAA-MM-DDTHH:MM)

const DATOS_INICIALES = {
  museos: [
    {
      id: 1,
      nombre: 'Museo Nacional de Colombia',
      descripcion: 'Museo fundado en 1823 en Bogotá, con colecciones de arte, historia y arqueología.',
      latitud: 4.615556,
      longitud: -74.068889,
      url_pagina: 'https://www.museonacional.gov.co'
    },
    {
      id: 2,
      nombre: 'Museo del Oro',
      descripcion: 'Museo del Banco de la República con piezas de orfebrería prehispánica.',
      latitud: 4.601944,
      longitud: -74.071944,
      url_pagina: 'https://www.banrepcultural.org/bogota/museo-del-oro'
    }
  ],

  salas: [
    { id: 1, museo_id: 1, nombre: 'Sala Arte Moderno', descripcion: 'Pintura y escultura colombiana del siglo XX.', capacidad: 60 },
    { id: 2, museo_id: 1, nombre: 'Sala Memoria y Nación', descripcion: 'Obras sobre la historia política y social del país.', capacidad: 80 },
    { id: 3, museo_id: 1, nombre: 'Sala de Exposiciones Temporales', descripcion: 'Muestras que cambian por temporada.', capacidad: 120 },
    { id: 4, museo_id: 2, nombre: 'Sala El Trabajo de los Metales', descripcion: 'Técnicas de orfebrería prehispánica.', capacidad: 70 },
    { id: 5, museo_id: 2, nombre: 'Sala Cosmología y Simbolismo', descripcion: 'Piezas relacionadas con rituales y chamanismo.', capacidad: 90 },
    { id: 6, museo_id: 2, nombre: 'Sala La Ofrenda', descripcion: 'Ofrendas votivas.', capacidad: 40 }
  ],

  categorias: [
    { id: 1, nombre: 'Pintura', descripcion: 'Óleos, acuarelas y técnicas mixtas.' },
    { id: 2, nombre: 'Escultura', descripcion: 'Obras en bronce, piedra o madera.' },
    { id: 3, nombre: 'Orfebrería prehispánica', descripcion: 'Piezas de oro y tumbaga anteriores a 1492.' }
  ],

  artistas: [
    { id: 1, nombre: 'Fernando Botero', nacionalidad: 'Colombiana', biografia: 'Pintor y escultor nacido en Medellín (1932-2023), conocido por sus figuras de gran volumen.' },
    { id: 2, nombre: 'Alejandro Obregón', nacionalidad: 'Colombiana', biografia: 'Pintor nacido en Barcelona (1920-1992) que vivió en Barranquilla y Cartagena.' },
    { id: 3, nombre: 'Débora Arango', nacionalidad: 'Colombiana', biografia: 'Pintora antioqueña (1907-2005) que trabajó temas sociales y políticos.' }
  ],

  // los objetos 5, 6 y 7 no tienen artista ni fecha de creación exacta
  objetos: [
    { id: 1, nombre: 'Violencia', descripcion: 'Óleo sobre lienzo.', url_imagen: '', fecha_creacion: '1962-01-01', fecha_adquisicion: '1963-05-14', porcentaje_conservacion: 92, categoria_id: 1, artista_id: 2, sala_exhibicion_id: 1 },
    { id: 2, nombre: 'Masacre del 9 de abril', descripcion: 'Óleo sobre el Bogotazo de 1948.', url_imagen: '', fecha_creacion: '1948-01-01', fecha_adquisicion: '1985-09-20', porcentaje_conservacion: 58, categoria_id: 1, artista_id: 3, sala_exhibicion_id: 2 },
    { id: 3, nombre: 'Mona Lisa', descripcion: 'Versión de Botero del cuadro de Leonardo.', url_imagen: '', fecha_creacion: '1978-01-01', fecha_adquisicion: '2000-11-01', porcentaje_conservacion: 88, categoria_id: 1, artista_id: 1, sala_exhibicion_id: 3 },
    { id: 4, nombre: 'Mujer reclinada', descripcion: 'Escultura en bronce.', url_imagen: '', fecha_creacion: '1993-01-01', fecha_adquisicion: '2001-03-15', porcentaje_conservacion: 95, categoria_id: 2, artista_id: 1, sala_exhibicion_id: 1 },
    { id: 5, nombre: 'Poporo Quimbaya', descripcion: 'Recipiente de oro, aprox. 500 a. C. - 600 d. C.', url_imagen: '', fecha_creacion: '', fecha_adquisicion: '1939-12-22', porcentaje_conservacion: 97, categoria_id: 3, artista_id: null, sala_exhibicion_id: 4 },
    { id: 6, nombre: 'Balsa Muisca', descripcion: 'Pieza de tumbaga encontrada en Pasca, Cundinamarca.', url_imagen: '', fecha_creacion: '', fecha_adquisicion: '1969-03-01', porcentaje_conservacion: 90, categoria_id: 3, artista_id: null, sala_exhibicion_id: 6 },
    { id: 7, nombre: 'Pectoral Calima', descripcion: 'Pectoral con rostro humano, tiene corrosión en los bordes.', url_imagen: '', fecha_creacion: '', fecha_adquisicion: '1958-07-10', porcentaje_conservacion: 47, categoria_id: 3, artista_id: null, sala_exhibicion_id: 5 }
  ],

  // el periodo sin fecha_fin es la sala donde está el objeto ahora
  historial: [
    { id: 1, objeto_id: 1, sala_exhibicion_id: 1, fecha_inicio: '2018-03-01T09:00', fecha_fin: null },
    { id: 2, objeto_id: 2, sala_exhibicion_id: 3, fecha_inicio: '2019-04-09T09:00', fecha_fin: '2019-10-31T18:00' },
    { id: 3, objeto_id: 2, sala_exhibicion_id: 2, fecha_inicio: '2019-11-05T09:00', fecha_fin: null },
    { id: 4, objeto_id: 3, sala_exhibicion_id: 1, fecha_inicio: '2017-02-01T09:00', fecha_fin: '2025-12-31T18:00' },
    { id: 5, objeto_id: 3, sala_exhibicion_id: 3, fecha_inicio: '2026-01-15T09:00', fecha_fin: null },
    { id: 6, objeto_id: 4, sala_exhibicion_id: 3, fecha_inicio: '2021-06-01T09:00', fecha_fin: '2021-12-15T18:00' },
    { id: 7, objeto_id: 4, sala_exhibicion_id: 1, fecha_inicio: '2022-01-10T09:00', fecha_fin: null },
    { id: 8, objeto_id: 5, sala_exhibicion_id: 5, fecha_inicio: '2015-08-01T09:00', fecha_fin: '2020-02-28T18:00' },
    { id: 9, objeto_id: 5, sala_exhibicion_id: 4, fecha_inicio: '2020-03-02T09:00', fecha_fin: null },
    { id: 10, objeto_id: 6, sala_exhibicion_id: 6, fecha_inicio: '2008-05-01T09:00', fecha_fin: null },
    { id: 11, objeto_id: 7, sala_exhibicion_id: 4, fecha_inicio: '2012-01-15T09:00', fecha_fin: '2024-06-30T18:00' },
    { id: 12, objeto_id: 7, sala_exhibicion_id: 5, fecha_inicio: '2024-07-08T09:00', fecha_fin: null }
  ],

  recorridos: [
    { id: 1, nombre: 'Arte colombiano del siglo XX', descripcion: 'Recorrido por las obras de Botero, Obregón y Arango.', fecha_inicio: '2026-12-01T09:00', fecha_fin: '2027-02-28T17:00', url_pagina: 'https://example.org/recorridos/arte-siglo-xx' },
    { id: 2, nombre: 'Ruta del oro prehispánico', descripcion: 'Recorrido por la orfebrería y los rituales prehispánicos.', fecha_inicio: '2026-11-20T10:00', fecha_fin: '2027-01-31T16:00', url_pagina: 'https://example.org/recorridos/ruta-del-oro' }
  ],

  recorrido_sala: [
    { recorrido_id: 1, sala_exhibicion_id: 3, numero_orden: 1 },
    { recorrido_id: 1, sala_exhibicion_id: 1, numero_orden: 2 },
    { recorrido_id: 1, sala_exhibicion_id: 2, numero_orden: 3 },
    { recorrido_id: 2, sala_exhibicion_id: 4, numero_orden: 1 },
    { recorrido_id: 2, sala_exhibicion_id: 5, numero_orden: 2 },
    { recorrido_id: 2, sala_exhibicion_id: 6, numero_orden: 3 }
  ]
};
