package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MoreMediaActivity extends AppCompatActivity {
    public static final String TAG = MoreMediaActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.more_photo_gridview)
    RecyclerView mMorePhotoRecyclerView;

    private int mSpanCount = 3;
    private GridLayoutManager mManager;
    private Context mContext;
    private MorePhotoAdapter mAdapter;

    private MediaShare mediaShare;
    private ArrayList<Media> mPhotos;

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_photo);

        ButterKnife.bind(this);

        initImageLoader();

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mContext = this;

        mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);

        mManager = new GridLayoutManager(mContext, mSpanCount);
        mMorePhotoRecyclerView.setLayoutManager(mManager);
        mMorePhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MorePhotoAdapter();
        mMorePhotoRecyclerView.setAdapter(mAdapter);

        mPhotos = new ArrayList<>();
        fillPhotoList(mediaShare.getMediaDigestInMediaShareContents());
        mAdapter.notifyDataSetChanged();

    }

    private void initImageLoader() {
        mImageLoader = new ImageLoader(RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void fillPhotoList(List<String> imageDigests) {

        mPhotos.clear();

        Media picItem;
        Media picItemRaw;
        for (String str : imageDigests) {
            picItem = new Media();
            picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(str);
            if (picItemRaw != null) {
                picItem.setLocal(false);
            } else {
                picItemRaw = LocalCache.LocalMediaMapKeyIsUUID.get(str);

                picItem.setLocal(true);
                picItem.setThumb(picItemRaw.getThumb());
            }

            picItem.setUuid(picItemRaw.getUuid());
            picItem.setWidth(picItemRaw.getWidth());
            picItem.setHeight(picItemRaw.getHeight());
            picItem.setTime(picItemRaw.getTime());
            picItem.setSelected(false);

            mPhotos.add(picItem);
        }

    }

    class MorePhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.more_photo_item_layout)
        LinearLayout mMorelPhotoItemLayout;

        private Media media;
        private int width, height;

        public MorePhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            media = mPhotos.get(position);

            String imageUrl = media.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!media.isLocal());
            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mMorelPhotoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, getAdapterPosition());
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.putParcelableArrayListExtra(Util.KEY_MEDIA_LIST, mPhotos);
                    intent.setClass(mContext, PhotoSliderActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    class MorePhotoAdapter extends RecyclerView.Adapter<MorePhotoViewHolder> {
        @Override
        public int getItemCount() {
            return mPhotos == null ? 0 : mPhotos.size();
        }

        @Override
        public MorePhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.more_photo_item, parent, false);

            return new MorePhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MorePhotoViewHolder holder, int position) {

            holder.refreshView(position);
        }

    }
}
