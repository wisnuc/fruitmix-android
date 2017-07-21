package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.invitation.ConfirmInviteUser;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class RemoteConfirmInviteUsersParser implements RemoteDatasParser<ConfirmInviteUser> {

    @Override
    public List<ConfirmInviteUser> parse(String json) throws JSONException {
        return null;
    }
}
