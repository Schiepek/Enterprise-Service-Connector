package models;

import models.gsonmodels.SalesforceContact;
import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
public class ServiceUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        this.salesforceId = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance() + "/" + contact.getId();
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
        cq.orderBy(cb.asc(c.get("fullName")));
        Query query = JPA.em().createQuery(cq);
        List<ServiceUser> result = query.getResultList();
        return result;
    }

    public static List<ServiceUser> companyUsers(String company) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(ServiceUser.class);
        Root<ServiceUser> c = cq.from(ServiceUser.class);
        if (company == null) {
            cq.where(cb.isNull(c.get("company")));
        } else {
            cq.where(cb.equal(c.get("company"), company));
        }
        cq.orderBy(cb.asc(c.get("fullName")));
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

    public static List<String> allCompanies() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<ServiceUser> root = cq.from(ServiceUser.class);
        cq.select(root.get("company")).distinct(true);
        cq.orderBy(cb.asc(root.get("company")));
        List<String> result = JPA.em().createQuery(cq).getResultList();
        return result;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompany() {
        return company;
    }

    public String getUsernameJira() {
        return usernameJira;
    }

    public String getUsernameConfluence() {
        return usernameConfluence;
    }

    public String getUsernameGoogle() {
        return usernameGoogle;
    }

    public String getFunction() {
        return function;
    }

    public String getMail() {
        return mail;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public String getPhoneCompany() {
        return phoneCompany;
    }

    public String getSalesforceId() {
        return salesforceId;
    }

    public List<ServiceGroup> getGroups() {
        return groups;
    }

}
