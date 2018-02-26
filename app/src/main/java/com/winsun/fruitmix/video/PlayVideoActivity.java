package com.winsun.fruitmix.video;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityPlayVideoBinding;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.mediaModule.model.Video;


public class PlayVideoActivity extends Activity {

    private static Video mVideo;

    private static RemoteFile remoteFile;

    private static String mDriveRootUUID;

    private static FileDownloadParam mFileDownloadParam;

    public static void startPlayVideoActivity(Activity activity, Video video) {

        mVideo = video;

        activity.startActivity(new Intent(activity, PlayVideoActivity.class));

    }

    public static void startPlayVideoActivity(Activity activity, String driveRootUUID, RemoteFile file) {

        remoteFile = file;
        mDriveRootUUID = driveRootUUID;

        activity.startActivity(new Intent(activity, PlayVideoActivity.class));

    }

    public static void startPlayVideoActivity(Activity activity, FileDownloadParam fileDownloadParam) {

        mFileDownloadParam = fileDownloadParam;

        activity.startActivity(new Intent(activity, PlayVideoActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPlayVideoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_play_video);

        FrameLayout frameLayout = binding.frameLayout;

        PlayVideoFragment playVideoFragment = new PlayVideoFragment(this);

        frameLayout.addView(playVideoFragment.getView());

        if (mVideo != null) {

            playVideoFragment.startPlayVideo(mVideo, this);

        } else if (mDriveRootUUID != null && remoteFile != null) {

            playVideoFragment.startPlayVideo(mDriveRootUUID, remoteFile);

        } else if (mFileDownloadParam != null) {

            playVideoFragment.startPlayVideo(mFileDownloadParam);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mVideo != null)
            mVideo = null;
        else if (mDriveRootUUID != null || remoteFile != null) {
            mDriveRootUUID = null;
            remoteFile = null;
        }else if(mFileDownloadParam != null){
            mFileDownloadParam = null;
        }

    }
}
