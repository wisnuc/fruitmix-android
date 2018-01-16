package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.databinding.SingleFileBinding;
import com.winsun.fruitmix.databinding.SingleFileCommentBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.PhotoComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/8/8.
 */

public class SingleFileCommentView extends UserCommentView {

    private ImageLoader imageLoader;

    public SingleFileCommentView(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    private SingleFileCommentBinding binding;


    @Override
    protected View generateContentView(Context context) {

        binding = SingleFileCommentBinding.inflate(LayoutInflater.from(context), null, false);

        return binding.getRoot();
    }

    @Override
    protected void refreshContent(Context context, UserComment data, boolean isLeftModel) {

        FrameLayout frameLayout = binding.singleFileFramelayout;

        if (data instanceof PhotoComment) {

            PhotoComment comment = (PhotoComment) data;

            SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), null, false);

            frameLayout.removeAllViews();

            frameLayout.addView(singlePhotoBinding.getRoot());

            Util.setHeight(singlePhotoBinding.container,Util.dip2px(context, 250));

            NetworkImageView networkImageView = singlePhotoBinding.coverImg;

            Media media = comment.getMedias().get(0);

            HttpRequest httpRequest = media.getImageThumbUrl(context);

            MediaUtil.setMediaImageUrl(media,networkImageView, httpRequest, imageLoader);

        } else if (data instanceof FileComment) {

            FileComment fileComment = (FileComment) data;

            SingleFileBinding singleFileBinding = SingleFileBinding.inflate(LayoutInflater.from(context), null, false);

            frameLayout.removeAllViews();

            frameLayout.addView(singleFileBinding.getRoot());

            Util.setHeight(singleFileBinding.container,Util.dip2px(context, 250));

            singleFileBinding.setFile(fileComment.getFiles().get(0));

        }

    }

    private void setHeight(Context context, ViewGroup container, FrameLayout.LayoutParams layoutParams) {

        layoutParams.height = Util.dip2px(context, 250);

        container.setLayoutParams(layoutParams);
    }
}
