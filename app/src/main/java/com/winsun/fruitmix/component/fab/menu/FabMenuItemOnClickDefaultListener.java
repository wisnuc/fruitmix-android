package com.winsun.fruitmix.component.fab.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.file.view.fragment.FileFragment;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2018/2/28.
 */

public class FabMenuItemOnClickDefaultListener implements FabMenuItemOnClickListener {

    private NewPhotoList mNewPhotoList;
    private FileFragment mFileFragment;

    private MediaDataSourceRepository mediaDataSourceRepository;

    private AbstractCommand mQuitSelectModeCommand;

    public FabMenuItemOnClickDefaultListener(NewPhotoList newPhotoList, FileFragment fileFragment,
                                             MediaDataSourceRepository mediaDataSourceRepository,
                                             AbstractCommand quitSelectModeCommand) {
        mNewPhotoList = newPhotoList;
        mFileFragment = fileFragment;
        this.mediaDataSourceRepository = mediaDataSourceRepository;

        mQuitSelectModeCommand = quitSelectModeCommand;
    }

    @Override
    public void systemShareBtnOnClick(final Context context, final int currentItem) {

        if (!Util.isNetworkConnected(context)) {
            Toast.makeText(context,context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
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
                handleShareToOtherApp(currentItem,context);

                mQuitSelectModeCommand.execute();

//                quitSelectMode();

            }

            @Override
            public void unExecute() {
            }
        };

        new ShareMenuBottomDialogFactory(shareInAppCommand, shareToOtherAppCommand).createDialog(context).show();


    }

    private void handleShareToOtherApp(int currentItem,Context context) {

        if (currentItem == ITEM_MEDIA)
            shareMediaToOtherApp(context);
        else if (currentItem == ITEM_FILE)
            shareFileToOtherApp();

    }

    private List<Media> mSelectMedias;

    private List<String> mSelectMediaOriginalPhotoPaths;

    private ProgressDialog mDialog;

    private void shareMediaToOtherApp(final Context context) {

        mSelectMedias = new ArrayList<>(mNewPhotoList.getSelectedMedias());

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
            mDialog.setCanceledOnTouchOutside(false);

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
            Toast.makeText(context, context.getString(R.string.download_original_photo_fail), Toast.LENGTH_SHORT).show();
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
