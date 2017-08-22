package com.winsun.fruitmix.upload.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
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

    public static final String UPLOAD_FILE_NAME = "android手机照片";

    private static UploadMediaUseCase instance;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private ThreadManager threadManager;

    private SystemSettingDataSource systemSettingDataSource;

    private CheckMediaIsUploadStrategy checkMediaIsUploadStrategy;

    private StationFileRepository stationFileRepository;

    private boolean mStopUpload = false;

    private boolean mAlreadyStartUpload = false;

    private String currentUserHome;

    private String currentUserUUID;

    private int uploadMediaCount = 0;

    private LoggedInUserDataSource loggedInUserDataSource;

    private List<String> uploadedMediaHashs;

    public static UploadMediaUseCase getInstance(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                                                 LoggedInUserDataSource loggedInUserDataSource,
                                                 SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy) {
        if (instance == null)
            instance = new UploadMediaUseCase(mediaDataSourceRepository, stationFileRepository, loggedInUserDataSource,
                    systemSettingDataSource, checkMediaIsUploadStrategy);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private UploadMediaUseCase(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                               LoggedInUserDataSource loggedInUserDataSource,
                               SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy) {
        this.mediaDataSourceRepository = mediaDataSourceRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.checkMediaIsUploadStrategy = checkMediaIsUploadStrategy;
        this.loggedInUserDataSource = loggedInUserDataSource;

        threadManager = ThreadManager.getInstance();

    }

    public void startUploadMedia() {

        if (mAlreadyStartUpload) {
            Log.d(TAG, "startUploadMedia: already start");

            return;
        }

        currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(currentUserUUID);

        if (loggedInUser == null)
            return;

        currentUserHome = loggedInUser.getUser().getHome();

        Log.d(TAG, "startUploadMedia: ");

        if (mStopUpload)
            mStopUpload = false;

        threadManager.runOnUploadMediaThread(new Runnable() {
            @Override
            public void run() {

                initUploadedMediaHashs();

            }
        });

    }

    private void initUploadedMediaHashs() {

        stationFileRepository.getFile(currentUserHome, currentUserHome, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                String uploadFolderUUID = getUploadFolderUUID(data);

                if (uploadFolderUUID != null) {

                    Log.i(TAG, "onSucceed: get uploaded media hash list");

                    if (uploadedMediaHashs == null)
                        getUploadedMediaHashList(uploadFolderUUID);
                    else
                        startAutoUpload(uploadFolderUUID);

                } else {

                    Log.d(TAG, "no upload folder");

                    uploadedMediaHashs = new CopyOnWriteArrayList<>();

                    checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

                    startAutoUpload(null);
                }

            }
        });


    }

    private void getUploadedMediaHashList(final String uploadFolderUUID) {

        stationFileRepository.getFile(currentUserHome, uploadFolderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                uploadedMediaHashs = new CopyOnWriteArrayList<>();

                for (AbstractRemoteFile file : data) {
                    if (file instanceof RemoteFile)
                        uploadedMediaHashs.add(((RemoteFile) file).getFileHash());
                }

                Log.d(TAG, "uploadedMediaHashs size: " + uploadedMediaHashs.size());

                checkMediaIsUploadStrategy.setUploadedMediaHashs(uploadedMediaHashs);

                startAutoUpload(uploadFolderUUID);

            }
        });

    }


    private void startAutoUpload(final String uploadFolderUUID) {
        if (!systemSettingDataSource.getAutoUploadOrNot())
            return;

        mAlreadyStartUpload = true;

        startUploadMediaInThread(mediaDataSourceRepository.getLocalMedia(), uploadFolderUUID);

    }

    private void startUploadMediaInThread(final List<Media> medias, String uploadFolderUUID) {

        if (uploadFolderUUID != null) {

            Log.i(TAG, "onSucceed: get uploaded media hash list");

            uploadMedia(medias, uploadFolderUUID);

        } else {

            Log.i(TAG, "onSucceed: create upload folder");

            createUploadFolder(medias);
        }

    }

    private void createUploadFolder(final List<Media> medias) {
        stationFileRepository.createFolder(UPLOAD_FILE_NAME, currentUserHome, currentUserHome, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteFileFolderParser parser = new RemoteFileFolderParser();

                try {

                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());

                    String uploadFolderUUID = getUploadFolderUUID(files);

                    if (uploadFolderUUID != null)
                        uploadMedia(medias, uploadFolderUUID);


                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "get upload folder uuid fail");

                }

            }

        });
    }

    private String getUploadFolderUUID(List<AbstractRemoteFile> files) {
        String uploadFolderUUID;
        for (AbstractRemoteFile file : files) {
            if (file.getName().equals(UPLOAD_FILE_NAME)) {

                uploadFolderUUID = file.getUuid();

                Log.d(TAG, "onSucceed: get upload folder uuid succeed file uuid:" + uploadFolderUUID);

                return uploadFolderUUID;
            }

        }
        return null;
    }

    private void uploadMedia(final List<Media> medias, String uploadFolderUUID) {

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

        if (needUploadedMedia.size() <= uploadMediaCount)
            mAlreadyStartUpload = false;
        else {
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

                            if (media.getUploadedUserUUIDs().isEmpty()) {
                                media.setUploadedUserUUIDs(currentUserUUID);
                            } else if (!media.getUploadedUserUUIDs().contains(currentUserUUID)) {
                                media.setUploadedUserUUIDs(media.getUploadedUserUUIDs() + "," + currentUserUUID);
                            }

                            mediaDataSourceRepository.updateMedia(media);

                            uploadedMediaHashs.add(media.getUuid());

                            if (!mStopUpload)
                                uploadMedia(medias, uploadFolderUUID);
                        }

                        @Override
                        public void onFail(OperationResult result) {
                            super.onFail(result);

                            if (!mStopUpload)
                                uploadMedia(medias, uploadFolderUUID);
                        }
                    });
                } else {

                    if (!mStopUpload)
                        uploadMedia(medias, uploadFolderUUID);
                }

            }
        });
    }


    public void stopUploadMedia() {

        mStopUpload = true;

        mAlreadyStartUpload = false;

        uploadMediaCount = 0;

        Log.d(TAG, "stopUploadMedia: ");

        threadManager.stopUploadMediaThreadNow();

    }

}
