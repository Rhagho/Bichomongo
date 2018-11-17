package epers.bichomon.dao.hibernate;

import epers.bichomon.model.bicho.Bicho;
import epers.bichomon.model.ubicacion.Dojo;
import epers.bichomon.model.ubicacion.Ubicacion;
import epers.bichomon.service.runner.Runner;
import org.hibernate.query.Query;

import java.util.List;
import java.util.stream.Collectors;

public class UbicacionDAOHib extends GenericDAOHib {

    public void save(Ubicacion ubicacion) {
        super.save(ubicacion);
    }

    public Ubicacion get(String ubicacion) {
        return super.getByName(Ubicacion.class, ubicacion);
    }

    private Ubicacion getByID(Integer ubicacion) {
        return super.get(Ubicacion.class, ubicacion);
    }

    public List<Ubicacion> getByIDs(List<Integer> ids) {
        return ids.stream().map(this::getByID).collect(Collectors.toList());
    }

    public Dojo getDojo(String dojo) {
        return super.getByName(Dojo.class, dojo);
    }

    public Bicho campeonHistorico(String dojo) {
        String hq1 = "select c.campeon from Dojo d inner join d.campeones c where d.nombre = :dojo order by DATEDIFF(IFNULL(c.hasta,NOW()),c.desde) desc";
        Query<Bicho> query = Runner.getCurrentSession().createQuery(hq1, Bicho.class);
        query.setParameter("dojo", dojo);
        query.setMaxResults(1);
        return query.getSingleResult();
    }
}
