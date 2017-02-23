package com.winsun.fruitmix.refactor.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.FileMainFragmentContract;
import com.winsun.fruitmix.refactor.contract.FileShareFragmentContract;
import com.winsun.fruitmix.refactor.presenter.FileShareFragmentPresenterImpl;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FileShareFragment implements FileShareFragmentContract.FileShareFragmentView {

    public static final String TAG = FileShareFragment.class.getSimpleName();

    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.file_share_recyclerview)
    RecyclerView fileShareRecyclerView;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;

    private FileShareRecyclerAdapter fileShareRecyclerAdapter;

    private BaseActivity baseActivity;

    private FileShareFragmentContract.FileShareFragmentPresenter mPresenter;

    private View view;

    public FileShareFragment(BaseActivity activity, FileMainFragmentContract.FileMainFragmentPresenter mainFragmentPresenter) {

        baseActivity = activity;

        view = LayoutInflater.from(baseActivity).inflate(R.layout.fragment_file_share, null);

        ButterKnife.bind(this, view);

        fileShareRecyclerAdapter = new FileShareRecyclerAdapter();

        fileShareRecyclerView.setAdapter(fileShareRecyclerAdapter);
        fileShareRecyclerView.setLayoutManager(new LinearLayoutManager(baseActivity));

        noContentImageView.setImageResource(R.drawable.no_file);

        mPresenter = new FileShareFragmentPresenterImpl(mainFragmentPresenter, Injection.injectDataRepository());
        mPresenter.attachView(this);
        mPresenter.onResume();
    }

    public FileShareFragmentContract.FileShareFragmentPresenter getPresenter() {
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
    public void showContent(List<AbstractRemoteFile> files) {

        fileShareRecyclerAdapter.setData(files);
        fileShareRecyclerAdapter.notifyDataSetChanged();
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
        fileShareRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        fileShareRecyclerView.setVisibility(View.INVISIBLE);
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

    private class FileShareRecyclerAdapter extends RecyclerView.Adapter<BaseFileShareRecyclerViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        private List<AbstractRemoteFile> abstractRemoteFiles;

        public void setData(List<AbstractRemoteFile> files) {
            abstractRemoteFiles = files;
        }

        @Override
        public BaseFileShareRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            BaseFileShareRecyclerViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileShareRecyclerAdapterViewHolder(view);
                    break;
                case VIEW_FOLDER:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_folder_item_layout, parent, false);
                    viewHolder = new FolderShareRecyclerAdapterViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(baseActivity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileShareRecyclerAdapterViewHolder(view);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseFileShareRecyclerViewHolder holder, int position) {
            holder.refreshView(abstractRemoteFiles.get(position), position);
        }

        @Override
        public int getItemCount() {
            return abstractRemoteFiles.size();
        }

        @Override
        public int getItemViewType(int position) {
            return abstractRemoteFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;
        }
    }

    private abstract class BaseFileShareRecyclerViewHolder extends RecyclerView.ViewHolder {

        private BaseFileShareRecyclerViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void refreshView(AbstractRemoteFile file, int position);
    }

    class FolderShareRecyclerAdapterViewHolder extends BaseFileShareRecyclerViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.remote_folder_item_layout)
        LinearLayout remoteFolderItemLayout;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;
        @BindView(R.id.owner)
        TextView ownerTextView;

        FolderShareRecyclerAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(final AbstractRemoteFile abstractRemoteFile, int position) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(baseActivity, 8), Util.dip2px(baseActivity, 16), 0);
                contentLayout.setLayoutParams(layoutParams);
            }

            fileName.setText(abstractRemoteFile.getName());

            setOwner(abstractRemoteFile, ownerTextView);

            remoteFolderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mPresenter.openFolder(abstractRemoteFile);

                }
            });

        }

    }

    class FileShareRecyclerAdapterViewHolder extends BaseFileShareRecyclerViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_time)
        TextView fileTime;
        @BindView(R.id.item_menu)
        ImageView itemMenu;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;
        @BindView(R.id.owner)
        TextView ownerTextView;

        FileShareRecyclerAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(AbstractRemoteFile abstractRemoteFile, int position) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(baseActivity, 8), Util.dip2px(baseActivity, 16), 0);
                contentLayout.setLayoutParams(layoutParams);

            }

            itemMenu.setVisibility(View.GONE);

            setOwner(abstractRemoteFile, ownerTextView);

            fileName.setText(abstractRemoteFile.getName());
            fileTime.setText(abstractRemoteFile.getTimeDateText());

        }

    }

    private void setOwner(AbstractRemoteFile abstractRemoteFile, TextView ownerTextView) {

        String name = mPresenter.getFileShareOwnerName(abstractRemoteFile);
        if (name != null) {

            ownerTextView.setVisibility(View.VISIBLE);
            ownerTextView.setText(name);
        }

    }

}
