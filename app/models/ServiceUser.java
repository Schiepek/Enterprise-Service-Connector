package models;

import models.gsonmodels.SalesforceContact;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "serviceUser")
public class ServiceUser {
    @Id
    @GeneratedValue
    private Long id;
    private String providerKey;
    private String firstName;
    private String name;
    private String mail;
    @ManyToMany(mappedBy="members", fetch = FetchType.LAZY)
    private List<ServiceGroup> groups;
    private ServiceProvider provider;

    public ServiceUser() {
    }

    public ServiceUser(com.google.api.services.admin.directory.model.User user, HashMap<String, com.google.api.services.admin.directory.model.Group> groups) {
        this.providerKey = user.getId();
        this.firstName = user.getName().getGivenName();
        this.name = user.getName().getFamilyName();
        this.mail = user.getPrimaryEmail();
        this.groups = new ArrayList<>();
        for (com.google.api.services.admin.directory.model.Group group : groups.values()) {
            ServiceGroup g = ServiceGroup.getGroupbyProviderKey(group.getId());
            g.addMember(this);
        }
        this.provider = ServiceProvider.GMAIL;
    }

    public ServiceUser(SalesforceContact contact, String providerKey, HashMap<String, com.google.api.services.admin.directory.model.Group> groups, ServiceProvider provider) {
        this.providerKey = providerKey;
        this.firstName = contact.getFirstName();
        this.name = contact.getLastName();
        this.mail = contact.getEmail();
        this.groups = new ArrayList<>();
        for (com.google.api.services.admin.directory.model.Group group : groups.values()) {
            ServiceGroup g = ServiceGroup.getGroupbyProviderKey(group.getId());
            g.addMember(this);
        }
        this.provider = provider;
    }

    public void save() {
        JPA.em().merge(this);
    }


    public static List<ServiceUser> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceUser.class);
        Root<ServiceUser> c = cq.from(ServiceUser.class);
        Query query = JPA.em().createQuery(cq);
        List<ServiceUser> result = query.getResultList();
        return result;
    }

    public static ServiceUser getUserByUsername(String username, ServiceProvider provider) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceUser.class);
        Root<ServiceUser> c = cq.from(ServiceUser.class);
        Predicate isGroupName = cb.equal(c.get("name"), username);
        Predicate hasProvider = cb.equal(c.get("provider"), provider);
        cq.where(cb.and(isGroupName, hasProvider));
        Query query = JPA.em().createQuery(cq);
        try {
            return (ServiceUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
