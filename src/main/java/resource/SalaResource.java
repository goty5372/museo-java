package resource;

import dto.RegistrarSalaRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.SalaExposicion;
import servicio.SistemaMuseoService;

import java.util.List;

@Path("/salas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SalaResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<SalaExposicion> listar() {
        return sistema.listarSalas();
    }

    @GET
    @Path("/{id}")
    public SalaExposicion buscar(@PathParam("id") int id) {
        return sistema.buscarSala(id);
    }

    @POST
    public Response registrar(RegistrarSalaRequest req) {
        SalaExposicion sala = sistema.registrarSala(req.idMuseo, req.nombre, req.descripcion, req.capacidad);
        return Response.status(Response.Status.CREATED).entity(sala).build();
    }
}
