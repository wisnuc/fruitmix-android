package com.winsun.fruitmix.init.system;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.equipment.search.data.EquipmentMDNSSearchManager;
import com.winsun.fruitmix.file.data.station.StationFileDataSourceImpl;
import com.winsun.fruitmix.file.data.station.StationFileRepositoryImpl;
import com.winsun.fruitmix.group.data.source.GroupRemoteDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.media.MediaDataSourceRepositoryImpl;
import com.winsun.fruitmix.media.local.media.LocalMediaRepository;
import com.winsun.fruitmix.media.remote.media.StationMediaRemoteDataSource;
import com.winsun.fruitmix.media.remote.media.StationMediaRepository;
import com.winsun.fruitmix.mediaModule.model.NewMediaListDataLoader;
import com.winsun.fruitmix.newdesign201804.init.InitNewDesignSystem;
import com.winsun.fruitmix.stations.StationsRemoteDataSource;
import com.winsun.fruitmix.stations.StationsRepository;
import com.winsun.fruitmix.user.datasource.UserDataRepositoryImpl;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/7/18.
 */

public class InitSystem {

    public static final String TAG = InitSystem.class.getSimpleName();

    public static void initSystem(Context context) {

        HttpRequestFactory.destroyInstance();

        EquipmentMDNSSearchManager.destroyInstance();

        OkHttpUtil.destroyInstance();

        GroupRepository.destroyInstance();

        GroupRemoteDataSource.destroyInstance();

        StationsRepository.destroyInstance();

        StationsRemoteDataSource.destroyInstance();

        ImageGifLoaderInstance.destroyInstance();

        UserDataRepositoryImpl.destroyInstance();

        StationMediaRepository.destroyInstance();

        StationMediaRemoteDataSource.destroyInstance();

        LocalMediaRepository.destroyInstance();

        MediaDataSourceRepositoryImpl.destroyInstance();

        StationFileRepositoryImpl.destroyInstance();

        StationFileDataSourceImpl.destroyInstance();

        NewMediaListDataLoader.destroyInstance();

        InitNewDesignSystem.Companion.init();

        boolean result = FileUtil.createDownloadFileStoreFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create download file store folder failed");
        }

        result = FileUtil.createLocalPhotoMiniThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo mini thumbnail folder failed");
        }

        result = FileUtil.createLocalPhotoMiniThumbnailNoMediaFile(context);

        Log.d(TAG, "initSystem: create local photo mini thumb no media file result:" + result);

        result = FileUtil.createLocalPhotoThumbnailFolder();

        if (!result) {
            Log.i(TAG, "onCreate: Create local photo thumbnail folder failed");
        }

        result = FileUtil.createLocalPhotoThumbnailNoMediaFile(context);

        Log.d(TAG, "initSystem: create local photo thumb no media file result:" + result);

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
