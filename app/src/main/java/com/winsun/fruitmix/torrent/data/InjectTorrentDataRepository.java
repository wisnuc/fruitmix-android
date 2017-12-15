package com.winsun.fruitmix.torrent.data;

import android.content.Context;

import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;

/**
 * Created by Administrator on 2017/12/14.
 */

public class InjectTorrentDataRepository {

    public static TorrentDataRepository provideInstance(Context context) {

        User currentUser = InjectUser.provideRepository(context).getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(context).getCurrentLoginUserUUID());

        return new TorrentDataRepositoryImpl(ThreadManagerImpl.getInstance(), new TorrentRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                InjectHttp.provideHttpRequestFactory(context)), currentUser, InjectStationFileRepository.provideStationFileRepository(context));

    }

}
