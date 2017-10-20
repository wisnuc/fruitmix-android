package com.winsun.fruitmix.person.info;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2017/10/19.
 */

public class PersonInfoRepository extends BaseDataRepository implements PersonInfoDataSource {

    private PersonInfoDataSource personInfoDataSource;

    public PersonInfoRepository(ThreadManager threadManager, PersonInfoDataSource personInfoDataSource) {
        super(threadManager);
        this.personInfoDataSource = personInfoDataSource;
    }

    @Override
    public void createBindWeChatUserTicket(final BaseOperateDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                personInfoDataSource.createBindWeChatUserTicket(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void fillBindWeChatUserTicket(final String ticketID, final String wechatUserToken, final BaseOperateDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                personInfoDataSource.fillBindWeChatUserTicket(ticketID, wechatUserToken, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void confirmBindWeChatUserTicket(final String ticketID, final String guid, final boolean isAccept, final BaseOperateDataCallback<String> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                personInfoDataSource.confirmBindWeChatUserTicket(ticketID, guid, isAccept, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
