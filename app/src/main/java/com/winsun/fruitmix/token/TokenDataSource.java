package com.winsun.fruitmix.token;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;

/**
 * Created by Administrator on 2017/8/28.
 */

public interface TokenDataSource {

    void getToken(LoadTokenParam loadTokenParam, BaseLoadDataCallback<String> callback);

    void getToken(String wechatCode, BaseLoadDataCallback<String> callback);

}
