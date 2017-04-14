package com.winsun.fruitmix.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.FileFragmentContract;
import com.winsun.fruitmix.contract.FileMainFragmentContract;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.presenter.FileFragmentPresenterImpl;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileFragment implements FileFragmentContract.FileFragmentView {

    public static final String TAG = FileFragment.class.getSimpleName();

    @BindView(R.id.file_recyclerview)
    RecyclerView fileRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private BaseActivity baseActivity;

    private View view;

    private FileFragmentContract.FileFragmentPresenter mPresenter;

    public FileFragment(BaseActivity activity, FileMainFragmentContract.FileMainFragmentPresenter fileMainFragmentPresenter) {
        baseActivity = activity;

        // Inflate the layout for this fragment

        view = LayoutInflater.from(baseActivity).inflate(R.layout.fragment_file, null);

        ButterKnife.bind(this, view);

        fileRecyclerViewAdapter = new FileRecyclerViewAdapter();
        fileRecyclerView.setAdapter(fileRecyclerViewAdapter);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(baseActivity));

        noContentImageView.setImageResource(R.drawable.no_file);

        mPresenter = new FileFragmentPresenterImpl(fileMainFragmentPresenter, Injection.injectDataRepository(baseActivity), FileUtil.getInstance());
        mPresenter.attachView(this);

    }

    public FileFragmentContract.FileFragmentPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onDestroyView() {

        baseActivity = null;

        mPresenter.onDestroyView();
        mPresenter.detachView();
    }

    @Override
    public String getString(int resID) {
        return baseActivity.getString(resID);
    }

    @Override
    public boolean isShowingLoadingUI() {
        return loadingLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showContents(List<AbstractRemoteFile> files, boolean selectMode) {

        fileRecyclerViewAdapter.setData(files, selectMode);
        fileRecyclerViewAdapter.notifyDataSetChanged();

    }

    @Override
    public Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {
        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(baseActivity);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }

    @Override
    public void showOpenFileFailToast() {
        Toast.makeText(baseActivity, getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoWriteExternalStoragePermission() {
        Toast.makeText(baseActivity, getString(R.string.android_no_write_external_storage_permission), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(baseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(baseActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            mPresenter.downloadSelectedFiles();
        }

    }

    @Override
    public View getView() {
        return view;
    }


    @Override
    public boolean isNetworkAlive() {
        return baseActivity.isNetworkAlive();
    }

    @Override
    public void showNoNetwork() {
        baseActivity.showNoNetwork();
    }

    @Override
    public void showLoadingUI() {
        loadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        noContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        noContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        fileRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        fileRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showDialog() {
        baseActivity.showDialog();
    }

    @Override
    public void dismissDialog() {
        baseActivity.dismissDialog();
    }

    @Override
    public void hideSoftInput() {
        baseActivity.hideSoftInput();
    }

    class FileRecyclerViewAdapter extends RecyclerView.Adapter<BaseFileRecyclerViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        private List<AbstractRemoteFile> abstractRemoteFiles;
        private boolean selectMode;

        public void setData(List<AbstractRemoteFile> abstractRemoteFiles, boolean selectMode) {
            this.abstractRemoteFiles = abstractRemoteFiles;
            this.selectMode = selectMode;
        }

        @Override
        public int getItemCount() {
            return abstractRemoteFiles == null ? 0 : abstractRemoteFiles.size();
        }

        @Override
        public BaseFileRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            BaseFileRecyclerViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileViewHolder(view);
                    break;
                case VIEW_FOLDER:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_folder_item_layout, parent, false);
                    viewHolder = new FolderViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileViewHolder(view);
            }


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseFileRecyclerViewHolder holder, int position) {
            holder.refreshView(abstractRemoteFiles.get(position), position, selectMode);
        }

        @Override
        public int getItemViewType(int position) {

            return abstractRemoteFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;

        }

    }

    private abstract class BaseFileRecyclerViewHolder extends RecyclerView.ViewHolder {

        private BaseFileRecyclerViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void refreshView(AbstractRemoteFile file, int position, boolean selectMode);
    }

    class FolderViewHolder extends BaseFileRecyclerViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.folder_icon_bg)
        ImageView folderIconBg;
        @BindView(R.id.remote_folder_item_layout)
        LinearLayout folderItemLayout;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;

        FolderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(final AbstractRemoteFile abstractRemoteFile, int position, boolean selectMode) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(baseActivity, 8), Util.dip2px(baseActivity, 16), 0);
                contentLayout.setLayoutParams(layoutParams);

            }


            fileName.setText(abstractRemoteFile.getName());

            if (selectMode) {
                folderIconBg.setVisibility(View.VISIBLE);
            } else {
                folderIconBg.setVisibility(View.INVISIBLE);
            }

            folderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mPresenter.openAbstractRemoteFolder(abstractRemoteFile);

                }
            });

        }
    }


    class FileViewHolder extends BaseFileRecyclerViewHolder {

        @BindView(R.id.file_icon_bg)
        ImageView fileIconBg;
        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_time)
        TextView fileTime;
        @BindView(R.id.remote_file_item_layout)
        LinearLayout remoteFileItemLayout;
        @BindView(R.id.item_menu_layout)
        ViewGroup itemMenuLayout;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;

        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshView(final AbstractRemoteFile abstractRemoteFile, int position, boolean selectMode) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(baseActivity, 8), Util.dip2px(baseActivity, 16), 0);
                contentLayout.setLayoutParams(layoutParams);

            }


            fileTime.setText(abstractRemoteFile.getTimeDateText());
            fileName.setText(abstractRemoteFile.getName());

            if (selectMode) {

                itemMenuLayout.setVisibility(View.GONE);
                fileIconBg.setVisibility(View.VISIBLE);

                toggleFileIconBgResource(abstractRemoteFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPresenter.toggleFileInSelectedFile(abstractRemoteFile);
                        toggleFileIconBgResource(abstractRemoteFile);

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(null);

            } else {


                itemMenuLayout.setVisibility(View.VISIBLE);
                fileIconBg.setVisibility(View.INVISIBLE);
                fileIcon.setVisibility(View.VISIBLE);

                itemMenuLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mPresenter.itemMenuOnClick(baseActivity, abstractRemoteFile);

                    }
                });

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPresenter.fileItemOnClick(baseActivity, abstractRemoteFile);

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        mPresenter.addSelectedFiles(Collections.singletonList(abstractRemoteFile));
                        mPresenter.refreshSelectMode(true);

                        return true;
                    }
                });

            }

        }

        private void toggleFileIconBgResource(AbstractRemoteFile abstractRemoteFile) {
            if (mPresenter.checkIsInSelectedFiles(abstractRemoteFile)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
            }
        }


    }
}
