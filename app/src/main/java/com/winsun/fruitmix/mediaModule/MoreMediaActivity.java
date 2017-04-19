package com.winsun.fruitmix.mediaModule;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaInMediaShareLoader;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreMediaActivity extends AppCompatActivity {

    public static final String TAG = "MoreMediaActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.more_photo_gridview)
    RecyclerView mMorePhotoRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    public static final int SPAN_COUNT = 2;

    private Context mContext;
    private List<Media> mPhotos;
    private ImageLoader mImageLoader;
    private MediaShare mediaShare;
    private MorePhotoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_photo);

        ButterKnife.bind(this);

        initImageLoader();

        mTitleTextView.setVisibility(View.INVISIBLE);

        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mContext = this;

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);
        mediaShare = LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);

//        GridLayoutManager manager = new GridLayoutManager(mContext, SPAN_COUNT);

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);

        mMorePhotoRecyclerView.setLayoutManager(manager);
        mMorePhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MorePhotoAdapter();
        mMorePhotoRecyclerView.setAdapter(mAdapter);

        loadMedia();
    }

    private void loadMedia() {
        final MediaInMediaShareLoader loader = MediaInMediaShareLoader.INSTANCE;
        loader.startLoad(mediaShare.getMediaUUIDInMediaShareContents(), new MediaInMediaShareLoader.OnMediaInMediaShareLoadListener() {
            @Override
            public void onMediaInMediaShareLoaded() {

                mPhotos = loader.getMedias();

                loadingLayout.setVisibility(View.GONE);
                if (mPhotos.isEmpty()) {
                    noContentLayout.setVisibility(View.VISIBLE);
                } else {
                    mMorePhotoRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                }

            }
        },true,true);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);
    }

    class MorePhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.more_photo_item_layout)
        LinearLayout mMorelPhotoItemLayout;

        private Media media;

        MorePhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            media = mPhotos.get(position);

            String imageUrl = media.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!media.isLocal());

            if (media.isLocal())
                mPhotoItem.setOrientationNumber(media.getOrientationNumber());

            setMainPicScreenHeight(mPhotoItem, media);

            mPhotoItem.setBackgroundResource(R.drawable.default_place_holder);
//            mPhotoItem.setBackgroundColor(ContextCompat.getColor(mContext,R.color.default_imageview_color));

            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.default_place_holder);
//            mPhotoItem.setDefaultBackgroundColor(ContextCompat.getColor(mContext,R.color.default_imageview_color));

            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mMorelPhotoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PhotoSliderActivity.setMediaList(mPhotos);

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, getAdapterPosition());
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.setClass(mContext, PhotoSliderActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    private void setMainPicScreenHeight(NetworkImageView mainPic, Media media) {

        int mediaWidth = Integer.parseInt(media.getWidth());
        int mediaHeight = Integer.parseInt(media.getHeight());
        int actualWidth;
        int actualHeight;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainPic.getLayoutParams();

        actualWidth = Util.calcScreenWidth(MoreMediaActivity.this) / SPAN_COUNT;
        actualHeight = mediaHeight * actualWidth / mediaWidth;

        layoutParams.width = actualWidth;
        layoutParams.height = actualHeight;

        mainPic.setLayoutParams(layoutParams);
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
