package com.winsun.fruitmix.upload.media;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMkDirParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/8/18.
 */

public class UploadMediaUseCase {

    public static final String TAG = UploadMediaUseCase.class.getSimpleName();

    static final String UPLOAD_PARENT_FOLDER_NAME = "上传的照片";

    static final String UPLOAD_FOLDER_NAME_PREFIX = "来自";

    public static final int RETRY_UPLOAD = 0x1001;

    public static final long RETRY_INTERVAL = 1 * 60 * 1000;

    private RetryUploadHandler retryUploadHandler;

    private String uploadFolderName;

    private static UploadMediaUseCase instance;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private CalcMediaDigestStrategy calcMediaDigestStrategy;

    private ThreadManager threadManager;

    private SystemSettingDataSource systemSettingDataSource;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private CheckMediaIsExistStrategy checkMediaIsExistStrategy;

    private StationFileRepository stationFileRepository;

    private NetworkStateManager networkStateManager;

    boolean mStopUpload = false;

    boolean mAlreadyStartUpload = false;

    private String currentUserHome = "";

    private String currentUserUUID = "";

    String uploadParentFolderUUID = "";

    String uploadFolderUUID = "";

    int alreadyUploadedMediaCount = 0;

    private UserDataRepository userDataRepository;

    List<String> uploadedMediaHashs;

    private List<Media> localMedias;

    private boolean stopRetryUpload = false;

    private static List<UploadMediaCountChangeListener> uploadMediaCountChangeListeners = new ArrayList<>();

    public static UploadMediaUseCase getInstance(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                                                 UserDataRepository userDataRepository, ThreadManager threadManager,
                                                 SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                                 CheckMediaIsExistStrategy checkMediaIsExistStrategy, String uploadFolderName, EventBus eventBus,
                                                 CalcMediaDigestStrategy calcMediaDigestStrategy, NetworkStateManager networkStateManager) {
        if (instance == null)
            instance = new UploadMediaUseCase(mediaDataSourceRepository, stationFileRepository, userDataRepository, threadManager,
                    systemSettingDataSource, checkMediaIsUploadStrategy, checkMediaIsExistStrategy,
                    uploadFolderName, eventBus, calcMediaDigestStrategy, networkStateManager);
        return instance;
    }

    public static void destroyInstance() {

        Log.d(TAG, "UploadMediaUseCase: destroyInstance");

        instance = null;
    }

    private UploadMediaUseCase(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                               UserDataRepository userDataRepository, ThreadManager threadManager,
                               SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                               CheckMediaIsExistStrategy checkMediaIsExistStrategy, String uploadFolderName, EventBus eventBus,
                               CalcMediaDigestStrategy calcMediaDigestStrategy, NetworkStateManager networkStateManager) {
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.checkMediaIsExistStrategy = checkMediaIsExistStrategy;
        this.userDataRepository = userDataRepository;
        this.calcMediaDigestStrategy = calcMediaDigestStrategy;
        this.networkStateManager = networkStateManager;

        this.threadManager = threadManager;

        this.uploadFolderName = uploadFolderName;

        retryUploadHandler = new RetryUploadHandler(this);

        localMedias = Collections.emptyList();

        Log.d(TAG, "UploadMediaUseCase: create new instance");

    }

    private String getUploadFolderName() {
        return UPLOAD_FOLDER_NAME_PREFIX + uploadFolderName;
    }

    public void registerUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        boolean result = uploadMediaCountChangeListeners.add(uploadMediaCountChangeListener);

        Log.d(TAG, "registerUploadMediaCountChangeListener: " + uploadMediaCountChangeListener + " result: " + result);

    }

    public void unregisterUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        boolean result = uploadMediaCountChangeListeners.remove(uploadMediaCountChangeListener);

        Log.d(TAG, "unregisterUploadMediaCountChangeListener: " + uploadMediaCountChangeListener + " result: " + result);

    }

    public void startUploadMedia() {

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                startUploadMediaInThread();
            }
        });

    }

    private void startUploadMediaInThread() {

        if (mAlreadyStartUpload) {
            Log.d(TAG, "startUploadMedia: already start");

            return;
        }

        uploadedMediaHashs = null;

        currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        User user = userDataRepository.getUserByUUID(currentUserUUID);

        if (user == null) {

            Log.i(TAG, "current user is null,stop upload and send retry");

            notifyGetFolderFail(-1);

            stopUploadMedia();

            sendRetryUploadMessage();

            return;

        }

        currentUserHome = user.getHome();

        Log.d(TAG, "startUploadMedia: ");

        if (mStopUpload)
            mStopUpload = false;

        mAlreadyStartUpload = true;

        alreadyUploadedMediaCount = 0;

        notifyGetUploadMediaCountStart();

        mediaDataSourceRepository.getLocalMedia(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                localMedias = data;

                if (!calcMediaDigestStrategy.isFinishCalcMediaDigest()) {

                    Log.d(TAG, "not finish calc media digest,stop upload media");

                    stopUploadMedia();

                    sendRetryUploadMessage();

                    return;

                }

                if (uploadParentFolderUUID.isEmpty() || uploadFolderUUID.isEmpty())
                    checkFolderExist(currentUserHome, currentUserHome, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                        @Override
                        public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                            super.onSucceed(data, operationResult);

                            handleGetRootFolderResult(data);

                        }

                    });
                else
                    startPrepareAutoUpload();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                Log.d(TAG, "onFail: get local media fail,stop upload media and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

            }
        });

    }

    private void checkFolderExist(String rootUUID, String dirUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        Log.i(TAG, "start checkUploadParentFolderExist");

        stationFileRepository.getFile(rootUUID, dirUUID, new BaseLoadDataCallback<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {

                if (operationResult instanceof OperationNetworkException) {
                    notifyGetFolderFail(((OperationNetworkException) operationResult).getHttpResponseCode());
                } else
                    notifyGetFolderFail(-1);

                Log.d(TAG, "onFail: get folder fail,stop upload and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

            }
        });

    }

    private void handleGetRootFolderResult(List<AbstractRemoteFile> data) {

        uploadParentFolderUUID = getFolderUUIDByName(data, UPLOAD_PARENT_FOLDER_NAME);

        if (uploadParentFolderUUID.length() != 0) {

            Log.i(TAG, "upload parent folder exist");

            checkFolderExist(currentUserHome, uploadParentFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    handleGetUploadFolderResult(data);

                }
            });

        } else {

            startPrepareAutoUploadWithNoFolder();

        }

    }

    private void startPrepareAutoUploadWithNoFolder() {
        Log.d(TAG, "init upload media hashs no upload folder");

        uploadedMediaHashs = new ArrayList<>();

        checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

        startPrepareAutoUpload();
    }


    private void handleGetUploadFolderResult(List<AbstractRemoteFile> data) {

        uploadFolderUUID = getFolderUUIDByName(data, getUploadFolderName());

        if (!uploadFolderUUID.isEmpty()) {

            Log.i(TAG, "uploaded folder exist");

            if (uploadedMediaHashs == null)
                getUploadedMediaHashList(uploadFolderUUID);
            else
                startPrepareAutoUpload();

        } else {
            startPrepareAutoUploadWithNoFolder();
        }

    }


    private void getUploadedMediaHashList(final String uploadFolderUUID) {

        Log.i(TAG, "start getUploadedMediaHashList");

        stationFileRepository.getFile(currentUserHome, uploadFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                uploadedMediaHashs = new ArrayList<>();

                for (AbstractRemoteFile file : data) {
                    if (file instanceof RemoteFile)
                        uploadedMediaHashs.add(((RemoteFile) file).getFileHash());
                }

                Log.i(TAG, "getUploadedMediaHashList uploadedMediaHashs size: " + uploadedMediaHashs.size());

                checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

                calcAlreadyUploadedMediaCount();

                startPrepareAutoUpload();

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                if (operationResult instanceof OperationNetworkException) {
                    notifyGetUploadMediaCountFail(((OperationNetworkException) operationResult).getHttpResponseCode());
                } else
                    notifyGetUploadMediaCountFail(-1);

                startPrepareAutoUploadWithNoFolder();
            }
        });

    }

    private void calcAlreadyUploadedMediaCount() {

        Log.d(TAG, "start calcAlreadyUploadedMediaCount");

        alreadyUploadedMediaCount = 0;
        for (Media media : localMedias) {
            if (checkMediaIsUploadStrategy.isMediaUploaded(media))
                alreadyUploadedMediaCount++;
        }

        Log.d(TAG, "finish calcAlreadyUploadedMediaCount: " + alreadyUploadedMediaCount);
    }


    private void startPrepareAutoUpload() {

        notifyUploadMediaCountChange();

        if (!systemSettingDataSource.getAutoUploadOrNot()) {

            Log.d(TAG, "startPrepareAutoUpload: auto upload false stop upload");

            stopUploadMedia();

            sendRetryUploadMessage();

            return;
        }

        createFolderIfNeed(localMedias);

    }

    private void createFolderIfNeed(final List<Media> medias) {

        if (uploadParentFolderUUID.isEmpty()) {

            Log.i(TAG, "start create upload parent folder");

            createUploadParentFolder(medias);
        } else if (uploadFolderUUID.isEmpty()) {

            Log.i(TAG, "start create upload folder");

            createUploadFolder(medias);
        } else {
            Log.i(TAG, "start upload media");

            calcNeedUploadMediaAndUpload(medias);
        }

    }

    private void createUploadParentFolder(final List<Media> medias) {

        createFolder(UPLOAD_PARENT_FOLDER_NAME, currentUserHome, currentUserHome, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteDataParser<AbstractRemoteFile> parser = new RemoteMkDirParser();

                try {

//                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());
//
//                    uploadParentFolderUUID = getFolderUUIDByName(files, UPLOAD_PARENT_FOLDER_NAME);

                    AbstractRemoteFile file = parser.parse(data.getResponseData());

                    uploadParentFolderUUID = file.getUuid();

                    if (uploadParentFolderUUID.length() != 0) {

                        Log.i(TAG, "create upload folder succeed folder uuid:" + uploadParentFolderUUID);

                        createUploadFolder(medias);

                    } else {

                        notifyCreateFolderFail(-1);

                        Log.i(TAG, "create upload folder succeed but can not find folder uuid,stop upload media and send retry");

                        stopUploadMedia();

                        sendRetryUploadMessage();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    notifyCreateFolderFail(-1);

                    Log.d(TAG, "parse upload parent folder result fail,stop upload media and send retry");

                    stopUploadMedia();

                    sendRetryUploadMessage();
                }

            }
        });

    }


    private void createUploadFolder(final List<Media> medias) {
        createFolder(getUploadFolderName(), currentUserHome, uploadParentFolderUUID, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteDataParser<AbstractRemoteFile> parser = new RemoteMkDirParser();

                try {

//                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());
//
//                    uploadFolderUUID = getFolderUUIDByName(files, getUploadFolderName());

                    AbstractRemoteFile file = parser.parse(data.getResponseData());

                    uploadFolderUUID = file.getUuid();

                    if (!uploadFolderUUID.isEmpty()) {

                        Log.i(TAG, "create upload folder succeed folder uuid:" + uploadFolderUUID);

                        calcNeedUploadMediaAndUpload(medias);

                    } else {

                        notifyCreateFolderFail(-1);

                        Log.i(TAG, "create upload folder succeed but can not find folder uuid,stop upload and send retry");

                        stopUploadMedia();

                        sendRetryUploadMessage();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    notifyCreateFolderFail(-1);

                    Log.d(TAG, "parse create upload folder result fail,stop upload and send retry");

                    stopUploadMedia();

                    sendRetryUploadMessage();

                }

            }

        });
    }

    private void createFolder(String folderName, String rootUUID, String dirUUID, final BaseOperateDataCallback<HttpResponse> callback) {
        stationFileRepository.createFolder(folderName, rootUUID, dirUUID, new BaseOperateDataCallback<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                callback.onSucceed(data, result);
            }

            @Override
            public void onFail(OperationResult result) {

                if (result instanceof OperationNetworkException)
                    notifyCreateFolderFail(((OperationNetworkException) result).getHttpResponseCode());
                else
                    notifyCreateFolderFail(-1);

                Log.d(TAG, "onFail: create folder fail,stop upload media and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

            }
        });
    }


    private String getFolderUUIDByName(List<AbstractRemoteFile> files, String folderName) {
        String folderUUID;
        for (AbstractRemoteFile file : files) {
            if (file.getName().equals(folderName)) {

                folderUUID = file.getUuid();

                return folderUUID;
            }

        }
        return "";
    }

    private void calcNeedUploadMediaAndUpload(List<Media> medias) {

        List<Media> needUploadedMedia = calcNeedUploadedMedia(medias);

        uploadMedia(needUploadedMedia);
    }

    @NonNull
    private List<Media> calcNeedUploadedMedia(List<Media> medias) {
        List<Media> needUploadedMedia = new ArrayList<>();

        for (Media media : medias) {
            if (!checkMediaIsUploadStrategy.isMediaUploaded(media))
                needUploadedMedia.add(media);
        }
        return needUploadedMedia;
    }

    private void uploadMedia(final List<Media> needUploadedMedias) {

//        for (final Media media : medias) {
//
//            if (mStopUpload)
//                return;
//
//            if (!checkMediaIsUploadStrategy.isMediaUploaded(media)) {
//                uploadMediaInThread(media, uploadFolderUUID,0);
//            }
//
//        }

        threadManager.runOnUploadMediaThread(new Runnable() {
            @Override
            public void run() {
                uploadMediaInThread(needUploadedMedias, uploadFolderUUID);
            }
        });

    }

    private void uploadMediaInThread(final List<Media> needUploadedMedias, final String uploadFolderUUID) {

        while (true) {

            Log.d(TAG, "uploadMediaInThread: mStopUpload: " + mStopUpload + " auto upload or not: " + systemSettingDataSource.getAutoUploadOrNot() + " threadID: " +
                    Thread.currentThread().getId());

            if (mStopUpload) {

                Log.d(TAG, "uploadMediaInThread: mStop upload true,stop upload and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

                break;
            }


            if (!systemSettingDataSource.getAutoUploadOrNot()) {

                Log.d(TAG, "uploadMediaInThread: auto upload false,stop upload and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

                break;
            }

            NetworkState networkState = networkStateManager.getNetworkState();

            if (systemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi() && !networkState.isWifiConnected()) {

                Log.d(TAG, "uploadMediaInThread: only auto upload when connect with wifi is true,but wifi is not connected,stop upload and send retry");

                stopUploadMedia();

                sendRetryUploadMessage();

                break;

            }

            if (needUploadedMedias.size() == 0) {

                Log.d(TAG, "uploadMedia: no need upload media,send retry upload message");

                stopUploadMedia();

                notifyUploadMediaCountChange();

                sendRetryUploadMessage();

                break;

            } else {

                stopRetryUploadTemporary();

                int code = uploadOneMedia(needUploadedMedias, uploadFolderUUID);

                if (code == 404) {

                    Log.d(TAG, "uploadMediaInThread: upload return 404,reset state and start upload media");

                    resetState();

                    startUploadMedia();

                    break;

                }

            }

        }

    }

    private int uploadOneMedia(final List<Media> needUploadedMedias, final String uploadFolderUUID) {

        final Media media = needUploadedMedias.get(0);

        if (!checkMediaIsUploadStrategy.isMediaUploaded(media)) {
            LocalFile localFile = new LocalFile();

            File file = new File(media.getOriginalPhotoPath());

            if (!checkMediaIsExistStrategy.checkMediaIsExist(media)) {

                Log.d(TAG, "media path not exist,path:" + media.getOriginalPhotoPath());

                handleUploadMediaSucceed(media, needUploadedMedias, uploadFolderUUID);

                return 200;
            }

            localFile.setFileHash(media.getUuid());
            localFile.setName(file.getName());
            localFile.setPath(media.getOriginalPhotoPath());
            localFile.setSize(file.length() + "");

            Log.d(TAG, "upload file: media uuid: " + media.getUuid());

            OperationResult result = stationFileRepository.uploadFile(localFile, currentUserHome, uploadFolderUUID);

            if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                handleUploadMediaSucceed(media, needUploadedMedias, uploadFolderUUID);

                return 200;

            } else {

                Log.i(TAG, "upload onFail: media uuid: " + media.getUuid());

                if (!mStopUpload) {

                    if (result instanceof OperationNetworkException) {

                        int code = ((OperationNetworkException) result).getHttpResponseCode();

                        if (code == 404) {
                            return 404;
                        } else {

                            HttpErrorBodyParser parser = new HttpErrorBodyParser();

                            try {
                                String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseBody());

                                if (messageInBody.contains(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE)) {

                                    Log.d(TAG, "uploadOneMedia: file exist,upload succeed");

                                    handleUploadMediaSucceed(media, needUploadedMedias, uploadFolderUUID);
                                } else {

                                    notifyUploadMediaFail(-1);

                                    handleUploadMediaFail(needUploadedMedias, media, uploadFolderUUID);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();

                                notifyUploadMediaFail(-1);

                                handleUploadMediaFail(needUploadedMedias, media, uploadFolderUUID);

                            }

                        }

                        return code;


                    } else {

                        notifyUploadMediaFail(-1);

                        handleUploadMediaFail(needUploadedMedias, media, uploadFolderUUID);

                        return -1;

                    }

                }

                return -1;

            }


        } else {

            Log.i(TAG, "media is uploaded,it's uuid: " + media.getUuid());

            handleUploadMediaSucceed(media, needUploadedMedias, uploadFolderUUID);

            return 200;
        }

    }

    private void handleUploadMediaFail(List<Media> needUploadedMedias, Media media, String uploadFolderUUID) {

        Log.d(TAG, "handleUploadMediaFail: media uuid: " + media.getUuid());

        needUploadedMedias.remove(media);
    }

    private void handleUploadMediaSucceed(Media media, List<Media> needUploadedMedias, String uploadFolderUUID) {
        Log.i(TAG, "upload onSucceed: media uuid: " + media.getUuid() + " user uuid: " + currentUserUUID);

        if (currentUserUUID.isEmpty() || mStopUpload) {

            Log.d(TAG, "handleUploadMediaSucceed: currentUserUUID is empty or mStopUpload is true,return.");
            Log.d(TAG, "handleUploadMediaSucceed: currentUserUUID: " + currentUserUUID + " mStopUpload: " + mStopUpload);

            return;
        }


        needUploadedMedias.remove(media);

        checkMediaIsUploadStrategy.addUploadedMediaUUID(media.getUuid());

        alreadyUploadedMediaCount++;

        notifyUploadMediaCountChange();

    }

    private void notifyUploadMediaCountChange() {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                notifyUploadMediaCountChangeInThread();
            }
        });

/*        UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.UPLOAD_MEDIA_COUNT_CHANGED);

        eventBus.postSticky(state);*/

    }

    private void notifyUploadMediaCountChangeInThread() {

        if (uploadMediaCountChangeListeners != null) {

            for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                Log.d(TAG, "call notifyUploadMediaCountChange " + uploadMediaCountChangeListener + " alreadyUploadedMediaCount: " + alreadyUploadedMediaCount + " localMedias Size: " + localMedias.size());

                uploadMediaCountChangeListener.onUploadMediaCountChanged(alreadyUploadedMediaCount, localMedias.size());
            }

        }

    }

    private void notifyGetUploadMediaCountFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                notifyGetUploadMediaCountFailInThread(httpErrorCode);
            }
        });
/*
        UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.GET_MEDIA_COUNT_FAIL);
        state.setErrorCode(httpErrorCode);

        eventBus.postSticky(state);*/

    }

    private void notifyGetUploadMediaCountFailInThread(int httpErrorCode) {
        if (uploadMediaCountChangeListeners != null) {

            for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                Log.d(TAG, "call notifyGetUploadMediaCountFail" + uploadMediaCountChangeListener);

                uploadMediaCountChangeListener.onGetUploadMediaCountFail(httpErrorCode);
            }

        }
    }

    private void notifyGetFolderFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                        Log.d(TAG, "call notifyGetFolderFail" + uploadMediaCountChangeListener);

                        uploadMediaCountChangeListener.onGetFolderFail(httpErrorCode);
                    }

                }

            }
        });

 /*       UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.GET_FOLDER_FAIL);
        state.setErrorCode(httpErrorCode);

        eventBus.postSticky(state);*/


    }

    private void notifyCreateFolderFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                        Log.d(TAG, "call notifyCreateFolderFail" + uploadMediaCountChangeListener);

                        uploadMediaCountChangeListener.onCreateFolderFail(httpErrorCode);

                    }

                }

            }
        });

/*        UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.CREATE_FOLDER_FAIL);
        state.setErrorCode(httpErrorCode);

        eventBus.postSticky(state);*/

    }

    private void notifyUploadMediaFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                        Log.d(TAG, "call notifyUploadMediaFail" + uploadMediaCountChangeListener);

                        uploadMediaCountChangeListener.onUploadMediaFail(httpErrorCode);
                    }

                }

            }
        });

/*        UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.UPLOAD_MEDIA_FAIL);
        state.setErrorCode(httpErrorCode);

        eventBus.postSticky(state);*/

    }

    private void notifyGetUploadMediaCountStart() {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

                        Log.d(TAG, "call notifyGetUploadMediaCountStart" + uploadMediaCountChangeListener);

                        uploadMediaCountChangeListener.onStartGetUploadMediaCount();
                    }

                }

            }
        });

/*        UploadMediaState state = new UploadMediaState();
        state.setType(UploadMediaState.START_GET_UPLOAD_MEDIA_COUNT);

        eventBus.postSticky(state);*/

    }


    public void stopUploadMedia() {

        Log.i(TAG, "stopUploadMedia: stop thread");

        threadManager.stopUploadMediaThreadNow();

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadParentFolderUUID = "";

        uploadFolderUUID = "";

        currentUserUUID = "";

        currentUserHome = "";

    }

    public void resetState() {

        Log.i(TAG, "reset state");

        mStopUpload = true;

        mAlreadyStartUpload = false;

        alreadyUploadedMediaCount = 0;

        uploadFolderUUID = "";

        uploadParentFolderUUID = "";

        currentUserUUID = "";

        currentUserHome = "";

        if (retryUploadHandler != null)
            retryUploadHandler.removeMessages(RETRY_UPLOAD);

    }

    public int getAlreadyUploadedMediaCount() {
        return alreadyUploadedMediaCount;
    }

    public List<Media> getLocalMedias() {
        return localMedias;
    }

    public void stopRetryUploadTemporary() {

        Log.d(TAG, "stopRetryUploadTemporary: ");

        if (retryUploadHandler != null) {

            retryUploadHandler.removeMessages(RETRY_UPLOAD);

        }

    }

    public void stopRetryUploadForever() {

        Log.d(TAG, "stopRetryUploadForever: ");

        stopRetryUpload = true;

        if (retryUploadHandler != null) {

            retryUploadHandler.removeMessages(RETRY_UPLOAD);

        }

    }

    private void sendRetryUploadMessage() {

        Log.d(TAG, "sendRetryUploadMessage: delay time " + RETRY_INTERVAL);

        if (stopRetryUpload)
            return;

        if (retryUploadHandler != null) {

            retryUploadHandler.sendEmptyMessageDelayed(RETRY_UPLOAD, RETRY_INTERVAL);
        }

    }

    private static class RetryUploadHandler extends Handler {

        WeakReference<UploadMediaUseCase> weakReference = null;

        RetryUploadHandler(UploadMediaUseCase uploadMediaUseCase) {

            weakReference = new WeakReference<>(uploadMediaUseCase);

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RETRY_UPLOAD:

                    weakReference.get().startUploadMedia();

                    break;
            }

        }
    }


}
