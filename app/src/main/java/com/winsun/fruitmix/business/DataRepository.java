package com.winsun.fruitmix.business;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.callback.EquipmentDiscoveryCallback;
import com.winsun.fruitmix.business.callback.FileDownloadOperationCallback;
import com.winsun.fruitmix.business.callback.FileOperationCallback;
import com.winsun.fruitmix.business.callback.FileShareOperationCallback;
import com.winsun.fruitmix.business.callback.LoadDeviceIdOperationCallback;
import com.winsun.fruitmix.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.business.callback.OperationCallback;
import com.winsun.fruitmix.business.callback.UserOperationCallback;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
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
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFolder;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.EquipmentAlias;
import com.winsun.fruitmix.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataRepository {

    private static DataRepository INSTANCE;

    public static final String TAG = DataRepository.class.getSimpleName();

    private DataSource mMemoryDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private FileUtil mFileUtil;

    private Handler mHandler;

    private ExecutorServiceInstance instance;

    private ImageGifLoaderInstance imageGifLoaderInstance;

    private List<String> mediaKeysInCreateAlbum;

    private MediaFragmentDataLoader mMediaFragmentDataLoader;

    private boolean remoteMediaShareLoaded = false;
    private boolean remoteMediaLoaded = false;
    private boolean localMediaLoaded = false;
    private boolean remoteUserLoaded = false;

    private boolean mediaBeginLoad = false;
    private boolean mediaLoaded = false;
    private boolean mediaShareBeginLoad = false;
    private boolean mediaShareLoaded = false;

    private boolean mCalcNewLocalMediaDigestFinished = false;

    private boolean startTimingRetrieveMediaShare = false;

    private boolean mStopUpload = false;

    private boolean mStopGenerateMiniThumb = false;

    private UserOperationCallback.LoadCurrentUserCallback mLoadCurrentUserCallback;

    private List<MediaOperationCallback.LoadMediasCallback> mLoadMediasCallbacks;
    private List<MediaShareOperationCallback.LoadMediaSharesCallback> mLoadMediaShareCallbacks;

    private static final int RETRIEVE_REMOTE_MEDIA_SHARE = 0x1003;

    private TimingRetrieveMediaShareTask task;

    private List<MediaShareOperationCallback.LoadMediaSharesCallback> RetrieveMediaShareTimelyCallback;

    private FileDownloadOperationCallback.FileDownloadStateChangedCallback mFileDownloadStateChangedCallback;

    public static final String HANDLER_THREAD_NAME = "timing_retrieve_media_share_thread";

    private HandlerThread mHandlerThread;

    private Subscription mSubscription;

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    private DataRepository(Context context, DataSource memoryDataSource, DataSource dbDataSource, DataSource serverDataSource,FileUtil fileUtil) {

        mMemoryDataSource = memoryDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

        mFileUtil = fileUtil;

        mHandler = new Handler(Looper.getMainLooper());

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;

        imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;

        imageGifLoaderInstance.initRequestQueue(context);

        RetrieveMediaShareTimelyCallback = new ArrayList<>();
        mediaKeysInCreateAlbum = new ArrayList<>();
        mLoadMediasCallbacks = new ArrayList<>();
        mLoadMediaShareCallbacks = new ArrayList<>();

    }

    public static DataRepository getInstance(Context context, DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource,FileUtil fileUtil) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(context, cacheDataSource, dbDataSource, serverDataSource,fileUtil);
        }
        return INSTANCE;
    }

    public void init() {

        instance.startUploadThreadPool();
        instance.startGenerateMiniThumbThreadPool();

        mDBDataSource.init();
        mMemoryDataSource.init();
        mServerDataSource.init();

        mMediaFragmentDataLoader = null;

        remoteMediaShareLoaded = false;
        remoteMediaLoaded = false;
        localMediaLoaded = false;
        remoteUserLoaded = false;
        mediaBeginLoad = false;
        mediaLoaded = false;
        mediaShareBeginLoad = false;
        mediaShareLoaded = false;

        mCalcNewLocalMediaDigestFinished = false;
        startTimingRetrieveMediaShare = false;

        mLoadCurrentUserCallback = null;
        RetrieveMediaShareTimelyCallback.clear();
        mediaKeysInCreateAlbum.clear();
        mLoadMediasCallbacks.clear();
        mLoadMediaShareCallbacks.clear();

        task = null;
        mHandlerThread = null;
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

    public void registerTimeRetrieveMediaShareCallback(MediaShareOperationCallback.LoadMediaSharesCallback callback) {
        RetrieveMediaShareTimelyCallback.add(callback);
    }

    public void unregisterTimeRetrieveMediaShareCallback(MediaShareOperationCallback.LoadMediaSharesCallback callback) {
        RetrieveMediaShareTimelyCallback.remove(callback);
    }

    public void stopTimingRetrieveMediaShare() {

        Log.d(TAG, "stopTimingRetrieveMediaShare: ");

        if (startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = false;

        if (task != null) {
            task.removeMessages(RETRIEVE_REMOTE_MEDIA_SHARE);
            task = null;
        }

        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }

    }

    public void pauseTimingRetrieveMediaShare() {
        if (startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = false;
    }

    public void resumeTimingRetrieveMediaShare() {
        if (!startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = true;
    }

    private void startTimingRetrieveMediaShare() {

        mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        task = new TimingRetrieveMediaShareTask(this, mHandlerThread.getLooper());

        task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_MEDIA_SHARE, 20 * 1000);

        if (!startTimingRetrieveMediaShare)
            startTimingRetrieveMediaShare = true;
    }

    private static class TimingRetrieveMediaShareTask extends Handler {

        WeakReference<DataRepository> weakReference = null;

        TimingRetrieveMediaShareTask(DataRepository dataRepository, Looper looper) {
            super(looper);

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

                    if (dataRepository.task != null)
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

        final List<MediaShare> mediaShares;

        List<String> oldMediaSharesDigests;
        List<String> newMediaSharesDigests;

        try {

            MediaSharesLoadOperationResult result = mServerDataSource.loadAllRemoteMediaShares();

            if (result.getOperationResult().getOperationResultType() != OperationResultType.SUCCEED)
                return;

            mediaShares = result.getMediaShares();

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share");

            newMediaSharesDigests = new ArrayList<>(mediaShares.size());
            for (MediaShare mediaShare : mediaShares) {
                newMediaSharesDigests.add(mediaShare.getShareDigest());
            }

            List<MediaShare> oldMediaShare = mDBDataSource.loadAllRemoteMediaShares().getMediaShares();

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

        String token = mDBDataSource.loadToken();
        String gateway = mDBDataSource.loadGateway();
        String deviceID = mDBDataSource.loadDeviceID().getDeviceID();
        String loginUserUUID = mDBDataSource.loadLoginUserUUID();

        if (token == null) {
            callback.onLoadFail(null);
        } else {

            mServerDataSource.insertToken(token);
            mServerDataSource.insertGateway(gateway);
            mServerDataSource.insertDeviceID(deviceID);

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

        final DeviceIDLoadOperationResult result = mDBDataSource.loadDeviceID();

        if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

            final String deviceID = result.getDeviceID();

            mMemoryDataSource.insertDeviceID(deviceID);

            mServerDataSource.insertDeviceID(deviceID);

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (callback != null)
                        callback.onLoadSucceed(result.getOperationResult(), deviceID);
                }
            });

        } else {

            final DeviceIDLoadOperationResult serverResult = mServerDataSource.loadDeviceID();

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

        Collection<String> localMediaThumbs = mMemoryDataSource.loadLocalMediaThumbs();

        final MediasLoadOperationResult result = mDBDataSource.loadLocalMediaInCamera(localMediaThumbs);

        List<Media> localMediaInCamera = result.getMedias();

        if (localMediaInCamera.size() > 0) {


            mMemoryDataSource.insertLocalMedias(localMediaInCamera);

            resetMediaFragmentDataLoader();

            final List<Media> medias = mMemoryDataSource.loadAllRemoteMedias().getMedias();
            medias.addAll(mMemoryDataSource.loadAllLocalMedias().getMedias());

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (callback != null)
                        callback.onLoadSucceed(result.getOperationResult(), medias);
                }
            });


            calcLocalMediaUUIDInCamera(localMediaInCamera);

        } else {
            mCalcNewLocalMediaDigestFinished = true;
        }

        startGenerateLocalPhotoThumbnail();
        startUploadMediaInThread();

    }

    private void resetMediaFragmentDataLoader() {
        mMediaFragmentDataLoader = null;
    }

    private void calcLocalMediaUUIDInCamera(List<Media> medias) {

        Log.d(TAG, "calcLocalMediaUUIDInCamera: ");

        for (Media media : medias) {
            if (media.getUuid().isEmpty()) {
                String uuid = Util.CalcSHA256OfFile(media.getThumb());
                media.setUuid(uuid);
            }
        }

        mDBDataSource.insertLocalMedias(medias);

        mCalcNewLocalMediaDigestFinished = true;

    }

    private void startGenerateLocalPhotoThumbnail() {

        Log.d(TAG, "startGenerateLocalPhotoThumbnail: ");

        List<Media> medias = mMemoryDataSource.loadAllLocalMedias().getMedias();

        for (final Media media : medias) {

            if (media.getMiniThumb().isEmpty()) {

                instance.doOnTaskInGenerateMiniThumbThreadPool(new Runnable() {
                    @Override
                    public void run() {

                        if (mStopGenerateMiniThumb) return;

                        boolean result = mFileUtil.writeBitmapToLocalPhotoThumbnailFolder(media);

                        if (result && !mStopGenerateMiniThumb) {

                            mDBDataSource.updateLocalMediaMiniThumb(media);

                        }
                    }
                });
            }

        }

    }

    public void startUploadMediaInThread() {

        Log.d(TAG, "startUploadMediaInThread: calc :" + mCalcNewLocalMediaDigestFinished + " remote media loaded: " + remoteMediaLoaded + " getAutoUploadOrNot: " + getAutoUploadOrNot());

        if (mCalcNewLocalMediaDigestFinished && remoteMediaLoaded && getAutoUploadOrNot()) {

            Log.d(TAG, "startUploadMedia");

            mStopUpload = false;

            List<Media> localMedias = mMemoryDataSource.loadAllLocalMedias().getMedias();

            final Collection<String> remoteMediaUUIDs = mMemoryDataSource.loadRemoteMediaUUIDs();

            final String deviceID = mMemoryDataSource.loadDeviceID().getDeviceID();

            for (final Media media : localMedias) {

                instance.doOnTaskInUploadThreadPool(new Runnable() {
                    @Override
                    public void run() {
                        startUploadMedia(media, remoteMediaUUIDs, deviceID);
                    }
                });

            }

        }

    }

    private void startUploadMedia(Media media, Collection<String> remoteMediaUUIDs, String deviceID) {

        if (mStopUpload) return;

        boolean uploaded = false;

        if (!media.getUploadedDeviceIDs().contains(deviceID)) {

            if (remoteMediaUUIDs.contains(media.getUuid())) {

                uploaded = true;

            } else {

                OperationResult result = mServerDataSource.insertLocalMedia(media);

                if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                    uploaded = true;

                }

            }

            if (!uploaded) return;

            if (media.getUploadedDeviceIDs().isEmpty()) {
                media.setUploadedDeviceIDs(deviceID);
            } else {
                media.setUploadedDeviceIDs(media.getUploadedDeviceIDs() + "," + deviceID);
            }
            mDBDataSource.updateLocalMediaUploadedDeviceID(media);
        }

    }

    public void stopUpload() {

        mStopUpload = true;

        instance.shutdownUploadMediaThreadPoolNow();
    }

    public void stopGenerateMiniThumb() {

        mStopGenerateMiniThumb = true;

        instance.shutdownGenerateMiniThumbThreadPoolNow();

    }

    public void loadMediasInThread(final MediaOperationCallback.LoadMediasCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                if (!mediaBeginLoad) {

                    mediaBeginLoad = true;

                    loadMedias(callback);

                } else if (!mediaLoaded) {

                    mLoadMediasCallbacks.add(callback);

                } else {
                    loadMedias(callback);
                }
            }
        });

    }

    private void loadMedias(final MediaOperationCallback.LoadMediasCallback callback) {

        Log.d(TAG, "loadMedias: start load medias");

        List<Media> remoteMedias;
        List<Media> localMedias;
        final List<Media> allMedias = new ArrayList<>();

        List<Media> localMediaInCamera = null;

        if (localMediaLoaded) {
            localMedias = mMemoryDataSource.loadAllLocalMedias().getMedias();
        } else {

            localMedias = mDBDataSource.loadAllLocalMedias().getMedias();

            mMemoryDataSource.insertLocalMedias(localMedias);

            Collection<String> localMediaThumbs = mMemoryDataSource.loadLocalMediaThumbs();

            MediasLoadOperationResult result = mDBDataSource.loadLocalMediaInCamera(localMediaThumbs);

            localMediaInCamera = result.getMedias();
            if (localMediaInCamera.size() > 0) {

                mMemoryDataSource.insertLocalMedias(localMediaInCamera);

                localMedias.addAll(localMediaInCamera);
            }

            localMediaLoaded = true;
        }

        Log.d(TAG, "loadMedias: finish load local medias");

        if (remoteMediaLoaded) {
            remoteMedias = mMemoryDataSource.loadAllRemoteMedias().getMedias();
        } else {

            MediasLoadOperationResult result = mServerDataSource.loadAllRemoteMedias();

            OperationResult operationResult = result.getOperationResult();
            if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
                remoteMedias = result.getMedias();

                mDBDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.deleteAllRemoteMedia();

                mDBDataSource.insertRemoteMedias(remoteMedias);
                mMemoryDataSource.insertRemoteMedias(remoteMedias);

            } else {
                remoteMedias = mDBDataSource.loadAllRemoteMedias().getMedias();

                mMemoryDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.insertRemoteMedias(remoteMedias);
            }

            remoteMediaLoaded = true;
            startUploadMediaInThread();

            // call this after media loaded
            if (remoteMediaShareLoaded)
                loadMediaSharesInThread(null);
        }

        Log.d(TAG, "loadMedias: finish load remote medias");

        Map<String, Media> remoteMediaMap = new HashMap<>(remoteMedias.size());

        for (Media media : remoteMedias) {
            remoteMediaMap.put(media.getUuid(), media);
        }

        for (Media media : localMedias) {
            remoteMediaMap.remove(media.getUuid());
        }

        Log.d(TAG, "loadMedias: finish handle remote media map");

        allMedias.addAll(remoteMediaMap.values());
        allMedias.addAll(localMedias);

        Log.d(TAG, "loadMedias: add all medias");

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "loaded media and call callback");

                OperationResult result = new OperationSuccess();
                List<Media> medias = new ArrayList<>(allMedias);

                if (callback != null)
                    callback.onLoadSucceed(result, medias);

                for (MediaOperationCallback.LoadMediasCallback loadMediasCallback : mLoadMediasCallbacks) {
                    if (loadMediasCallback != null)
                        loadMediasCallback.onLoadSucceed(result, medias);
                }

            }
        });

        mediaLoaded = true;

        if (localMediaInCamera != null && localMediaInCamera.size() > 0) {
            calcLocalMediaUUIDInCamera(localMediaInCamera);
        } else {
            mCalcNewLocalMediaDigestFinished = true;
        }

        startGenerateLocalPhotoThumbnail();
        startUploadMediaInThread();

    }


    public Media loadMediaFromMemory(String mediaKey) {
        return mMemoryDataSource.loadMedia(mediaKey);
    }

    public User loadUserFromMemory(String userUUID) {
        return mMemoryDataSource.loadRemoteUser(userUUID);
    }

    public MediaShare loadMediaShareFromMemory(String mediaShareUUID) {
        return mMemoryDataSource.loadRemoteMediaShare(mediaShareUUID);
    }

    public void loadMediaInMediaShareFromMemory(final MediaShare mediaShare, final MediaOperationCallback.LoadMediasCallback callback) {

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

    public void loadMediaSharesInThread(final MediaShareOperationCallback.LoadMediaSharesCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                if (!mediaShareBeginLoad) {

                    mediaShareBeginLoad = true;
                    loadMediaShares(callback);

                } else if (!remoteMediaLoaded || !mediaShareLoaded) {

                    mLoadMediaShareCallbacks.add(callback);

                } else {

                    loadMediaShares(callback);

                }

            }
        });

    }

    private void loadMediaShares(final MediaShareOperationCallback.LoadMediaSharesCallback callback) {
        final Collection<MediaShare> remoteMediaShares;

        if (remoteMediaShareLoaded) {

            remoteMediaShares = mMemoryDataSource.loadAllRemoteMediaShares().getMediaShares();

        } else {

            MediaSharesLoadOperationResult result = mServerDataSource.loadAllRemoteMediaShares();
            if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                remoteMediaShares = result.getMediaShares();

                mDBDataSource.deleteAllRemoteMediaShare();
                mMemoryDataSource.deleteAllRemoteMediaShare();

                mDBDataSource.insertRemoteMediaShares(remoteMediaShares);
                mMemoryDataSource.insertRemoteMediaShares(remoteMediaShares);

            } else {

                remoteMediaShares = mDBDataSource.loadAllRemoteMediaShares().getMediaShares();

                mMemoryDataSource.deleteAllRemoteMediaShare();

                mMemoryDataSource.insertRemoteMediaShares(remoteMediaShares);

            }

            remoteMediaShareLoaded = true;

        }

        startTimingRetrieveMediaShare();

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "loaded media share and call callback");

                if (!remoteMediaLoaded)
                    return;

                OperationResult result = new OperationSuccess();

                if (callback != null)
                    callback.onLoadSucceed(result, remoteMediaShares);

                for (MediaShareOperationCallback.LoadMediaSharesCallback loadMediaSharesCallback : mLoadMediaShareCallbacks) {

                    if (loadMediaSharesCallback != null)
                        loadMediaSharesCallback.onLoadSucceed(result, remoteMediaShares);
                }
            }
        });

        mediaShareLoaded = true;
    }

    public boolean isMediaSharePublic(MediaShare mediaShare) {

        String loginUserUUID = mMemoryDataSource.loadLoginUserUUID();
        Collection<String> userUUIDs = mMemoryDataSource.loadAllRemoteUserUUID();

        return (userUUIDs.size() == 1 && loginUserUUID != null && mediaShare.getCreatorUUID().equals(loginUserUUID))
                || (mediaShare.getViewersListSize() != 0 && userUUIDs.contains(mediaShare.getCreatorUUID()));
    }

    public boolean checkPermissionToOperateMediaShare(MediaShare mediaShare) {

        String loginUserUUID = mMemoryDataSource.loadLoginUserUUID();

        return mediaShare.getMaintainers().contains(loginUserUUID) || mediaShare.getCreatorUUID().equals(loginUserUUID);
    }

    public boolean checkIsDownloaded(String fileUUID) {

        return FileDownloadManager.INSTANCE.checkIsDownloaded(fileUUID);
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

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<EquipmentAlias> equipmentAliases = mServerDataSource.loadEquipmentAlias(url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onLoadSucceed(null, equipmentAliases);

                    }
                });
            }
        });


    }

    public void loadUserByLoginApi(final String url, final UserOperationCallback.LoadUsersCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<User> users = mServerDataSource.loadRemoteUserByLoginApi(url);

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

                mDBDataSource.deleteToken();
                mDBDataSource.deleteDeviceID();
                mDBDataSource.deleteAllRemoteMedia();
                mDBDataSource.deleteAllRemoteMediaShare();
                mDBDataSource.deleteAllRemoteUsers();

                mMemoryDataSource.deleteToken();
                mMemoryDataSource.deleteDeviceID();
                mMemoryDataSource.deleteAllRemoteUsers();
                mMemoryDataSource.deleteAllRemoteMedia();
                mMemoryDataSource.deleteAllRemoteMediaShare();

                stopUpload();

                stopTimingRetrieveMediaShare();

                init();

                insertLoggedInUserToMemory(loadLoggedInUserInDB());

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

    public String loadCurrentLoginUserUUIDInMemory() {
        return mMemoryDataSource.loadLoginUserUUID();
    }

    public User loadCurrentLoginUserInMemory() {

        String userUUID = mMemoryDataSource.loadLoginUserUUID();

        return mMemoryDataSource.loadRemoteUser(userUUID);
    }

    public Collection<User> loadUsersInMemory() {

        return mMemoryDataSource.loadRemoteUsers().getUsers();

    }


    public void loadUsersInThread(final UserOperationCallback.LoadUsersCallback callback) {

        UsersLoadOperationResult result = mMemoryDataSource.loadRemoteUsers();

        if (remoteUserLoaded) {

            if (callback != null)
                callback.onLoadSucceed(result.getOperationResult(), result.getUsers());

        } else {

            instance.doOneTaskInCachedThread(new Runnable() {
                @Override
                public void run() {

                    loadUsers(callback);

                }
            });

        }

    }

    private void loadUsers(final UserOperationCallback.LoadUsersCallback callback) {

        UsersLoadOperationResult serverResult = mServerDataSource.loadRemoteUsers();

        final List<User> users;
        final OperationResult operationResult = serverResult.getOperationResult();

        if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {

            users = serverResult.getUsers();

            mDBDataSource.deleteAllRemoteUsers();
            mMemoryDataSource.deleteAllRemoteUsers();

            mDBDataSource.insertRemoteUsers(users);
            mMemoryDataSource.insertRemoteUsers(users);


        } else {

            serverResult = mDBDataSource.loadRemoteUsers();

            users = serverResult.getUsers();

            mMemoryDataSource.deleteAllRemoteUsers();

            mMemoryDataSource.insertRemoteUsers(users);


        }

        remoteUserLoaded = true;

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

    public void loadCurrentLoginUser(UserOperationCallback.LoadCurrentUserCallback callback) {

        String userUUID = mMemoryDataSource.loadLoginUserUUID();

        User user = mMemoryDataSource.loadRemoteUser(userUUID);
        if (user != null) {
            callback.onLoadSucceed(new OperationSuccess(), user);
        } else {
            user = mDBDataSource.loadRemoteUser(userUUID);

            if (user == null)
                addCallbackWhenLoadUsersFinished(callback);
            else
                callback.onLoadSucceed(new OperationSuccess(), user);

        }

    }

    private void addCallbackWhenLoadUsersFinished(UserOperationCallback.LoadCurrentUserCallback callback) {
        mLoadCurrentUserCallback = callback;
    }


    public void createUser(final String userName, final String userPassword, final UserOperationCallback.OperateUserCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final OperateUserResult operateUserResult = mServerDataSource.insertRemoteUser(userName, userPassword);

                if (operateUserResult.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                    final User user = operateUserResult.getUser();

                    mDBDataSource.insertRemoteUsers(Collections.singletonList(user));
                    mMemoryDataSource.insertRemoteUsers(Collections.singletonList(user));

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

    public void createMediaShare(final MediaShare mediaShare, final MediaShareOperationCallback.OperateMediaShareCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final OperateMediaShareResult operateMediaShareResult = mServerDataSource.insertRemoteMediaShare(mediaShare);

                if (operateMediaShareResult.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                    final MediaShare newMediaShare = operateMediaShareResult.getMediaShare();

                    mDBDataSource.insertRemoteMediaShare(newMediaShare);
                    mMemoryDataSource.insertRemoteMediaShare(newMediaShare);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateSucceed(operateMediaShareResult.getOperationResult(), newMediaShare);
                        }
                    });


                } else {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateFail(operateMediaShareResult.getOperationResult());
                        }
                    });

                }

            }
        });


    }

    public Collection<String> loadAllUserUUIDInMemory() {
        return mMemoryDataSource.loadAllRemoteUserUUID();
    }

    public MediaShare createMediaShareInMemory(boolean isAlbum, boolean isPublic, boolean otherMaintainer, String title, String desc, List<String> mediaKeys) {

        String currentUserUUID = mMemoryDataSource.loadLoginUserUUID();

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        List<MediaShareContent> mediaShareContents = new ArrayList<>();

        for (String mediaKey : mediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setMediaUUID(mediaKey);
            mediaShareContent.setAuthor(currentUserUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);

        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageUUID(mediaKeys.get(0));

        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        Collection<String> userUUIDs = mMemoryDataSource.loadAllRemoteUserUUID();

        if (isPublic) {
            for (String userUUID : userUUIDs) {
                mediaShare.addViewer(userUUID);
            }
        } else mediaShare.clearViewers();

        if (otherMaintainer) {
            for (String userUUID : userUUIDs) {
                mediaShare.addMaintainer(userUUID);
            }
        } else {
            mediaShare.clearMaintainers();
            mediaShare.addMaintainer(currentUserUUID);
        }

        mediaShare.setCreatorUUID(currentUserUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(isAlbum);
        mediaShare.setLocal(true);
        mediaShare.setArchived(false);
        mediaShare.setDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));

        return mediaShare;

    }

    private String createStringOperateContentsInMediaShare(MediaShare mediaShare, String op) {

        String returnValue;

        List<MediaShareContent> mediaShareContents = mediaShare.getMediaShareContents();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"op\":\"");
        stringBuilder.append(op);
        stringBuilder.append("\",\"path\":\"");
        stringBuilder.append("contents");
        stringBuilder.append("\",\"value\":[");
        for (MediaShareContent value : mediaShareContents) {
            stringBuilder.append("\"");

            String key = value.getMediaUUID();
            if (key.contains("/")) {

                Media media = mMemoryDataSource.loadLocalMediaByThumb(key);
                key = media.getUuid();
                if (key.isEmpty()) {
                    key = Util.CalcSHA256OfFile(key);
                }

            }

            stringBuilder.append(key);
            stringBuilder.append("\",");
        }

        if (mediaShareContents.size() > 0) {
            returnValue = stringBuilder.substring(0, stringBuilder.length() - 1);
        } else {
            returnValue = stringBuilder.toString();
        }

        returnValue += "]}";

        return returnValue;
    }

    public void modifyMediaInMediaShare(final MediaShare diffContentsOriginalMediaShare, final MediaShare diffContentsModifiedMediaShare, final MediaShare modifiedMediaShare,
                                        final MediaShareOperationCallback.OperateMediaShareCallback callback) {

        String requestData = "[";
        if (diffContentsOriginalMediaShare.getMediaShareContentsListSize() != 0) {
            requestData += createStringOperateContentsInMediaShare(diffContentsOriginalMediaShare, Util.DELETE);
        }
        if (diffContentsModifiedMediaShare.getMediaShareContentsListSize() != 0) {
            requestData += createStringOperateContentsInMediaShare(diffContentsModifiedMediaShare, Util.ADD);
        }
        requestData += "]";

        final String request = requestData;

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final OperationResult result = mServerDataSource.modifyMediaInRemoteMediaShare(request, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);

                if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                    mDBDataSource.modifyMediaInRemoteMediaShare(request, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);
                    mMemoryDataSource.modifyMediaInRemoteMediaShare(request, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateSucceed(result, modifiedMediaShare);
                        }
                    });

                } else {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateFail(result);
                        }
                    });

                }

            }
        });


    }


    public void modifyMediaShare(final String requestData, final MediaShare modifiedMediaShare, final MediaShareOperationCallback.OperateMediaShareCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final OperationResult result = mServerDataSource.modifyRemoteMediaShare(requestData, modifiedMediaShare);

                if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                    mDBDataSource.modifyRemoteMediaShare(requestData, modifiedMediaShare);
                    mMemoryDataSource.modifyRemoteMediaShare(requestData, modifiedMediaShare);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateSucceed(result, modifiedMediaShare);
                        }
                    });

                } else {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateFail(result);
                        }
                    });

                }

            }
        });


    }

    public void deleteMediaShare(final MediaShare mediaShare, final MediaShareOperationCallback.OperateMediaShareCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final OperationResult result = mServerDataSource.deleteRemoteMediaShare(mediaShare);

                if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                    mDBDataSource.deleteRemoteMediaShare(mediaShare);
                    mMemoryDataSource.deleteRemoteMediaShare(mediaShare);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (callback != null)
                                callback.onOperateSucceed(result, mediaShare);
                        }
                    });

                } else {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onOperateFail(result);
                        }
                    });

                }

            }
        });

    }

    public void loadRemoteFileShare(final FileShareOperationCallback.LoadFileShareCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final FileSharesLoadOperationResult result = mServerDataSource.loadRemoteFileRootShares();

                if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                    final List<AbstractRemoteFile> files = result.getFiles();

                    mMemoryDataSource.deleteAllRemoteFileShare();
                    mMemoryDataSource.insertRemoteFileShare(files);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (callback != null)
                                callback.onLoadSucceed(result.getOperationResult(), files);
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
        });

    }

    public void loadRemoteFolderContent(final String folderUUID, final FileOperationCallback.LoadFileOperationCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final FilesLoadOperationResult result = mServerDataSource.loadRemoteFolder(folderUUID);

                if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                    AbstractRemoteFile remoteFolder = new RemoteFolder();
                    remoteFolder.setUuid(folderUUID);
                    remoteFolder.initChildAbstractRemoteFileList(result.getFiles());

                    mMemoryDataSource.deleteAllRemoteFiles();
                    mMemoryDataSource.insertRemoteFiles(remoteFolder);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onLoadSucceed(result.getOperationResult(), result.getFiles());
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
        });

    }

    public void loadDownloadedFiles(final FileDownloadOperationCallback.LoadDownloadedFilesCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                String currentUserUUID = mMemoryDataSource.loadLoginUserUUID();

                final FileDownloadLoadOperationResult result = mDBDataSource.loadDownloadedFilesRecord(currentUserUUID);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onLoaded(result.getFileDownloadItems());
                    }
                });

            }
        });

    }

    public void registerFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

        mFileDownloadStateChangedCallback = callback;
    }

    public void unregisterFileDownloadedStateChanged(FileDownloadOperationCallback.FileDownloadStateChangedCallback callback) {

        mFileDownloadStateChangedCallback = null;

    }

    public void downloadFile(AbstractRemoteFile file) {

        final FileDownloadItem fileDownloadItem = new FileDownloadItem(file.getName(), Long.parseLong(file.getSize()), file.getUuid());

        fileDownloadItem.registerStateChangedCallback(new FileDownloadOperationCallback.FileDownloadStateChangedCallback() {
            @Override
            public void onStateChanged(final DownloadState state) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (mFileDownloadStateChangedCallback != null)
                            mFileDownloadStateChangedCallback.onStateChanged(state);

                    }
                });

            }
        });

        fileDownloadItem.registerStartDownloadFileCallback(new FileDownloadOperationCallback.StartDownloadFileCallback() {
            @Override
            public void start() {
                startDownloadFileInThread(fileDownloadItem);
            }
        });

        FileDownloadManager.INSTANCE.addFileDownloadItem(fileDownloadItem);

    }

    private void startDownloadFileInThread(final FileDownloadItem fileDownloadItem) {
        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                FileDownloadState fileDownloadState = fileDownloadItem.getFileDownloadState();

                OperationResult result = mServerDataSource.downloadRemoteFile(fileDownloadState);

                if (result.getOperationResultType() == OperationResultType.SUCCEED) {

                    FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

                    fileDownloadItem.setFileTime(System.currentTimeMillis());
                    mDBDataSource.insertDownloadedFileRecord(fileDownloadItem);

                }

            }
        });
    }

    public void deleteDownloadedFileRecords(final List<String> fileUUIDs, final FileDownloadOperationCallback.DeleteDownloadedFilesCallback callback) {

        final List<String> mFileUUIDs = new ArrayList<>(fileUUIDs);

        final String userUUID = mMemoryDataSource.loadLoginUserUUID();

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                mDBDataSource.deleteDownloadedFileRecord(mFileUUIDs, userUUID);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (callback != null)
                            callback.onFinished();

                    }
                });

            }
        });

    }

    private void loadMediaToGifTouchNetworkImageView(String remoteUrl, Media media, GifTouchNetworkImageView view) {
        view.setOrientationNumber(media.getOrientationNumber());

        view.setDefaultImageResId(R.drawable.placeholder_photo);

        view.setCurrentMedia(media);

        view.setTag(remoteUrl);

        String token = mMemoryDataSource.loadToken();

        if (media.getType().equalsIgnoreCase("gif")) {

            GifLoader loader = imageGifLoaderInstance.getGifLoader(token);
            loader.setShouldCache(!media.isLocal());

            view.setGifUrl(remoteUrl, loader);
        } else {

            ImageLoader loader = imageGifLoaderInstance.getImageLoader(token);
            loader.setShouldCache(!media.isLocal());

            view.setImageUrl(remoteUrl, loader);
        }
    }

    public void loadOriginalMediaToGifTouchNetworkImageView(Media media, GifTouchNetworkImageView view) {

        String remoteUrl = loadImageOriginalUrl(media);

        loadMediaToGifTouchNetworkImageView(remoteUrl, media, view);
    }

    public void loadThumbMediaToGifTouchNetworkImageView(Media media, GifTouchNetworkImageView view) {

        String remoteUrl = loadImageThumbUrl(media);

        loadMediaToGifTouchNetworkImageView(remoteUrl, media, view);
    }

    private void loadMediaToNetworkImageView(boolean needSetOrientationNumber, String remoteUrl, Media media, NetworkImageView view) {

        String token = mMemoryDataSource.loadToken();
        ImageLoader loader = imageGifLoaderInstance.getImageLoader(token);

        if (media != null) {

            loader.setShouldCache(!media.isLocal());

            if (needSetOrientationNumber)
                view.setOrientationNumber(media.getOrientationNumber());

            view.setTag(remoteUrl);
            view.setDefaultImageResId(R.drawable.placeholder_photo);
            view.setImageUrl(remoteUrl, loader);


        } else {

            view.setDefaultImageResId(R.drawable.placeholder_photo);
            view.setImageUrl(null, loader);
        }

    }

    public void loadOriginalMediaToNetworkImageView(Media media, NetworkImageView view) {

        if (media == null) {
            view.setImageUrl(null, null);
            return;
        }

        String remoteUrl = loadImageOriginalUrl(media);

        loadMediaToNetworkImageView(true, remoteUrl, media, view);

    }

    public void loadThumbMediaToNetworkImageView(Media media, NetworkImageView view) {

        if (media == null) {
            view.setImageUrl(null, null);
            return;
        }

        String remoteUrl = loadImageThumbUrl(media);

        boolean needSetOrientationNumber = media.isLocal();

        loadMediaToNetworkImageView(needSetOrientationNumber, remoteUrl, media, view);
    }

    public void loadSmallThumbMediaToNetworkImageView(Media media, NetworkImageView view) {

        if (media == null) {
            view.setImageUrl(null, null);
            return;
        }

        String remoteUrl = loadImageSmallThumbUrl(media);

        boolean needSetOrientationNumber = media.isLocal();

        loadMediaToNetworkImageView(needSetOrientationNumber, remoteUrl, media, view);
    }


    public String loadImageSmallThumbUrl(Media media) {

        String imageUrl;
        if (media.isLocal()) {
            imageUrl = media.getMiniThumb();

            if (imageUrl.isEmpty())
                imageUrl = media.getThumb();

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            imageUrl = mMemoryDataSource.loadGateway() + ":" + Util.PORT + Util.MEDIA_PARAMETER + "/" + media.getUuid() + "/thumbnail?width=32&height=32&autoOrient=true&modifier=caret";


        }
        return imageUrl;

    }

    public String loadImageThumbUrl(Media media) {

        String imageUrl;

        if (media.isLocal()) {
            imageUrl = media.getThumb();
        } else {

            int width = Integer.parseInt(media.getWidth());
            int height = Integer.parseInt(media.getHeight());

            int[] result = Util.formatPhotoWidthHeight(width, height);

            imageUrl = mMemoryDataSource.loadGateway() + ":" + Util.PORT + Util.MEDIA_PARAMETER + "/" + media.getUuid() + "/thumbnail?width=" + String.valueOf(result[0]) + "&height=" + String.valueOf(result[1]) + "&autoOrient=true&modifier=caret";

        }

        return imageUrl;
    }

    public String loadImageOriginalUrl(Media media) {

        String imageUrl;
        if (media.isLocal()) {
            imageUrl = media.getThumb();
        } else {
            imageUrl = mMemoryDataSource.loadGateway() + ":" + Util.PORT + Util.MEDIA_PARAMETER + "/" + media.getUuid() + "/download";
        }
        return imageUrl;
    }

    public List<LoggedInUser> loadLoggedInUserInDB() {
        return mDBDataSource.loadLoggedInUser();
    }

    public List<LoggedInUser> loadLoggedInUserInMemory() {
        return mMemoryDataSource.loadLoggedInUser();
    }

    public void insertLoggedInUserToDB(List<LoggedInUser> loggedInUsers) {
        mDBDataSource.insertLoggedInUser(loggedInUsers);
    }

    public void insertLoggedInUserToMemory(List<LoggedInUser> loggedInUsers) {
        mMemoryDataSource.insertLoggedInUser(loggedInUsers);
    }

    public void startDiscovery(RxDnssd rxDnssd, final EquipmentDiscoveryCallback callback) {
        mSubscription = rxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(rxDnssd.resolve())
                .compose(rxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        if (bonjourService.isLost()) return;

                        callback.onEquipmentDiscovery(bonjourService);
                    }
                });
    }

    public void stopDiscovery() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    public void insertTokenToMemory(String token) {
        mMemoryDataSource.insertToken(token);
    }

    public void insertDeviceIDToMemory(String deviceID) {
        mMemoryDataSource.insertDeviceID(deviceID);
    }

    public void insertGatewayToMemory(String gateway) {
        mMemoryDataSource.insertGateway(gateway);
    }

    public void insertLoginUserUUIDToMemory(String userUUID) {
        mMemoryDataSource.insertLoginUserUUID(userUUID);
    }

    public void insertTokenToDB(String token) {
        mDBDataSource.insertToken(token);
    }

    public void insertDeviceIDToDB(String deviceID) {
        mDBDataSource.insertDeviceID(deviceID);
    }

    public void insertGatewayToDB(String gateway) {
        mDBDataSource.insertGateway(gateway);
    }

    public void insertLoginUserUUIDToDB(String userUUID) {
        mDBDataSource.insertLoginUserUUID(userUUID);
    }

    public void insertGatewayToServer(String gateway) {
        mServerDataSource.insertGateway(gateway);
    }

    public void insertTokenToServer(String token) {
        mServerDataSource.insertToken(token);
    }

    public void insertDeviceIdToServer(String deviceId) {
        mServerDataSource.insertDeviceID(deviceId);
    }

    public boolean checkAutoUpload() {

        List<LoggedInUser> loggedInUsers = loadLoggedInUserInMemory();
        String userUUID = loadCurrentLoginUserUUIDInMemory();

        String deviceId = mMemoryDataSource.loadDeviceID().getDeviceID();

        for (LoggedInUser loggedInUser : loggedInUsers) {

            if (loggedInUser.getUser().getUuid().equals(userUUID)) {
                if (!mDBDataSource.getCurrentUploadDeviceID().equals(deviceId)) {
                    mDBDataSource.saveAutoUploadOrNot(false);

                    return false;

                } else {
                    mDBDataSource.saveAutoUploadOrNot(true);

                    return true;
                }
            }
        }

        return false;
    }

    public void saveLoggedInUser(String equipmentGroupName) {
        User currentUser = loadCurrentLoginUserInMemory();

        String deviceID = mMemoryDataSource.loadDeviceID().getDeviceID();
        String token = mMemoryDataSource.loadToken();
        String gateway = mMemoryDataSource.loadGateway();

        LoggedInUser loggedInUser = new LoggedInUser(deviceID, token, gateway, equipmentGroupName, currentUser);

        insertLoggedInUserToDB(Collections.singletonList(loggedInUser));
        insertLoggedInUserToMemory(Collections.singletonList(loggedInUser));

    }

    public void deleteLoggedInUser(LoggedInUser loggedInUser) {

        mDBDataSource.deleteLoggedInUser(loggedInUser);
        mMemoryDataSource.deleteLoggedInUser(loggedInUser);
    }


    public void saveAutoUploadOrNot(boolean autoUploadOrNot) {
        mDBDataSource.saveAutoUploadOrNot(autoUploadOrNot);
    }

    public boolean getAutoUploadOrNot() {
        return mDBDataSource.getAutoUploadOrNot();
    }

    public void saveCurrentUploadDeviceID() {
        mDBDataSource.saveCurrentUploadDeviceID(mMemoryDataSource.loadDeviceID().getDeviceID());
    }

    public int getAlreadyUploadMediaCount() {

        List<Media> medias = mMemoryDataSource.loadAllLocalMedias().getMedias();

        String deviceID = mMemoryDataSource.loadDeviceID().getDeviceID();

        int alreadyUploadMediaCount = 0;

        for (Media media : medias) {

            if (media.getUploadedDeviceIDs().contains(deviceID)) {
                alreadyUploadMediaCount++;
            }

        }

        return alreadyUploadMediaCount;

    }

    public int getTotalMediaCount() {
        return mMemoryDataSource.loadAllLocalMedias().getMedias().size();
    }

    public void preLoadMediaSmallThumb(String url, int width, int height) {

        ImageLoader imageLoader = imageGifLoaderInstance.getImageLoader(mMemoryDataSource.loadToken());
        imageLoader.preLoadMediaSmallThumb(url, width, height);

    }

    public void cancelPreLoadMediaSmallThumb() {

        imageGifLoaderInstance.getImageLoader(mMemoryDataSource.loadToken()).cancelAllPreLoadMedia();

    }


}
