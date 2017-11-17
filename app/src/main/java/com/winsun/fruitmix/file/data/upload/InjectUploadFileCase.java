package com.winsun.fruitmix.file.data.upload;

import android.content.Context;
import android.os.Build;

import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/11/15.
 */

public class InjectUploadFileCase {

    public static UploadFileUseCase provideInstance(Context context) {

        String uploadFolderName = Build.MANUFACTURER + "-" + Build.MODEL;

        String fileTemporaryFolderName = FileUtil.getExternalCacheDirPath(context);

        return new UploadFileUseCase(InjectUser.provideRepository(context), InjectStationFileRepository.provideStationFileRepository(context),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(context), InjectNetworkStateManager.provideNetworkStateManager(context),
                uploadFolderName,fileTemporaryFolderName);

    }

}
