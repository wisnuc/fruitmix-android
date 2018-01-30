package com.winsun.fruitmix.contact.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2018/1/27.
 */

public interface ContactDataSource {

    void getContacts(BaseLoadDataCallback<User> callback);



}
