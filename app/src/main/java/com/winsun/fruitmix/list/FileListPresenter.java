package com.winsun.fruitmix.list;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.databinding.RemoteFileItemLayoutBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.view.viewmodel.FileItemViewModel;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FileListPresenter {

    private List<AbstractFile> mAbstractFiles;

    public FileListPresenter(List<AbstractFile> abstractFiles) {
        mAbstractFiles = abstractFiles;
    }

    public void refreshView(RecyclerView recyclerView){

        FileListAdapter fileListAdapter = new FileListAdapter();

        recyclerView.setAdapter(fileListAdapter);

        fileListAdapter.setAbstractFiles(mAbstractFiles);
        fileListAdapter.notifyDataSetChanged();
    }

    private class FileListAdapter extends RecyclerView.Adapter<FileListViewHolder>{

        private List<AbstractFile> mAbstractFiles;

        public FileListAdapter() {
            mAbstractFiles = new ArrayList<>();
        }

        public void setAbstractFiles(List<AbstractFile> abstractFiles) {
            mAbstractFiles.clear();
            mAbstractFiles.addAll(abstractFiles);
        }

        @Override
        public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RemoteFileItemLayoutBinding binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent,false);

            return new FileListViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(FileListViewHolder holder, int position) {

            AbstractFile file = mAbstractFiles.get(position);

            holder.getViewDataBinding().setVariable(BR.file, file);

            holder.refreshView(file);

            holder.getViewDataBinding().executePendingBindings();

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mAbstractFiles.size();
        }
    }

    private class FileListViewHolder extends BindingViewHolder{

        public FileListViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        void refreshView(AbstractFile file){

            RemoteFileItemLayoutBinding binding = (RemoteFileItemLayoutBinding) getViewDataBinding();

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(false);

            fileItemViewModel.showFileIcon.set(true);

            binding.setFileItemViewModel(fileItemViewModel);
        }

    }

}
