package com.winsun.fruitmix.upload.media;

import android.content.Context;
import android.os.Build;

import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/8/18.
 */

public class InjectUploadMediaUseCase {

    public static UploadMediaUseCase provideUploadMediaUseCase(Context context) {
        return UploadMediaUseCase.getInstance(InjectMedia.provideMediaDataSourceRepository(context),
                InjectStationFileRepository.provideStationFileRepository(context),
                InjectLoggedInUser.provideLoggedInUserRepository(context), ThreadManagerImpl.getInstance(),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(context),
                CheckMediaIsUploadStrategy.getInstance(), CheckMediaIsExistStrategy.getInstance(),Build.MODEL, EventBus.getDefault());
    }

}
