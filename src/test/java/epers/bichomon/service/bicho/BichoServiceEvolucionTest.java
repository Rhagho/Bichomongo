package epers.bichomon.service.bicho;

import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.bicho.BichoNoEvolucionableException;
import epers.bichomon.model.entrenador.Entrenador;
import epers.bichomon.model.entrenador.Nivel;
import epers.bichomon.model.especie.Especie;
import epers.bichomon.model.especie.TipoBicho;
import epers.bichomon.model.especie.condicion.*;
import epers.bichomon.service.ServiceFactory;
import epers.bichomon.service.runner.SessionFactoryProvider;
import epers.bichomon.service.test.TestService;
import jersey.repackaged.com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BichoServiceEvolucionTest {

    static private BichoService service = ServiceFactory.getBichoService();
    static private TestService testService = ServiceFactory.getTestService();

    @BeforeAll
    static void prepare() {
        testService.crearEntidad(new Especie("EspecieFinal", TipoBicho.FUEGO));

        testService.crearEntidad(new Especie("EspecieEdad",TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class, "EspecieFinal"),
                Sets.newHashSet(new CondicionEdad(5))));

        testService.crearEntidad(new Especie("EspecieEnergia", TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class, "EspecieFinal"),
                Sets.newHashSet(new CondicionEnergia(10))));

        testService.crearEntidad(new Especie("EspecieNivel", TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class, "EspecieFinal"),
                Sets.newHashSet(new CondicionNivel(5))));

        testService.crearEntidad(new Especie("EspecieVictorias", TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class, "EspecieFinal"),
                Sets.newHashSet(new CondicionVictorias(5))));
    }

    @AfterAll
    static void cleanup() {
        SessionFactoryProvider.destroy();
    }

    private void crearEspecieEvolucionable(String nombre, Especie evolucion, Set<Condicion> condiciones) {
        testService.crearEntidad(new Especie(nombre, TipoBicho.FUEGO, evolucion, condiciones));
    }

    private Bicho crearBicho(String especie, Entrenador entrenador) {
        Especie e = testService.recuperarByName(Especie.class, especie);
        Bicho b = e.crearBicho();
        if (entrenador != null) {
            b.capturadoPor(entrenador);
        }
        testService.crearEntidad(b);
        return b;
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
        testService.crearEntidad(new Especie("EspecieBase",TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class,"EspecieFinal"),
                set));
        Bicho b= this.crearBicho("EspecieBase", null);
        service.evolucionar(b.getID());
        testService.actualizar(b);
        assertEquals("EspecieFinal", b.getEspecie().getNombre());
    }

    @Test
    void evolucionar_bicho_de_especie_no_evolucionable_raise_exception() {
        Bicho b = this.crearBicho("EspecieFinal", null);
        assertThrows(BichoNoEvolucionableException.class, () -> service.evolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de edad

    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_edad_false() {
        Entrenador e = new Entrenador("unEntrenador");
        testService.crearEntidad(e);
        Bicho b = new Bicho(testService.recuperarByName(Especie.class, "EspecieEdad"), e,
                LocalDate.of(2018, 10, 7));
        testService.crearEntidad(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_edad_puede_evolucionar() {
        // Nota!!!! Cuando querramos hacer el test, cambiar la fecha de captura, de tal forma que
        Entrenador e = new Entrenador("unEntrenador1");
        testService.crearEntidad(e);
        Bicho b = new Bicho(testService.recuperarByName(Especie.class, "EspecieEdad"), e,
                LocalDate.of(2018, 1, 6));
        testService.crearEntidad(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de energia
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_energia_false() {
        Bicho b = this.crearBicho("EspecieEnergia",null);
        b.incEnergia(9);
        testService.actualizar(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_energia_puede_evolucionar() {
        Bicho b = this.crearBicho("EspecieEnergia",null);
        b.incEnergia(11);
        testService.actualizar(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de nivel
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_nivel_false() {
        Nivel lvl = new Nivel(4, 5, 15);
        Entrenador e = new Entrenador("unEntrenador2", lvl);
        testService.crearEntidad(e);
        Bicho b = this.crearBicho("EspecieNivel", e);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_nivel_puede_evolucionar() {
        Nivel lvl = new Nivel(5, 5, 15);
        Entrenador e = new Entrenador("unEntrenador3", lvl);
        testService.crearEntidad(e);
        Bicho b = this.crearBicho("EspecieNivel", e);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests sobre la condicion de victorias
    @Test
    void puede_evolucionar_un_bicho_que_no_cumple_con_la_condicion_de_victorias_false() {
        Bicho b = this.crearBicho("EspecieVictorias",null);
        //Le seteo 4 victorias
        for (int i = 0; i < 4; i++) b.ganasteDuelo();
        testService.actualizar(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_la_condicion_de_victorias_puede_evolucionar() {
        Bicho b = this.crearBicho("EspecieVictorias",null);
        //Le seteo 5 victorias
        for (int i = 0; i < 5; i++) b.ganasteDuelo();
        testService.actualizar(b);
        assertTrue(service.puedeEvolucionar(b.getID()));
    }

    //-------> Tests combinados
    @Test
    void un_bicho_que_no_cumple_con_todas_las_condiciones_juntas_no_puede_evolucionar() {
        testService.crearEntidad(new Especie("EspecieCombinados", TipoBicho.FUEGO,
                testService.recuperarByName(Especie.class, "EspecieFinal"),
                Sets.newHashSet(new CondicionVictorias(5),new CondicionEnergia(10))));
        Bicho b = this.crearBicho("EspecieCombinados",null);
        for (int i = 0; i < 5; i++) b.ganasteDuelo();
        testService.actualizar(b);
        assertFalse(service.puedeEvolucionar(b.getID()));
    }

    @Test
    void un_bicho_que_cumple_con_todas_las_condiciones_juntas_puede_evolucionar() {
        fail();
    }
}
