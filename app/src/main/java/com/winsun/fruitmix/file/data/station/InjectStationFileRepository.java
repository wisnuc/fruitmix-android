package com.winsun.fruitmix.file.data.station;

import android.content.Context;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.SCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;
import com.winsun.fruitmix.token.param.SCloudTokenParam;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectStationFileRepository {

    public static StationFileRepository provideStationFileRepository(Context context) {

        return StationFileRepositoryImpl.getInstance(provideStationFileDataSource(context), ThreadManagerImpl.getInstance());
    }


    private static StationFileDataSource provideStationFileDataSource(Context context) {

        return StationFileDataSourceImpl
                .getInstance(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context));

    }

}
