package models;

import models.gsonmodels.SalesforceContact;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "serviceUser")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String providerKey;
    private String firstName;
    private String name;
    private String mail;
    @ManyToMany(mappedBy="members", fetch = FetchType.LAZY)
    private List<Group> groups;
    private ServiceProvider provider;

    public User() {
    }

    public User(com.google.api.services.admin.directory.model.User user, HashMap<String, com.google.api.services.admin.directory.model.Group> groups) {
        this.providerKey = user.getId();
        this.firstName = user.getName().getGivenName();
        this.name = user.getName().getFamilyName();
        this.mail = user.getPrimaryEmail();
        this.groups = new ArrayList<>();
        for (com.google.api.services.admin.directory.model.Group group : groups.values()) {
            Group g = Group.getGroupbyProviderKey(group.getId());
            g.addMember(this);
        }
        this.provider = ServiceProvider.GMAIL;
    }

    public User(SalesforceContact contact, String providerKey, HashMap<String, com.google.api.services.admin.directory.model.Group> groups, ServiceProvider provider) {
        this.providerKey = providerKey;
        this.firstName = contact.getFirstName();
        this.name = contact.getLastName();
        this.mail = contact.getEmail();
        this.groups = new ArrayList<>();
        for (com.google.api.services.admin.directory.model.Group group : groups.values()) {
            Group g = Group.getGroupbyProviderKey(group.getId());
            g.addMember(this);
        }
        this.provider = provider;
    }

    public void save() {
        JPA.em().merge(this);
    }


    public static List<User> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(User.class);
        Root<User> c = cq.from(User.class);
        Query query = JPA.em().createQuery(cq);
        List<User> result = query.getResultList();
        return result;
    }

}
