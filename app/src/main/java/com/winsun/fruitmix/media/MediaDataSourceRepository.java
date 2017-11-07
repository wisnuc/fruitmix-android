package com.winsun.fruitmix.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;

import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface MediaDataSourceRepository {

    void getMedia(BaseLoadDataCallback<Media> callback);

    void downloadMedia(List<Media> medias, BaseOperateDataCallback<Boolean> callback);

    void updateMedia(Media media);

    void updateVideo(Video video);

    void registerCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback);

    void unregisterCalcDigestCallback(CalcMediaDigestStrategy.CalcMediaDigestCallback calcMediaDigestCallback);

    void getLocalMedia(BaseLoadDataCallback<Media> callback);

    void getStationMediaForceRefresh(BaseLoadDataCallback<Media> callback);

    boolean clearAllStationMediasInDB();

    void resetState();

}
