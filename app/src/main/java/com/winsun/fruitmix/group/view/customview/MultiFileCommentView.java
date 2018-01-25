package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FileTweetGroupItemBinding;
import com.winsun.fruitmix.databinding.MorePhotoMaskBinding;
import com.winsun.fruitmix.databinding.MultiFileCommentBinding;
import com.winsun.fruitmix.databinding.SingleFileBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.viewmodel.FileCommentViewModel;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

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

        FrameLayout[] frameLayouts = {binding.pic0, binding.pic1, binding.pic2, binding.pic0SecondRow, binding.pic1SecondRow, binding.pic2SecondRow};

        int totalSize;

        for (int i = 0; i < 6; i++) {

            frameLayouts[i].setVisibility(View.VISIBLE);
            frameLayouts[i].removeAllViews();

        }

        if (data instanceof MediaComment) {

            MediaComment comment = (MediaComment) data;

            List<Media> medias = comment.getMedias();

            totalSize = medias.size();

            int showSize = totalSize;

            if (totalSize > 6) {
                showSize = 6;
            }

            handleLayout(context, frameLayouts, showSize);

            int layoutNum = 0;

            for (int i = 0; i < showSize; i++) {

                for (; layoutNum < 6; layoutNum++) {

                    if (frameLayouts[layoutNum].getVisibility() != View.GONE)
                        break;

                }

                Media media = medias.get(i);

                HttpRequest httpRequest = media.getImageThumbUrl(context, data.getGroupUUID());

                SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), frameLayouts[layoutNum], false);

                frameLayouts[layoutNum].addView(singlePhotoBinding.getRoot());

                MediaUtil.setMediaImageUrl(media, singlePhotoBinding.coverImg, httpRequest, imageLoader);

                layoutNum++;

            }

            if (totalSize > 6) {

                MorePhotoMaskBinding morePhotoMaskBinding;

                FrameLayout addMaskLayout = isLeftModel ? frameLayouts[3] : frameLayouts[5];

                morePhotoMaskBinding = MorePhotoMaskBinding.inflate(LayoutInflater.from(context),
                        addMaskLayout, false);

                addMaskLayout.addView(morePhotoMaskBinding.getRoot());

                String text = "+" + (totalSize - 6);

                morePhotoMaskBinding.sizeTextview.setText(text);

            }

        } else if (data instanceof FileComment) {

            for (int i = 0; i < 5; i++) {

                frameLayouts[5 - i].setVisibility(View.GONE);

            }

            Util.setWidthAndHeight(frameLayouts[0], Util.dip2px(context, 269), Util.dip2px(context, 56));

            FileTweetGroupItemBinding fileTweetGroupItemBinding = FileTweetGroupItemBinding.inflate(LayoutInflater.from(context),
                    frameLayouts[0], false);

            frameLayouts[0].addView(fileTweetGroupItemBinding.getRoot());

            FileCommentViewModel fileCommentViewModel = fileTweetGroupItemBinding.getFileCommentViewModel();

            if (fileCommentViewModel == null)
                fileCommentViewModel = new FileCommentViewModel();

            fileTweetGroupItemBinding.setFileCommentViewModel(fileCommentViewModel);

            FileComment comment = (FileComment) data;

            List<AbstractFile> files = comment.getFiles();

            totalSize = files.size();

            AbstractFile firstFile = files.get(0);

            fileCommentViewModel.fileResID.set(firstFile.getFileTypeResID());
            fileCommentViewModel.shareFileSize.set("");

            String formatName = firstFile.getFormatName(context);

            String name = context.getString(R.string.share) +
                    "\"" +
                    formatName +
                    "\"" +
                    context.getString(R.string.more, context.getResources().getQuantityString(R.plurals.file, totalSize, totalSize));

            int start = name.indexOf(formatName);

            int end = start + formatName.length();

            SpannableString spannableString = new SpannableString(name);

            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            fileCommentViewModel.shareText.set(spannableString.toString());

        }


    }

    private void handleLayout(Context context, FrameLayout[] frameLayouts, int size) {

        if (size == 2) {

            int hideItemCount = 6 - size;

            for (int i = 0; i < hideItemCount; i++) {

                frameLayouts[5 - i].setVisibility(View.GONE);

            }

            int widthHeight = Util.dip2px(context, 134);

            for (int i = 0; i < size; i++) {

                Util.setWidthAndHeight(frameLayouts[i], widthHeight, widthHeight);

            }

        } else if (size == 4) {

            for (int i = 0; i < 6; i++) {

                if ((i - 2) % 3 == 0) {

                    frameLayouts[i].setVisibility(View.GONE);

                } else {

                    int widthHeight = Util.dip2px(context, 89);

                    Util.setWidthAndHeight(frameLayouts[i], widthHeight, widthHeight);

                }

            }

        } else {

            int hideItemCount = 6 - size;

            for (int i = 0; i < hideItemCount; i++) {

                frameLayouts[5 - i].setVisibility(View.GONE);

            }

            int widthHeight = Util.dip2px(context, 89);

            for (int i = 0; i < size; i++) {

                Util.setWidthAndHeight(frameLayouts[i], widthHeight, widthHeight);

            }

        }

    }


    private void hideItem(FrameLayout[] frameLayouts, int size) {
        if (size < 3) {

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
