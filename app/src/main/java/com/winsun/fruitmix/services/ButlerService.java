package com.winsun.fruitmix.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.executor.UploadMediaTask;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

public class ButlerService extends Service {

    private static final String TAG = ButlerService.class.getSimpleName();

    private LocalBroadcastManager broadcastManager;
    private CustomBroadCastReceiver broadCastReceiver;

    public static void startButlerService(Context context) {
        Intent intent = new Intent(context, ButlerService.class);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        broadcastManager = LocalBroadcastManager.getInstance(this);

        broadCastReceiver = new CustomBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter(Util.OPERATION);
        broadcastManager.registerReceiver(broadCastReceiver, intentFilter);

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
        super.onDestroy();

        broadcastManager.unregisterReceiver(broadCastReceiver);
    }

    private class CustomBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.OPERATION)) {
                String type = intent.getStringExtra(Util.OPERATION_TYPE_NAME);

                OperationType operationType = OperationType.valueOf(type);

                switch (operationType) {
                    case CREATE:
                        handleCreateOperation(intent);
                        break;
                    case MODIFY:
                        handleModifyOperation(intent);
                        break;
                    case EDIT_PHOTO_IN_MEDIASHARE:
                        handleEditPhotoInMediaShareOperation(intent);
                        break;
                    case DELETE:
                        handleDeleteOperation(intent);
                        break;
                    case GET:
                        handleGetOperation(intent);
                        break;
                }
            }

        }
    }

    private void handleCreateOperation(Intent intent) {
        String type = intent.getStringExtra(Util.OPERATION_TARGET_TYPE_NAME);

        OperationTargetType targetType = OperationTargetType.valueOf(type);

        Log.i(TAG, "handle create operation target type:" + targetType);

        String imageUUID;
        Comment comment;
        Media media;
        MediaShare mediaShare;
        ExecutorServiceInstance instance;

        switch (targetType) {
            case LOCAL_MEDIASHARE:
                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                CreateLocalMediaShareService.startActionCreateLocalShare(this, mediaShare);
                break;
            case LOCAL_MEDIA_COMMENT:
                imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                CreateLocalCommentService.startActionCreateLocalComment(this, imageUUID, comment);
                break;

            case REMOTE_MEDIA:

                media = intent.getParcelableExtra(Util.OPERATION_MEDIA);

                instance = ExecutorServiceInstance.SINGLE_INSTANCE;
                UploadMediaTask task = new UploadMediaTask(this,media);
                instance.doOneTaskInFixedThreadPool(task);

                break;
            case REMOTE_MEDIASHARE:

                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);

                CreateRemoteMediaShareService.startActionCreateRemoteMediaShareTask(this, mediaShare);

                break;
            case REMOTE_MEDIA_COMMENT:
                imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                CreateRemoteCommentService.startActionCreateRemoteCommentTask(this, comment, imageUUID);
                break;
        }

    }


    private void handleModifyOperation(Intent intent) {
        String type = intent.getStringExtra(Util.OPERATION_TARGET_TYPE_NAME);

        OperationTargetType targetType = OperationTargetType.valueOf(type);

        Log.i(TAG, "handle modify operation target type:" + targetType);

        MediaShare mediaShare;

        switch (targetType) {
            case LOCAL_MEDIASHARE:
                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                ModifyLocalMediaShareService.startActionModifyLocalMediaShare(this, mediaShare);
                break;
            case REMOTE_MEDIASHARE:
                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                String requestData = intent.getStringExtra(Util.KEY_MODIFY_REMOTE_MEDIASHARE_REQUEST_DATA);
                ModifyRemoteMediaShareService.startActionModifyRemoteMediaShare(this, mediaShare,requestData);
                break;

        }
    }

    private void handleEditPhotoInMediaShareOperation(Intent intent) {

        MediaShare originalMediaShare = intent.getParcelableExtra(Util.OPERATION_ORIGINAL_MEDIASHARE_WHEN_EDIT_PHOTO);
        MediaShare modifiedMediaShare = intent.getParcelableExtra(Util.OPERATION_MODIFIED_MEDIASHARE_WHEN_EDIT_PHOTO);

        String type = intent.getStringExtra(Util.OPERATION_TARGET_TYPE_NAME);

        OperationTargetType targetType = OperationTargetType.valueOf(type);

        Log.i(TAG, "handle modify operation target type:" + targetType);

        switch (targetType) {
            case REMOTE_MEDIASHARE:
                ModifyMediaInRemoteMediaShareService.startActionEditPhotoInMediaShare(this, originalMediaShare, modifiedMediaShare);
                break;
            case LOCAL_MEDIASHARE:
                ModifyMediaInLocalMediaShareService.startActionEditPhotoInMediaShare(this, originalMediaShare, modifiedMediaShare);
                break;
        }
    }

    private void handleDeleteOperation(Intent intent) {
        String type = intent.getStringExtra(Util.OPERATION_TARGET_TYPE_NAME);

        OperationTargetType targetType = OperationTargetType.valueOf(type);

        Log.i(TAG, "handle delete operation target type:" + targetType);

        MediaShare mediaShare;
        Comment comment;
        String imageUUID;

        switch (targetType) {
            case LOCAL_MEDIASHARE:
                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                DeleteLocalMediaShareService.startActionDeleteLocalShare(this,mediaShare);
                break;
            case REMOTE_MEDIASHARE:
                mediaShare = intent.getParcelableExtra(Util.OPERATION_MEDIASHARE);
                DeleteRemoteMediaShareService.startActionDeleteRemoteShare(this, mediaShare);
                break;
            case LOCAL_MEDIA_COMMENT:
                comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                DeleteLocalCommentService.startActionDeleteLocalComment(this, comment, imageUUID);
                break;
        }
    }


    private void handleGetOperation(Intent intent) {
        String type = intent.getStringExtra(Util.OPERATION_TARGET_TYPE_NAME);

        OperationTargetType targetType = OperationTargetType.valueOf(type);

        if (targetType != OperationTargetType.REMOTE_MEDIA_COMMENT)
            Log.i(TAG, "handle get operation target type:" + targetType);

        String imageUUID;

        switch (targetType) {
            case LOCAL_MEDIA:
                RetrieveLocalMediaService.startActionRetrieveLocalMedia(this);
                break;
            case LOCAL_MEDIASHARE:
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
            case REMOTE_MEDIASHARE:
                RetrieveRemoteMediaShareService.startActionRetrieveRemoteMediaShare(this);
                break;
            case REMOTE_MEDIA_COMMENT:
                imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                RetrieveRemoteMediaCommentService.startActionRetrieveRemoteMediaComment(this, imageUUID);
                break;
            case REMOTE_DEVICEID:
                RetrieveDeviceIdService.startActionRetrieveDeviceId(this);
                break;
            case REMOTE_TOKEN:
                final String gateway = intent.getStringExtra(Util.GATEWAY);
                final String userUUID = intent.getStringExtra(Util.USER_UUID);
                final String userPassword = intent.getStringExtra(Util.PASSWORD);

                RetrieveTokenService.startActionRetrieveToken(this, gateway, userUUID, userPassword);

                break;
            case LOCAL_MEDIA_IN_CAMERA:
                RetrieveNewLocalMediaInCameraService.startActionRetrieveNewLocalMediaInCamera(this);
                break;
            case REMOTE_FILE:
                String folderUUID = intent.getStringExtra(Util.FOLDER_UUID);
                RetrieveRemoteFileService.startActionRetrieveRemoteFile(this,folderUUID);
                break;
        }
    }
}
