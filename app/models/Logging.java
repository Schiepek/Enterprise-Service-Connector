package models;

import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;

@Entity
public class Logging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date date;
    private String message;


    public Long getId() {
        return id;
    }

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

    public static void log(String message) {
        Logging log = new Logging();
        log.setDate(new Date());
        log.setMessage(message);
        log.save();
    }

    public static void logTransfer(List<String> transferInformation) throws IOException {
        PrintWriter out = null;
        BufferedWriter bufWriter;

        bufWriter = Files.newBufferedWriter(
                Paths.get("transfer.log"),
                Charset.forName("UTF8"),
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);
        out = new PrintWriter(bufWriter, true);

        out.println("*********************************************************************");
        out.println("Salesforce > Gmail Transfer");
        out.println("Date: "  + new Date().toString());
        out.println("---------------------------");
        for(String line : transferInformation) {
            out.println(line);
        }
        if(transferInformation.isEmpty()) {
            out.println("No data transfered, everything up to date!");
        }
        out.println("*********************************************************************");
        out.close();
        Logging.log("Salesforce > Gmail Transfer successfull: " + transferInformation.size() + " Contacts modified");
    }

    public static Logging getLast() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(Logging.class);
        Root<Logging> c = cq.from(Logging.class);
        cq.orderBy(cb.desc(c.get("date")));
        Query query = JPA.em().createQuery(cq);
        query.setMaxResults(1);
        List<Logging> result = query.getResultList();
        return result.get(0);
    }
}
