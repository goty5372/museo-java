package resource;

import dto.RegistrarArtistaRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Artista;
import servicio.SistemaMuseoService;

import java.util.List;

@Path("/artistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistaResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<Artista> listar() {
        return sistema.listarArtistas();
    }

    @GET
    @Path("/{id}")
    public Artista buscar(@PathParam("id") int id) {
        return sistema.buscarArtista(id);
    }

    @POST
    public Response registrar(RegistrarArtistaRequest req) {
        Artista artista = sistema.registrarArtista(req.nombre, req.nacionalidad, req.biografia);
        return Response.status(Response.Status.CREATED).entity(artista).build();
    }
}
