package com.winsun.fruitmix.person.info;

import com.winsun.fruitmix.callback.BaseOperateDataCallback;

/**
 * Created by Administrator on 2017/10/19.
 */

public interface PersonInfoDataSource {

    void createBindWeChatUserTicket(BaseOperateDataCallback<String> callback);

    void fillBindWeChatUserTicket(String ticketID, String wechatUserToken, BaseOperateDataCallback<String> callback);

    void confirmBindWeChatUserTicket(String ticketID, String guid, boolean isAccept, BaseOperateDataCallback<String> callback);

}
