package epers.bichomon.service.especie;

import epers.bichomon.AbstractServiceTest;
import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.entrenador.Entrenador;
import epers.bichomon.model.especie.Especie;
import epers.bichomon.model.especie.TipoBicho;
import epers.bichomon.service.ServiceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EspecieServiceTest extends AbstractServiceTest {

    private EspecieService service = ServiceFactory.INSTANCE.getEspecieService();

    @BeforeAll
    static void prepare() {
        testService.save(new Especie("Rojomon", TipoBicho.FUEGO, 180, 75, 100, "/rojomon.jpg"));
        testService.save(new Especie("Amarillomon", TipoBicho.AIRE, 170, 69, 300, "/amarillomon.jpg"));
        testService.save(new Especie("Verdemon", TipoBicho.PLANTA, 150, 55, 500, "/verdemon.jpg"));
        testService.save(new Especie("Violetamon", TipoBicho.TIERRA, 150, 55, 500, ""));
        testService.save(new Especie("Azulmon", TipoBicho.ELECTRICIDAD, 150, 55, 500, ""));
        testService.save(new Especie("Naranjamon", TipoBicho.CHOCOLATE, 150, 55, 500, ""));
        testService.save(new Especie("Marronmon", TipoBicho.AGUA, 150, 55, 500, ""));
        testService.save(new Especie("Lilamon", TipoBicho.AIRE, 150, 55, 500, ""));
        testService.save(new Especie("Celestemon", TipoBicho.AGUA, 150, 55, 500, ""));
        testService.save(new Especie("Ocremon", TipoBicho.FUEGO, 150, 55, 500, ""));
        testService.save(new Especie("Turquesamon", TipoBicho.PLANTA, 150, 55, 500, ""));
    }

    private void borrarBichos(Set<Integer> bichos) {
        bichos.forEach(b -> testService.delete(Bicho.class, b));
    }

    private Set<Integer> crearBichos(List<String> especies, Entrenador entrenador) {
        return especies.stream().map(especie -> {
            Bicho b = testService.getByName(Especie.class, especie).crearBicho();
            if (entrenador != null) b.capturadoPor(entrenador);
            testService.save(b);
            return b.getID();
        }).collect(Collectors.toSet());
    }

    @Test
    void actualizar_inexistente_raise_exception() {
        assertThrows(EspecieNoExistente.class, () -> service.getEspecie("inexistente"));
    }

    @Test
    void restaurar_guardado_tiene_mismos_datos() {
        Especie especie = new Especie("prueba", TipoBicho.AGUA, 100, 350, 50, "url");

        service.crearEspecie(especie);
        Especie restored = service.getEspecie("prueba");

        assertEquals(especie.getNombre(), restored.getNombre());
        assertEquals(especie.getTipo(), restored.getTipo());
        assertEquals(especie.getAltura(), restored.getAltura());
        assertEquals(especie.getPeso(), restored.getPeso());
        assertEquals(especie.getEnergiaInicial(), restored.getEnergiaInicial());
        assertEquals(especie.getUrlFoto(), restored.getUrlFoto());
        assertEquals(0, restored.getCantidadBichos());

        testService.deleteByName(Especie.class, "prueba");
    }

    @Test
    void recuperar_todos_no_tiene_especie_inexistente() {
        assertTrue(service.getAllEspecies().stream().noneMatch(e -> e.getNombre().equals("inexistente")));
    }

    @Test
    void recuperar_todos_tiene_especie_rojomon() {
        assertTrue(service.getAllEspecies().stream().anyMatch(e -> e.getNombre().equals("Rojomon")));
    }

    @Test
    void recuperar_todos_tiene_11_especies() {
        assertEquals(11, service.getAllEspecies().size());
    }

    @Test
    void crear_bicho_aumenta_en_1_la_cantidad_de_bichos() {
        int cantidadDeBichos = service.getEspecie("Rojomon").getCantidadBichos();
        service.crearBicho("Rojomon");

        assertEquals(cantidadDeBichos + 1, service.getEspecie("Rojomon").getCantidadBichos());
    }

    @Test
    void si_no_hay_entrenadores_con_algun_bicho_de_especie_creada_no_hay_populares() {
        assertEquals(0, service.populares().size());
    }

    @Test
    void con_solo_seis_especies_cuyos_bichos_tengan_entrenador_hay_seis_especies_populares() {
        List<String> especies = Arrays.asList("Rojomon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon");
        Entrenador e = new Entrenador("unEntrenador");
        testService.save(e);
        crearBichos(especies, e);

        assertEquals(6, service.populares().size());

        testService.deleteByName(Entrenador.class, "unEntrenador");
    }

    @Test
    void al_recuperar_las_populares_no_esta_la_impopular() {
        List<String> especies = Arrays.asList("Rojomon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon", "Celestemon", "Marronmon", "Naranjamon", "Ocremon");
        Entrenador e = new Entrenador("unEntrenador");
        testService.save(e);
        crearBichos(especies, e);

        assertFalse(service.populares().contains(testService.getByName(Especie.class, "Turquesamon")));

        testService.deleteByName(Entrenador.class, "unEntrenador");
    }

    @Test
    void se_recuperan_las_populares_y_hay_10() {
        List<String> especies = Arrays.asList("Rojomon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon", "Celestemon", "Marronmon", "Naranjamon", "Ocremon");
        Entrenador e = new Entrenador("unEntrenador3");
        testService.save(e);
        crearBichos(especies, e);

        assertEquals(10, service.populares().size());

        testService.deleteByName(Entrenador.class, "unEntrenador3");
    }

    @Test
    void sin_bichos_creados_no_hay_especies_impopulares() {
        assertEquals(0, service.impopulares().size());
    }

    @Test
    void con_solo_seis_especies_hay_seis_especies_impopulares() {
        List<String> especies = Arrays.asList("Rojomon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon");
        Set<Integer> bichos = crearBichos(especies, null);

        assertEquals(6, service.impopulares().size());

        borrarBichos(bichos);
    }

    @Test
    void al_recuperar_las_impopulares_no_esta_la_popular() {
        Entrenador e = new Entrenador("unEntrenador");
        testService.save(e);
        crearBichos(Collections.singletonList("Rojomon"), e);
        List<String> especiesImpopulares = Arrays.asList("Turquesamon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon", "Celestemon", "Marronmon", "Naranjamon", "Ocremon");
        Set<Integer> bichos = crearBichos(especiesImpopulares, null);

        assertFalse(service.impopulares().contains(testService.getByName(Especie.class, "Rojomon")));

        borrarBichos(bichos);
        testService.deleteByName(Entrenador.class, "unEntrenador");
    }

    @Test
    void se_recuperan_las_impopulares_y_hay_10() {
        List<String> especiesImpopulares = Arrays.asList("Turquesamon", "Amarillomon", "Verdemon", "Violetamon", "Azulmon", "Lilamon", "Celestemon", "Marronmon", "Naranjamon", "Ocremon");
        Set<Integer> bichos = crearBichos(especiesImpopulares, null);

        assertEquals(10, service.impopulares().size());

        borrarBichos(bichos);
    }

}
