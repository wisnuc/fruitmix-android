package com.winsun.fruitmix.mediaModule.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.interfaces.IPhotoListListener;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.mediaModule.AlbumPicContentActivity;
import com.winsun.fruitmix.mediaModule.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.AnimatorBuilder;
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
public class AlbumList implements Page, IShowHideFragmentListener {

    public static final String TAG = AlbumList.class.getSimpleName();

    private OnMediaFragmentInteractionListener listener;

    private Activity containerActivity;
    private View view;

    @BindView(R.id.add_album)
    FloatingActionButton ivAdd;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.mainRecylerView)
    RecyclerView mainRecyclerView;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

    private AlbumRecyclerViewAdapter mAdapter;

    private List<MediaShare> remoteMediaShares;
    private Map<String, User> remoteUserMaps;
    private Map<String, Media> remoteMediaMaps;

    private List<MediaShare> mediaShareList;

    private ImageLoader mImageLoader;

    private SwipeLayout lastSwipeLayout;

    private boolean mShowPhoto = false;

    private List<IPhotoListListener> mPhotoListListeners;

    private boolean mFillRecommendAlbum = false;

    private boolean mIsFling = false;

    public AlbumList(Activity activity_, OnMediaFragmentInteractionListener listener) {

        containerActivity = activity_;

        this.listener = listener;

        view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        noContentTextView.setText(containerActivity.getString(R.string.no_albums));

//        mainRecyclerView.addOnScrollListener(new AlbumRecycleViewScrollListener());

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(containerActivity));
        mainRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new AlbumRecyclerViewAdapter();

        mainRecyclerView.setAdapter(mAdapter);

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(containerActivity, NewAlbumPicChooseActivity.class);
                containerActivity.startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
            }
        });

        mediaShareList = new ArrayList<>();

        remoteMediaShares = new ArrayList<>();
        remoteUserMaps = new HashMap<>();
        remoteMediaMaps = new HashMap<>();

        mPhotoListListeners = new ArrayList<>();

        initImageLoader();

    }

    public void addPhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.add(listListener);
    }

    public void removePhotoListListener(IPhotoListListener listListener) {
        mPhotoListListeners.remove(listListener);
    }

    @Override
    public void show() {
        //MobclickAgent.onPageStart("AlbumFragment");

        //TODO:fix bug about show fab animator

//        if (mLoadingLayout.getVisibility() != View.VISIBLE) {
//            ivAdd.setVisibility(View.INVISIBLE);
//            new AnimatorBuilder(containerActivity, R.animator.fab_scale_restore, ivAdd).setStartDelay(200)
//                    .addAdapter(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//                            super.onAnimationStart(animation);
//
//                            ivAdd.setVisibility(View.VISIBLE);
//                        }
//                    }).startAnimator();
//
//        }
    }

    @Override
    public void hide() {
        //MobclickAgent.onPageEnd("AlbumFragment");
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(containerActivity);

    }

    private void reloadList() {
        mediaShareList.clear();

        remoteMediaShares.clear();
        remoteMediaShares.addAll(LocalCache.RemoteMediaShareMapKeyIsUUID.values());

        remoteUserMaps.clear();
        remoteUserMaps.putAll(LocalCache.RemoteUserMapKeyIsUUID);

        remoteMediaMaps.clear();
        remoteMediaMaps.putAll(LocalCache.RemoteMediaMapKeyIsUUID);

        fillMediaShareList(mediaShareList);

        sortMediaShareList(mediaShareList);

        if (mFillRecommendAlbum) {

            mediaShareList.addAll(0, LocalCache.RecommendMediaShares);

        }

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

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                mediaShareList.add(mediaShare);
            }
        }

        for (MediaShare mediaShare : remoteMediaShares) {

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                mediaShareList.add(mediaShare);
            }
        }

    }

    public void showPhoto() {

        mShowPhoto = true;

        if (mainRecyclerView.getVisibility() == View.VISIBLE)
            mAdapter.notifyDataSetChanged();

    }

    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);
        ivAdd.setVisibility(View.INVISIBLE);

        if (!Util.isRemoteMediaShareLoaded()) {
            return;
        }

        reloadList();

        mLoadingLayout.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        if (mediaShareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainRecyclerView.setVisibility(View.GONE);
        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mainRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void refreshRecommendAlbum() {

        mFillRecommendAlbum = true;

        refreshView();

    }

    public void onDidAppear() {

        refreshView();
        mainRecyclerView.smoothScrollToPosition(0);
    }

    public View getView() {
        return view;
    }

    private class AlbumRecyclerViewAdapter extends RecyclerSwipeAdapter<BaseAlbumViewHolder> {

        static final int NORMAL_ALBUM = 0x1001;
        static final int RECOMMEND_ALBUM = 0x1002;

        AlbumRecyclerViewAdapter() {
//            setHasStableIds(true);
        }

        @Override
        public int getItemViewType(int position) {

            if (mediaShareList.get(position).isRecommend()) {
                return RECOMMEND_ALBUM;
            } else {
                return NORMAL_ALBUM;
            }

        }


        /**
         * return the {@link SwipeLayout} resource id, int the view item.
         *
         * @param position
         * @return
         */
        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe_layout;
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            if (mediaShareList == null) return 0;
            return mediaShareList.size();
        }

        @Override
        public BaseAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;

            switch (viewType) {
                case NORMAL_ALBUM:
                    view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list_item, parent, false);
                    return new AlbumListViewHolder(view);

                case RECOMMEND_ALBUM:

                    view = LayoutInflater.from(containerActivity).inflate(R.layout.recommend_album_list_item, parent, false);
                    return new RecommendAlbumViewHolder(view);

            }

            return null;

        }

        @Override
        public void onBindViewHolder(BaseAlbumViewHolder viewHolder, int position) {

            viewHolder.refreshView(position, mediaShareList.get(position));

        }
    }

    class BaseAlbumViewHolder extends RecyclerView.ViewHolder {

        Media coverImg;
        MediaShare currentItem;

        BaseAlbumViewHolder(View itemView) {
            super(itemView);
        }

        void refreshView(int position, MediaShare mediaShare) {

            currentItem = mediaShare;

            String key = currentItem.getCoverImageUUID();

            if (key.isEmpty()) {
                coverImg = null;
            } else {
                coverImg = LocalCache.findMediaInLocalMediaMap(key);
                if (coverImg == null) {
                    coverImg = remoteMediaMaps.get(key);
                }
            }

            Log.d(TAG, "refreshView: coverImg: " + coverImg + " mShowPhoto: " + mShowPhoto);
        }
    }

    class RecommendAlbumViewHolder extends BaseAlbumViewHolder {

        @BindView(R.id.network_image_view)
        NetworkImageView networkImageView;
        @BindView(R.id.time)
        TextView mTimeTextView;
        @BindView(R.id.photo_count)
        TextView mPhotoCountTextView;

        RecommendAlbumViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void refreshView(int position, MediaShare mediaShare) {

            super.refreshView(position, mediaShare);

            if (coverImg != null && mShowPhoto) {

                String imageUrl = coverImg.getImageThumbUrl(containerActivity);

                mImageLoader.setShouldCache(!coverImg.isLocal());

                if (coverImg.isLocal())
                    networkImageView.setOrientationNumber(coverImg.getOrientationNumber());

                networkImageView.setTag(imageUrl);
                networkImageView.setDefaultImageResId(R.drawable.default_place_holder);
//                ivMainPic.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));
                networkImageView.setImageUrl(imageUrl, mImageLoader);

            } else {
                networkImageView.setDefaultImageResId(R.drawable.default_place_holder);
//                ivMainPic.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));
                networkImageView.setImageUrl(null, mImageLoader);

            }

            mTimeTextView.setText(currentItem.getRecommendPhotoTime());

            Log.i(TAG, "refreshView: media contents list size: " + currentItem.getMediaContentsListSize());

            int mediaContentsListSize = currentItem.getMediaContentsListSize();

            String count = mediaContentsListSize + " " + containerActivity.getResources().getQuantityString(R.plurals.photo, mediaContentsListSize);

            mPhotoCountTextView.setText(count);
        }
    }

    class AlbumListViewHolder extends BaseAlbumViewHolder {

        @BindView(R.id.mainBar)
        RelativeLayout mainBar;
        @BindView(R.id.mainPic)
        NetworkImageView ivMainPic;
        @BindView(R.id.lock)
        ImageView ivLock;
        @BindView(R.id.title)
        TextView lbTitle;
        @BindView(R.id.desc)
        TextView lbDesc;
        @BindView(R.id.date)
        TextView lbDate;
        @BindView(R.id.owner)
        TextView lbOwner;
        @BindView(R.id.delete)
        TextView lbDelete;
        @BindView(R.id.share)
        TextView lbShare;
        @BindView(R.id.swipe_layout)
        SwipeLayout swipeLayout;

        @BindView(R.id.spacing_layout)
        View mSpacingLayout;


        AlbumListViewHolder(View view) {

            super(view);

            ButterKnife.bind(this, view);
        }

        void refreshView(int position, MediaShare mediaShare) {

            super.refreshView(position, mediaShare);

/*            if (position == 0) {
                mSpacingLayout.setVisibility(View.VISIBLE);
            } else {
                mSpacingLayout.setVisibility(View.GONE);
            }*/

            if (coverImg != null && mShowPhoto) {

                String imageUrl = coverImg.getImageThumbUrl(containerActivity);

                mImageLoader.setShouldCache(!coverImg.isLocal());

                if (coverImg.isLocal())
                    ivMainPic.setOrientationNumber(coverImg.getOrientationNumber());

                ivMainPic.setTag(imageUrl);
                ivMainPic.setDefaultImageResId(R.drawable.default_place_holder);
//                ivMainPic.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));
                ivMainPic.setImageUrl(imageUrl, mImageLoader);

            } else {
                ivMainPic.setDefaultImageResId(R.drawable.default_place_holder);
//                ivMainPic.setDefaultBackgroundColor(ContextCompat.getColor(containerActivity,R.color.default_imageview_color));
                ivMainPic.setImageUrl(null, mImageLoader);

            }

            if (currentItem.getViewersListSize() == 0) {
                ivLock.setVisibility(View.GONE);
                lbShare.setText(containerActivity.getString(R.string.share_verb));
            } else {
                ivLock.setVisibility(View.VISIBLE);
                lbShare.setText(containerActivity.getString(R.string.private_text));
            }

            String title = currentItem.getTitle();

            String photoCount = String.valueOf(currentItem.getMediaContentsListSize());

            if (title.length() > 8) {
                title = title.substring(0, 8);

                title += containerActivity.getString(R.string.android_ellipsize);
            }

            title = String.format(containerActivity.getString(R.string.android_share_album_title), title, photoCount, containerActivity.getResources().getQuantityString(R.plurals.photo, currentItem.getMediaContentsListSize()));

            lbTitle.setText(title);

            String desc = currentItem.getDesc();

            if (desc == null || desc.length() == 0) {
                lbDesc.setVisibility(View.GONE);
            } else {
                lbDesc.setVisibility(View.VISIBLE);
                lbDesc.setText(currentItem.getDesc());
            }

            lbDate.setText(currentItem.getDate().substring(0, 10));

            String createUUID = currentItem.getCreatorUUID();
            if (remoteUserMaps.containsKey(createUUID)) {
                lbOwner.setText(remoteUserMaps.get(createUUID).getUserName());
            }

            lbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    restoreSwipeLayoutState();

                    MediaShare cloneMediaShare = currentItem.cloneMyself();

                    listener.modifyMediaShare(cloneMediaShare);

                }
            });

            lbDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    new AlertDialog.Builder(containerActivity).setMessage(String.format(containerActivity.getString(R.string.confirm_delete), currentItem.getTitle()))
                            .setPositiveButton(containerActivity.getString(R.string.remove), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    restoreSwipeLayoutState();

                                    listener.deleteMediaShare(currentItem);

                                }
                            }).setNegativeButton(containerActivity.getString(R.string.cancel), null).create().show();


                }
            });

            mainBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(containerActivity, AlbumPicContentActivity.class);
                    intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, currentItem.getUuid());
                    containerActivity.startActivityForResult(intent, Util.KEY_ALBUM_CONTENT_REQUEST_CODE);
                }
            });

            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    super.onStartOpen(layout);

                    if (lastSwipeLayout != null) {
                        lastSwipeLayout.close();
                    }
                    lastSwipeLayout = swipeLayout;
                }
            });

        }

        private void restoreSwipeLayoutState() {
            //restore mainbar state
//            mainBar.setTranslationX(0.0f);
            swipeLayout.close();
        }

    }

    private class AlbumRecycleViewScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                mIsFling = true;
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                if (mIsFling) {
                    mIsFling = false;

                    mAdapter.notifyDataSetChanged();
                }

//                for (IPhotoListListener listener : mPhotoListListeners) {
//                    listener.onPhotoListScrollFinished();
//                }

            }
        }

//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//
//            if (dy > 0) {
//
//                for (IPhotoListListener listener : mPhotoListListeners) {
//                    listener.onPhotoListScrollDown();
//                }
//
//            } else if (dy < 0) {
//
//                for (IPhotoListListener listener : mPhotoListListeners) {
//                    listener.onPhotoListScrollUp();
//                }
//
//            }
//        }
    }

}

