package com.winsun.fruitmix.file;

import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.UnscrollableViewPager;
import com.winsun.fruitmix.file.fragment.FileDownloadFragment;
import com.winsun.fruitmix.file.fragment.FileFragment;
import com.winsun.fruitmix.file.fragment.FileShareFragment;
import com.winsun.fruitmix.file.interfaces.OnFragmentInteractionListener;
import com.winsun.fruitmix.file.model.AbstractRemoteFile;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileMainActivity extends AppCompatActivity implements OnFragmentInteractionListener{

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.file_main_viewpager)
    UnscrollableViewPager fileMainViewPager;

    public static final int PAGE_FILE_SHARE = 0;
    public static final int PAGE_FILE = 1;
    public static final int PAGE_FILE_DOWNLOAD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_main);

        ButterKnife.bind(this);

        fileMainViewPager.setAdapter(new FilePageAdapter(getSupportFragmentManager()));
        fileMainViewPager.setCurrentItem(PAGE_FILE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.file_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.select_file:
                break;
        }

        return true;
    }


    private class FilePageAdapter extends FragmentPagerAdapter {

        FilePageAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {

            Fragment fragment;

            switch (position) {
                case PAGE_FILE:
                    fragment = FileFragment.newInstance();
                    break;
                case PAGE_FILE_SHARE:
                    fragment = FileShareFragment.newInstance();
                    break;
                case PAGE_FILE_DOWNLOAD:
                    fragment = FileDownloadFragment.newInstance();
                    break;
                default:
                    fragment = null;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
