package com.winsun.fruitmix.invitation.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/12.
 */

public class FakeInvitationDataSource implements InvitationDataSource {

    @Override
    public void createInvitation(BaseOperateDataCallback<String> callback) {

    }

    @Override
    public void getInvitation(BaseLoadDataCallback<ConfirmInviteUser> callback) {

        List<ConfirmInviteUser> confirmInviteUsers = new ArrayList<>();

        for (int i = 0; i < 2; i++) {

            ConfirmInviteUser confirmInviteUser = new ConfirmInviteUser();

            confirmInviteUser.setUserGUID("");
            confirmInviteUser.setOperateType(ConfirmInviteUser.OPERATE_TYPE_PENDING);
            confirmInviteUser.setCreateFormatTime(Util.getCurrentFormatTime());
            confirmInviteUser.setStation("c0c4f63f-029f-4297-aef7-0fdda2cef749");
            confirmInviteUser.setTicketUUID("");
            confirmInviteUser.setUserName("test" + i);
            confirmInviteUser.setUserAvatar("");

            confirmInviteUsers.add(confirmInviteUser);
        }

        callback.onSucceed(confirmInviteUsers, new OperationSuccess());

    }

    @Override
    public void confirmInvitation(ConfirmInviteUser confirmInviteUser, boolean isAccepted, BaseOperateDataCallback<String> callback) {

        callback.onSucceed("", new OperationSuccess());

    }
}
