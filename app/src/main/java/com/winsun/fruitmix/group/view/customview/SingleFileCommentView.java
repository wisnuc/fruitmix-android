package com.winsun.fruitmix.group.view.customview;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FileTweetGroupItemBinding;
import com.winsun.fruitmix.databinding.SingleFileCommentBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.viewmodel.FileCommentViewModel;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.list.TweetContentListActivity;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import java.util.Collections;

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
    protected View generateContentView(Context context, ViewGroup parent) {

        binding = SingleFileCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return binding.getRoot();
    }

    @Override
    protected void refreshContent(final Context context, final View toolbar, final UserComment data, boolean isLeftModel) {

        FrameLayout frameLayout = binding.singleFileFramelayout;

        if (data instanceof MediaComment) {

            frameLayout.removeAllViews();

            MediaComment comment = (MediaComment) data;

            final SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), frameLayout, false);

//            final NewPhotoGridlayoutItemBinding newPhotoGridlayoutItemBinding = NewPhotoGridlayoutItemBinding.inflate(LayoutInflater.from(context),
//                    frameLayout,false);

            View rootView = singlePhotoBinding.getRoot();

            frameLayout.addView(rootView);

            int widthHeight = Util.dip2px(context, 134);

            Util.setWidthAndHeight(singlePhotoBinding.container, widthHeight, widthHeight);

            final NetworkImageView networkImageView = singlePhotoBinding.coverImg;

            final Media media = comment.getMedias().get(0);

            HttpRequest httpRequest = media.getImageThumbUrl(InjectHttp.provideHttpRequestFactory(context), new GroupRequestParam(data.getGroupUUID(), data.getStationID()));

            httpRequest.setUrl(httpRequest.getUrl() + "&randomUUID=" + Util.createLocalUUid());

            MediaUtil.setMediaImageUrl(media, networkImageView, httpRequest, imageLoader);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PhotoSliderActivity.startPhotoSliderActivityWithMedias(toolbar, (Activity) context, Collections.singletonList(media), data.getGroupUUID(),
                            data.getStationID(), 3, networkImageView, media);

                }
            });


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

            long totalFileSize = 0;

            if (file instanceof AbstractRemoteFile)
                totalFileSize += ((AbstractRemoteFile) file).getSize();

            if (totalFileSize != 0)
                fileCommentViewModel.shareFileSize.set(Formatter.formatFileSize(context, totalFileSize));

            String formatName = file.getName();

            String name = context.getString(R.string.share) +
                    context.getString(R.string.file)
                    + " " +
                    formatName;

//            int start = name.indexOf(formatName);
//
//            int end = start + formatName.length();
//
//            SpannableString spannableString = new SpannableString(name);
//
//            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//            fileCommentViewModel.shareText.set(spannableString.toString());

            fileCommentViewModel.shareText.set(name);

            fileTweetGroupItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TweetContentListActivity.startListActivity(data, context);

                }
            });

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
