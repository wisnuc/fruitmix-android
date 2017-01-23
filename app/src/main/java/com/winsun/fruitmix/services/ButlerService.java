package com.winsun.fruitmix.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.eventbus.DownloadFileEvent;
import com.winsun.fruitmix.eventbus.EditPhotoInMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.MediaCommentRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.MediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.ModifyMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.RetrieveMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.eventbus.UserRequestEvent;
import com.winsun.fruitmix.executor.DeleteDownloadedFileTask;
import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.executor.UploadMediaTask;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

public class ButlerService extends Service {

    private static final String TAG = ButlerService.class.getSimpleName();

    private static final int RETRIEVE_REMOTE_MEDIA_SHARE = 0x1003;

    private TimingRetrieveMediaShareTask task;

    private boolean mCalcNewLocalMediaDigestFinished = false;
    private boolean mRetrieveRemoteMediaFinished = false;

    public static void startButlerService(Context context) {
        Intent intent = new Intent(context, ButlerService.class);
        context.startService(intent);
    }

    public static void stopButlerService(Context context) {
        context.stopService(new Intent(context, ButlerService.class));
    }

    public static void stopTimingRetrieveMediaShare() {
        if (Util.startTimingRetrieveMediaShare)
            Util.startTimingRetrieveMediaShare = false;
    }

    public static void startTimingRetrieveMediaShare() {
        if (!Util.startTimingRetrieveMediaShare)
            Util.startTimingRetrieveMediaShare = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

        task = new TimingRetrieveMediaShareTask(this, getMainLooper());

        task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_MEDIA_SHARE, 20 * 1000);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        task.removeMessages(RETRIEVE_REMOTE_MEDIA_SHARE);

        stopTimingRetrieveMediaShare();

        OkHttpUtil.INSTANCE.cancelAllNotFinishCall();

        task = null;

        super.onDestroy();

    }

    private class TimingRetrieveMediaShareTask extends Handler {

        WeakReference<ButlerService> weakReference = null;

        TimingRetrieveMediaShareTask(ButlerService butlerService, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(butlerService);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RETRIEVE_REMOTE_MEDIA_SHARE:

                    ButlerService butlerService = weakReference.get();

                    if (Util.startTimingRetrieveMediaShare)
                        FNAS.retrieveRemoteMediaShare(butlerService, false);

                    task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_MEDIA_SHARE, Util.refreshMediaShareDelayTime);

                    break;
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handleEvent(DownloadFileEvent downloadFileEvent) {

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        DownloadFileTask downloadFileTask = new DownloadFileTask(downloadFileEvent.getFileDownloadState(), this);
        instance.doOneTaskInFixedThreadPool(downloadFileTask);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        OperationResult operationResult = operationEvent.getOperationResult();
        OperationResultType operationResultType = operationResult.getOperationResultType();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED:
                mCalcNewLocalMediaDigestFinished = true;
                startUpload();
                break;
            case Util.REMOTE_MEDIA_RETRIEVED:
                mRetrieveRemoteMediaFinished = true;
                startUpload();
                break;
            case Util.REMOTE_TOKEN_RETRIEVED:

                handleRemoteTokenRetrieved(operationResult, operationResultType);

                break;
            case Util.REMOTE_DEVICEID_RETRIEVED:

                handleRemoteDeviceIdRetrieved(operationResult, operationResultType);
                break;
            case Util.REMOTE_USER_RETRIEVED:

                if (Util.loginType == LoginType.LOGIN) {
                    EventBus.getDefault().post(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                } else {
                    EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                }

                FNAS.retrieveRemoteMedia(this);
                break;
        }

    }

    private void handleRemoteDeviceIdRetrieved(OperationResult operationResult, OperationResultType operationResultType) {
        switch (operationResultType) {
            case SUCCEED:
                FNAS.retrieveUser(this);
                break;
            default:

                if (Util.loginType == LoginType.SPLASH_SCREEN) {
                    LocalCache.DeviceID = LocalCache.GetGlobalData(this, Util.DEVICE_ID_MAP_NAME);

                    FNAS.retrieveUser(this);

                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();

                } else {
                    EventBus.getDefault().post(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                }

        }
    }

    private void handleRemoteTokenRetrieved(OperationResult operationResult, OperationResultType operationResultType) {
        switch (operationResultType) {
            case SUCCEED:
                if (Util.loginType == LoginType.LOGIN) {
                    LocalCache.CleanAll(this);
                    LocalCache.Init();
                }
                FNAS.retrieveRemoteDeviceID(this);
                break;
            default:

                if (Util.loginType == LoginType.SPLASH_SCREEN) {
                    FNAS.JWT = LocalCache.getToken(this);

                    LocalCache.DeviceID = LocalCache.GetGlobalData(this, Util.DEVICE_ID_MAP_NAME);

                    FNAS.retrieveUser(this);

                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();

                } else {
                    EventBus.getDefault().post(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                }

        }
    }

    private void startUpload() {
        if (mCalcNewLocalMediaDigestFinished && mRetrieveRemoteMediaFinished) {
            startUploadAllLocalPhoto();
        }
    }

    private void startUploadAllLocalPhoto() {
        for (Media media : LocalCache.LocalMediaMapKeyIsThumb.values()) {
            if (!media.isUploaded()) {
                FNAS.createRemoteMedia(this, media);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handleRequestEvent(RequestEvent requestEvent) {

        OperationType operationType = requestEvent.getOperationType();
        switch (operationType) {
            case CREATE:
                handleCreateOperation(requestEvent);
                break;
            case MODIFY:
                handleModifyOperation(requestEvent);
                break;
            case EDIT_PHOTO_IN_MEDIASHARE:
                handleEditPhotoInMediaShareOperation(requestEvent);
                break;
            case DELETE:
                handleDeleteOperation(requestEvent);
                break;
            case GET:
                handleGetOperation(requestEvent);
                break;
        }

    }


    private void handleCreateOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        if (targetType != OperationTargetType.REMOTE_MEDIA)
            Log.i(TAG, "handle create operation target type:" + targetType);

        String imageUUID;
        Comment comment;
        Media media;
        MediaShare mediaShare;
        ExecutorServiceInstance instance;

        switch (targetType) {
            case LOCAL_MEDIA_SHARE:
                mediaShare = ((MediaShareRequestEvent) requestEvent).getMediaShare();
                CreateLocalMediaShareService.startActionCreateLocalShare(this, mediaShare);
                break;
            case LOCAL_MEDIA_COMMENT:
                MediaCommentRequestEvent localMediaShareCommentOperationEvent = (MediaCommentRequestEvent) requestEvent;
                imageUUID = localMediaShareCommentOperationEvent.getImageUUID();
                comment = localMediaShareCommentOperationEvent.getComment();

                CreateLocalCommentService.startActionCreateLocalComment(this, imageUUID, comment);
                break;

            case REMOTE_MEDIA:

                media = ((MediaRequestEvent) requestEvent).getMedia();

                instance = ExecutorServiceInstance.SINGLE_INSTANCE;
                UploadMediaTask task = new UploadMediaTask(this, media);
                instance.doOneTaskInFixedThreadPool(task);

                break;
            case REMOTE_MEDIA_SHARE:

                mediaShare = ((MediaShareRequestEvent) requestEvent).getMediaShare();

                CreateRemoteMediaShareService.startActionCreateRemoteMediaShareTask(this, mediaShare);

                break;
            case REMOTE_MEDIA_COMMENT:
                MediaCommentRequestEvent remoteMediaShareCommentOperationEvent = (MediaCommentRequestEvent) requestEvent;
                imageUUID = remoteMediaShareCommentOperationEvent.getImageUUID();
                comment = remoteMediaShareCommentOperationEvent.getComment();

                CreateRemoteCommentService.startActionCreateRemoteCommentTask(this, comment, imageUUID);
                break;
            case REMOTE_USER:

                UserRequestEvent userRequestEvent = (UserRequestEvent) requestEvent;
                String userName = userRequestEvent.getmUserName();
                String userPassword = userRequestEvent.getmUserPassword();

                CreateRemoteUserService.startActionCreateRemoteUser(this, userName, userPassword);

                break;
        }

    }


    private void handleModifyOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        Log.i(TAG, "handle modify operation target type:" + targetType);

        MediaShare mediaShare;

        switch (targetType) {
            case LOCAL_MEDIA_SHARE:
                mediaShare = ((MediaShareRequestEvent) requestEvent).getMediaShare();
                ModifyLocalMediaShareService.startActionModifyLocalMediaShare(this, mediaShare);
                break;
            case REMOTE_MEDIA_SHARE:

                ModifyMediaShareRequestEvent modifyMediaShareRequestEvent = (ModifyMediaShareRequestEvent) requestEvent;

                mediaShare = modifyMediaShareRequestEvent.getMediaShare();
                String requestData = modifyMediaShareRequestEvent.getRequestData();
                ModifyRemoteMediaShareService.startActionModifyRemoteMediaShare(this, mediaShare, requestData);
                break;

        }
    }

    private void handleEditPhotoInMediaShareOperation(RequestEvent requestEvent) {

        EditPhotoInMediaShareRequestEvent editPhotoInMediaShareRequestEvent = (EditPhotoInMediaShareRequestEvent) requestEvent;

        MediaShare diffContentsInOriginalMediaShare = editPhotoInMediaShareRequestEvent.getDiffContentsInOriginalMediaShare();
        MediaShare diffContentsInModifiedMediaShare = editPhotoInMediaShareRequestEvent.getDiffContentsInModifiedMediaShare();
        MediaShare modifiedMediaShare = editPhotoInMediaShareRequestEvent.getModifiedMediaShare();

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        Log.i(TAG, "handle modify operation target type:" + targetType);

        switch (targetType) {
            case REMOTE_MEDIA_SHARE:
                ModifyMediaInRemoteMediaShareService.startActionModifyMediaInRemoteMediaShare(this, diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare, modifiedMediaShare);
                break;
            case LOCAL_MEDIA_SHARE:
                ModifyMediaInLocalMediaShareService.startActionModifyMediaInLocalMediaShare(this, diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare, modifiedMediaShare);
                break;
        }
    }

    private void handleDeleteOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        Log.i(TAG, "handle delete operation target type:" + targetType);

        MediaShare mediaShare;
        Comment comment;
        String imageUUID;

        switch (targetType) {
            case LOCAL_MEDIA_SHARE:
                mediaShare = ((MediaShareRequestEvent) requestEvent).getMediaShare();
                DeleteLocalMediaShareService.startActionDeleteLocalShare(this, mediaShare);
                break;
            case REMOTE_MEDIA_SHARE:
                mediaShare = ((MediaShareRequestEvent) requestEvent).getMediaShare();
                DeleteRemoteMediaShareService.startActionDeleteRemoteShare(this, mediaShare);
                break;
            case LOCAL_MEDIA_COMMENT:
                MediaCommentRequestEvent remoteMediaShareCommentOperationEvent = (MediaCommentRequestEvent) requestEvent;
                imageUUID = remoteMediaShareCommentOperationEvent.getImageUUID();
                comment = remoteMediaShareCommentOperationEvent.getComment();
                DeleteLocalCommentService.startActionDeleteLocalComment(this, comment, imageUUID);
                break;
            case DOWNLOADED_FILE:

                List<String> fileUUIDs = ((DeleteDownloadedRequestEvent) requestEvent).getFileUUIDs();
                ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;

                DeleteDownloadedFileTask deleteDownloadedFileTask = new DeleteDownloadedFileTask(this, fileUUIDs);

                instance.doOneTaskInCachedThreadUsingCallable(deleteDownloadedFileTask);

                break;
        }
    }


    private void handleGetOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        if (targetType != OperationTargetType.REMOTE_MEDIA_COMMENT)
            Log.i(TAG, "handle get operation target type:" + targetType);

        String imageUUID;

        switch (targetType) {
            case LOCAL_MEDIA:
                RetrieveLocalMediaService.startActionRetrieveLocalMedia(this);
                break;
            case LOCAL_MEDIA_SHARE:
                RetrieveLocalMediaShareService.startActionRetrieveMediaShare(this);
                break;
            case LOCAL_MEDIA_COMMENT:
                RetrieveLocalMediaCommentService.startActionRetrieveLocalComment(this);
                break;
            case REMOTE_USER:
                RetrieveRemoteUserService.startActionRetrieveRemoteUser(this);
                break;
            case REMOTE_MEDIA:
                RetrieveRemoteMediaService.startActionRetrieveRemoteMedia(this);
                break;
            case REMOTE_MEDIA_SHARE:

                boolean loadMediaShareInDBWhenExceptionOccur = ((RetrieveMediaShareRequestEvent) requestEvent).isLoadMediaShareInDBWhenExceptionOccur();

                RetrieveRemoteMediaShareService.startActionRetrieveRemoteMediaShare(this, loadMediaShareInDBWhenExceptionOccur);
                break;
            case REMOTE_MEDIA_COMMENT:
                MediaCommentRequestEvent remoteMediaShareCommentOperationEvent = (MediaCommentRequestEvent) requestEvent;
                imageUUID = remoteMediaShareCommentOperationEvent.getImageUUID();

                RetrieveRemoteMediaCommentService.startActionRetrieveRemoteMediaComment(this, imageUUID);
                break;
            case REMOTE_DEVICE_ID:
                RetrieveDeviceIdService.startActionRetrieveDeviceId(this);
                break;
            case REMOTE_TOKEN:

                TokenRequestEvent tokenRequestEvent = (TokenRequestEvent) requestEvent;

                String gateway = tokenRequestEvent.getGateway();
                String userUUID = tokenRequestEvent.getUserUUID();
                String userPassword = tokenRequestEvent.getUserPassword();

                RetrieveTokenService.startActionRetrieveToken(this, gateway, userUUID, userPassword);

                break;
            case LOCAL_MEDIA_IN_CAMERA:
                RetrieveNewLocalMediaInCameraService.startActionRetrieveNewLocalMediaInCamera(this);
                break;
            case REMOTE_FILE:
                String folderUUID = ((AbstractFileRequestEvent) requestEvent).getFolderUUID();
                RetrieveRemoteFileService.startActionRetrieveRemoteFile(this, folderUUID);
                break;
            case REMOTE_FILE_SHARE:
                RetrieveRemoteFileShareService.startActionRetrieveRemoteFileShare(this);
                break;
            case DOWNLOADED_FILE:
                RetrieveDownloadedFileService.startActionRetrieveDownloadedFile(this);
                break;
        }
    }
}
