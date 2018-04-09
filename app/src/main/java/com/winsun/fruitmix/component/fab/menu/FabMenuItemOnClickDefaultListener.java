package com.winsun.fruitmix.component.fab.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.GroupShareMenuLayout;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.view.fragment.FileFragment;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.view.GroupContentActivity;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2018/2/28.
 */

public class FabMenuItemOnClickDefaultListener implements FabMenuItemOnClickListener {

    private SelectedMediasListener mSelectedMediasListener;

    private FileFragment mFileFragment;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private AbstractCommand mQuitSelectModeCommand;

    private SystemSettingDataSource mSystemSettingDataSource;

    private GroupRepository mGroupRepository;

    private User currentUser;

    public FabMenuItemOnClickDefaultListener(SelectedMediasListener selectedMediasListener, FileFragment fileFragment,
                                             MediaDataSourceRepository mediaDataSourceRepository,
                                             AbstractCommand quitSelectModeCommand, SystemSettingDataSource systemSettingDataSource,
                                             GroupRepository groupRepository, UserDataRepository userDataRepository) {
        mSelectedMediasListener = selectedMediasListener;
        mFileFragment = fileFragment;
        this.mediaDataSourceRepository = mediaDataSourceRepository;

        mSystemSettingDataSource = systemSettingDataSource;
        mGroupRepository = groupRepository;

        currentUser = userDataRepository.getUserByUUID(mSystemSettingDataSource.getCurrentLoginUserUUID());

        mQuitSelectModeCommand = quitSelectModeCommand;
    }

    @Override
    public void systemShareBtnOnClick(final Context context, final int currentItem) {

        if (!Util.isNetworkConnected(context)) {
            ToastUtil.showToast(context, context.getString(R.string.no_network));
            return;
        }

        AbstractCommand shareInAppCommand = new AbstractCommand() {
            @Override
            public void execute() {
            }

            @Override
            public void unExecute() {
            }
        };

        AbstractCommand shareToOtherAppCommand = new AbstractCommand() {
            @Override
            public void execute() {
                handleShareToOtherApp(currentItem, context);

                mQuitSelectModeCommand.execute();

//                quitSelectMode();

            }

            @Override
            public void unExecute() {
            }
        };

/*
        if (mSystemSettingDataSource.getCurrentWAToken().isEmpty())
            new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(context).show();
        else
            new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand,
                    new GroupShareMenuLayout.GroupShareMenuItemOnClickListener() {
                        @Override
                        public void onClick(PrivateGroup item) {

                            handleShareToGroup(currentItem, context, item);

                            mQuitSelectModeCommand.execute();

                        }
                    }).createDialog(context).show();
*/

        new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(context).show();

    }

    private void handleShareToOtherApp(int currentItem, Context context) {

        if (currentItem == ITEM_MEDIA)
            shareMediaToOtherApp(context);
        else if (currentItem == ITEM_FILE)
            shareFileToOtherApp();

    }

    private void handleShareToGroup(int currentItem, final Context context, PrivateGroup group) {

        UserComment userComment;

        final String groupUUID = group.getUUID();
        String stationID = group.getStationID();

        if (currentItem == ITEM_MEDIA) {
            userComment = createMediaComment(groupUUID, stationID);
        } else {
            userComment = createFileComment(groupUUID, stationID);
        }

        mDialog = ProgressDialog.show(context, null, context.getString(R.string.send), true, true);
        mDialog.setCancelable(false);

        GroupRequestParam groupRequestParam = new GroupRequestParam(groupUUID, stationID);

        mGroupRepository.insertUserComment(groupRequestParam, userComment, new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                dismissDialog();

                ToastUtil.showToast(context,context.getString(R.string.finish_send_hint));

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                ToastUtil.showToast(context, operationResult.getResultMessage(context));

            }
        });

    }

    @NonNull
    private UserComment createFileComment(String groupUUID, String stationID) {
        UserComment userComment;

        List<AbstractRemoteFile> abstractRemoteFiles = mFileFragment.getSelectedFiles();

        List<AbstractFile> files = new ArrayList<>(abstractRemoteFiles.size());

        files.addAll(abstractRemoteFiles);

        userComment = new FileComment(Util.createLocalUUid(), currentUser, System.currentTimeMillis(), groupUUID, stationID, files);

        return userComment;
    }

    @NonNull
    private UserComment createMediaComment(String groupUUID, String stationID) {
        UserComment userComment;

        List<Media> medias = mSelectedMediasListener.getSelectedMedias();

        userComment = new MediaComment(Util.createLocalUUid(), currentUser, System.currentTimeMillis(), groupUUID, stationID, medias);

        return userComment;

    }

    private void startGroupContentActivity(Context context, String groupUUID) {

        Intent intent = new Intent(context, GroupContentActivity.class);
        intent.putExtra(GroupContentActivity.GROUP_UUID, groupUUID);

        context.startActivity(intent);
    }

    private List<Media> mSelectMedias;

    private List<String> mSelectMediaOriginalPhotoPaths;

    private ProgressDialog mDialog;

    private void shareMediaToOtherApp(final Context context) {

        mSelectMedias = new ArrayList<>(mSelectedMediasListener.getSelectedMedias());

        mSelectMediaOriginalPhotoPaths = new ArrayList<>(mSelectMedias.size());

        Iterator<Media> iterator = mSelectMedias.iterator();
        while (iterator.hasNext()) {
            Media media = iterator.next();
            String originalPhotoPath = media.getOriginalPhotoPath();

            if (originalPhotoPath.length() != 0) {
                mSelectMediaOriginalPhotoPaths.add(originalPhotoPath);
                iterator.remove();
            }
        }

        if (mSelectMedias.size() == 0) {
            FileUtil.sendShareToOtherApp(context, mSelectMediaOriginalPhotoPaths);
        } else {

            mDialog = ProgressDialog.show(context, null, String.format(context.getString(R.string.operating_title), context.getString(R.string.download_original_photo)), true, true);
            mDialog.setCancelable(false);

            mediaDataSourceRepository.downloadMedia(mSelectMedias, new BaseOperateDataCallbackImpl<Boolean>() {
                @Override
                public void onSucceed(Boolean data, OperationResult result) {
                    super.onSucceed(data, result);

                    handleDownloadMedia(context);

                }
            });

//            EventBus.getDefault().post(new RetrieveMediaOriginalPhotoRequestEvent(OperationType.GET, OperationTargetType.MEDIA_ORIGINAL_PHOTO, mSelectMedias));

        }
    }

    private void handleDownloadMedia(Context context) {
        if (mDialog == null || !mDialog.isShowing()) {
            return;
        }

        dismissDialog();

        String path;
        for (Media media : mSelectMedias) {
            path = media.getOriginalPhotoPath();

            if (!path.isEmpty())
                mSelectMediaOriginalPhotoPaths.add(path);
        }

        if (mSelectMediaOriginalPhotoPaths.isEmpty()) {
            ToastUtil.showToast(context, context.getString(R.string.download_original_photo_fail));
        } else {
            FileUtil.sendShareToOtherApp(context, mSelectMediaOriginalPhotoPaths);
        }
    }

    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }


    private void shareFileToOtherApp() {

        mFileFragment.shareSelectFilesToOtherApp();

    }

    @Override
    public void downloadFileBtnOnClick(Context context) {

        mFileFragment.downloadSelectItems();

        mQuitSelectModeCommand.execute();

//        quitSelectMode();

    }


}
