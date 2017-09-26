package com.winsun.fruitmix.wechat.user;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;

/**
 * Created by Administrator on 2017/9/20.
 */

public class InjectWeChatUserDataSource {

    public static WeChatUserDataSource provideWeChatUserDataSource(Context context) {

        return new WeChatUserDBDDataSource(DBUtils.getInstance(context));

    }

}
