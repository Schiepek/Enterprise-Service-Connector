package models;

import com.google.api.services.admin.directory.model.Alias;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "serviceGroup")
public class Group {
    @Id
    @GeneratedValue
    private Long id;
    private String providerKey;
    private String name;
    private String mail;
    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinTable(
            name = "serviceGroupAlias",
            joinColumns = { @JoinColumn(name="groupId", referencedColumnName = "id")},
            inverseJoinColumns = { @JoinColumn(name="aliasId", referencedColumnName = "id", unique = true)}
    )
    private List<models.Alias> aliases;
    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name="serviceUserGroup",
            joinColumns = {@JoinColumn(name="groupID", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "userId", referencedColumnName = "id")}
    )
    private List<User> members;
    private ServiceProvider provider;

    public Group() {
    }

    public Group(com.google.api.services.admin.directory.model.Group group, List<Alias> aliases) {
        this.providerKey = group.getId();
        this.name = group.getName();
        this.mail = group.getEmail();
        this.aliases = new ArrayList<>();
        for (Alias a : aliases) {
            models.Alias alias = new models.Alias(a);
            this.aliases.add(alias);
        }
        this.members = new ArrayList<>();
        this.provider = ServiceProvider.GMAIL;
    }

    public void save() {
        JPA.em().merge(this);
    }

    public static List<Group> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(Group.class);
        Root<Group> c = cq.from(Group.class);
        Query query = JPA.em().createQuery(cq);
        List<Group> result = query.getResultList();
        return result;
    }

    public static Group getGroupbyProviderKey(String key) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(Group.class);
        Root<Group> c = cq.from(Group.class);
        cq.where(cb.equal(c.get("providerKey"), key));
        Query query = JPA.em().createQuery(cq);
        return (Group) query.getSingleResult();
    }

    public void addMember(User user) {
        if (!members.contains(user)) {
            this.members.add(user);
        }
    }

}
