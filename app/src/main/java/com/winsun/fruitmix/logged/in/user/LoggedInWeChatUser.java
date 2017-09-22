package com.winsun.fruitmix.logged.in.user;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/9/21.
 */

public class LoggedInWeChatUser extends LoggedInUser {

    private String stationID;

    public LoggedInWeChatUser(String deviceID, String token, String gateway, String equipmentName, User user, String stationID) {
        super(deviceID, token, gateway, equipmentName, user);
        this.stationID = stationID;
    }

    public String getStationID() {
        return stationID;
    }


}
