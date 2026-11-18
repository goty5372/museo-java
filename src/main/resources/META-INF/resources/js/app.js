// Sistema de administración de museos
// Conectado con Backend Quarkus (http://localhost:8080/api/datos) + MySQL (XAMPP) y respaldo en localStorage

const API_URL = 'http://localhost:8080/api/datos';
let datos = JSON.parse(localStorage.getItem('museos')) || copiarDatosIniciales();
let idEditando = null;      // id del registro que se esta editando (null = nuevo)
let salasRecorrido = [];    // salas que se van agregando al recorrido, en orden

function copiarDatosIniciales() {
  return JSON.parse(JSON.stringify(DATOS_INICIALES));
}

function guardar() {
  localStorage.setItem('museos', JSON.stringify(datos));
  fetch(API_URL, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  }).catch(() => {});
}

async function cargarDesdeBackend() {
  try {
    const res = await fetch(API_URL);
    if (res.ok) {
      const remoto = await res.json();
      if (remoto && remoto.museos) {
        datos = remoto;
        localStorage.setItem('museos', JSON.stringify(datos));
        actualizarTodo();
      }
    }
  } catch (e) {
    // Si el backend Quarkus aun no esta encendido, usa los datos locales
  }
}

function siguienteId(lista) {
  let mayor = 0;
  lista.forEach(item => {
    if (item.id > mayor) mayor = item.id;
  });
  return mayor + 1;
}

function buscarPorId(lista, id) {
  return lista.find(item => item.id == id);
}

// convierte 2024-05-10 en 10/05/2024
function formatoFecha(fecha) {
  if (!fecha) return 'Sin dato';
  const partes = fecha.substring(0, 10).split('-');
  return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function formatoFechaHora(fecha) {
  if (!fecha) return '';
  return formatoFecha(fecha) + ' ' + fecha.substring(11, 16);
}

// fecha y hora actual en el formato del input datetime-local
function ahora() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  const hora = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return d.getFullYear() + '-' + mes + '-' + dia + 'T' + hora + ':' + min;
}

function estadoConservacion(porcentaje) {
  if (porcentaje >= 80) return { clase: 'bueno', texto: 'Buen estado' };
  if (porcentaje >= 60) return { clase: 'regular', texto: 'Regular' };
  return { clase: 'riesgo', texto: 'En riesgo' };
}

function nombreSala(idSala) {
  const sala = buscarPorId(datos.salas, idSala);
  return sala ? sala.nombre : 'Sala eliminada';
}

function nombreMuseoDeSala(idSala) {
  const sala = buscarPorId(datos.salas, idSala);
  if (!sala) return '';
  const museo = buscarPorId(datos.museos, sala.museo_id);
  return museo ? museo.nombre : '';
}


// ---------------- menu ----------------

const botonesMenu = document.querySelectorAll('.menu button');
const secciones = document.querySelectorAll('.seccion');

botonesMenu.forEach(boton => {
  boton.addEventListener('click', () => {
    mostrarSeccion(boton.dataset.seccion);
  });
});

function mostrarSeccion(id) {
  secciones.forEach(s => s.classList.remove('activa'));
  botonesMenu.forEach(b => b.classList.remove('activo'));
  document.getElementById(id).classList.add('activa');
  document.querySelector('.menu button[data-seccion="' + id + '"]').classList.add('activo');
  window.scrollTo(0, 0);
}


// ---------------- selects ----------------

// llena los select de filtros y de los formularios con los datos actuales
function llenarSelects() {
  let museos = '';
  datos.museos.forEach(m => {
    museos += `<option value="${m.id}">${m.nombre}</option>`;
  });

  let categorias = '';
  datos.categorias.forEach(c => {
    categorias += `<option value="${c.id}">${c.nombre}</option>`;
  });

  let artistas = '';
  datos.artistas.forEach(a => {
    artistas += `<option value="${a.id}">${a.nombre}</option>`;
  });

  let objetos = '';
  datos.objetos.forEach(o => {
    objetos += `<option value="${o.id}">${o.nombre}</option>`;
  });

  // las salas se agrupan por museo
  let salas = '';
  datos.museos.forEach(m => {
    salas += `<optgroup label="${m.nombre}">`;
    datos.salas.filter(s => s.museo_id == m.id).forEach(s => {
      salas += `<option value="${s.id}">${s.nombre}</option>`;
    });
    salas += '</optgroup>';
  });

  // filtros (se guarda lo que estaba seleccionado)
  cambiarOpciones('filtro-sala-museo', '<option value="">Todos</option>' + museos);
  cambiarOpciones('filtro-museo', '<option value="">Todos</option>' + museos);
  cambiarOpciones('filtro-categoria', '<option value="">Todas</option>' + categorias);
  cambiarOpciones('filtro-historial', '<option value="">Todos los objetos</option>' + objetos);

  // formularios
  document.getElementById('sala-museo').innerHTML = '<option value="">Seleccione...</option>' + museos;
  document.getElementById('objeto-categoria').innerHTML = '<option value="">Seleccione...</option>' + categorias;
  document.getElementById('objeto-artista').innerHTML = '<option value="">Anónimo / desconocido</option>' + artistas;
  document.getElementById('objeto-sala').innerHTML = '<option value="">Seleccione...</option>' + salas;
  document.getElementById('traslado-objeto').innerHTML = objetos;
  document.getElementById('traslado-sala').innerHTML = salas;
  document.getElementById('recorrido-sala').innerHTML = salas;
}

function cambiarOpciones(idSelect, html) {
  const select = document.getElementById(idSelect);
  const valorAnterior = select.value;
  select.innerHTML = html;
  select.value = valorAnterior;
  if (select.value != valorAnterior) select.value = '';
}


// ---------------- inicio ----------------

function mostrarInicio() {
  let suma = 0;
  datos.objetos.forEach(o => suma += o.porcentaje_conservacion);
  const promedio = datos.objetos.length > 0 ? Math.round(suma / datos.objetos.length) : 0;

  const hoy = ahora();
  const activos = datos.recorridos.filter(r => r.fecha_inicio <= hoy && r.fecha_fin >= hoy).length;

  document.getElementById('resumen').innerHTML = `
    <div class="dato"><span class="numero">${datos.museos.length}</span> Museos</div>
    <div class="dato"><span class="numero">${datos.salas.length}</span> Salas</div>
    <div class="dato"><span class="numero">${datos.objetos.length}</span> Objetos</div>
    <div class="dato"><span class="numero">${datos.artistas.length}</span> Artistas</div>
    <div class="dato"><span class="numero">${promedio}%</span> Conservación promedio</div>
    <div class="dato"><span class="numero">${activos}</span> Recorridos activos</div>
  `;

  const enRiesgo = datos.objetos.filter(o => o.porcentaje_conservacion < 60);
  let html = '';
  enRiesgo.forEach(o => {
    html += `
      <li>
        <strong>${o.nombre}</strong>
        <span class="etiqueta riesgo">${o.porcentaje_conservacion}%</span>
        <small>${nombreSala(o.sala_exhibicion_id)}, ${nombreMuseoDeSala(o.sala_exhibicion_id)}</small>
      </li>`;
  });
  document.getElementById('lista-riesgo').innerHTML = html || '<li>No hay objetos en riesgo.</li>';
}


// ---------------- museos ----------------

function mostrarMuseos() {
  const texto = document.getElementById('buscar-museo').value.toLowerCase();
  const lista = datos.museos.filter(m => m.nombre.toLowerCase().includes(texto));
  let html = '';

  lista.forEach(m => {
    const salas = datos.salas.filter(s => s.museo_id == m.id);
    let cantidadObjetos = 0;
    salas.forEach(s => {
      cantidadObjetos += datos.objetos.filter(o => o.sala_exhibicion_id == s.id).length;
    });

    let ubicacion = '<p class="gris">Sin coordenadas</p>';
    if (m.latitud != null && m.longitud != null) {
      const lat = Number(m.latitud);
      const lon = Number(m.longitud);
      ubicacion = `
        <div class="coordenadas">
          <div><span>Latitud</span><strong>${Math.abs(lat).toFixed(4)}° ${lat >= 0 ? 'N' : 'S'}</strong></div>
          <div><span>Longitud</span><strong>${Math.abs(lon).toFixed(4)}° ${lon >= 0 ? 'E' : 'O'}</strong></div>
          <a href="https://www.google.com/maps?q=${lat},${lon}" target="_blank">Ver en el mapa</a>
        </div>`;
    }

    html += `
      <article class="tarjeta">
        <h2>${m.nombre}</h2>
        <p class="gris">${m.descripcion || ''}</p>
        <div class="numeros">
          <div><span>Salas</span><strong>${salas.length}</strong></div>
          <div><span>Objetos</span><strong>${cantidadObjetos}</strong></div>
        </div>
        ${ubicacion}
        ${m.url_pagina ? `<a href="${m.url_pagina}" target="_blank">Página principal</a>` : ''}
        <div class="acciones">
          <button class="boton-borde" onclick="editarMuseo(${m.id})">Editar</button>
          <button class="boton-eliminar" onclick="eliminarMuseo(${m.id})">Eliminar</button>
        </div>
      </article>`;
  });

  document.getElementById('lista-museos').innerHTML = html || '<p class="vacio">No se encontraron museos.</p>';
}

function nuevoMuseo() {
  idEditando = null;
  document.getElementById('form-museo').reset();
  document.getElementById('titulo-museo').textContent = 'Nuevo museo';
  document.getElementById('modal-museo').showModal();
}

function editarMuseo(id) {
  const m = buscarPorId(datos.museos, id);
  idEditando = id;
  document.getElementById('titulo-museo').textContent = 'Editar museo';
  document.getElementById('museo-nombre').value = m.nombre;
  document.getElementById('museo-descripcion').value = m.descripcion || '';
  document.getElementById('museo-latitud').value = m.latitud != null ? m.latitud : '';
  document.getElementById('museo-longitud').value = m.longitud != null ? m.longitud : '';
  document.getElementById('museo-url').value = m.url_pagina || '';
  document.getElementById('modal-museo').showModal();
}

document.getElementById('form-museo').addEventListener('submit', function (e) {
  e.preventDefault();
  const lat = document.getElementById('museo-latitud').value;
  const lon = document.getElementById('museo-longitud').value;

  const museo = {
    nombre: document.getElementById('museo-nombre').value.trim(),
    descripcion: document.getElementById('museo-descripcion').value.trim(),
    latitud: lat === '' ? null : Number(lat),
    longitud: lon === '' ? null : Number(lon),
    url_pagina: document.getElementById('museo-url').value.trim()
  };

  if (idEditando) {
    Object.assign(buscarPorId(datos.museos, idEditando), museo);
  } else {
    museo.id = siguienteId(datos.museos);
    datos.museos.push(museo);
  }
  terminarGuardado('modal-museo');
});

function eliminarMuseo(id) {
  const museo = buscarPorId(datos.museos, id);
  const idsSalas = datos.salas.filter(s => s.museo_id == id).map(s => s.id);

  if (salaEnUso(idsSalas)) {
    alert('No se puede eliminar el museo porque sus salas tienen objetos, historial o recorridos.');
    return;
  }
  if (!confirm('¿Eliminar el museo "' + museo.nombre + '"? También se eliminan sus salas.')) return;

  datos.salas = datos.salas.filter(s => s.museo_id != id);
  datos.museos = datos.museos.filter(m => m.id != id);
  guardar();
  actualizarTodo();
}


// ---------------- salas ----------------

function mostrarSalas() {
  const museo = document.getElementById('filtro-sala-museo').value;
  const texto = document.getElementById('buscar-sala').value.toLowerCase();

  // para la barra de aforo se compara con la sala mas grande
  let mayorCapacidad = 1;
  datos.salas.forEach(s => {
    if (s.capacidad > mayorCapacidad) mayorCapacidad = s.capacidad;
  });

  let html = '';
  datos.salas.forEach(s => {
    if (museo && s.museo_id != museo) return;
    if (!s.nombre.toLowerCase().includes(texto)) return;

    const objetos = datos.objetos.filter(o => o.sala_exhibicion_id == s.id).length;
    const recorridos = datos.recorrido_sala.filter(r => r.sala_exhibicion_id == s.id).length;
    const ancho = Math.round(s.capacidad / mayorCapacidad * 100);

    html += `
      <article class="tarjeta">
        <p class="categoria">${nombreMuseoDeSala(s.id)}</p>
        <h2>${s.nombre}</h2>
        <p class="gris">${s.descripcion || ''}</p>
        <div class="fila-barra"><span>Aforo</span><strong>${s.capacidad} personas</strong></div>
        <div class="barra"><div style="width: ${ancho}%"></div></div>
        <div class="numeros">
          <div><span>Objetos</span><strong>${objetos}</strong></div>
          <div><span>En recorridos</span><strong>${recorridos}</strong></div>
        </div>
        <div class="acciones">
          <button class="boton-borde" onclick="editarSala(${s.id})">Editar</button>
          <button class="boton-eliminar" onclick="eliminarSala(${s.id})">Eliminar</button>
        </div>
      </article>`;
  });

  document.getElementById('lista-salas').innerHTML = html || '<p class="vacio">No hay salas con esos filtros.</p>';
}

function nuevaSala() {
  idEditando = null;
  document.getElementById('form-sala').reset();
  document.getElementById('titulo-sala').textContent = 'Nueva sala';
  document.getElementById('modal-sala').showModal();
}

function editarSala(id) {
  const s = buscarPorId(datos.salas, id);
  idEditando = id;
  document.getElementById('titulo-sala').textContent = 'Editar sala';
  document.getElementById('sala-museo').value = s.museo_id;
  document.getElementById('sala-nombre').value = s.nombre;
  document.getElementById('sala-descripcion').value = s.descripcion || '';
  document.getElementById('sala-capacidad').value = s.capacidad;
  document.getElementById('modal-sala').showModal();
}

document.getElementById('form-sala').addEventListener('submit', function (e) {
  e.preventDefault();
  const sala = {
    museo_id: Number(document.getElementById('sala-museo').value),
    nombre: document.getElementById('sala-nombre').value.trim(),
    descripcion: document.getElementById('sala-descripcion').value.trim(),
    capacidad: Number(document.getElementById('sala-capacidad').value)
  };

  if (idEditando) {
    Object.assign(buscarPorId(datos.salas, idEditando), sala);
  } else {
    sala.id = siguienteId(datos.salas);
    datos.salas.push(sala);
  }
  terminarGuardado('modal-sala');
});

// una sala esta en uso si tiene objetos, historial o esta en algun recorrido
function salaEnUso(ids) {
  return datos.objetos.some(o => ids.includes(o.sala_exhibicion_id)) ||
         datos.historial.some(h => ids.includes(h.sala_exhibicion_id)) ||
         datos.recorrido_sala.some(r => ids.includes(r.sala_exhibicion_id));
}

function eliminarSala(id) {
  const sala = buscarPorId(datos.salas, id);
  if (salaEnUso([id])) {
    alert('No se puede eliminar la sala porque tiene objetos, historial o está en un recorrido.');
    return;
  }
  if (!confirm('¿Eliminar la sala "' + sala.nombre + '"?')) return;

  datos.salas = datos.salas.filter(s => s.id != id);
  guardar();
  actualizarTodo();
}


// ---------------- objetos ----------------

function mostrarObjetos() {
  const texto = document.getElementById('buscar-objeto').value.toLowerCase();
  const categoria = document.getElementById('filtro-categoria').value;
  const museo = document.getElementById('filtro-museo').value;
  const estado = document.getElementById('filtro-estado').value;

  const lista = datos.objetos.filter(o => {
    const artista = buscarPorId(datos.artistas, o.artista_id);
    const sala = buscarPorId(datos.salas, o.sala_exhibicion_id);
    const nombreArtista = artista ? artista.nombre : 'anónimo';

    if (!o.nombre.toLowerCase().includes(texto) && !nombreArtista.toLowerCase().includes(texto)) return false;
    if (categoria && o.categoria_id != categoria) return false;
    if (museo && (!sala || sala.museo_id != museo)) return false;
    if (estado && estadoConservacion(o.porcentaje_conservacion).clase != estado) return false;
    return true;
  });

  document.getElementById('contador-objetos').textContent =
    'Mostrando ' + lista.length + ' de ' + datos.objetos.length + ' objetos';

  let html = '';
  lista.forEach(o => {
    const categoria = buscarPorId(datos.categorias, o.categoria_id);
    const artista = buscarPorId(datos.artistas, o.artista_id);
    const estado = estadoConservacion(o.porcentaje_conservacion);
    const nombreCategoria = categoria ? categoria.nombre : 'Sin categoría';

    // si no hay imagen (o no carga) se ve un marco con la inicial de la categoria
    let imagen = `<span class="inicial">${nombreCategoria.charAt(0)}</span>`;
    if (o.url_imagen) {
      imagen += `<img src="${o.url_imagen}" alt="${o.nombre}" onerror="this.remove()">`;
    }

    html += `
      <article class="tarjeta obra">
        <div class="imagen color-${o.categoria_id % 3}">${imagen}</div>
        <div class="obra-info">
          <p class="categoria">${nombreCategoria}</p>
          <h2>${o.nombre}</h2>
          <p class="autor">${artista ? artista.nombre : 'Anónimo'}</p>
          <p class="gris">${o.descripcion || ''}</p>
          <div class="fila-barra">
            <span class="etiqueta ${estado.clase}">${estado.texto}</span>
            <span>Conservación: <strong>${o.porcentaje_conservacion}%</strong></span>
          </div>
          <div class="barra"><div class="${estado.clase}" style="width: ${o.porcentaje_conservacion}%"></div></div>
          <div class="ficha">
            <div><span>Creación</span>${formatoFecha(o.fecha_creacion)}</div>
            <div><span>Adquisición</span>${formatoFecha(o.fecha_adquisicion)}</div>
            <div class="completo"><span>Ubicación actual</span>${nombreSala(o.sala_exhibicion_id)}, ${nombreMuseoDeSala(o.sala_exhibicion_id)}</div>
          </div>
          <div class="acciones">
            <button class="boton-borde" onclick="editarObjeto(${o.id})">Editar</button>
            <button class="boton-borde" onclick="verHistorial(${o.id})">Historial</button>
            <button class="boton-eliminar" onclick="eliminarObjeto(${o.id})">Eliminar</button>
          </div>
        </div>
      </article>`;
  });

  document.getElementById('lista-objetos').innerHTML = html || '<p class="vacio">Ningún objeto coincide con los filtros.</p>';
}

function nuevoObjeto() {
  idEditando = null;
  document.getElementById('form-objeto').reset();
  document.getElementById('titulo-objeto').textContent = 'Nuevo objeto';
  document.getElementById('modal-objeto').showModal();
}

function editarObjeto(id) {
  const o = buscarPorId(datos.objetos, id);
  idEditando = id;
  document.getElementById('titulo-objeto').textContent = 'Editar objeto';
  document.getElementById('objeto-nombre').value = o.nombre;
  document.getElementById('objeto-descripcion').value = o.descripcion || '';
  document.getElementById('objeto-imagen').value = o.url_imagen || '';
  document.getElementById('objeto-creacion').value = o.fecha_creacion || '';
  document.getElementById('objeto-adquisicion').value = o.fecha_adquisicion || '';
  document.getElementById('objeto-conservacion').value = o.porcentaje_conservacion;
  document.getElementById('objeto-categoria').value = o.categoria_id;
  document.getElementById('objeto-artista').value = o.artista_id || '';
  document.getElementById('objeto-sala').value = o.sala_exhibicion_id;
  document.getElementById('modal-objeto').showModal();
}

document.getElementById('form-objeto').addEventListener('submit', function (e) {
  e.preventDefault();
  const creacion = document.getElementById('objeto-creacion').value;
  const adquisicion = document.getElementById('objeto-adquisicion').value;
  const artista = document.getElementById('objeto-artista').value;

  if (creacion && adquisicion && adquisicion < creacion) {
    alert('La fecha de adquisición no puede ser anterior a la fecha de creación.');
    return;
  }

  const objeto = {
    nombre: document.getElementById('objeto-nombre').value.trim(),
    descripcion: document.getElementById('objeto-descripcion').value.trim(),
    url_imagen: document.getElementById('objeto-imagen').value.trim(),
    fecha_creacion: creacion,
    fecha_adquisicion: adquisicion,
    porcentaje_conservacion: Number(document.getElementById('objeto-conservacion').value),
    categoria_id: Number(document.getElementById('objeto-categoria').value),
    artista_id: artista ? Number(artista) : null,
    sala_exhibicion_id: Number(document.getElementById('objeto-sala').value)
  };

  if (idEditando) {
    const actual = buscarPorId(datos.objetos, idEditando);
    // si le cambiaron la sala, el cambio queda en el historial
    if (actual.sala_exhibicion_id != objeto.sala_exhibicion_id) {
      moverObjeto(actual.id, objeto.sala_exhibicion_id, ahora());
    }
    Object.assign(actual, objeto);
  } else {
    objeto.id = siguienteId(datos.objetos);
    datos.objetos.push(objeto);
    datos.historial.push({
      id: siguienteId(datos.historial),
      objeto_id: objeto.id,
      sala_exhibicion_id: objeto.sala_exhibicion_id,
      fecha_inicio: ahora(),
      fecha_fin: null
    });
  }
  terminarGuardado('modal-objeto');
});

function eliminarObjeto(id) {
  const objeto = buscarPorId(datos.objetos, id);
  if (!confirm('¿Eliminar "' + objeto.nombre + '"? También se borra su historial.')) return;

  datos.historial = datos.historial.filter(h => h.objeto_id != id);
  datos.objetos = datos.objetos.filter(o => o.id != id);
  guardar();
  actualizarTodo();
}

function verHistorial(id) {
  document.getElementById('filtro-historial').value = id;
  mostrarHistorial();
  mostrarSeccion('historial');
}


// ---------------- categorias ----------------

function mostrarCategorias() {
  let html = '';
  datos.categorias.forEach(c => {
    const cantidad = datos.objetos.filter(o => o.categoria_id == c.id).length;
    html += `
      <tr>
        <td><strong>${c.nombre}</strong></td>
        <td>${c.descripcion || ''}</td>
        <td>${cantidad}</td>
        <td class="botones-tabla">
          <button class="boton-borde" onclick="editarCategoria(${c.id})">Editar</button>
          <button class="boton-eliminar" onclick="eliminarCategoria(${c.id})">Eliminar</button>
        </td>
      </tr>`;
  });
  document.getElementById('tabla-categorias').innerHTML = html || '<tr><td colspan="4">No hay categorías.</td></tr>';
}

function nuevaCategoria() {
  idEditando = null;
  document.getElementById('form-categoria').reset();
  document.getElementById('titulo-categoria').textContent = 'Nueva categoría';
  document.getElementById('modal-categoria').showModal();
}

function editarCategoria(id) {
  const c = buscarPorId(datos.categorias, id);
  idEditando = id;
  document.getElementById('titulo-categoria').textContent = 'Editar categoría';
  document.getElementById('categoria-nombre').value = c.nombre;
  document.getElementById('categoria-descripcion').value = c.descripcion || '';
  document.getElementById('modal-categoria').showModal();
}

document.getElementById('form-categoria').addEventListener('submit', function (e) {
  e.preventDefault();
  const nombre = document.getElementById('categoria-nombre').value.trim();

  // no se permiten dos categorias con el mismo nombre
  const repetida = datos.categorias.find(c => c.nombre.toLowerCase() == nombre.toLowerCase() && c.id != idEditando);
  if (repetida) {
    alert('Ya existe una categoría con ese nombre.');
    return;
  }

  const categoria = {
    nombre: nombre,
    descripcion: document.getElementById('categoria-descripcion').value.trim()
  };

  if (idEditando) {
    Object.assign(buscarPorId(datos.categorias, idEditando), categoria);
  } else {
    categoria.id = siguienteId(datos.categorias);
    datos.categorias.push(categoria);
  }
  terminarGuardado('modal-categoria');
});

function eliminarCategoria(id) {
  const categoria = buscarPorId(datos.categorias, id);
  if (datos.objetos.some(o => o.categoria_id == id)) {
    alert('No se puede eliminar la categoría porque tiene objetos.');
    return;
  }
  if (!confirm('¿Eliminar la categoría "' + categoria.nombre + '"?')) return;

  datos.categorias = datos.categorias.filter(c => c.id != id);
  guardar();
  actualizarTodo();
}


// ---------------- artistas ----------------

function mostrarArtistas() {
  const texto = document.getElementById('buscar-artista').value.toLowerCase();
  let html = '';

  datos.artistas.forEach(a => {
    const buscar = (a.nombre + ' ' + (a.nacionalidad || '')).toLowerCase();
    if (!buscar.includes(texto)) return;

    const obras = datos.objetos.filter(o => o.artista_id == a.id).map(o => o.nombre);
    html += `
      <article class="tarjeta">
        <p class="categoria">${a.nacionalidad || 'Sin nacionalidad'}</p>
        <h2>${a.nombre}</h2>
        <p class="gris">${a.biografia || ''}</p>
        <p><strong>Obras (${obras.length}):</strong> ${obras.length > 0 ? obras.join(', ') : 'ninguna'}</p>
        <div class="acciones">
          <button class="boton-borde" onclick="editarArtista(${a.id})">Editar</button>
          <button class="boton-eliminar" onclick="eliminarArtista(${a.id})">Eliminar</button>
        </div>
      </article>`;
  });

  document.getElementById('lista-artistas').innerHTML = html || '<p class="vacio">No se encontraron artistas.</p>';
}

function nuevoArtista() {
  idEditando = null;
  document.getElementById('form-artista').reset();
  document.getElementById('titulo-artista').textContent = 'Nuevo artista';
  document.getElementById('modal-artista').showModal();
}

function editarArtista(id) {
  const a = buscarPorId(datos.artistas, id);
  idEditando = id;
  document.getElementById('titulo-artista').textContent = 'Editar artista';
  document.getElementById('artista-nombre').value = a.nombre;
  document.getElementById('artista-nacionalidad').value = a.nacionalidad || '';
  document.getElementById('artista-biografia').value = a.biografia || '';
  document.getElementById('modal-artista').showModal();
}

document.getElementById('form-artista').addEventListener('submit', function (e) {
  e.preventDefault();
  const artista = {
    nombre: document.getElementById('artista-nombre').value.trim(),
    nacionalidad: document.getElementById('artista-nacionalidad').value.trim(),
    biografia: document.getElementById('artista-biografia').value.trim()
  };

  if (idEditando) {
    Object.assign(buscarPorId(datos.artistas, idEditando), artista);
  } else {
    artista.id = siguienteId(datos.artistas);
    datos.artistas.push(artista);
  }
  terminarGuardado('modal-artista');
});

// si se borra un artista sus obras quedan como anonimas (igual que ON DELETE SET NULL)
function eliminarArtista(id) {
  const artista = buscarPorId(datos.artistas, id);
  const obras = datos.objetos.filter(o => o.artista_id == id);
  let mensaje = '¿Eliminar a "' + artista.nombre + '"?';
  if (obras.length > 0) mensaje += ' Sus ' + obras.length + ' obras quedarán como anónimas.';
  if (!confirm(mensaje)) return;

  obras.forEach(o => o.artista_id = null);
  datos.artistas = datos.artistas.filter(a => a.id != id);
  guardar();
  actualizarTodo();
}


// ---------------- historial ----------------

function mostrarHistorial() {
  const idObjeto = document.getElementById('filtro-historial').value;
  let lista = datos.historial;
  if (idObjeto) lista = lista.filter(h => h.objeto_id == idObjeto);

  // ordenado por objeto y luego por fecha
  lista = lista.slice().sort((a, b) => {
    const objA = buscarPorId(datos.objetos, a.objeto_id);
    const objB = buscarPorId(datos.objetos, b.objeto_id);
    if (a.objeto_id != b.objeto_id) return objA.nombre.localeCompare(objB.nombre);
    return a.fecha_inicio.localeCompare(b.fecha_inicio);
  });

  // cuando se escoge un objeto tambien se muestra la linea de tiempo
  let linea = '';
  if (idObjeto && lista.length > 0) {
    linea = '<ol class="linea-tiempo">';
    lista.forEach(h => {
      linea += `
        <li class="${h.fecha_fin ? '' : 'actual'}">
          <strong>${nombreSala(h.sala_exhibicion_id)}</strong>
          <span>${nombreMuseoDeSala(h.sala_exhibicion_id)} - del ${formatoFecha(h.fecha_inicio)} al ${h.fecha_fin ? formatoFecha(h.fecha_fin) : 'hoy (ubicación actual)'}</span>
        </li>`;
    });
    linea += '</ol>';
  }
  document.getElementById('linea-tiempo').innerHTML = linea;

  let html = '';
  lista.forEach(h => {
    const objeto = buscarPorId(datos.objetos, h.objeto_id);
    const fin = h.fecha_fin ? new Date(h.fecha_fin) : new Date();
    const dias = Math.floor((fin - new Date(h.fecha_inicio)) / (1000 * 60 * 60 * 24));

    html += `
      <tr>
        <td>${objeto.nombre}</td>
        <td>${nombreSala(h.sala_exhibicion_id)}</td>
        <td>${nombreMuseoDeSala(h.sala_exhibicion_id)}</td>
        <td>${formatoFechaHora(h.fecha_inicio)}</td>
        <td>${h.fecha_fin ? formatoFechaHora(h.fecha_fin) : '<span class="etiqueta bueno">Actual</span>'}</td>
        <td>${dias}</td>
      </tr>`;
  });
  document.getElementById('tabla-historial').innerHTML = html || '<tr><td colspan="6">No hay registros.</td></tr>';
}

// cierra el periodo actual del objeto y abre uno nuevo en la otra sala
function moverObjeto(idObjeto, idSala, fecha) {
  const objeto = buscarPorId(datos.objetos, idObjeto);
  const actual = datos.historial.find(h => h.objeto_id == idObjeto && !h.fecha_fin);

  if (actual && actual.fecha_inicio >= fecha) {
    // si el periodo empezo en ese mismo minuto solo se cambia la sala
    actual.sala_exhibicion_id = idSala;
  } else {
    if (actual) actual.fecha_fin = fecha;
    datos.historial.push({
      id: siguienteId(datos.historial),
      objeto_id: idObjeto,
      sala_exhibicion_id: idSala,
      fecha_inicio: fecha,
      fecha_fin: null
    });
  }
  objeto.sala_exhibicion_id = idSala;
}

function abrirTraslado() {
  if (datos.objetos.length == 0) {
    alert('Primero debe registrar un objeto.');
    return;
  }
  const filtro = document.getElementById('filtro-historial').value;
  document.getElementById('traslado-objeto').value = filtro || datos.objetos[0].id;
  document.getElementById('traslado-fecha').value = ahora();
  mostrarUbicacionActual();
  document.getElementById('modal-traslado').showModal();
}

function mostrarUbicacionActual() {
  const objeto = buscarPorId(datos.objetos, document.getElementById('traslado-objeto').value);
  document.getElementById('traslado-actual').textContent =
    'Ubicación actual: ' + nombreSala(objeto.sala_exhibicion_id) + ' (' + nombreMuseoDeSala(objeto.sala_exhibicion_id) + ')';
}

document.getElementById('traslado-objeto').addEventListener('change', mostrarUbicacionActual);

document.getElementById('form-traslado').addEventListener('submit', function (e) {
  e.preventDefault();
  const objeto = buscarPorId(datos.objetos, document.getElementById('traslado-objeto').value);
  const idSala = Number(document.getElementById('traslado-sala').value);
  const fecha = document.getElementById('traslado-fecha').value;
  const actual = datos.historial.find(h => h.objeto_id == objeto.id && !h.fecha_fin);

  if (idSala == objeto.sala_exhibicion_id) {
    alert('El objeto ya está en esa sala.');
    return;
  }
  if (actual && fecha <= actual.fecha_inicio) {
    alert('La fecha debe ser posterior al ' + formatoFechaHora(actual.fecha_inicio) + ', que es cuando llegó a la sala actual.');
    return;
  }

  moverObjeto(objeto.id, idSala, fecha);
  document.getElementById('filtro-historial').value = objeto.id;
  terminarGuardado('modal-traslado');
});


// ---------------- recorridos ----------------

function salasDelRecorrido(idRecorrido) {
  return datos.recorrido_sala
    .filter(r => r.recorrido_id == idRecorrido)
    .sort((a, b) => a.numero_orden - b.numero_orden)
    .map(r => r.sala_exhibicion_id);
}

function mostrarRecorridos() {
  const hoy = ahora();
  let html = '';

  datos.recorridos.forEach(r => {
    let estado = '<span class="etiqueta bueno">Activo</span>';
    if (hoy < r.fecha_inicio) estado = '<span class="etiqueta regular">Próximo</span>';
    if (hoy > r.fecha_fin) estado = '<span class="etiqueta terminado">Finalizado</span>';

    let salas = '';
    salasDelRecorrido(r.id).forEach(id => {
      salas += `<li>${nombreSala(id)} <small>(${nombreMuseoDeSala(id)})</small></li>`;
    });

    html += `
      <article class="tarjeta">
        <div>${estado}</div>
        <h2>${r.nombre}</h2>
        <p class="gris">${r.descripcion || ''}</p>
        <p>Del <strong>${formatoFechaHora(r.fecha_inicio)}</strong> al <strong>${formatoFechaHora(r.fecha_fin)}</strong></p>
        <p class="categoria">Orden de las salas</p>
        <ol class="salas-recorrido">${salas}</ol>
        ${r.url_pagina ? `<a href="${r.url_pagina}" target="_blank">Página del recorrido</a>` : ''}
        <div class="acciones">
          <button class="boton-borde" onclick="editarRecorrido(${r.id})">Editar</button>
          <button class="boton-eliminar" onclick="eliminarRecorrido(${r.id})">Eliminar</button>
        </div>
      </article>`;
  });

  document.getElementById('lista-recorridos').innerHTML = html || '<p class="vacio">Todavía no hay recorridos.</p>';
}

function nuevoRecorrido() {
  idEditando = null;
  salasRecorrido = [];
  document.getElementById('form-recorrido').reset();
  document.getElementById('titulo-recorrido').textContent = 'Nuevo recorrido';
  mostrarOrdenSalas();
  document.getElementById('modal-recorrido').showModal();
}

function editarRecorrido(id) {
  const r = buscarPorId(datos.recorridos, id);
  idEditando = id;
  salasRecorrido = salasDelRecorrido(id);
  document.getElementById('titulo-recorrido').textContent = 'Editar recorrido';
  document.getElementById('recorrido-nombre').value = r.nombre;
  document.getElementById('recorrido-descripcion').value = r.descripcion || '';
  document.getElementById('recorrido-inicio').value = r.fecha_inicio;
  document.getElementById('recorrido-fin').value = r.fecha_fin;
  document.getElementById('recorrido-url').value = r.url_pagina || '';
  mostrarOrdenSalas();
  document.getElementById('modal-recorrido').showModal();
}

// lista de salas del formulario con botones para subir, bajar o quitar
function mostrarOrdenSalas() {
  let html = '';
  salasRecorrido.forEach((idSala, i) => {
    html += `
      <li>
        <span class="numero-orden">${i + 1}</span>
        <span class="nombre-sala">${nombreSala(idSala)}<small>${nombreMuseoDeSala(idSala)}</small></span>
        <button type="button" onclick="subirSala(${i})" title="Subir" aria-label="Subir" ${i == 0 ? 'disabled' : ''}>&uarr;</button>
        <button type="button" onclick="bajarSala(${i})" title="Bajar" aria-label="Bajar" ${i == salasRecorrido.length - 1 ? 'disabled' : ''}>&darr;</button>
        <button type="button" onclick="quitarSala(${i})" title="Quitar" aria-label="Quitar">&times;</button>
      </li>`;
  });
  document.getElementById('orden-salas').innerHTML = html || '<li class="sin-salas">Agregue al menos una sala.</li>';
}

function agregarSalaRecorrido() {
  const idSala = Number(document.getElementById('recorrido-sala').value);
  if (salasRecorrido.includes(idSala)) {
    alert('Esa sala ya está en el recorrido.');
    return;
  }
  salasRecorrido.push(idSala);
  mostrarOrdenSalas();
}

function subirSala(i) {
  const temp = salasRecorrido[i - 1];
  salasRecorrido[i - 1] = salasRecorrido[i];
  salasRecorrido[i] = temp;
  mostrarOrdenSalas();
}

function bajarSala(i) {
  const temp = salasRecorrido[i + 1];
  salasRecorrido[i + 1] = salasRecorrido[i];
  salasRecorrido[i] = temp;
  mostrarOrdenSalas();
}

function quitarSala(i) {
  salasRecorrido.splice(i, 1);
  mostrarOrdenSalas();
}

document.getElementById('form-recorrido').addEventListener('submit', function (e) {
  e.preventDefault();
  const inicio = document.getElementById('recorrido-inicio').value;
  const fin = document.getElementById('recorrido-fin').value;

  if (fin <= inicio) {
    alert('La fecha de fin debe ser después de la fecha de inicio.');
    return;
  }
  if (salasRecorrido.length == 0) {
    alert('Agregue al menos una sala al recorrido.');
    return;
  }

  const recorrido = {
    nombre: document.getElementById('recorrido-nombre').value.trim(),
    descripcion: document.getElementById('recorrido-descripcion').value.trim(),
    fecha_inicio: inicio,
    fecha_fin: fin,
    url_pagina: document.getElementById('recorrido-url').value.trim()
  };

  let id = idEditando;
  if (idEditando) {
    Object.assign(buscarPorId(datos.recorridos, idEditando), recorrido);
  } else {
    id = siguienteId(datos.recorridos);
    recorrido.id = id;
    datos.recorridos.push(recorrido);
  }

  // se borra el orden anterior y se guarda el nuevo (numero_orden empieza en 1)
  datos.recorrido_sala = datos.recorrido_sala.filter(r => r.recorrido_id != id);
  salasRecorrido.forEach((idSala, i) => {
    datos.recorrido_sala.push({ recorrido_id: id, sala_exhibicion_id: idSala, numero_orden: i + 1 });
  });

  terminarGuardado('modal-recorrido');
});

function eliminarRecorrido(id) {
  const recorrido = buscarPorId(datos.recorridos, id);
  if (!confirm('¿Eliminar el recorrido "' + recorrido.nombre + '"?')) return;

  datos.recorrido_sala = datos.recorrido_sala.filter(r => r.recorrido_id != id);
  datos.recorridos = datos.recorridos.filter(r => r.id != id);
  guardar();
  actualizarTodo();
}


// ---------------- general ----------------

function terminarGuardado(idModal) {
  guardar();
  actualizarTodo();
  document.getElementById(idModal).close();
}

function actualizarTodo() {
  llenarSelects();
  mostrarInicio();
  mostrarMuseos();
  mostrarSalas();
  mostrarObjetos();
  mostrarCategorias();
  mostrarArtistas();
  mostrarHistorial();
  mostrarRecorridos();
}

function restaurarDatos() {
  if (!confirm('Se van a borrar los cambios y se cargan otra vez los datos de ejemplo. ¿Continuar?')) return;
  datos = copiarDatosIniciales();
  guardar();
  actualizarTodo();
}

// todos los botones de cancelar cierran su ventana
document.querySelectorAll('.cerrar-modal').forEach(boton => {
  boton.addEventListener('click', () => boton.closest('dialog').close());
});

// filtros en tiempo real
document.getElementById('buscar-museo').addEventListener('input', mostrarMuseos);
document.getElementById('buscar-sala').addEventListener('input', mostrarSalas);
document.getElementById('filtro-sala-museo').addEventListener('change', mostrarSalas);
document.getElementById('buscar-objeto').addEventListener('input', mostrarObjetos);
document.getElementById('filtro-categoria').addEventListener('change', mostrarObjetos);
document.getElementById('filtro-museo').addEventListener('change', mostrarObjetos);
document.getElementById('filtro-estado').addEventListener('change', mostrarObjetos);
document.getElementById('buscar-artista').addEventListener('input', mostrarArtistas);
document.getElementById('filtro-historial').addEventListener('change', mostrarHistorial);

actualizarTodo();
cargarDesdeBackend();
