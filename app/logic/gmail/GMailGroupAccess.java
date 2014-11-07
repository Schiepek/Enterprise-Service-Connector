package logic.gmail;

import com.google.api.services.admin.directory.model.*;
import models.*;
import com.google.api.services.admin.directory.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GMailGroupAccess {
    private Directory service;
    private final String CUSTOMER = "my_customer";
    private final String TYPE_CUSTOMER = "CUSTOMER";
    private final String TYPE_GROUP = "GROUP";
    private final String TYPE_USER = "USER";
    private final int GOOGLE_MAX_RESULTS = 500;
    private final int GOOGLE_MAX_GROUPS_RESULTS = 1000000;

    public GMailGroupAccess() throws IOException {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL));
    }

    public GMailGroupAccess(APIConfig config) throws IOException {
        service = new GMailConnector(config).getDirectoryService();
    }

    public List<User> getAllUsers() throws IOException {
        List<User> allUsers = new ArrayList<>();
        Directory.Users.List request = service.users().list().setMaxResults(GOOGLE_MAX_RESULTS).setCustomer(CUSTOMER);

        do {
            Users currentPage = request.execute();
            if (currentPage.getUsers() != null) {
                allUsers.addAll(currentPage.getUsers());
                request.setPageToken(currentPage.getNextPageToken());
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0 );

        return allUsers;
    }

    public HashMap<String, User> getCustomerUsers(String id) throws IOException {
        List<User> allUsers = new ArrayList<>();
        HashMap<String, User> members = new HashMap<>();
        Directory.Users.List request = service.users().list().setMaxResults(GOOGLE_MAX_RESULTS).setCustomer(id);

        do {
            Users currentPage = request.execute();
            if (currentPage.getUsers() != null) {
                allUsers.addAll(currentPage.getUsers());
                request.setPageToken(currentPage.getNextPageToken());
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0 );

        for (User u : allUsers) {
            members.put(u.getId(), u);
        }
        return members;
    }

    public List<Group> getAllGroups() throws IOException {
        List<Group> allGroups = new ArrayList<>();
        Directory.Groups.List request =service.groups().list().setMaxResults(GOOGLE_MAX_GROUPS_RESULTS).setCustomer(CUSTOMER);

        do {
            Groups currentPage = request.execute();
            if (currentPage.getGroups() != null) {
                allGroups.addAll(currentPage.getGroups());
                request.setPageToken(currentPage.getNextPageToken());
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0 );

        return allGroups;
    }

    public List<Alias> getGroupAliases(String id) throws IOException {
        List<Alias> allAliases = new ArrayList<>();
        Directory.Groups.Aliases.List request = service.groups().aliases().list(id);

        Aliases currentPage = request.execute();
        if (currentPage.getAliases() != null) {
            allAliases.addAll(currentPage.getAliases());
        }

        return allAliases;
    }

    public HashMap<String, User> getGroupMembers(String id) throws IOException {
        List<Member> allMembers = new ArrayList<>();
        HashMap<String, User> members = new HashMap<>();
        Directory.Members.List request = service.members().list(id).setMaxResults(GOOGLE_MAX_RESULTS);

        do {
            Members currentPage = request.execute();
            if (currentPage.getMembers() != null) {
                allMembers.addAll(currentPage.getMembers());
                request.setPageToken(currentPage.getNextPageToken());
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0 );

        for (Member m : allMembers) {
            if (m.getType().equals(TYPE_GROUP)) {
                members.putAll(getGroupMembers(m.getId()));
            } else if (m.getType().equals(TYPE_USER)) {
                User u = getUser(m.getId());
                members.put(u.getId(), u);
            } else if (m.getType().equals(TYPE_CUSTOMER)) {
                members.putAll(getCustomerUsers(m.getId()));
            }
        }
        return members;
    }

    public User getUser(String id) throws IOException {
        Directory.Users.Get request = service.users().get(id);

        User user = request.execute();
        return user;
    }

    public HashMap<String, Group> getMemberGroups(String id) throws IOException {
        List<Group> allGroups = new ArrayList<>();
        HashMap<String, Group> groups = new HashMap<>();
        Directory.Groups.List request = service.groups().list().setUserKey(id).setMaxResults(GOOGLE_MAX_GROUPS_RESULTS);

        do {
            Groups currentPage = request.execute();
            if (currentPage.getGroups() != null) {
                allGroups.addAll(currentPage.getGroups());
                request.setPageToken(currentPage.getNextPageToken());
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0 );

        for (Group g : allGroups) {
            groups.put(g.getId(), g);
            groups.putAll(getMemberGroups(g.getEmail()));
        }
        return groups;
    }


}
