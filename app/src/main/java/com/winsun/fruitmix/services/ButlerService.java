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

import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.eventbus.LoggedInUserRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.RetrieveMediaOriginalPhotoRequestEvent;
import com.winsun.fruitmix.eventbus.RetrieveTicketOperationEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.eventbus.UserRequestEvent;
import com.winsun.fruitmix.executor.DeleteDownloadedFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.executor.GenerateLocalMediaMiniThumbTask;
import com.winsun.fruitmix.executor.GenerateLocalMediaThumbTask;
import com.winsun.fruitmix.executor.RetrieveOriginalPhotoTask;
import com.winsun.fruitmix.executor.UploadMediaTask;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.inject.Inject;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.invitation.InvitationRemoteDataSource;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ButlerService extends Service {

    private static final String TAG = ButlerService.class.getSimpleName();

    private static final int RETRIEVE_REMOTE_TICKETS = 0x1003;

    private TimingRetrieveTicketsTask task;

    private boolean mCalcNewLocalMediaDigestFinished = false;
    private boolean mRetrieveRemoteMediaFinished = false;

    private boolean mStopUpload = false;

    private boolean mStopGenerateMiniThumb = false;

    private boolean mStopGenerateThumb = false;

    private InvitationRemoteDataSource invitationRemoteDataSource;

    public static void startButlerService(Context context) {
        Intent intent = new Intent(context, ButlerService.class);
        context.startService(intent);
    }

    public static void stopButlerService(Context context) {
        context.stopService(new Intent(context, ButlerService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

//        task = new TimingRetrieveTicketsTask(this,getMainLooper());
//
//        task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS,20 * 1000);

        initInvitationRemoteDataSource();
    }

    private void initInvitationRemoteDataSource() {

        if (invitationRemoteDataSource == null)
            invitationRemoteDataSource = new InvitationRemoteDataSource(Inject.provideIHttpUtil(this), Inject.provideHttpRequestFactory());
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

//        task.removeMessages(RETRIEVE_REMOTE_TICKETS);

        task = null;

        stopUpload();

        stopGenerateThumb();

        stopGenerateMiniThumb();

        super.onDestroy();

    }

    private class TimingRetrieveTicketsTask extends Handler {

        WeakReference<ButlerService> weakReference = null;

        TimingRetrieveTicketsTask(ButlerService butlerService, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(butlerService);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RETRIEVE_REMOTE_TICKETS:

                    ButlerService butlerService = weakReference.get();

                    //TODO: check token is exist when call getInvitation
                    butlerService.initInvitationRemoteDataSource();

                    butlerService.invitationRemoteDataSource.getInvitation(new BaseLoadDataCallbackImpl<ConfirmInviteUser>() {
                        @Override
                        public void onSucceed(List<ConfirmInviteUser> data, OperationResult operationResult) {
                            super.onSucceed(data, operationResult);

                            if (!data.isEmpty())
                                EventBus.getDefault().post(new RetrieveTicketOperationEvent(Util.REMOTE_CONFIRM_INVITE_USER_RETRIEVED, new OperationSuccess(), new ArrayList<>(data)));
                        }
                    });

                    task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS, Util.refreshMediaShareDelayTime);

                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        OperationResult operationResult = operationEvent.getOperationResult();
        OperationResultType operationResultType = operationResult.getOperationResultType();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED:

                startGenerateLocalPhotoThumbnail();

                startGenerateLocalPhotoMiniThumbnail();

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
            case Util.REMOTE_DEVICE_ID_RETRIEVED:

                handleRemoteDeviceIdRetrieved(operationResult, operationResultType);
                break;
            case Util.REMOTE_USER_RETRIEVED:

                /*if (Util.loginType == LoginType.LOGIN) {
                    EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                } else {
                    EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                }*/

                EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));

                if (operationResultType == OperationResultType.SUCCEED)
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
                    LocalCache.DeviceID = LocalCache.getGlobalData(this, Util.DEVICE_ID_MAP_NAME);

                    FNAS.retrieveUser(this);

                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();

                } else {
                    EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
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
//                FNAS.retrieveUser(this);

                EventBus.getDefault().postSticky(new OperationEvent(Util.REMOTE_USER_RETRIEVED, new OperationSuccess()));

                break;
            default:

                if (Util.loginType == LoginType.SPLASH_SCREEN) {
                    FNAS.JWT = LocalCache.getToken(this);

                    LocalCache.DeviceID = LocalCache.getGlobalData(this, Util.DEVICE_ID_MAP_NAME);

                    FNAS.retrieveUser(this);

                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();

                } else {
                    EventBus.getDefault().postSticky(new OperationEvent(Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED, operationResult));
                }

        }
    }

    private void startGenerateLocalPhotoThumbnail() {

        if (LocalCache.LocalMediaMapKeyIsOriginalPhotoPath == null) {
            Log.w(TAG, "LocalMediaMapKeyIsOriginalPhotoPath", new NullPointerException());

            return;
        }

        DBUtils dbUtils = DBUtils.getInstance(this);
        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;

        for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

            if (mStopGenerateThumb) return;

            if (media.getThumb().isEmpty()) {

                GenerateLocalMediaThumbTask task = new GenerateLocalMediaThumbTask(media, dbUtils, mStopGenerateThumb);
                instance.doOneTaskInGenerateThumbThreadPool(task);
            }

        }

    }

    private void stopGenerateThumb() {

        mStopGenerateThumb = true;

        ExecutorServiceInstance.SINGLE_INSTANCE.shutdownGenerateMiniThumbThreadPoolNow();

    }


    private void startGenerateLocalPhotoMiniThumbnail() {

        if (LocalCache.LocalMediaMapKeyIsOriginalPhotoPath == null) {
            Log.w(TAG, "LocalMediaMapKeyIsOriginalPhotoPath", new NullPointerException());

            return;
        }

        DBUtils dbUtils = DBUtils.getInstance(this);
        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;

        for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

            if (mStopGenerateMiniThumb) return;

            if (media.getMiniThumbPath().isEmpty()) {

                GenerateLocalMediaMiniThumbTask task = new GenerateLocalMediaMiniThumbTask(media, dbUtils, mStopGenerateMiniThumb);
                instance.doOneTaskInGenerateMiniThumbThreadPool(task);
            }

        }

    }

    private void stopGenerateMiniThumb() {

        mStopGenerateMiniThumb = true;

        ExecutorServiceInstance.SINGLE_INSTANCE.shutdownGenerateMiniThumbThreadPoolNow();

    }

    private void startUpload() {

        Log.i(TAG, "startUpload: auto upload:" + LocalCache.getAutoUploadOrNot(this));

        if (mStopUpload)
            mStopUpload = false;

        if (mCalcNewLocalMediaDigestFinished && mRetrieveRemoteMediaFinished && LocalCache.getAutoUploadOrNot(this)) {
            startUploadAllLocalPhoto();

            Log.i(TAG, "startUpload");
        }
    }

    private void stopUpload() {

        mStopUpload = true;

        ExecutorServiceInstance.SINGLE_INSTANCE.shutdownUploadMediaThreadPoolNow();
    }


    private void startUploadAllLocalPhoto() {
        for (Media media : LocalCache.LocalMediaMapKeyIsOriginalPhotoPath.values()) {

            if (mStopUpload) return;

            if (LocalCache.DeviceID != null && !media.getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
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
            case START_UPLOAD:
                startUpload();
                break;
            case STOP_UPLOAD:
                stopUpload();
                break;
        }

    }


    private void handleCreateOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        if (targetType != OperationTargetType.REMOTE_MEDIA)
            Log.i(TAG, "handle create operation target type:" + targetType);

        String imageUUID;
        Media media;
        ExecutorServiceInstance instance;

        switch (targetType) {

            case REMOTE_MEDIA:

                media = ((MediaRequestEvent) requestEvent).getMedia();

                instance = ExecutorServiceInstance.SINGLE_INSTANCE;
                UploadMediaTask task = new UploadMediaTask(DBUtils.getInstance(this), media, mStopUpload);
                instance.doOneTaskInUploadMediaThreadPool(task);

                break;

            case REMOTE_USER:

                UserRequestEvent userRequestEvent = (UserRequestEvent) requestEvent;
                String userName = userRequestEvent.getmUserName();
                String userPassword = userRequestEvent.getmUserPassword();

                CreateRemoteUserService.startActionCreateRemoteUser(this, userName, userPassword);

                break;
            case LOCAL_LOGGED_IN_USER:
                LoggedInUserRequestEvent loggedInUserRequestEvent = (LoggedInUserRequestEvent) requestEvent;
                LoggedInUser loggedInUser = loggedInUserRequestEvent.getmLoggedInUser();
                DBUtils.getInstance(this).insertLoggedInUserInDB(Collections.singletonList(loggedInUser));
        }

    }


    private void handleModifyOperation(RequestEvent requestEvent) {

    }

    private void handleEditPhotoInMediaShareOperation(RequestEvent requestEvent) {

    }

    private void handleDeleteOperation(RequestEvent requestEvent) {

        OperationTargetType targetType = requestEvent.getOperationTargetType();

        Log.i(TAG, "handle delete operation target type:" + targetType);

        switch (targetType) {

            case DOWNLOADED_FILE:

                List<String> fileUUIDs = ((DeleteDownloadedRequestEvent) requestEvent).getFileUUIDs();
                ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;

                DeleteDownloadedFileTask deleteDownloadedFileTask = new DeleteDownloadedFileTask(DBUtils.getInstance(this), fileUUIDs);

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

            case REMOTE_USER:
                RetrieveRemoteUserService.startActionRetrieveRemoteUser(this);
                break;
            case REMOTE_MEDIA:
                RetrieveRemoteMediaService.startActionRetrieveRemoteMedia(this);
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

                AbstractFileRequestEvent fileRequestEvent = (AbstractFileRequestEvent) requestEvent;

                String folderUUID = fileRequestEvent.getFolderUUID();
                String rootUUID = fileRequestEvent.getRootUUID();
                RetrieveRemoteFileService.startActionRetrieveRemoteFile(this, folderUUID, rootUUID);
                break;
            case DOWNLOADED_FILE:
                RetrieveDownloadedFileService.startActionRetrieveDownloadedFile(this);
                break;
            case LOCAL_LOGGED_IN_USER:
                DBUtils dbUtils = DBUtils.getInstance(this);
                LocalCache.LocalLoggedInUsers.addAll(dbUtils.getAllLoggedInUser());
                break;
            case MEDIA_ORIGINAL_PHOTO:

                List<Media> medias = ((RetrieveMediaOriginalPhotoRequestEvent) requestEvent).getMedias();
                ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
                RetrieveOriginalPhotoTask downloadFileTask = new RetrieveOriginalPhotoTask(medias, DBUtils.getInstance(this));
                instance.doOneTaskInCachedThreadUsingCallable(downloadFileTask);

                break;
        }
    }
}
