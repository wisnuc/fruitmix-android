package com.winsun.fruitmix.invitation;

import android.widget.ImageView;

import com.winsun.fruitmix.eventbus.OperationEvent;

/**
 * Created by Andy on 2017/7/12.
 */

public interface ConfirmInviteUserPresenter {

    void getInvitations();

    void acceptInviteUser(ConfirmInviteUser confirmInviteUser);

    void refuseInviteUser(ConfirmInviteUser confirmInviteUser);

    void postOperation(String ticketID);

    void handleOperationEvent(OperationEvent operationEvent);

}
