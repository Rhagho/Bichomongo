package epers.bichomon.service.bicho;

import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.bicho.BichoNoEvolucionableException;
import epers.bichomon.model.entrenador.Entrenador;
import epers.bichomon.model.entrenador.Nivel;
import epers.bichomon.model.entrenador.XPuntos;
import epers.bichomon.model.especie.Especie;
import epers.bichomon.model.especie.TipoBicho;
import epers.bichomon.model.especie.condicion.*;
import epers.bichomon.service.AbstractServiceTest;
import epers.bichomon.service.ServiceFactory;
import jersey.repackaged.com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BichoServiceEvolucionTest extends AbstractServiceTest {

    //TODO testear que efectivamente evolucionan
    private BichoService service = ServiceFactory.getBichoService();

    @BeforeAll
    static void prepare() {
        testService.save(new Especie("EspecieFinal", TipoBicho.FUEGO));
        crearEspecieEvolucionable("EspecieEdad", testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionEdad(5)));
        crearEspecieEvolucionable("EspecieEnergia", testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionEnergia(10)));
        crearEspecieEvolucionable("EspecieNivel", testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionNivel(5)));
        crearEspecieEvolucionable("EspecieVictorias", testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionVictorias(5)));
    }

    private static void crearEspecieEvolucionable(String nombre, Especie evolucion, Set<Condicion> condiciones) {
        testService.save(new Especie(nombre, TipoBicho.FUEGO, evolucion, condiciones));
    }

    private Bicho crearBicho(String especie, Entrenador entrenador) {
        Especie e = testService.getByName(Especie.class, especie);
        Bicho b = e.crearBicho();
        if(entrenador != null) {
            b.capturadoPor(entrenador);
        }
        testService.save(b);
        return b;
    }

    private Entrenador newEntrenador(String nombre, Set<Bicho> bichos) {
        Entrenador e = new Entrenador(nombre, testService.getBy(Nivel.class, "nro", 1), testService.get(XPuntos.class, 1), bichos);
        testService.save(e);
        return e;
    }


    //-------> Tests sobre condiciones genéricas
    @Test
    void puede_evolucionar_bicho_de_especie_no_evolucionable_false() {
        Bicho b = this.crearBicho("EspecieFinal", null);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void evolucionar_un_bicho_evolucionable_sin_condicion_especifica_tiene_especie_final() {
        Set<Condicion> set = new HashSet<>();
        testService.save(new Especie("EspecieBase", TipoBicho.FUEGO, testService.getByName(Especie.class, "EspecieFinal"), set));
        Bicho b = this.crearBicho("EspecieBase", null);
        service.evolucionar(b.getID());
        assertEquals("EspecieFinal", testService.get(Bicho.class, b.getID()).getEspecie().getNombre());
    }

    @Test
    void evolucionar_bicho_de_especie_no_evolucionable_raise_exception() {
        Bicho b = this.crearBicho("EspecieFinal", null);
        assertThrows(BichoNoEvolucionableException.class, () -> service.evolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de edad

    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_edad_false() {
        Entrenador e = newEntrenador("unEntrenador10", Sets.newHashSet());
        Bicho b = new Bicho(testService.getByName(Especie.class, "EspecieEdad"), e, LocalDate.of(2018, 10, 7));
        testService.save(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_edad_puede_evolucionar() {
        Entrenador e = new Entrenador("unEntrenador1");
        testService.save(e);
        Bicho b = new Bicho(testService.getByName(Especie.class, "EspecieEdad"), e, LocalDate.of(2018, 10, 5));
        testService.save(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de energia
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_energia_false() {
        Bicho b = this.crearBicho("EspecieEnergia", null);
        b.incEnergia(9);
        testService.upd(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_energia_puede_evolucionar() {
        Bicho b = this.crearBicho("EspecieEnergia", null);
        b.incEnergia(11);
        testService.upd(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de nivel
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_nivel_false() {
        Bicho b = testService.getByName(Especie.class, "EspecieNivel").crearBicho();
        Entrenador e = newEntrenador("unEntrenador9", Sets.newHashSet(b));
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_nivel_puede_evolucionar() {
        Nivel lvl = new Nivel(5, 1, 1);
        testService.save(lvl);
        Entrenador e = new Entrenador("Entrenador3", lvl);
        testService.save(e);
        Bicho b = this.crearBicho("EspecieNivel", e);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de victorias
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_victorias_false() {
        Bicho b = this.crearBicho("EspecieVictorias", null);
        //Le seteo 4 victorias
        for(int i = 0; i < 4; i++) b.ganasteDuelo();
        testService.upd(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_victorias_puede_evolucionar() {
        Bicho b = this.crearBicho("EspecieVictorias", null);
        //Le seteo 5 victorias
        for(int i = 0; i < 5; i++) b.ganasteDuelo();
        testService.upd(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests combinados
    @Test
    void un_bicho_que_no_cumple_con_todas_las_condiciones_juntas_no_puede_evolucionar() {
        testService.save(new Especie("EspecieCombinados", TipoBicho.FUEGO, testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionVictorias(5), new CondicionEnergia(10))));
        Bicho b = this.crearBicho("EspecieCombinados", null);
        for(int i = 0; i < 5; i++) b.ganasteDuelo();
        testService.upd(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_todas_las_condiciones_juntas_puede_evolucionar() {
        testService.save(new Especie("EspecieCombinados2", TipoBicho.FUEGO, testService.getByName(Especie.class, "EspecieFinal"), Sets.newHashSet(new CondicionVictorias(5), new CondicionEnergia(10))));
        Bicho b = this.crearBicho("EspecieCombinados2", null);
        for(int i = 0; i < 5; i++) b.ganasteDuelo();
        b.incEnergia(10);
        testService.upd(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }
}
