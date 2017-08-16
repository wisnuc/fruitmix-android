package com.winsun.fruitmix.login;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenRemoteDataSource;
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

public class LoginUseCase {

    private static LoginUseCase instance;

    private LoggedInUserDataSource loggedInUserDataSource;

    private TokenRemoteDataSource tokenRemoteDataSource;

    private HttpRequestFactory httpRequestFactory;

    private UserDataRepository userDataRepository;

    private StationFileRepository stationFileRepository;

    private SystemSettingDataSource systemSettingDataSource;

    private ImageGifLoaderInstance imageGifLoaderInstance;

    private EventBus eventBus;

    private ThreadManager threadManager;

    private LoginUseCase(LoggedInUserDataSource loggedInUserDataSource, TokenRemoteDataSource tokenRemoteDataSource,
                         HttpRequestFactory httpRequestFactory, UserDataRepository userDataRepository,
                         StationFileRepository stationFileRepository, SystemSettingDataSource systemSettingDataSource,
                         ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus, ThreadManager threadManager) {
        this.loggedInUserDataSource = loggedInUserDataSource;
        this.tokenRemoteDataSource = tokenRemoteDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.userDataRepository = userDataRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.imageGifLoaderInstance = imageGifLoaderInstance;
        this.threadManager = threadManager;
        this.eventBus = eventBus;
    }

    public static LoginUseCase getInstance(LoggedInUserDataSource loggedInUserDataSource, TokenRemoteDataSource tokenRemoteDataSource,
                                           HttpRequestFactory httpRequestFactory, UserDataRepository userDataRepository,
                                           StationFileRepository stationFileRepository, SystemSettingDataSource systemSettingDataSource,
                                           ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus, ThreadManager threadManager) {

        if (instance == null)
            instance = new LoginUseCase(loggedInUserDataSource, tokenRemoteDataSource,
                    httpRequestFactory, userDataRepository, stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, threadManager);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void loginWithNoParam(BaseOperateDataCallback<Boolean> callback) {

        LoggedInUser loggedInUser = loggedInUserDataSource.getCurrentLoggedInUser();

        if (loggedInUser == null)
            callback.onFail(new OperationSQLException());
        else {

            httpRequestFactory.setCurrentData(loggedInUser.getToken(), loggedInUser.getGateway());

            imageGifLoaderInstance.setToken(loggedInUser.getToken());

            stationFileRepository.clearDownloadFileRecordInCache();

            callback.onSucceed(true, new OperationSuccess());

            getUsers();

        }
    }

    private void getUsers() {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>());

    }

    public void loginWithLoadTokenParam(final LoadTokenParam loadTokenParam, final BaseLoadDataCallback<String> callback) {

        tokenRemoteDataSource.getToken(loadTokenParam, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                httpRequestFactory.setCurrentData(token, loadTokenParam.getGateway());

                imageGifLoaderInstance.setToken(token);

                stationFileRepository.clearDownloadFileRecordInCache();

                callback.onSucceed(data, operationResult);

                getUsers(loadTokenParam, token);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void getUsers(final LoadTokenParam loadTokenParam, final String token) {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                for (User user : data) {
                    if (user.getUuid().equals(loadTokenParam.getUserUUID())) {

                        Collection<LoggedInUser> loggedInUsers = loggedInUserDataSource.getAllLoggedInUsers();

                        if (loggedInUsers.isEmpty()) {

                            systemSettingDataSource.setAutoUploadOrNot(true);
                            systemSettingDataSource.setCurrentUploadDeviceID(user.getLibrary());

                            systemSettingDataSource.setShowAutoUploadDialog(true);
                        } else {

                            systemSettingDataSource.setShowAutoUploadDialog(false);
                        }

                        LoggedInUser loggedInUser = new LoggedInUser(user.getLibrary(), token, loadTokenParam.getGateway(), loadTokenParam.getEquipmentName(), user);

                        loggedInUserDataSource.insertLoggedInUsers(Collections.singletonList(loggedInUser));

                        loggedInUserDataSource.setCurrentLoggedInUser(loggedInUser);

                        eventBus.postSticky(new OperationEvent(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN, new OperationSuccess()));

                    }
                }

            }

        });

    }


    public void loginWithUser(User user, BaseOperateDataCallback<Boolean> callback) {

        Collection<LoggedInUser> loggedInUsers = loggedInUserDataSource.getAllLoggedInUsers();

        LoggedInUser currentLoggedInUser = null;

        for (LoggedInUser loggedInUser : loggedInUsers) {
            if (loggedInUser.getUser().getUuid().equals(user.getUuid()))
                currentLoggedInUser = loggedInUser;

        }

        if (currentLoggedInUser == null)
            callback.onFail(new OperationSQLException());
        else
            loginWithLoggedInUser(currentLoggedInUser, callback);


    }

    public void loginWithLoggedInUser(LoggedInUser currentLoggedInUser, BaseOperateDataCallback<Boolean> callback) {

        httpRequestFactory.setCurrentData(currentLoggedInUser.getToken(), currentLoggedInUser.getGateway());

        imageGifLoaderInstance.setToken(currentLoggedInUser.getToken());

        loggedInUserDataSource.setCurrentLoggedInUser(currentLoggedInUser);

        stationFileRepository.clearDownloadFileRecordInCache();

        String preDeviceID = systemSettingDataSource.getCurrentUploadDeviceID();

        if (preDeviceID.equals(currentLoggedInUser.getDeviceID())) {
            systemSettingDataSource.setAutoUploadOrNot(true);
        } else {
            systemSettingDataSource.setAutoUploadOrNot(false);
        }

        callback.onSucceed(true, new OperationSuccess());

        getUsers();

    }


    public void loginWithWeChatCode(String code, final BaseOperateDataCallback<Boolean> callback) {

        tokenRemoteDataSource.getToken(code, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                //TODO: check get gateway and user when login by wechat code

                httpRequestFactory.setCurrentData(token, "10.10.9.49");

                imageGifLoaderInstance.setToken(token);

                stationFileRepository.clearDownloadFileRecordInCache();

                callback.onSucceed(true, operationResult);

                getUsers(token);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });


    }

    private void getUsers(String token) {


    }


}
