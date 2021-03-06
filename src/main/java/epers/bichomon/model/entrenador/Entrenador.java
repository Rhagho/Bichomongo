package epers.bichomon.model.entrenador;

import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.ubicacion.Ubicacion;
import epers.bichomon.model.ubicacion.duelo.ResultadoCombate;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Entrenador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String nombre;

    @ManyToOne
    private Ubicacion ubicacion;

    @OneToMany(mappedBy = "entrenador", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Bicho> bichos = new HashSet<>();

    private int xp = 0;

    private int monedas = 10;

    @ManyToOne
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private Nivel nivel;

    @ManyToOne
    private XPuntos xpuntos;

    private LocalDate ultimaCaptura;

    protected Entrenador() {
    }

    public Entrenador(String nombre) {
        this.nombre = nombre;
        this.ultimaCaptura = LocalDate.now();
    }

    public Entrenador(String nombre, Nivel nivel, XPuntos xpuntos) {
        this(nombre);
        this.nivel = nivel;
        this.xpuntos = xpuntos;
    }

    public Entrenador(String nombre, Nivel nivel, XPuntos xpuntos, Set<Bicho> bichos) {
        this(nombre, nivel, xpuntos);
        this.bichos = bichos;
        this.bichos.forEach(b -> b.capturadoPor(this));
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void moverA(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivel() {
        return nivel.getNro();
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public int getXP() {
        return this.xp;
    }

    public LocalDate getUltimaCaptura() {
        return this.ultimaCaptura;
    }

    public int getMonedas() {
        return monedas;
    }

    public void incXP(int puntos) {
        this.xp += puntos;
        this.nivel.subeNivel(this);
    }

    private Boolean puedeBuscar() {
        return this.bichos.size() < this.nivel.getCantMax();
    }

    public Bicho buscar() {
        if (!puedeBuscar()) return null;
        Bicho b = this.ubicacion.buscar(this);
        if (b == null) return null;
        bichos.add(b);
        b.capturadoPor(this);
        incXP(xpuntos.getCapturar());
        ultimaCaptura = LocalDate.now();
        return b;
    }

    public void evolucion() {
        incXP(xpuntos.getEvolucionar());
    }

    public boolean contains(Bicho bicho) {
        return bichos.stream().anyMatch(b -> b.equals(bicho));
    }

    public void abandonar(Bicho bicho) {
        if (!contains(bicho) || this.bichos.size() <= 1) throw new BichoIncorrectoException(bicho.getID());
        ubicacion.abandonar(bicho);
        bichos.remove(bicho);
        bicho.abandonado();
    }

    public ResultadoCombate duelo(Bicho b) {
        if (!contains(b)) return null;
        ResultadoCombate res = ubicacion.duelo(b);
        if (res != null) incXP(xpuntos.getCombatir());
        return res;
    }

    public void pagar(int costo) {
        this.monedas -= costo;
    }
}
