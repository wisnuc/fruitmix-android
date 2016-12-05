package com.winsun.fruitmix.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.eventbus.DownloadFileEvent;
import com.winsun.fruitmix.eventbus.EditPhotoInMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.MediaCommentRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.MediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.ModifyMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.executor.DeleteDownloadedFileTask;
import com.winsun.fruitmix.executor.DownloadFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.executor.UploadMediaTask;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class ButlerService extends Service {

    private static final String TAG = ButlerService.class.getSimpleName();

    public static void startButlerService(Context context) {
        Intent intent = new Intent(context, ButlerService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
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

        super.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void handleEvent(DownloadFileEvent downloadFileEvent) {

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        DownloadFileTask downloadFileTask = new DownloadFileTask(downloadFileEvent.getFileDownloadState(), this);
        instance.doOneTaskInFixedThreadPool(downloadFileTask);

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

        MediaShare originalMediaShare = editPhotoInMediaShareRequestEvent.getOriginalMediaShare();
        MediaShare modifiedMediaShare = editPhotoInMediaShareRequestEvent.getModifiedMediaShare();

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        Log.i(TAG, "handle modify operation target type:" + targetType);

        switch (targetType) {
            case REMOTE_MEDIA_SHARE:
                ModifyMediaInRemoteMediaShareService.startActionEditPhotoInMediaShare(this, originalMediaShare, modifiedMediaShare);
                break;
            case LOCAL_MEDIA_SHARE:
                ModifyMediaInLocalMediaShareService.startActionEditPhotoInMediaShare(this, originalMediaShare, modifiedMediaShare);
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
                RetrieveRemoteMediaShareService.startActionRetrieveRemoteMediaShare(this);
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

                final String gateway = tokenRequestEvent.getGateway();
                final String userUUID = tokenRequestEvent.getUserUUID();
                final String userPassword = tokenRequestEvent.getUserPassword();

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
