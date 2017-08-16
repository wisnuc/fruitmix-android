package com.winsun.fruitmix.login;

import android.content.Context;

import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.TokenRemoteDataSource;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/7/28.
 */

public class InjectLoginUseCase {

    public static LoginUseCase provideLoginUseCase(Context context) {

        LoggedInUserDataSource loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(context);

        HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory();

        TokenRemoteDataSource tokenRemoteDataSource = InjectTokenRemoteDataSource.provideTokenRemoteDataSource(context);

        StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(context);

        SystemSettingDataSource systemSettingDataSource = SystemSettingDataSource.getInstance(context);

        UserDataRepository userDataRepository = InjectUser.provideRepository(context);

        ImageGifLoaderInstance imageGifLoaderInstance = InjectHttp.provideImageGifLoaderIntance();

        return LoginUseCase.getInstance(loggedInUserDataSource, tokenRemoteDataSource, httpRequestFactory,
                userDataRepository, stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, EventBus.getDefault(), ThreadManager.getInstance());

    }

}
