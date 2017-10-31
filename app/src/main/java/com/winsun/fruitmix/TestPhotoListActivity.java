package com.winsun.fruitmix;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.media.InjectMedia;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.NewPhotoListDataLoader;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.sin3hz.fastjumper.FastJumper;
import io.github.sin3hz.fastjumper.callback.LinearScrollCalculator;
import io.github.sin3hz.fastjumper.callback.SpannableCallback;

public class TestPhotoListActivity extends AppCompatActivity {

    public static final String TAG = TestPhotoListActivity.class.getSimpleName();

    @BindView(R.id.photo_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;

    private int mSpanCount = 3;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mItemWidth;

    private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

    //TODO use SparseArray or ArrayMap to optimize memory use effect

    private Map<Integer, String> mMapKeyIsPhotoPositionValueIsPhotoDate;
    private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;

    private List<Media> medias;

    private int mScreenWidth;

    private boolean mIsFling = false;

    private ImageLoader mImageLoader;

    private Context mContext;

    private PhotoRecycleAdapter mPhotoRecycleAdapter;

    private MediaDataSourceRepository mediaDataSourceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_photo_list);

        ButterKnife.bind(this);

        mContext = this;

        calcScreenWidth();

        NewPhotoListScrollListener mScrollListener = new NewPhotoListScrollListener();
        mRecyclerView.addOnScrollListener(mScrollListener);

        mPhotoRecycleAdapter = new PhotoRecycleAdapter();

        setupFastJumper();
        mRecyclerView.setAdapter(mPhotoRecycleAdapter);
        setupLayoutManager();

        mediaDataSourceRepository = InjectMedia.provideMediaDataSourceRepository(mContext);

        refreshView();
    }

    public void refreshView() {

        mediaDataSourceRepository.getMedia(new BaseLoadDataCallback<Media>() {
            @Override
            public void onSucceed(List<Media> data, OperationResult operationResult) {

                initImageLoader();

                final NewPhotoListDataLoader loader = NewPhotoListDataLoader.getInstance();

                loader.retrieveData(new NewPhotoListDataLoader.OnPhotoListDataListener() {
                    @Override
                    public void onDataLoadFinished() {
                        doAfterReloadData(loader);


                    }
                }, data);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                mLoadingLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    private void doAfterReloadData(NewPhotoListDataLoader loader) {

        List<String> mPhotoDateGroups = loader.getPhotoDateGroups();
        mMapKeyIsDateValueIsPhotoList = loader.getMapKeyIsDateValueIsPhotoList();
        mMapKeyIsPhotoPositionValueIsPhotoDate = loader.getMapKeyIsPhotoPositionValueIsPhotoDate();
        mMapKeyIsPhotoPositionValueIsPhoto = loader.getMapKeyIsPhotoPositionValueIsPhoto();
        medias = new ArrayList<>();

        for (Media media : loader.getMedias()) {

            if (media.isLocal())
                medias.add(media);

        }

        mLoadingLayout.setVisibility(View.GONE);
        if (mPhotoDateGroups.size() == 0) {
            mNoContentLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

        } else {
            mNoContentLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mPhotoRecycleAdapter.notifyDataSetChanged();

        }

    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = InjectHttp.provideImageGifLoaderInstance(mContext);
        mImageLoader = imageGifLoaderInstance.getImageLoader(this);

    }

    private void setupFastJumper() {
        mLinearScrollCalculator = new LinearScrollCalculator(mRecyclerView) {

            @Override
            public int getItemHeight(int position) {
                return mPhotoRecycleAdapter.getItemHeight(position);
            }

            @Override
            public int getSpanSize(int position) {
                return mPhotoRecycleAdapter.getSpanSize(position);
            }

            @Override
            public int getSpanCount() {
                return mSpanCount;
            }
        };

        mJumperCallback = new SpannableCallback() {
            @Override
            public boolean isSectionEnable() {
                return true;
            }

            @Override
            public String getSection(int position) {
                return mPhotoRecycleAdapter.getSectionForPosition(position);
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        mFastJumper = new FastJumper(mJumperCallback);

        mFastJumper.addListener(new FastJumper.Listener() {

            @Override
            public void onStateChange(int state) {
                super.onStateChange(state);

                if (state == FastJumper.STATE_DRAGGING) {
                    mIsFling = true;
                } else if (state == FastJumper.STATE_GONE) {
                    if (mIsFling) {

                        mIsFling = false;

                        mPhotoRecycleAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }

    private void setupGridLayoutManager() {
        calcPhotoItemWidth();
        GridLayoutManager glm = new GridLayoutManager(this, mSpanCount);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mPhotoRecycleAdapter.getSpanSize(position);
            }
        });
        mLayoutManager = glm;
        mScrollCalculator = mLinearScrollCalculator;
    }

    private void setupLayoutManager() {
        mFastJumper.attachToRecyclerView(null);
        setupGridLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mJumperCallback.setScrollCalculator(mScrollCalculator);
        mFastJumper.attachToRecyclerView(mRecyclerView);
        mFastJumper.invalidate();
    }

    private void calcScreenWidth() {

        mScreenWidth = Util.calcScreenWidth(this);
    }

    private void calcPhotoItemWidth() {
        mItemWidth = mScreenWidth / mSpanCount - Util.dip2px(this, 5);
    }

    private class PhotoRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEAD = 0;
        private static final int VIEW_TYPE_CONTENT = 1;

        private int mSubHeaderHeight = getResources().getDimensionPixelSize(R.dimen.photo_title_height);

        PhotoRecycleAdapter() {
            setHasStableIds(true);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Log.i(TAG, "onBindViewHolder: ");

            int type = getItemViewType(position);
            switch (type) {

                case VIEW_TYPE_CONTENT:
                    PhotoHolder photoHolder = (PhotoHolder) holder;
                    photoHolder.refreshView(position);
                    break;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Log.i(TAG, "onCreateViewHolder: ");

            switch (viewType) {

                case VIEW_TYPE_CONTENT: {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.test_photo_list_item, parent, false);

                    return new PhotoHolder(view);
                }
            }

            return null;
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return super.onFailedToRecycleView(holder);
        }

        @Override
        public int getItemCount() {
            return medias.size();
        }

        @Override
        public int getItemViewType(int position) {

            return VIEW_TYPE_CONTENT;

        }

        int getSpanSize(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mSpanCount;
            } else {
                return 1;
            }
        }

        int getItemHeight(int position) {
            if (getItemViewType(position) == VIEW_TYPE_HEAD) {
                return mItemWidth;
            } else {
                return mSubHeaderHeight;
            }
        }

        String getSectionForPosition(int position) {
            String title;

            if (position < 0) {
                position = 0;
            }
            if (position >= getItemCount()) {
                position = getItemCount() - 1;
            }

            if (mMapKeyIsPhotoPositionValueIsPhotoDate.containsKey(position))
                title = mMapKeyIsPhotoPositionValueIsPhotoDate.get(position);
            else {
                title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getDate();
            }

            if (title.contains(Util.DEFAULT_DATE)) {
                return getString(R.string.unknown_time);
            } else {
                String[] titleSplit = title.split("-");
                return titleSplit[0] + "年" + titleSplit[1] + "月";
            }

        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_iv)
        NetworkImageView mPhotoIv;

        @BindView(R.id.photo_item_layout)
        ViewGroup mImageLayout;

        PhotoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int position) {

            final Media currentMedia = medias.get(position);

            if (currentMedia == null) return;

            HttpRequest httpRequest;

            if (!mIsFling) {

                httpRequest = currentMedia.getImageThumbUrl(mContext);

            } else {

                httpRequest = currentMedia.getImageSmallThumbUrl(mContext);

            }

            MediaUtil.setMediaImageUrl(currentMedia,mPhotoIv,httpRequest,mImageLoader);


            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getDate());
            int mediaInListPosition = getPosition(mediaList, currentMedia);

            setPhotoItemMargin(mediaInListPosition);

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i(TAG, "onClick: media thumb: " + currentMedia.getThumb() + " mini thumb: " + currentMedia.getMiniThumbPath());

                }
            });

        }

        private void setPhotoItemMargin(int mediaInListPosition) {

            int normalMargin = Util.dip2px(mContext, 2.5f);

            if ((mediaInListPosition + 1) % mSpanCount == 0) {

                Util.setMarginAndHeight(mImageLayout, mItemWidth, normalMargin, normalMargin, normalMargin, 0);

            } else {

                Util.setMarginAndHeight(mImageLayout, mItemWidth, normalMargin, normalMargin, 0, 0);

            }

        }

        private int getPosition(List<Media> mediaList, Media media) {

            int position = 0;
            int size = mediaList.size();

            for (int i = 0; i < size; i++) {
                Media media1 = mediaList.get(i);

                if (media.getKey().equals(media1.getKey())) {
                    position = i;
                }
            }
            return position;
        }
    }

    private class NewPhotoListScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {

                mIsFling = true;

            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                if (mIsFling) {

                    mIsFling = false;

                    mPhotoRecycleAdapter.notifyDataSetChanged();
                }

            }
        }

    }

}
