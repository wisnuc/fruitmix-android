package com.winsun.fruitmix.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.MediaShareFragmentContract;
import com.winsun.fruitmix.mediaModule.MediaShareCommentActivity;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.presenter.MediaShareFragmentPresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaShareFragment implements MediaShareFragmentContract.MediaShareFragmentView {

    public static final String TAG = MediaShareFragment.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.share_list_framelayout)
    FrameLayout mShareListFrameLayout;
    @BindView(R.id.mainList)
    RecyclerView mainRecyclerView;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;

    private Map<String, List<Comment>> mMapKeyIsImageUUIDValueIsComments;
    private ShareRecyclerViewAdapter mAdapter;

    private MediaShareFragmentContract.MediaShareFragmentPresenter mPresenter;

    public MediaShareFragment(Activity activity_) {
        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(R.layout.share_list2, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        mAdapter = new ShareRecyclerViewAdapter();
        mainRecyclerView.setAdapter(mAdapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(containerActivity));

        mMapKeyIsImageUUIDValueIsComments = new HashMap<>();

        mPresenter = new MediaShareFragmentPresenterImpl(Injection.injectDataRepository(containerActivity));
        mPresenter.attachView(this);

        mPresenter.refreshData();
    }

    public MediaShareFragmentContract.MediaShareFragmentPresenter getPresenter() {
        return mPresenter;
    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        mPresenter.onMapSharedElements(names, sharedElements);

    }

    public void onActivityReenter(int resultCode, Intent data) {

        mPresenter.onActivityReenter(resultCode, data);

    }

    public View getView() {
        return view;
    }

    @Override
    public void showMediaShares(List<MediaShare> mediaShares) {

        mAdapter.setMediaShares(mediaShares);
        mAdapter.notifyDataSetChanged();

        mainRecyclerView.smoothScrollToPosition(0);

    }

    @Override
    public View findViewWithMedia(Media media) {

        String currentMediaTag;

        if (media == null)
            currentMediaTag = "";
        else
            currentMediaTag = mPresenter.loadImageThumbUrl(media);

        return mainRecyclerView.findViewWithTag(currentMediaTag);
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.postponeEnterTransition(containerActivity);
        mainRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mainRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mainRecyclerView.requestLayout();
                ActivityCompat.startPostponedEnterTransition(containerActivity);

                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
    }

    @Override
    public boolean isNetworkAlive() {
        return Util.getNetworkState(containerActivity);
    }

    @Override
    public void showNoNetwork() {
        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadingUI() {
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        mLoadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        mNoContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        mNoContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        mainRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        mainRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showDialog() {

    }

    @Override
    public void dismissDialog() {

    }

    @Override
    public void hideSoftInput() {

    }

    private class ShareRecyclerViewAdapter extends RecyclerView.Adapter<CardItemViewHolder> {

        private static final int VIEW_ALBUM_CARD_ITEM = 0;
        private static final int VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM = 1;
        private static final int VIEW_ONE_MEDIA_SHARE_CARD_ITEM = 2;

        private List<MediaShare> mMediaShares;

        ShareRecyclerViewAdapter() {

            setHasStableIds(true);
            mMediaShares = new ArrayList<>();
        }

        void setMediaShares(List<MediaShare> mediaShares) {

            mMediaShares.clear();
            mMediaShares.addAll(mediaShares);
        }

        @Override
        public CardItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(containerActivity).inflate(R.layout.share_list_cell, parent, false);

            switch (viewType) {
                case VIEW_ALBUM_CARD_ITEM:
                    return new AlbumCardItemViewHolder(view);

                case VIEW_ONE_MEDIA_SHARE_CARD_ITEM:
                    return new OneMediaShareCardItemViewHolder(view, mMapKeyIsImageUUIDValueIsComments);

                case VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM:
                    return new OneMoreMediaShareCardItemViewHolder(view);

            }
            return null;
        }

        @Override
        public void onBindViewHolder(CardItemViewHolder holder, int position) {

            MediaShare mediaShare = mMediaShares.get(position);

            holder.refreshView(mediaShare, position);

        }

        @Override
        public int getItemCount() {

            if (mMediaShares == null) return 0;
            return mMediaShares.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            MediaShare mediaShare = mMediaShares.get(position);
            if (mediaShare.isAlbum()) {
                return VIEW_ALBUM_CARD_ITEM;
            } else if (mediaShare.getMediaShareContentsListSize() == 1) {
                return VIEW_ONE_MEDIA_SHARE_CARD_ITEM;
            } else {
                return VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM;
            }
        }

    }

    class CardItemViewHolder extends RecyclerView.ViewHolder {

        MediaShare currentItem;

        TextView mAvatar;
        TextView lbNick;
        TextView lbTime;
        FrameLayout cardItemContentFrameLayout;
        CardView cardView;

        String nickName;

        CardItemViewHolder(View view) {

            super(view);

            mAvatar = (TextView) view.findViewById(R.id.avatar);
            lbNick = (TextView) view.findViewById(R.id.nick);
            lbTime = (TextView) view.findViewById(R.id.time);
            cardItemContentFrameLayout = (FrameLayout) view.findViewById(R.id.card_item_content_frame_layout);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }

        public void refreshView(MediaShare mediaShare, int position) {

            currentItem = mediaShare;

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) cardView.getLayoutParams();
            int margin = Util.dip2px(containerActivity, 8);

            if (position == 0) {
                layoutParams.setMargins(0, margin, 0, margin);
            } else {
                layoutParams.setMargins(0, 0, 0, margin);
            }
            cardView.setLayoutParams(layoutParams);

            lbTime.setText(Util.formatTime(containerActivity, Long.parseLong(currentItem.getTime())));

            String createUUID = currentItem.getCreatorUUID();

            User user = mPresenter.loadUser(createUUID);

            if (user != null) {

                nickName = user.getUserName();
                lbNick.setText(nickName);

                String avatar;
                avatar = user.getAvatar();
                if (avatar.equals("defaultAvatar.jpg")) {
                    avatar = user.getDefaultAvatar();
                }
                mAvatar.setText(avatar);

                mAvatar.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());
            }

        }

    }

    class AlbumCardItemViewHolder extends CardItemViewHolder {

        @BindView(R.id.cover_img)
        NetworkImageView ivCover;
        @BindView(R.id.album_title)
        TextView lbAlbumTitle;

        Media coverImg;

        AlbumCardItemViewHolder(View view) {
            super(view);

            View cardContentView = View.inflate(containerActivity, R.layout.album_card_item_layout, cardItemContentFrameLayout);

            ButterKnife.bind(this, cardContentView);
        }

        @Override
        public void refreshView(MediaShare mediaShare, int position) {
            super.refreshView(mediaShare, position);

            refreshViewAttributeWhenIsAlbum();
        }

        private void refreshViewAttributeWhenIsAlbum() {

            String title = currentItem.getTitle();

            String photoCount = String.valueOf(currentItem.getMediaShareContents().size());

            if (title.length() > 8) {
                title = title.substring(0, 8);

                title += containerActivity.getString(R.string.android_ellipsize);
            }

            title = String.format(containerActivity.getString(R.string.android_share_album_title), title, photoCount);

            lbAlbumTitle.setText(title);

            coverImg = mPresenter.loadMedia(currentItem.getCoverImageUUID());

            mPresenter.loadOriginalMediaToView(containerActivity, coverImg, ivCover);

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(containerActivity, AlbumContentActivity.class);
                    intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
                    intent.putExtra(Util.NEED_SHOW_MENU, false);
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    containerActivity.startActivity(intent);
                }
            });
        }
    }

    class OneMediaShareCardItemViewHolder extends CardItemViewHolder {

        @BindView(R.id.cover_img)
        NetworkImageView ivCover;
        @BindView(R.id.share_comment_textview)
        TextView mShareCommentTextView;
        @BindView(R.id.comment_layout)
        LinearLayout mCommentLayout;
        @BindView(R.id.share_comment_count_textview)
        TextView mShareCommentCountTextView;

        Map<String, List<Comment>> commentMap;

        Media itemImg;

        List<String> imageKeys;


        OneMediaShareCardItemViewHolder(View view, Map<String, List<Comment>> commentMap) {
            super(view);

            this.commentMap = commentMap;

            View cardContentView = View.inflate(containerActivity, R.layout.one_mediashare_card_item_layout, cardItemContentFrameLayout);

            ButterKnife.bind(this, cardContentView);
        }

        @Override
        public void refreshView(MediaShare mediaShare, int position) {
            super.refreshView(mediaShare, position);

            imageKeys = mediaShare.getMediaKeyInMediaShareContents();

            refreshViewAttributeWhenOneImage(commentMap);
        }

        private void refreshViewAttributeWhenOneImage(Map<String, List<Comment>> commentMap) {
            Log.d(TAG, "images[0]:" + imageKeys.get(0));

            itemImg = mPresenter.loadMedia(imageKeys.get(0));

            mPresenter.loadOriginalMediaToView(containerActivity, itemImg, ivCover);

            if (itemImg != null) {

                String uuid = itemImg.getUuid();
                if (commentMap.containsKey(uuid)) {
                    List<Comment> commentList = commentMap.get(uuid);
                    if (commentList.size() != 0) {
                        mShareCommentTextView.setText(String.format(containerActivity.getString(R.string.android_share_comment_text), nickName, commentList.get(0).getText()));
                    } else {
                        mShareCommentTextView.setText("");
                    }
                    mShareCommentCountTextView.setText(String.valueOf(commentList.size()));

                } else {
                    mShareCommentTextView.setText("");
                    mShareCommentCountTextView.setText("0");
                }

                if (!mShareCommentTextView.getText().equals("")) {
                    mShareCommentTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(containerActivity, containerActivity.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();

//                                    gotoMediaShareCommentActivity(false);
                        }
                    });
                }

                mCommentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(containerActivity, containerActivity.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();

//                                gotoMediaShareCommentActivity(true);
                    }
                });

            }

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Media> imageList = mPresenter.loadMedias(currentItem.getMediaKeyInMediaShareContents());

                    fillLocalCachePhotoData(imageList);

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                    intent.putExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, false);
                    intent.setClass(containerActivity, OriginalMediaActivity.class);

                    if (ivCover.isLoaded()) {

                        String transitionName = imageList.get(0).getKey();

                        ViewCompat.setTransitionName(ivCover, transitionName);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

                        containerActivity.startActivity(intent, options.toBundle());
                    } else {

                        intent.putExtra(Util.KEY_NEED_TRANSITION, false);
                        containerActivity.startActivity(intent);

                    }

                }
            });
        }

        private void gotoMediaShareCommentActivity(boolean showSoftInputWhenEnter) {
            Intent intent = new Intent(containerActivity, MediaShareCommentActivity.class);
            intent.putExtra(Util.IMAGE_KEY, imageKeys.get(0));
            intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
            intent.putExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, showSoftInputWhenEnter);

            String transitionName = String.valueOf(imageKeys.get(0));

            ViewCompat.setTransitionName(ivCover, transitionName);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

            containerActivity.startActivity(intent, options.toBundle());
        }
    }

    class OneMoreMediaShareCardItemViewHolder extends CardItemViewHolder {

        @BindView(R.id.pic_row1)
        View llPic1;
        @BindView(R.id.pic_row2)
        View llPic2;
        @BindView(R.id.pic_row3)
        View llPic3;
        @BindView(R.id.share_count_layout)
        RelativeLayout mShareCountLayout;
        @BindView(R.id.share_count_textview)
        TextView mShareCountTextView;
        @BindView(R.id.check_more_photos)
        TextView mCheckMorePhoto;

        NetworkImageView ivItems[];

        Media itemImg;

        List<String> imageKeys;

        OneMoreMediaShareCardItemViewHolder(View view) {
            super(view);

            View cardContentView = View.inflate(containerActivity, R.layout.one_more_mediashare_card_item_layout, cardItemContentFrameLayout);

            ButterKnife.bind(this, cardContentView);

            ivItems = new NetworkImageView[9];
            ivItems[0] = (NetworkImageView) view.findViewById(R.id.mainPic0);
            ivItems[1] = (NetworkImageView) view.findViewById(R.id.mainPic1);
            ivItems[2] = (NetworkImageView) view.findViewById(R.id.mainPic2);
            ivItems[3] = (NetworkImageView) view.findViewById(R.id.mainPic3);
            ivItems[4] = (NetworkImageView) view.findViewById(R.id.mainPic4);
            ivItems[5] = (NetworkImageView) view.findViewById(R.id.mainPic5);
            ivItems[6] = (NetworkImageView) view.findViewById(R.id.mainPic6);
            ivItems[7] = (NetworkImageView) view.findViewById(R.id.mainPic7);
            ivItems[8] = (NetworkImageView) view.findViewById(R.id.mainPic8);
        }

        @Override
        public void refreshView(MediaShare mediaShare, int position) {
            super.refreshView(mediaShare, position);

            imageKeys = mediaShare.getMediaKeyInMediaShareContents();

            refreshViewVisibilityWhenNotOneImage();

            setShareCountText();

            refreshViewAttributeWhenNotOneImage();
        }

        private void refreshViewVisibilityWhenNotOneImage() {
            if (imageKeys.size() <= 3) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.GONE);
                llPic3.setVisibility(View.GONE);

                mCheckMorePhoto.setVisibility(View.GONE);

            } else if (imageKeys.size() <= 6) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.GONE);

                mCheckMorePhoto.setVisibility(View.GONE);

            } else {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.VISIBLE);

                if (imageKeys.size() > 9) {
                    mCheckMorePhoto.setVisibility(View.VISIBLE);
                    mCheckMorePhoto.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    mCheckMorePhoto.getPaint().setAntiAlias(true);

                    mCheckMorePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(containerActivity, MoreMediaActivity.class);
                            intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
                            containerActivity.startActivity(intent);
                        }
                    });
                } else {
                    mCheckMorePhoto.setVisibility(View.GONE);
                }

            }

            mShareCountLayout.setVisibility(View.VISIBLE);
        }

        private void setShareCountText() {
            String shareCountText = String.format(containerActivity.getString(R.string.android_share_count), String.valueOf(imageKeys.size()));
            int start = shareCountText.indexOf(String.valueOf(imageKeys.size()));
            int end = start + String.valueOf(imageKeys.size()).length();
            SpannableStringBuilder builder = new SpannableStringBuilder(shareCountText);
            ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(containerActivity, R.color.light_black));
            ForegroundColorSpan beforeSpan = new ForegroundColorSpan(ContextCompat.getColor(containerActivity, R.color.light_gray));
            ForegroundColorSpan afterSpan = new ForegroundColorSpan(ContextCompat.getColor(containerActivity, R.color.light_gray));
            builder.setSpan(beforeSpan, 0, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            builder.setSpan(afterSpan, end, shareCountText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mShareCountTextView.setText(builder);
        }

        private void refreshViewAttributeWhenNotOneImage() {
            for (int i = 0; i < 9; i++) {
                ivItems[i].setVisibility(View.INVISIBLE);
            }

            int imageKeySize = imageKeys.size();

            int length = imageKeySize > 9 ? 9 : imageKeySize;

            for (int i = 0; i < length; i++) {

                ivItems[i].setVisibility(View.VISIBLE);

                itemImg = mPresenter.loadMedia(imageKeys.get(i));

                mPresenter.loadThumbMediaToView(containerActivity, itemImg, ivItems[i]);

                final int mItemPosition = i;

                ivItems[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<Media> imageList = mPresenter.loadMedias(currentItem.getMediaKeyInMediaShareContents());

                        fillLocalCachePhotoData(imageList);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, mItemPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                        intent.setClass(containerActivity, OriginalMediaActivity.class);

                        View transitionView = ivItems[mItemPosition];

                        if (((NetworkImageView) transitionView).isLoaded()) {

                            String transitionName = String.valueOf(imageList.get(mItemPosition).getKey());

                            ViewCompat.setTransitionName(ivItems[mItemPosition], transitionName);

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, transitionView, transitionName);

                            containerActivity.startActivity(intent, options.toBundle());

                        } else {

                            intent.putExtra(Util.KEY_NEED_TRANSITION, false);
                            containerActivity.startActivity(intent);
                        }
                    }
                });
            }
        }
    }

    private void fillLocalCachePhotoData(List<Media> imageList) {
        OriginalMediaActivity.setMediaList(imageList);
    }

    public void refreshRemoteComment() {

//        refreshComment(LocalCache.RemoteMediaCommentMapKeyIsImageUUID);

    }

    public void refreshLocalComment() {

//        refreshComment(LocalCache.LocalMediaCommentMapKeyIsImageUUID);

    }

    private void refreshComment(ConcurrentMap<String, List<Comment>> map) {

        for (Map.Entry<String, List<Comment>> entry : map.entrySet()) {
            List<Comment> comments = new ArrayList<>();

            String imageUUID = entry.getKey();

            comments.addAll(entry.getValue());

            mMapKeyIsImageUUIDValueIsComments.put(imageUUID, comments);
        }

        if (mMapKeyIsImageUUIDValueIsComments.size() != 0) {
            mAdapter.notifyDataSetChanged();
        }

    }
}

