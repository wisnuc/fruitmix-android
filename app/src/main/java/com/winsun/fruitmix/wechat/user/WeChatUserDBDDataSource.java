package com.winsun.fruitmix.wechat.user;

import com.winsun.fruitmix.db.DBUtils;

/**
 * Created by Administrator on 2017/9/20.
 */

public class WeChatUserDBDDataSource implements WeChatUserDataSource {

    private DBUtils dbUtils;

    WeChatUserDBDDataSource(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public boolean insertWeChatUser(WeChatUser weChatUser) {
        return dbUtils.insertWeChatUser(weChatUser) > 0;
    }

    @Override
    public WeChatUser getWeChatUser(String token, String guid) {
        return dbUtils.getWeChatUserByToken(token,guid);
    }

    @Override
    public boolean deleteWeChatUser(String token) {
        return dbUtils.deleteWeChatUser(token) > 0;
    }


}
