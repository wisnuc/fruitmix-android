package com.winsun.fruitmix.media.remote.media;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.StationsDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaDBDataSource {

    public static final String TAG = StationMediaDBDataSource.class.getSimpleName();

    private DBUtils dbUtils;

    private static StationMediaDBDataSource instance;

    public static StationMediaDBDataSource getInstance(DBUtils dbUtils) {

        if (instance == null)
            instance = new StationMediaDBDataSource(dbUtils);

        return instance;
    }

    private StationMediaDBDataSource(DBUtils dbUtils) {

        this.dbUtils = dbUtils;

    }

    public void getMedia(BaseLoadDataCallback<Media> callback) {

        List<Media> medias = dbUtils.getAllRemoteMedia();

        List<Video> videos = dbUtils.getAllRemoteVideos();

        medias.addAll(videos);

        callback.onSucceed(medias, new OperationSuccess());

    }

    boolean clearAllMedias() {
        boolean deleteAllRemoteMediaResult = dbUtils.deleteAllRemoteMedia() > 0;

        boolean deleteAllRemoteVideoResult = dbUtils.deleteAllRemoteVideo() > 0;

        Log.d(TAG, "clearAllMedias: deleteAllRemoteMediaResult: " + deleteAllRemoteMediaResult + " deleteAllRemoteVideoResult: " + deleteAllRemoteVideoResult);

        return true;

    }


    void insertMedias(Collection<Media> medias) {

        Collection<Media> remoteMedias = new ArrayList<>();
        Collection<Video> remoteVideos = new ArrayList<>();

        for (Media media : medias) {
            if (media instanceof Video)
                remoteVideos.add((Video) media);
            else
                remoteMedias.add(media);
        }

        dbUtils.insertRemoteMedias(remoteMedias);
        dbUtils.insertRemoteVideos(remoteVideos);

    }

    void updateMedia(Media media) {

        if (media instanceof Video)
            dbUtils.updateRemoteVideo((Video) media);
        else
            dbUtils.updateRemoteMedia(media);

    }


}
