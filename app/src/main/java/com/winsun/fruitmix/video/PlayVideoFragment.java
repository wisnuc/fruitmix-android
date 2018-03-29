package com.winsun.fruitmix.video;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.base.data.InjectBaseDataOperator;
import com.winsun.fruitmix.base.data.SCloudTokenContainer;
import com.winsun.fruitmix.base.data.retry.RefreshTokenRetryStrategy;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.databinding.PlayVideoFragmentBinding;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromBoxDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.token.manager.InjectSCloudTokenManager;
import com.winsun.fruitmix.token.manager.TokenManager;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/2.
 */

public class PlayVideoFragment implements SCloudTokenContainer {

    public static final String TAG = PlayVideoFragment.class.getSimpleName();

    private static final String DOWNLOAD_FILE_PARAMETER = "/drives";

    private VideoView videoView;

    private HttpRequestFactory httpRequestFactory;

    private View view;

    private ImageView playVideoView;

    private ProgressBar progressBar;

    private boolean mIsPlaying = false;

    private Context mContext;

    private String mSCloudToken;

    public PlayVideoFragment(Context context) {

        mContext = context;

        initVideoView(context);

    }

    private void initVideoView(Context context) {
        PlayVideoFragmentBinding binding = PlayVideoFragmentBinding.inflate(LayoutInflater.from(context), null, false);

        view = binding.getRoot();

        playVideoView = binding.playVideo;

        videoView = binding.videoView;

        progressBar = binding.progressBar;

        httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);

        MediaController mediaController = new MediaController(context);

        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                onPlayCompleted();

            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Log.d(TAG, "onError: what: " + what + " extra: " + extra);

                progressBar.setVisibility(View.INVISIBLE);

                onPlayCompleted();

                Context viewContext = view.getContext();

                ToastUtil.showToast(viewContext, viewContext.getString(R.string.fail, viewContext.getString(R.string.play_video)));

                return true;

            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                progressBar.setVisibility(View.INVISIBLE);

                mIsPlaying = true;

            }
        });

    }

    private void onPlayCompleted() {
        playVideoView.setVisibility(View.VISIBLE);

        mIsPlaying = false;
    }

    public View getView() {
        return view;
    }

    public void startPlayVideo(Video video, Context context) {

        startPlayVideo(getHttpRequest(video, context));

    }

    public void startPlayVideo(String driveRootUUID, RemoteFile remoteFile) {

        startPlayVideo(getHttpRequest(driveRootUUID, remoteFile));
    }

    public void startPlayVideo(final FileDownloadParam fileDownloadParam) {


        try {

            if (fileDownloadParam instanceof FileFromBoxDownloadParam) {

//                HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(fileDownloadParam.getFileDownloadPath(),
//                        Util.KEY_JWT_HEAD + ((FileFromBoxDownloadParam) fileDownloadParam).getCloudToken());

                TokenManager tokenManager = InjectSCloudTokenManager.provideInstance(mContext);

                BaseDataOperator baseDataOperator = InjectBaseDataOperator.provideInstance(mContext,
                        tokenManager, this, new RefreshTokenRetryStrategy(tokenManager));

                tokenManager.resetToken();

                baseDataOperator.preConditionCheck(true, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        HttpRequest httpRequest = null;

                        FileFromBoxDownloadParam fileFromBoxDownloadParam = (FileFromBoxDownloadParam) fileDownloadParam;

                        try {
                            httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.getFileDownloadPath(),
                                    fileFromBoxDownloadParam.getBoxUUID(), fileFromBoxDownloadParam.getStationID());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        startPlayVideo(httpRequest);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        HttpRequest httpRequest = null;

                        FileFromBoxDownloadParam fileFromBoxDownloadParam = (FileFromBoxDownloadParam) fileDownloadParam;

                        try {
                            httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.getFileDownloadPath(),
                                    fileFromBoxDownloadParam.getBoxUUID(), fileFromBoxDownloadParam.getStationID());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        startPlayVideo(httpRequest);

                    }
                });


            } else if (fileDownloadParam instanceof FileFromStationFolderDownloadParam) {

                FileFromStationFolderDownloadParam fileFromStationFolderDownloadParam = (FileFromStationFolderDownloadParam) fileDownloadParam;

                startPlayVideo(getHttpRequest(fileFromStationFolderDownloadParam));

            }

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();

        }

    }

    private void startPlayVideo(HttpRequest httpRequest) {

        if (mIsPlaying) {

            return;

        }

        if (playVideoView.getVisibility() == View.VISIBLE)
            playVideoView.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);

        String url = httpRequest.getUrl();

        Uri uri = Uri.parse(url);

        Map<String, String> map = new HashMap<>();
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

    private HttpRequest getHttpRequest(FileFromStationFolderDownloadParam fileFromStationFolderDownloadParam) throws UnsupportedEncodingException {
        return httpRequestFactory.createHttpGetFileRequest(fileFromStationFolderDownloadParam.getFileDownloadPath());
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

        return video.getImageOriginalUrl(InjectHttp.provideHttpRequestFactory(context));

    }

    public void stopPlayVideo() {

        videoView.stopPlayback();
        videoView.suspend();

        onPlayCompleted();

    }


    public void onDestroy() {

        mContext = null;

    }


    @Override
    public void setSCloudToken(String sCloudToken) {

        mSCloudToken = sCloudToken;

    }
}
