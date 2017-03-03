package com.winsun.fruitmix.refactor.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.MoreMediaContract;
import com.winsun.fruitmix.refactor.presenter.MoreMediaPresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreMediaActivity extends BaseActivity implements MoreMediaContract.MoreMediaView {
    public static final String TAG = MoreMediaActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.more_photo_gridview)
    RecyclerView mMorePhotoRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private int mSpanCount = 3;
    private Context mContext;
    private MorePhotoAdapter mAdapter;

    private MoreMediaContract.MoreMediaPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_photo);

        ButterKnife.bind(this);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleBackEvent();
            }
        });

        mContext = this;

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);
        mMorePhotoRecyclerView.setLayoutManager(mManager);
        mMorePhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MorePhotoAdapter();
        mMorePhotoRecyclerView.setAdapter(mAdapter);

        mPresenter = new MoreMediaPresenterImpl(Injection.injectDataRepository(this), mediaShareUUID);
        mPresenter.attachView(this);
        mPresenter.loadMediaInMediaShare();
    }

    @Override
    public void showLoadingUI() {
        super.showLoadingUI();

        loadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        super.dismissLoadingUI();

        loadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        super.showNoContentUI();

        noContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        super.dismissNoContentUI();

        noContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        super.showContentUI();

        mMorePhotoRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        super.dismissContentUI();

        mMorePhotoRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        mPresenter.detachView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void showMedias(List<Media> medias) {
        mAdapter.setPhotos(medias);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void finishActivity() {
        finish();
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

        public void refreshView(final List<Media> mPhotos, int position) {
            media = mPhotos.get(position);

            mPresenter.loadMediaToView(mContext,media,mPhotoItem);

            mMorelPhotoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    OriginalMediaActivity.setMediaList(mPhotos);

                    Intent intent = new Intent();
                    intent.putExtra(Util.INITIAL_PHOTO_POSITION, getAdapterPosition());
                    intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, true);
                    intent.setClass(mContext, OriginalMediaActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    class MorePhotoAdapter extends RecyclerView.Adapter<MorePhotoViewHolder> {

        private List<Media> mPhotos;

        void setPhotos(List<Media> photos) {
            mPhotos = photos;
        }

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

            holder.refreshView(mPhotos, position);
        }

    }
}
