package com.winsun.fruitmix.invitation.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.invitation.model.ConfirmInviteUser;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface InvitationDataSource {

    void createInvitation(final BaseOperateDataCallback<String> callback);

    void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback);

    void confirmInvitation(final ConfirmInviteUser confirmInviteUser,boolean isAccepted, final BaseOperateDataCallback<String> callback);

}
