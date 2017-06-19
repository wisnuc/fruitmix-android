package com.winsun.fruitmix.executor;

import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/3/24.
 */

public class RetrieveOriginalPhotoTask implements Callable<Boolean> {

    public static final String TAG = RetrieveOriginalPhotoTask.class.getSimpleName();

    private List<Media> medias;
    private DBUtils dbUtils;

    public RetrieveOriginalPhotoTask(List<Media> medias, DBUtils dbUtils) {
        this.medias = medias;
        this.dbUtils = dbUtils;
    }

    @Override
    public Boolean call() throws Exception{

        Log.d(TAG, "call: begin retrieve original photo task");

        for (Media media : medias) {

            Log.d(TAG, "call: media uuid:" + media.getUuid());

            String downloadMediaUrl = FNAS.getDownloadOriginalMediaUrl(media);

            HttpRequest httpRequest = new HttpRequest(downloadMediaUrl, Util.HTTP_GET_METHOD);
            httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);

            boolean result = false;

            try {

                ResponseBody responseBody = new OkHttpUtil().downloadFile(httpRequest);

                result = FileUtil.downloadMediaToOriginalPhotoFolder(responseBody, media);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result)
                dbUtils.updateRemoteMedia(media);
        }

        Log.d(TAG, "call: finish retrieve original photo task");

        EventBus.getDefault().post(new OperationEvent(Util.SHARED_PHOTO_THUMB_RETRIEVED, null));

        return null;
    }
}
