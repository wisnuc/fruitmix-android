package com.winsun.fruitmix.refactor.data.memory;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.util.LocalCache;

import java.util.Collection;
import java.util.List;

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
    public void deleteToken() {

    }


    @Override
    public OperateMediaShareResult insertRemoteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {

        return null;
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {

        return null;
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {

        return null;
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteRemoteMediaShare(MediaShare mediaShare) {
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

    public Collection<String> loadLocalMediaUUIDs() {

        return LocalCache.LocalMediaMapKeyIsThumb.keySet();

    }

    @Override
    public MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {
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

    @Override
    public void updateLocalMediasUploadedFalse() {

    }

    @Override
    public OperateUserResult insertUser(String userName, String userPassword) {
        return null;
    }

    @Override
    public OperationResult insertUsers(List<User> users) {
        return null;
    }

    public User loadUser(String userUUID) {
        if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(userUUID)) {
            return LocalCache.RemoteUserMapKeyIsUUID.get(userUUID);
        } else
            return null;
    }


    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        return LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);
    }

    @Override
    public MediaSharesLoadOperationResult loadAllMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFiles() {
        return null;
    }

    @Override
    public OperationResult deleteDownloadedFile(List<String> fileUUIDs) {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {
        return null;
    }

    @Override
    public User loadCurrentLoginUser() {
        return null;
    }

    @Override
    public LoadTokenParam getLoadTokenParam() {
        return null;
    }

    @Override
    public boolean getShowAlbumTipsValue() {
        return false;
    }

    @Override
    public void saveShowAlbumTipsValue(boolean value) {

    }

    @Override
    public boolean getShowPhotoReturnTipsValue() {
        return false;
    }

    @Override
    public void saveShowPhotoReturnTipsValue(boolean value) {

    }

    @Override
    public List<EquipmentAlias> loadEquipmentAlias(String url) {
        return null;
    }

    @Override
    public List<User> loadUserByLoginApi(String url) {
        return null;
    }
}
