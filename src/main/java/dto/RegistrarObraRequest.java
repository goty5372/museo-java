package dto;
import java.time.LocalDate;
public class RegistrarObraRequest {
    public String nombre;
    public String descripcion;
    public String urlImagen;
    public LocalDate fechaCreacion;
    public LocalDate fechaAdquisicion;
    public int porcentajeConservacion;
    public int idCategoria;
    public Integer idArtista;
    public int idSala;
}
