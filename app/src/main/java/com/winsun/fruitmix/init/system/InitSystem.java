package com.winsun.fruitmix.init.system;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.file.data.station.StationFileRepositoryImpl;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
import com.winsun.fruitmix.media.MediaDataSourceRepositoryImpl;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.stations.StationsRepository;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.datasource.UserDataRepositoryImpl;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/7/18.
 */

public class InitSystem {

    public static final String TAG = InitSystem.class.getSimpleName();

    public static void initSystem(Context context) {

        OkHttpUtil.destroyInstance();

        StationsRepository.destroyInstance();

        HttpRequestFactory.destroyInstance();

        ImageGifLoaderInstance.destroyInstance();

        UserDataRepositoryImpl.destroyInstance();

        StationMediaRepository.destroyInstance();

        LocalMediaRepository.destroyInstance();

        MediaDataSourceRepositoryImpl.destroyInstance();

        StationFileRepositoryImpl.destroyInstance();

        LoggedInUserRepository.destroyInstance();

        UploadMediaUseCase.destroyInstance();

        NewPhotoListDataLoader.destroyInstance();

        ButlerService.startButlerService(context);

        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }

        result = FileUtil.createLocalPhotoMiniThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo mini thumbnail folder failed");
        }

        result = FileUtil.createLocalPhotoThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo thumbnail folder failed");
        }

        result = FileUtil.createOriginalPhotoFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create shared photo folder failed");
        }

        result = FileUtil.createAudioRecordFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create audio record folder failed");
        }
    }
}
