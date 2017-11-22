package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class OperationMoreThanOneStation extends OperationResult {

    private List<LoggedInWeChatUser> mLoggedInWeChatUsers;

    private WeChatTokenUserWrapper mWeChatTokenUserWrapper;

    public OperationMoreThanOneStation(List<LoggedInWeChatUser> loggedInWeChatUsers, WeChatTokenUserWrapper weChatTokenUserWrapper) {
        mLoggedInWeChatUsers = loggedInWeChatUsers;
        mWeChatTokenUserWrapper = weChatTokenUserWrapper;
    }

    @Override
    public String getResultMessage(Context context) {
        return "";
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.MORE_THAN_ONE_STATION;
    }

    public List<LoggedInWeChatUser> getLoggedInWeChatUsers() {
        return mLoggedInWeChatUsers;
    }

    public WeChatTokenUserWrapper getWeChatTokenUserWrapper() {
        return mWeChatTokenUserWrapper;
    }
}
