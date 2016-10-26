package com.winsun.fruitmix.file;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.file.model.AbstractLocalFile;
import com.winsun.fruitmix.file.model.LocalFolder;
import com.winsun.fruitmix.file.model.LocalFile;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalFileActivity extends AppCompatActivity {

    @BindView(R.id.file_recyclerview)
    RecyclerView fileRecyclerView;

    Context mContext;

    List<AbstractLocalFile> abstractLocalFiles;
    FileRecyclerAdapter fileRecyclerAdapter;

    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file);

        ButterKnife.bind(this);

        mContext = this;

        abstractLocalFiles = new ArrayList<>();

        fileRecyclerAdapter = new FileRecyclerAdapter();
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        fileRecyclerView.setAdapter(fileRecyclerAdapter);

        if (FileUtil.checkExternalStorageState()) {
            String path = FileUtil.getExternalStorageDirectoryPath();

            currentPath = path;

            fillAbstractFiles(path);
        }

    }

    @Override
    public void onBackPressed() {

        if(currentPath.equals(FileUtil.getExternalStorageDirectoryPath())){
            super.onBackPressed();
        }else {

            currentPath = new File(currentPath).getParent();
            fillAbstractFiles(currentPath);

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
                }
                abstractLocalFile.setName(file.getName());
                abstractLocalFile.setPath(file.getAbsolutePath());
                abstractLocalFiles.add(abstractLocalFile);
            }

            fileRecyclerAdapter.notifyDataSetChanged();
        }

    }


    class FileRecyclerAdapter extends RecyclerView.Adapter<FileViewHolder> {

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.local_file_item_layout, parent, false);
            return new FileViewHolder(view);

        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {

            holder.refreshView(position);

        }

        @Override
        public int getItemCount() {
            return abstractLocalFiles.size();
        }

    }

    class FileViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_icon)
        ImageView itemIcon;
        @BindView(R.id.item_name)
        TextView itemName;
        @BindView(R.id.file_item_layout)
        LinearLayout fileItemLayout;

        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(int position) {

            final AbstractLocalFile abstractLocalFile = abstractLocalFiles.get(position);
            if (abstractLocalFile.isFolder()) {
                itemIcon.setImageResource(R.drawable.folder_icon);

                fileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        currentPath = abstractLocalFile.getPath();

                        fillAbstractFiles(currentPath);
                    }
                });

            } else {
                itemIcon.setImageResource(R.drawable.file_icon);

                fileItemLayout.setOnClickListener(null);
            }

            itemName.setText(abstractLocalFile.getName());

        }
    }

}
