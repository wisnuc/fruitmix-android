package com.winsun.fruitmix.token.manager;

import android.content.Context;

import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.token.param.SCloudTokenParam;
import com.winsun.fruitmix.user.datasource.InjectUser;

/**
 * Created by Administrator on 2018/3/2.
 */

public class InjectSCloudTokenManager {

    public static SCloudTokenManager provideInstance(Context context){

        SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(context);

        String currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        String currentUserGUID = InjectUser.provideRepository(context).getUserByUUID(currentUserUUID).getAssociatedWeChatGUID();

        TokenDataSource tokenDataSource = InjectTokenRemoteDataSource.provideTokenDataSource(context);

        return new SCloudTokenManager(new SCloudTokenParam(currentUserGUID),tokenDataSource);

    }

}
