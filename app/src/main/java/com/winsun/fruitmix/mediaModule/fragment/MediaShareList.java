package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
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
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaShareList implements Page {

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
    @BindView(R.id.mainList)
    ListView mainListView;

    private List<MediaShare> mediaShareList;
    private Map<String, List<Comment>> mMapKeyIsImageUUIDValueIsComments;
    private ShareListViewAdapter mAdapter;

    private ImageLoader mImageLoader;
    private Bundle reenterState;

    public MediaShareList(Activity activity_, OnMediaFragmentInteractionListener listener) {
        containerActivity = activity_;

        this.listener = listener;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.share_list2, null);

        ButterKnife.bind(this, view);

        initImageLoader();

        mAdapter = new ShareListViewAdapter(this);
        mainListView.setAdapter(mAdapter);

        mMapKeyIsImageUUIDValueIsComments = new HashMap<>();

    }

    private void initImageLoader() {
        RequestQueue mRequestQueue = RequestQueueInstance.getInstance(containerActivity).getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
    }

    private void reloadList() {

        List<MediaShare> mediaShareList = new ArrayList<>();

        fillMediaShareList(mediaShareList);

        sortMediaShareList(mediaShareList);

        this.mediaShareList = mediaShareList;

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
            if (!mediaShare.isArchived() && mediaShare.getViewersListSize() != 0 && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID())) {
                mediaShareList.add(mediaShare);
            }
        }

        for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {
            if (!mediaShare.isArchived() && mediaShare.getViewersListSize() != 0 && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID())) {
                mediaShareList.add(mediaShare);
            }
        }
    }


    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        if (!listener.isRemoteMediaShareLoaded()) {
            return;
        }

        reloadList();

        mMapKeyIsImageUUIDValueIsComments.clear();

        mLoadingLayout.setVisibility(View.GONE);
        if (mediaShareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainListView.setVisibility(View.GONE);
        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mainListView.setVisibility(View.VISIBLE);
            ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();

        }

        refreshRemoteComment();
        refreshLocalComment();
    }

    public void onDidAppear() {

        refreshView();

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

                List<String> currentMediaUUIDs = mediaShareList.get(currentMediaSharePosition).getMediaDigestInMediaShareContents();

                String currentMediaUUID = currentMediaUUIDs.get(currentPhotoPosition);

                View currentSharedElementView = mainListView.findViewWithTag(findMediaTagByMediaUUID(currentMediaUUID));

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

        ActivityCompat.postponeEnterTransition(containerActivity);
        mainListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mainListView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mainListView.requestLayout();
                ActivityCompat.startPostponedEnterTransition(containerActivity);

                return true;
            }
        });

/*        if (initialPhotoPosition != currentPhotoPosition) {

            ActivityCompat.postponeEnterTransition(containerActivity);
            mainListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mainListView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mainListView.requestLayout();
                    ActivityCompat.startPostponedEnterTransition(containerActivity);

                    return true;
                }
            });
            ActivityCompat.startPostponedEnterTransition(containerActivity);

        }*/
    }

    private int findShareItemPosition(String currentMediaShareTime) {

        int returnPosition = 0;
        for (int i = 0; i < mediaShareList.size(); i++) {
            MediaShare mediaShare = mediaShareList.get(i);
            String mediashareTime = mediaShare.getTime();
            if (currentMediaShareTime.equals(mediashareTime)) {
                returnPosition = i;
                break;
            }
        }

        return returnPosition;
    }

    private String findMediaTagByMediaUUID(String imageUUID) {
        String currentMediaTag;
        Media currentMedia = LocalCache.RemoteMediaMapKeyIsUUID.get(imageUUID);
        if (currentMedia == null) {
            currentMedia = LocalCache.LocalMediaMapKeyIsUUID.get(imageUUID);
        }
        currentMediaTag = currentMedia.getImageThumbUrl(containerActivity);
        return currentMediaTag;
    }

    public View getView() {
        return view;
    }


    private class ShareListViewAdapter extends BaseAdapter {

        MediaShareList container;
        Map<String, List<Comment>> commentMap;

        private static final int VIEW_ALBUM_CARD_ITEM = 0;
        private static final int VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM = 1;
        private static final int VIEW_ONE_MEDIA_SHARE_CARD_ITEM = 2;

        ShareListViewAdapter(MediaShareList container_) {
            container = container_;
            commentMap = new HashMap<>();
        }

        @Override
        public int getCount() {
            if (container.mediaShareList == null) return 0;
            return container.mediaShareList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            CardItemViewHolder viewHolder;

            if (convertView == null) {

                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.share_list_cell, parent, false);

                int type = getItemViewType(position);
                switch (type) {
                    case VIEW_ALBUM_CARD_ITEM:
                        viewHolder = new AlbumCardItemViewHolder(view);
                        break;
                    case VIEW_ONE_MEDIA_SHARE_CARD_ITEM:
                        viewHolder = new OneMediaShareCardItemViewHolder(view, commentMap);
                        break;
                    case VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM:
                        viewHolder = new OneMoreMediaShareCardItemViewHolder(view);
                        break;
                    default:
                        viewHolder = new AlbumCardItemViewHolder(view);
                }

                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (CardItemViewHolder) view.getTag();
            }

            MediaShare currentItem = (MediaShare) getItem(position);
            viewHolder.refreshView(currentItem);

            return view;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Object getItem(int position) {
            return container.mediaShareList.get(position);
        }

        @Override
        public int getItemViewType(int position) {

            MediaShare mediaShare = (MediaShare) getItem(position);
            if (mediaShare.isAlbum()) {
                return VIEW_ALBUM_CARD_ITEM;
            } else if (mediaShare.getMediaContentsListSize() == 1) {
                return VIEW_ONE_MEDIA_SHARE_CARD_ITEM;
            } else {
                return VIEW_ONE_MORE_MEDIA_SHARE_CARD_ITEM;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }
    }

    class CardItemViewHolder {

        MediaShare currentItem;

        TextView mAvatar;
        TextView lbNick;
        TextView lbTime;
        FrameLayout cardItemContentFrameLayout;

        String nickName;

        CardItemViewHolder(View view) {

            mAvatar = (TextView) view.findViewById(R.id.avatar);
            lbNick = (TextView) view.findViewById(R.id.nick);
            lbTime = (TextView) view.findViewById(R.id.time);
            cardItemContentFrameLayout = (FrameLayout) view.findViewById(R.id.card_item_content_frame_layout);

        }

        public void refreshView(MediaShare mediaShare) {

            currentItem = mediaShare;

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
        public void refreshView(MediaShare mediaShare) {
            super.refreshView(mediaShare);

            refreshViewAttributeWhenIsAlbum();
        }

        private void refreshViewAttributeWhenIsAlbum() {
            lbAlbumTitle.setText(String.format(containerActivity.getString(R.string.share_album_title), currentItem.getTitle(), String.valueOf(currentItem.getMediaShareContents().size())));

            coverImg = LocalCache.RemoteMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
            if (coverImg == null) {
                coverImg = LocalCache.LocalMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
            }
            if (coverImg != null) {

                String imageUrl = coverImg.getImageOriginalUrl(containerActivity);
                mImageLoader.setShouldCache(!coverImg.isLocal());
                ivCover.setTag(imageUrl);
                ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                ivCover.setImageUrl(imageUrl, mImageLoader);

            } else {
                ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                ivCover.setImageUrl(null, mImageLoader);
            }

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(containerActivity, AlbumPicContentActivity.class);
                    intent.putExtra(Util.KEY_MEDIASHARE, currentItem);
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

        List<String> imageDigests;


        OneMediaShareCardItemViewHolder(View view, Map<String, List<Comment>> commentMap) {
            super(view);

            this.commentMap = commentMap;

            View cardContentView = View.inflate(containerActivity, R.layout.one_mediashare_card_item_layout, cardItemContentFrameLayout);

            ButterKnife.bind(this, cardContentView);
        }

        @Override
        public void refreshView(MediaShare mediaShare) {
            super.refreshView(mediaShare);

            imageDigests = mediaShare.getMediaDigestInMediaShareContents();

            refreshViewAttributeWhenOneImage(commentMap);
        }

        private void refreshViewAttributeWhenOneImage(Map<String, List<Comment>> commentMap) {
            Log.i(TAG, "images[0]:" + imageDigests.get(0));
            itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageDigests.get(0));
            if (itemImg == null) {
                itemImg = LocalCache.LocalMediaMapKeyIsUUID.get(imageDigests.get(0));
            }

            if (itemImg != null) {

                String imageUrl = itemImg.getImageOriginalUrl(containerActivity);
                mImageLoader.setShouldCache(!itemImg.isLocal());
                ivCover.setTag(imageUrl);
                ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                ivCover.setImageUrl(imageUrl, mImageLoader);

                String uuid = itemImg.getUuid();
                if (commentMap.containsKey(uuid)) {
                    List<Comment> commentList = commentMap.get(uuid);
                    if (commentList.size() != 0) {
                        mShareCommentTextView.setText(String.format(containerActivity.getString(R.string.share_comment_text), nickName, commentList.get(0).getText()));
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
                ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                ivCover.setImageUrl(null, mImageLoader);
            }

            ivCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Media> imageList = getImgList(currentItem.getMediaDigestInMediaShareContents());

                    LocalCache.photoSliderList.clear();
                    LocalCache.photoSliderList.addAll(imageList);

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                    intent.putExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, false);
                    intent.setClass(containerActivity, PhotoSliderActivity.class);

                    String transitionName = String.valueOf(imageList.get(0).getUuid());

                    ViewCompat.setTransitionName(ivCover, transitionName);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

                    containerActivity.startActivity(intent, options.toBundle());
                }
            });
        }

        private void gotoMediaShareCommentActivity(boolean showSoftInputWhenEnter) {
            Intent intent = new Intent(containerActivity, MediaShareCommentActivity.class);
            intent.putExtra(Util.IMAGE_UUID, imageDigests.get(0));
            intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
            intent.putExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, showSoftInputWhenEnter);

            String transitionName = String.valueOf(imageDigests.get(0));

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

        List<String> imageDigests;

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
        public void refreshView(MediaShare mediaShare) {
            super.refreshView(mediaShare);

            imageDigests = mediaShare.getMediaDigestInMediaShareContents();

            refreshViewVisibilityWhenNotOneImage();

            setShareCountText();

            refreshViewAttributeWhenNotOneImage();
        }

        private void refreshViewVisibilityWhenNotOneImage() {
            if (imageDigests.size() <= 3) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.GONE);
                llPic3.setVisibility(View.GONE);

                mCheckMorePhoto.setVisibility(View.GONE);

            } else if (imageDigests.size() <= 6) {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.GONE);

                mCheckMorePhoto.setVisibility(View.GONE);

            } else {

                llPic1.setVisibility(View.VISIBLE);
                llPic2.setVisibility(View.VISIBLE);
                llPic3.setVisibility(View.VISIBLE);

                if (imageDigests.size() > 9) {
                    mCheckMorePhoto.setVisibility(View.VISIBLE);
                    mCheckMorePhoto.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    mCheckMorePhoto.getPaint().setAntiAlias(true);

                    mCheckMorePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(containerActivity, MoreMediaActivity.class);
                            intent.putExtra(Util.KEY_MEDIASHARE, currentItem);
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
            String shareCountText = String.format(containerActivity.getString(R.string.share_comment_count), String.valueOf(imageDigests.size()));
            int start = shareCountText.indexOf(String.valueOf(imageDigests.size()));
            int end = start + String.valueOf(imageDigests.size()).length();
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

            int imageDigestSize = imageDigests.size();

            int length = imageDigestSize > 9 ? 9 : imageDigestSize;

            for (int i = 0; i < length; i++) {

                ivItems[i].setVisibility(View.VISIBLE);
                itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageDigests.get(i));
                if (itemImg == null)
                    itemImg = LocalCache.LocalMediaMapKeyIsUUID.get(imageDigests.get(i));

                if (itemImg != null) {

                    String imageUrl = itemImg.getImageThumbUrl(containerActivity);
                    mImageLoader.setShouldCache(!itemImg.isLocal());
                    ivItems[i].setTag(imageUrl);
                    ivItems[i].setDefaultImageResId(R.drawable.placeholder_photo);
                    ivItems[i].setImageUrl(imageUrl, mImageLoader);

                } else {
                    ivItems[i].setDefaultImageResId(R.drawable.placeholder_photo);
                    ivItems[i].setImageUrl(null, mImageLoader);
                }

                final int mItemPosition = i;

                ivItems[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<Media> imageList = getImgList(currentItem.getMediaDigestInMediaShareContents());

                        LocalCache.photoSliderList.clear();
                        LocalCache.photoSliderList.addAll(imageList);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, mItemPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                        intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                        intent.setClass(containerActivity, PhotoSliderActivity.class);

                        String transitionName = String.valueOf(imageList.get(mItemPosition).getUuid());
                        View transitionView = ivItems[mItemPosition];

                        ViewCompat.setTransitionName(ivItems[mItemPosition], transitionName);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, transitionView, transitionName);

                        containerActivity.startActivity(intent, options.toBundle());

                    }
                });
            }
        }
    }

    private List<Media> getImgList(List<String> imageDigests) {
        ArrayList<Media> picList;
        Media picItem;
        Media picItemRaw;

        picList = new ArrayList<>();

        for (String aStArr : imageDigests) {

            picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);
            if (picItemRaw == null) {

                picItemRaw = LocalCache.LocalMediaMapKeyIsUUID.get(aStArr);

                if (picItemRaw == null) {
                    picItem = new Media();
                    picItem.setUuid(aStArr);
                    picItem.setLocal(false);
                } else {

                    picItem = picItemRaw.cloneSelf();
                    picItem.setLocal(true);
                }

            } else {

                picItem = picItemRaw.cloneSelf();
                picItem.setLocal(false);

            }

            picItem.setSelected(false);

            picList.add(picItem);
        }

        return picList;
    }


    public void refreshRemoteComment() {

        for (Map.Entry<String, List<Comment>> entry : LocalCache.RemoteMediaCommentMapKeyIsImageUUID.entrySet()) {
            List<Comment> comments = new ArrayList<>();

            String imageUUID = entry.getKey();

            comments.addAll(entry.getValue());

            mMapKeyIsImageUUIDValueIsComments.put(imageUUID, comments);
        }

        if (mMapKeyIsImageUUIDValueIsComments.size() != 0) {
            mAdapter.commentMap.putAll(mMapKeyIsImageUUIDValueIsComments);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void refreshLocalComment() {
        for (Map.Entry<String, List<Comment>> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {
            List<Comment> comments = new ArrayList<>();

            String imageUUID = entry.getKey();

            comments.addAll(entry.getValue());

            mMapKeyIsImageUUIDValueIsComments.put(imageUUID, comments);
        }

        mAdapter.commentMap.putAll(mMapKeyIsImageUUIDValueIsComments);
        mAdapter.notifyDataSetChanged();
    }
}

