package com.winsun.fruitmix.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.eventbus.LoggedInUserRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.RetrieveTicketOperationEvent;
import com.winsun.fruitmix.executor.DeleteDownloadedFileTask;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.executor.UploadMediaTask;
import com.winsun.fruitmix.generate.media.GenerateMediaThumbUseCase;
import com.winsun.fruitmix.generate.media.InjectGenerateMediaThumbUseCase;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.invitation.data.InjectInvitationDataSource;
import com.winsun.fruitmix.invitation.data.InvitationDataSource;
import com.winsun.fruitmix.media.CalcMediaDigestStrategy;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.network.change.InjectNetworkChangeUseCase;
import com.winsun.fruitmix.network.change.NetworkChangeUseCase;
import com.winsun.fruitmix.upload.media.InjectUploadMediaUseCase;
import com.winsun.fruitmix.upload.media.UploadMediaCountChangeListener;
import com.winsun.fruitmix.upload.media.UploadMediaUseCase;
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

public class ButlerService extends Service implements UploadMediaCountChangeListener {

    private static final String TAG = ButlerService.class.getSimpleName();

    private static final int RETRIEVE_REMOTE_TICKETS = 0x1003;

    private TimingRetrieveTicketsTask task;

    private boolean mCalcNewLocalMediaDigestFinished = false;
    private boolean mRetrieveRemoteMediaFinished = false;

    private boolean mStopUpload = false;

    private InvitationDataSource invitationDataSource;

    private GenerateMediaThumbUseCase generateMediaThumbUseCase;
    private UploadMediaUseCase uploadMediaUseCase;

    private static boolean startRetrieveTicketTask = false;

    private static boolean stopRetrieveTicketTask = false;

    private boolean alreadyStart = false;

    private CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback;

    private static List<UploadMediaCountChangeListener> uploadMediaCountChangeListeners = new ArrayList<>();

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

        Log.d(TAG, "onCreate: start service");

        EventBus.getDefault().register(this);

        task = new TimingRetrieveTicketsTask(this, getMainLooper());

        //TODO: commit this for consider get ticket when login with wechat token,api is not exist
//        task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS, Util.refreshTicketsDelayTime);

        calcMediaDigestCallback = new CalcMediaDigestStrategy.CalcMediaDigestCallback() {
            @Override
            public void handleFinished() {

                generateMediaThumbUseCase.startGenerateMediaMiniThumb();
                generateMediaThumbUseCase.startGenerateMediaThumb();

                uploadMediaUseCase.startUploadMedia();

            }

            @Override
            public void handleNothing() {

                generateMediaThumbUseCase.startGenerateMediaMiniThumb();
                generateMediaThumbUseCase.startGenerateMediaThumb();

                uploadMediaUseCase.startUploadMedia();

            }
        };

        initInstance();

        alreadyStart = true;

    }

    private void initInstance() {
        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(this);

        mediaDataSourceRepository.registerCalcDigestCallback(calcMediaDigestCallback);

        generateMediaThumbUseCase = InjectGenerateMediaThumbUseCase.provideGenerateMediaThumbUseCase(this);
        uploadMediaUseCase = InjectUploadMediaUseCase.provideUploadMediaUseCase(this);

        initInvitationRemoteDataSource();

//        uploadMediaUseCase.registerUploadMediaCountChangeListener(this);

    }

    public static void registerUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        Log.d(TAG, "registerUploadMediaCountChangeListener: " + uploadMediaCountChangeListener);

        uploadMediaCountChangeListeners.add(uploadMediaCountChangeListener);

    }

    public static void unregisterUploadMediaCountChangeListener(UploadMediaCountChangeListener uploadMediaCountChangeListener) {

        Log.d(TAG, "unregisterUploadMediaCountChangeListener: " + uploadMediaCountChangeListener);

        uploadMediaCountChangeListeners.remove(uploadMediaCountChangeListener);
    }

    @Override
    public void onGetUploadMediaCountFail(int httpErrorCode) {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "onGetUploadMediaCountFail: " + uploadMediaCountChangeListener);

            uploadMediaCountChangeListener.onGetUploadMediaCountFail(httpErrorCode);
        }

    }

    @Override
    public void onUploadMediaFail(int httpErrorCode) {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "onUploadMediaFail: " + uploadMediaCountChangeListener);

            uploadMediaCountChangeListener.onUploadMediaFail(httpErrorCode);
        }

    }

    @Override
    public void onCreateFolderFail(int httpErrorCode) {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "onCreateFolderFail: " + uploadMediaCountChangeListener);

            uploadMediaCountChangeListener.onCreateFolderFail(httpErrorCode);
        }

    }

    @Override
    public void onGetFolderFail(int httpErrorCode) {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "onGetFolderFail: " + uploadMediaCountChangeListener);

            uploadMediaCountChangeListener.onGetFolderFail(httpErrorCode);
        }

    }

    @Override
    public void onStartGetUploadMediaCount() {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "onStartGetUploadMediaCount: " + uploadMediaCountChangeListener);

            uploadMediaCountChangeListener.onStartGetUploadMediaCount();
        }

    }

    @Override
    public void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount) {

        for (UploadMediaCountChangeListener uploadMediaCountChangeListener : uploadMediaCountChangeListeners) {

            Log.d(TAG, "call notifyUploadMediaCountChange " + uploadMediaCountChangeListener + " alreadyUploadedMediaCount: " + uploadedMediaCount + " localMedias Size: " + totalCount);

            uploadMediaCountChangeListener.onUploadMediaCountChanged(uploadedMediaCount, totalCount);
        }

    }

    public static void startRetrieveTicketTask() {

        startRetrieveTicketTask = true;

    }

    public static void stopRetrieveTicketTask() {

        startRetrieveTicketTask = false;
    }

    private static void stopRetrieveTicketTaskForever() {
        stopRetrieveTicketTask = true;
    }

    private void initInvitationRemoteDataSource() {

        if (invitationDataSource == null)
            invitationDataSource = InjectInvitationDataSource.provideInvitationDataSource(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: " + alreadyStart);

        //if app is killed by user,onCreate will not be called,so check alreadyStart and initInstance
        if (!alreadyStart) {
            initInstance();
        } else
            alreadyStart = false;

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

        stopRetrieveTicketTask();

        stopRetrieveTicketTaskForever();

        task.removeMessages(RETRIEVE_REMOTE_TICKETS);

        task = null;

        generateMediaThumbUseCase.stopGenerateLocalPhotoThumbnail();
        generateMediaThumbUseCase.stopGenerateLocalPhotoMiniThumbnail();

        uploadMediaUseCase.stopUploadMedia();

        uploadMediaUseCase.stopRetryUploadForever();

//        uploadMediaUseCase.unregisterUploadMediaCountChangeListener(this);

        MediaDataSourceRepository mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(this);

        mediaDataSourceRepository.unregisterCalcDigestCallback(calcMediaDigestCallback);

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

                    Log.d(TAG, "start RetrieveTicketTask :" + startRetrieveTicketTask);

                    if (!ButlerService.startRetrieveTicketTask) {

                        if (!ButlerService.stopRetrieveTicketTask && task != null)
                            task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS, Util.refreshTicketsDelayTime);

                        return;
                    }

                    //TODO: check token is exist when call getInvitation
                    butlerService.initInvitationRemoteDataSource();

                    butlerService.invitationDataSource.getInvitation(new BaseLoadDataCallbackImpl<ConfirmInviteUser>() {
                        @Override
                        public void onSucceed(List<ConfirmInviteUser> data, OperationResult operationResult) {
                            super.onSucceed(data, operationResult);

                            Log.d(ButlerService.TAG, "onSucceed: retrieve tickets in service, data size:" + data.size());

                            if (!data.isEmpty())
                                EventBus.getDefault().post(new RetrieveTicketOperationEvent(Util.REMOTE_CONFIRM_INVITE_USER_RETRIEVED, new OperationSuccess(), new ArrayList<>(data)));

                            if (!ButlerService.stopRetrieveTicketTask && task != null)
                                task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS, Util.refreshTicketsDelayTime);

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {
                            super.onFail(operationResult);

                            Log.d(ButlerService.TAG, "onFail: retrieve tickets in service");

                            if (!ButlerService.stopRetrieveTicketTask && task != null)
                                task.sendEmptyMessageDelayed(RETRIEVE_REMOTE_TICKETS, Util.refreshTicketsDelayTime);
                        }
                    });

                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        switch (action) {
            case Util.CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED:

                mCalcNewLocalMediaDigestFinished = true;

                break;
            case Util.REMOTE_MEDIA_RETRIEVED:
                mRetrieveRemoteMediaFinished = true;
                break;

            case Util.NETWORK_CHANGED:

                NetworkChangeUseCase networkChangeUseCase = InjectNetworkChangeUseCase.provideInstance(this);
                networkChangeUseCase.handleNetworkChange();

                break;

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
                uploadMediaUseCase.startUploadMedia();
                break;
            case STOP_UPLOAD:
                uploadMediaUseCase.stopUploadMedia();
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

                break;
            case REMOTE_MEDIA:

                break;

            case REMOTE_DEVICE_ID:

                break;
            case REMOTE_TOKEN:

                break;
            case LOCAL_MEDIA_IN_CAMERA:
                RetrieveNewLocalMediaInCameraService.startActionRetrieveNewLocalMediaInCamera(this);
                break;
            case REMOTE_FILE:

                break;
            case DOWNLOADED_FILE:
                RetrieveDownloadedFileService.startActionRetrieveDownloadedFile(this);
                break;
            case LOCAL_LOGGED_IN_USER:
                DBUtils dbUtils = DBUtils.getInstance(this);
                LocalCache.LocalLoggedInUsers.addAll(dbUtils.getAllLoggedInUser());
                break;
            case MEDIA_ORIGINAL_PHOTO:

                break;
        }
    }
}
