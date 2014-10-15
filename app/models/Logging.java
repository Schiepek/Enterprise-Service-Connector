package models;

import play.db.jpa.JPA;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Entity
public class Logging {
    @Id
    @GeneratedValue
    private Long id;

    private Date date;
    private String message;


    public Long getId() { return id; }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Logging getLog(Long id) {
        return JPA.em().find(Logging.class, id);
    }

    public static List<Logging> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Logging> cq = cb.createQuery(Logging.class);
        Root<Logging> root = cq.from(Logging.class);
        CriteriaQuery<Logging> all = cq.select(root);
        TypedQuery<Logging> allQuery = JPA.em().createQuery(all);
        return allQuery.getResultList();
    }

    public void save() {
        JPA.em().persist(this);
    }

    public static void delete(Long id) {
        Logging config = getLog(id);
        JPA.em().remove(config);
    }
}
