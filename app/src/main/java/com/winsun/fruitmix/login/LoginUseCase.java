package com.winsun.fruitmix.login;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class LoginUseCase extends BaseDataRepository{

    public static final String TAG = LoginUseCase.class.getSimpleName();

    private static LoginUseCase instance;

    private LoggedInUserDataSource loggedInUserDataSource;

    private TokenDataSource tokenDataSource;

    private HttpRequestFactory httpRequestFactory;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private UploadMediaUseCase uploadMediaUseCase;

    private UserDataRepository userDataRepository;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private StationFileRepository stationFileRepository;

    private SystemSettingDataSource systemSettingDataSource;

    private ImageGifLoaderInstance imageGifLoaderInstance;

    private EventBus eventBus;

    public static String mToken;
    public static String mGateway;

    private LoginUseCase(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                         HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy, UploadMediaUseCase uploadMediaUseCase,
                         UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                         SystemSettingDataSource systemSettingDataSource, ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus,
                         ThreadManager threadManager) {
        super(threadManager);
        this.loggedInUserDataSource = loggedInUserDataSource;
        this.tokenDataSource = tokenDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.uploadMediaUseCase = uploadMediaUseCase;
        this.userDataRepository = userDataRepository;
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.imageGifLoaderInstance = imageGifLoaderInstance;
        this.eventBus = eventBus;
    }

    public static LoginUseCase getInstance(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                                           HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                           UploadMediaUseCase uploadMediaUseCase, UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository,
                                           StationFileRepository stationFileRepository, SystemSettingDataSource systemSettingDataSource,
                                           ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus, ThreadManager threadManager) {

        if (instance == null)
            instance = new LoginUseCase(loggedInUserDataSource, tokenDataSource,
                    httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase, userDataRepository, mediaDataSourceRepository,
                    stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, threadManager);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void loginWithNoParam(BaseOperateDataCallback<Boolean> callback) {

        String currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(currentUserUUID);

        if (loggedInUser == null)
            callback.onFail(new OperationSQLException());
        else {

            Log.d(TAG, "loginWithNoParam: http request factory set current data token:" + loggedInUser.getToken() + " gateway: " + loggedInUser.getGateway());

            mToken = loggedInUser.getToken();
            mGateway = loggedInUser.getGateway();

            initSystemState(loggedInUser.getToken(), loggedInUser.getGateway(), loggedInUser.getUser().getUuid());

            callback.onSucceed(true, new OperationSuccess());

            getUsers();

        }
    }

    private void getUsers() {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>());

    }

    public void loginWithLoadTokenParam(final LoadTokenParam loadTokenParam, final BaseLoadDataCallback<String> callback) {

        tokenDataSource.getToken(loadTokenParam, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                Log.d(TAG, "loginWithLoadTokenParam: http request factory set current data token:" + token + " gateway: " + loadTokenParam.getGateway());

                mToken = token;
                mGateway = loadTokenParam.getGateway();

                initSystemState(token, loadTokenParam.getGateway(), loadTokenParam.getUserUUID());

                userDataRepository.clearAllUsersInDB();

                mediaDataSourceRepository.clearAllStationMediasInDB();

                getUsers(loadTokenParam, token, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void getUsers(final LoadTokenParam loadTokenParam, final String token, final BaseLoadDataCallback<String> callback) {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                for (final User user : data) {
                    if (user.getUuid().equals(loadTokenParam.getUserUUID())) {

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                user.setHome(data.get(0));

                                Collection<LoggedInUser> loggedInUsers = loggedInUserDataSource.getAllLoggedInUsers();

                                if (loggedInUsers.isEmpty()) {

                                    systemSettingDataSource.setAutoUploadOrNot(true);
                                    systemSettingDataSource.setCurrentUploadUserUUID(user.getUuid());

                                    systemSettingDataSource.setShowAutoUploadDialog(false);
                                } else {

                                    systemSettingDataSource.setAutoUploadOrNot(false);
                                    systemSettingDataSource.setShowAutoUploadDialog(true);
                                }

                                LoggedInUser loggedInUser = new LoggedInUser(user.getLibrary(), token, loadTokenParam.getGateway(), loadTokenParam.getEquipmentName(), user);

                                loggedInUserDataSource.insertLoggedInUsers(Collections.singletonList(loggedInUser));

                                callback.onSucceed(Collections.singletonList(token), new OperationSuccess());

                                eventBus.postSticky(new OperationEvent(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN, new OperationSuccess()));

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {

                                callback.onFail(operationResult);

                            }
                        });

                        break;

                    }
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                callback.onFail(operationResult);
            }
        });

    }


    public void loginWithUser(User user, BaseOperateDataCallback<Boolean> callback) {

        BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        Collection<LoggedInUser> loggedInUsers = loggedInUserDataSource.getAllLoggedInUsers();

        LoggedInUser currentLoggedInUser = null;

        for (LoggedInUser loggedInUser : loggedInUsers) {
            if (loggedInUser.getUser().getUuid().equals(user.getUuid()))
                currentLoggedInUser = loggedInUser;

        }

        if (currentLoggedInUser == null)
            runOnMainThreadCallback.onFail(new OperationSQLException());
        else
            loginWithLoggedInUser(currentLoggedInUser, callback);


    }

    public void loginWithLoggedInUser(final LoggedInUser currentLoggedInUser, final BaseOperateDataCallback<Boolean> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                loginWithLoggedInUserInThread(currentLoggedInUser, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    private void loginWithLoggedInUserInThread(LoggedInUser currentLoggedInUser, BaseOperateDataCallback<Boolean> callback) {
        Log.d(TAG, "loginWithLoggedInUser: http request factory set current data token:" + currentLoggedInUser.getToken() + " gateway: " + currentLoggedInUser.getGateway());

        mToken = currentLoggedInUser.getToken();
        mGateway = currentLoggedInUser.getGateway();

        initSystemState(currentLoggedInUser.getToken(), currentLoggedInUser.getGateway(), currentLoggedInUser.getUser().getUuid());

        userDataRepository.clearAllUsersInDB();

        mediaDataSourceRepository.clearAllStationMediasInDB();

        String preUploadUserUUID = systemSettingDataSource.getCurrentUploadUserUUID();

        boolean result;

        if (preUploadUserUUID.equals(currentLoggedInUser.getUser().getUuid())) {

            result = true;
        } else {
            systemSettingDataSource.setAutoUploadOrNot(false);

            result = false;
        }

        callback.onSucceed(result, new OperationSuccess());

        getUsers();
    }

    private void initSystemState(String token, String gateway, String loginUserUUID) {

        Log.i(TAG, "initSystemState: token: " + token + " gateway: " + gateway + " loginUserUUID: " + loginUserUUID);

        httpRequestFactory.setCurrentData(token, gateway);

        checkMediaIsUploadStrategy.setCurrentUserUUID(loginUserUUID);

        checkMediaIsUploadStrategy.setUploadedMediaHashs(Collections.<String>emptyList());

        uploadMediaUseCase.resetState();

        imageGifLoaderInstance.setToken(token);

        systemSettingDataSource.setCurrentLoginUserUUID(loginUserUUID);

        stationFileRepository.clearDownloadFileRecordInCache();

    }


    public void loginWithWeChatCode(String code, final BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        tokenDataSource.getToken(code, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                //TODO: check get gateway and user when login by wechat code

                Log.d(TAG, "loginWithNoParam: http request factory set current data token:" + token + " gateway: " + "10.10.9.49");

                httpRequestFactory.setCurrentData(token, "10.10.9.49");

                imageGifLoaderInstance.setToken(token);

                stationFileRepository.clearDownloadFileRecordInCache();

                runOnMainThreadCallback.onSucceed(true, operationResult);

                getUsers(token);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                runOnMainThreadCallback.onFail(operationResult);

            }
        });


    }

    private void getUsers(String token) {


    }


}
