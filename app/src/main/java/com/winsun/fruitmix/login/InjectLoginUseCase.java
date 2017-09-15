package com.winsun.fruitmix.login;

import android.content.Context;

import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectLoginUseCase {

    public static LoginUseCase provideLoginUseCase(Context context) {

        LoggedInUserDataSource loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(context);

        HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);

        CheckMediaIsUploadStrategy checkMediaIsUploadStrategy = CheckMediaIsUploadStrategy.getInstance();

        UploadMediaUseCase uploadMediaUseCase = InjectUploadMediaUseCase.provideUploadMediaUseCase(context);

        TokenDataSource tokenDataSource = InjectTokenRemoteDataSource.provideTokenDataSource(context);

        StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(context);

        SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(context);

        UserDataRepository userDataRepository = InjectUser.provideRepository(context);

        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(context);

        ImageGifLoaderInstance imageGifLoaderInstance = InjectHttp.provideImageGifLoaderInstance(context);

        return LoginUseCase.getInstance(loggedInUserDataSource, tokenDataSource, httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase,
                userDataRepository, mediaDataSourceRepository, stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, EventBus.getDefault(),
                ThreadManagerImpl.getInstance(), NewPhotoListDataLoader.getInstance(), InjectStation.provideStationDataSource(context));

    }

}
