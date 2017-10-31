package com.winsun.fruitmix.invitation;

import com.winsun.fruitmix.eventbus.OperationEvent;

/**
 * Created by Andy on 2017/7/12.
 */

public interface ConfirmInviteUserPresenter {

    void createInvitation();

    void getInvitations();

    void acceptInviteUser(ConfirmInviteUser confirmInviteUser);

    void refuseInviteUser(ConfirmInviteUser confirmInviteUser);

    void handleOperationEvent(OperationEvent operationEvent);

    void onDestroy();
}
