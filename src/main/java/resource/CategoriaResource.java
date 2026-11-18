package resource;

import dto.RegistrarCategoriaRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Categoria;
import servicio.SistemaMuseoService;

import java.util.List;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<Categoria> listar() {
        return sistema.listarCategorias();
    }

    @GET
    @Path("/{id}")
    public Categoria buscar(@PathParam("id") int id) {
        return sistema.buscarCategoria(id);
    }

    @POST
    public Response registrar(RegistrarCategoriaRequest req) {
        Categoria categoria = sistema.registrarCategoria(req.nombre, req.descripcion);
        return Response.status(Response.Status.CREATED).entity(categoria).build();
    }
}
