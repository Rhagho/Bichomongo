package epers.bichomon.model.ubicacion;

import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.entrenador.Entrenador;
import epers.bichomon.model.ubicacion.busqueda.BusquedaFracasoException;
import epers.bichomon.model.ubicacion.busqueda.ProbabilidadBusqueda;
import epers.bichomon.model.ubicacion.duelo.ResultadoCombate;

import javax.persistence.*;
import java.util.Set;

@Entity
public abstract class Ubicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String nombre;

    @OneToMany(mappedBy = "ubicacion")
    protected Set<Entrenador> entrenadores;

    @Transient
    private ProbabilidadBusqueda busqueda = UbicacionFactory.getBusqueda();

    protected Ubicacion() {
    }

    public Ubicacion(String nombreUbicacion) {
        this.nombre = nombreUbicacion;
    }

    public Integer getID() {
        return this.id;
    }

    public Bicho buscar(Entrenador e) {
        if (!this.busqueda.exitosa(e))
            throw new BusquedaFracasoException(nombre);
        return buscarBicho(e);
    }

    protected abstract Bicho buscarBicho(Entrenador entrenador);

    public void abandonar(Bicho bicho) {
        throw new UbicacionIncorrectaException(nombre);
    }

    public ResultadoCombate duelo(Bicho bicho) {
        throw new UbicacionIncorrectaException(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public int getPoblacion() {
        return -1;
    }
}
