package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 16.12.2014.
 */
public class TransferStatus {
    private List<Status> statusList = new ArrayList<>();

    public void addStatus(Status status) {
        statusList.add(status);
    }

    public void removeStatus(Status status) {
        statusList.remove(status);
    }

    public List<Status> getStatusList() {
        return statusList;
    }
}
