package com.winsun.fruitmix.login;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationMoreThanOneStation;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.wechat.user.WeChatUser;
import com.winsun.fruitmix.wechat.user.WeChatUserDataSource;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/14.
 */

public class LoginUseCase extends BaseDataRepository {

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

    private NewPhotoListDataLoader newPhotoListDataLoader;

    private EventBus eventBus;

    private StationsDataSource stationsDataSource;

    private WeChatUserDataSource weChatUserDataSource;

    private static String mToken;
    private static String mGateway;

    private LoginUseCase(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                         HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy, UploadMediaUseCase uploadMediaUseCase,
                         UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                         SystemSettingDataSource systemSettingDataSource, ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus,
                         ThreadManager threadManager, NewPhotoListDataLoader newPhotoListDataLoader, StationsDataSource stationsDataSource,
                         WeChatUserDataSource weChatUserDataSource) {

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
        this.newPhotoListDataLoader = newPhotoListDataLoader;
        this.eventBus = eventBus;
        this.stationsDataSource = stationsDataSource;
        this.weChatUserDataSource = weChatUserDataSource;

    }

    public static LoginUseCase getInstance(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                                           HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                           UploadMediaUseCase uploadMediaUseCase, UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository,
                                           StationFileRepository stationFileRepository, SystemSettingDataSource systemSettingDataSource,
                                           ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus, ThreadManager threadManager,
                                           NewPhotoListDataLoader newPhotoListDataLoader, StationsDataSource stationsDataSource,
                                           WeChatUserDataSource weChatUserDataSource) {

        if (instance == null)
            instance = new LoginUseCase(loggedInUserDataSource, tokenDataSource,
                    httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase, userDataRepository, mediaDataSourceRepository,
                    stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, threadManager,
                    newPhotoListDataLoader, stationsDataSource, weChatUserDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public static String getToken() {

        return mToken;
    }

    public static String getGateway() {
        return mGateway;
    }

    public void loginWithNoParam(final BaseOperateDataCallback<Boolean> callback) {

        String currentLoginToken = systemSettingDataSource.getCurrentLoginToken();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByToken(currentLoginToken);

        if (loggedInUser == null) {

            String currentLoginStationID = systemSettingDataSource.getCurrentLoginStationID();

            final WeChatUser weChatUser = weChatUserDataSource.getWeChatUser(currentLoginToken, currentLoginStationID);

            loginWithWeChatUser(callback, weChatUser);


        } else {

            Log.d(TAG, "loginWithNoParam: http request factory set current data token:" + loggedInUser.getToken() + " gateway: " + loggedInUser.getGateway());

            mToken = loggedInUser.getToken();
            mGateway = loggedInUser.getGateway();

            httpRequestFactory.setCurrentData(mToken, mGateway);

            initSystemState(loggedInUser.getToken(), loggedInUser.getGateway(), loggedInUser.getUser().getUuid());

            getUsers(loggedInUser.getUser().getUuid(), new BaseOperateDataCallback<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {

                    callback.onSucceed(true, new OperationSuccess());

                }

                @Override
                public void onFail(OperationResult result) {

                    callback.onFail(result);

                }
            });

        }
    }

    private void loginWithWeChatUser(final BaseOperateDataCallback<Boolean> callback, final WeChatUser weChatUser) {
        if (weChatUser == null)
            callback.onFail(new OperationSQLException());
        else {

            mToken = weChatUser.getToken();

            mGateway = HttpRequestFactory.CLOUD_IP;

            httpRequestFactory.setCurrentData(mToken, mGateway);

            userDataRepository.getUserByGUIDWithCloudAPI(weChatUser.getGuid(), new BaseLoadDataCallback<User>() {
                @Override
                public void onSucceed(final List<User> data, OperationResult operationResult) {

                    WeChatTokenUserWrapper weChatTokenUserWrapper = new WeChatTokenUserWrapper();

                    User user = data.get(0);

                    weChatTokenUserWrapper.setAvatarUrl(user.getAvatar());
                    weChatTokenUserWrapper.setNickName(user.getUserName());
                    weChatTokenUserWrapper.setToken(weChatUser.getToken());
                    weChatTokenUserWrapper.setGuid(weChatUser.getGuid());

                    getUsersAfterChooseStationID(weChatTokenUserWrapper, weChatUser.getStationID(), new BaseOperateDataCallbackImpl<Boolean>() {
                        @Override
                        public void onSucceed(Boolean data, OperationResult result) {
                            super.onSucceed(data, result);

                            callback.onSucceed(data, result);

                        }

                        @Override
                        public void onFail(OperationResult result) {
                            super.onFail(result);

                            callback.onFail(result);

                        }
                    });

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    callback.onFail(operationResult);

                }
            });

        }
    }

    private void getUsers(final String currentUserUUID, final BaseOperateDataCallback<Boolean> callback) {

        systemSettingDataSource.setLoginWithWechatCodeOrNot(false);

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(currentUserUUID, new BaseLoadDataCallbackImpl<User>() {

            @Override
            public void onSucceed(final List<User> users, OperationResult operationResult) {
                super.onSucceed(users, operationResult);

                for (final User user : users) {

                    if (user.getUuid().equals(currentUserUUID)) {

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                user.setHome(data.get(0));

                                userDataRepository.insertUsers(users);

                                callback.onSucceed(true, new OperationSuccess());

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {
                                callback.onFail(operationResult);
                            }
                        });

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

    public void loginWithLoadTokenParam(final LoadTokenParam loadTokenParam, final BaseLoadDataCallback<String> callback) {

        tokenDataSource.getToken(loadTokenParam, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                String token = data.get(0);

                Log.d(TAG, "loginWithLoadTokenParam: http request factory set current data token:" + token + " gateway: " + loadTokenParam.getGateway());

                mToken = token;
                mGateway = loadTokenParam.getGateway();

                httpRequestFactory.setCurrentData(mToken, mGateway);

                userDataRepository.clearAllUsersInDB();

                getUsers(loadTokenParam, token, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void getUsers(final LoadTokenParam loadTokenParam, final String token, final BaseLoadDataCallback<String> callback) {

        systemSettingDataSource.setLoginWithWechatCodeOrNot(false);

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(loadTokenParam.getUserUUID(), new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(final List<User> users, OperationResult operationResult) {

                for (final User user : users) {
                    if (user.getUuid().equals(loadTokenParam.getUserUUID())) {

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                user.setHome(data.get(0));

                                userDataRepository.insertUsers(users);

                                systemSettingDataSource.setAutoUploadOrNot(false);

                                systemSettingDataSource.setShowAutoUploadDialog(true);

                                LoggedInUser loggedInUser = new LoggedInUser(user.getLibrary(), token, loadTokenParam.getGateway(), loadTokenParam.getEquipmentName(), user);

                                boolean result = loggedInUserDataSource.insertLoggedInUsers(Collections.singletonList(loggedInUser));

                                Log.d(TAG, "onSucceed: insert result :" + result);

                                initSystemState(token, loadTokenParam.getGateway(), loadTokenParam.getUserUUID());

                                mediaDataSourceRepository.clearAllStationMediasInDB();

                                mediaDataSourceRepository.resetState();

                                callback.onSucceed(Collections.singletonList(token), new OperationSuccess());

//                                eventBus.postSticky(new OperationEvent(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN, new OperationSuccess()));

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

    private void loginWithLoggedInUserInThread(LoggedInUser currentLoggedInUser, final BaseOperateDataCallback<Boolean> callback) {
        Log.d(TAG, "loginWithLoggedInUser: http request factory set current data token:" + currentLoggedInUser.getToken() + " gateway: " + currentLoggedInUser.getGateway());

        mToken = currentLoggedInUser.getToken();
        mGateway = currentLoggedInUser.getGateway();

        httpRequestFactory.setCurrentData(mToken, mGateway);

        initSystemState(currentLoggedInUser.getToken(), currentLoggedInUser.getGateway(), currentLoggedInUser.getUser().getUuid());

        userDataRepository.clearAllUsersInDB();

        mediaDataSourceRepository.clearAllStationMediasInDB();

        mediaDataSourceRepository.resetState();

        String preUploadUserUUID = systemSettingDataSource.getCurrentUploadUserUUID();

        if (preUploadUserUUID.equals(currentLoggedInUser.getUser().getUuid())) {

            systemSettingDataSource.setAutoUploadOrNot(true);

        } else {
            systemSettingDataSource.setAutoUploadOrNot(false);

        }

        getUsers(currentLoggedInUser.getUser().getUuid(), new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {
                callback.onSucceed(true, new OperationSuccess());
            }

            @Override
            public void onFail(OperationResult result) {
                callback.onFail(result);
            }
        });
    }

    private void initSystemState(String token, String gateway, String loginUserUUID) {

        Log.i(TAG, "initSystemState: token: " + token + " gateway: " + gateway + " loginUserUUID: " + loginUserUUID);

        checkMediaIsUploadStrategy.setCurrentUserUUID(loginUserUUID);

        checkMediaIsUploadStrategy.setUploadedMediaHashs(new ArrayList<String>());

        uploadMediaUseCase.resetState();

        imageGifLoaderInstance.setToken(token);

        systemSettingDataSource.setCurrentLoginUserUUID(loginUserUUID);

        systemSettingDataSource.setCurrentLoginToken(token);

        systemSettingDataSource.setCurrentEquipmentIp(gateway);

        systemSettingDataSource.setCurrentLoginUserGUID("");

        systemSettingDataSource.setCurrentLoginStationID("");

        stationFileRepository.clearDownloadFileRecordInCache();

        newPhotoListDataLoader.setNeedRefreshData(true);

        ImageGifLoaderInstance.destroyInstance();

    }


    public void loginWithWeChatCode(String code, final BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        tokenDataSource.getToken(code, new BaseLoadDataCallback<WeChatTokenUserWrapper>() {
            @Override
            public void onSucceed(List<WeChatTokenUserWrapper> data, OperationResult operationResult) {

                WeChatTokenUserWrapper weChatTokenUserWrapper = data.get(0);

                String token = weChatTokenUserWrapper.getToken();

                //TODO: check get gateway and user when login by wechat code

                Log.d(TAG, "loginWithWeChatCode: http request factory set current data token: " + token + " gateway: "
                        + HttpRequestFactory.CLOUD_IP + " guid: " + weChatTokenUserWrapper.getGuid());

                mToken = token;

                mGateway = HttpRequestFactory.CLOUD_IP;

                httpRequestFactory.setCurrentData(mToken, mGateway);

                systemSettingDataSource.setCurrentLoginToken(mToken);

                getStationList(weChatTokenUserWrapper, new BaseOperateDataCallback<Boolean>() {
                    @Override
                    public void onSucceed(Boolean data, OperationResult result) {
                        runOnMainThreadCallback.onSucceed(data, result);
                    }

                    @Override
                    public void onFail(OperationResult result) {

                        systemSettingDataSource.setCurrentLoginToken("");

                        runOnMainThreadCallback.onFail(result);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                runOnMainThreadCallback.onFail(operationResult);

            }
        });

    }

    private void getStationList(final WeChatTokenUserWrapper weChatTokenUserWrapper, final BaseOperateDataCallback<Boolean> callback) {

        stationsDataSource.getStationsByWechatGUID(weChatTokenUserWrapper.getGuid(), new BaseLoadDataCallback<Station>() {
            @Override
            public void onSucceed(List<Station> data, OperationResult operationResult) {

                Log.d(TAG, "onSucceed: station size: " + data.size());

                if (data.size() == 1) {

                    final String stationID = data.get(0).getId();

                    getUsersAfterChooseStationID(weChatTokenUserWrapper, stationID, new BaseOperateDataCallback<Boolean>() {
                        @Override
                        public void onSucceed(Boolean data, OperationResult result) {

                            handleLoginWithWeChatCodeSucceed(weChatTokenUserWrapper.getGuid(), weChatTokenUserWrapper.getToken(), stationID);

                            callback.onSucceed(data, result);
                        }

                        @Override
                        public void onFail(OperationResult result) {

                            callback.onFail(result);
                        }
                    });

                } else if (data.size() > 1) {

                    callback.onSucceed(true, new OperationMoreThanOneStation(data, weChatTokenUserWrapper));

                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });


    }

    public void getUsersAfterChooseStationID(final WeChatTokenUserWrapper weChatTokenUserWrapper, final String stationID, final BaseOperateDataCallback<Boolean> callback) {

        Log.d(TAG, "getUsersAfterChooseStationID: stationID: " + stationID);

        systemSettingDataSource.setLoginWithWechatCodeOrNot(true);

        httpRequestFactory.setStationID(stationID);

        userDataRepository.getUsersByStationIDWithCloudAPI(stationID, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                User currentLocalUser = null;

                for (User user : data) {

                    if (user.getAssociatedWeChatGUID().equals(weChatTokenUserWrapper.getGuid())) {

                        currentLocalUser = user;
                        break;
                    }

                }

                if (currentLocalUser == null) {

                    Log.d(TAG, "onSucceed: not find associatedWechatGUID");

                    callback.onFail(new OperationIOException());

                } else {

                    currentLocalUser.setAssociatedWeChatUserName(weChatTokenUserWrapper.getNickName());
                    currentLocalUser.setAvatar(weChatTokenUserWrapper.getAvatarUrl());

                    getUserByUserUUID(stationID, currentLocalUser, data, new BaseOperateDataCallbackImpl<Boolean>() {
                        @Override
                        public void onSucceed(Boolean data, OperationResult result) {
                            super.onSucceed(data, result);

                            systemSettingDataSource.setCurrentLoginUserGUID(weChatTokenUserWrapper.getGuid());

                            callback.onSucceed(data, result);
                        }

                        @Override
                        public void onFail(OperationResult result) {
                            super.onFail(result);

                            callback.onFail(result);
                        }
                    });

                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void getUserByUserUUID(final String stationID, final User currentUser, final List<User> users, final BaseOperateDataCallback<Boolean> callback) {

        userDataRepository.getUserDetailedInfoByUUID(currentUser.getUuid(), new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                boolean findUser = false;

                for (final User user : data) {

                    if (user.getUuid().equals(currentUser.getUuid())) {

                        findUser = true;

                        Log.d(TAG, "onSucceed: currentUser isAdmin: " + user.isAdmin());

                        currentUser.setAdmin(user.isAdmin());

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                Log.d(TAG, "onSucceed: current User Home: " + data.get(0));

                                currentUser.setHome(data.get(0));

                                userDataRepository.insertUsers(users);

                                initSystemState(mToken, mGateway, currentUser.getUuid());

                                systemSettingDataSource.setCurrentLoginStationID(stationID);

//                                eventBus.postSticky(new OperationEvent(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN, new OperationSuccess()));

                                callback.onSucceed(true, new OperationSuccess());

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {

                                callback.onFail(operationResult);

                            }
                        });

                    }

                }

                if (!findUser) {
                    callback.onFail(new OperationFail("get user detailed info failed"));
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });

    }

    public void handleLoginWithWeChatCodeSucceed(String guid, String token, String stationID) {

        systemSettingDataSource.setAutoUploadOrNot(false);

        systemSettingDataSource.setShowAutoUploadDialog(true);

        mediaDataSourceRepository.clearAllStationMediasInDB();

        mediaDataSourceRepository.resetState();

        weChatUserDataSource.insertWeChatUser(new WeChatUser(token,
                guid, stationID));
    }

    public void loginWithOtherWeChatUserBindingLocalUser(LoggedInWeChatUser loggedInWeChatUser, final BaseOperateDataCallback<Boolean> callback) {

        final WeChatUser weChatUser = new WeChatUser(loggedInWeChatUser.getToken(), loggedInWeChatUser.getUser().getAssociatedWeChatGUID(), loggedInWeChatUser.getStationID());

        Log.d(TAG, "loginWithOtherWeChatUserBindingLocalUser: weChatUserToken: " + weChatUser.getToken()
                + " weChatUserGUID: " + weChatUser.getGuid() + " weChatUserStationID: " + weChatUser.getStationID());

        loginWithWeChatUser(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                handleLoginWithWeChatCodeSucceed(weChatUser.getGuid(), weChatUser.getToken(), weChatUser.getStationID());

                //do not show dialog in this case
                systemSettingDataSource.setShowAutoUploadDialog(false);

                callback.onSucceed(true, new OperationSuccess());

            }

            @Override
            public void onFail(OperationResult result) {

                callback.onFail(result);

            }
        }, weChatUser);

    }


}
