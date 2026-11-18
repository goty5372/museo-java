package resource;

import dto.MoverObraRequest;
import dto.RegistrarObraRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.Obra;
import servicio.SistemaMuseoService;

import java.time.LocalDateTime;
import java.util.List;

@Path("/obras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ObraResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<Obra> listar() {
        return sistema.listarObras();
    }

    @GET
    @Path("/{id}")
    public Obra buscar(@PathParam("id") int id) {
        return sistema.buscarObra(id);
    }

    @POST
    public Response registrar(RegistrarObraRequest req) {
        Obra obra = sistema.registrarObra(
                req.nombre, req.descripcion, req.urlImagen,
                req.fechaCreacion, req.fechaAdquisicion,
                req.porcentajeConservacion, req.idCategoria,
                req.idArtista, req.idSala, LocalDateTime.now());
        return Response.status(Response.Status.CREATED).entity(obra).build();
    }

    @PUT
    @Path("/{id}/mover")
    public Response mover(@PathParam("id") int id, MoverObraRequest req) {
        LocalDateTime fecha = req.fecha != null ? req.fecha : LocalDateTime.now();
        sistema.moverObra(id, req.idSala, fecha);
        return Response.ok(sistema.buscarObra(id)).build();
    }
}
