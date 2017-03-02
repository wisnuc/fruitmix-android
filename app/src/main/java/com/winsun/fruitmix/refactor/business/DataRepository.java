package com.winsun.fruitmix.refactor.business;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFolder;
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
import com.winsun.fruitmix.refactor.business.callback.LoadDeviceIdOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.refactor.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.OperationCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
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
import com.winsun.fruitmix.refactor.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataRepository {

    private static DataRepository INSTANCE;

    public static final String TAG = DataRepository.class.getSimpleName();

    private DataSource mMemoryDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private Handler mHandler;

    private ExecutorServiceInstance instance;

    private ImageGifLoaderInstance imageGifLoaderInstance;

    private List<String> mediaKeysInCreateAlbum;

    private MediaFragmentDataLoader mMediaFragmentDataLoader;

    private boolean remoteMediaShareLoaded = false;
    private boolean remoteMediaLoaded = false;
    private boolean localMediaLoaded = false;
    private boolean remoteUserLoaded = false;

    private boolean mCalcNewLocalMediaDigestFinished = false;

    private boolean startTimingRetrieveMediaShare = false;

    private UserOperationCallback.LoadCurrentUserCallback mLoadCurrentUserCallback;

    private static final int RETRIEVE_REMOTE_MEDIA_SHARE = 0x1003;

    private TimingRetrieveMediaShareTask task;

    private List<MediaShareOperationCallback.LoadMediaSharesCallback> RetrieveMediaShareTimelyCallback;

    private FileDownloadOperationCallback.FileDownloadStateChangedCallback mFileDownloadStateChangedCallback;

    private DataRepository(DataSource memoryDataSource, DataSource dbDataSource, DataSource serverDataSource) {

        mMemoryDataSource = memoryDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

        mHandler = new Handler(Looper.getMainLooper());

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;

        imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;

        RetrieveMediaShareTimelyCallback = new ArrayList<>();

        task = new TimingRetrieveMediaShareTask(this);

        mediaKeysInCreateAlbum = new ArrayList<>();

    }

    public static DataRepository getInstance(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(cacheDataSource, dbDataSource, serverDataSource);
        }
        return INSTANCE;
    }

    public void insertMediaKeysInCreateAlbum(List<String> mediaKeysInCreateAlbum) {
        this.mediaKeysInCreateAlbum.addAll(mediaKeysInCreateAlbum);
    }

    public List<String> getMediaKeysInCreateAlbum() {
        return mediaKeysInCreateAlbum;
    }

    public void clearMediaKeysInCreateAlbum() {
        mediaKeysInCreateAlbum.clear();
    }

    public void shutdownFixedThreadPoolNow() {

        instance.shutdownFixedThreadPoolNow();
        instance = null;
    }

    public void registerTimeRetrieveMediaShareCallback(MediaShareOperationCallback.LoadMediaSharesCallback callback) {
        RetrieveMediaShareTimelyCallback.add(callback);
    }

    public void unregisterTimeRetrieveMediaShareCallback(MediaShareOperationCallback.LoadMediaSharesCallback callback) {
        RetrieveMediaShareTimelyCallback.remove(callback);
    }

    public void stopTimingRetrieveMediaShare() {

        Log.i(TAG, "stopTimingRetrieveMediaShare: ");

        if (startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = false;

        task.removeMessages(RETRIEVE_REMOTE_MEDIA_SHARE);

        task = null;
    }

    private void startTimingRetrieveMediaShare() {

        task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_MEDIA_SHARE, 20 * 1000);

        if (!startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = true;
    }

    private static class TimingRetrieveMediaShareTask extends Handler {

        WeakReference<DataRepository> weakReference = null;

        TimingRetrieveMediaShareTask(DataRepository dataRepository) {
            weakReference = new WeakReference<>(dataRepository);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RETRIEVE_REMOTE_MEDIA_SHARE:

                    DataRepository dataRepository = weakReference.get();

                    if (dataRepository.startTimingRetrieveMediaShare)
                        dataRepository.loadMediaShareTimelyInThread();

                    dataRepository.task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_MEDIA_SHARE, Util.refreshMediaShareDelayTime);

                    break;
            }
        }
    }

    private void loadMediaShareTimelyInThread() {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                loadMediaShareTimely();
            }
        });

    }

    private void loadMediaShareTimely() {

        //TODO:call in thread

        final List<MediaShare> mediaShares;

        List<String> oldMediaSharesDigests;
        List<String> newMediaSharesDigests;

        try {

            String token = mMemoryDataSource.loadToken(null).getToken();
            String url = generateUrl(Util.MEDIASHARE_PARAMETER);

            MediaSharesLoadOperationResult result = mServerDataSource.loadAllRemoteMediaShares(url, token);

            mediaShares = result.getMediaShares();

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share");

            newMediaSharesDigests = new ArrayList<>(mediaShares.size());
            for (MediaShare mediaShare : mediaShares) {
                newMediaSharesDigests.add(mediaShare.getShareDigest());
            }

            List<MediaShare> oldMediaShare = mDBDataSource.loadAllRemoteMediaShares(null, null).getMediaShares();

            oldMediaSharesDigests = new ArrayList<>(oldMediaShare.size());
            for (MediaShare mediaShare : oldMediaShare) {
                oldMediaSharesDigests.add(mediaShare.getShareDigest());
            }

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: generate oldMediaShares and newMediaShares");

            if (oldMediaSharesDigests.containsAll(newMediaSharesDigests) && newMediaSharesDigests.containsAll(oldMediaSharesDigests)) {

                Log.d(TAG, "handleActionRetrieveRemoteMediaShare: old media shares are same as newMediaShares");

                Util.refreshMediaShareDelayTime = Util.refreshMediaShareDelayTime * 2;

                if (Util.refreshMediaShareDelayTime > Util.MAX_REFRESH_MEDIA_SHARE_DELAY_TIME) {
                    Util.refreshMediaShareDelayTime = Util.MAX_REFRESH_MEDIA_SHARE_DELAY_TIME;
                }

                return;

            } else {
                Util.refreshMediaShareDelayTime = Util.DEFAULT_REFRESH_MEDIA_SHARE_DELAY_TIME;
            }

            mDBDataSource.deleteAllRemoteMediaShare();
            mMemoryDataSource.deleteAllRemoteMediaShare();

            mDBDataSource.insertRemoteMediaShares(mediaShares);
            mMemoryDataSource.insertRemoteMediaShares(mediaShares);

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    for (MediaShareOperationCallback.LoadMediaSharesCallback callback : RetrieveMediaShareTimelyCallback) {
                        if (callback != null)
                            callback.onLoadSucceed(new OperationSuccess(), mediaShares);
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadTokenDeviceIDAndGatewayInSplash(LoadTokenOperationCallback.LoadTokenCallback callback) {

        String token = mDBDataSource.loadToken(null).getToken();
        String gateway = mDBDataSource.loadGateway();
        String deviceID = mDBDataSource.loadDeviceID(null, null).getDeviceID();
        String loginUserUUID = mDBDataSource.loadLoginUserUUID();

        if (token == null) {
            callback.onLoadFail(null);
        } else {

            mMemoryDataSource.insertToken(token);
            mMemoryDataSource.insertGateway(gateway);
            mMemoryDataSource.insertDeviceID(deviceID);
            mMemoryDataSource.insertLoginUserUUID(loginUserUUID);

            callback.onLoadSucceed(null, token);

        }
    }


    public void loadRemoteTokenWhenLoginInThread(final LoadTokenParam param, final LoadTokenOperationCallback.LoadTokenCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                loadRemoteTokenWhenLogin(param, callback);
            }
        });

    }

    private void loadRemoteTokenWhenLogin(LoadTokenParam param, final LoadTokenOperationCallback.LoadTokenCallback callback) {

        //TODO:call in thread
        final TokenLoadOperationResult result = mServerDataSource.loadToken(param);

        if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            final String token = result.getToken();

            mDBDataSource.insertToken(token);
            mMemoryDataSource.insertToken(token);

            mDBDataSource.insertGateway(param.getGateway());
            mMemoryDataSource.insertGateway(param.getGateway());

            mDBDataSource.insertLoginUserUUID(param.getUserUUID());
            mMemoryDataSource.insertLoginUserUUID(param.getUserUUID());

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null)
                        callback.onLoadSucceed(result.getOperationResult(), token);
                }
            });

        } else {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null)
                        callback.onLoadFail(result.getOperationResult());
                }
            });

        }

    }


    public void loadRemoteDeviceIDInThread(final LoadDeviceIdOperationCallback.LoadDeviceIDCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                loadRemoteDeviceID(callback);
            }
        });

    }


    private void loadRemoteDeviceID(final LoadDeviceIdOperationCallback.LoadDeviceIDCallback callback) {

        final DeviceIDLoadOperationResult result = mDBDataSource.loadDeviceID(null, null);

        if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            final String deviceID = result.getDeviceID();

            mMemoryDataSource.insertDeviceID(deviceID);

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (callback != null)
                        callback.onLoadSucceed(result.getOperationResult(), deviceID);
                }
            });

        } else {

            String token = mMemoryDataSource.loadToken(null).getToken();

            String url = generateUrl(Util.DEVICE_ID_PARAMETER);

            final DeviceIDLoadOperationResult serverResult = mServerDataSource.loadDeviceID(url, token);

            if (serverResult.getOperationResult().getOperationResultType().equals(OperationResultType.SUCCEED)) {

                final String deviceID = serverResult.getDeviceID();

                mDBDataSource.insertDeviceID(deviceID);
                mMemoryDataSource.insertDeviceID(deviceID);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onLoadSucceed(serverResult.getOperationResult(), deviceID);
                    }
                });


            } else {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onLoadFail(serverResult.getOperationResult());
                    }
                });

            }

        }

    }


    public void loadLocalMediaInCameraInThread(final MediaOperationCallback.LoadMediasCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                loadLocalMediaInCamera(callback);
            }
        });

    }

    private void loadLocalMediaInCamera(final MediaOperationCallback.LoadMediasCallback callback) {

        Collection<String> localMediaUUIDs = mMemoryDataSource.loadLocalMediaUUIDs();

        //TODO:call in thread
        final MediasLoadOperationResult result = mDBDataSource.loadLocalMediaInCamera(localMediaUUIDs);

        List<Media> localMediaInCamera = result.getMedias();

        mMemoryDataSource.insertLocalMedias(localMediaInCamera);

        if (localMediaInCamera.size() > 0) {

            resetMediaFragmentDataLoader();

            final List<Media> medias = mMemoryDataSource.loadAllRemoteMedias(null, null).getMedias();
            medias.addAll(mMemoryDataSource.loadAllLocalMedias().getMedias());

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    //TODO:call in main thread
                    if (callback != null)
                        callback.onLoadSucceed(result.getOperationResult(), medias);
                }
            });


            //TODO:call in thread
            calcLocalMediaUUIDInCamera(localMediaInCamera);

        }

    }

    private void resetMediaFragmentDataLoader() {
        mMediaFragmentDataLoader = null;
    }

    private void calcLocalMediaUUIDInCamera(List<Media> medias) {

        //TODO:call in thread
        for (Media media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = Util.CalcSHA256OfFile(media.getThumb());
                media.setUuid(uuid);
            }
        }

        mDBDataSource.insertLocalMedias(medias);

        mCalcNewLocalMediaDigestFinished = true;
        startUploadMediaInThread();
    }

    private void startUploadMediaInThread() {

        //TODO:call in thread
        if (mCalcNewLocalMediaDigestFinished && remoteMediaLoaded) {

            instance.doOnTaskInFixedThreadPool(new Runnable() {
                @Override
                public void run() {
                    startUploadMedia();
                }
            });

        }

    }

    private void startUploadMedia(){
        List<Media> localMedias = mMemoryDataSource.loadAllLocalMedias().getMedias();

        Collection<String> remoteMediaUUIDs = mMemoryDataSource.loadRemoteMediaUUIDs();

        for (Media media : localMedias) {

            if (!media.isUploaded()) {

                if (remoteMediaUUIDs.contains(media.getUuid())) {
                    media.setUploaded(true);

                    mDBDataSource.updateLocalMedia(media);
                } else {

                    String token = mMemoryDataSource.loadToken(null).getToken();
                    String url = generateUrl(Util.DEVICE_ID_PARAMETER + "/" + mMemoryDataSource.loadDeviceID(null, null));

                    OperationResult result = mServerDataSource.insertLocalMedia(url, token, media);

                    if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                        media.setUploaded(true);

                        mDBDataSource.updateLocalMedia(media);

                    }

                }

            }

        }
    }


    public void loadMediasInThread(final MediaOperationCallback.LoadMediasCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {
                loadMedias(callback);
            }
        });

    }

    private void loadMedias(final MediaOperationCallback.LoadMediasCallback callback) {

        List<Media> remoteMedias;
        List<Media> localMedias;
        final Set<Media> allMedias = new HashSet<>();

        //TODO:call in thread
        if (localMediaLoaded) {
            localMedias = mMemoryDataSource.loadAllLocalMedias().getMedias();
        } else {

            localMedias = mDBDataSource.loadAllLocalMedias().getMedias();

            mMemoryDataSource.insertLocalMedias(localMedias);

            Collection<String> localMediaUUIDs = mMemoryDataSource.loadLocalMediaUUIDs();

            MediasLoadOperationResult result = mDBDataSource.loadLocalMediaInCamera(localMediaUUIDs);

            List<Media> localMediaInCamera = result.getMedias();
            if (localMediaInCamera.size() > 0) {
                mDBDataSource.insertLocalMedias(localMediaInCamera);

                mMemoryDataSource.insertLocalMedias(localMediaInCamera);
            }

            localMediaLoaded = true;
        }

        if (remoteMediaLoaded) {
            remoteMedias = mMemoryDataSource.loadAllRemoteMedias(null, null).getMedias();
        } else {

            String token = mMemoryDataSource.loadToken(null).getToken();
            String url = generateUrl(Util.MEDIA_PARAMETER);

            MediasLoadOperationResult result = mServerDataSource.loadAllRemoteMedias(url, token);

            OperationResult operationResult = result.getOperationResult();
            if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
                remoteMedias = result.getMedias();

                mDBDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.deleteAllRemoteMedia();

                mDBDataSource.insertRemoteMedias(remoteMedias);
                mMemoryDataSource.insertRemoteMedias(remoteMedias);

            } else {
                remoteMedias = mDBDataSource.loadAllRemoteMedias(null, null).getMedias();

                mMemoryDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.insertRemoteMedias(remoteMedias);
            }

            remoteMediaLoaded = true;
            startUploadMedia();
        }

        allMedias.addAll(remoteMedias);
        allMedias.addAll(localMedias);

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (callback != null)
                    callback.onLoadSucceed(new OperationSuccess(), new ArrayList<>(allMedias));

            }
        });

    }


    public Media loadMediaFromMemory(String mediaKey) {
        return mMemoryDataSource.loadMedia(mediaKey);
    }

    public User loadUserFromMemory(String userUUID) {
        return mMemoryDataSource.loadUser(userUUID);
    }

    public MediaShare loadMediaShareFromMemory(String mediaShareUUID) {
        return mMemoryDataSource.loadRemoteMediaShare(mediaShareUUID);
    }

    public void loadMediaInMediaShareFromMemory(final MediaShare mediaShare, final MediaOperationCallback.LoadMediasCallback callback) {

        //TODO:call in thread

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                List<String> mediaKeys = mediaShare.getMediaKeyInMediaShareContents();
                final List<Media> medias = new ArrayList<>(mediaKeys.size());

                for (String key : mediaKeys) {

                    Media media = mMemoryDataSource.loadMedia(key);
                    if (media == null) {
                        media = new Media();
                    }
                    medias.add(media);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onLoadSucceed(new OperationSuccess(), medias);
                    }
                });

            }
        });


    }

    public void handleMediasForMediaFragment(final Collection<Media> medias, final MediaOperationCallback.HandleMediaForMediaFragmentCallback callback) {

        if (mMediaFragmentDataLoader != null) {
            callback.onOperateFinished(mMediaFragmentDataLoader);
            return;
        }

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                if (mMediaFragmentDataLoader == null) {
                    mMediaFragmentDataLoader = new MediaFragmentDataLoader();
                }

                //TODO: handle medias in thread
                mMediaFragmentDataLoader.reloadData(medias);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onOperateFinished(mMediaFragmentDataLoader);

                    }
                });

            }
        });


    }

    public void loadMediaShares(final MediaShareOperationCallback.LoadMediaSharesCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final Collection<MediaShare> remoteMediaShares;

                //TODO: call in thread
                if (remoteMediaShareLoaded) {

                    remoteMediaShares = mMemoryDataSource.loadAllRemoteMediaShares(null, null).getMediaShares();

                } else {

                    String token = mMemoryDataSource.loadToken(null).getToken();
                    String url = generateUrl(Util.MEDIASHARE_PARAMETER);

                    MediaSharesLoadOperationResult result = mServerDataSource.loadAllRemoteMediaShares(url, token);
                    if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                        remoteMediaShares = result.getMediaShares();

                        mDBDataSource.deleteAllRemoteMediaShare();
                        mMemoryDataSource.deleteAllRemoteMediaShare();

                        mDBDataSource.insertRemoteMediaShares(remoteMediaShares);
                        mMemoryDataSource.insertRemoteMediaShares(remoteMediaShares);

                    } else {

                        remoteMediaShares = mDBDataSource.loadAllRemoteMediaShares(null, null).getMediaShares();

                        mMemoryDataSource.deleteAllRemoteMediaShare();

                        mMemoryDataSource.insertRemoteMediaShares(remoteMediaShares);

                    }

                    remoteMediaShareLoaded = true;

                }

                startTimingRetrieveMediaShare();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onLoadSucceed(new OperationSuccess(), remoteMediaShares);
                    }
                });


            }
        });

    }

    public boolean isMediaSharePublic(MediaShare mediaShare) {

        String loginUserUUID = mMemoryDataSource.loadLoginUserUUID();
        Collection<String> userUUIDs = mMemoryDataSource.loadAllUserUUID();

        return userUUIDs.size() == 1 || (loginUserUUID != null && mediaShare.getCreatorUUID().equals(loginUserUUID))
                || (mediaShare.getViewersListSize() != 0 && userUUIDs.contains(mediaShare.getCreatorUUID()));
    }

    public boolean checkPermissionToOperateMediaShare(MediaShare mediaShare) {

        String loginUserUUID = mMemoryDataSource.loadLoginUserUUID();

        return mediaShare.checkMaintainersListContainCurrentUserUUID() || mediaShare.getCreatorUUID().equals(loginUserUUID);
    }

    public boolean checkIsDownloaded(String fileUUID) {
        FileDownloadItem fileDownloadItem = mDBDataSource.loadDownloadFileRecord(fileUUID);

        return fileDownloadItem.getDownloadState() == DownloadState.FINISHED;
    }

    public boolean getShowAlbumTipsValue() {
        return mDBDataSource.getShowAlbumTipsValue();
    }

    public void saveShowAlbumTipsValue(boolean value) {
        mDBDataSource.saveShowAlbumTipsValue(value);
    }

    public boolean getShowPhotoReturnTipsValue() {

        return mDBDataSource.getShowPhotoReturnTipsValue();
    }

    public void setShowPhotoReturnTipsValue(boolean value) {

        mDBDataSource.saveShowPhotoReturnTipsValue(value);
    }

    public void loadEquipmentAlias(final String url, final LoadEquipmentAliasCallback callback) {

        final String token = mMemoryDataSource.loadToken(null).getToken();

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<EquipmentAlias> equipmentAliases = mServerDataSource.loadEquipmentAlias(token, url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback == null)
                            return;

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

        final String token = mMemoryDataSource.loadToken(null).getToken();

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<User> users = mServerDataSource.loadUserByLoginApi(token, url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback == null)
                            return;

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

    public void logout(final OperationCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                //TODO:call in thread
                mDBDataSource.deleteToken();
                mDBDataSource.deleteDeviceID();
                mDBDataSource.deleteAllRemoteMedia();
                mDBDataSource.deleteAllRemoteMediaShare();
                mDBDataSource.deleteAllRemoteUsers();

                mDBDataSource.updateLocalMediasUploadedFalse();

                mMemoryDataSource.deleteToken();
                mMemoryDataSource.deleteDeviceID();
                mMemoryDataSource.deleteAllRemoteUsers();
                mMemoryDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.deleteAllRemoteMediaShare();

                instance.shutdownFixedThreadPoolNow();

                stopTimingRetrieveMediaShare();

                Util.setRemoteMediaLoaded(false);
                Util.setRemoteMediaShareLoaded(false);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onOperationSucceed(new OperationSuccess());
                    }
                });

            }
        });


    }

    public User loadCurrentLoginUserFromMemory() {

        String userUUID = mMemoryDataSource.loadLoginUserUUID();

        return mMemoryDataSource.loadUser(userUUID);
    }


    public void loadUsers(final UserOperationCallback.LoadUsersCallback callback) {

        UsersLoadOperationResult result = mMemoryDataSource.loadUsers("", "", "");

        if (remoteUserLoaded) {
            callback.onLoadSucceed(result.getOperationResult(), result.getUsers());
        } else {

            instance.doOneTaskInCachedThread(new Runnable() {
                @Override
                public void run() {

                    String loadUserUrl = generateUrl(Util.USER_PARAMETER);
                    String loadOtherUserUrl = generateUrl(Util.LOGIN_PARAMETER);
                    String token = mMemoryDataSource.loadToken(null).getToken();

                    //TODO: need call in thread
                    UsersLoadOperationResult serverResult = mServerDataSource.loadUsers(loadUserUrl, loadOtherUserUrl, token);

                    final List<User> users;
                    final OperationResult operationResult = serverResult.getOperationResult();

                    if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {

                        users = serverResult.getUsers();

                        mDBDataSource.deleteAllRemoteUsers();
                        mMemoryDataSource.deleteAllRemoteUsers();

                        mDBDataSource.insertUsers(users);
                        mMemoryDataSource.insertUsers(users);


                    } else {

                        serverResult = mDBDataSource.loadUsers("", "", "");

                        users = serverResult.getUsers();

                        mMemoryDataSource.deleteAllRemoteUsers();

                        mMemoryDataSource.insertUsers(users);


                    }

                    remoteUserLoaded = true;

                    //TODO:need call in main thread

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (callback != null)
                                callback.onLoadSucceed(operationResult, users);

                            if (mLoadCurrentUserCallback != null)
                                mLoadCurrentUserCallback.onLoadSucceed(operationResult, users.get(0));
                        }
                    });

                }
            });

        }

    }

    public void loadCurrentLoginUser(UserOperationCallback.LoadCurrentUserCallback callback) {

        String userUUID = mMemoryDataSource.loadLoginUserUUID();

        User user = mMemoryDataSource.loadUser(userUUID);
        if (user != null) {
            callback.onLoadSucceed(new OperationSuccess(), user);
        } else {
            user = mDBDataSource.loadUser(userUUID);
            callback.onLoadSucceed(new OperationSuccess(), user);

            addCallbackWhenLoadUsersFinished(callback);

        }

    }

    private void addCallbackWhenLoadUsersFinished(UserOperationCallback.LoadCurrentUserCallback callback) {
        mLoadCurrentUserCallback = callback;
    }

    private String generateUrl(String req) {
        return mMemoryDataSource.loadGateway() + ":" + Util.PORT + req;
    }


    public void createUser(final String userName, final String userPassword, final UserOperationCallback.OperateUserCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                //TODO:call in thread
                final OperateUserResult operateUserResult = mServerDataSource.insertUser(generateUrl(Util.USER_PARAMETER),
                        mMemoryDataSource.loadToken(null).getToken(), userName, userPassword);

                if (operateUserResult.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                    final User user = operateUserResult.getUser();

                    mDBDataSource.insertUsers(Collections.singletonList(user));
                    mMemoryDataSource.insertUsers(Collections.singletonList(user));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateSucceed(operateUserResult.getOperationResult(), user);
                        }
                    });


                } else {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateFail(operateUserResult.getOperationResult());
                        }
                    });

                }

            }
        });

    }

    public void createMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

        //TODO:call in thread
        OperateMediaShareResult operateMediaShareResult = mServerDataSource.insertRemoteMediaShare(generateUrl(Util.MEDIASHARE_PARAMETER),
                mMemoryDataSource.loadToken(null).getToken(), mediaShare);

        if (operateMediaShareResult.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            MediaShare newMediaShare = operateMediaShareResult.getMediaShare();

            mDBDataSource.insertRemoteMediaShare(null, null, newMediaShare);
            mMemoryDataSource.insertRemoteMediaShare(null, null, newMediaShare);

            callback.onOperateSucceed(operateMediaShareResult.getOperationResult(), newMediaShare);

        } else {
            callback.onOperateFail(operateMediaShareResult.getOperationResult());
        }

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

    public void modifyMediaShare(String requestData, MediaShare modifiedMediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

        //TODO:call in thread
        OperationResult result = mServerDataSource.modifyRemoteMediaShare(generateUrl(Util.MEDIASHARE_PARAMETER + "/" + modifiedMediaShare.getUuid() + "/update"),
                mMemoryDataSource.loadToken(null).getToken(), requestData, modifiedMediaShare);

        if (result.getOperationResultType() == OperationResultType.SUCCEED) {

            mDBDataSource.modifyRemoteMediaShare(null, null, requestData, modifiedMediaShare);
            mMemoryDataSource.modifyRemoteMediaShare(null, null, requestData, modifiedMediaShare);

            callback.onOperateSucceed(result, modifiedMediaShare);

        } else {
            callback.onOperateFail(result);
        }

    }

    public void deleteMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

        //TODO:call in thread

        OperationResult result = mServerDataSource.deleteRemoteMediaShare(generateUrl(Util.MEDIASHARE_PARAMETER + "/" + mediaShare.getUuid()),
                mMemoryDataSource.loadToken(null).getToken(), mediaShare);

        if (result.getOperationResultType() == OperationResultType.SUCCEED) {

            mDBDataSource.deleteRemoteMediaShare(null, null, mediaShare);
            mMemoryDataSource.deleteRemoteMediaShare(null, null, mediaShare);

            callback.onOperateSucceed(result, mediaShare);

        } else {
            callback.onOperateFail(result);
        }

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

        String token = mMemoryDataSource.loadToken(null).getToken();
        String loadFileSharedWithMeUrl = generateUrl(Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_ME_PARAMETER);
        String loadFileShareWithOthersUrl = generateUrl(Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_OTHERS_PARAMETER);

        //TODO:call in thread

        FileSharesLoadOperationResult result = mServerDataSource.loadRemoteFileRootShares(loadFileSharedWithMeUrl, loadFileShareWithOthersUrl, token);

        if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            List<AbstractRemoteFile> files = result.getFiles();

            mMemoryDataSource.deleteAllRemoteFileShare();
            mMemoryDataSource.insertRemoteFileShare(files);

            callback.onLoadSucceed(result.getOperationResult(), files);

        } else {
            callback.onLoadFail(result.getOperationResult());
        }

    }

    public void loadRemoteFolderContent(String folderUUID, FileOperationCallback.LoadFileOperationCallback callback) {

        String token = mMemoryDataSource.loadToken(null).getToken();
        String url = generateUrl(Util.FILE_PARAMETER + "/" + folderUUID);

        //TODO:call in thread

        FilesLoadOperationResult result = mServerDataSource.loadRemoteFolder(url, token);

        if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            AbstractRemoteFile remoteFolder = new RemoteFolder();
            remoteFolder.setUuid(folderUUID);
            remoteFolder.initChildAbstractRemoteFileList(result.getFiles());

            mMemoryDataSource.deleteAllRemoteFiles();
            mMemoryDataSource.insertRemoteFiles(remoteFolder);

            callback.onLoadSucceed(result.getOperationResult(), result.getFiles());

        } else {
            callback.onLoadFail(result.getOperationResult());
        }

    }

    public void loadDownloadedFiles(FileDownloadOperationCallback.LoadDownloadedFilesCallback callback) {

        //TODO:call in thread

        FileDownloadLoadOperationResult result = mDBDataSource.loadDownloadedFilesRecord();

        callback.onLoaded(result.getFileDownloadItems());

    }

    public void registerFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

        mFileDownloadStateChangedCallback = callback;
    }

    public void unregisterFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

        mFileDownloadStateChangedCallback = null;

    }

    public void downloadFile(FileDownloadState fileDownloadState) {

        fileDownloadState.getFileDownloadItem().registerStateChangedCallback(new FileDownloadOperationCallback.FileDownloadStateChangedCallback() {
            @Override
            public void onStateChanged(DownloadState state) {

                //TODO:call in main thread

                if (mFileDownloadStateChangedCallback != null)
                    mFileDownloadStateChangedCallback.onStateChanged(state);

            }
        });

        String baseUrl = mMemoryDataSource.loadGateway() + ":" + Util.PORT;
        String token = mMemoryDataSource.loadToken(null).getToken();

        //TODO:call in thread

        OperationResult result = mServerDataSource.loadRemoteFile(baseUrl, token, fileDownloadState);

        if (result.getOperationResultType() == OperationResultType.SUCCEED) {

            FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

            fileDownloadItem.setFileTime(System.currentTimeMillis());
            mDBDataSource.insertDownloadedFileRecord(fileDownloadItem);

        }

    }

    public void deleteDownloadedFileRecords(List<String> fileUUIDs, FileDownloadOperationCallback.DeleteDownloadedFilesCallback callback) {

        //TODO:call in thread

        mDBDataSource.deleteDownloadedFileRecord(fileUUIDs);

        callback.onFinished();
    }

}
