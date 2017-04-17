package com.winsun.fruitmix.model;

import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public class Equipment {

    private String serviceName;
    private List<String> hosts;
    private int port;
    private String model;
    private String serialNumber;

    public Equipment(String serviceName, List<String> hosts, int port) {
        this.serviceName = serviceName;
        this.hosts = hosts;
        this.port = port;
    }

    public Equipment() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
