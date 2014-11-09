package models;

import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
@Table(name = "serviceAlias")
public class Alias {
    @Id
    @GeneratedValue
    private Long id;
    private String providerKey;
    private String name;
    private String mail;
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;
    private ServiceProvider provider;

    public Alias() {
    }

    public Alias (com.google.api.services.admin.directory.model.Alias alias) {
        this.providerKey = alias.getId();
        this.name = alias.getAlias();
        this.mail = alias.getPrimaryEmail();
        this.provider = ServiceProvider.GMAIL;
    }

    public void save() {
        JPA.em().merge(this);
    }

    public static List<Alias> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(Alias.class);
        Root<Alias> c = cq.from(Alias.class);
        Query query = JPA.em().createQuery(cq);
        List<Alias> result = query.getResultList();
        return result;
    }
}
