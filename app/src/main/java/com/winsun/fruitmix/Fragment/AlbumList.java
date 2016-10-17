package com.winsun.fruitmix.Fragment;

import android.content.Intent;
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

/**
 * Created by Administrator on 2016/4/19.
 */
public class AlbumList implements NavPagerActivity.Page {

    public static final String TAG = AlbumList.class.getSimpleName();

    NavPagerActivity containerActivity;
    View view;
    ImageView ivAdd;
    LinearLayout mLoadingLayout;
    LinearLayout mNoContentLayout;

    private ListView mainListView;

    List<MediaShare> mediaShareList;

    private long mDownTime = 0;
    private double mDiffTimeMilliSecond = 200;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    public AlbumList(NavPagerActivity activity_) {

        containerActivity = activity_;

        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(
                R.layout.album_list, null);

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mainListView = (ListView) view.findViewById(R.id.mainList);
        mainListView.setAdapter(new AlbumListViewAdapter(this));

        mLoadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
        mNoContentLayout = (LinearLayout) view.findViewById(R.id.no_content_layout);

        ivAdd = (ImageView) view.findViewById(R.id.add_album);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(containerActivity, NewAlbumPicChooseActivity.class);
                containerActivity.startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
            }
        });


    }


    public void reloadList() {
        List<MediaShare> mediaShareList1;
        mediaShareList1 = new ArrayList<>();

        for (MediaShare mediaShare : LocalCache.LocalMediaShareMapKeyIsUUID.values()) {

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {

                mediaShareList1.add(mediaShare);

            }
        }


        for (MediaShare mediaShare : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {

            if (mediaShare.isAlbum() && !mediaShare.isArchived()) {

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
        RelativeLayout lastMainbar;

        public static final int LOCAL_ITEM_WITH_NETWORK = 0;
        public static final int SUCCEED = 1;
        public static final int REMOTE_ITEM_WITHOUT_NETWORK = 2;
        public static final int EXCEPTION = 3;

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
            final MediaShare currentItem;
            final RelativeLayout mainBar;
            NetworkImageView ivMainPic;
            ImageView ivRecommand, ivCreate, ivLock;
            TextView lbHot, lbTitle, lbDesc, lbDate, lbOwner, lbPhotoCount;
            TextView lbDelete, lbShare;
            Media coverImg;
            final boolean sLocal;
            int w, h;


            if (convertView == null)
                view = LayoutInflater.from(container.containerActivity).inflate(R.layout.album_list_cell, parent, false);
            else view = convertView;

            currentItem = (MediaShare) this.getItem(position);

            mainBar = (RelativeLayout) view.findViewById(R.id.mainBar);
            ivMainPic = (NetworkImageView) view.findViewById(R.id.mainPic);
            ivRecommand = (ImageView) view.findViewById(R.id.recommand);
            ivCreate = (ImageView) view.findViewById(R.id.create);
            ivLock = (ImageView) view.findViewById(R.id.lock);
            lbHot = (TextView) view.findViewById(R.id.hot);
            lbTitle = (TextView) view.findViewById(R.id.title);
            lbPhotoCount = (TextView) view.findViewById(R.id.photo_count_tv);
            lbDesc = (TextView) view.findViewById(R.id.desc);
            lbDelete = (TextView) view.findViewById(R.id.delete);
            lbShare = (TextView) view.findViewById(R.id.share);
            lbDate = (TextView) view.findViewById(R.id.date);
            lbOwner = (TextView) view.findViewById(R.id.owner);

            //restore mainbar state
            mainBar.setTranslationX(0.0f);

            //check image
            coverImg = LocalCache.RemoteMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
            if (coverImg != null) sLocal = false;
            else {
                coverImg = LocalCache.LocalMediaMapKeyIsUUID.get(currentItem.getCoverImageDigest());
                sLocal = true;
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

            if (currentItem.getViewers().isEmpty()) {
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

                    MediaShare cloneMediaShare = currentItem.cloneMyself();

                    if (cloneMediaShare.getViewers().isEmpty()) {
                        for(String userUUID:LocalCache.RemoteUserMapKeyIsUUID.keySet()){
                            cloneMediaShare.addViewer(userUUID);
                        }
                    } else {
                        cloneMediaShare.clearViewers();
                    }

                    containerActivity.modifyMediaShare(cloneMediaShare);

                }
            });

            lbDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    containerActivity.deleteMediaShare(currentItem);

                }
            });

            mainBar.setOnTouchListener(new View.OnTouchListener() {

                float x, y, lastX, lastY, vY, vX;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int margin;
                    margin = Util.dip2px(100);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            if (lastMainbar != null) lastMainbar.setTranslationX(0.0f);
                            lastMainbar = mainBar;
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
                                else mainBar.setTranslationX(0.0f);
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

}

