package com.winsun.fruitmix.video;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityPlayVideoBinding;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.util.Util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import static com.winsun.fruitmix.file.data.station.StationFileDataSourceImpl.DOWNLOAD_FILE_PARAMETER;

public class PlayVideoActivity extends Activity {

    private static Video mVideo;

    private static RemoteFile remoteFile;

    private static String mDriveRootUUID;

    public static void startPlayVideoActivity(Activity activity, Video video) {

        mVideo = video;

        activity.startActivity(new Intent(activity, PlayVideoActivity.class));

    }

    public static void startPlayVideoActivity(Activity activity, String driveRootUUID, RemoteFile file) {

        remoteFile = file;
        mDriveRootUUID = driveRootUUID;

        activity.startActivity(new Intent(activity, PlayVideoActivity.class));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPlayVideoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_play_video);

        FrameLayout frameLayout = binding.frameLayout;

        PlayVideoFragment playVideoFragment = new PlayVideoFragment(this);

        frameLayout.addView(playVideoFragment.getView());

        playVideoFragment.getPlayVideoView().setVisibility(View.INVISIBLE);

        if (mVideo != null) {

            playVideoFragment.startPlayVideo(mVideo, this);

        } else if (mDriveRootUUID != null && remoteFile != null) {

            playVideoFragment.startPlayVideo(mDriveRootUUID, remoteFile);

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
        }

    }
}
