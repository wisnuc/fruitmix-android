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

    private VideoView videoView;

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

        videoView = binding.videoView;

        HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(this);

        final MediaController mediaController = new MediaController(this);

        videoView.setMediaController(mediaController);

        mediaController.setAnchorView(videoView);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                videoView = null;
                finish();

            }
        });

        if (mVideo != null) {

            Uri uri = Uri.fromFile(new File(mVideo.getOriginalPhotoPath()));

            videoView.setVideoURI(uri);

        } else if (mDriveRootUUID != null && remoteFile != null) {

            String encodedFileName = null;
            try {
                encodedFileName = URLEncoder.encode(remoteFile.getName(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpRequest httpRequest = httpRequestFactory.createHttpGetFileRequest(DOWNLOAD_FILE_PARAMETER + "/"
                    + mDriveRootUUID + "/dirs/" + remoteFile.getParentFolderUUID()
                    + "/entries/" + remoteFile.getUuid() + "?name=" + encodedFileName);

            Uri uri = Uri.parse(httpRequest.getUrl());

            Map<String, String> map = new ArrayMap<>();
            map.put(httpRequest.getHeaderKey(), httpRequest.getHeaderValue());

            if (Util.checkRunningOnLollipopOrHigher()) {
                videoView.setVideoURI(uri, map);
            } else {

                videoView.setVideoURI(uri);

                try {
                    Field field = VideoView.class.getDeclaredField("mHeaders");
                    field.setAccessible(true);
                    field.set(videoView, map);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        videoView.start();


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
