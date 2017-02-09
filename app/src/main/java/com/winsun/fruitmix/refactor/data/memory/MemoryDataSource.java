package com.winsun.fruitmix.refactor.data.memory;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.util.LocalCache;

import java.util.Collection;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MemoryDataSource implements DataSource {

    public void logout(){
        Collection<Media> medias = LocalCache.LocalMediaMapKeyIsThumb.values();

        for (Media media : medias) {
            media.restoreUploadState();
        }
    }

    @Override
    public OperationResult saveUser(User user) {
        return null;
    }

    @Override
    public OperationResult saveMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {
        return null;
    }

    @Override
    public UsersLoadOperationResult loadUsers() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadMedias() {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadFiles() {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadFileShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {
        return null;
    }

    @Override
    public User loadCurrentUser() {
        return null;
    }
}
