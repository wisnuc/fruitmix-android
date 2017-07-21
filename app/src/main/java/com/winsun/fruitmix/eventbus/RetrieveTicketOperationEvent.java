package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class RetrieveTicketOperationEvent extends OperationEvent{

    private List<ConfirmInviteUser> confirmInviteUsers;

    public RetrieveTicketOperationEvent(String action, OperationResult operationResult, List<ConfirmInviteUser> confirmInviteUsers) {
        super(action, operationResult);
        this.confirmInviteUsers = confirmInviteUsers;
    }

    public List<ConfirmInviteUser> getConfirmInviteUsers() {
        return confirmInviteUsers;
    }
}
