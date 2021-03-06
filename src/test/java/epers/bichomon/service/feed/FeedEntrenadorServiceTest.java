package epers.bichomon.service.feed;

import epers.bichomon.AbstractConectadosTest;
import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.entrenador.BichoIncorrectoException;
import epers.bichomon.model.especie.Especie;
import epers.bichomon.model.especie.TipoBicho;
import epers.bichomon.model.evento.*;
import epers.bichomon.model.ubicacion.Dojo;
import epers.bichomon.model.ubicacion.Guarderia;
import epers.bichomon.model.ubicacion.Pueblo;
import epers.bichomon.model.ubicacion.Ubicacion;
import epers.bichomon.model.ubicacion.busqueda.BusquedaFracasoException;
import epers.bichomon.service.ServiceFactory;
import epers.bichomon.service.bicho.BichoService;
import epers.bichomon.service.mapa.MapaService;
import epers.bichomon.service.mapa.UbicacionMuyLejanaException;
import jersey.repackaged.com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FeedEntrenadorServiceTest extends AbstractConectadosTest {

    private MapaService mapService = ServiceFactory.INSTANCE.getMapService();
    private BichoService bichoService = ServiceFactory.INSTANCE.getBichoService();
    private FeedService feedService = ServiceFactory.INSTANCE.getFeedService();

    @BeforeAll
    static void prepare() {
        testService.save(new Especie("rocamon", TipoBicho.TIERRA, 10));
        testService.save(new Especie("metalmon", TipoBicho.TIERRA, 100));
    }

    @Test
    void entrenadorInexistenteRetornaListaVacia() {
        assertTrue(feedService.feedEntrenador("fruta").isEmpty());
    }

    @Test
    void entrenadorNuevoSinEventosRetornaListaVacia() {
        newEntrenador("nuevo", testService.getByName(Ubicacion.class, "Plantalandia"));
        assertTrue(feedService.feedEntrenador("nuevo").isEmpty());
    }

    @Test
    void EntrenadorViajoFeedEntrenadorMuestraElViaje() {
        String e = "entrenador";
        newEntrenador(e, testService.getByName(Ubicacion.class, "Plantalandia"));

        mapService.mover(e, "Agualandia");

        List<Evento> eventos = feedService.feedEntrenador(e);
        assertEquals(1, eventos.size());
        checkEvento((EventoArribo) eventos.get(0), e, "Plantalandia", "Agualandia");
    }

    @Test
    void EntrenadorViajo2VecesMuestraViajesEnOrden() {
        String e = "viajador2";
        newEntrenador(e, testService.getByName(Ubicacion.class, "Plantalandia"));

        mapService.mover(e, "Agualandia");
        mapService.mover(e, "Plantalandia");

        List<Evento> eventos = feedService.feedEntrenador(e);
        assertEquals(2, eventos.size());
        checkEvento((EventoArribo) eventos.get(0), e, "Agualandia", "Plantalandia");
        checkEvento((EventoArribo) eventos.get(1), e, "Plantalandia", "Agualandia");
    }

    @Test
    void FeedEntrenadorConEventoCaptura() {
        Bicho bichin = testService.getByName(Especie.class, "rocamon").crearBicho();
        testService.save(bichin);
        String trainer = "trainer";
        Ubicacion place = new Guarderia("guardaBicho");
        place.abandonar(bichin);
        testService.save(place);
        newEntrenador(trainer, place);

        bichoService.buscar(trainer);

        List<Evento> eventos = feedService.feedEntrenador(trainer);
        assertEquals(1, eventos.size());
        checkEvento((EventoCaptura) eventos.get(0), trainer, "guardaBicho", "rocamon");
    }

    @Test
    void FeedEntrenadorConEventoAbandono() {
        Especie e = testService.getByName(Especie.class, "rocamon");
        Bicho b1 = e.crearBicho();
        Bicho b2 = e.crearBicho();

        newEntrenador("lucas", testService.getByName(Guarderia.class, "Poke"), Sets.newHashSet(b1, b2));

        bichoService.abandonar("lucas", b1.getID());

        List<Evento> eventos = feedService.feedEntrenador("lucas");
        assertEquals(1, eventos.size());
        checkEvento((EventoAbandono) eventos.get(0), "lucas", "Poke", "rocamon");
    }

    @Test
    void FeedEntrenadorConEventoCoronado() {
        Bicho b = testService.getByName(Especie.class, "rocamon").crearBicho();
        Ubicacion place = new Dojo("dojito");
        testService.save(place);
        newEntrenador("brock", place, Sets.newHashSet(b));

        bichoService.duelo("brock", b.getID());

        List<Evento> eventos = feedService.feedEntrenador("brock");
        assertEquals(1, eventos.size());
        checkEvento((EventoCoronacion) eventos.get(0), "brock", "", "dojito");
    }

    @Test
    void FeedEntrenadorConEventoCoronadoYDescoronado() {
        String dojo = "A1";
        Bicho b1 = testService.getByName(Especie.class, "rocamon").crearBicho();
        newEntrenador("alberto", testService.getByName(Dojo.class, dojo), Sets.newHashSet(b1));
        Bicho b2 = testService.getByName(Especie.class, "metalmon").crearBicho();
        newEntrenador("julio", testService.getByName(Dojo.class, dojo), Sets.newHashSet(b2));

        bichoService.duelo("alberto", b1.getID());
        bichoService.duelo("julio", b2.getID());

        List<Evento> eventosAlberto = feedService.feedEntrenador("alberto");
        assertEquals(2, eventosAlberto.size());
        checkEvento((EventoCoronacion) eventosAlberto.get(0), "julio", "alberto", dojo);
        checkEvento((EventoCoronacion) eventosAlberto.get(1), "alberto", "", dojo);

        List<Evento> eventosJulio = feedService.feedEntrenador("julio");
        assertEquals(1, eventosJulio.size());
        checkEvento((EventoCoronacion) eventosJulio.get(0), "julio", "alberto", dojo);
    }

    @Test
    void FeedEntrenadorSiNoHayCoronacion() {
        String dojo = "A2";
        Bicho b1 = testService.getByName(Especie.class, "metalmon").crearBicho();
        newEntrenador("roberto", testService.getByName(Dojo.class, dojo), Sets.newHashSet(b1));
        Bicho b2 = testService.getByName(Especie.class, "rocamon").crearBicho();
        newEntrenador("ana", testService.getByName(Dojo.class, dojo), Sets.newHashSet(b2));

        bichoService.duelo("roberto", b1.getID());
        bichoService.duelo("ana", b2.getID());

        List<Evento> eventosRoberto = feedService.feedEntrenador("roberto");
        assertEquals(1, eventosRoberto.size());
        checkEvento((EventoCoronacion) eventosRoberto.get(0), "roberto", "", dojo);
        assertEquals(0, feedService.feedEntrenador("ana").size());
    }

    @Test
    void FeedEntrenadorVacioSiEntrenadorNoPuedeAbandonarBicho() {
        String guarderia = "Poke";
        Bicho b1 = testService.getByName(Especie.class, "metalmon").crearBicho();
        newEntrenador("lucia", testService.getByName(Guarderia.class, guarderia), Sets.newHashSet(b1));
        assertThrows(BichoIncorrectoException.class, () -> bichoService.abandonar("lucia", b1.getID()));
        assertEquals(0, feedService.feedEntrenador("lucia").size());
    }

    @Test
    void FeedEntrenadorNoTieneArriboSiNoPuedeViajar() {
        newEntrenador("Alex", testService.getByName(Pueblo.class, "Plantalandia"));
        assertThrows(UbicacionMuyLejanaException.class, () -> mapService.mover("Alex", "Poke"));
        assertEquals(0, feedService.feedEntrenador("Alex").size());
    }

    @Test
    void FeedEntrenadorNoTieneCapturasSiNoPuedeCapturar() {
        newEntrenador("ash", testService.getByName(Guarderia.class, "St.Blah"));

        assertThrows(BusquedaFracasoException.class, () -> bichoService.buscar("ash"));
        assertEquals(0, feedService.feedEntrenador("ash").size());
    }

    @Test
    void FeedEntrenadorTieneTodosLosEventos() {
        String coach = "juani";
        String dojo = "Tibet Dojo";
        String sGuarderia = "St.Blah";
        Ubicacion guarderia = testService.getByName(Ubicacion.class, "St.Blah");
        Bicho b1 = testService.getByName(Especie.class, "metalmon").crearBicho();
        Bicho b2 = testService.getByName(Especie.class, "metalmon").crearBicho();
        Bicho b3 = testService.getByName(Especie.class, "rocamon").crearBicho();
        newEntrenador(coach, guarderia, Sets.newHashSet(b1));

        // Creo al entrenador que será descoronado más tarde
        newEntrenador("loser", testService.getByName(Ubicacion.class, dojo), Sets.newHashSet(b3));
        bichoService.duelo("loser", b3.getID());
        // Agrego un bicho a la guardería y actualizo
        testService.save(b2);
        guarderia.abandonar(b2);
        testService.upd(guarderia);

        // Hago que el entrenador capture un bicho
        bichoService.buscar(coach);
        // Hago que el entrenador abandone el bicho b1
        bichoService.abandonar(coach, b1.getID());
        // Hago que el entrenador se mueva al dojo
        mapService.mover(coach, dojo);
        // Hago que peleen, y que nuestro coach se corone
        bichoService.duelo(coach, b2.getID());
        List<Evento> eventosLoser = feedService.feedEntrenador("loser");

        assertEquals(2, eventosLoser.size());
        checkEvento((EventoCoronacion) eventosLoser.get(0), coach, "loser", dojo);
        checkEvento((EventoCoronacion) eventosLoser.get(1), "loser", "", dojo);

        List<Evento> eventosCoach = feedService.feedEntrenador(coach);
        assertEquals(4, eventosCoach.size());
        checkEvento((EventoCoronacion) eventosCoach.get(0), coach, "loser", dojo);
        checkEvento((EventoArribo) eventosCoach.get(1), coach, sGuarderia, dojo);
        checkEvento((EventoAbandono) eventosCoach.get(2), coach, sGuarderia, b1.getEspecie().getNombre());
        checkEvento((EventoCaptura) eventosCoach.get(3), coach, sGuarderia, b2.getEspecie().getNombre());

    }

}
