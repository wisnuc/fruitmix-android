package com.winsun.fruitmix.login;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.R;
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
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.token.WechatTokenUserWrapper;
import com.winsun.fruitmix.upload.media.CheckMediaIsUploadStrategy;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

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

    public static String mToken;
    public static String mGateway;

    private LoginUseCase(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                         HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy, UploadMediaUseCase uploadMediaUseCase,
                         UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                         SystemSettingDataSource systemSettingDataSource, ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus,
                         ThreadManager threadManager, NewPhotoListDataLoader newPhotoListDataLoader, StationsDataSource stationsDataSource) {
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

    }

    public static LoginUseCase getInstance(LoggedInUserDataSource loggedInUserDataSource, TokenDataSource tokenDataSource,
                                           HttpRequestFactory httpRequestFactory, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                           UploadMediaUseCase uploadMediaUseCase, UserDataRepository userDataRepository, MediaDataSourceRepository mediaDataSourceRepository,
                                           StationFileRepository stationFileRepository, SystemSettingDataSource systemSettingDataSource,
                                           ImageGifLoaderInstance imageGifLoaderInstance, EventBus eventBus, ThreadManager threadManager,
                                           NewPhotoListDataLoader newPhotoListDataLoader, StationsDataSource stationsDataSource) {

        if (instance == null)
            instance = new LoginUseCase(loggedInUserDataSource, tokenDataSource,
                    httpRequestFactory, checkMediaIsUploadStrategy, uploadMediaUseCase, userDataRepository, mediaDataSourceRepository,
                    stationFileRepository, systemSettingDataSource, imageGifLoaderInstance, eventBus, threadManager, newPhotoListDataLoader, stationsDataSource);

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

    public void loginWithNoParam(BaseOperateDataCallback<Boolean> callback) {

        String currentLoginToken = systemSettingDataSource.getCurrentLoginToken();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByToken(currentLoginToken);

        if (loggedInUser == null)
            callback.onFail(new OperationSQLException());
        else {

            Log.d(TAG, "loginWithNoParam: http request factory set current data token:" + loggedInUser.getToken() + " gateway: " + loggedInUser.getGateway());

            mToken = loggedInUser.getToken();
            mGateway = loggedInUser.getGateway();

            initSystemState(loggedInUser.getToken(), loggedInUser.getGateway(), loggedInUser.getUser().getUuid());

            callback.onSucceed(true, new OperationSuccess());

            getUsers(loggedInUser.getUser().getUuid());

        }
    }

    private void getUsers(String currentUserUUID) {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(currentUserUUID, new BaseLoadDataCallbackImpl<User>());

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
        userDataRepository.getUsers(loadTokenParam.getUserUUID(), new BaseLoadDataCallbackImpl<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                for (final User user : data) {
                    if (user.getUuid().equals(loadTokenParam.getUserUUID())) {

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                user.setHome(data.get(0));

                                systemSettingDataSource.setAutoUploadOrNot(false);
                                systemSettingDataSource.setShowAutoUploadDialog(true);

                                LoggedInUser loggedInUser = new LoggedInUser(user.getLibrary(), token, loadTokenParam.getGateway(), loadTokenParam.getEquipmentName(), user);

                                boolean result = loggedInUserDataSource.insertLoggedInUsers(Collections.singletonList(loggedInUser));

                                Log.d(TAG, "onSucceed: insert result :" + result);

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

            systemSettingDataSource.setAutoUploadOrNot(true);

            result = true;
        } else {
            systemSettingDataSource.setAutoUploadOrNot(false);

            result = false;
        }

        callback.onSucceed(result, new OperationSuccess());

        getUsers(currentLoggedInUser.getUser().getUuid());
    }

    private void initSystemState(String token, String gateway, String loginUserUUID) {

        Log.i(TAG, "initSystemState: token: " + token + " gateway: " + gateway + " loginUserUUID: " + loginUserUUID);

        httpRequestFactory.setCurrentData(token, gateway);

        checkMediaIsUploadStrategy.setCurrentUserUUID(loginUserUUID);

        checkMediaIsUploadStrategy.setUploadedMediaHashs(new ArrayList<String>());

        uploadMediaUseCase.resetState();

        imageGifLoaderInstance.setToken(token);

        systemSettingDataSource.setCurrentLoginUserUUID(loginUserUUID);

        stationFileRepository.clearDownloadFileRecordInCache();

        newPhotoListDataLoader.setNeedRefreshData(true);

    }


    public void loginWithWeChatCode(String code, final BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        tokenDataSource.getToken(code, new BaseLoadDataCallback<WechatTokenUserWrapper>() {
            @Override
            public void onSucceed(List<WechatTokenUserWrapper> data, OperationResult operationResult) {

                WechatTokenUserWrapper wechatTokenUserWrapper = data.get(0);

                String token = wechatTokenUserWrapper.getToken();

                String guid = wechatTokenUserWrapper.getGuid();

                //TODO: check get gateway and user when login by wechat code

                Log.d(TAG, "loginWithNoParam: http request factory set current data token:" + token + " gateway: " + "10.10.9.59" + " guid: " + guid);

                mToken = token;

                mGateway = Util.HTTP + "10.10.9.59";

                httpRequestFactory.setCurrentData(token, Util.HTTP + "10.10.9.59");

                httpRequestFactory.setPort(4000);

                imageGifLoaderInstance.setToken(token);

                stationFileRepository.clearDownloadFileRecordInCache();

                getStationList(guid, runOnMainThreadCallback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                runOnMainThreadCallback.onFail(operationResult);

            }
        });

    }

    private void getStationList(final String guid, final BaseOperateDataCallback<Boolean> callback) {

        stationsDataSource.getStationsByWechatGUID(guid, new BaseLoadDataCallback<Station>() {
            @Override
            public void onSucceed(List<Station> data, OperationResult operationResult) {

                if (data.size() == 1) {

                    getUsers(guid, data.get(0).getId(), callback);

                } else if (data.size() > 1) {

                    callback.onSucceed(true, new OperationResult() {
                        @Override
                        public String getResultMessage(Context context) {
                            return "";
                        }

                        @Override
                        public OperationResultType getOperationResultType() {
                            return OperationResultType.MOER_THAN_ONE_STATION;
                        }
                    });

                } else {

                    callback.onFail(new OperationResult() {
                        @Override
                        public String getResultMessage(Context context) {
                            return "未绑定nas用户，请绑定";
                        }

                        @Override
                        public OperationResultType getOperationResultType() {
                            return null;
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


    private void getUsers(final String guid, final String stationID, final BaseOperateDataCallback<Boolean> callback) {

        httpRequestFactory.setStationID(stationID);

        userDataRepository.getUsersByStationID(stationID, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                User currentLocalUser = null;

                for (User user : data) {

                    if (user.getAssociatedWechatGUID().equals(guid)) {

                        currentLocalUser = user;
                        break;
                    }

                }

                if (currentLocalUser == null) {

                    Log.d(TAG, "onSucceed: not find associatedWechatGUID");

                    callback.onFail(new OperationIOException());

                } else {

                    systemSettingDataSource.setCurrentLoginUserUUID(currentLocalUser.getUuid());

                    systemSettingDataSource.setCurrentLoginStationID(stationID);

                    getCurrentByUserUUID(currentLocalUser.getUuid(), callback);

                }


            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);
            }
        });

    }

    private void getCurrentByUserUUID(final String currentUserUUID, final BaseOperateDataCallback<Boolean> callback) {

        userDataRepository.setCacheDirty();
        userDataRepository.getUsers(currentUserUUID, new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> data, OperationResult operationResult) {

                for (final User user : data) {

                    if (user.getUuid().equals(currentUserUUID)) {

                        userDataRepository.getCurrentUserHome(new BaseLoadDataCallback<String>() {
                            @Override
                            public void onSucceed(List<String> data, OperationResult operationResult) {

                                user.setHome(data.get(0));

                                callback.onSucceed(true, new OperationSuccess());

                                eventBus.postSticky(new OperationEvent(Util.SET_CURRENT_LOGIN_USER_AFTER_LOGIN, new OperationSuccess()));

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

                callback.onFail(operationResult);

            }
        });


    }


}
