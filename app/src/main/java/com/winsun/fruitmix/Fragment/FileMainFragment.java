package com.winsun.fruitmix.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.UnscrollableViewPager;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.fragment.FileDownloadFragment;
import com.winsun.fruitmix.fileModule.fragment.FileFragment;
import com.winsun.fruitmix.fileModule.fragment.FileShareFragment;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.OnMainFragmentInteractionListener;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
    @BindView(R.id.file_main_menu)
    ImageView fileMainMenu;

    public static final int PAGE_FILE_SHARE = 0;
    public static final int PAGE_FILE = 1;
    public static final int PAGE_FILE_DOWNLOAD = 2;

    private FileFragment fileFragment;
    private FileShareFragment fileShareFragment;
    private FileDownloadFragment fileDownloadFragment;

    private Context context;

    private OnMainFragmentInteractionListener mListener;

    private BottomSheetDialog bottomSheetDialog;

    private View bottomSheetView;

    private BottomSheetRecyclerViewAdapter bottomSheetRecyclerViewAdapter;

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

        initBottomSheetDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO:modify layout to change ui effect same as ios app,modify no content ui effect

        View view = inflater.inflate(R.layout.activity_file_main, container, false);

        ButterKnife.bind(this, view);

        initToolbar();

        initViewPager();

        initNavigationView();

        fileMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fileMainViewPager.getCurrentItem() == PAGE_FILE) {
                    showBottomSheetDialog(fileFragment.getMainMenuItem());
                } else if (fileMainViewPager.getCurrentItem() == PAGE_FILE_DOWNLOAD) {
                    showBottomSheetDialog(fileDownloadFragment.getMainMenuItem());
                }

            }
        });


        return view;
    }

    @Override
    public void dismissBottomSheetDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing())
            bottomSheetDialog.dismiss();
    }

    @Override
    public void showBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        bottomSheetRecyclerViewAdapter.setBottomMenuItems(bottomMenuItems);
        bottomSheetRecyclerViewAdapter.notifyDataSetChanged();

        View parent = (View) bottomSheetView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

/*        bottomSheetView.measure(0, 0);
        behavior.setPeekHeight(bottomSheetView.getMeasuredHeight());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        parent.setLayoutParams(params);*/
        bottomSheetDialog.show();

    }

    private void initBottomSheetDialog() {

        bottomSheetView = View.inflate(getActivity(), R.layout.bottom_sheet_dialog_layout, null);

        RecyclerView bottomSheetRecyclerView = (RecyclerView) bottomSheetView.findViewById(R.id.bottom_sheet_recyclerview);

        bottomSheetRecyclerViewAdapter = new BottomSheetRecyclerViewAdapter();

        bottomSheetRecyclerView.setAdapter(bottomSheetRecyclerViewAdapter);

        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        bottomSheetDialog = new BottomSheetDialog(getActivity());

        bottomSheetDialog.setContentView(bottomSheetView);
    }

    private class BottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<BottomSheetRecyclerViewViewHolder> {

        private List<BottomMenuItem> bottomMenuItems;

        BottomSheetRecyclerViewAdapter() {
            bottomMenuItems = new ArrayList<>();
        }

        @Override
        public BottomSheetRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet_dialog_item, parent, false);

            return new BottomSheetRecyclerViewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BottomSheetRecyclerViewViewHolder holder, int position) {
            holder.refreshView(bottomMenuItems.get(position));
        }

        @Override
        public int getItemCount() {
            return bottomMenuItems.size();
        }

        void setBottomMenuItems(List<BottomMenuItem> bottomMenuItems) {
            this.bottomMenuItems.clear();
            this.bottomMenuItems.addAll(bottomMenuItems);
        }
    }


    class BottomSheetRecyclerViewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_text)
        TextView itemTextView;

        BottomSheetRecyclerViewViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void refreshView(final BottomMenuItem bottomMenuItem) {
            itemTextView.setText(bottomMenuItem.getText());

            itemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomMenuItem.handleOnClickEvent();
                }
            });
        }
    }


    private void initNavigationView() {
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
    }

    private void initViewPager() {
        FilePageAdapter filePageAdapter = new FilePageAdapter(getActivity().getSupportFragmentManager());

        fileMainViewPager.setAdapter(filePageAdapter);
        fileMainViewPager.setCurrentItem(PAGE_FILE);

        fileMainViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position != PAGE_FILE_SHARE) {
                    fileMainMenu.setVisibility(View.VISIBLE);
                } else {
                    fileMainMenu.setVisibility(View.GONE);
                }

            }
        });
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.switchDrawerOpenState();
            }
        });
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
                    fileDownloadFragment = FileDownloadFragment.newInstance(FileMainFragment.this);
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
