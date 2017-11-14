package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.invitation.ConfirmInviteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class RemoteConfirmInviteUsersParser extends BaseRemoteDataParser implements RemoteDatasParser<ConfirmInviteUser> {

    @Override
    public List<ConfirmInviteUser> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray jsonArray = new JSONArray(root);

        List<ConfirmInviteUser> confirmInviteUsers = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject rootObject = jsonArray.getJSONObject(i);

            String time = rootObject.optString("createdAt");

            String ticketID = rootObject.optString("id");

            String stationID = rootObject.optString("stationId");

            JSONArray users = rootObject.optJSONArray("users");

            if(users == null)
                continue;

            for (int j = 0; j < users.length(); j++) {

                JSONObject user = users.getJSONObject(j);

                ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();

                confirmInviteUser.setStation(stationID);
                confirmInviteUser.setCreateFormatTime(time);
                confirmInviteUser.setTicketUUID(ticketID);
                confirmInviteUser.setUserGUID(user.optString("userId"));
                confirmInviteUser.setOperateType(user.optString("type"));

                confirmInviteUser.setUserName(user.optString("nickName"));
                confirmInviteUser.setUserAvatar(user.optString("avatarUrl"));

                confirmInviteUsers.add(confirmInviteUser);
            }

        }


        return confirmInviteUsers;
    }
}
