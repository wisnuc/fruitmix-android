package com.winsun.fruitmix.invitation.data;

import com.winsun.fruitmix.model.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.invitation.model.ConfirmInviteUser;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2017/8/28.
 */

public class InvitationDataRepository extends BaseDataRepository implements InvitationDataSource {

    private InvitationRemoteDataSource invitationRemoteDataSource;

    public InvitationDataRepository(ThreadManager threadManager, InvitationRemoteDataSource invitationRemoteDataSource) {
        super(threadManager);
        this.invitationRemoteDataSource = invitationRemoteDataSource;
    }

    @Override
    public void createInvitation(final BaseOperateDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                invitationRemoteDataSource.createInvitation(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getInvitation(final BaseLoadDataCallback<ConfirmInviteUser> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                invitationRemoteDataSource.getInvitation(createLoadCallbackRunOnMainThread(callback));

            }
        });

    }

    @Override
    public void confirmInvitation(final ConfirmInviteUser confirmInviteUser, final boolean isAccepted, final BaseOperateDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                invitationRemoteDataSource.confirmInvitation(confirmInviteUser,isAccepted, createOperateCallbackRunOnMainThread(callback));

            }
        });

    }
}
