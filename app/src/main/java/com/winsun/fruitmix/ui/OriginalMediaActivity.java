package com.winsun.fruitmix.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.component.GifTouchNetworkImageView;
import com.winsun.fruitmix.contract.OriginalMediaContract;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.presenter.OriginalMediaPresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OriginalMediaActivity extends BaseActivity implements IImageLoadListener, OriginalMediaContract.OriginalMediaView {

    public static final String TAG = OriginalMediaActivity.class.getSimpleName();

    @BindView(R.id.date)
    TextView lbDate;
    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.comment_layout)
    LinearLayout ivComment;
    @BindView(R.id.comment)
    ImageView commentImg;
    @BindView(R.id.chooseHeader)
    Toolbar rlChooseHeader;
    @BindView(R.id.panelFooter)
    RelativeLayout rlPanelFooter;
    @BindView(R.id.return_resize)
    ImageView mReturnResize;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    private static List<Media> mediaList;

    private Context mContext;

    private OriginalMediaContract.OriginalMediaPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_slider);

        mContext = this;

        ButterKnife.bind(this);

        int initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        boolean needTransition = getIntent().getBooleanExtra(Util.KEY_NEED_TRANSITION, true);

        Log.d(TAG, "onCreate: needTransition:" + needTransition);

        boolean transitionMediaNeedShowThumb = getIntent().getBooleanExtra(Util.KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB, true);

        boolean mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        initCommentBtn(mShowCommentBtn);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleBackEvent();
            }
        });

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                mPresenter.onSystemUiVisibilityChange(visibility);
            }
        });

        mPresenter = new OriginalMediaPresenterImpl(mediaList, Injection.injectDataRepository(this), initialPhotoPosition, needTransition, transitionMediaNeedShowThumb,
                getIntent().getStringExtra(Util.CURRENT_MEDIASHARE_TIME));

        mPresenter.attachView(this);
        mPresenter.initView();

        initViewPager();
        mViewPager.setCurrentItem(initialPhotoPosition);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ivBack.setImageResource(R.drawable.ic_back);
        commentImg.setImageResource(R.drawable.comment);
        mReturnResize.setImageResource(R.drawable.return_resize);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mPresenter.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaList = null;

        mContext = null;

        mPresenter.detachView();
    }

    public static void setMediaList(List<Media> mediaList) {
        OriginalMediaActivity.mediaList = mediaList;
    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();
        mViewPager.setAdapter(myAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                mPresenter.setTitle(position);
            }

        });
    }

    private void initCommentBtn(boolean mShowCommentBtn) {

        ivComment.setVisibility(View.GONE);

/*        if (mShowCommentBtn) {
            ivComment.setVisibility(View.VISIBLE);
            ivComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mediaList.size() > currentPhotoPosition) {

                        Toast.makeText(mContext, mContext.getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();

                        String imageUUID = mediaList.get(currentPhotoPosition).getUuid();

                        Intent intent = new Intent();
                        intent.setClass(PhotoSliderActivity.this, MediaShareCommentActivity.class);
                        intent.putExtra(Util.IMAGE_KEY, imageUUID);

                        intent.putExtra(Util.INITIAL_PHOTO_POSITION, currentPhotoPosition);

                        View view = mViewPager.getChildAt(currentPhotoPosition).findViewById(R.id.mainPic);

                        ViewCompat.setTransitionName(view, imageUUID);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(PhotoSliderActivity.this, view, imageUUID);

                        startActivity(intent, options.toBundle());
                    }
                }
            });
        } else {
            ivComment.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mPresenter.handleBackEvent();

    }

    @Override
    public void finishAfterTransition() {

        mPresenter.finishAfterTransition();

        super.finishAfterTransition();
    }

    @Override
    public void onImageLoadFinish(String url, View view) {

        if (url == null)
            return;

        Media media = ((GifTouchNetworkImageView) view).getCurrentMedia();

        mPresenter.handleMediaLoaded(mContext, url, media, view);

    }

    @Override
    public boolean isCurrentViewPage(int viewPosition) {
        return mViewPager.getCurrentItem() == viewPosition;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void addTransitionListener(Transition.TransitionListener listener) {
        getWindow().getSharedElementEnterTransition().addListener(listener);
    }

    @Override
    public void addOnTouchListener(GifTouchNetworkImageView view, View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @Override
    public void setTransitionName(View view, String transitionName) {
        ViewCompat.setTransitionName(view, transitionName);
    }

    @Override
    public void setDate(String date) {
        lbDate.setText(date);
    }

    @Override
    public void setHeaderVisibility(int visibility) {
        rlChooseHeader.setVisibility(visibility);
    }

    @Override
    public void setFooterVisibility(int visibility) {
        rlPanelFooter.setVisibility(visibility);
    }

    @Override
    public void setReturnResizeVisibility(int visibility) {
        mReturnResize.setVisibility(visibility);
    }

    @Override
    public void setReturnResizeOnClickListener(View.OnClickListener listener) {
        mReturnResize.setOnClickListener(listener);
    }

    @Override
    public View findViewWithTag(String tag) {
        return mViewPager.findViewWithTag(tag);
    }

    @Override
    public void hideSystemUI() {
        Util.hideSystemUI(getWindow().getDecorView());
    }

    @Override
    public void showSystemUI() {
        Util.showSystemUI(getWindow().getDecorView());
    }

    @Override
    public void finishActivity() {
        finish();
    }

    private class MyAdapter extends PagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View view;

            view = LayoutInflater.from(mContext).inflate(R.layout.photo_slider_cell, null);

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) view.findViewById(R.id.mainPic);

            mainPic.registerImageLoadListener(OriginalMediaActivity.this);

            mPresenter.instantiateItem(mContext, position, mainPic);

            container.addView(view);

            Log.d(TAG, "inistatiate position : " + position);

            return view;

        }

        private void setMainPicScreenHeight(GifTouchNetworkImageView mainPic, Media media) {

            if (media.isLocal())
                return;

            int mediaWidth = Integer.parseInt(media.getWidth());
            int mediaHeight = Integer.parseInt(media.getHeight());
            int actualWidth = 0;
            int actualHeight = 0;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mainPic.getLayoutParams();

            if (mediaWidthLargerThanHeight(media, mediaWidth, mediaHeight)) {
                actualWidth = Util.calcScreenWidth(OriginalMediaActivity.this);
                actualHeight = mediaHeight * actualWidth / mediaWidth;
            } else if (mediaHeightLargerThanWidth(media, mediaWidth, mediaHeight)) {
                actualHeight = Util.dip2px(mContext, 600);
                actualWidth = mediaWidth * actualHeight / mediaHeight;
            } else if (mediaWidthEqualsHeight(mediaWidth, mediaHeight)) {

                actualWidth = actualHeight = Util.calcScreenWidth(OriginalMediaActivity.this);
            }

            layoutParams.width = actualWidth;
            layoutParams.height = actualHeight;

            mainPic.setLayoutParams(layoutParams);
        }

        private boolean mediaWidthEqualsHeight(int mediaWidth, int mediaHeight) {
            return mediaWidth == mediaHeight;
        }

        private boolean mediaHeightLargerThanWidth(Media media, int mediaWidth, int mediaHeight) {
            return (mediaWidth < mediaHeight && media.getOrientationNumber() <= 4) || (mediaWidth > mediaHeight && media.getOrientationNumber() > 4);
        }

        private boolean mediaWidthLargerThanHeight(Media media, int mediaWidth, int mediaHeight) {
            return (mediaWidth > mediaHeight && media.getOrientationNumber() <= 4) || (mediaWidth < mediaHeight && media.getOrientationNumber() > 4);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            GifTouchNetworkImageView mainPic = (GifTouchNetworkImageView) ((View) object).findViewById(R.id.mainPic);
            mainPic.unregisterImageLoadListener();

            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mPresenter.getMediasCount();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

}
