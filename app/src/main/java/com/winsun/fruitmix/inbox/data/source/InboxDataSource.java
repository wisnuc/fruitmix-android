package com.winsun.fruitmix.inbox.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;

/**
 * Created by Administrator on 2018/1/10.
 */

public interface InboxDataSource {

    void getAllGroupInfoAboutUser(String userUUID, BaseLoadDataCallback<GroupUserComment> callback);


}
