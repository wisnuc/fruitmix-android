package com.winsun.fruitmix.file;

import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileMainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.file_main_viewpager)
    UnscrollableViewPager fileMainViewPager;

    private FilePageAdapter filePageAdapter;

    public static final int PAGE_FILE_SHARE = 0;
    public static final int PAGE_FILE = 1;
    public static final int PAGE_FILE_DOWNLOAD = 2;

    private FileFragment fileFragment;
    private FileShareFragment fileShareFragment;
    private FileDownloadFragment fileDownloadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_main);

        ButterKnife.bind(this);

        filePageAdapter = new FilePageAdapter(getSupportFragmentManager());

        fileMainViewPager.setAdapter(filePageAdapter);
        fileMainViewPager.setCurrentItem(PAGE_FILE);

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

    @Override
    public void onBackPressed() {

        if(notRootFolder()){
            fileFragment.onBackPressed();
        }else if(fileMainViewPager.getCurrentItem() == PAGE_FILE_SHARE){
            fileShareFragment.onBackPressed();
        }else {
            super.onBackPressed();
        }
    }

    private boolean notRootFolder() {

        User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);
        String homeFolderUUID = user.getHome();

        return fileMainViewPager.getCurrentItem() == PAGE_FILE && !(fileFragment.getCurrentFolderUUID().equals(homeFolderUUID));
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

            switch (position) {
                case PAGE_FILE:
                    fileFragment = FileFragment.newInstance();
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
