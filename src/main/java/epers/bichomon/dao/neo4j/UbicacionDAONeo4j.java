package epers.bichomon.dao.neo4j;

import epers.bichomon.model.ubicacion.Ubicacion;
import org.neo4j.driver.v1.*;

import java.util.List;

public class UbicacionDAONeo4j {

    private Driver driver;

    public UbicacionDAONeo4j() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    private <T> T runWithSession(SessionBlock<T> bloque) {
        try (Session s = this.driver.session()) {
            return bloque.executeWith(s);
        }
    }

    public void save(Ubicacion ubicacion) {
        runWithSession(s -> s.run("MERGE (n:Ubicacion {id: {elID}})", Values.parameters("elID", ubicacion.getID())));
    }

    public void saveCamino(Ubicacion desde, String camino, Ubicacion hasta) {
        String q = "MATCH (desde:Ubicacion {id: {idDesde}}) " +
                "MATCH (hasta:Ubicacion {id: {idHasta}}) " +
                "MERGE (desde)-[:" + camino + "]->(hasta)";
        runWithSession(s -> s.run(q, Values.parameters("idDesde", desde.getID(), "idHasta", hasta.getID())));
    }

    public List<Integer> conectados(Ubicacion ubicacion, String tipoCamino) {
        String q = "MATCH (:Ubicacion {id: {elID}})-[:" + tipoCamino + "]-(u) RETURN DISTINCT u";
        StatementResult result = runWithSession(s -> s.run(q, Values.parameters("elID", ubicacion.getID())));
        return result.list(record -> {
            Value u = record.get(0);
            return u.get("id").asInt();
        });
    }

//    public List<Persona> getHijosDe(Persona padre) {
//        String query = "MATCH (padre:Persona {dni: {elDniPadre}}) " +
//                "MATCH (hijo)-[:hijoDe]->(padre) " +
//                "RETURN hijo";
//        StatementResult result = runWithSession(session ->
//                session.run(query, Values.parameters("elDniPadre", padre.getDni())));
//        //Similar a list.stream().map(...)
//        return result.list(record -> {
//            Value hijo = record.get(0);
//            String dni = hijo.get("dni").asString();
//            String nombre = hijo.get("nombre").asString();
//            String apellido = hijo.get("apellido").asString();
//            return new Persona(dni, nombre, apellido);
//        });
//    }
}