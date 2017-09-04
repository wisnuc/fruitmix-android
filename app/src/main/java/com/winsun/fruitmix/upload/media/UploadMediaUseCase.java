package com.winsun.fruitmix.upload.media;

import android.os.Handler;
import android.os.Message;
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
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
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

    private ThreadManager threadManager;

    private SystemSettingDataSource systemSettingDataSource;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private StationFileRepository stationFileRepository;

    boolean mStopUpload = false;

    boolean mAlreadyStartUpload = false;

    private String currentUserHome = "";

    private String currentUserUUID = "";

    String uploadParentFolderUUID = "";

    String uploadFolderUUID = "";

    int uploadMediaCount = 0;

    int alreadyUploadedMediaCount = 0;

    private LoggedInUserDataSource loggedInUserDataSource;

    List<String> uploadedMediaHashs;

    private List<UploadMediaCountChangeListener> uploadMediaCountChangeListeners;

    public static UploadMediaUseCase getInstance(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                                                 LoggedInUserDataSource loggedInUserDataSource, ThreadManager threadManager,
                                                 SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy,
                                                 String uploadFolderName) {
        if (instance == null)
            instance = new UploadMediaUseCase(mediaDataSourceRepository, stationFileRepository, loggedInUserDataSource, threadManager,
                    systemSettingDataSource, checkMediaIsUploadStrategy, uploadFolderName);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private UploadMediaUseCase(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                               LoggedInUserDataSource loggedInUserDataSource, ThreadManager threadManager,
                               SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy, String uploadFolderName) {
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.loggedInUserDataSource = loggedInUserDataSource;

        this.threadManager = threadManager;

        this.uploadFolderName = uploadFolderName;

        uploadMediaCountChangeListeners = new ArrayList<>();

        retryUploadHandler = new RetryUploadHandler();

    }

    private String getUploadFolderName() {
        return UPLOAD_FOLDER_NAME_PREFIX + uploadFolderName;
    }

    public void registerUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        Log.d(TAG, "registerUploadMediaCountChangeListener: " + uploadMediaCountChangeListener);

        uploadMediaCountChangeListeners.add(uploadMediaCountChangeListener);
    }

    public void unregisterUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        Log.d(TAG, "unregisterUploadMediaCountChangeListener: " + uploadMediaCountChangeListener);

        uploadMediaCountChangeListeners.remove(uploadMediaCountChangeListener);
    }

    public void startUploadMedia(){

        threadManager.runOnUploadMediaThread(new Runnable() {
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

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(currentUserUUID);

        if (loggedInUser == null) {

            Log.i(TAG, "no logged in user,stop upload");

            stopUploadMedia();

            return;

        }

        currentUserHome = loggedInUser.getUser().getHome();

        Log.d(TAG, "startUploadMedia: ");

        if (mStopUpload)
            mStopUpload = false;

        mAlreadyStartUpload = true;

        notifyGetUploadMediaCountStart();

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

                stopUploadMedia();

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
        for (Media media : mediaDataSourceRepository.getLocalMedia()) {
            if (checkMediaIsUploadStrategy.isMediaUploaded(media))
                alreadyUploadedMediaCount++;
        }
    }


    private void startPrepareAutoUpload() {

        if (mStopUpload)
            return;

        notifyUploadMediaCountChange();

        if (!systemSettingDataSource.getAutoUploadOrNot()) {

            Log.d(TAG, "startPrepareAutoUpload: auto upload false stop upload");

            stopUploadMedia();

            return;
        }

        createFolderIfNeed(mediaDataSourceRepository.getLocalMedia());

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

            uploadMedia(medias);
        }

    }

    private void createUploadParentFolder(final List<Media> medias) {

        createFolder(UPLOAD_PARENT_FOLDER_NAME, currentUserHome, currentUserHome, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteFileFolderParser parser = new RemoteFileFolderParser();

                try {

                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());

                    uploadParentFolderUUID = getFolderUUIDByName(files, UPLOAD_PARENT_FOLDER_NAME);

                    if (uploadParentFolderUUID.length() != 0) {

                        Log.i(TAG, "create upload folder succeed folder uuid:" + uploadParentFolderUUID);

                        createUploadFolder(medias);

                    } else {

                        Log.i(TAG, "create upload folder succeed but can not find folder uuid");

                        notifyCreateFolderFail(-1);
                        stopUploadMedia();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "create upload folder fail");

                    notifyCreateFolderFail(-1);
                    stopUploadMedia();
                }

            }
        });

    }


    private void createUploadFolder(final List<Media> medias) {
        createFolder(getUploadFolderName(), currentUserHome, uploadParentFolderUUID, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteFileFolderParser parser = new RemoteFileFolderParser();

                try {

                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());

                    uploadFolderUUID = getFolderUUIDByName(files, getUploadFolderName());

                    if (!uploadFolderUUID.isEmpty()) {

                        Log.i(TAG, "create upload folder succeed folder uuid:" + uploadFolderUUID);

                        uploadMedia(medias);

                    } else {
                        Log.i(TAG, "create upload folder succeed but can not find folder uuid");

                        stopUploadMedia();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "create upload folder fail");

                    stopUploadMedia();

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

                stopUploadMedia();
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

    private void uploadMedia(final List<Media> medias) {

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

        List<Media> needUploadedMedia = new ArrayList<>();

        for (Media media : medias) {
            if (!checkMediaIsUploadStrategy.isMediaUploaded(media))
                needUploadedMedia.add(media);
        }

        if (mStopUpload)
            return;

        if (needUploadedMedia.size() == 0) {

            stopUploadMedia();

            sendRetryUploadMessage();

        } else if (needUploadedMedia.size() <= uploadMediaCount) {

            Log.d(TAG, "uploadMedia: finish upload and stop");

            stopUploadMedia();

            startUploadMedia();

        } else {
            uploadMediaInThread(needUploadedMedia, uploadFolderUUID, uploadMediaCount);
            uploadMediaCount++;
        }

    }

    private void uploadMediaInThread(final List<Media> medias, final String uploadFolderUUID, final int i) {
        threadManager.runOnUploadMediaThread(new Runnable() {
            @Override
            public void run() {

                if (mStopUpload)
                    return;

                final Media media = medias.get(i);

                if (!checkMediaIsUploadStrategy.isMediaUploaded(media)) {
                    LocalFile localFile = new LocalFile();
                    localFile.setFileHash(media.getUuid());

                    File file = new File(media.getOriginalPhotoPath());

                    localFile.setName(file.getName());
                    localFile.setPath(media.getOriginalPhotoPath());
                    localFile.setSize(file.length() + "");

                    Log.d(TAG, "upload file: media uuid: " + media.getUuid());

                    stationFileRepository.uploadFile(localFile, currentUserHome, uploadFolderUUID, new BaseOperateDataCallbackImpl<Boolean>() {
                        @Override
                        public void onSucceed(Boolean data, OperationResult result) {
                            super.onSucceed(data, result);

                            handleMediaUploadSucceed();
                        }

                        private void handleMediaUploadSucceed() {
                            Log.i(TAG, "upload onSucceed: media uuid: " + media.getUuid() + " user uuid: " + currentUserUUID);

                            if (currentUserUUID.isEmpty() || mStopUpload)
                                return;

                            if (media.getUploadedUserUUIDs().isEmpty()) {
                                media.setUploadedUserUUIDs(currentUserUUID);
                            } else if (currentUserUUID.length() != 0 && !media.getUploadedUserUUIDs().contains(currentUserUUID)) {
                                media.setUploadedUserUUIDs(media.getUploadedUserUUIDs() + "," + currentUserUUID);
                            }

                            mediaDataSourceRepository.updateMedia(media);

                            checkMediaIsUploadStrategy.addUploadedMediaUUID(media.getUuid());

                            alreadyUploadedMediaCount++;

                            notifyUploadMediaCountChange();

                            if (!mStopUpload)
                                uploadMedia(medias);
                        }

                        @Override
                        public void onFail(OperationResult result) {
                            super.onFail(result);

                            Log.i(TAG, "upload onFail: media uuid: " + media.getUuid());

                            if (!mStopUpload) {

                                if (result instanceof OperationNetworkException) {

                                    int code = ((OperationNetworkException) result).getHttpResponseCode();

                                    if (code == 404) {
                                        resetState();
                                        startUploadMedia();
                                    } else if (code == 403) {

                                        HttpErrorBodyParser parser = new HttpErrorBodyParser();
                                        try {
                                            String codeInBody = parser.parse(((OperationNetworkException) result).getHttpResponseBody());

                                            if (codeInBody.equals(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE))
                                                handleMediaUploadSucceed();

                                        } catch (JSONException e) {
                                            e.printStackTrace();

                                            uploadMedia(medias);

                                        }


                                    } else {

                                        uploadMedia(medias);

                                        notifyUploadMediaFail(code);

                                    }


                                } else {

                                    notifyUploadMediaFail(-1);

                                    uploadMedia(medias);
                                }

                            }

                        }
                    });
                } else {

                    Log.i(TAG, "media is uploaded,it's uuid: " + media.getUuid());

                    if (!mStopUpload)
                        uploadMedia(medias);
                }

            }
        });
    }

    private void notifyUploadMediaCountChange() {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                notifyUploadMediaCountChangeInThread();
            }
        });

    }

    private void notifyUploadMediaCountChangeInThread() {

        if (uploadMediaCountChangeListeners != null) {

            Log.i(TAG, "call notifyUploadMediaCountChange");

            for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                uploadMediaCountChangeListener.onUploadMediaCountChanged(alreadyUploadedMediaCount, mediaDataSourceRepository.getLocalMedia().size());
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


        sendRetryUploadMessage();
    }

    private void notifyGetUploadMediaCountFailInThread(int httpErrorCode) {
        if (uploadMediaCountChangeListeners != null) {

            Log.i(TAG, "call notifyGetUploadMediaCountFail");

            for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                uploadMediaCountChangeListener.onGetUploadMediaCountFail(httpErrorCode);
            }

        }
    }

    private void notifyGetFolderFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    Log.i(TAG, "call notifyGetFolderFail");

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                        uploadMediaCountChangeListener.onGetFolderFail(httpErrorCode);
                    }

                }

            }
        });

        sendRetryUploadMessage();

    }

    private void notifyCreateFolderFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    Log.i(TAG, "call notifyCreateFolderFail");

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                        uploadMediaCountChangeListener.onCreateFolderFail(httpErrorCode);
                    }

                }

            }
        });

        sendRetryUploadMessage();


    }

    private void notifyUploadMediaFail(final int httpErrorCode) {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    Log.i(TAG, "call notifyUploadMediaFail");

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                        uploadMediaCountChangeListener.onUploadMediaFail(httpErrorCode);
                    }

                }

            }
        });

    }

    private void notifyGetUploadMediaCountStart() {

        threadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {

                if (uploadMediaCountChangeListeners != null) {

                    Log.i(TAG, "call notifyGetUploadMediaCountStart");

                    for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {
                        uploadMediaCountChangeListener.onStartGetUploadMediaCount();
                    }

                }

            }
        });

    }


    public void stopUploadMedia() {

        Log.i(TAG, "stopUploadMedia: stop thread");

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadMediaCount = 0;

        threadManager.stopUploadMediaThread();

    }

    public void resetState() {

        Log.i(TAG, "reset state");

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadMediaCount = 0;

        alreadyUploadedMediaCount = 0;

        uploadFolderUUID = "";

        uploadParentFolderUUID = "";

        currentUserUUID = "";

        currentUserHome = "";

        retryUploadHandler.removeMessages(RETRY_UPLOAD);

    }

    public int getAlreadyUploadedMediaCount() {
        return alreadyUploadedMediaCount;
    }

    private void sendRetryUploadMessage() {

        Log.d(TAG, "sendRetryUploadMessage: delay time " + RETRY_INTERVAL);

        retryUploadHandler.sendEmptyMessageDelayed(RETRY_UPLOAD, RETRY_INTERVAL);

    }

    private class RetryUploadHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RETRY_UPLOAD:

                    startUploadMedia();

                    break;
            }

        }
    }


}
