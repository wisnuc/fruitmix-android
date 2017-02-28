package com.winsun.fruitmix.refactor.data.memory;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/2/9.
 */

public class MemoryDataSource implements DataSource {

    private String mGateway = "http://192.168.5.98";
    private String mPort = "3721";

    private String mToken = null;
    private String mDeviceID = null;

    private String mCurrentLoginUserUUID = null;

    private ConcurrentMap<String, MediaShare> remoteMediaShareMapKeyIsUUID = null;
    private ConcurrentMap<String, User> remoteUserMapKeyIsUUID = null;
    private ConcurrentMap<String, Media> remoteMediaMapKeyIsUUID = null;
    private ConcurrentMap<String, Media> localMediaMapKeyIsThumb = null;
    private ConcurrentMap<String, AbstractRemoteFile> remoteFileMapKeyIsUUID = null;
    private List<AbstractRemoteFile> remoteFileShareList = null;

    public MemoryDataSource() {

        remoteMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteUserMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        localMediaMapKeyIsThumb = new ConcurrentHashMap<>();
        remoteFileMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteFileShareList = new ArrayList<>();
    }

    @Override
    public void deleteToken() {
        mToken = null;
    }

    @Override
    public OperateMediaShareResult insertRemoteMediaShare(String url,String token,MediaShare mediaShare) {

        remoteMediaShareMapKeyIsUUID.putIfAbsent(mediaShare.getUuid(), mediaShare);

        OperateMediaShareResult result = new OperateMediaShareResult();
        result.setMediaShare(mediaShare);
        result.setOperationResult(new OperationSuccess());

        return result;

    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {

        for (MediaShare mediaShare : mediaShares) {
            remoteMediaShareMapKeyIsUUID.putIfAbsent(mediaShare.getUuid(), mediaShare);
        }

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {

        for (Media media : medias) {
            localMediaMapKeyIsThumb.putIfAbsent(media.getThumb(), media);
        }

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {

        for (Media media : medias) {
            remoteMediaMapKeyIsUUID.putIfAbsent(media.getUuid(), media);
        }

        return new OperationSuccess();
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String url,String token,String requestData, MediaShare modifiedMediaShare) {

        MediaShare originalMediaShare = remoteMediaShareMapKeyIsUUID.get(modifiedMediaShare.getUuid());

        originalMediaShare.setTitle(modifiedMediaShare.getTitle());
        originalMediaShare.setDesc(modifiedMediaShare.getDesc());
        originalMediaShare.clearViewers();
        originalMediaShare.addViewers(modifiedMediaShare.getViewers());
        originalMediaShare.clearMaintainers();
        originalMediaShare.addMaintainers(modifiedMediaShare.getMaintainers());

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteRemoteMediaShare(String url,String token,MediaShare mediaShare) {

        remoteMediaShareMapKeyIsUUID.remove(mediaShare.getUuid());

        return new OperationSuccess();
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {
        return null;
    }

    @Override
    public UsersLoadOperationResult loadUsers() {

        UsersLoadOperationResult result = new UsersLoadOperationResult();

        result.setUsers(new ArrayList<>(remoteUserMapKeyIsUUID.values()));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    public Collection<String> loadLocalMediaUUIDs() {

        return localMediaMapKeyIsThumb.keySet();

    }

    @Override
    public MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        result.setMedias(new ArrayList<>(remoteMediaMapKeyIsUUID.values()));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        result.setMedias(new ArrayList<>(localMediaMapKeyIsThumb.values()));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    public Media loadMedia(String mediaKey) {
        Media media;

        media = findMediaInLocalMediaMap(mediaKey);

        if (media == null) {
            media = remoteMediaMapKeyIsUUID.get(mediaKey);
        }
        return media;
    }

    private Media findMediaInLocalMediaMap(String key) {

        Collection<Media> collection = localMediaMapKeyIsThumb.values();

        for (Media media : collection) {
            if (media.getUuid().equals(key) || media.getThumb().equals(key))
                return media;
        }

        return null;
    }

    @Override
    public void updateLocalMediasUploadedFalse() {
    }

    @Override
    public OperateUserResult insertUser(String url, String token, String userName, String userPassword) {
        return null;
    }

    @Override
    public OperationResult insertUsers(List<User> users) {

        for (User user : users) {
            remoteUserMapKeyIsUUID.putIfAbsent(user.getUuid(), user);
        }

        return new OperationSuccess();
    }

    public User loadUser(String userUUID) {
        if (remoteUserMapKeyIsUUID.containsKey(userUUID)) {
            return remoteUserMapKeyIsUUID.get(userUUID);
        } else
            return null;
    }


    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        return findMediaShareInLocalCacheMap(mediaShareUUID);
    }

    private MediaShare findMediaShareInLocalCacheMap(String mediaShareUUID) {

        return remoteMediaShareMapKeyIsUUID.get(mediaShareUUID);

    }

    @Override
    public MediaSharesLoadOperationResult loadAllRemoteMediaShares() {

        MediaSharesLoadOperationResult result = new MediaSharesLoadOperationResult();

        result.setMediaShares(new ArrayList<>(remoteMediaShareMapKeyIsUUID.values()));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFilesRecord() {
        return null;
    }

    @Override
    public OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs) {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        TokenLoadOperationResult result = new TokenLoadOperationResult();

        result.setToken(mToken);
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public User loadCurrentLoginUser() {

        return remoteUserMapKeyIsUUID.get(mCurrentLoginUserUUID);

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
    public List<EquipmentAlias> loadEquipmentAlias(String token,String url) {
        return null;
    }

    @Override
    public List<User> loadUserByLoginApi(String token,String url) {
        return null;
    }

    @Override
    public OperationResult deleteAllRemoteMediaShare() {

        remoteMediaShareMapKeyIsUUID.clear();

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteAllRemoteMedia() {

        remoteMediaMapKeyIsUUID.clear();

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteAllRemoteUsers() {

        remoteUserMapKeyIsUUID.clear();

        return null;
    }

    @Override
    public OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem) {
        return null;
    }

    @Override
    public OperationResult insertToken(String token) {

        mToken = token;

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertDeviceID(String deviceID) {

        mDeviceID = deviceID;

        return new OperationSuccess();
    }

    @Override
    public void deleteDeviceID() {
        mDeviceID = null;
    }

    @Override
    public OperationResult insertCurrentLoginUser(User user) {

        mCurrentLoginUserUUID = user.getUuid();

        return new OperationSuccess();
    }

    @Override
    public String loadGateway() {
        return mGateway;
    }

    @Override
    public String loadPort() {
        return mPort;
    }
}
