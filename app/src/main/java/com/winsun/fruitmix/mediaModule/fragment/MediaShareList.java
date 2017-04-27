package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.mediaModule.AlbumPicContentActivity;
import com.winsun.fruitmix.mediaModule.MediaShareCommentActivity;
import com.winsun.fruitmix.mediaModule.MoreMediaActivity;
import com.winsun.fruitmix.mediaModule.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.EnterPatternPathMotion;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.ReturnPatternPathMotion;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaShareList implements Page, IShowHideFragmentListener {

    public static final String TAG = MediaShareList.class.getSimpleName();

    private OnMediaFragmentInteractionListener listener;

    private Activity containerActivity;
    private View view;

    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.share_list_framelayout)
    FrameLayout mShareListFrameLayout;
    @BindView(R.id.mainRecylerView)
    RecyclerView mainRecyclerView;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

    private List<MediaShare> mediaShareList;
    private Map<String, List<Comment>> mMapKeyIsImageUUIDValueIsComments;
    private ShareRecyclerViewAdapter mAdapter;

    private ImageLoader mImageLoader;
    private Bundle reenterState;

    private boolean mShowPhoto = false;

    private List<IPhotoListListener> mPhotoListListeners;

    public MediaShareList(Activity activity_, OnMediaFragmentInteractionListener listener) {
        containerActivity = activity_;

        this.listener = listener;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(R.layout.share_list2, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        noContentTextView.setText(containerActivity.getString(R.string.no_media_shares));

//        mainRecyclerView.addOnScrollListener(new ShareRecycleViewScrollListener());

        mAdapter = new ShareRecyclerViewAdapter();
        mainRecyclerView.setAdapter(mAdapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(containerActivity));

        mMapKeyIsImageUUIDValueIsComments = new HashMap<>();

        mediaShareList = new ArrayList<>();

        mPhotoListListeners = new ArrayList<>();
    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.remove(listListener);
    }

    @Override
    public void show() {
        MobclickAgent.onPageStart("MediaShareFragment");
    }

    @Override
    public void hide() {
        MobclickAgent.onPageEnd("MediaShareFragment");
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(containerActivity);

    }

    private void reloadList() {

        mediaShareList.clear();

        fillMediaShareList(mediaShareList);

        sortMediaShareList(mediaShareList);

    }

    private void sortMediaShareList(List<MediaShare> mediaShareList) {
        Collections.sort(mediaShareList, new Comparator<MediaShare>() {
            @Override
            public int compare(MediaShare lhs, MediaShare rhs) {
                long time1 = Long.parseLong(lhs.getTime());
                long time2 = Long.parseLong(rhs.getTime());

                if (time1 < time2)
                    return 1;
                else if (time1 > time2)
                    return -1;
                else return 0;

            }
        });
    }

    private void fillMediaShareList(List<MediaShare> mediaShareList) {
        for (MediaShare mediaShare : LocalCache.LocalMediaShareMapKeyIsUUID.values()) {
            if (isMediaSharePublic(mediaShare)) {
                mediaShareList.add(mediaShare);
            }
        }

        for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {
            if (isMediaSharePublic(mediaShare)) {
                mediaShareList.add(mediaShare);
            }
        }
    }

    private boolean isMediaSharePublic(MediaShare mediaShare) {
        return (LocalCache.RemoteUserMapKeyIsUUID.size() == 1 && FNAS.userUUID != null && mediaShare.getCreatorUUID().equals(FNAS.userUUID)) || (mediaShare.getViewersListSize() != 0 && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID()));
    }

    public void showPhoto() {

        mShowPhoto = true;

        if (mainRecyclerView.getVisibility() == View.VISIBLE)
            mAdapter.notifyDataSetChanged();

    }

    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        if (!Util.isRemoteMediaShareLoaded()) {
            return;
        }

        initImageLoader();

        reloadList();

        mLoadingLayout.setVisibility(View.GONE);
        if (mediaShareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainRecyclerView.setVisibility(View.GONE);
        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mainRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();

        }

        mMapKeyIsImageUUIDValueIsComments.clear();

        refreshRemoteComment();
        refreshLocalComment();
    }

    public void onDidAppear() {

        refreshView();
        mainRecyclerView.smoothScrollToPosition(0);

    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        if (reenterState != null) {

            int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
            int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);
            String currentMediaShareTime = reenterState.getString(Util.CURRENT_MEDIASHARE_TIME);
            if (initialPhotoPosition != currentPhotoPosition) {

                names.clear();
                sharedElements.clear();

                int currentMediaSharePosition = findShareItemPosition(currentMediaShareTime);

                List<String> currentMediaUUIDs = mediaShareList.get(currentMediaSharePosition).getMediaUUIDInMediaShareContents();

                String currentMediaUUID = currentMediaUUIDs.get(currentPhotoPosition);

                View currentSharedElementView = mainRecyclerView.findViewWithTag(findMediaTagByMediaKey(currentMediaUUID));

                names.add(currentMediaUUID);
                sharedElements.put(currentMediaUUID, currentSharedElementView);

            }

        }
        reenterState = null;

    }

    public void onActivityReenter(int resultCode, Intent data) {
        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

        if (initialPhotoPosition != currentPhotoPosition) {

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
    }

    private int findShareItemPosition(String currentMediaShareTime) {

        int returnPosition = 0;
        int size = mediaShareList.size();
        for (int i = 0; i < size; i++) {
            MediaShare mediaShare = mediaShareList.get(i);
            String mediaShareTime = mediaShare.getTime();
            if (currentMediaShareTime.equals(mediaShareTime)) {
                returnPosition = i;
                break;
            }
        }

        return returnPosition;
    }

    private String findMediaTagByMediaKey(String imageKey) {
        String currentMediaTag;
        Media currentMedia = LocalCache.RemoteMediaMapKeyIsUUID.get(imageKey);
        if (currentMedia == null) {
            currentMedia = LocalCache.findMediaInLocalMediaMap(imageKey);
        }

        if (currentMedia == null)
            currentMediaTag = "";
        else
            currentMediaTag = currentMedia.getImageThumbUrl(containerActivity);

        return currentMediaTag;
    }

    public View getView() {
        return view;
    }

    private class ShareRecyclerViewAdapter extends RecyclerView.Adapter<CardItemViewHolder> {

        private static final int VIEW_ALBUM_CARD_ITEM = 0;
        private static final int VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM = 1;
        private static final int VIEW_ONE_MEDIA_SHARE_CARD_ITEM = 2;

        ShareRecyclerViewAdapter() {

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

            MediaShare mediaShare = mediaShareList.get(position);

            holder.refreshView(mediaShare, position);

        }

        @Override
        public int getItemCount() {

            if (mediaShareList == null) return 0;
            return mediaShareList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            MediaShare mediaShare = mediaShareList.get(position);
            if (mediaShare.isAlbum()) {
                return VIEW_ALBUM_CARD_ITEM;
            } else if (mediaShare.getMediaContentsListSize() == 1) {
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

        View mSpacingLayout;

        String nickName;

        CardItemViewHolder(View view) {

            super(view);

            mAvatar = (TextView) view.findViewById(R.id.avatar);
            lbNick = (TextView) view.findViewById(R.id.nick);
            lbTime = (TextView) view.findViewById(R.id.time);
            cardItemContentFrameLayout = (FrameLayout) view.findViewById(R.id.card_item_content_frame_layout);
            cardView = (CardView) view.findViewById(R.id.card_view);

            mSpacingLayout = view.findViewById(R.id.spacing_layout);
        }

        public void refreshView(MediaShare mediaShare, int position) {

/*            if (position == 0) {
                mSpacingLayout.setVisibility(View.VISIBLE);
            } else {
                mSpacingLayout.setVisibility(View.GONE);
            }*/

            currentItem = mediaShare;

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
            int margin = Util.dip2px(containerActivity, 8);

            if (position == 0) {
                layoutParams.setMargins(0, margin, 0, margin);
            } else {
                layoutParams.setMargins(0, 0, 0, margin);
            }
            cardView.setLayoutParams(layoutParams);

            lbTime.setText(Util.formatTime(containerActivity, Long.parseLong(currentItem.getTime())));

            String createUUID = currentItem.getCreatorUUID();
            if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(createUUID)) {

                User user = LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreatorUUID());

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

            String photoCount = String.valueOf(currentItem.getMediaContentsListSize());

            if (title.length() > 20) {
                title = title.substring(0, 20);

                title += containerActivity.getString(R.string.android_ellipsize);
            }

            title = String.format(containerActivity.getString(R.string.android_share_album_title), title, photoCount, containerActivity.getResources().getQuantityString(R.plurals.photo, currentItem.getMediaContentsListSize()));

            lbAlbumTitle.setText(title);

            String key = currentItem.getCoverImageUUID();

            if (key.isEmpty()) {
                coverImg = null;
            } else {
                coverImg = LocalCache.findMediaInLocalMediaMap(key);
                if (coverImg == null) {
                    coverImg = LocalCache.RemoteMediaMapKeyIsUUID.get(key);
                }
            }

            ivCover.setBackgroundResource(R.drawable.default_place_holder);
//            ivCover.setBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

            Log.d(TAG, "refreshView: coverImg: " + coverImg + " mShowPhoto: " + mShowPhoto);

            if (coverImg != null && mShowPhoto) {

                String imageUrl = coverImg.getImageOriginalUrl(containerActivity);
                mImageLoader.setShouldCache(!coverImg.isLocal());
                ivCover.setTag(imageUrl);
                ivCover.setDefaultImageResId(R.drawable.default_place_holder);
//                ivCover.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                ivCover.setOrientationNumber(coverImg.getOrientationNumber());
                ivCover.setImageUrl(imageUrl, mImageLoader);

            } else {
                ivCover.setDefaultImageResId(R.drawable.default_place_holder);
//                ivCover.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                ivCover.setImageUrl(null, mImageLoader);
            }

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(containerActivity, AlbumPicContentActivity.class);
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

            imageKeys = mediaShare.getMediaUUIDInMediaShareContents();

            refreshViewAttributeWhenOneImage(commentMap);
        }

        private void refreshViewAttributeWhenOneImage(Map<String, List<Comment>> commentMap) {
            Log.d(TAG, "images[0]:" + imageKeys.get(0));

            itemImg = LocalCache.findMediaInLocalMediaMap(imageKeys.get(0));
            if (itemImg == null) {
                itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageKeys.get(0));
            }

            ivCover.setBackgroundResource(R.drawable.default_place_holder);
//            ivCover.setBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

            if (itemImg != null) {

                Log.d(TAG, "refreshView: itemImg: " + itemImg + " mShowPhoto: " + mShowPhoto);

                if (mShowPhoto) {
                    String imageUrl = itemImg.getImageOriginalUrl(containerActivity);
                    mImageLoader.setShouldCache(!itemImg.isLocal());
                    ivCover.setTag(imageUrl);
                    ivCover.setDefaultImageResId(R.drawable.default_place_holder);

//                    ivCover.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                    ivCover.setOrientationNumber(itemImg.getOrientationNumber());
                    ivCover.setImageUrl(imageUrl, mImageLoader);
                }

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

            } else {
                ivCover.setDefaultImageResId(R.drawable.default_place_holder);

//                ivCover.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                ivCover.setImageUrl(null, mImageLoader);
            }

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Media> imageList = getImgList(currentItem.getMediaUUIDInMediaShareContents());

                    fillLocalCachePhotoData(imageList);

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                    intent.putExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, false);
                    intent.setClass(containerActivity, PhotoSliderActivity.class);

                    if (ivCover.isLoaded()) {

                        String transitionName = imageList.get(0).getKey();

                        ViewCompat.setTransitionName(ivCover, transitionName);

                        Pair mediaPair = new Pair<>((View) ivCover, transitionName);

                        Pair[] pairs = Util.createSafeTransitionPairs(containerActivity, true, mediaPair);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, pairs);

//                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

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

            Pair mediaPair = new Pair<>((View) ivCover, transitionName);

            Pair[] pairs = Util.createSafeTransitionPairs(containerActivity, true, mediaPair);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, pairs);

//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

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

            imageKeys = mediaShare.getMediaUUIDInMediaShareContents();

            refreshViewVisibilityWhenNotOneImage();

            setShareCountText();

            refreshViewAttributeWhenNotOneImage();
        }

        private void refreshViewVisibilityWhenNotOneImage() {
            if (imageKeys.size() <= 3) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.GONE);
                llPic3.setVisibility(View.GONE);

//                mCheckMorePhoto.setVisibility(View.GONE);

            } else if (imageKeys.size() <= 6) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.GONE);

//                mCheckMorePhoto.setVisibility(View.GONE);

            } else {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.VISIBLE);

//                if (imageKeys.size() > 9) {
//                    mCheckMorePhoto.setVisibility(View.VISIBLE);
//                    mCheckMorePhoto.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//                    mCheckMorePhoto.getPaint().setAntiAlias(true);
//
//                    mCheckMorePhoto.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(containerActivity, MoreMediaActivity.class);
//                            intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
//                            containerActivity.startActivity(intent);
//                        }
//                    });
//                } else {
//                    mCheckMorePhoto.setVisibility(View.GONE);
//                }

            }

            mShareCountLayout.setVisibility(View.VISIBLE);

            mShareCountLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(containerActivity, MoreMediaActivity.class);
                    intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
                    containerActivity.startActivity(intent);
                }
            });
        }

        private void setShareCountText() {

            String shareCountText = String.format(containerActivity.getString(R.string.android_share_count), String.valueOf(imageKeys.size()), containerActivity.getResources().getQuantityString(R.plurals.photo, imageKeys.size()));

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
                itemImg = LocalCache.findMediaInLocalMediaMap(imageKeys.get(i));

                if (itemImg == null)
                    itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageKeys.get(i));

                ivItems[i].setBackgroundResource(R.drawable.default_place_holder);

//                ivItems[i].setBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                Log.d(TAG, "refreshViewAttributeWhenNotOneImage: ivItems[" + i + "]: " + ivItems[i] + " mShowPhoto: " + mShowPhoto);

                if (itemImg != null && mShowPhoto) {

                    String imageUrl = itemImg.getImageThumbUrl(containerActivity);
                    mImageLoader.setShouldCache(!itemImg.isLocal());

                    if (itemImg.isLocal())
                        ivItems[i].setOrientationNumber(itemImg.getOrientationNumber());

                    ivItems[i].setTag(imageUrl);
                    ivItems[i].setDefaultImageResId(R.drawable.default_place_holder);

//                    ivItems[i].setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                    ivItems[i].setImageUrl(imageUrl, mImageLoader);

                } else {
                    ivItems[i].setDefaultImageResId(R.drawable.default_place_holder);

//                    ivItems[i].setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));

                    ivItems[i].setImageUrl(null, mImageLoader);
                }

                final int mItemPosition = i;

                ivItems[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<Media> imageList = getImgList(currentItem.getMediaUUIDInMediaShareContents());

                        fillLocalCachePhotoData(imageList);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, mItemPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        View transitionView = ivItems[mItemPosition];

                        if (((NetworkImageView) transitionView).isLoaded()) {

                            Util.setMotion(mItemPosition, 3);

                            String transitionName = String.valueOf(imageList.get(mItemPosition).getKey());

                            ViewCompat.setTransitionName(ivItems[mItemPosition], transitionName);

                            Pair mediaPair = new Pair<>((View) ivItems[mItemPosition], transitionName);

                            Pair[] pairs = Util.createSafeTransitionPairs(containerActivity, true, mediaPair);

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, pairs);

//                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, transitionView, transitionName);

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

    private class ShareRecycleViewScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                for (IPhotoListListener listener : mPhotoListListeners) {
                    listener.onPhotoListScrollFinished();
                }

            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (dy > 0) {

                for (IPhotoListListener listener : mPhotoListListeners) {
                    listener.onPhotoListScrollDown();
                }

            } else if (dy < 0) {

                for (IPhotoListListener listener : mPhotoListListeners) {
                    listener.onPhotoListScrollUp();
                }

            }
        }
    }


    private void fillLocalCachePhotoData(List<Media> imageList) {
        PhotoSliderActivity.setMediaList(imageList);
    }

    private List<Media> getImgList(List<String> imageKeys) {
        ArrayList<Media> picList;
        Media picItem;
        Media picItemRaw;

        picList = new ArrayList<>();

        for (String aStArr : imageKeys) {

            picItemRaw = LocalCache.findMediaInLocalMediaMap(aStArr);

            if (picItemRaw == null) {

                picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);

                if (picItemRaw == null) {
                    picItem = new Media();

                } else {

                    picItem = picItemRaw;
                    picItem.setLocal(false);
                }

            } else {

                picItem = picItemRaw;
                picItem.setLocal(true);

            }

            picItem.setSelected(false);

            picList.add(picItem);
        }

        return picList;
    }


    public void refreshRemoteComment() {

        refreshComment(LocalCache.RemoteMediaCommentMapKeyIsImageUUID);

    }

    public void refreshLocalComment() {

        refreshComment(LocalCache.LocalMediaCommentMapKeyIsImageUUID);

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

