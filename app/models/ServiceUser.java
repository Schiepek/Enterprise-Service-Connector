package models;

import models.gsonmodels.SalesforceContact;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
@Table(name = "serviceUser")
public class ServiceUser {
    @Id
    @GeneratedValue
    private Long id;
    private String fullName;
    private String company;
    private String usernameJira;
    private String usernameConfluence;
    private String usernameGoogle;
    private String function;
    private String mail;
    private String phoneWork;
    private String phoneMobile;
    private String phoneCompany;
    private String salesforceId;
    @ManyToMany(mappedBy="members", fetch = FetchType.LAZY)
    private List<ServiceGroup> groups;

    public ServiceUser() {
    }

    public ServiceUser(SalesforceContact contact, String username, ServiceProvider provider, String[] groups) {
        this.fullName = contact.getName();
        this.company = contact.getAccountName();
        switch (provider) {
            case GMAIL:
                this.usernameGoogle = username;
                break;
            case JIRA:
                this.usernameJira = username;
                break;
            case CONFLUENCE:
                this.usernameConfluence = username;
                break;
        }
        this.function = contact.getTitle();
        this.mail = contact.getEmail();
        this.phoneWork = contact.getPhone();
        this.phoneMobile = contact.getMobilePhone();
        this.phoneCompany = contact.getAccountPhone();
        this.salesforceId = contact.getId();
        for (String group : groups) {
                ServiceGroup g = ServiceGroup.getGroupByGroupname(group, provider);
                g.addMember(this);
        }

    }

    public ServiceUser(String fullName, String username, String mail, ServiceProvider provider, String[] groups) {
        this.fullName = fullName;
        switch (provider) {
            case GMAIL:
                this.usernameGoogle = username;
                break;
            case JIRA:
                this.usernameJira = username;
                break;
            case CONFLUENCE:
                this.usernameConfluence = username;
                break;
        }
        this.mail = mail;
        for (String group : groups) {
            ServiceGroup g = ServiceGroup.getGroupByGroupname(group, provider);
            g.addMember(this);
        }
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
        switch (provider) {
            case GMAIL:
                cq.where(cb.equal(c.get("usernameGoogle"), username));
                break;
            case JIRA:
                cq.where(cb.equal(c.get("usernameJira"), username));
                break;
            case CONFLUENCE:
                cq.where(cb.equal(c.get("usernameConfluence"), username));
                break;
        }
        Query query = JPA.em().createQuery(cq);
        try {
            return (ServiceUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void addToGroups(String[] groups, ServiceProvider provider) {
        for (String groupName : groups) {
            ServiceGroup group = ServiceGroup.getGroupByGroupname(groupName, provider);
            group.addMember(this);
        }
    }


    public void setMail(String mail) {
        this.mail = mail;
    }

}
