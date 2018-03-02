package com.winsun.fruitmix.login;

import android.content.Context;

import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.upload.InjectUploadFileCase;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logout.InjectLogoutUseCase;
import com.winsun.fruitmix.logout.LogoutUseCase;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.NewMediaListDataLoader;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.usecase.GetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.usecase.InjectGetAllBindingLocalUserUseCase;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.wechat.user.InjectWeChatUserDataSource;

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

        GetAllBindingLocalUserUseCase getAllBindingLocalUserUseCase = InjectGetAllBindingLocalUserUseCase.provideInstance(context);

        String temporaryUploadFolderPath = FileUtil.getTemporaryDataFolderParentFolderPath(context);

        LogoutUseCase logoutUseCase = InjectLogoutUseCase.provideLogoutUseCase(context);

        FileTool fileTool = FileTool.getInstance();

        UploadFileUseCase uploadFileUseCase = InjectUploadFileCase.provideInstance(context);

        NetworkStateManager networkStateManager = InjectNetworkStateManager.provideNetworkStateManager(context);

        FileTaskManager fileTaskManager = FileTaskManager.getInstance();

        LoginNewUserCallbackWrapper<Boolean> loginNewUserCallbackWrapper = new LoginNewUserCallbackWrapper<>(temporaryUploadFolderPath, fileTool,
                uploadFileUseCase, networkStateManager,
                fileTaskManager, systemSettingDataSource, stationFileRepository,logoutUseCase);

        return LoginUseCase.getInstance(loggedInUserDataSource, tokenDataSource, httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase,
                userDataRepository, mediaDataSourceRepository, stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, EventBus.getDefault(),
                ThreadManagerImpl.getInstance(), NewMediaListDataLoader.getInstance(), InjectStation.provideStationDataSource(context),
                getAllBindingLocalUserUseCase, InjectWeChatUserDataSource.provideWeChatUserDataSource(context), loginNewUserCallbackWrapper);

    }

}
