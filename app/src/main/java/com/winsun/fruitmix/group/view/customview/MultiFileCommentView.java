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
import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.SCloudTokenContainer;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.databinding.FileTweetGroupItemBinding;
import com.winsun.fruitmix.databinding.MorePhotoMaskBinding;
import com.winsun.fruitmix.databinding.MultiFileCommentBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.RetryFailUserCommentStrategy;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.viewmodel.FileCommentViewModel;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.list.TweetContentListActivity;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class MultiFileCommentView extends UserCommentView implements SCloudTokenContainer {

    private MultiFileCommentBinding binding;

    private ImageLoader imageLoader;

    private String mSCloudToken = "";

    public MultiFileCommentView(RetryFailUserCommentStrategy retryFailUserCommentStrategy,ImageLoader imageLoader) {
        super(retryFailUserCommentStrategy);
        this.imageLoader = imageLoader;
    }

    @Override
    protected View generateContentView(Context context, ViewGroup parent) {

        binding = MultiFileCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return binding.getRoot();
    }

    @Override
    protected void refreshContent(final Context context, final View toolbar, final UserComment data, boolean isLeftModel) {

        FrameLayout[] frameLayouts = {binding.pic0, binding.pic1, binding.pic2, binding.pic0SecondRow, binding.pic1SecondRow, binding.pic2SecondRow};

        int totalSize;

        for (int i = 0; i < 6; i++) {

            frameLayouts[i].setVisibility(View.VISIBLE);
            frameLayouts[i].removeAllViews();

        }

        if (data instanceof MediaComment) {

            MediaComment comment = (MediaComment) data;

            final List<Media> medias = comment.getMedias();

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

                final Media media = medias.get(i);

                View root;
                final NetworkImageView networkImageView;

                final SinglePhotoBinding singlePhotoBinding = SinglePhotoBinding.inflate(LayoutInflater.from(context), frameLayouts[layoutNum], false);

                root = singlePhotoBinding.getRoot();

                networkImageView = singlePhotoBinding.coverImg;

                frameLayouts[layoutNum].addView(root);

/*                final NewPhotoGridlayoutItemBinding newPhotoGridlayoutItemBinding = NewPhotoGridlayoutItemBinding.inflate(LayoutInflater.from(context),
                        frameLayouts[layoutNum],false);

                frameLayouts[layoutNum].addView(newPhotoGridlayoutItemBinding.getRoot());

                MediaUtil.setMediaImageUrl(media, newPhotoGridlayoutItemBinding.photoIv, httpRequest, imageLoader);*/

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PhotoSliderActivity.startPhotoSliderActivityWithMedias(toolbar, (Activity) context, medias, data.getGroupUUID(), data.getStationID(),
                                3, networkImageView, media);

                    }
                });


                TokenManager tokenManager = InjectSCloudTokenManager.provideInstance(context);

                BaseDataOperator baseDataOperator = InjectBaseDataOperator.provideInstance(context,
                        tokenManager, this, new RefreshTokenRetryStrategy(tokenManager));

                baseDataOperator.preConditionCheck(true, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        handleGetSCloudToken(context, data, media, singlePhotoBinding);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        handleGetSCloudToken(context, data, media, singlePhotoBinding);

                    }
                });


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

                morePhotoMaskBinding.maskContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        TweetContentListActivity.startListActivity(data, context);

                    }
                });

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

            long totalFileSize = 0;

            for (AbstractFile file : files) {

                if (file instanceof AbstractRemoteFile)
                    totalFileSize += ((AbstractRemoteFile) file).getSize();

            }

            if (totalFileSize != 0)
                fileCommentViewModel.shareFileSize.set(Formatter.formatFileSize(context, totalFileSize));

//            String formatName = firstFile.getFormatName(context);

            String name = context.getString(R.string.share) +
                    context.getString(R.string.file) + " " +
                    totalSize + context.getString(R.string.file_unit);

//            int start = name.indexOf(formatName);
//
//            int end = start + formatName.length();
//
//            SpannableString spannableString = new SpannableString(name);
//
//            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            fileCommentViewModel.shareText.set(name);

            fileTweetGroupItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TweetContentListActivity.startListActivity(data, context);

                }
            });

        }


    }

    private void handleGetSCloudToken(Context context, UserComment data, Media media, SinglePhotoBinding singlePhotoBinding) {
        HttpRequest httpRequest = media.getImageThumbUrl(InjectHttp.provideHttpRequestFactory(context),
                new GroupRequestParam(data.getGroupUUID(), data.getStationID()), mSCloudToken);

        if (!media.isLocal())
            httpRequest.setUrl(httpRequest.getUrl() + "&randomUUID=" + Util.createLocalUUid());

        MediaUtil.setMediaImageUrl(media, singlePhotoBinding.coverImg, httpRequest, imageLoader);
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

    @Override
    public void setSCloudToken(String sCloudToken) {
        mSCloudToken = sCloudToken;
    }
}
