package com.winsun.fruitmix.mediaModule.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.winsun.fruitmix.mediaModule.AlbumPicContentActivity;
import com.winsun.fruitmix.mediaModule.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.interfaces.OnMediaFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
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
public class AlbumList implements Page {

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
    @BindView(R.id.mainList)
    ListView mainListView;

    private List<MediaShare> mediaShareList;

    private long mDownTime = 0;
    private double mDiffTimeMilliSecond = 200;

    private ImageLoader mImageLoader;

    private SwipeLayout lastSwipeLayout;


    public AlbumList(Activity activity_, OnMediaFragmentInteractionListener listener) {

        containerActivity = activity_;

        this.listener = listener;

        view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list, null);

        ButterKnife.bind(this, view);

        initImageLoader();

        mainListView.setAdapter(new AlbumListAdapter(this));

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(containerActivity, NewAlbumPicChooseActivity.class);
                containerActivity.startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
            }
        });


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
        List<MediaShare> mediaShareList;
        mediaShareList = new ArrayList<>();

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

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                mediaShareList.add(mediaShare);
            }
        }

        for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {
                mediaShareList.add(mediaShare);
            }
        }
    }

    public void refreshView() {

        mLoadingLayout.setVisibility(View.VISIBLE);
        ivAdd.setVisibility(View.INVISIBLE);

        if (!listener.isRemoteMediaShareLoaded()) {
            return;
        }

        reloadList();

        mLoadingLayout.setVisibility(View.GONE);
        ivAdd.setVisibility(View.VISIBLE);
        if (mediaShareList.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mainListView.setVisibility(View.GONE);
        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mainListView.setVisibility(View.VISIBLE);
            ((BaseAdapter) (mainListView.getAdapter())).notifyDataSetChanged();
            mainListView.setSelection(0);
        }

    }

    public void onDidAppear() {

        refreshView();
    }


    public View getView() {
        return view;
    }

    private class AlbumListAdapter extends BaseSwipeAdapter {

        AlbumList albumList;

        AlbumListAdapter(AlbumList albumList) {
            this.albumList = albumList;
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
         * generate a new view item.
         * Never bind SwipeListener or fill values here, every item has a chance to fill value or bind
         * listeners in fillValues.
         * to fill it in {@code fillValues} method.
         *
         * @param position
         * @param parent
         * @return
         */
        @Override
        public View generateView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(containerActivity).inflate(R.layout.album_list_item, null);

            AlbumListViewHolder viewHolder = new AlbumListViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        /**
         * fill values or bind listeners to the view.
         *
         * @param position
         * @param convertView
         */
        @Override
        public void fillValues(int position, View convertView) {

            View view;
            AlbumListViewHolder viewHolder;

            view = convertView;
            viewHolder = (AlbumListViewHolder) view.getTag();

            MediaShare currentItem = (MediaShare) getItem(position);
            viewHolder.refreshView(currentItem);

        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            if (albumList.mediaShareList == null) return 0;
            return albumList.mediaShareList.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return albumList.mediaShareList.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }
    }


    class AlbumListViewHolder {

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
        @BindView(R.id.photo_count_tv)
        TextView lbPhotoCount;
        @BindView(R.id.delete)
        TextView lbDelete;
        @BindView(R.id.share)
        TextView lbShare;
        @BindView(R.id.swipe_layout)
        SwipeLayout swipeLayout;

        Media coverImg;
        MediaShare currentItem;

        AlbumListViewHolder(View view) {

            ButterKnife.bind(this, view);
        }

        void refreshView(MediaShare mediaShare) {

            currentItem = mediaShare;
            restoreSwipeLayoutState();

            coverImg = LocalCache.RemoteMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
            if (coverImg == null) {
                coverImg = LocalCache.LocalMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
            }

            if (coverImg != null) {

                String imageUrl = coverImg.getImageThumbUrl(containerActivity);
                mImageLoader.setShouldCache(!coverImg.isLocal());
                ivMainPic.setTag(imageUrl);
                ivMainPic.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMainPic.setImageUrl(imageUrl, mImageLoader);

            } else {
                ivMainPic.setDefaultImageResId(R.drawable.placeholder_photo);
                ivMainPic.setImageUrl(null, mImageLoader);
            }

            if (currentItem.getViewersListSize() == 0) {
                ivLock.setVisibility(View.GONE);
                lbShare.setText(containerActivity.getString(R.string.share_text));
            } else {
                ivLock.setVisibility(View.VISIBLE);
                lbShare.setText(containerActivity.getString(R.string.private_text));
            }

            lbTitle.setText(currentItem.getTitle());
            lbPhotoCount.setText(String.format(containerActivity.getString(R.string.photo_count), String.valueOf(currentItem.getMediaShareContents().size())));
            lbDesc.setText(currentItem.getDesc());
            lbDate.setText(currentItem.getDate().substring(0, 10));

            String createUUID = currentItem.getCreatorUUID();
            if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(createUUID)) {
                lbOwner.setText(LocalCache.RemoteUserMapKeyIsUUID.get(createUUID).getUserName());
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

                    new AlertDialog.Builder(containerActivity).setMessage(containerActivity.getString(R.string.confirm_delete))
                            .setPositiveButton(containerActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                    intent.putExtra(Util.KEY_MEDIASHARE, currentItem);
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

}

