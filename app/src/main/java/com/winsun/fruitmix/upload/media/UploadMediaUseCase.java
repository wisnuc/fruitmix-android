package com.winsun.fruitmix.upload.media;

import android.util.Log;
import android.widget.Toast;

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
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017/8/18.
 */

public class UploadMediaUseCase {

    public static final String TAG = UploadMediaUseCase.class.getSimpleName();

    static final String UPLOAD_PARENT_FOLDER_NAME = "上传的照片";

    static final String UPLOAD_FOLDER_NAME_PREFIX = "来自";

    private String uploadFolderName;

    private static UploadMediaUseCase instance;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private ThreadManager threadManager;

    private SystemSettingDataSource systemSettingDataSource;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private StationFileRepository stationFileRepository;

    boolean mStopUpload = false;

    boolean mAlreadyStartUpload = false;

    private String currentUserHome;

    private String currentUserUUID;

    String uploadParentFolderUUID;

    String uploadFolderUUID;

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

    public void startUploadMedia() {

        if (mAlreadyStartUpload) {
            Log.d(TAG, "startUploadMedia: already start");

            return;
        }

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

        if (uploadParentFolderUUID == null || uploadFolderUUID == null)
            checkFolderExist(currentUserHome, currentUserHome, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    handleGetRootFolderResult(data);

                }

            });
        else
            startAutoUpload();

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
                    notifyGetFolderFail(((OperationNetworkException) operationResult).getResponseCode());
                } else
                    notifyGetFolderFail(-1);

                stopUploadMedia();

            }
        });

    }

    private void handleGetRootFolderResult(List<AbstractRemoteFile> data) {

        uploadParentFolderUUID = getFolderUUIDByName(data, UPLOAD_PARENT_FOLDER_NAME);

        if (uploadParentFolderUUID != null) {

            Log.i(TAG, "upload parent folder exist");

            checkFolderExist(currentUserHome, uploadParentFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    handleGetUploadFolderResult(data);

                }
            });

        } else {

            startAutoUploadWithNoFolder();

        }

    }

    private void startAutoUploadWithNoFolder() {
        Log.d(TAG, "init upload media hashs no upload folder");

        uploadedMediaHashs = new CopyOnWriteArrayList<>();

        checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

        startAutoUpload();
    }


    private void handleGetUploadFolderResult(List<AbstractRemoteFile> data) {

        uploadFolderUUID = getFolderUUIDByName(data, getUploadFolderName());

        if (uploadFolderUUID != null) {

            Log.i(TAG, "uploaded folder exist");

            if (uploadedMediaHashs == null)
                getUploadedMediaHashList(uploadFolderUUID);
            else
                startAutoUpload();

        } else {
            startAutoUploadWithNoFolder();
        }

    }


    private void getUploadedMediaHashList(final String uploadFolderUUID) {

        Log.i(TAG, "start getUploadedMediaHashList");

        stationFileRepository.getFile(currentUserHome, uploadFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                uploadedMediaHashs = new CopyOnWriteArrayList<>();

                for (AbstractRemoteFile file : data) {
                    if (file instanceof RemoteFile)
                        uploadedMediaHashs.add(((RemoteFile) file).getFileHash());
                }

                Log.i(TAG, "getUploadedMediaHashList uploadedMediaHashs size: " + uploadedMediaHashs.size());

                checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

                calcAlreadyUploadedMediaCount();

                startAutoUpload();

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                if (operationResult instanceof OperationNetworkException) {
                    notifyGetUploadMediaCountFail(((OperationNetworkException) operationResult).getResponseCode());
                } else
                    notifyGetUploadMediaCountFail(-1);

                startAutoUploadWithNoFolder();
            }
        });

    }

    private void calcAlreadyUploadedMediaCount() {
        for (Media media : mediaDataSourceRepository.getLocalMedia()) {
            if (checkMediaIsUploadStrategy.isMediaUploaded(media))
                alreadyUploadedMediaCount++;
        }
    }


    private void startAutoUpload() {

        notifyUploadMediaCountChange();

        if (!systemSettingDataSource.getAutoUploadOrNot()) {

            Log.d(TAG, "startAutoUpload: auto upload false stop upload");

            stopUploadMedia();

            return;
        }

        startUploadMediaInThread(mediaDataSourceRepository.getLocalMedia());

    }

    private void startUploadMediaInThread(final List<Media> medias) {

        if (uploadParentFolderUUID == null) {

            Log.i(TAG, "start create upload parent folder");

            createUploadParentFolder(medias);
        } else if (uploadFolderUUID == null) {

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

                    if (uploadParentFolderUUID != null) {

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

                    if (uploadFolderUUID != null) {

                        Log.i(TAG, "create upload folder succeed folder uuid:" + uploadFolderUUID);

                        uploadMedia(medias);

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

    private void createFolder(String folderName, String rootUUID, String dirUUID, final BaseOperateDataCallback<HttpResponse> callback) {
        stationFileRepository.createFolder(folderName, rootUUID, dirUUID, new BaseOperateDataCallback<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                callback.onSucceed(data, result);
            }

            @Override
            public void onFail(OperationResult result) {

                if (result instanceof OperationNetworkException)
                    notifyCreateFolderFail(((OperationNetworkException) result).getResponseCode());
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
        return null;
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

                            Log.i(TAG, "upload onSucceed: media uuid: " + media.getUuid() + " user uuid: " + currentUserUUID);

                            if (media.getUploadedUserUUIDs().isEmpty()) {
                                media.setUploadedUserUUIDs(currentUserUUID);
                            } else if (!media.getUploadedUserUUIDs().contains(currentUserUUID)) {
                                media.setUploadedUserUUIDs(media.getUploadedUserUUIDs() + "," + currentUserUUID);
                            }

                            mediaDataSourceRepository.updateMedia(media);

                            uploadedMediaHashs.add(media.getUuid());

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

                                    int code = ((OperationNetworkException) result).getResponseCode();

                                    if (code == 404) {
                                        resetState();
                                        startUploadMedia();
                                    } else
                                        uploadMedia(medias);

                                    notifyUploadMediaFail(code);

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


    public void stopUploadMedia() {

        Log.i(TAG, "stopUploadMedia: stop thread");

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadMediaCount = 0;

        threadManager.stopUploadMediaThreadNow();

    }

    public void resetState() {

        Log.i(TAG, "reset state");

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadMediaCount = 0;

        alreadyUploadedMediaCount = 0;

        uploadFolderUUID = null;

        uploadParentFolderUUID = null;

        uploadedMediaHashs = null;

        currentUserUUID = null;

        currentUserHome = null;

    }

}
