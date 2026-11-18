package resource;

import dto.RegistrarMuseoRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Museo;
import servicio.SistemaMuseoService;

import java.util.List;

@Path("/museos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MuseoResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<Museo> listar() {
        return sistema.listarMuseos();
    }

    @GET
    @Path("/{id}")
    public Museo buscar(@PathParam("id") int id) {
        return sistema.buscarMuseo(id);
    }

    @POST
    public Response registrar(RegistrarMuseoRequest req) {
        Museo museo = sistema.registrarMuseo(req.nombre, req.descripcion, req.latitud, req.longitud, req.urlPagina);
        return Response.status(Response.Status.CREATED).entity(museo).build();
    }
}
