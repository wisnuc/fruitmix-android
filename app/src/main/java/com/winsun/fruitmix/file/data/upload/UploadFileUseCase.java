package com.winsun.fruitmix.file.data.upload;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.network.NetworkState;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMkDirParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 */

public class UploadFileUseCase {

    public static final String TAG = UploadFileUseCase.class.getSimpleName();

    static final String UPLOAD_PARENT_FOLDER_NAME = "上传的文件";

    static final String UPLOAD_FOLDER_NAME_PREFIX = "来自";

    private UserDataRepository userDataRepository;

    private StationFileRepository stationFileRepository;

    private SystemSettingDataSource systemSettingDataSource;

    private NetworkStateManager networkStateManager;

    private String uploadFolderName;

    private String currentUserHome;

    private String currentUserUUID;

    private String uploadParentFolderUUID;

    private String uploadFolderUUID;

    private boolean needRetryForEEXIST = false;

    public UploadFileUseCase(UserDataRepository userDataRepository, StationFileRepository stationFileRepository,
                             SystemSettingDataSource systemSettingDataSource, NetworkStateManager networkStateManager,
                             String uploadFolderName) {
        this.userDataRepository = userDataRepository;
        this.stationFileRepository = stationFileRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.uploadFolderName = uploadFolderName;
        this.networkStateManager = networkStateManager;
    }

    public void updateFile(final FileUploadState fileUploadState) {

        do {

            currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();

            User user = userDataRepository.getUserByUUID(currentUserUUID);

            if (user == null) {

                notifyError(fileUploadState);

                return;
            }

            currentUserHome = user.getHome();

            checkFolderExist(currentUserHome, currentUserHome, fileUploadState, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    handleGetRootFolderResult(data, fileUploadState);
                }
            });

        } while (needRetryForEEXIST);

    }


    private void checkFolderExist(String rootUUID, String dirUUID, final FileUploadState fileUploadState, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        Log.i(TAG, "start check folder exist");

        stationFileRepository.getFileWithoutCreateNewThread(rootUUID, dirUUID, new BaseLoadDataCallback<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                callback.onSucceed(data, operationResult);
            }

            @Override
            public void onFail(OperationResult operationResult) {

                notifyError(fileUploadState);

            }
        });

    }

    private void notifyError(FileUploadState fileUploadState) {
        FileUploadItem fileUploadItem = fileUploadState.getFileUploadItem();

        fileUploadItem.setFileUploadState(new FileUploadErrorState(fileUploadItem));

        needRetryForEEXIST = false;
    }

    private void handleGetRootFolderResult(List<AbstractRemoteFile> data, final FileUploadState fileUploadState) {

        uploadParentFolderUUID = getFolderUUIDByName(data, UPLOAD_PARENT_FOLDER_NAME);

        if (uploadParentFolderUUID.length() != 0) {

            Log.i(TAG, "upload parent folder exist");

            checkFolderExist(currentUserHome, uploadParentFolderUUID, fileUploadState, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                    super.onSucceed(data, operationResult);

                    handleGetUploadFolderResult(data, fileUploadState);

                }
            });

        } else {

            startCreateFolderAndUpload(fileUploadState);

        }

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

    private void handleGetUploadFolderResult(List<AbstractRemoteFile> data, FileUploadState fileUploadState) {

        uploadFolderUUID = getFolderUUIDByName(data, getUploadFolderName());

        if (!uploadFolderUUID.isEmpty()) {

            Log.i(TAG, "uploaded folder exist");

            startPrepareAutoUpload(fileUploadState);

        } else {
            startCreateFolderAndUpload(fileUploadState);
        }

    }

    private String getUploadFolderName() {
        return UPLOAD_FOLDER_NAME_PREFIX + uploadFolderName;
    }

    private void startCreateFolderAndUpload(FileUploadState fileUploadState) {
        Log.d(TAG, "init upload media hashs no upload folder");

        startPrepareAutoUpload(fileUploadState);
    }

    private void startPrepareAutoUpload(FileUploadState fileUploadState) {

        createFolderIfNeed(fileUploadState);

    }

    private void createFolderIfNeed(FileUploadState fileUploadState) {

        if (uploadParentFolderUUID.isEmpty()) {

            Log.i(TAG, "start create upload parent folder");

            createUploadParentFolder(fileUploadState);
        } else if (uploadFolderUUID.isEmpty()) {

            Log.i(TAG, "start create upload folder");

            createUploadFolder(fileUploadState);
        } else {
            Log.i(TAG, "start upload file");

            startUploadFile(fileUploadState);

        }

    }

    private void createUploadParentFolder(final FileUploadState fileUploadState) {

        createFolder(UPLOAD_PARENT_FOLDER_NAME, currentUserHome, currentUserHome, fileUploadState, new BaseOperateDataCallbackImpl<HttpResponse>() {
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

                        createUploadFolder(fileUploadState);

                    } else {

                        Log.i(TAG, "create upload folder succeed but can not find folder uuid,stop upload");

                        notifyError(fileUploadState);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "parse upload parent folder result fail,stop upload");

                    notifyError(fileUploadState);
                }

            }
        });

    }


    private void createUploadFolder(final FileUploadState fileUploadState) {
        createFolder(getUploadFolderName(), currentUserHome, uploadParentFolderUUID, fileUploadState, new BaseOperateDataCallbackImpl<HttpResponse>() {
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

                        startUploadFile(fileUploadState);

                    } else {

                        Log.i(TAG, "create upload folder succeed but can not find folder uuid,stop upload");

                        notifyError(fileUploadState);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.d(TAG, "parse create upload folder result fail,stop upload");

                    notifyError(fileUploadState);

                }

            }

        });
    }

    private void createFolder(String folderName, String rootUUID, String dirUUID, final FileUploadState fileUploadState, final BaseOperateDataCallback<HttpResponse> callback) {
        stationFileRepository.createFolderWithoutCreateNewThread(folderName, rootUUID, dirUUID, new BaseOperateDataCallback<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {
                callback.onSucceed(data, result);
            }

            @Override
            public void onFail(OperationResult result) {

                Log.d(TAG, "onFail: create folder fail,stop upload");

                notifyError(fileUploadState);

                if (result instanceof OperationNetworkException) {

                    int code = ((OperationNetworkException) result).getHttpResponseCode();

                    Log.d(TAG, "create folder onFail,error code: " + code);

                    HttpErrorBodyParser parser = new HttpErrorBodyParser();

                    try {
                        String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseBody());

                        if (messageInBody.contains(HttpErrorBodyParser.UPLOAD_FILE_EXIST_CODE)) {

                            needRetryForEEXIST = true;

                        } else {
                            notifyError(fileUploadState);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        notifyError(fileUploadState);
                    }


                } else
                    notifyError(fileUploadState);

            }
        });
    }

    private void startUploadFile(FileUploadState fileUploadState) {

        if (!checkCanUploadFile())
            return;

        LocalFile localFile = new LocalFile();

        FileUploadItem fileUploadItem = fileUploadState.getFileUploadItem();

        localFile.setFileHash(fileUploadItem.getFileUUID());
        localFile.setSize(fileUploadItem.getFileSize() + "");
        localFile.setPath(fileUploadItem.getFilePath());

        localFile.setName(fileUploadState.getFileUploadItem().getFileName());

        Log.d(TAG, "startUploadFile: file name: " + localFile.getName() + " file path: " + localFile.getPath()
                + " file hash: " + localFile.getFileHash());

        OperationResult result = stationFileRepository.uploadFileWithProgress(localFile, fileUploadState, currentUserHome, uploadFolderUUID,currentUserUUID);

        if (result.getOperationResultType() != OperationResultType.SUCCEED) {
            notifyError(fileUploadState);
        }

    }

    private boolean checkCanUploadFile() {

        NetworkState networkState = networkStateManager.getNetworkState();

        if (systemSettingDataSource.getOnlyAutoUploadWhenConnectedWithWifi() && !networkState.isWifiConnected()) {

            Log.d(TAG, "uploadFile: only auto upload when connect with wifi is true,but wifi is not connected,stop upload");

            return false;

        } else
            return true;

    }

}
