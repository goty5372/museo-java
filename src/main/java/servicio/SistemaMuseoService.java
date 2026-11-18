package servicio;

import config.ConexionBD;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import modelo.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// clase que maneja todos los datos del sistema y sincroniza con MySQL (XAMPP) y la aplicacion web
@ApplicationScoped
public class SistemaMuseoService {

    private Map<Integer, Museo> museos = new HashMap<>();
    private Map<Integer, SalaExposicion> salas = new HashMap<>();
    private Map<Integer, Categoria> categorias = new HashMap<>();
    private Map<Integer, Artista> artistas = new HashMap<>();
    private Map<Integer, Obra> obras = new HashMap<>();
    private Map<Integer, RecorridoGuiado> recorridos = new HashMap<>();

    // para ir generando los id
    private int idMuseo = 1;
    private int idSala = 1;
    private int idCategoria = 1;
    private int idArtista = 1;
    private int idObra = 1;
    private int idRecorrido = 1;

    private boolean sincronizarBD = false;
    private static final DateTimeFormatter FMT_WEB = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // ---------- registrar ----------

    public Museo registrarMuseo(String nombre, String descripcion, Double latitud, Double longitud, String url) {
        Museo museo = new Museo(idMuseo, nombre, descripcion, latitud, longitud, url);
        museos.put(museo.getId(), museo);
        idMuseo++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO museo (id, nombre, descripcion, latitud, longitud, url_pagina) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, museo.getId());
                ps.setString(2, museo.getNombre());
                ps.setString(3, museo.getDescripcion());
                if (museo.getLatitud() != null) ps.setDouble(4, museo.getLatitud()); else ps.setNull(4, Types.DOUBLE);
                if (museo.getLongitud() != null) ps.setDouble(5, museo.getLongitud()); else ps.setNull(5, Types.DOUBLE);
                ps.setString(6, museo.getUrlPagina());
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
        }
        return museo;
    }

    public SalaExposicion registrarSala(int idDelMuseo, String nombre, String descripcion, int capacidad) {
        Museo museo = buscarMuseo(idDelMuseo);
        SalaExposicion sala = new SalaExposicion(idSala, nombre, descripcion, capacidad);
        museo.agregarSala(sala);
        salas.put(sala.getId(), sala);
        idSala++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO sala_exposicion (id, id_museo, nombre, descripcion, capacidad) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, sala.getId());
                ps.setInt(2, idDelMuseo);
                ps.setString(3, sala.getNombre());
                ps.setString(4, sala.getDescripcion());
                ps.setInt(5, sala.getCapacidad());
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
        }
        return sala;
    }

    public Categoria registrarCategoria(String nombre, String descripcion) {
        for (Categoria c : categorias.values()) {
            if (c.getNombre().equalsIgnoreCase(nombre.trim())) {
                throw new IllegalArgumentException("Ya existe una categoria llamada " + c.getNombre());
            }
        }
        Categoria categoria = new Categoria(idCategoria, nombre, descripcion);
        categorias.put(categoria.getId(), categoria);
        idCategoria++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO categoria (id, nombre, descripcion) VALUES (?, ?, ?)")) {
                ps.setInt(1, categoria.getId());
                ps.setString(2, categoria.getNombre());
                ps.setString(3, categoria.getDescripcion());
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
        }
        return categoria;
    }

    public Artista registrarArtista(String nombre, String nacionalidad, String biografia) {
        Artista artista = new Artista(idArtista, nombre, nacionalidad, biografia);
        artistas.put(artista.getId(), artista);
        idArtista++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO artista (id, nombre, nacionalidad, biografia) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, artista.getId());
                ps.setString(2, artista.getNombre());
                ps.setString(3, artista.getNacionalidad());
                ps.setString(4, artista.getBiografia());
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
        }
        return artista;
    }

    // idDelArtista puede ser null cuando la obra es anonima
    public Obra registrarObra(String nombre, String descripcion, String urlImagen,
                              LocalDate fechaCreacion, LocalDate fechaAdquisicion, int porcentaje,
                              int idDeCategoria, Integer idDelArtista, int idDeSala, LocalDateTime fechaIngreso) {
        Categoria categoria = buscarCategoria(idDeCategoria);
        Artista artista = idDelArtista == null ? null : buscarArtista(idDelArtista);
        SalaExposicion sala = buscarSala(idDeSala);

        Obra obra = new Obra(idObra, nombre, categoria, artista, sala, porcentaje, fechaIngreso);
        obra.setDescripcion(descripcion);
        obra.setUrlImagen(urlImagen);
        obra.setFechaCreacion(fechaCreacion);
        obra.setFechaAdquisicion(fechaAdquisicion);

        obras.put(obra.getId(), obra);
        idObra++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO obra (id, nombre, descripcion, url_imagen, fecha_creacion, fecha_adquisicion, porcentaje_conservacion, estado_conservacion, id_categoria, id_artista, id_sala_actual) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, obra.getId());
                    ps.setString(2, obra.getNombre());
                    ps.setString(3, obra.getDescripcion());
                    ps.setString(4, obra.getUrlImagen());
                    if (fechaCreacion != null) ps.setDate(5, Date.valueOf(fechaCreacion)); else ps.setNull(5, Types.DATE);
                    if (fechaAdquisicion != null) ps.setDate(6, Date.valueOf(fechaAdquisicion)); else ps.setNull(6, Types.DATE);
                    ps.setInt(7, obra.getPorcentajeConservacion());
                    ps.setString(8, obra.getEstadoConservacion().getNombre());
                    ps.setInt(9, idDeCategoria);
                    if (idDelArtista != null) ps.setInt(10, idDelArtista); else ps.setNull(10, Types.INTEGER);
                    ps.setInt(11, idDeSala);
                    ps.executeUpdate();
                }
                try (PreparedStatement psHist = conn.prepareStatement(
                        "INSERT INTO historial_exhibicion (id_obra, id_sala, fecha_entrada, fecha_salida) VALUES (?, ?, ?, NULL)")) {
                    psHist.setInt(1, obra.getId());
                    psHist.setInt(2, idDeSala);
                    psHist.setTimestamp(3, Timestamp.valueOf(fechaIngreso));
                    psHist.executeUpdate();
                }
            } catch (Exception ignored) {
            }
        }
        return obra;
    }

    public RecorridoGuiado crearRecorrido(String nombre, String descripcion,
                                          LocalDateTime inicio, LocalDateTime fin, String url) {
        RecorridoGuiado recorrido = new RecorridoGuiado(idRecorrido, nombre, descripcion, inicio, fin, url);
        recorridos.put(recorrido.getId(), recorrido);
        idRecorrido++;

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO recorrido_guiado (id, nombre, descripcion, fecha_inicio, fecha_fin, url_pagina) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, recorrido.getId());
                ps.setString(2, recorrido.getNombre());
                ps.setString(3, recorrido.getDescripcion());
                ps.setTimestamp(4, Timestamp.valueOf(inicio));
                ps.setTimestamp(5, Timestamp.valueOf(fin));
                ps.setString(6, recorrido.getUrlPagina());
                ps.executeUpdate();
            } catch (Exception ignored) {
            }
        }
        return recorrido;
    }

    // ---------- operaciones ----------

    public void moverObra(int idDeObra, int idDeSala, LocalDateTime fecha) {
        Obra obra = buscarObra(idDeObra);
        SalaExposicion sala = buscarSala(idDeSala);
        obra.moverASala(sala, fecha);

        if (sincronizarBD) {
            try (Connection conn = ConexionBD.obtenerConexion()) {
                try (PreparedStatement psClose = conn.prepareStatement(
                        "UPDATE historial_exhibicion SET fecha_salida = ? WHERE id_obra = ? AND fecha_salida IS NULL")) {
                    psClose.setTimestamp(1, Timestamp.valueOf(fecha));
                    psClose.setInt(2, idDeObra);
                    psClose.executeUpdate();
                }
                try (PreparedStatement psInsert = conn.prepareStatement(
                        "INSERT INTO historial_exhibicion (id_obra, id_sala, fecha_entrada, fecha_salida) VALUES (?, ?, ?, NULL)")) {
                    psInsert.setInt(1, idDeObra);
                    psInsert.setInt(2, idDeSala);
                    psInsert.setTimestamp(3, Timestamp.valueOf(fecha));
                    psInsert.executeUpdate();
                }
                try (PreparedStatement psUpdate = conn.prepareStatement(
                        "UPDATE obra SET id_sala_actual = ? WHERE id = ?")) {
                    psUpdate.setInt(1, idDeSala);
                    psUpdate.setInt(2, idDeObra);
                    psUpdate.executeUpdate();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void agregarSalaARecorrido(int idDelRecorrido, int idDeSala) {
        RecorridoGuiado recorrido = buscarRecorrido(idDelRecorrido);
        recorrido.agregarSala(buscarSala(idDeSala));
        sincronizarSalasRecorridoBD(recorrido);
    }

    public void cambiarOrdenRecorrido(int idDelRecorrido, int posicionActual, int nuevaPosicion) {
        RecorridoGuiado recorrido = buscarRecorrido(idDelRecorrido);
        recorrido.cambiarOrden(posicionActual, nuevaPosicion);
        sincronizarSalasRecorridoBD(recorrido);
    }

    public void quitarSalaDeRecorrido(int idDelRecorrido, int posicion) {
        RecorridoGuiado recorrido = buscarRecorrido(idDelRecorrido);
        recorrido.quitarSala(posicion);
        sincronizarSalasRecorridoBD(recorrido);
    }

    private void sincronizarSalasRecorridoBD(RecorridoGuiado recorrido) {
        if (!sincronizarBD) return;
        try (Connection conn = ConexionBD.obtenerConexion()) {
            try (PreparedStatement psDel = conn.prepareStatement(
                    "DELETE FROM detalle_recorrido_sala WHERE id_recorrido = ?")) {
                psDel.setInt(1, recorrido.getId());
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement(
                    "INSERT INTO detalle_recorrido_sala (id_recorrido, id_sala, numero_orden) VALUES (?, ?, ?)")) {
                for (DetalleRecorridoSala d : recorrido.getDetalles()) {
                    psIns.setInt(1, recorrido.getId());
                    psIns.setInt(2, d.getSala().getId());
                    psIns.setInt(3, d.getNumeroOrden());
                    psIns.executeUpdate();
                }
            }
        } catch (Exception ignored) {
        }
    }

    // ---------- buscar ----------

    public Museo buscarMuseo(int id) {
        Museo m = museos.get(id);
        if (m == null) throw new IllegalArgumentException("No existe un museo con id " + id);
        return m;
    }

    public SalaExposicion buscarSala(int id) {
        SalaExposicion s = salas.get(id);
        if (s == null) throw new IllegalArgumentException("No existe una sala con id " + id);
        return s;
    }

    public Categoria buscarCategoria(int id) {
        Categoria c = categorias.get(id);
        if (c == null) throw new IllegalArgumentException("No existe una categoria con id " + id);
        return c;
    }

    public Artista buscarArtista(int id) {
        Artista a = artistas.get(id);
        if (a == null) throw new IllegalArgumentException("No existe un artista con id " + id);
        return a;
    }

    public Obra buscarObra(int id) {
        Obra o = obras.get(id);
        if (o == null) throw new IllegalArgumentException("No existe una obra con id " + id);
        return o;
    }

    public RecorridoGuiado buscarRecorrido(int id) {
        RecorridoGuiado r = recorridos.get(id);
        if (r == null) throw new IllegalArgumentException("No existe un recorrido con id " + id);
        return r;
    }

    // ---------- listas (ordenadas por id) ----------

    public List<Museo> listarMuseos() {
        List<Museo> lista = new ArrayList<>(museos.values());
        lista.sort(Comparator.comparingInt(Museo::getId));
        return lista;
    }

    public List<SalaExposicion> listarSalas() {
        List<SalaExposicion> lista = new ArrayList<>(salas.values());
        lista.sort(Comparator.comparingInt(SalaExposicion::getId));
        return lista;
    }

    public List<Categoria> listarCategorias() {
        List<Categoria> lista = new ArrayList<>(categorias.values());
        lista.sort(Comparator.comparingInt(Categoria::getId));
        return lista;
    }

    public List<Artista> listarArtistas() {
        List<Artista> lista = new ArrayList<>(artistas.values());
        lista.sort(Comparator.comparingInt(Artista::getId));
        return lista;
    }

    public List<Obra> listarObras() {
        List<Obra> lista = new ArrayList<>(obras.values());
        lista.sort(Comparator.comparingInt(Obra::getId));
        return lista;
    }

    public List<RecorridoGuiado> listarRecorridos() {
        List<RecorridoGuiado> lista = new ArrayList<>(recorridos.values());
        lista.sort(Comparator.comparingInt(RecorridoGuiado::getId));
        return lista;
    }

    // ---------- reportes ----------

    public List<Obra> obrasEnSala(SalaExposicion sala) {
        List<Obra> lista = new ArrayList<>();
        for (Obra o : listarObras()) {
            if (o.getSalaActual().equals(sala)) {
                lista.add(o);
            }
        }
        return lista;
    }

    public Map<SalaExposicion, Double> promedioConservacionPorSala() {
        Map<SalaExposicion, Double> promedios = new LinkedHashMap<>();
        for (SalaExposicion sala : listarSalas()) {
            List<Obra> obrasSala = obrasEnSala(sala);
            if (obrasSala.isEmpty()) continue;
            double suma = 0;
            for (Obra o : obrasSala) {
                suma += o.getPorcentajeConservacion();
            }
            promedios.put(sala, suma / obrasSala.size());
        }
        return promedios;
    }

    public List<Obra> obrasPorEstado(EstadoConservacion estado) {
        List<Obra> lista = new ArrayList<>();
        for (Obra o : listarObras()) {
            if (o.getEstadoConservacion() == estado) {
                lista.add(o);
            }
        }
        return lista;
    }

    public List<Reportable> elementosReportables() {
        List<Reportable> lista = new ArrayList<>();
        lista.addAll(listarMuseos());
        lista.addAll(listarObras());
        lista.addAll(listarRecorridos());
        return lista;
    }

    // ---------- integracion con museos-app (Frontend Web) y MySQL (XAMPP) ----------

    public synchronized Map<String, Object> obtenerEstadoWeb() {
        recargarDesdeMySQL();
        Map<String, Object> res = new LinkedHashMap<>();

        List<Map<String, Object>> lMuseos = new ArrayList<>();
        for (Museo m : listarMuseos()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("nombre", m.getNombre());
            item.put("descripcion", m.getDescripcion());
            item.put("latitud", m.getLatitud());
            item.put("longitud", m.getLongitud());
            item.put("url_pagina", m.getUrlPagina());
            lMuseos.add(item);
        }
        res.put("museos", lMuseos);

        List<Map<String, Object>> lSalas = new ArrayList<>();
        for (SalaExposicion s : listarSalas()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("museo_id", s.getMuseo().getId());
            item.put("nombre", s.getNombre());
            item.put("descripcion", s.getDescripcion());
            item.put("capacidad", s.getCapacidad());
            lSalas.add(item);
        }
        res.put("salas", lSalas);

        List<Map<String, Object>> lCategorias = new ArrayList<>();
        for (Categoria c : listarCategorias()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("nombre", c.getNombre());
            item.put("descripcion", c.getDescripcion());
            lCategorias.add(item);
        }
        res.put("categorias", lCategorias);

        List<Map<String, Object>> lArtistas = new ArrayList<>();
        for (Artista a : listarArtistas()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("nombre", a.getNombre());
            item.put("nacionalidad", a.getNacionalidad());
            item.put("biografia", a.getBiografia());
            lArtistas.add(item);
        }
        res.put("artistas", lArtistas);

        List<Map<String, Object>> lObjetos = new ArrayList<>();
        List<Map<String, Object>> lHistorial = new ArrayList<>();
        for (Obra o : listarObras()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", o.getId());
            item.put("nombre", o.getNombre());
            item.put("descripcion", o.getDescripcion());
            item.put("url_imagen", o.getUrlImagen());
            item.put("fecha_creacion", o.getFechaCreacion() != null ? o.getFechaCreacion().toString() : "");
            item.put("fecha_adquisicion", o.getFechaAdquisicion() != null ? o.getFechaAdquisicion().toString() : "");
            item.put("porcentaje_conservacion", o.getPorcentajeConservacion());
            item.put("categoria_id", o.getCategoria().getId());
            item.put("artista_id", o.getArtista() != null ? o.getArtista().getId() : null);
            item.put("sala_exhibicion_id", o.getSalaActual().getId());
            lObjetos.add(item);

            for (HistorialExhibicion h : o.getHistorial()) {
                Map<String, Object> hItem = new LinkedHashMap<>();
                hItem.put("id", h.getId());
                hItem.put("objeto_id", o.getId());
                hItem.put("sala_exhibicion_id", h.getSala().getId());
                hItem.put("fecha_inicio", h.getFechaInicio().format(FMT_WEB));
                hItem.put("fecha_fin", h.getFechaFin() != null ? h.getFechaFin().format(FMT_WEB) : null);
                lHistorial.add(hItem);
            }
        }
        res.put("objetos", lObjetos);
        res.put("historial", lHistorial);

        List<Map<String, Object>> lRecorridos = new ArrayList<>();
        List<Map<String, Object>> lRecorridoSala = new ArrayList<>();
        for (RecorridoGuiado r : listarRecorridos()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("nombre", r.getNombre());
            item.put("descripcion", r.getDescripcion());
            item.put("fecha_inicio", r.getFechaInicio().format(FMT_WEB));
            item.put("fecha_fin", r.getFechaFin().format(FMT_WEB));
            item.put("url_pagina", r.getUrlPagina());
            lRecorridos.add(item);

            for (DetalleRecorridoSala d : r.getDetalles()) {
                Map<String, Object> dItem = new LinkedHashMap<>();
                dItem.put("recorrido_id", r.getId());
                dItem.put("sala_exhibicion_id", d.getSala().getId());
                dItem.put("numero_orden", d.getNumeroOrden());
                lRecorridoSala.add(dItem);
            }
        }
        res.put("recorridos", lRecorridos);
        res.put("recorrido_sala", lRecorridoSala);

        return res;
    }

    @SuppressWarnings("unchecked")
    public synchronized void guardarEstadoWeb(Map<String, Object> datos) {
        if (datos == null) return;

        List<Map<String, Object>> lMuseos = (List<Map<String, Object>>) datos.getOrDefault("museos", List.of());
        List<Map<String, Object>> lSalas = (List<Map<String, Object>>) datos.getOrDefault("salas", List.of());
        List<Map<String, Object>> lCategorias = (List<Map<String, Object>>) datos.getOrDefault("categorias", List.of());
        List<Map<String, Object>> lArtistas = (List<Map<String, Object>>) datos.getOrDefault("artistas", List.of());
        List<Map<String, Object>> lObjetos = (List<Map<String, Object>>) datos.getOrDefault("objetos", List.of());
        List<Map<String, Object>> lHistorial = (List<Map<String, Object>>) datos.getOrDefault("historial", List.of());
        List<Map<String, Object>> lRecorridos = (List<Map<String, Object>>) datos.getOrDefault("recorridos", List.of());
        List<Map<String, Object>> lRecorridoSala = (List<Map<String, Object>>) datos.getOrDefault("recorrido_sala", List.of());

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
                st.execute("TRUNCATE TABLE detalle_recorrido_sala");
                st.execute("TRUNCATE TABLE recorrido_guiado");
                st.execute("TRUNCATE TABLE historial_exhibicion");
                st.execute("TRUNCATE TABLE obra");
                st.execute("TRUNCATE TABLE artista");
                st.execute("TRUNCATE TABLE categoria");
                st.execute("TRUNCATE TABLE sala_exposicion");
                st.execute("TRUNCATE TABLE museo");
                st.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO museo (id, nombre, descripcion, latitud, longitud, url_pagina) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (Map<String, Object> m : lMuseos) {
                    ps.setInt(1, toInt(m.get("id")));
                    ps.setString(2, toStr(m.get("nombre")));
                    ps.setString(3, toStr(m.get("descripcion")));
                    Double lat = toDouble(m.get("latitud"));
                    Double lon = toDouble(m.get("longitud"));
                    if (lat != null) ps.setDouble(4, lat); else ps.setNull(4, Types.DOUBLE);
                    if (lon != null) ps.setDouble(5, lon); else ps.setNull(5, Types.DOUBLE);
                    ps.setString(6, toStr(m.get("url_pagina")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sala_exposicion (id, id_museo, nombre, descripcion, capacidad) VALUES (?, ?, ?, ?, ?)")) {
                for (Map<String, Object> s : lSalas) {
                    ps.setInt(1, toInt(s.get("id")));
                    ps.setInt(2, toInt(s.get("museo_id")));
                    ps.setString(3, toStr(s.get("nombre")));
                    ps.setString(4, toStr(s.get("descripcion")));
                    ps.setInt(5, toInt(s.get("capacidad")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO categoria (id, nombre, descripcion) VALUES (?, ?, ?)")) {
                for (Map<String, Object> c : lCategorias) {
                    ps.setInt(1, toInt(c.get("id")));
                    ps.setString(2, toStr(c.get("nombre")));
                    ps.setString(3, toStr(c.get("descripcion")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO artista (id, nombre, nacionalidad, biografia) VALUES (?, ?, ?, ?)")) {
                for (Map<String, Object> a : lArtistas) {
                    ps.setInt(1, toInt(a.get("id")));
                    ps.setString(2, toStr(a.get("nombre")));
                    ps.setString(3, toStr(a.get("nacionalidad")));
                    ps.setString(4, toStr(a.get("biografia")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO obra (id, nombre, descripcion, url_imagen, fecha_creacion, fecha_adquisicion, porcentaje_conservacion, estado_conservacion, id_categoria, id_artista, id_sala_actual) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Map<String, Object> o : lObjetos) {
                    int pct = toInt(o.get("porcentaje_conservacion"));
                    EstadoConservacion est = EstadoConservacion.segunPorcentaje(pct);
                    ps.setInt(1, toInt(o.get("id")));
                    ps.setString(2, toStr(o.get("nombre")));
                    ps.setString(3, toStr(o.get("descripcion")));
                    ps.setString(4, toStr(o.get("url_imagen")));
                    LocalDate fc = toLocalDate(o.get("fecha_creacion"));
                    LocalDate fa = toLocalDate(o.get("fecha_adquisicion"));
                    if (fc != null) ps.setDate(5, Date.valueOf(fc)); else ps.setNull(5, Types.DATE);
                    if (fa != null) ps.setDate(6, Date.valueOf(fa)); else ps.setNull(6, Types.DATE);
                    ps.setInt(7, pct);
                    ps.setString(8, est.getNombre());
                    ps.setInt(9, toInt(o.get("categoria_id")));
                    Integer idArt = toNullableInt(o.get("artista_id"));
                    if (idArt != null) ps.setInt(10, idArt); else ps.setNull(10, Types.INTEGER);
                    ps.setInt(11, toInt(o.get("sala_exhibicion_id")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO historial_exhibicion (id, id_obra, id_sala, fecha_entrada, fecha_salida) VALUES (?, ?, ?, ?, ?)")) {
                for (Map<String, Object> h : lHistorial) {
                    ps.setInt(1, toInt(h.get("id")));
                    ps.setInt(2, toInt(h.get("objeto_id")));
                    ps.setInt(3, toInt(h.get("sala_exhibicion_id")));
                    LocalDateTime fi = toLocalDateTime(h.get("fecha_inicio"));
                    LocalDateTime ff = toLocalDateTime(h.get("fecha_fin"));
                    ps.setTimestamp(4, Timestamp.valueOf(fi != null ? fi : LocalDateTime.now()));
                    if (ff != null) ps.setTimestamp(5, Timestamp.valueOf(ff)); else ps.setNull(5, Types.TIMESTAMP);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO recorrido_guiado (id, nombre, descripcion, fecha_inicio, fecha_fin, url_pagina) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (Map<String, Object> r : lRecorridos) {
                    ps.setInt(1, toInt(r.get("id")));
                    ps.setString(2, toStr(r.get("nombre")));
                    ps.setString(3, toStr(r.get("descripcion")));
                    LocalDateTime fi = toLocalDateTime(r.get("fecha_inicio"));
                    LocalDateTime ff = toLocalDateTime(r.get("fecha_fin"));
                    ps.setTimestamp(4, Timestamp.valueOf(fi != null ? fi : LocalDateTime.now()));
                    ps.setTimestamp(5, Timestamp.valueOf(ff != null ? ff : LocalDateTime.now().plusDays(1)));
                    ps.setString(6, toStr(r.get("url_pagina")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO detalle_recorrido_sala (id_recorrido, id_sala, numero_orden) VALUES (?, ?, ?)")) {
                for (Map<String, Object> d : lRecorridoSala) {
                    ps.setInt(1, toInt(d.get("recorrido_id")));
                    ps.setInt(2, toInt(d.get("sala_exhibicion_id")));
                    ps.setInt(3, toInt(d.get("numero_orden")));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (Exception ignored) {
        }

        recargarDesdeMySQL();
    }

    public synchronized void recargarDesdeMySQL() {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            Map<Integer, Museo> nMuseos = new HashMap<>();
            Map<Integer, SalaExposicion> nSalas = new HashMap<>();
            Map<Integer, Categoria> nCategorias = new HashMap<>();
            Map<Integer, Artista> nArtistas = new HashMap<>();
            Map<Integer, Obra> nObras = new HashMap<>();
            Map<Integer, RecorridoGuiado> nRecorridos = new HashMap<>();

            int maxMuseo = 0, maxSala = 0, maxCat = 0, maxArt = 0, maxObra = 0, maxRec = 0;

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, descripcion, latitud, longitud, url_pagina FROM museo ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Double lat = rs.getObject("latitud") != null ? rs.getDouble("latitud") : null;
                    Double lon = rs.getObject("longitud") != null ? rs.getDouble("longitud") : null;
                    Museo m = new Museo(id, rs.getString("nombre"), rs.getString("descripcion"), lat, lon, rs.getString("url_pagina"));
                    nMuseos.put(id, m);
                    if (id > maxMuseo) maxMuseo = id;
                }
            }

            if (nMuseos.isEmpty()) {
                return;
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, id_museo, nombre, descripcion, capacidad FROM sala_exposicion ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int idM = rs.getInt("id_museo");
                    SalaExposicion s = new SalaExposicion(id, rs.getString("nombre"), rs.getString("descripcion"), rs.getInt("capacidad"));
                    Museo m = nMuseos.get(idM);
                    if (m != null) {
                        m.agregarSala(s);
                        nSalas.put(id, s);
                        if (id > maxSala) maxSala = id;
                    }
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, descripcion FROM categoria ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Categoria c = new Categoria(id, rs.getString("nombre"), rs.getString("descripcion"));
                    nCategorias.put(id, c);
                    if (id > maxCat) maxCat = id;
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, nacionalidad, biografia FROM artista ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Artista a = new Artista(id, rs.getString("nombre"), rs.getString("nacionalidad"), rs.getString("biografia"));
                    nArtistas.put(id, a);
                    if (id > maxArt) maxArt = id;
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, descripcion, url_imagen, fecha_creacion, fecha_adquisicion, porcentaje_conservacion, id_categoria, id_artista, id_sala_actual FROM obra ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Categoria cat = nCategorias.get(rs.getInt("id_categoria"));
                    Integer idArt = rs.getObject("id_artista") != null ? rs.getInt("id_artista") : null;
                    Artista art = idArt != null ? nArtistas.get(idArt) : null;
                    SalaExposicion sala = nSalas.get(rs.getInt("id_sala_actual"));
                    if (cat != null && sala != null) {
                        Obra o = new Obra(id, rs.getString("nombre"), cat, art, sala, rs.getInt("porcentaje_conservacion"), LocalDateTime.now());
                        o.setDescripcion(rs.getString("descripcion"));
                        o.setUrlImagen(rs.getString("url_imagen"));
                        Date fc = rs.getDate("fecha_creacion");
                        Date fa = rs.getDate("fecha_adquisicion");
                        if (fc != null) o.setFechaCreacion(fc.toLocalDate());
                        if (fa != null) o.setFechaAdquisicion(fa.toLocalDate());
                        nObras.put(id, o);
                        if (id > maxObra) maxObra = id;
                    }
                }
            }

            Map<Integer, List<HistorialExhibicion>> histPorObra = new HashMap<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, id_obra, id_sala, fecha_entrada, fecha_salida FROM historial_exhibicion ORDER BY id_obra, fecha_entrada")) {
                while (rs.next()) {
                    int idHist = rs.getInt("id");
                    int idOb = rs.getInt("id_obra");
                    int idSa = rs.getInt("id_sala");
                    Obra ob = nObras.get(idOb);
                    SalaExposicion sa = nSalas.get(idSa);
                    Timestamp fe = rs.getTimestamp("fecha_entrada");
                    Timestamp fs = rs.getTimestamp("fecha_salida");
                    if (ob != null && sa != null && fe != null) {
                        HistorialExhibicion h = ob.crearRegistroHistorial(
                                idHist, sa, fe.toLocalDateTime(), fs != null ? fs.toLocalDateTime() : null);
                        histPorObra.computeIfAbsent(idOb, k -> new ArrayList<>()).add(h);
                    }
                }
            }
            for (Map.Entry<Integer, List<HistorialExhibicion>> entry : histPorObra.entrySet()) {
                Obra ob = nObras.get(entry.getKey());
                if (ob != null && !entry.getValue().isEmpty()) {
                    ob.reemplazarHistorial(entry.getValue(), ob.getSalaActual());
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre, descripcion, fecha_inicio, fecha_fin, url_pagina FROM recorrido_guiado ORDER BY id")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Timestamp fi = rs.getTimestamp("fecha_inicio");
                    Timestamp ff = rs.getTimestamp("fecha_fin");
                    RecorridoGuiado r = new RecorridoGuiado(id, rs.getString("nombre"), rs.getString("descripcion"),
                            fi.toLocalDateTime(), ff.toLocalDateTime(), rs.getString("url_pagina"));
                    nRecorridos.put(id, r);
                    if (id > maxRec) maxRec = id;
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id_recorrido, id_sala, numero_orden FROM detalle_recorrido_sala ORDER BY id_recorrido, numero_orden")) {
                while (rs.next()) {
                    RecorridoGuiado r = nRecorridos.get(rs.getInt("id_recorrido"));
                    SalaExposicion s = nSalas.get(rs.getInt("id_sala"));
                    if (r != null && s != null) {
                        r.agregarSala(s);
                    }
                }
            }

            this.museos = nMuseos;
            this.salas = nSalas;
            this.categorias = nCategorias;
            this.artistas = nArtistas;
            this.obras = nObras;
            this.recorridos = nRecorridos;

            this.idMuseo = maxMuseo + 1;
            this.idSala = maxSala + 1;
            this.idCategoria = maxCat + 1;
            this.idArtista = maxArt + 1;
            this.idObra = maxObra + 1;
            this.idRecorrido = maxRec + 1;
        } catch (Exception ignored) {
        }
    }

    private int toInt(Object val) {
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(String.valueOf(val));
    }

    private Integer toNullableInt(Object val) {
        if (val == null || String.valueOf(val).isBlank()) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(String.valueOf(val));
    }

    private Double toDouble(Object val) {
        if (val == null || String.valueOf(val).isBlank()) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(String.valueOf(val));
    }

    private String toStr(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    private LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        String s = String.valueOf(val).trim();
        if (s.isEmpty()) return null;
        return LocalDate.parse(s.substring(0, 10));
    }

    private LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        String s = String.valueOf(val).trim();
        if (s.isEmpty()) return null;
        s = s.replace(" ", "T");
        if (s.length() == 16) s = s + ":00";
        return LocalDateTime.parse(s);
    }

    // ---------- datos de ejemplo (los mismos del script SQL) ----------

    @PostConstruct
    public void cargarDatosDePrueba() {
        sincronizarBD = false;
        museos.clear();
        salas.clear();
        categorias.clear();
        artistas.clear();
        obras.clear();
        recorridos.clear();
        idMuseo = 1;
        idSala = 1;
        idCategoria = 1;
        idArtista = 1;
        idObra = 1;
        idRecorrido = 1;

        registrarMuseo("Museo Nacional de Colombia",
                "Museo fundado en 1823 en Bogota, con colecciones de arte, historia y arqueologia",
                4.615556, -74.068889, "https://www.museonacional.gov.co");
        registrarMuseo("Museo del Oro",
                "Museo del Banco de la Republica con piezas de orfebreria prehispanica",
                4.601944, -74.071944, "https://www.banrepcultural.org/bogota/museo-del-oro");

        registrarSala(1, "Sala Arte Moderno", "Pintura y escultura colombiana del siglo XX", 60);
        registrarSala(1, "Sala Memoria y Nacion", "Obras sobre la historia politica y social del pais", 80);
        registrarSala(1, "Sala de Exposiciones Temporales", "Muestras que cambian por temporada", 120);
        registrarSala(2, "Sala El Trabajo de los Metales", "Tecnicas de orfebreria prehispanica", 70);
        registrarSala(2, "Sala Cosmologia y Simbolismo", "Piezas relacionadas con rituales y chamanismo", 90);
        registrarSala(2, "Sala La Ofrenda", "Ofrendas votivas", 40);

        registrarCategoria("Pintura", "Oleos, acuarelas y tecnicas mixtas");
        registrarCategoria("Escultura", "Obras en bronce, piedra o madera");
        registrarCategoria("Orfebreria prehispanica", "Piezas de oro y tumbaga anteriores a 1492");

        registrarArtista("Fernando Botero", "Colombiana", "Pintor y escultor nacido en Medellin (1932-2023)");
        registrarArtista("Alejandro Obregon", "Colombiana", "Pintor nacido en Barcelona (1920-1992)");
        registrarArtista("Debora Arango", "Colombiana", "Pintora antioquena (1907-2005)");

        registrarObra("Violencia", "Oleo sobre lienzo", "", LocalDate.of(1962, 1, 1),
                LocalDate.of(1963, 5, 14), 92, 1, 2, 1, LocalDateTime.of(2018, 3, 1, 9, 0));

        registrarObra("Masacre del 9 de abril", "Oleo sobre el Bogotazo de 1948", "", LocalDate.of(1948, 1, 1),
                LocalDate.of(1985, 9, 20), 58, 1, 3, 3, LocalDateTime.of(2019, 4, 9, 9, 0));
        moverObra(2, 2, LocalDateTime.of(2019, 11, 5, 9, 0));

        registrarObra("Mona Lisa", "Version de Botero del cuadro de Leonardo", "", LocalDate.of(1978, 1, 1),
                LocalDate.of(2000, 11, 1), 88, 1, 1, 1, LocalDateTime.of(2017, 2, 1, 9, 0));
        moverObra(3, 3, LocalDateTime.of(2026, 1, 15, 9, 0));

        registrarObra("Mujer reclinada", "Escultura en bronce", "", LocalDate.of(1993, 1, 1),
                LocalDate.of(2001, 3, 15), 95, 2, 1, 3, LocalDateTime.of(2021, 6, 1, 9, 0));
        moverObra(4, 1, LocalDateTime.of(2022, 1, 10, 9, 0));

        registrarObra("Poporo Quimbaya", "Recipiente de oro, aprox. 500 a.C. - 600 d.C.", "", null,
                LocalDate.of(1939, 12, 22), 97, 3, null, 5, LocalDateTime.of(2015, 8, 1, 9, 0));
        moverObra(5, 4, LocalDateTime.of(2020, 3, 2, 9, 0));

        registrarObra("Balsa Muisca", "Pieza de tumbaga encontrada en Pasca, Cundinamarca", "", null,
                LocalDate.of(1969, 3, 1), 90, 3, null, 6, LocalDateTime.of(2008, 5, 1, 9, 0));

        registrarObra("Pectoral Calima", "Pectoral con rostro humano, tiene corrosion en los bordes", "", null,
                LocalDate.of(1958, 7, 10), 47, 3, null, 4, LocalDateTime.of(2012, 1, 15, 9, 0));
        moverObra(7, 5, LocalDateTime.of(2024, 7, 8, 9, 0));

        crearRecorrido("Arte colombiano del siglo XX", "Recorrido por las obras de Botero, Obregon y Arango",
                LocalDateTime.of(2026, 12, 1, 9, 0), LocalDateTime.of(2027, 2, 28, 17, 0),
                "https://example.org/recorridos/arte-siglo-xx");
        agregarSalaARecorrido(1, 3);
        agregarSalaARecorrido(1, 1);
        agregarSalaARecorrido(1, 2);

        crearRecorrido("Ruta del oro prehispanico", "Recorrido por la orfebreria y los rituales prehispanicos",
                LocalDateTime.of(2026, 11, 20, 10, 0), LocalDateTime.of(2027, 1, 31, 16, 0),
                "https://example.org/recorridos/ruta-del-oro");
        agregarSalaARecorrido(2, 4);
        agregarSalaARecorrido(2, 5);
        agregarSalaARecorrido(2, 6);

        recargarDesdeMySQL();
        sincronizarBD = true;
    }
}
