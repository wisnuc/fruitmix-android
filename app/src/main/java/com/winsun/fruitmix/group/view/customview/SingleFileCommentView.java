package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FileTweetGroupItemBinding;
import com.winsun.fruitmix.databinding.SingleFileBinding;
import com.winsun.fruitmix.databinding.SingleFileCommentBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.viewmodel.FileCommentViewModel;
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

        if (data instanceof MediaComment) {

            frameLayout.removeAllViews();

            MediaComment comment = (MediaComment) data;

            SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), frameLayout, false);

            frameLayout.addView(singlePhotoBinding.getRoot());

            int widthHeight = Util.dip2px(context, 134);

            Util.setWidthAndHeight(singlePhotoBinding.container, widthHeight, widthHeight);

            NetworkImageView networkImageView = singlePhotoBinding.coverImg;

            Media media = comment.getMedias().get(0);

            HttpRequest httpRequest = media.getImageThumbUrl(context,data.getGroupUUID());

            MediaUtil.setMediaImageUrl(media, networkImageView, httpRequest, imageLoader);

        } else if (data instanceof FileComment) {

            frameLayout.removeAllViews();

            FileComment fileComment = (FileComment) data;

            AbstractFile file = fileComment.getFiles().get(0);

            FileTweetGroupItemBinding fileTweetGroupItemBinding = FileTweetGroupItemBinding.inflate(LayoutInflater.from(context),
                    frameLayout, false);

            frameLayout.addView(fileTweetGroupItemBinding.getRoot());

            FileCommentViewModel fileCommentViewModel = fileTweetGroupItemBinding.getFileCommentViewModel();

            if (fileCommentViewModel == null)
                fileCommentViewModel = new FileCommentViewModel();

            fileTweetGroupItemBinding.setFileCommentViewModel(fileCommentViewModel);

            fileCommentViewModel.fileResID.set(file.getFileTypeResID());
            fileCommentViewModel.shareFileSize.set("");

            String formatName = file.getFormatName(context);

            String name = context.getString(R.string.share) +
                    "\"" +
                    formatName +
                    "\"" +
                    context.getString(R.string.file);

            int start = name.indexOf(formatName);

            int end = start + formatName.length();

            SpannableString spannableString = new SpannableString(name);

            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            fileCommentViewModel.shareText.set(spannableString.toString());

//            SingleFileBinding singleFileBinding = SingleFileBinding.inflate(LayoutInflater.from(context), frameLayout, false);
//
//            frameLayout.addView(singleFileBinding.getRoot());
//
//            Util.setHeight(singleFileBinding.container, Util.dip2px(context, 250));
//
//            singleFileBinding.setFile(fileComment.getFiles().get(0));

        }

    }

}
