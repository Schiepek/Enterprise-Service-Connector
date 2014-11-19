package models;

import com.google.api.services.admin.directory.model.Alias;
import com.google.api.services.admin.directory.model.Group;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ServiceGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String providerKey;
    private String name;
    private String mail;
    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinTable(
            name = "ServiceGroupAlias",
            joinColumns = { @JoinColumn(name="groupId", referencedColumnName = "id")},
            inverseJoinColumns = { @JoinColumn(name="aliasId", referencedColumnName = "id", unique = true)}
    )
    private List<ServiceAlias> aliases;
    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name="ServiceUserGroup",
            joinColumns = {@JoinColumn(name="groupID", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "userId", referencedColumnName = "id")}
    )
    private List<ServiceUser> members;
    private ServiceProvider provider;

    public ServiceGroup() {
        this.members = new ArrayList<>();
    }

    public ServiceGroup(Group group, List<Alias> aliases) {
        this.providerKey = group.getId();
        this.name = group.getName();
        this.mail = group.getEmail();
        this.aliases = new ArrayList<>();
        for (Alias a : aliases) {
            ServiceAlias alias = new ServiceAlias(a);
            this.aliases.add(alias);
        }
        this.members = new ArrayList<>();
        this.provider = ServiceProvider.GMAIL;
    }

    public void save() {
        JPA.em().merge(this);
    }

    public static List<ServiceGroup> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceGroup.class);
        Root<ServiceGroup> c = cq.from(ServiceGroup.class);
        Query query = JPA.em().createQuery(cq);
        List<ServiceGroup> result = query.getResultList();
        return result;
    }

    public static ServiceGroup getGroupbyProviderKey(String key) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceGroup.class);
        Root<ServiceGroup> c = cq.from(ServiceGroup.class);
        cq.where(cb.equal(c.get("providerKey"), key));
        Query query = JPA.em().createQuery(cq);
        return (ServiceGroup) query.getSingleResult();
    }

    public static ServiceGroup getGroupByGroupname(String groupname, ServiceProvider provider) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceGroup.class);
        Root<ServiceGroup> c = cq.from(ServiceGroup.class);
        Predicate isGroupName = cb.equal(c.get("name"), groupname);
        Predicate hasProvider = cb.equal(c.get("provider"), provider);
        cq.where(cb.and(isGroupName, hasProvider));
        Query query = JPA.em().createQuery(cq);
        return (ServiceGroup) query.getSingleResult();
    }

    public static List<ServiceGroup> serviceGroups(ServiceProvider provider) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceGroup.class);
        Root<ServiceGroup> c = cq.from(ServiceGroup.class);
        cq.where(cb.equal(c.get("provider"), provider));
        Query query = JPA.em().createQuery(cq);
        List<ServiceGroup> result = query.getResultList();
        return result;
    }

    public void addMember(ServiceUser user) {
        if (!members.contains(user)) {
            this.members.add(user);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public List<ServiceUser> getMembers() {
        return members;
    }

    public String getMail() {
        return mail;
    }

    public List<ServiceAlias> getAliases() {
        return aliases;
    }
}
