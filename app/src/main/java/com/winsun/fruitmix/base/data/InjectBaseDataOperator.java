package com.winsun.fruitmix.base.data;

import android.content.Context;

import com.winsun.fruitmix.base.data.retry.DefaultHttpRetryStrategy;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.manager.TokenManager;

/**
 * Created by Administrator on 2018/3/2.
 */

public class InjectBaseDataOperator {

    public static BaseDataOperator provideInstance(Context context, TokenManager tokenManager, SCloudTokenContainer sCloudTokenContainer,
                                                   DefaultHttpRetryStrategy defaultHttpRetryStrategy){

        SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(context);

        return new BaseDataOperator(systemSettingDataSource,tokenManager,
                sCloudTokenContainer,defaultHttpRetryStrategy);

    }

}
