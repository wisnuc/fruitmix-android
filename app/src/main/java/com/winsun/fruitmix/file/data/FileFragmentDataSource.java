package com.winsun.fruitmix.file.data;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteBuiltInDrive;
import com.winsun.fruitmix.file.data.model.RemotePrivateDrive;
import com.winsun.fruitmix.file.data.model.RemotePublicDrive;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/2/24.
 */

public class FileFragmentDataSource implements FileListDataSource {

    private static final String SHARED_DRIVE_UUID = "shared_drive_uuid";

    private static String ROOT_DRIVE_UUID = "root_drive_uuid";

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private String currentFolderUUID;
    private String currentFolderName;

    private List<String> retrievedFolderUUIDList;

    private List<String> retrievedFolderNameList;

    private String rootUUID;

    private String driveRootUUID;

    private StationFileRepository mStationFileRepository;

    private List<AbstractRemoteFile> sharedDriveFiles;

    private String mCurrentUserUUID;

    public FileFragmentDataSource(Context context, StationFileRepository stationFileRepository) {

        reset(context);

        mStationFileRepository = stationFileRepository;

    }

    public void setCurrentUserUUID(String currentUserUUID) {
        mCurrentUserUUID = currentUserUUID;
    }

    @Override
    public void reset(Context context) {
        rootUUID = ROOT_DRIVE_UUID;

        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();
        retrievedFolderNameList = new ArrayList<>();

        currentFolderUUID = rootUUID;
        currentFolderName = context.getString(R.string.files);

        driveRootUUID = "";

        sharedDriveFiles = new ArrayList<>();

    }

    @Override
    public void getFilesInCurrentFolder(Context context, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
            retrievedFolderUUIDList.add(currentFolderUUID);
            retrievedFolderNameList.add(currentFolderName);
        }

        getFileInThread(context, callback);

    }

    private void getFileInThread(final Context context, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        if (currentFolderUUID.equals(ROOT_DRIVE_UUID)) {

            mStationFileRepository.getRootDrive(new BaseLoadDataCallback<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {

                    handleGetFileSucceed(handleRootDriveUUID(context, data), false);

                    callback.onSucceed(abstractRemoteFiles, operationResult);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    handleGetFileFail();

                    callback.onFail(operationResult);
                }
            });

        } else if (currentFolderUUID.equals(SHARED_DRIVE_UUID)) {

            handleGetFileSucceed(sharedDriveFiles, false);

            callback.onSucceed(abstractRemoteFiles, new OperationSuccess());

        } else {

            mStationFileRepository.getFile(driveRootUUID, currentFolderUUID, currentFolderName,new BaseLoadDataCallback<AbstractRemoteFile>() {
                @Override
                public void onSucceed(final List<AbstractRemoteFile> data, OperationResult operationResult) {

                    handleGetFileSucceed(data, true);

                    callback.onSucceed(abstractRemoteFiles, operationResult);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    if (operationResult instanceof OperationNetworkException) {
                        ToastUtil.showToast(context, operationResult.getResultMessage(context));
                    }

                    handleGetFileFail();

                    callback.onFail(operationResult);

                }
            });

        }

    }

    private List<AbstractRemoteFile> handleRootDriveUUID(Context context, List<AbstractRemoteFile> data) {

        sharedDriveFiles.clear();

        List<AbstractRemoteFile> result = new ArrayList<>();

        for (AbstractRemoteFile file : data) {

            if (file instanceof RemotePrivateDrive) {
                file.setName(context.getString(R.string.my_file));
                result.add(file);
            } else if (file instanceof RemotePublicDrive) {

                if (file.getWriteList().contains(mCurrentUserUUID) || file.getWriteList().contains(AbstractRemoteFile.ALL_CAN_VIEW))
                    sharedDriveFiles.add(file);

            } else if (file instanceof RemoteBuiltInDrive) {

                file.setName(context.getString(R.string.built_in_drive));

                result.add(file);

            }

        }

        if (sharedDriveFiles.size() != 0) {

            AbstractRemoteFile file = new RemotePublicDrive();
            file.setName(context.getString(R.string.shared_drive));
            file.setUuid(SHARED_DRIVE_UUID);

            result.add(file);
        }

        return result;

    }


    private void handleGetFileFail() {

        abstractRemoteFiles.clear();

    }

    private void handleGetFileSucceed(List<AbstractRemoteFile> files, boolean needSort) {

        abstractRemoteFiles.clear();
        abstractRemoteFiles.addAll(files);

        if (needSort)
            sortFile(abstractRemoteFiles);

    }

    private void sortFile(List<AbstractRemoteFile> abstractRemoteFiles) {

        List<AbstractRemoteFile> files = new ArrayList<>();
        List<AbstractRemoteFile> folders = new ArrayList<>();

        for (AbstractRemoteFile abstractRemoteFile : abstractRemoteFiles) {
            if (abstractRemoteFile.isFolder())
                folders.add(abstractRemoteFile);
            else
                files.add(abstractRemoteFile);
        }

        Comparator<AbstractRemoteFile> comparator = new Comparator<AbstractRemoteFile>() {
            @Override
            public int compare(AbstractRemoteFile lhs, AbstractRemoteFile rhs) {

                long lhsTime = lhs.getTime();
                long rhsTime = rhs.getTime();

                if (lhsTime > rhsTime)
                    return 1;
                else if (rhsTime > lhsTime)
                    return -1;
                else
                    return 0;

            }
        };

        Collections.sort(folders, comparator);
        Collections.sort(files, comparator);

        abstractRemoteFiles.clear();
        abstractRemoteFiles.addAll(folders);
        abstractRemoteFiles.addAll(files);

    }


    @Override
    public void getFilesInFolder(Context context, AbstractRemoteFile folder, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        currentFolderUUID = folder.getUuid();

        retrievedFolderUUIDList.add(currentFolderUUID);

        currentFolderName = folder.getName();

        retrievedFolderNameList.add(currentFolderName);

        if (folder instanceof RemotePrivateDrive)
            driveRootUUID = currentFolderUUID;
        else if (folder instanceof RemotePublicDrive)
            driveRootUUID = currentFolderUUID;
        else if (folder instanceof RemoteBuiltInDrive)
            driveRootUUID = currentFolderUUID;

        getFileInThread(context, callback);

    }

    @Override
    public boolean checkCanGoToUpperLevel() {
        return !currentFolderUUID.equals(rootUUID);
    }

    @Override
    public void getFilesInParentFolder(Context context, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);
        currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

        retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
        currentFolderName = retrievedFolderNameList.get(retrievedFolderNameList.size() - 1);

        getFileInThread(context, callback);

    }

    @Override
    public FileDownloadParam createFileDownloadParam(AbstractRemoteFile abstractRemoteFile) {
        return new FileFromStationFolderDownloadParam(abstractRemoteFile.getUuid(),
                abstractRemoteFile.getParentFolderUUID(), driveRootUUID, abstractRemoteFile.getName());
    }

    @Override
    public String getCurrentFolderName() {
        return currentFolderName;
    }
}
