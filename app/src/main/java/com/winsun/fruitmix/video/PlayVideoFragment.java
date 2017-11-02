package com.winsun.fruitmix.video;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.winsun.fruitmix.databinding.PlayVideoFragmentBinding;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.util.Util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import static com.winsun.fruitmix.file.data.station.StationFileDataSourceImpl.DOWNLOAD_FILE_PARAMETER;

/**
 * Created by Administrator on 2017/11/2.
 */

public class PlayVideoFragment {

    private VideoView videoView;

    private HttpRequestFactory httpRequestFactory;

    private View view;

    public PlayVideoFragment(Context context) {

        initVideoView(context);

    }

    private void initVideoView(Context context) {
        PlayVideoFragmentBinding binding = PlayVideoFragmentBinding.inflate(LayoutInflater.from(context), null, false);

        view = binding.getRoot();

        videoView = binding.videoView;

        httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);

        final MediaController mediaController = new MediaController(context);

        videoView.setMediaController(mediaController);

        mediaController.setAnchorView(videoView);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });

    }

    public View getView() {
        return view;
    }

    public void startPlayVideo(Video video,Context context){

        startPlayVideo(getHttpRequest(video,context));

    }

    public void startPlayVideo(String driveRootUUID, RemoteFile remoteFile){

        startPlayVideo(getHttpRequest(driveRootUUID,remoteFile));
    }

    private void startPlayVideo(HttpRequest httpRequest) {
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

        videoView.start();
    }


    private HttpRequest getHttpRequest(String driveRootUUID, RemoteFile remoteFile) {


        String encodedFileName = null;
        try {
            encodedFileName = URLEncoder.encode(remoteFile.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return httpRequestFactory.createHttpGetFileRequest(DOWNLOAD_FILE_PARAMETER + "/"
                + driveRootUUID + "/dirs/" + remoteFile.getParentFolderUUID()
                + "/entries/" + remoteFile.getUuid() + "?name=" + encodedFileName);
    }

    private HttpRequest getHttpRequest(Video video, Context context) {

        return video.getImageOriginalUrl(context);

    }

    public void onDestroy(){



    }


}
