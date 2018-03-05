package com.winsun.fruitmix.token.manager;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.token.param.TokenParam;

/**
 * Created by Administrator on 2018/3/1.
 */

public abstract class TokenManager<T extends TokenParam>{

    protected T mTokenParam;
    protected TokenDataSource mTokenDataSource;

    public TokenManager(T tokenParam, TokenDataSource tokenDataSource) {
        mTokenParam = tokenParam;
        mTokenDataSource = tokenDataSource;
    }

    public abstract void resetToken();

    public abstract void getToken(boolean changeThread,BaseLoadDataCallback<String> callback);

}
