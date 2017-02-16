package com.winsun.fruitmix.refactor.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.NewAlbumPicChooseActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaInMediaShareLoader;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.EditPhotoContract;
import com.winsun.fruitmix.refactor.presenter.EditPhotoPresenterImpl;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPhotoActivity extends BaseActivity implements View.OnClickListener, EditPhotoContract.EditPhotoView {

    public static final String TAG = EditPhotoActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.finish)
    TextView mFinish;
    @BindView(R.id.edit_photo_gridview)
    RecyclerView mEditPhotoRecyclerView;
    @BindView(R.id.add_album)
    FloatingActionButton mAddPhoto;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private int mSpanCount = 3;

    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private ImageLoader mImageLoader;

    private EditPhotoContract.EditPhotoPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        ButterKnife.bind(this);

        mContext = this;

        initImageLoader();

        mBack.setOnClickListener(this);
        mAddPhoto.setOnClickListener(this);
        mFinish.setOnClickListener(this);

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);
        mPresenter = new EditPhotoPresenterImpl(Injection.injectDataRepository(), mediaShareUUID);
        mPresenter.attachView(this);

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);
        mEditPhotoRecyclerView.setLayoutManager(mManager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

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

        mEditPhotoRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        super.dismissContentUI();

        mEditPhotoRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        mPresenter.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                mPresenter.handleBackEvent();
                break;
            case R.id.add_album:

                break;
            case R.id.finish:

                mPresenter.modifyMediaInMediaShare();

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mPresenter.handleOnActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void showMedias(List<Media> medias) {
        mAdapter.setMediaList(medias);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showOperationResultToast(OperationResult result) {
        Toast.makeText(mContext, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
    }

    class EditPhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.del_photo)
        ImageView mDelPhoto;
        @BindView(R.id.del_photo_layout)
        FrameLayout mDelPhotoLayout;

        EditPhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(Media media) {

            String imageUrl = media.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!media.isLocal());

            if (media.isLocal())
                mPhotoItem.setOrientationNumber(media.getOrientationNumber());

            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mDelPhotoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mPresenter.removeContent(getAdapterPosition());

                    mAdapter.notifyItemRemoved(getAdapterPosition());

                }
            });
        }

    }

    class EditPhotoAdapter extends RecyclerView.Adapter<EditPhotoViewHolder> {

        private List<Media> mPhotoList;

        void setMediaList(List<Media> mediaList) {
            mPhotoList = mediaList;
        }

        @Override
        public int getItemCount() {
            return mPhotoList == null ? 0 : mPhotoList.size();
        }

        @Override
        public EditPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.edit_photo_item, parent, false);

            return new EditPhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EditPhotoViewHolder holder, int position) {

            holder.refreshView(mPhotoList.get(position));
        }

    }

}
