package com.winsun.fruitmix.file.data.station;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectStationFileRepository {

    public static StationFileRepository provideStationFileRepository(Context context) {

        return StationFileRepositoryImpl.getInstance(provideStationFileDataSource(context), provideDownloadedFileDataSource(context), ThreadManagerImpl.getInstance());
    }


    private static StationFileDataSource provideStationFileDataSource(Context context) {

        return StationFileDataSourceImpl.getInstance(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context), InjectHttp.provideIHttpFileUtil());

    }

    private static DownloadedFileDataSource provideDownloadedFileDataSource(Context context) {

        return DownloadFileDataSourceImpl.getInstance(DBUtils.getInstance(context), FileDownloadManager.getInstance());

    }

}
