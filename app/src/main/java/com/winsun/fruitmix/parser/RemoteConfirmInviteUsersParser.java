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

public class RemoteConfirmInviteUsersParser implements RemoteDatasParser<ConfirmInviteUser> {

    @Override
    public List<ConfirmInviteUser> parse(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);

        List<ConfirmInviteUser> confirmInviteUsers = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject rootObject = jsonArray.getJSONObject(i);

            String ticketID = rootObject.optString("id");

            JSONArray users = rootObject.getJSONArray("users");

            for (int j = 0; j < users.length(); j++) {

                JSONObject user = users.getJSONObject(j);

                ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();

                confirmInviteUser.setTicketUUID(ticketID);
                confirmInviteUser.setUserGUID(user.getString("userId"));
                confirmInviteUser.setOperateType(user.getString("type"));

                confirmInviteUsers.add(confirmInviteUser);
            }

        }


        return confirmInviteUsers;
    }
}
