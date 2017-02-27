package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.callback.FileOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.FileShareOperationCallback;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileShareFragmentContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/22.
 */

public class FileShareFragmentPresenterImpl implements FileShareFragmentContract.FileShareFragmentPresenter {

    private FileShareFragmentContract.FileShareFragmentView mView;

    private FileMainFragmentContract.FileMainFragmentPresenter fileMainFragmentPresenter;

    private List<AbstractRemoteFile> abstractRemoteFiles;
    private boolean remoteFileShareLoaded = false;

    private String currentFolderUUID;
    private String currentFolderName;

    private List<String> retrievedFolderUUIDList;
    private List<String> retrievedFolderNameList;

    private String currentUserUUID;

    private DataRepository mRepository;

    public FileShareFragmentPresenterImpl(FileMainFragmentContract.FileMainFragmentPresenter fileMainFragmentPresenter, DataRepository mRepository) {
        this.fileMainFragmentPresenter = fileMainFragmentPresenter;
        this.mRepository = mRepository;

        retrievedFolderUUIDList = new ArrayList<>();
        retrievedFolderNameList = new ArrayList<>();

        abstractRemoteFiles = new ArrayList<>();
    }

    @Override
    public void handleTitle() {

        if (handleBackPressedOrNot()) {

            fileMainFragmentPresenter.setTitleText(currentFolderName);

            fileMainFragmentPresenter.setNavigationIcon(R.drawable.ic_back);
            fileMainFragmentPresenter.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        } else {
            fileMainFragmentPresenter.setTitleText(mView.getString(R.string.file));
            fileMainFragmentPresenter.setNavigationIcon(R.drawable.menu);
            fileMainFragmentPresenter.setDefaultNavigationOnClickListener();
        }

    }

    @Override
    public boolean handleBackPressedOrNot() {

        return currentFolderUUID.equals(currentUserUUID);
    }

    @Override
    public void onResume() {

        if (!remoteFileShareLoaded) {

            currentUserUUID = mRepository.loadCurrentLoginUserFromMemory().getUuid();

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
                retrievedFolderNameList.add(mView.getString(R.string.file));
            }

            mRepository.loadRemoteFileShare(new FileShareOperationCallback.LoadFileShareCallback() {
                @Override
                public void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files) {

                    remoteFileShareLoaded = true;

                    handleLoadSucceed(files);
                }

                @Override
                public void onLoadFail(OperationResult result) {
                    mView.dismissLoadingUI();
                    mView.showNoContentUI();
                }
            });
        }

    }

    @Override
    public void onDestroyView() {
        remoteFileShareLoaded = false;
    }

    public void onBackPressed() {

        if (mView.isShowingLoadingUI()) {
            return;
        }

        mView.showLoadingUI();

        retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

        currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

        retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
        currentFolderName = retrievedFolderNameList.get(retrievedFolderNameList.size() - 1);

        if (handleBackPressedOrNot()) {

            mRepository.loadRemoteFileShare(new FileShareOperationCallback.LoadFileShareCallback() {
                @Override
                public void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files) {

                    remoteFileShareLoaded = true;

                    handleLoadSucceed(files);
                }

                @Override
                public void onLoadFail(OperationResult result) {
                    mView.dismissLoadingUI();
                    mView.showNoContentUI();
                }
            });

        } else {

            mRepository.loadRemoteFolderContent(currentFolderUUID, new FileOperationCallback.LoadFileOperationCallback() {
                @Override
                public void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files) {
                    handleLoadSucceed(files);
                }

                @Override
                public void onLoadFail(OperationResult result) {
                    mView.dismissLoadingUI();
                    mView.showNoContentUI();
                }
            });

        }

        handleTitle();

    }

    private void handleLoadSucceed(Collection<AbstractRemoteFile> files) {

        mView.dismissLoadingUI();

        if (files.isEmpty()) {
            mView.showNoContentUI();
            mView.dismissContentUI();
        } else {
            mView.dismissNoContentUI();
            mView.showContentUI();

            abstractRemoteFiles.clear();
            abstractRemoteFiles.addAll(files);
            mView.showContent(abstractRemoteFiles);
        }

    }

    @Override
    public void attachView(FileShareFragmentContract.FileShareFragmentView view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void handleBackEvent() {
        onBackPressed();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public String getFileShareOwnerName(AbstractRemoteFile file) {

        List<String> owners = file.getOwners();
        if (!owners.isEmpty()) {
            String owner = owners.get(0);

            User user = mRepository.loadUserFromMemory(owner);
            if (user != null)
                return user.getUserName();
            else
                return null;
        }

        return null;
    }

    @Override
    public void openFolder(AbstractRemoteFile abstractRemoteFile) {

        currentFolderUUID = abstractRemoteFile.getUuid();

        retrievedFolderUUIDList.add(currentFolderUUID);

        currentFolderName = abstractRemoteFile.getName();
        retrievedFolderNameList.add(currentFolderName);

        mView.showLoadingUI();

        mRepository.loadRemoteFolderContent(abstractRemoteFile.getUuid(), new FileOperationCallback.LoadFileOperationCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, Collection<AbstractRemoteFile> files) {

                handleLoadSucceed(files);

            }

            @Override
            public void onLoadFail(OperationResult result) {

                mView.dismissLoadingUI();
                mView.showNoContentUI();
            }
        });

        handleTitle();

    }
}
