package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.databinding.MultiFileCommentBinding;
import com.winsun.fruitmix.databinding.SingleFileBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.MediaUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class MultiFileCommentView extends UserCommentView {

    private MultiFileCommentBinding binding;

    private ImageLoader imageLoader;

    public MultiFileCommentView(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    @Override
    protected View generateContentView(Context context) {

        binding = MultiFileCommentBinding.inflate(LayoutInflater.from(context), null, false);

        return binding.getRoot();
    }

    @Override
    protected void refreshContent(Context context, UserComment data, boolean isLeftModel) {

        FrameLayout[] frameLayouts = {binding.pic0, binding.pic1, binding.pic2, binding.pic3, binding.pic4, binding.pic5};

        int size;

        if (data instanceof MediaComment) {

            MediaComment comment = (MediaComment) data;

            List<Media> medias = comment.getMedias();

            size = medias.size();

            hideItem(frameLayouts, size);

            for (int i = 0; i < medias.size(); i++) {

                Media media = medias.get(i);

                HttpRequest httpRequest = media.getImageThumbUrl(context);

                frameLayouts[i].removeAllViews();

                SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), frameLayouts[i], false);

                frameLayouts[i].addView(singlePhotoBinding.getRoot());

                MediaUtil.setMediaImageUrl(media,singlePhotoBinding.coverImg, httpRequest, imageLoader);

            }

        } else if (data instanceof FileComment) {

            FileComment comment = (FileComment) data;

            List<AbstractFile> files = comment.getFiles();

            size = files.size();

            hideItem(frameLayouts, size);

            for (int i = 0; i < files.size(); i++) {

                frameLayouts[i].removeAllViews();

                AbstractFile file = files.get(i);

                SingleFileBinding singleFileBinding = SingleFileBinding.inflate(LayoutInflater.from(context), frameLayouts[i], false);

                frameLayouts[i].addView(singleFileBinding.getRoot());

                singleFileBinding.setFile(file);

            }

        }


    }

    private void hideItem(FrameLayout[] frameLayouts, int size) {
        if (size < 3) {

            binding.picRow2.setVisibility(View.GONE);

            int hideItemCount = 3 - size;

            for (int i = 0; i < hideItemCount; i++) {

                frameLayouts[2 - i].setVisibility(View.GONE);

            }

        } else if (size < 6) {

            int hideItemCount = 6 - size;

            for (int i = 0; i < hideItemCount; i++) {

                frameLayouts[5 - i].setVisibility(View.GONE);

            }

        }
    }
}
