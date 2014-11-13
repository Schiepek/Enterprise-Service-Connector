package models;

import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
public class ServiceAlias {
    @Id
    @GeneratedValue
    private Long id;
    private String providerKey;
    private String name;
    private String mail;
    @ManyToOne(fetch = FetchType.LAZY)
    private ServiceGroup group;
    private ServiceProvider provider;

    public ServiceAlias() {
    }

    public ServiceAlias(com.google.api.services.admin.directory.model.Alias alias) {
        this.providerKey = alias.getId();
        this.name = alias.getAlias();
        this.mail = alias.getPrimaryEmail();
        this.provider = ServiceProvider.GMAIL;
    }

    public void save() {
        JPA.em().merge(this);
    }

    public static List<ServiceAlias> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceAlias.class);
        Root<ServiceAlias> c = cq.from(ServiceAlias.class);
        Query query = JPA.em().createQuery(cq);
        List<ServiceAlias> result = query.getResultList();
        return result;
    }
}
