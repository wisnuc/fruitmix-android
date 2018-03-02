package com.winsun.fruitmix.list.data;

import android.content.Context;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.FileListViewDataSource;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromBoxDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/25.
 */

public class FileInTweetViewDataSource implements FileListViewDataSource {

    private String mCurrentUserUUID;

    private List<AbstractRemoteFile> mAbstractRemoteFiles;

    private StationFileRepository mStationFileRepository;

    private String groupUUID;

    private String stationID;

    public FileInTweetViewDataSource(Context context, StationFileRepository stationFileRepository,
                                     FileComment fileComment, String currentUserUUID) {
        mStationFileRepository = stationFileRepository;
        this.groupUUID = fileComment.getGroupUUID();
        this.stationID = fileComment.getStationID();

        reset(context);

        List<AbstractFile> files = fileComment.getFiles();

        for (AbstractFile file : files) {
            mAbstractRemoteFiles.add((AbstractRemoteFile) file);
        }

        setCurrentUserUUID(currentUserUUID);
    }

    @Override
    public void reset(Context context) {

        mAbstractRemoteFiles = new ArrayList<>();

    }

    @Override
    public void setCurrentUserUUID(String currentUserUUID) {

        mCurrentUserUUID = currentUserUUID;

    }

    @Override
    public String getCurrentFolderName() {
        return "";
    }

    @Override
    public void getFilesInCurrentFolder(Context context, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        callback.onSucceed(mAbstractRemoteFiles, new OperationSuccess());

    }

    @Override
    public boolean checkCanGoToUpperLevel() {
        return false;
    }

    @Override
    public void getFilesInParentFolder(Context context, BaseLoadDataCallback<AbstractRemoteFile> callback) {

    }

    @Override
    public void getFilesInFolder(Context context, AbstractRemoteFile folder, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        callback.onSucceed(mAbstractRemoteFiles, new OperationSuccess());

    }

    @Override
    public FileDownloadParam createFileDownloadParam(AbstractRemoteFile abstractRemoteFile) {
        return new FileFromBoxDownloadParam(groupUUID, stationID, ((RemoteFile) abstractRemoteFile).getFileHash());
    }

}
