package com.winsun.fruitmix.data.memory;

import android.util.Log;

import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.data.DataSource;
import com.winsun.fruitmix.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.EquipmentAlias;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by Administrator on 2017/2/9.
 */

public class MemoryDataSource implements DataSource {

    private static MemoryDataSource INSTANCE;

    public static final String TAG = MemoryDataSource.class.getSimpleName();

    private String mGateway = "http://192.168.5.98";

    private String mToken = null;
    private String mDeviceID = null;

    private String mCurrentLoginUserUUID = null;

    private ConcurrentMap<String, MediaShare> remoteMediaShareMapKeyIsUUID = null;
    private ConcurrentMap<String, User> remoteUserMapKeyIsUUID = null;
    private ConcurrentMap<String, Media> remoteMediaMapKeyIsUUID = null;
    private ConcurrentMap<String, Media> localMediaMapKeyIsThumb = null;
    private ConcurrentMap<String, AbstractRemoteFile> remoteFileMapKeyIsUUID = null;
    private List<AbstractRemoteFile> remoteFileShareList = null;
    private List<LoggedInUser> mLoggedInUsers = null;

    private MemoryDataSource() {

        remoteMediaShareMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteUserMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteMediaMapKeyIsUUID = new ConcurrentHashMap<>();
        localMediaMapKeyIsThumb = new ConcurrentHashMap<>();
        remoteFileMapKeyIsUUID = new ConcurrentHashMap<>();
        remoteFileShareList = new ArrayList<>();
        mLoggedInUsers = new ArrayList<>();
    }

    public static MemoryDataSource getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MemoryDataSource();

        return INSTANCE;
    }

    @Override
    public void init() {

        mToken = null;
        mDeviceID = null;
        mCurrentLoginUserUUID = null;

        remoteMediaShareMapKeyIsUUID.clear();
        remoteUserMapKeyIsUUID.clear();
        remoteMediaMapKeyIsUUID.clear();
        remoteFileMapKeyIsUUID.clear();
        remoteFileShareList.clear();
        mLoggedInUsers.clear();

    }

    @Override
    public void deleteToken() {
        mToken = null;
    }

    @Override
    public OperateMediaShareResult insertRemoteMediaShare(MediaShare mediaShare) {

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
    public OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare) {

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
    public OperationResult modifyMediaInRemoteMediaShare(String requestData, MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare, MediaShare modifiedMediaShare) {

        remoteMediaShareMapKeyIsUUID.put(diffContentsModifiedMediaShare.getUuid(), modifiedMediaShare);

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteRemoteMediaShare(MediaShare mediaShare) {

        remoteMediaShareMapKeyIsUUID.remove(mediaShare.getUuid());

        return new OperationSuccess();
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {

        DeviceIDLoadOperationResult result = new DeviceIDLoadOperationResult();
        result.setDeviceID(mDeviceID);

        return result;
    }

    @Override
    public UsersLoadOperationResult loadRemoteUsers() {

        UsersLoadOperationResult result = new UsersLoadOperationResult();

        result.setUsers(new ArrayList<>(remoteUserMapKeyIsUUID.values()));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    public Collection<String> loadLocalMediaThumbs() {

        return localMediaMapKeyIsThumb.keySet();

    }

    @Override
    public OperationResult insertLocalMedia(Media media) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult updateLocalMediaMiniThumb(Media media) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult updateLocalMediaUploadedDeviceID(Media media) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public Collection<String> loadRemoteMediaUUIDs() {
        return remoteMediaMapKeyIsUUID.keySet();
    }

    @Override
    public Media loadLocalMediaByThumb(String thumb) {
        return localMediaMapKeyIsThumb.get(thumb);
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

        if (mediaKey.isEmpty())
            return null;

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
    public OperateUserResult insertRemoteUser(String userName, String userPassword) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertRemoteUsers(List<User> users) {

        for (User user : users) {
            remoteUserMapKeyIsUUID.putIfAbsent(user.getUuid(), user);
        }

        return new OperationSuccess();
    }

    public User loadRemoteUser(String userUUID) {
        if (remoteUserMapKeyIsUUID.containsKey(userUUID)) {
            return remoteUserMapKeyIsUUID.get(userUUID);
        } else
            return null;
    }

    @Override
    public Collection<String> loadAllRemoteUserUUID() {
        return remoteUserMapKeyIsUUID.keySet();
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
    public FilesLoadOperationResult loadRemoteFolder(String folderUUID) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertRemoteFiles(AbstractRemoteFile folder) {

        remoteFileMapKeyIsUUID.putIfAbsent(folder.getUuid(), folder);

        return new OperationSuccess();
    }

    @Override
    public OperationResult downloadRemoteFile(FileDownloadState fileDownloadState) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult deleteAllRemoteFiles() {

        remoteFileMapKeyIsUUID.clear();

        return new OperationSuccess();
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFilesRecord(String userUUID) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs, String userUUID) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertRemoteFileShare(List<AbstractRemoteFile> files) {

        remoteFileShareList.addAll(files);

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteAllRemoteFileShare() {

        remoteFileShareList.clear();

        return new OperationSuccess();
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        TokenLoadOperationResult result = new TokenLoadOperationResult();

        result.setToken(mToken);
        result.setOperationResult(null);

        return result;
    }

    @Override
    public String loadToken() {
        return mToken;
    }

    @Override
    public OperationResult insertGateway(String gateway) {

        mGateway = gateway;

        return new OperationSuccess();
    }

    @Override
    public boolean getShowAlbumTipsValue() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public void saveShowAlbumTipsValue(boolean value) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public boolean getShowPhotoReturnTipsValue() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public void saveShowPhotoReturnTipsValue(boolean value) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public List<EquipmentAlias> loadEquipmentAlias(String url) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public List<User> loadRemoteUserByLoginApi(String url) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public boolean deleteAllRemoteMediaShare() {

        remoteMediaShareMapKeyIsUUID.clear();

        return true;
    }

    @Override
    public boolean deleteAllRemoteMedia() {

        remoteMediaMapKeyIsUUID.clear();

        return true;
    }

    @Override
    public boolean deleteAllRemoteUsers() {

        remoteUserMapKeyIsUUID.clear();

        return true;
    }

    @Override
    public OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
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
    public List<LoggedInUser> loadLoggedInUser() {

        return new ArrayList<>(mLoggedInUsers);
    }

    @Override
    public void insertLoggedInUser(List<LoggedInUser> loggedInUsers) {
        mLoggedInUsers.addAll(loggedInUsers);
    }

    @Override
    public void deleteLoggedInUser(LoggedInUser loggedInUser) {
        mLoggedInUsers.remove(loggedInUser);
    }

    @Override
    public String loadGateway() {
        return mGateway;
    }

    @Override
    public OperationResult insertLoginUserUUID(String userUUID) {

        mCurrentLoginUserUUID = userUUID;
        return new OperationSuccess();
    }

    @Override
    public String loadLoginUserUUID() {

        return mCurrentLoginUserUUID;
    }

    @Override
    public boolean getAutoUploadOrNot() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public void saveAutoUploadOrNot(boolean autoUploadOrNot) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public void saveCurrentUploadDeviceID(String currentUploadDeviceID) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public String getCurrentUploadDeviceID() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }
}
