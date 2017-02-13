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
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;

import java.util.Collection;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MemoryDataSource implements DataSource {

    public void logout() {
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

    public Media loadMedia(String mediaKey) {
        Media media;

        media = LocalCache.findMediaInLocalMediaMap(mediaKey);

        if (media == null) {
            media = LocalCache.RemoteMediaMapKeyIsUUID.get(mediaKey);
        }
        return media;
    }

    public User loadUser(String userUUID) {
        if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(userUUID)) {
            return LocalCache.RemoteUserMapKeyIsUUID.get(userUUID);
        } else
            return null;
    }


    public MediaShare loadMediaShare(String mediaShareUUID) {
        return LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);
    }

    public boolean isMediaSharePublic(MediaShare mediaShare) {
        return LocalCache.RemoteUserMapKeyIsUUID.size() == 1 || (FNAS.userUUID != null && mediaShare.getCreatorUUID().equals(FNAS.userUUID)) || (mediaShare.getViewersListSize() != 0 && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID()));
    }

    public boolean checkPermissionToOperateMediaShare(MediaShare mediaShare) {
        return mediaShare.checkMaintainersListContainCurrentUserUUID() || mediaShare.getCreatorUUID().equals(FNAS.userUUID);
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
