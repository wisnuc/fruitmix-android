package com.winsun.fruitmix.upload.media;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
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
import java.util.List;
import java.util.concurrent.Callable;

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

    private String currentUserHome;

    private String currentUserUUID;

    private LoggedInUserDataSource loggedInUserDataSource;

    public static UploadMediaUseCase getInstance(MediaDataSourceRepository mediaDataSourceRepository, StationFileRepository stationFileRepository,
                                                 LoggedInUserDataSource loggedInUserDataSource,
                                                 SystemSettingDataSource systemSettingDataSource, CheckMediaIsUploadStrategy checkMediaIsUploadStrategy) {
        if (instance == null)
            instance = new UploadMediaUseCase(mediaDataSourceRepository, stationFileRepository, loggedInUserDataSource,
                    systemSettingDataSource, checkMediaIsUploadStrategy);
        return instance;
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

        currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

        LoggedInUser loggedInUser = loggedInUserDataSource.getLoggedInUserByUserUUID(currentUserUUID);

        if (loggedInUser == null)
            return;

        currentUserHome = loggedInUser.getUser().getHome();

        Log.d(TAG, "startUploadMedia: ");

        if (mStopUpload)
            mStopUpload = false;

        if (!systemSettingDataSource.getAutoUploadOrNot())
            return;

        threadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                startUploadMediaInThread(mediaDataSourceRepository.getLocalMedia());

//                uploadMedia(mediaDataSourceRepository.getLocalMedia(), "8b180f72-5a95-48b0-802c-6fe30c6ba855");

            }
        });

    }

    private void startUploadMediaInThread(final List<Media> medias) {

        stationFileRepository.createFolder(UPLOAD_FILE_NAME, currentUserHome, currentUserHome, new BaseOperateDataCallbackImpl<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                super.onSucceed(data, result);

                RemoteFileFolderParser parser = new RemoteFileFolderParser();

                try {

                    String uploadFolderUUID;

                    List<AbstractRemoteFile> files = parser.parse(data.getResponseData());

                    for (AbstractRemoteFile file : files) {
                        if (file.getName().equals(UPLOAD_FILE_NAME)) {

                            uploadFolderUUID = file.getUuid();

                            Log.d(TAG, "onSucceed: get upload folder uuid succeed file uuid:" + uploadFolderUUID);

                            uploadMedia(medias, uploadFolderUUID);

                            return;
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "onSucceed: get upload folder uuid fail");

                }

            }
        });

    }

    private int i = 0;

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

        uploadMediaInThread(medias, uploadFolderUUID, i);
        i++;

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
                        }
                    });
                }

                uploadMedia(medias, uploadFolderUUID);

            }
        });
    }


    public void stopUploadMedia() {

        mStopUpload = true;

        threadManager.stopUploadMediaThread();

    }

}
