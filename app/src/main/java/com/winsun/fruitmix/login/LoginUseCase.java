package com.winsun.fruitmix.login;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserRepository;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenRemoteDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class LoginUseCase {

    private static LoginUseCase instance;

    private LoggedInUserRepository loggedInUserRepository;

    private TokenRemoteDataSource tokenRemoteDataSource;

    private HttpRequestFactory httpRequestFactory;

    private UserDataRepository userDataRepository;

    private FileDownloadManager fileDownloadManager;

    private SystemSettingDataSource systemSettingDataSource;

    private LoginUseCase(LoggedInUserRepository loggedInUserRepository, TokenRemoteDataSource tokenRemoteDataSource,
                         HttpRequestFactory httpRequestFactory, UserDataRepository userDataRepository,
                         FileDownloadManager fileDownloadManager, SystemSettingDataSource systemSettingDataSource) {
        this.loggedInUserRepository = loggedInUserRepository;
        this.tokenRemoteDataSource = tokenRemoteDataSource;
        this.httpRequestFactory = httpRequestFactory;
        this.userDataRepository = userDataRepository;
        this.fileDownloadManager = fileDownloadManager;
        this.systemSettingDataSource = systemSettingDataSource;
    }

    public static LoginUseCase getInstance(LoggedInUserRepository loggedInUserRepository, TokenRemoteDataSource tokenRemoteDataSource,
                                           HttpRequestFactory httpRequestFactory, UserDataRepository userDataRepository,
                                           FileDownloadManager fileDownloadManager, SystemSettingDataSource systemSettingDataSource) {

        if (instance == null)
            instance = new LoginUseCase(loggedInUserRepository, tokenRemoteDataSource,
                    httpRequestFactory, userDataRepository, fileDownloadManager, systemSettingDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public boolean loginWithNoParam() {

        LoggedInUser loggedInUser = loggedInUserRepository.getCurrentLoggedInUser();

        if (loggedInUser == null)
            return false;
        else {

            httpRequestFactory.setCurrentData(loggedInUser.getToken(), loggedInUser.getGateway());

            fileDownloadManager.clearFileDownloadItems();

            userDataRepository.setCacheDirty();
            userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>());

            return true;

        }
    }

    public void loginWithLoadTokenParam(final LoadTokenParam loadTokenParam, final BaseLoadDataCallback<String> callback) {

        tokenRemoteDataSource.getToken(loadTokenParam, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                httpRequestFactory.setCurrentData(token, loadTokenParam.getGateway());

                fileDownloadManager.clearFileDownloadItems();

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

                        Collection<LoggedInUser> loggedInUsers = loggedInUserRepository.getAllLoggedInUsers();

                        if (loggedInUsers.isEmpty()) {

                            systemSettingDataSource.setAutoUploadOrNot(true);
                            systemSettingDataSource.setCurrentUploadDeviceID(user.getLibrary());

                            systemSettingDataSource.setShowAutoUploadDialog(true);
                        } else {

                            systemSettingDataSource.setShowAutoUploadDialog(false);
                        }

                        LoggedInUser loggedInUser = new LoggedInUser(user.getLibrary(), token, loadTokenParam.getGateway(), loadTokenParam.getEquipmentName(), user);

                        loggedInUserRepository.insertLoggedInUsers(Collections.singletonList(loggedInUser));

                        loggedInUserRepository.setCurrentLoggedInUser(loggedInUser);

                    }
                }

            }

        });

    }


    public boolean loginWithUser(User user) {

        Collection<LoggedInUser> loggedInUsers = loggedInUserRepository.getAllLoggedInUsers();

        LoggedInUser currentLoggedInUser = null;

        for (LoggedInUser loggedInUser : loggedInUsers) {
            if (loggedInUser.getUser().getUuid().equals(user.getUuid()))
                currentLoggedInUser = loggedInUser;

        }

        return currentLoggedInUser != null && loginWithLoggedInUser(currentLoggedInUser);

    }

    public boolean loginWithLoggedInUser(LoggedInUser currentLoggedInUser) {

        httpRequestFactory.setCurrentData(currentLoggedInUser.getToken(), currentLoggedInUser.getGateway());

        loggedInUserRepository.setCurrentLoggedInUser(currentLoggedInUser);

        fileDownloadManager.clearFileDownloadItems();

        String preDeviceID = systemSettingDataSource.getCurrentUploadDeviceID();

        if (preDeviceID.equals(currentLoggedInUser.getDeviceID())) {
            systemSettingDataSource.setAutoUploadOrNot(true);
        } else {
            systemSettingDataSource.setAutoUploadOrNot(false);
        }

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(new BaseLoadDataCallbackImpl<User>());

        return true;


    }


}
