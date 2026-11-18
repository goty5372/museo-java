package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import modelo.*;
import servicio.SistemaMuseoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReporteResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    @Path("/obras")
    public List<Map<String, Object>> reporteObras() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Obra o : sistema.listarObras()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", o.getId());
            item.put("nombre", o.getNombre());
            item.put("artista", o.getNombreArtista());
            item.put("categoria", o.getCategoria().getNombre());
            item.put("sala", o.getSalaActual().getNombre());
            item.put("museo", o.getSalaActual().getMuseo().getNombre());
            item.put("conservacion", o.getPorcentajeConservacion());
            item.put("estado", o.getEstadoConservacion().getNombre());
            lista.add(item);
        }
        return lista;
    }

    @GET
    @Path("/conservacion")
    public List<Map<String, Object>> promedioConservacion() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Map.Entry<SalaExposicion, Double> e : sistema.promedioConservacionPorSala().entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("sala", e.getKey().getNombre());
            item.put("museo", e.getKey().getMuseo().getNombre());
            item.put("promedio", e.getValue());
            lista.add(item);
        }
        return lista;
    }

    @GET
    @Path("/estados")
    public Map<String, List<Map<String, Object>>> obrasPorEstado() {
        Map<String, List<Map<String, Object>>> resultado = new HashMap<>();
        for (EstadoConservacion estado : EstadoConservacion.values()) {
            List<Map<String, Object>> obras = new ArrayList<>();
            for (Obra o : sistema.obrasPorEstado(estado)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", o.getId());
                item.put("nombre", o.getNombre());
                item.put("conservacion", o.getPorcentajeConservacion());
                item.put("recomendacion", estado.getRecomendacion());
                obras.add(item);
            }
            resultado.put(estado.getNombre(), obras);
        }
        return resultado;
    }

    @GET
    @Path("/general")
    public List<String> reporteGeneral() {
        List<String> lista = new ArrayList<>();
        for (Reportable r : sistema.elementosReportables()) {
            lista.add(r.generarReporte());
        }
        return lista;
    }
}
