package resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import servicio.SistemaMuseoService;

import java.util.Map;

@Path("/api/datos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EstadoWebResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public Map<String, Object> obtenerEstado() {
        return sistema.obtenerEstadoWeb();
    }

    @PUT
    public Response guardarEstado(Map<String, Object> datos) {
        sistema.guardarEstadoWeb(datos);
        return Response.ok(sistema.obtenerEstadoWeb()).build();
    }
}
