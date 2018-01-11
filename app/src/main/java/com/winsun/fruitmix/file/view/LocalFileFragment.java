package com.winsun.fruitmix.file.view;

import android.app.Activity;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityLocalFileBinding;
import com.winsun.fruitmix.databinding.RemoteFileItemLayoutBinding;
import com.winsun.fruitmix.databinding.RemoteFolderItemLayoutBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractLocalFile;
import com.winsun.fruitmix.file.data.model.LocalFolder;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.view.viewmodel.FileItemViewModel;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseBindingViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFileFragment {

    private Context mContext;

    private List<AbstractLocalFile> abstractLocalFiles;
    private FileRecyclerAdapter fileRecyclerAdapter;

    private String currentPath;

    private View view;

    private boolean selectMode;

    private List<AbstractFile> selectedFiles;

    private List<String> alreadySelectedFileArrayList;

    public LocalFileFragment(Activity activity) {

        ActivityLocalFileBinding binding = ActivityLocalFileBinding.inflate(LayoutInflater.from(activity), null, false);

        mContext = activity;

        RecyclerView fileRecyclerView = binding.fileRecyclerview;

        view = binding.getRoot();

        abstractLocalFiles = new ArrayList<>();

        selectedFiles = new ArrayList<>();

        fileRecyclerAdapter = new FileRecyclerAdapter();
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        fileRecyclerView.setAdapter(fileRecyclerAdapter);

    }

    public void setAlreadySelectedFileArrayList(List<String> alreadySelectedFileArrayList) {

        this.alreadySelectedFileArrayList = alreadySelectedFileArrayList;

    }

    public void refreshView() {
        if (FileUtil.checkExternalStorageState()) {
            String path = FileUtil.getExternalStorageDirectoryPath();

            currentPath = path;

            fillAbstractFiles(path);
        }
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    public List<AbstractFile> getSelectFiles() {
        return selectedFiles;
    }

    public View getView() {
        return view;
    }

    public void onDestroy() {

        mContext = null;
    }

    public boolean onBackPressed() {

        if (currentPath.equals(FileUtil.getExternalStorageDirectoryPath())) {
            return false;
        } else {

            currentPath = new File(currentPath).getParent();
            fillAbstractFiles(currentPath);

            return true;
        }

    }

    private void fillAbstractFiles(String path) {

        abstractLocalFiles.clear();

        File[] files = new File(path).listFiles();

        if (files != null) {

            for (File file : files) {

                AbstractLocalFile abstractLocalFile;
                if (file.isDirectory()) {
                    abstractLocalFile = new LocalFolder();

                } else {
                    abstractLocalFile = new LocalFile();

                    abstractLocalFile.setFileTypeResID(FileUtil.getFileTypeResID(file.getName()));
                }

                abstractLocalFile.setName(file.getName());
                abstractLocalFile.setPath(file.getAbsolutePath());
                abstractLocalFile.setTime(String.valueOf(file.lastModified()));

                if (alreadySelectedFileArrayList != null && alreadySelectedFileArrayList.contains(abstractLocalFile.getName()))
                    selectedFiles.add(abstractLocalFile);

                abstractLocalFiles.add(abstractLocalFile);

            }

            fileRecyclerAdapter.notifyDataSetChanged();
        }

    }


    class FileRecyclerAdapter extends RecyclerView.Adapter<BaseBindingViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        @Override
        public BaseBindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;
            BaseBindingViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:

                    binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FileViewHolder(binding);

                    break;
                case VIEW_FOLDER:

                    binding = RemoteFolderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FolderViewHolder(binding);
                    break;
                default:
                    binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FileViewHolder(binding);
            }


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseBindingViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.file, abstractLocalFiles.get(position));

            holder.refreshView(position);

            holder.getViewDataBinding().executePendingBindings();

        }

        @Override
        public int getItemCount() {
            return abstractLocalFiles.size();
        }

        @Override
        public int getItemViewType(int position) {
            return abstractLocalFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;
        }
    }

    class FolderViewHolder extends BaseBindingViewHolder {

        LinearLayout folderItemLayout;

        RelativeLayout contentLayout;

        private RemoteFolderItemLayoutBinding binding;

        FolderViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (RemoteFolderItemLayoutBinding) viewDataBinding;

            contentLayout = binding.contentLayout;

            folderItemLayout = binding.remoteFolderItemLayout;
        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                Util.setMargin(contentLayout,0,Util.dip2px(mContext,8),Util.dip2px(mContext,16),0);

            }

            final AbstractLocalFile abstractLocalFile = abstractLocalFiles.get(position);

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(selectMode);

            folderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    currentPath = abstractLocalFile.getPath();

                    fillAbstractFiles(currentPath);
                }
            });

            binding.setFileItemViewModel(fileItemViewModel);

        }
    }

    class FileViewHolder extends BaseBindingViewHolder {

        LinearLayout remoteFileItemLayout;

        RelativeLayout contentLayout;

        ImageButton itemMenu;

        private RemoteFileItemLayoutBinding binding;

        FileViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (RemoteFileItemLayoutBinding) viewDataBinding;

            contentLayout = binding.contentLayout;
            remoteFileItemLayout = binding.remoteFileItemLayout;
            itemMenu = binding.itemMenu;

        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                Util.setMargin(contentLayout,0,Util.dip2px(mContext,8),0,0);

            }

            final AbstractLocalFile abstractLocalFile = abstractLocalFiles.get(position);

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(selectMode);

            if (selectMode) {

                toggleFileIconBgResource(fileItemViewModel, abstractLocalFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        toggleFileInSelectedFile(abstractLocalFile);
                        toggleFileIconBgResource(binding.getFileItemViewModel(), abstractLocalFile);

                    }
                });

            }

            binding.setFileItemViewModel(fileItemViewModel);

        }

        private void toggleFileIconBgResource(FileItemViewModel fileItemViewModel, AbstractFile abstractFile) {
            if (selectedFiles.contains(abstractFile)) {

                fileItemViewModel.fileIconBg.set(R.drawable.check_circle_selected);
                fileItemViewModel.showFileIcon.set(false);


            } else {

                fileItemViewModel.fileIconBg.set(R.drawable.round_circle);
                fileItemViewModel.showFileIcon.set(true);

            }
        }

        private void toggleFileInSelectedFile(AbstractFile abstractFile) {
            if (selectedFiles.contains(abstractFile)) {
                selectedFiles.remove(abstractFile);
            } else {
                selectedFiles.add(abstractFile);
            }
        }

    }


}
