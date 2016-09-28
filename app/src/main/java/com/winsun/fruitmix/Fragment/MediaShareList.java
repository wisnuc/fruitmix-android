package com.winsun.fruitmix.Fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.AlbumPicContentActivity;
import com.winsun.fruitmix.MediaShareCommentActivity;
import com.winsun.fruitmix.MoreMediaActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.PhotoSliderActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/19.
 */
public class MediaShareList implements NavPagerActivity.Page {

    public static final String TAG = MediaShareList.class.getSimpleName();

    NavPagerActivity containerActivity;
    View view;
    LinearLayout mLoadingLayout;
    LinearLayout mNoContentLayout;

    public ListView mainListView;

    List<MediaShare> mediaShareList;

    Map<String, List<Comment>> mMapKeyIsImageUUIDValueIsComments;
    ShareListViewAdapter mAdapter;

    private DBUtils dbUtils;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private Bundle reenterState;

    private List<String> mMediaUUIDWhichHaveStartedLoadComment;

    public MediaShareList(NavPagerActivity activity_) {
        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.share_list2, null);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mainListView = (ListView) view.findViewById(R.id.mainList);
        mAdapter = new ShareListViewAdapter(this);
        mainListView.setAdapter(mAdapter);

        mLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        mNoContentLayout = (LinearLayout) view.findViewById(R.id.no_content_layout);

        mMapKeyIsImageUUIDValueIsComments = new HashMap<>();

        mMediaUUIDWhichHaveStartedLoadComment = new ArrayList<>();

    }

    public void reloadList() {
        List<MediaShare> mediaShareList1;

        mediaShareList1 = new ArrayList<>();

        for (MediaShare mediaShare : LocalCache.LocalMediaShareMapKeyIsUUID.values()) {

            if (!mediaShare.isArchived() && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID())) {
                mediaShareList1.add(mediaShare);
            }

        }

        for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {

            if (!mediaShare.isArchived() && LocalCache.RemoteUserMapKeyIsUUID.containsKey(mediaShare.getCreatorUUID())) {
                mediaShareList1.add(mediaShare);
            }

        }

        Collections.sort(mediaShareList1, new Comparator<MediaShare>() {
            @Override
            public int compare(MediaShare lhs, MediaShare rhs) {
                long mtime1 = Long.parseLong(lhs.getTime());
                long mtime2 = Long.parseLong(rhs.getTime());

                if (mtime1 < mtime2)
                    return 1;
                else if (mtime1 > mtime2)
                    return -1;
                else return 0;

            }
        });

        mediaShareList = mediaShareList1;

    }


    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);

        reloadList();

        mMapKeyIsImageUUIDValueIsComments.clear();

        mLoadingLayout.setVisibility(View.INVISIBLE);
        if (mediaShareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainListView.setVisibility(View.INVISIBLE);
        } else {
            mNoContentLayout.setVisibility(View.INVISIBLE);
            mainListView.setVisibility(View.VISIBLE);
            ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();

        }

        loadRemoteComment();

        refreshRemoteComment();
        refreshLocalComment();
    }

    public void loadRemoteComment() {

        for (MediaShare mediaShare : mediaShareList) {
            if (!mediaShare.isAlbum()) {
                List<String> imageDigests = mediaShare.getImageDigests();
                if (imageDigests.size() == 1) {

                    Media media = LocalCache.RemoteMediaMapKeyIsUUID.get(imageDigests.get(0));

                    if (media != null) {
                        String uuid = media.getUuid();
                        if (!mMediaUUIDWhichHaveStartedLoadComment.contains(uuid)) {
                            loadCommentList(uuid);

                            Log.i(TAG, "load image comment,image uuid:" + uuid);
                            mMediaUUIDWhichHaveStartedLoadComment.add(uuid);
                        }

                    }

                }
            }
        }
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

                List<String> currentMediaUUIDs = mediaShareList.get(currentMediaSharePosition).getImageDigests();

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

        if (initialPhotoPosition != currentPhotoPosition) {

/*            ActivityCompat.postponeEnterTransition(containerActivity);
            mainListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mainListView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mainListView.requestLayout();
                    ActivityCompat.startPostponedEnterTransition(containerActivity);

                    return true;
                }
            });*/
            ActivityCompat.startPostponedEnterTransition(containerActivity);
        }
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
            currentMediaTag = currentMedia.getThumb();
        } else {

            int w, h;
            w = Integer.parseInt(currentMedia.getWidth());
            h = Integer.parseInt(currentMedia.getHeight());

            int[] result = Util.formatPhotoWidthHeight(w, h);

            currentMediaTag = String.format(containerActivity.getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + currentMedia.getUuid(), String.valueOf(result[0]), String.valueOf(result[1]));

        }
        return currentMediaTag;
    }

    public View getView() {
        return view;
    }


    private class ShareListViewAdapter extends BaseAdapter {

        MediaShareList container;
        Map<String, List<Comment>> commentMap;

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
            final MediaShare currentItem;
            Media coverImg, itemImg;
            TextView lbNick, lbTime, lbAlbumTitle;
            ImageView lbAlbumShare;
            final NetworkImageView ivCover;
            final NetworkImageView ivItems[];
            View rlAlbum, llPic1, llPic2, llPic3;
            boolean sLocal;
            int w, h, i;
            final List<String> imageDigests;

            RelativeLayout mShareCountLayout;
            RelativeLayout mShareCommentLayout;
            TextView mShareCountTextView;
            TextView mShareCommentTextView;
            TextView mShareCommentCountTextView;
            TextView mAvator;
            ImageView commentShare;

            String nickName;

            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.share_list_cell, parent, false);
            else view = convertView;

            currentItem = (MediaShare) this.getItem(position);

            nickName = LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreatorUUID()).getUserName();

            lbNick = (TextView) view.findViewById(R.id.nick);
            lbTime = (TextView) view.findViewById(R.id.time);
            ivCover = (NetworkImageView) view.findViewById(R.id.cover_img);
            lbAlbumTitle = (TextView) view.findViewById(R.id.album_title);
            lbAlbumShare = (ImageView) view.findViewById(R.id.album_share);

            mShareCountLayout = (RelativeLayout) view.findViewById(R.id.share_count_layout);
            mShareCommentLayout = (RelativeLayout) view.findViewById(R.id.share_comment_layout);
            mAvator = (TextView) view.findViewById(R.id.avatar);
            mShareCommentCountTextView = (TextView) view.findViewById(R.id.share_comment_count_textview);

            lbNick.setText(nickName);
            lbTime.setText(Util.formatTime(containerActivity, Long.parseLong(currentItem.getTime())));

            User user = LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreatorUUID());

            String avatar;
            int color = 0;

            avatar = user.getAvatar();
            color = Integer.valueOf(user.getDefaultAvatarBgColor());
            if (avatar.equals("defaultAvatar.jpg")) {
                avatar = user.getDefaultAvatar();
            }

            mAvator.setText(avatar);
            switch (color) {
                case 0:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                    break;
                case 1:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_green);
                    break;
                case 2:
                    mAvator.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                    break;
            }

            rlAlbum = view.findViewById(R.id.album_row);
            llPic1 = view.findViewById(R.id.pic_row1);
            llPic2 = view.findViewById(R.id.pic_row2);
            llPic3 = view.findViewById(R.id.pic_row3);

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

            if (currentItem.isAlbum()) {
                rlAlbum.setVisibility(View.VISIBLE);
                llPic1.setVisibility(View.GONE);
                llPic2.setVisibility(View.GONE);
                llPic3.setVisibility(View.GONE);

                mShareCommentLayout.setVisibility(View.GONE);
                mShareCountLayout.setVisibility(View.GONE);

                lbAlbumShare.setVisibility(View.VISIBLE);
                lbAlbumTitle.setVisibility(View.VISIBLE);


                lbAlbumTitle.setText(String.format(containerActivity.getString(R.string.share_album_title), currentItem.getTitle(), String.valueOf(currentItem.getImageDigests().size())));

                coverImg = LocalCache.RemoteMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
                if (coverImg != null) sLocal = false;
                else {
                    coverImg = LocalCache.LocalMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
                    sLocal = true;
                }
                if (coverImg != null) {

                    if (sLocal) {

                        String url = String.valueOf(coverImg.getThumb());

                        mImageLoader.setShouldCache(false);
                        ivCover.setTag(url);
                        ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                        ivCover.setImageUrl(url, mImageLoader);

                    } else {

                        String url = String.format(containerActivity.getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + coverImg.getUuid());

                        mImageLoader.setShouldCache(true);
                        ivCover.setTag(url);
                        ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                        ivCover.setImageUrl(url, mImageLoader);

                    }
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

            } else {

                lbAlbumShare.setVisibility(View.GONE);
                lbAlbumTitle.setVisibility(View.GONE);

                imageDigests = currentItem.getImageDigests();

                if (imageDigests.size() == 1) {
                    rlAlbum.setVisibility(View.VISIBLE);
                    llPic1.setVisibility(View.GONE);
                    llPic2.setVisibility(View.GONE);
                    llPic3.setVisibility(View.GONE);

                    mShareCommentLayout.setVisibility(View.VISIBLE);
                    mShareCountLayout.setVisibility(View.GONE);

                    Log.i(TAG, "images[0]:" + imageDigests.get(0));
                    itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageDigests.get(0));
                    if (itemImg != null) sLocal = false;
                    else {
                        itemImg = LocalCache.LocalMediaMapKeyIsUUID.get(imageDigests.get(0));
                        sLocal = true;
                    }

                    if (itemImg != null) {
                        if (sLocal) {

                            String url = itemImg.getThumb();

                            mImageLoader.setShouldCache(false);
                            ivCover.setTag(url);
                            ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                            ivCover.setImageUrl(url, mImageLoader);


                        } else {

                            String url = String.format(containerActivity.getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + itemImg.getUuid());

                            mImageLoader.setShouldCache(true);
                            ivCover.setTag(url);
                            ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                            ivCover.setImageUrl(url, mImageLoader);

                        }

                        mShareCommentTextView = (TextView) view.findViewById(R.id.share_comment_textview);
                        commentShare = (ImageView) view.findViewById(R.id.comment_share);

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
                                    Intent intent = new Intent(containerActivity, MediaShareCommentActivity.class);
                                    intent.putExtra(Util.IMAGE_UUID, imageDigests.get(0));
                                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                                    intent.putExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, false);

                                    String transitionName = String.valueOf(imageDigests.get(0));

                                    ViewCompat.setTransitionName(ivCover, transitionName);

                                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

                                    containerActivity.startActivity(intent, options.toBundle());
                                }
                            });
                        }

                        commentShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(containerActivity, MediaShareCommentActivity.class);
                                intent.putExtra(Util.IMAGE_UUID, imageDigests.get(0));
                                intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                                intent.putExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, true);

                                String transitionName = String.valueOf(imageDigests.get(0));

                                ViewCompat.setTransitionName(ivCover, transitionName);

                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

                                containerActivity.startActivity(intent, options.toBundle());
                            }
                        });

                    } else {
                        ivCover.setDefaultImageResId(R.drawable.placeholder_photo);
                        ivCover.setImageUrl(null, mImageLoader);
                    }

                    ivCover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ArrayList<Media> imageList = (ArrayList<Media>) getImgList(currentItem.getImageDigests());

                            Intent intent = new Intent();
                            intent.putExtra(Util.INITIAL_PHOTO_POSITION, 0);
                            intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                            intent.putExtra(Util.CURRENT_MEDIASHARE_TIME, currentItem.getTime());
                            intent.putExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, false);

                            intent.putParcelableArrayListExtra(Util.KEY_MEDIA_LIST, imageList);
                            intent.setClass(containerActivity, PhotoSliderActivity.class);

                            String transitionName = String.valueOf(imageList.get(0).getUuid());

                            ViewCompat.setTransitionName(ivCover, transitionName);

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, ivCover, transitionName);

                            containerActivity.startActivity(intent, options.toBundle());
                        }
                    });

                } else {

                    if (imageDigests.size() <= 3) {

                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.GONE);
                        llPic3.setVisibility(View.GONE);

                    } else if (imageDigests.size() <= 6) {

                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.VISIBLE);
                        llPic3.setVisibility(View.GONE);

                    } else {
                        rlAlbum.setVisibility(View.GONE);
                        llPic1.setVisibility(View.VISIBLE);
                        llPic2.setVisibility(View.VISIBLE);
                        llPic3.setVisibility(View.VISIBLE);

                        TextView mCheckMorePhoto = (TextView) view.findViewById(R.id.check_more_photos);
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

                    mShareCommentLayout.setVisibility(View.GONE);
                    mShareCountLayout.setVisibility(View.VISIBLE);

                    mShareCountTextView = (TextView) view.findViewById(R.id.share_count_textview);

                    String shareCountText = String.format(containerActivity.getString(R.string.share_comment_count), String.valueOf(imageDigests.size()));
                    int start = shareCountText.indexOf(String.valueOf(imageDigests.size()));
                    int end = start + String.valueOf(imageDigests.size()).length();
                    SpannableStringBuilder builder = new SpannableStringBuilder(shareCountText);
                    ForegroundColorSpan span = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_black));
                    ForegroundColorSpan beforeSpan = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_gray));
                    ForegroundColorSpan afterSpan = new ForegroundColorSpan(containerActivity.getResources().getColor(R.color.light_gray));
                    builder.setSpan(beforeSpan, 0, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    builder.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(afterSpan, end, shareCountText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mShareCountTextView.setText(builder);

                    for (i = 0; i < 9; i++) {
                        if (i >= imageDigests.size()) {
                            ivItems[i].setVisibility(View.INVISIBLE);
                            continue;
                        }
                        ivItems[i].setVisibility(View.VISIBLE);
                        itemImg = LocalCache.RemoteMediaMapKeyIsUUID.get(imageDigests.get(i));
                        if (itemImg != null) sLocal = false;
                        else {
                            itemImg = LocalCache.LocalMediaMapKeyIsUUID.get(imageDigests.get(i));
                            sLocal = true;
                        }

                        if (itemImg != null) {
                            if (sLocal) {

                                String url = itemImg.getThumb();

                                mImageLoader.setShouldCache(false);
                                ivItems[i].setTag(url);
                                ivItems[i].setDefaultImageResId(R.drawable.placeholder_photo);
                                ivItems[i].setImageUrl(url, mImageLoader);

                            } else {

                                w = Integer.parseInt(itemImg.getWidth());
                                h = Integer.parseInt(itemImg.getHeight());

                                int[] result = Util.formatPhotoWidthHeight(w, h);

                                String url = String.format(containerActivity.getString(R.string.thumb_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + itemImg.getUuid(), String.valueOf(result[0]), String.valueOf(result[1]));

                                mImageLoader.setShouldCache(true);
                                ivItems[i].setTag(url);
                                ivItems[i].setDefaultImageResId(R.drawable.placeholder_photo);
                                ivItems[i].setImageUrl(url, mImageLoader);
                            }
                        } else {
                            ivItems[i].setDefaultImageResId(R.drawable.placeholder_photo);
                            ivItems[i].setImageUrl(null, mImageLoader);
                        }

                        final int mItemPosition = i;

                        ivItems[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("winsun", currentItem + "");

                                List<Media> imageList = getImgList(currentItem.getImageDigests());

                                LocalCache.TransActivityContainer.put("imgSliderList", imageList);

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

        public List<Media> getImgList(List<String> imageDigests) {
            List<Media> picList;
            Media picItem;
            Media picItemRaw;

            picList = new ArrayList<>();

            for (String aStArr : imageDigests) {
                picItem = new Media();
                picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);
                if (picItemRaw != null) {
                    picItem.setLocal(false);
                } else {
                    picItemRaw = LocalCache.LocalMediaMapKeyIsUUID.get(aStArr);
                    picItem.setLocal(true);
                    picItem.setThumb(picItemRaw.getThumb());
                }

                picItem.setUuid(picItemRaw.getUuid());
                picItem.setWidth(picItemRaw.getWidth());
                picItem.setHeight(picItemRaw.getHeight());
                picItem.setTime(picItemRaw.getTime());
                picItem.setSelected(false);

                picList.add(picItem);
            }


            return picList;
        }
    }


    private void loadCommentList(String imageUuid) {

        containerActivity.retrieveRemoteMediaComment(imageUuid);

    }

    public void refreshRemoteComment() {

        for (Map.Entry<String, Comment> entry : LocalCache.RemoteMediaCommentMapKeyIsImageUUID.entrySet()) {
            List<Comment> comments;

            String imageUUID = entry.getKey();

            if (!mMapKeyIsImageUUIDValueIsComments.containsKey(imageUUID)) {
                comments = new ArrayList<>();
                mMapKeyIsImageUUIDValueIsComments.put(imageUUID, comments);
            } else {
                comments = mMapKeyIsImageUUIDValueIsComments.get(imageUUID);
            }

            comments.add(entry.getValue());
        }

        if (mMapKeyIsImageUUIDValueIsComments.size() != 0) {
            mAdapter.commentMap.putAll(mMapKeyIsImageUUIDValueIsComments);
            mAdapter.notifyDataSetChanged();
        } 

    }

    public void refreshLocalComment() {
        for (Map.Entry<String, Comment> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {
            List<Comment> comments;

            String imageUUID = entry.getKey();

            if (!mMapKeyIsImageUUIDValueIsComments.containsKey(imageUUID)) {
                comments = new ArrayList<>();
                mMapKeyIsImageUUIDValueIsComments.put(imageUUID, comments);
            } else {
                comments = mMapKeyIsImageUUIDValueIsComments.get(imageUUID);
            }

            comments.add(entry.getValue());
        }

        mAdapter.commentMap.putAll(mMapKeyIsImageUUIDValueIsComments);
        mAdapter.notifyDataSetChanged();
    }
}

