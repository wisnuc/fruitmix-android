package com.winsun.fruitmix.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.BaseAnimationListener;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.MainPageContract;
import com.winsun.fruitmix.contract.MediaFragmentContract;
import com.winsun.fruitmix.contract.MediaMainFragmentContract;
import com.winsun.fruitmix.presenter.MediaFragmentPresenterImpl;
import com.winsun.fruitmix.presenter.MediaMainFragmentPresenterImpl;
import com.winsun.fruitmix.util.Util;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaMainFragment extends Fragment implements MediaMainFragmentContract.MediaMainFragmentView {

    public static final String TAG = MediaMainFragment.class.getSimpleName();

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.right)
    TextView lbRight;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    private AlbumFragment mAlbumFragment;
    private MediaFragment mMediaFragment;
    private MediaShareFragment mMediaShareFragment;

    private MediaMainFragmentContract.MediaMainFragmentPresenter mPresenter;
    private MainPageContract.MainPagePresenter mMainPagePresenter;

    public MediaMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaMainFragment.
     */
    public static MediaMainFragment newInstance(MainPageContract.MainPagePresenter mainPagePresenter) {
        MediaMainFragment fragment = new MediaMainFragment();
        fragment.mMainPagePresenter = mainPagePresenter;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:fix ui bug

        mPresenter = new MediaMainFragmentPresenterImpl(mMainPagePresenter);

        MediaFragmentContract.MediaFragmentPresenter presenter = new MediaFragmentPresenterImpl(mPresenter, Injection.injectDataRepository(getContext()), Collections.<String>emptyList());

        mMediaFragment = new MediaFragment(getActivity(), presenter);
        mMediaShareFragment = new MediaShareFragment(getActivity());
        mAlbumFragment = new AlbumFragment(getActivity());

        mPresenter.setmMediaFragmentPresenter(mMediaFragment.getPresenter());
        mPresenter.setmMediaShareFragmentPresenter(mMediaShareFragment.getPresenter());
        mPresenter.setmAlbumFragmentPresenter(mAlbumFragment.getPresenter());

        mPresenter.attachView(this);
        mMainPagePresenter = null;

        mPresenter.onCreate();

        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.media_main_fragment, container, false);

        ButterKnife.bind(this, view);

        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.switchDrawerOpenState();
            }
        });

        initNavigationView();

        initViewPager();

        mPresenter.onCreateView();

        lbRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.selectModeBtnClick();
            }
        });

        Log.d(TAG, "onCreateView: ");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mPresenter.detachView();

        mMediaFragment.onDestroyView();
        mAlbumFragment.onDestroyView();
        mMediaShareFragment.onDestroyView();

        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();

        Log.d(TAG, "onDestroy: ");
    }


    private void initNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                mPresenter.onNavigationItemSelected(item.getItemId());

                return true;
            }
        });

        resetBottomNavigationItemCheckState();

    }

    @Override
    public void resetBottomNavigationItemCheckState() {

        int size = bottomNavigationView.getMenu().size();

        for (int i = 0; i < size; i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }

    }

    @Override
    public void setBottomNavigationItemChecked(int position) {
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void setViewPageCurrentItem(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void setTitleText(int resID) {
        title.setText(getString(resID));
    }

    @Override
    public void setTitleText(String titleText) {
        title.setText(titleText);
    }

    @Override
    public void setToolbarNavigationIcon(int resID) {
        toolbar.setNavigationIcon(resID);
    }

    @Override
    public void setSelectModeBtnVisibility(int visibility) {
        lbRight.setVisibility(visibility);
    }

    private void initViewPager() {
        MyAdapter myAdapter = new MyAdapter();
        viewPager.setAdapter(myAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                mPresenter.onPageSelected(position);

            }
        });
    }

    @Override
    public void showBottomNavAnim() {

        bottomNavigationView.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.show_bottom_item_anim);
        animation.setAnimationListener(new BaseAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
                lp.bottomMargin = Util.dip2px(getActivity(), 56.0f);
                //if(LocalCache.ScreenWidth==540) lp.bottomMargin=76;
                //else if(LocalCache.ScreenWidth==1080) lp.bottomMargin=140;
                viewPager.setLayoutParams(lp);
            }
        });


        bottomNavigationView.startAnimation(animation);
    }

    @Override
    public void dismissBottomNavAnim() {

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dismiss_bottom_item_anim);
        animation.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) viewPager.getLayoutParams();
                lp.bottomMargin = 0;
                viewPager.setLayoutParams(lp);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);

                bottomNavigationView.setVisibility(View.GONE);
            }
        });

        bottomNavigationView.startAnimation(animation);
    }

    @Override
    public void setToolbarNavigationOnClickListener(View.OnClickListener listener) {
        toolbar.setNavigationOnClickListener(listener);
    }

    @Override
    public int getCurrentViewPageItem() {
        return viewPager.getCurrentItem();
    }

    private class MyAdapter extends PagerAdapter {


        @Override
        public CharSequence getPageTitle(int position) {
            return "选X项" + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = null;

            switch (position) {
                case 0:
                    view = mAlbumFragment.getView();
                    break;
                case 1:
                    view = mMediaFragment.getView();
                    break;
                case 2:
                    view = mMediaShareFragment.getView();
                    break;
            }

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }


}
