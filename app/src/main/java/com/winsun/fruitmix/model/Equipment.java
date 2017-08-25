package com.winsun.fruitmix.model;

import android.util.Log;

import com.github.druk.rxdnssd.BonjourService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public class Equipment {

    public static final String TAG = Equipment.class.getSimpleName();

    private String serviceName;
    private List<String> hosts;
    private int port;
    private String model;
    private String serialNumber;

    private EquipmentInfo equipmentInfo;

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

    public String getEquipmentName() {
        return getModel() + "-" + getSerialNumber();
    }

    public EquipmentInfo getEquipmentInfo() {
        return equipmentInfo;
    }

    public void setEquipmentInfo(EquipmentInfo equipmentInfo) {
        this.equipmentInfo = equipmentInfo;
    }

    public static Equipment createEquipment(BonjourService bonjourService) {

        if(bonjourService.getInet4Address() == null){
            return null;
        }

        String hostName = bonjourService.getHostname();

        Log.d(TAG, "call: hostName: " + hostName);

        String model = "";
        String serialNumber = "";

        if (hostName != null && hostName.contains("-")) {

            String[] hostNames = hostName.split("-");

            model = hostNames[1].toUpperCase();

            String[] hostNames2 = hostNames[2].split("\\.");

            serialNumber = hostNames2[0].toUpperCase();

            Log.d(TAG, "discovery: model:" + model + " serialNumber:" + serialNumber);
        }

        Equipment equipment = new Equipment();
        String hostAddress = bonjourService.getInet4Address().getHostAddress();
        Log.d(TAG, "host address:" + hostAddress);

        List<String> hosts = new ArrayList<>();
        hosts.add(hostAddress);

        equipment.setHosts(hosts);
        equipment.setPort(bonjourService.getPort());

        equipment.setModel(model);
        equipment.setSerialNumber(serialNumber);

        return equipment;
    }


}
