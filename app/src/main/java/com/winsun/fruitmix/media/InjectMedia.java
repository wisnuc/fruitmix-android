package com.winsun.fruitmix.media;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.media.local.media.LocalMediaAppDBDataSource;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.local.media.LocalMediaSystemDBDataSource;
import com.winsun.fruitmix.media.remote.media.StationMediaDBDataSource;
import com.winsun.fruitmix.media.remote.media.StationMediaRemoteDataSource;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectMedia {

    public static MediaDataSourceRepository provideMediaDataSourceRepository(Context context) {

        return MediaDataSourceRepositoryImpl.getInstance(provideLocalMediaRepository(context), provideStationMediaRepository(context), CalcMediaDigestStrategy.getInstance(), ThreadManagerImpl.getInstance());

    }

    private static LocalMediaRepository provideLocalMediaRepository(Context context) {

        return LocalMediaRepository.getInstance(LocalMediaAppDBDataSource.getInstance(DBUtils.getInstance(context)), LocalMediaSystemDBDataSource.getInstance(context),
                ThreadManagerImpl.getInstance());

    }

    private static StationMediaRepository provideStationMediaRepository(Context context) {

        return StationMediaRepository.getInstance(StationMediaDBDataSource.getInstance(DBUtils.getInstance(context)), StationMediaRemoteDataSource.getInstance(InjectHttp.provideIHttpUtil(context),
                InjectHttp.provideHttpRequestFactory(context), InjectHttp.provideIHttpFileUtil()));
    }

}
