package resource;

import dto.AgregarSalaRequest;
import dto.CambiarOrdenRequest;
import dto.CrearRecorridoRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import modelo.RecorridoGuiado;
import servicio.SistemaMuseoService;

import java.util.List;

@Path("/recorridos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecorridoResource {

    @Inject
    SistemaMuseoService sistema;

    @GET
    public List<RecorridoGuiado> listar() {
        return sistema.listarRecorridos();
    }

    @GET
    @Path("/{id}")
    public RecorridoGuiado buscar(@PathParam("id") int id) {
        return sistema.buscarRecorrido(id);
    }

    @POST
    public Response crear(CrearRecorridoRequest req) {
        RecorridoGuiado recorrido = sistema.crearRecorrido(
                req.nombre, req.descripcion, req.fechaInicio, req.fechaFin, req.urlPagina);
        return Response.status(Response.Status.CREATED).entity(recorrido).build();
    }

    @POST
    @Path("/{id}/salas")
    public Response agregarSala(@PathParam("id") int id, AgregarSalaRequest req) {
        sistema.agregarSalaARecorrido(id, req.idSala);
        return Response.ok(sistema.buscarRecorrido(id)).build();
    }

    @PUT
    @Path("/{id}/salas/orden")
    public Response cambiarOrden(@PathParam("id") int id, CambiarOrdenRequest req) {
        sistema.cambiarOrdenRecorrido(id, req.posicionActual, req.nuevaPosicion);
        return Response.ok(sistema.buscarRecorrido(id)).build();
    }

    @DELETE
    @Path("/{id}/salas/{posicion}")
    public Response quitarSala(@PathParam("id") int id, @PathParam("posicion") int posicion) {
        sistema.quitarSalaDeRecorrido(id, posicion);
        return Response.ok(sistema.buscarRecorrido(id)).build();
    }
}
