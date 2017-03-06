package com.winsun.fruitmix.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.component.UnscrollableViewPager;
import com.winsun.fruitmix.contract.FileMainFragmentContract;
import com.winsun.fruitmix.contract.MainPageContract;
import com.winsun.fruitmix.presenter.FileMainFragmentPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileMainFragment extends Fragment implements FileMainFragmentContract.FileMainFragmentView {

    @BindView(R.id.title)
    TextView titleTextView;
    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.file_main_viewpager)
    UnscrollableViewPager fileMainViewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.file_main_menu)
    ImageView fileMainMenu;

    private FileFragment fileFragment;
    private FileShareFragment fileShareFragment;
    private FileDownloadFragment fileDownloadFragment;

    private FileMainFragmentContract.FileMainFragmentPresenter mPresenter;

    private MainPageContract.MainPagePresenter mainPagePresenter;

    public FileMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileMainFragment newInstance(MainPageContract.MainPagePresenter mainPagePresenter) {
        FileMainFragment fragment = new FileMainFragment();
        fragment.mainPagePresenter = mainPagePresenter;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();

        mPresenter = new FileMainFragmentPresenterImpl(mainPagePresenter);

        fileFragment = new FileFragment(activity, mPresenter);
        fileShareFragment = new FileShareFragment(activity, mPresenter);
        fileDownloadFragment = new FileDownloadFragment(activity, mPresenter);

        mPresenter.setFileDownloadFragmentPresenter(fileDownloadFragment.getPresenter());
        mPresenter.setFileFragmentPresenter(fileFragment.getPresenter());
        mPresenter.setFileShareFragmentPresenter(fileShareFragment.getPresenter());

        mPresenter.attachView(this);
        mainPagePresenter = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.file_main_layout, container, false);

        ButterKnife.bind(this, view);

        initToolbar();

        initNavigationView();

        initViewPager();

        fileMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPresenter.fileMainMenuOnClick();

            }
        });

        mPresenter.initView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        fileFragment.onResume();
        fileShareFragment.onResume();
    }

    private void initNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                mPresenter.onNavigationItemSelected(item.getItemId());

                return true;
            }
        });

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

        bottomNavigationView.getMenu().getItem(position).setChecked(false);
    }

    @Override
    public void setViewPagerCurrentItem(int position) {
        fileMainViewPager.setCurrentItem(position);
    }

    @Override
    public void setTitleText(String titleText) {
        titleTextView.setText(titleText);
    }

    private void initViewPager() {
        FilePageAdapter filePageAdapter = new FilePageAdapter();

        fileMainViewPager.setAdapter(filePageAdapter);

        fileMainViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                mPresenter.onPageSelected(position);

            }
        });

    }

    private void initToolbar() {
        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.switchDrawerOpenState();
            }
        });
    }


    @Override
    public void setNavigationIcon(int id) {
        toolbar.setNavigationIcon(id);
    }

    @Override
    public void setFileMainMenuVisibility(int visibility) {
        fileMainMenu.setVisibility(visibility);
    }

    @Override
    public int getCurrentPage() {
        return fileMainViewPager.getCurrentItem();
    }

    @Override
    public void setNavigationOnClickListener(View.OnClickListener onClickListener) {
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    private class FilePageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;

            switch (position) {
                case 0:
                    view = fileShareFragment.getView();
                    break;
                case 1:
                    view = fileFragment.getView();
                    break;
                case 2:
                    view = fileDownloadFragment.getView();
                    break;
            }

            container.addView(view);

            return view;
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * Determines whether a page View is associated with a specific key object
         * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
         * required for a PagerAdapter to function properly.
         *
         * @param view   Page View to check for association with <code>object</code>
         * @param object Object to check for association with <code>view</code>
         * @return true if <code>view</code> is associated with the key object <code>object</code>
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
