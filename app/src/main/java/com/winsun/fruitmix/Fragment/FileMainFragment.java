package com.winsun.fruitmix.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.UnscrollableViewPager;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.fragment.FileDownloadFragment;
import com.winsun.fruitmix.fileModule.fragment.FileFragment;
import com.winsun.fruitmix.fileModule.fragment.FileShareFragment;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileMainFragment extends Fragment implements OnFileFragmentInteractionListener {

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.file_main_viewpager)
    UnscrollableViewPager fileMainViewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.right)
    TextView rightTextView;

    public static final int PAGE_FILE_SHARE = 0;
    public static final int PAGE_FILE = 1;
    public static final int PAGE_FILE_DOWNLOAD = 2;

    private FileFragment fileFragment;
    private FileShareFragment fileShareFragment;
    private FileDownloadFragment fileDownloadFragment;

    private Context context;

    private OnMainFragmentInteractionListener mListener;


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
    public static FileMainFragment newInstance() {
        FileMainFragment fragment = new FileMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO:modify layout to change ui effect same as ios app,modify no content ui effect 

        View view = inflater.inflate(R.layout.activity_file_main, container, false);

        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        setHasOptionsMenu(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });

        rightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fileFragment.isSelectMode()) {
                    rightTextView.setText(getString(R.string.select_file));
                    fileFragment.refreshSelectMode(false);
                } else {
                    rightTextView.setText(getString(R.string.quit_select_file));
                    fileFragment.refreshSelectMode(true);
                }

            }
        });

        FilePageAdapter filePageAdapter = new FilePageAdapter(getActivity().getSupportFragmentManager());

        fileMainViewPager.setAdapter(filePageAdapter);
        fileMainViewPager.setCurrentItem(PAGE_FILE);

        fileMainViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == PAGE_FILE) {
                    rightTextView.setVisibility(View.VISIBLE);
                } else {
                    rightTextView.setVisibility(View.GONE);
                }

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.share:
                        fileMainViewPager.setCurrentItem(PAGE_FILE_SHARE);
                        break;
                    case R.id.file:
                        fileMainViewPager.setCurrentItem(PAGE_FILE);
                        break;
                    case R.id.download:
                        fileMainViewPager.setCurrentItem(PAGE_FILE_DOWNLOAD);
                        break;
                }

                return true;
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            if (fileMainViewPager.getCurrentItem() == PAGE_FILE) {
                fileFragment.handleOperationResult(operationEvent);
            } else if (fileMainViewPager.getCurrentItem() == PAGE_FILE_SHARE) {
                fileShareFragment.handleOperationEvent(operationEvent);
            }
        } else if (action.equals(Util.REMOTE_FILE_SHARE_RETRIEVED)) {
            fileShareFragment.handleOperationEvent(operationEvent);
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentInteractionListener) {
            mListener = (OnMainFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (fileMainViewPager.getCurrentItem() == PAGE_FILE)
            fileFragment.requestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void handleBackPressed() {

        if (fileMainViewPager.getCurrentItem() == PAGE_FILE) {
            fileFragment.onBackPressed();
        } else if (fileMainViewPager.getCurrentItem() == PAGE_FILE_SHARE) {
            fileShareFragment.onBackPressed();
        }

    }

    public boolean handleBackPressedOrNot() {

        if (fileMainViewPager.getCurrentItem() == PAGE_FILE) {
            return fileFragment.notRootFolder();
        }
        if (fileMainViewPager.getCurrentItem() == PAGE_FILE_SHARE) {
            return fileShareFragment.notRootFolder();
        }
        return false;

    }

    @Override
    public void changeFilePageToFileFragment() {
        fileMainViewPager.setCurrentItem(PAGE_FILE);
    }

    @Override
    public void changeFilePageToFileShareFragment() {
        fileMainViewPager.setCurrentItem(PAGE_FILE_SHARE);
    }

    @Override
    public void changeFilePageToFileDownloadFragment() {
        fileMainViewPager.setCurrentItem(PAGE_FILE_DOWNLOAD);
    }


    private class FilePageAdapter extends FragmentPagerAdapter {

        FilePageAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case PAGE_FILE:
                    fileFragment = FileFragment.newInstance(FileMainFragment.this);
                    return fileFragment;
                case PAGE_FILE_SHARE:
                    fileShareFragment = FileShareFragment.newInstance();
                    return fileShareFragment;
                case PAGE_FILE_DOWNLOAD:
                    fileDownloadFragment = FileDownloadFragment.newInstance();
                    return fileDownloadFragment;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
