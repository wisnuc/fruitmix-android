package com.winsun.fruitmix.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.BaseAnimationListener;
import com.winsun.fruitmix.contract.MediaFragmentContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.MediaFragmentDataLoader;
import com.winsun.fruitmix.util.Util;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.sin3hz.fastjumper.FastJumper;
import io.github.sin3hz.fastjumper.callback.LinearScrollCalculator;
import io.github.sin3hz.fastjumper.callback.SpannableCallback;

public class MediaFragment implements MediaFragmentContract.MediaFragmentView, View.OnClickListener {

    public static final String TAG = MediaFragment.class.getSimpleName();

    private Activity containerActivity;
    private View view;

    @BindView(R.id.photo_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout mNoContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.bt_album)
    ImageView ivBtAlbum;
    @BindView(R.id.bt_share)
    ImageView ivBtShare;

    private Animator mAnimator;

    private int mSpanCount = 3;

    private PhotoRecycleAdapter mPhotoRecycleAdapter;

    private FastJumper mFastJumper;
    private SpannableCallback mJumperCallback;
    private SpannableCallback.ScrollCalculator mLinearScrollCalculator;
    private SpannableCallback.ScrollCalculator mScrollCalculator;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mItemWidth;

    private int mScreenWidth;

    private boolean mIsFling = false;

    private boolean mUseAnim = false;

    private float mOldSpan = 0;
    private ScaleGestureDetector mPinchScaleDetector;

    private List<String> alreadySelectedImageKeyArrayList;

    private MediaFragmentContract.MediaFragmentPresenter mPresenter;

    private ProgressDialog mDialog;

    public MediaFragment(Activity activity, MediaFragmentContract.MediaFragmentPresenter presenter) {
        containerActivity = activity;

        view = View.inflate(containerActivity,R.layout.new_photo_layout,null);

//        view = LayoutInflater.from(containerActivity.getApplicationContext()).inflate(R.layout.new_photo_layout, null);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_photo);

        calcScreenWidth();

        mPinchScaleDetector = new ScaleGestureDetector(containerActivity, new PinchScaleListener());

        NewPhotoListScrollListener mScrollListener = new NewPhotoListScrollListener();
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mPinchScaleDetector.onTouchEvent(event);

                return false;
            }
        });

        mPhotoRecycleAdapter = new PhotoRecycleAdapter();

        setupFastJumper();
        mRecyclerView.setAdapter(mPhotoRecycleAdapter);
        setupLayoutManager();

        mPresenter = presenter;
        mPresenter.attachView(this);

        fab.setOnClickListener(this);
        ivBtAlbum.setOnClickListener(this);
        ivBtShare.setOnClickListener(this);
    }

    public MediaFragmentContract.MediaFragmentPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab:
                mPresenter.fabOnClick();
                break;
            case R.id.bt_album:
                mPresenter.albumBtnOnClick();
                break;
            case R.id.bt_share:
                mPresenter.shareBtnOnClick();
                break;
        }

    }

    @Override
    public void setFABVisibility(int visibility) {
        fab.setVisibility(visibility);
    }

    public void collapseFab() {

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.fab_restore);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.album_btn_restore);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.share_btn_restore);
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ivBtAlbum.setVisibility(View.GONE);
                ivBtShare.setVisibility(View.GONE);

            }
        });

    }

    public void expandFab() {

        ivBtAlbum.setVisibility(View.VISIBLE);
        ivBtShare.setVisibility(View.VISIBLE);

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.fab_remote);
        mAnimator.setTarget(fab);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.album_btn_translation);
        mAnimator.setTarget(ivBtAlbum);
        mAnimator.start();

        mAnimator = AnimatorInflater.loadAnimator(containerActivity, R.animator.share_btn_translation);
        mAnimator.setTarget(ivBtShare);
        mAnimator.start();

    }

    @Override
    public void showMedias(MediaFragmentDataLoader loader) {

        mPhotoRecycleAdapter.setData(loader);
        mPhotoRecycleAdapter.notifyDataSetChanged();

    }

    public void setAlreadySelectedImageKeyArrayList(List<String> alreadySelectedImageKeyArrayList) {
        this.alreadySelectedImageKeyArrayList = alreadySelectedImageKeyArrayList;
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

    }

    private void setupGridLayoutManager() {
        calcPhotoItemWidth();
        GridLayoutManager glm = new GridLayoutManager(containerActivity, mSpanCount);
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

        mScreenWidth = Util.calcScreenWidth(containerActivity);
    }

    private void calcPhotoItemWidth() {
        mItemWidth = mScreenWidth / mSpanCount - Util.dip2px(containerActivity, 5);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void notifyDataSetChangedUseAnim() {
        mUseAnim = true;
        mPhotoRecycleAdapter.notifyDataSetChanged();
    }

    @Override
    public void smoothScrollToPosition(int position) {
        mRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.postponeEnterTransition(containerActivity);
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mRecyclerView.requestLayout();
                ActivityCompat.startPostponedEnterTransition(containerActivity);

                return true;
            }
        });
    }

    @Override
    public View findViewByMedia(Media media) {
        return mRecyclerView.findViewWithTag(findPhotoTag(media));
    }

    @Override
    public void startCreateAlbumActivity() {
        Intent intent = new Intent();
        intent.setClass(containerActivity, CreateAlbumActivity.class);
        containerActivity.startActivityForResult(intent, Util.KEY_CREATE_ALBUM_REQUEST_CODE);
    }

    @Override
    public void showSelectNothingToast() {
        Toast.makeText(containerActivity, containerActivity.getString(R.string.select_nothing), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
    }


    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

        mPresenter.onMapSharedElements(names, sharedElements);

    }

    public void onActivityReenter(int resultCode, Intent data) {

        mPresenter.onActivityReenter(resultCode, data);

    }

    private String findPhotoTag(Media media) {
        return mPresenter.loadImageThumbUrl(media);
    }

    @Override
    public boolean isNetworkAlive() {
        return Util.getNetworkState(containerActivity);
    }

    @Override
    public void showNoNetwork() {
        Toast.makeText(containerActivity, containerActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadingUI() {
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoadingUI() {
        mLoadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoContentUI() {
        mNoContentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNoContentUI() {
        mNoContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showContentUI() {
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissContentUI() {
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showDialog() {
        mDialog = ProgressDialog.show(containerActivity, containerActivity.getString(R.string.operating_title), null, true, false);
    }

    @Override
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    public void hideSoftInput() {

    }

    private class PhotoRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEAD = 0;
        private static final int VIEW_TYPE_CONTENT = 1;

        private int mSubHeaderHeight = containerActivity.getResources().getDimensionPixelSize(R.dimen.photo_title_height);

        private int mAdapterItemTotalCount = 0;
        private SparseArray<String> mMapKeyIsPhotoPositionValueIsPhotoDate;
        private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;
        private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;
        private List<Media> mMedias;

        PhotoRecycleAdapter() {
            setHasStableIds(true);
        }

        void setData(MediaFragmentDataLoader loader) {
            mMapKeyIsDateValueIsPhotoList = loader.getMapKeyIsDateValueIsPhotoList();
            mMapKeyIsPhotoPositionValueIsPhoto = loader.getMapKeyIsPhotoPositionValueIsPhoto();
            mMapKeyIsPhotoPositionValueIsPhotoDate = loader.getMapKeyIsPhotoPositionValueIsPhotoDate();
            mMedias = loader.getMedias();
            mAdapterItemTotalCount = loader.getAdapterItemTotalCount();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int type = getItemViewType(position);
            switch (type) {
                case VIEW_TYPE_HEAD:
                    PhotoGroupHolder groupHolder = (PhotoGroupHolder) holder;
                    groupHolder.refreshView(position, mMapKeyIsPhotoPositionValueIsPhotoDate, mMapKeyIsDateValueIsPhotoList);
                    break;
                case VIEW_TYPE_CONTENT:
                    PhotoHolder photoHolder = (PhotoHolder) holder;
                    photoHolder.refreshView(position, mMapKeyIsPhotoPositionValueIsPhoto, mMapKeyIsDateValueIsPhotoList, mMedias);
                    break;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEAD: {
                    View view = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_title_item, parent, false);
                    return new PhotoGroupHolder(view);
                }
                case VIEW_TYPE_CONTENT: {
                    View view = LayoutInflater.from(containerActivity).inflate(R.layout.new_photo_gridlayout_item, parent, false);
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
            return mAdapterItemTotalCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (mMapKeyIsPhotoPositionValueIsPhotoDate.indexOfKey(position) >= 0)
                return VIEW_TYPE_HEAD;
            else
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

            if (mMapKeyIsPhotoPositionValueIsPhotoDate.indexOfKey(position) >= 0)
                title = mMapKeyIsPhotoPositionValueIsPhotoDate.get(position);
            else {
                title = mMapKeyIsPhotoPositionValueIsPhoto.get(position).getDate();
            }

            if (title.contains(Util.DEFAULT_DATE)) {
                return containerActivity.getString(R.string.unknown_time);
            } else {
                String[] titleSplit = title.split("-");
                return titleSplit[0] + "年" + titleSplit[1] + "月";
            }

        }
    }

    class PhotoGroupHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_group_tv)
        TextView mPhotoTitle;

        @BindView(R.id.photo_title_select_img)
        ImageView mPhotoTitleSelectImg;

        @BindView(R.id.photo_title_layout)
        LinearLayout mPhotoTitleLayout;

        @BindView(R.id.spacing_layout)
        View mSpacingLayout;

        @BindView(R.id.spacing_second_layout)
        View mSpacingSecondLayout;

        private SparseArray<String> mMapKeyIsPhotoPositionValueIsPhotoDate;
        private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;

        PhotoGroupHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition, SparseArray<String> mapKeyIsPhotoPositionValueIsPhotoDate, Map<String, List<Media>> mapKeyIsDateValueIsPhotoList) {

            mMapKeyIsDateValueIsPhotoList = mapKeyIsDateValueIsPhotoList;
            mMapKeyIsPhotoPositionValueIsPhotoDate = mapKeyIsPhotoPositionValueIsPhotoDate;

            final int selectCount = mPresenter.getSelectCount();

            final String date = mMapKeyIsPhotoPositionValueIsPhotoDate.get(groupPosition);

            if (groupPosition == 0) {
                mSpacingLayout.setVisibility(View.GONE);
                mSpacingSecondLayout.setVisibility(View.GONE);
            } else {
                mSpacingLayout.setVisibility(View.VISIBLE);
                mSpacingSecondLayout.setVisibility(View.VISIBLE);
            }

            if (date.equals(Util.DEFAULT_DATE)) {
                mPhotoTitle.setText(containerActivity.getString(R.string.unknown_time));
            } else {
                mPhotoTitle.setText(date);
            }

            if (mPresenter.isSelectState()) {

                if (!isPhotoTitleSelectImgVisible()) {
                    if (mUseAnim) {
                        showPhotoTitleSelectImgAnim();
                    } else
                        showPhotoTitleSelectImg();
                }

                List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                int selectNum = 0;
                for (Media media : mediaList) {
                    if (media.isSelected())
                        selectNum++;
                }
                if (selectNum == mediaList.size())
                    mPhotoTitleSelectImg.setSelected(true);
                else
                    mPhotoTitleSelectImg.setSelected(false);

            } else {

                if (isPhotoTitleSelectImgVisible()) {
                    if (mUseAnim) {
                        dismissPhotoTitleSelectImgAnim();
                    } else
                        dismissPhotoTitleSelectImg();
                }
            }


            mPhotoTitleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPresenter.isSelectState()) {

                        List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(date);
                        boolean selected = mPhotoTitleSelectImg.isSelected();

                        int unSelectNumInList = 0;

                        for (Media media : mediaList) {
                            if (!media.isSelected())
                                unSelectNumInList++;
                        }

                        mPresenter.calcSelectedPhoto();

                        if (!selected && unSelectNumInList + selectCount > Util.MAX_PHOTO_SIZE) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

                            int newSelectItemCount = 0;
                            int newSelectItemTotalCount = Util.MAX_PHOTO_SIZE - selectCount;
                            if (newSelectItemTotalCount != 0) {

                                for (Media media : mediaList) {

                                    if (!media.isSelected()) {
                                        media.setSelected(true);
                                        newSelectItemCount++;
                                        if (newSelectItemCount == newSelectItemTotalCount)
                                            break;
                                    }

                                }
                            }

                        } else {
                            mPhotoTitleSelectImg.setSelected(!selected);

                            for (Media media : mediaList)
                                media.setSelected(!selected);
                        }

                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        mPresenter.calcSelectedPhoto();

                        mPresenter.setSelectCountText(String.format(containerActivity.getString(R.string.select_count), mPresenter.getSelectCount()));
                    }
                }
            });

        }

        private boolean isPhotoTitleSelectImgVisible() {
            return mPhotoTitleSelectImg.getVisibility() == View.VISIBLE;
        }

        private void showPhotoTitleSelectImgAnim() {
            mPhotoTitleSelectImg.setVisibility(View.VISIBLE);

            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.show_right_item_anim);

            mPhotoTitleSelectImg.startAnimation(animation);

        }

        private void dismissPhotoTitleSelectImgAnim() {
            Animation animation = AnimationUtils.loadAnimation(containerActivity, R.anim.dismiss_right_item_anim);
            animation.setAnimationListener(new BaseAnimationListener() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);

                    mPhotoTitleSelectImg.setVisibility(View.GONE);

                }
            });

            mPhotoTitleSelectImg.startAnimation(animation);

        }

        private void showPhotoTitleSelectImg() {
            mPhotoTitleSelectImg.setVisibility(View.VISIBLE);
        }

        private void dismissPhotoTitleSelectImg() {
            mPhotoTitleSelectImg.setVisibility(View.GONE);
        }

    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_iv)
        NetworkImageView mPhotoIv;

        @BindView(R.id.photo_item_layout)
        RelativeLayout mImageLayout;

        @BindView(R.id.photo_select_img)
        ImageView mPhotoSelectedIv;

        private SparseArray<Media> mMapKeyIsPhotoPositionValueIsPhoto;
        private Map<String, List<Media>> mMapKeyIsDateValueIsPhotoList;
        private List<Media> mMedias;

        PhotoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void refreshView(int position, SparseArray<Media> mapKeyIsPhotoPositionValueIsPhoto, Map<String, List<Media>> mapKeyIsDateValueIsPhotoList, List<Media> medias) {

            mMedias = medias;
            mMapKeyIsDateValueIsPhotoList = mapKeyIsDateValueIsPhotoList;
            mMapKeyIsPhotoPositionValueIsPhoto = mapKeyIsPhotoPositionValueIsPhoto;

            final Media currentMedia = mMapKeyIsPhotoPositionValueIsPhoto.get(position);

            if (currentMedia == null) return;

            // because alreadySelectedImageKeyArrayList is media share content,it must be uuid about media now
            if (alreadySelectedImageKeyArrayList != null && alreadySelectedImageKeyArrayList.contains(currentMedia.getUuid()))
                currentMedia.setSelected(true);

//            mImageLoader.setTag(position);

            if (!mIsFling) {
                mPresenter.loadMediaToView(containerActivity, currentMedia, mPhotoIv);
            } else {
                mPresenter.cancelLoadMediaToView(mPhotoIv);
            }

            List<Media> mediaList = mMapKeyIsDateValueIsPhotoList.get(currentMedia.getDate());
            int mediaInListPosition = getPosition(mediaList, currentMedia);

            setPhotoItemMargin(mediaInListPosition);

            if (mPresenter.isSelectState()) {
                boolean selected = currentMedia.isSelected();
                if (selected) {
                    int selectMargin = Util.dip2px(containerActivity, 20);
                    setPhotoIvLayoutParams(selectMargin);
                    mPhotoSelectedIv.setVisibility(View.VISIBLE);
                } else {
                    setPhotoIvLayoutParams(0);
                    mPhotoSelectedIv.setVisibility(View.GONE);
                }
            } else {

                currentMedia.setSelected(false);

                setPhotoIvLayoutParams(0);
                mPhotoSelectedIv.setVisibility(View.GONE);
            }

            mImageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPresenter.isSelectState()) {

                        if (!currentMedia.isSharing()) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.photo_not_sharing), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (alreadySelectedImageKeyArrayList != null && alreadySelectedImageKeyArrayList.contains(currentMedia.getUuid())) {
                            Toast.makeText(containerActivity, containerActivity.getString(R.string.already_select_media), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mPresenter.calcSelectedPhoto();

                        boolean selected = currentMedia.isSelected();

                        int selectCount = mPresenter.getSelectCount();

                        if (!selected && selectCount >= Util.MAX_PHOTO_SIZE) {

                            Toast.makeText(containerActivity, containerActivity.getString(R.string.max_select_photo), Toast.LENGTH_SHORT).show();

                            return;
                        }

                        if (selected) {
                            int selectMargin = Util.dip2px(containerActivity, 20);
                            setPhotoIvLayoutParams(selectMargin);
                            mPhotoSelectedIv.setVisibility(View.VISIBLE);
                        } else {
                            setPhotoIvLayoutParams(0);
                            mPhotoSelectedIv.setVisibility(View.GONE);
                        }

                        currentMedia.setSelected(!selected);
                        mPhotoRecycleAdapter.notifyDataSetChanged();

                        mPresenter.calcSelectedPhoto();

                        mPresenter.setSelectCountText(String.format(containerActivity.getString(R.string.select_count), mPresenter.getSelectCount()));

                    } else {

                        int initialPhotoPosition;

                        int size = mMedias.size();

                        for (initialPhotoPosition = 0; initialPhotoPosition < size; initialPhotoPosition++) {

                            Media media = mMedias.get(initialPhotoPosition);

                            if (media.getKey().equals(currentMedia.getKey()))
                                break;

                        }

                        OriginalMediaActivity.setMediaList(mMedias);

                        Intent intent = new Intent();
                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
                        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, false);
                        intent.setClass(containerActivity, OriginalMediaActivity.class);

                        if (mPhotoIv.isLoaded()) {

                            ViewCompat.setTransitionName(mPhotoIv, currentMedia.getKey());

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(containerActivity, mPhotoIv, currentMedia.getKey());

                            containerActivity.startActivity(intent, options.toBundle());
                        } else {

                            intent.putExtra(Util.KEY_NEED_TRANSITION, false);
                            containerActivity.startActivity(intent);

                        }

                    }

                    Log.d(TAG, "image key:" + currentMedia.getKey());
                }
            });

            mImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (!mPresenter.isSelectState()) {
                        mPresenter.enterChooseMode();

                        currentMedia.setSelected(true);

                        mPresenter.calcSelectedPhoto();

                        mPresenter.setSelectCountText(String.format(containerActivity.getString(R.string.select_count), mPresenter.getSelectCount()));

                    }

                    return true;
                }
            });

        }

        private void setPhotoItemMargin(int mediaInListPosition) {
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) mImageLayout.getLayoutParams();

            params.height = mItemWidth;

            int normalMargin = Util.dip2px(containerActivity, 2.5f);

            if ((mediaInListPosition + 1) % mSpanCount == 0) {
                params.setMargins(normalMargin, normalMargin, normalMargin, 0);
            } else {
                params.setMargins(normalMargin, normalMargin, 0, 0);
            }

            mImageLayout.setLayoutParams(params);
        }

        private void setPhotoIvLayoutParams(int margin) {
            RelativeLayout.LayoutParams photoParams = (RelativeLayout.LayoutParams) mPhotoIv.getLayoutParams();
            photoParams.setMargins(margin, margin, margin, margin);
            mPhotoIv.setLayoutParams(photoParams);
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

    private class PinchScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private int mSpanMaxCount = 6;
        private int mSpanMinCount = 2;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mOldSpan = detector.getCurrentSpan();

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            float newSpan = detector.getCurrentSpan();

            if (newSpan > mOldSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount > mSpanMinCount) {
                    mSpanCount--;
                    calcPhotoItemWidth();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch more");

            } else if (mOldSpan > newSpan + Util.dip2px(containerActivity, 2)) {

                if (mSpanCount < mSpanMaxCount) {
                    mSpanCount++;
                    calcPhotoItemWidth();
                    ((GridLayoutManager) mLayoutManager).setSpanCount(mSpanCount);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mPhotoRecycleAdapter.notifyItemRangeChanged(0, mPhotoRecycleAdapter.getItemCount());
                }

                Log.i(TAG, "pinch less");
            }

        }
    }

}
