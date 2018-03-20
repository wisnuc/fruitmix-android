package com.winsun.fruitmix.stations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/9/14.
 */

public class Station {

    private String id;
    private String label;
    private List<String> ips;
    private boolean isOnline;

    public Station() {

        ips = new ArrayList<>();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label != null ? label : "";
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void addIp(String ip) {
        ips.add(ip);
    }

    public List<String> getIps() {
        return Collections.unmodifiableList(ips);
    }

    public String getFirstIp() {
        return ips.size() > 0 ? ips.get(0) : "";
    }


}
