package com.winsun.fruitmix.wechat.user;



/**
 * Created by Administrator on 2017/9/20.
 */

public interface WeChatUserDataSource {

    boolean insertWeChatUser(WeChatUser weChatUser);

    WeChatUser getWeChatUser(String token, String stationID);

    boolean deleteWeChatUser(String token);

}
