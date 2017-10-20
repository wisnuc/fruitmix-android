package com.winsun.fruitmix.equipment.manage.model;

/**
 * Created by Administrator on 2017/10/17.
 */

public class EquipmentTimeInfo {

    private String localTime = "";
    private String universalTime = "";
    private String RTCTime = "";
    private String timeZone = "";
    private boolean networkTimeOn;
    private boolean NTPSynchronized;

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }

    public String getUniversalTime() {
        return universalTime;
    }

    public void setUniversalTime(String universalTime) {
        this.universalTime = universalTime;
    }

    public String getRTCTime() {
        return RTCTime;
    }

    public void setRTCTime(String RTCTime) {
        this.RTCTime = RTCTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isNetworkTimeOn() {
        return networkTimeOn;
    }

    public void setNetworkTimeOn(boolean networkTimeOn) {
        this.networkTimeOn = networkTimeOn;
    }

    public boolean isNTPSynchronized() {
        return NTPSynchronized;
    }

    public void setNTPSynchronized(boolean NTPSynchronized) {
        this.NTPSynchronized = NTPSynchronized;
    }
}
