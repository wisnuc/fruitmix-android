package com.winsun.fruitmix.contact.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/1/27.
 */

public class ContactDataRepository extends BaseDataRepository implements ContactDataSource {

    private ContactDataSource mContactRemoteDataSource;

    public ContactDataRepository(ThreadManager threadManager, ContactDataSource contactRemoteDataSource) {
        super(threadManager);
        mContactRemoteDataSource = contactRemoteDataSource;
    }

    @Override
    public void getContacts(final String currentUserGUID,final BaseLoadDataCallback<User> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                mContactRemoteDataSource.getContacts(currentUserGUID,createLoadCallbackRunOnMainThread(callback));

            }
        });

    }

}
