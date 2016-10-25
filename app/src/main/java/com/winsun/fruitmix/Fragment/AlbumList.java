package com.winsun.fruitmix.Fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.MediaShare;
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
public class AlbumList implements NavPagerActivity.Page {

    public static final String TAG = AlbumList.class.getSimpleName();

    private NavPagerActivity containerActivity;
    private View view;

    @BindView(R.id.add_album)
    ImageView ivAdd;
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
    private RelativeLayout lastMainBar;


    public AlbumList(NavPagerActivity activity_) {

        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.album_list, null);

        ButterKnife.bind(this, view);

        initImageLoader();

        mainListView.setAdapter(new AlbumListViewAdapter(this));

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


    public void reloadList() {
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

    private class AlbumListViewAdapter extends BaseAdapter {

        AlbumList container;

        AlbumListViewAdapter(AlbumList container_) {
            container = container_;
        }

        @Override
        public int getCount() {
            if (container.mediaShareList == null) return 0;
            return container.mediaShareList.size();
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View view;
            AlbumListViewHolder viewHolder;

            if (convertView == null) {
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.album_list_cell, parent, false);
                viewHolder = new AlbumListViewHolder(view);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (AlbumListViewHolder) view.getTag();
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

        Media coverImg;
        MediaShare currentItem;

        AlbumListViewHolder(View view) {

            ButterKnife.bind(this, view);
        }

        void refreshView(MediaShare mediaShare) {

            currentItem = mediaShare;
            restoreMainBarState();

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
                lbShare.setText(containerActivity.getString(R.string.public_text));
            } else {
                ivLock.setVisibility(View.VISIBLE);
                lbShare.setText(containerActivity.getString(R.string.private_text));
            }

            lbTitle.setText(currentItem.getTitle());
            lbPhotoCount.setText(String.format(containerActivity.getString(R.string.photo_count), String.valueOf(currentItem.getMediaShareContents().size())));
            lbDesc.setText(currentItem.getDesc());
            lbDate.setText(currentItem.getDate().substring(0, 10));

            lbOwner.setText(LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreatorUUID()).getUserName());

            lbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    restoreMainBarState();

                    MediaShare cloneMediaShare = currentItem.cloneMyself();

                    String requestData = createRequestData(cloneMediaShare);

                    containerActivity.modifyMediaShare(cloneMediaShare, requestData);

                }
            });

            lbDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    restoreMainBarState();

                    containerActivity.deleteMediaShare(currentItem);

                }
            });

            mainBar.setOnTouchListener(new View.OnTouchListener() {

                float x, y, lastX, lastY, vY, vX;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int margin;
                    margin = Util.dip2px(containerActivity, 100);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:

                            if (lastMainBar != null) lastMainBar.setTranslationX(0.0f);
                            lastMainBar = mainBar;
                            x = event.getRawX() - mainBar.getTranslationX();
                            y = event.getRawY();
                            lastX = x;
                            lastY = y;

                            mDownTime = System.currentTimeMillis();

                            Log.d(TAG, "down");
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(TAG, "up X:" + (lastX - x) + " Y:" + (lastY - y));

                            if (System.currentTimeMillis() - mDownTime < mDiffTimeMilliSecond && (lastX - x) * (lastX - x) + (lastY - y) * (lastY - y) < 100.0) {
                                Intent intent = new Intent();
                                intent.setClass(containerActivity, AlbumPicContentActivity.class);
                                intent.putExtra(Util.KEY_MEDIASHARE, currentItem);
                                containerActivity.startActivityForResult(intent, Util.KEY_ALBUM_CONTENT_REQUEST_CODE);

                            }
                        case MotionEvent.ACTION_CANCEL:
                            Log.d(TAG, "cancel " + (lastX - x) + " " + (lastY - y));
                            if (lastX - x > -margin + 0.5 && lastX - x < margin - 0.5) {
                                if (vX > 30.0) mainBar.setTranslationX(margin);
                                else if (vX < -30.0) mainBar.setTranslationX(-margin);
                                else restoreMainBarState();
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            vX = event.getRawX() - lastX;
                            vY = event.getRawX() - lastY;
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            if (lastX - x > margin) lastX = x + margin;
                            else if (lastX - x < -margin) lastX = x - margin;
                            mainBar.setTranslationX(lastX - x);
                            break;
                    }
                    return true;
                }

            });
        }

        private void restoreMainBarState() {
            //restore mainbar state
            mainBar.setTranslationX(0.0f);
        }

        @NonNull
        private String createRequestData(MediaShare cloneMediaShare) {
            String requestData;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");

            if (cloneMediaShare.getViewersListSize() == 0) {

                for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                    cloneMediaShare.addViewer(userUUID);
                }

                stringBuilder.append(cloneMediaShare.createStringOperateViewersInMediaShare(Util.ADD));

            } else {

                stringBuilder.append(cloneMediaShare.createStringOperateViewersInMediaShare(Util.DELETE));

                cloneMediaShare.clearViewers();
            }

            stringBuilder.append("]");
            requestData = stringBuilder.toString();
            return requestData;
        }
    }

}

