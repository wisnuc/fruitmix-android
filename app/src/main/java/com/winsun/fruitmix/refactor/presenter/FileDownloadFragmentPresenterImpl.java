package com.winsun.fruitmix.refactor.presenter;

import android.content.Context;
import android.content.Intent;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.DeleteDownloadedFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.FileDownloadOperationCallback;
import com.winsun.fruitmix.refactor.contract.FileDownloadFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.util.FileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/22.
 */

public class FileDownloadFragmentPresenterImpl implements FileDownloadFragmentContract.FileDownloadFragmentPresenter, OnViewSelectListener {

    private FileDownloadFragmentContract.FileDownloadFragmentView mView;

    private List<FileDownloadItem> downloadingItems;
    private List<FileDownloadItem> downloadedItems;

    private boolean selectMode = false;

    private List<String> selectDownloadedItemUUID;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand nullCommand;

    private DataRepository mRepository;

    private FileMainFragmentContract.FileMainFragmentPresenter mainFragmentPresenter;

    private FileDownloadOperationCallback.FileDownloadStateChangedCallback fileDownloadStateChangedCallback;

    public FileDownloadFragmentPresenterImpl(DataRepository mRepository, FileMainFragmentContract.FileMainFragmentPresenter mainFragmentPresenter) {
        this.mRepository = mRepository;
        this.mainFragmentPresenter = mainFragmentPresenter;

        downloadedItems = new ArrayList<>();
        downloadingItems = new ArrayList<>();

        selectDownloadedItemUUID = new ArrayList<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();
    }

    @Override
    public void handleTitle() {

        mainFragmentPresenter.setTitleText(mView.getString(R.string.file));
        mainFragmentPresenter.setNavigationIcon(R.drawable.menu);
        mainFragmentPresenter.setDefaultNavigationOnClickListener();

    }

    @Override
    public boolean handleBackPressedOrNot() {
        return selectMode;
    }

    @Override
    public void fileMainMenuOnClick() {
        mView.getBottomSheetDialog(getMainMenuItem()).show();
    }

    private List<BottomMenuItem> getMainMenuItem() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (selectMode) {

            BottomMenuItem clearSelectItem = new BottomMenuItem(mView.getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            AbstractCommand macroCommand = new MacroCommand();
            macroCommand.addCommand(new AbstractCommand() {
                @Override
                public void execute() {
                    mRepository.deleteDownloadedFileRecords(selectDownloadedItemUUID, new FileDownloadOperationCallback.DeleteDownloadedFilesCallback() {
                        @Override
                        public void onFinished() {
                            loadDownloadedFile();
                        }
                    });
                }

                @Override
                public void unExecute() {

                }
            });
            macroCommand.addCommand(showUnSelectModeViewCommand);

            BottomMenuItem deleteSelectItem = new BottomMenuItem(mView.getString(R.string.delete_text), macroCommand);

            bottomMenuItems.add(deleteSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(mView.getString(R.string.choose_text), showSelectModeViewCommand);

            bottomMenuItems.add(selectItem);

        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(mView.getString(R.string.cancel), nullCommand);

        bottomMenuItems.add(cancelMenuItem);

        return bottomMenuItems;
    }

    @Override
    public void registerFileDownloadStateChangedListener() {

        if(fileDownloadStateChangedCallback == null){
            fileDownloadStateChangedCallback = new FileDownloadOperationCallback.FileDownloadStateChangedCallback() {
                @Override
                public void onStateChanged(DownloadState state) {

                    if (state == DownloadState.NO_ENOUGH_SPACE) {
                        mView.showNoEnoughSpaceToast();
                    } else {
                        loadDownloadedFile();
                    }

                }
            };
        }

        mRepository.registerFileDownloadedStateChanged(fileDownloadStateChangedCallback);

    }

    @Override
    public void unregisterFileDownloadStateChangedListener() {
        mRepository.unregisterFileDownloadedStateChanged(fileDownloadStateChangedCallback);
    }

    @Override
    public boolean checkIsInSelectedFiles(String fileUUID) {
        return selectDownloadedItemUUID.contains(fileUUID);
    }

    @Override
    public void toggleFileInSelectedFile(String fileUUID) {
        if (selectDownloadedItemUUID.contains(fileUUID)) {
            selectDownloadedItemUUID.remove(fileUUID);
        } else {
            selectDownloadedItemUUID.add(fileUUID);
        }
    }

    @Override
    public void openFileIfDownloaded(Context context, FileDownloadItem fileDownloadItem) {

        if (fileDownloadItem.getDownloadState().equals(DownloadState.FINISHED)) {
            FileUtil.openAbstractRemoteFile(context, fileDownloadItem.getFileName());
        }
    }

    @Override
    public void loadDownloadedFile() {

        mRepository.loadDownloadedFiles(new FileDownloadOperationCallback.LoadDownloadedFilesCallback() {
            @Override
            public void onLoaded(Collection<FileDownloadItem> fileDownloadItems) {

                filterFileDownloadItems(fileDownloadItems);

                mView.showDownloadingContent(downloadingItems);
                mView.showDownloadedContent(downloadedItems, selectMode);
            }
        });

    }

    private void filterFileDownloadItems(Collection<FileDownloadItem> fileDownloadItems) {

        downloadingItems.clear();
        downloadedItems.clear();

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {

            DownloadState downloadState = fileDownloadItem.getDownloadState();

            if (downloadState.equals(DownloadState.FINISHED) || downloadState.equals(DownloadState.ERROR)) {
                downloadedItems.add(fileDownloadItem);
            } else {
                downloadingItems.add(fileDownloadItem);
            }

        }

    }

    @Override
    public void attachView(FileDownloadFragmentContract.FileDownloadFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {

        if (selectMode) {
            refreshSelectMode(false);
        }

    }

    private void refreshSelectMode(boolean selectMode) {
        this.selectMode = selectMode;

        if (!selectMode) {
            selectDownloadedItemUUID.clear();
        }

        mView.showDownloadingContent(downloadingItems);
        mView.showDownloadedContent(downloadedItems, selectMode);

    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void selectMode() {
        refreshSelectMode(true);
    }

    @Override
    public void unSelectMode() {
        refreshSelectMode(false);
    }
}
