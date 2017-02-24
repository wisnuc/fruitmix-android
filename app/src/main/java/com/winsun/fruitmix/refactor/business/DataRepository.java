package com.winsun.fruitmix.refactor.business;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.refactor.business.callback.FileDownloadOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.FileOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.FileShareOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.refactor.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.OperationCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.data.memory.MemoryDataSource;
import com.winsun.fruitmix.refactor.data.server.ServerDataSource;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.refactor.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataRepository {

    private static DataRepository INSTANCE;

    private DataSource mMemoryDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private Handler mHandler;

    private ExecutorServiceInstance instance;

    private ImageGifLoaderInstance imageGifLoaderInstance;

    private UserOperationCallback.LoadCurrentUserCallback mLoadCurrentUserCallback;

    private DataRepository(DataSource memoryDataSource, DataSource dbDataSource, DataSource serverDataSource) {

        mMemoryDataSource = memoryDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

        mHandler = new Handler(Looper.getMainLooper());

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;

        imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
    }

    public static DataRepository getInstance(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(cacheDataSource, dbDataSource, serverDataSource);
        }
        return INSTANCE;
    }

    public void shutdownFixedThreadPoolNow() {

        instance.shutdownFixedThreadPoolNow();
        instance = null;
    }

    public String loadTokenInDB() {
        return mDBDataSource.loadToken(null).getToken();
    }

    public void loadRemoteToken(LoadTokenParam param, LoadTokenOperationCallback.LoadTokenCallback callback) {

        TokenLoadOperationResult result = mServerDataSource.loadToken(param);
        if (result.getOperationResult().getOperationResultType().equals(OperationResultType.SUCCEED)) {
            callback.onLoadSucceed(result.getOperationResult(), result.getToken());
        } else {
            callback.onLoadFail(result.getOperationResult());
        }

    }

    public void loadUsers(UserOperationCallback.LoadUsersCallback callback) {

        UsersLoadOperationResult result = mMemoryDataSource.loadUsers();

        if (result != null) {
            callback.onLoadSucceed(result.getOperationResult(), result.getUsers());
        } else {

            //TODO: need call in thread
            result = mServerDataSource.loadUsers();

            if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                for (User user : result.getUsers()) {
                    mDBDataSource.insertUser(user);
                    mMemoryDataSource.insertUser(user);
                }

            } else {

                result = mDBDataSource.loadUsers();
                for (User user : result.getUsers()) {
                    mMemoryDataSource.insertUser(user);
                }

            }

            //TODO:need call in main thread
            callback.onLoadSucceed(result.getOperationResult(), result.getUsers());
            if (mLoadCurrentUserCallback != null)
                mLoadCurrentUserCallback.onLoadSucceed(result.getOperationResult(), mMemoryDataSource.loadCurrentLoginUser());

        }

    }

    public void loadLocalMediaInCamera(MediaOperationCallback.LoadMediasCallback callback) {

        Collection<String> localMediaUUIDs = ((MemoryDataSource) mMemoryDataSource).loadLocalMediaUUIDsInDB();

        //TODO:call in thread
        MediasLoadOperationResult result = ((DBDataSource) mDBDataSource).loadLocalMediaInCamera(localMediaUUIDs);

        List<Media> localMediaInCamera = result.getMedias();

        mMemoryDataSource.insertLocalMedias(localMediaInCamera);

        if (localMediaInCamera.size() > 0) {

            List<Media> medias = mMemoryDataSource.loadAllRemoteMedias().getMedias();
            medias.addAll(mMemoryDataSource.loadAllLocalMedias().getMedias());

            //TODO:call in main thread
            callback.onLoadSucceed(result.getOperationResult(), medias);

            //TODO:call in thread
            calcLocalMediaUUIDInCamera(localMediaInCamera);

        }

    }

    private void calcLocalMediaUUIDInCamera(List<Media> medias) {

        for (Media media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = Util.CalcSHA256OfFile(media.getThumb());
                media.setUuid(uuid);
            }
        }

        mDBDataSource.insertLocalMedias(medias);
    }


    public void loadMedias(MediaOperationCallback.LoadMediasCallback callback) {

        List<Media> remoteMedias;
        List<Media> localMedias;
        List<Media> allMedias = new ArrayList<>();

        //TODO:call in thread
        if (((MemoryDataSource) mMemoryDataSource).isLocalMediaLoaded()) {
            localMedias = mMemoryDataSource.loadAllLocalMedias().getMedias();
        } else {
            localMedias = mDBDataSource.loadAllLocalMedias().getMedias();
        }

        if (((MemoryDataSource) mMemoryDataSource).isRemoteMediaLoaded()) {
            remoteMedias = mMemoryDataSource.loadAllRemoteMedias().getMedias();
        } else {

            MediasLoadOperationResult result = mServerDataSource.loadAllRemoteMedias();

            OperationResult operationResult = result.getOperationResult();
            if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
                remoteMedias = result.getMedias();
            } else {
                remoteMedias = mDBDataSource.loadAllRemoteMedias().getMedias();
            }
        }

        allMedias.addAll(remoteMedias);
        allMedias.addAll(localMedias);

        if (callback != null)
            callback.onLoadSucceed(new OperationSuccess(), allMedias);

    }

    public Media loadMediaFromMemory(String mediaKey) {
        return ((MemoryDataSource) mMemoryDataSource).loadMedia(mediaKey);
    }

    public User loadUserFromMemory(String userUUID) {
        return ((MemoryDataSource) mMemoryDataSource).loadUser(userUUID);
    }

    public MediaShare loadMediaShareFromMemory(String mediaShareUUID) {
        return ((MemoryDataSource) mMemoryDataSource).loadMediaShare(mediaShareUUID);
    }

    public void loadMediaInMediaShareFromMemory(MediaShare mediaShare, MediaOperationCallback.LoadMediasCallback callback) {

        //TODO:call in thread
        List<String> mediaKeys = mediaShare.getMediaKeyInMediaShareContents();
        List<Media> medias = new ArrayList<>(mediaKeys.size());

        for (String key : mediaKeys) {

            Media media = ((MemoryDataSource) mMemoryDataSource).loadMedia(key);
            if (media == null) {
                media = new Media();
            }
            medias.add(media);
        }

        callback.onLoadSucceed(new OperationSuccess(), medias);

    }

    public void handleMediasForMediaFragment(List<Media> medias, MediaOperationCallback.HandleMediaForMediaFragmentCallback callback) {

        MediaFragmentDataLoader loader = new MediaFragmentDataLoader();

        //TODO: handle medias in thread


    }

    public void loadMediaShares(MediaShareOperationCallback.LoadMediaSharesCallback callback) {

    }

    public boolean isMediaSharePublic(MediaShare mediaShare) {
        return ((MemoryDataSource) mMemoryDataSource).isMediaSharePublic(mediaShare);
    }

    public boolean checkPermissionToOperateMediaShare(MediaShare mediaShare) {
        return ((MemoryDataSource) mMemoryDataSource).checkPermissionToOperateMediaShare(mediaShare);
    }

    public boolean getShowAlbumTipsValue() {
        return ((DBDataSource) mDBDataSource).getShowAlbumTipsValue();
    }

    public void saveShowAlbumTipsValue(boolean value) {
        ((DBDataSource) mDBDataSource).saveShowAlbumTipsValue(value);
    }

    public boolean getShowPhotoReturnTipsValue() {

        return ((DBDataSource) mDBDataSource).getShowPhotoReturnTipsValue();
    }

    public void setShowPhotoReturnTipsValue(boolean value) {

        ((DBDataSource) mDBDataSource).setShowPhotoReturnTipsValue(value);
    }

    public LoadTokenParam getLoadTokenParamInDB() {

        return ((DBDataSource) mDBDataSource).getLoadTokenParam();

    }

    public void loadEquipmentAlias(final String url, final LoadEquipmentAliasCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<EquipmentAlias> equipmentAliases = ((ServerDataSource) mServerDataSource).loadEquipmentAlias(url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (equipmentAliases.isEmpty()) {
                            callback.onLoadFail(null);
                        } else {
                            callback.onLoadSucceed(null, equipmentAliases);
                        }
                    }
                });
            }
        });


    }

    public void loadUserByLoginApi(final String url, final UserOperationCallback.LoadUsersCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<User> users = ((ServerDataSource) mServerDataSource).loadUserByLoginApi(url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (url.isEmpty()) {
                            callback.onLoadFail(null);
                        } else {
                            callback.onLoadSucceed(null, users);
                        }
                    }
                });

            }
        });

    }

    public void logout(OperationCallback callback) {

        ((MemoryDataSource) mMemoryDataSource).logout();
        ((DBDataSource) mDBDataSource).logout();

        instance.shutdownFixedThreadPoolNow();

        ButlerService.stopTimingRetrieveMediaShare();

        Util.setRemoteMediaLoaded(false);
        Util.setRemoteMediaShareLoaded(false);
    }

    public User loadCurrentLoginUserFromMemory() {
        return mMemoryDataSource.loadCurrentLoginUser();
    }

    public void loadCurrentLoginUser(UserOperationCallback.LoadCurrentUserCallback callback) {

        User user = mMemoryDataSource.loadCurrentLoginUser();
        if (user != null) {
            callback.onLoadSucceed(new OperationSuccess(), user);
        } else {
            user = mDBDataSource.loadCurrentLoginUser();
            callback.onLoadSucceed(new OperationSuccess(), user);

            addCallbackWhenLoadUsersFinished(callback);

        }

    }

    private void addCallbackWhenLoadUsersFinished(UserOperationCallback.LoadCurrentUserCallback callback) {
        mLoadCurrentUserCallback = callback;
    }

    public void createUser(String userName, String userPassword, UserOperationCallback.OperateUserCallback callback) {

    }

    public void createMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }

    public MediaShare createMediaShareInMemory(boolean isAlbum, boolean isPublic, boolean otherMaintainer, String title, String desc, List<String> mediaKeys) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        List<MediaShareContent> mediaShareContents = new ArrayList<>();

        for (String mediaKey : mediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setKey(mediaKey);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);

        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageKey(mediaKeys.get(0));

        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        if (isPublic) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addViewer(userUUID);
            }
        } else mediaShare.clearViewers();

        if (otherMaintainer) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addMaintainer(userUUID);
            }
        } else {
            mediaShare.clearMaintainers();
            mediaShare.addMaintainer(FNAS.userUUID);
        }

        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(isAlbum);
        mediaShare.setLocal(true);
        mediaShare.setArchived(false);
        mediaShare.setDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));

        return mediaShare;

    }

    public void modifyMediaShare(String requestData, MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }

    public void deleteMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }

    private void loadMediaToGifTouchNetworkImageView(Context context, String remoteUrl, Media media, GifTouchNetworkImageView view) {
        view.setOrientationNumber(media.getOrientationNumber());

        view.setDefaultImageResId(R.drawable.placeholder_photo);

        view.setCurrentMedia(media);

        view.setTag(remoteUrl);

        if (media.getType().equalsIgnoreCase("gif")) {

            GifLoader loader = imageGifLoaderInstance.getGifLoader(context);
            loader.setShouldCache(!media.isLocal());

            view.setGifUrl(remoteUrl, loader);
        } else {

            ImageLoader loader = imageGifLoaderInstance.getImageLoader(context);
            loader.setShouldCache(!media.isLocal());

            view.setImageUrl(remoteUrl, loader);
        }
    }

    public void loadOriginalMediaToGifTouchNetworkImageView(Context context, Media media, GifTouchNetworkImageView view) {

        String remoteUrl = media.getImageOriginalUrl(context);


        loadMediaToGifTouchNetworkImageView(context, remoteUrl, media, view);
    }

    public void loadThumbMediaToGifTouchNetworkImageView(Context context, Media media, GifTouchNetworkImageView view) {

        String remoteUrl = media.getImageThumbUrl(context);

        loadMediaToGifTouchNetworkImageView(context, remoteUrl, media, view);
    }


    public void loadRemoteFileShare(FileShareOperationCallback.LoadFileShareCallback callback) {

    }

    public void loadRemoteFolderContent(String folderUUID, FileOperationCallback.LoadFileOperationCallback callback) {

    }

    public void loadDownloadedFiles(FileDownloadOperationCallback.LoadDownloadedFilesCallback callback) {

    }

    public void registerFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

    }

    public void unregisterFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

    }

    public void deleteDownloadedFileRecords(List<String> fileUUIDs, FileDownloadOperationCallback.DeleteDownloadedFilesCallback callback) {

    }

}
