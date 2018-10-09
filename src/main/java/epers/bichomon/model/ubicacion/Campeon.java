package epers.bichomon.model.ubicacion;

import epers.bichomon.model.bicho.Bicho;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class Campeon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Bicho campeon;

    private LocalDate desde;

    private LocalDate hasta;

    protected Campeon() {}

    public Campeon(Bicho campeon) {
        this.campeon = campeon;
        this.desde = LocalDate.now();
    }

    public Campeon(Bicho campeon, LocalDate desde, LocalDate hasta) {
        this(campeon);
        this.desde = desde;
        this.hasta = hasta;
    }

    public Bicho getCampeon() {
        return campeon;
    }

    public void derrotado() {
        this.hasta = LocalDate.now();
    }
}
