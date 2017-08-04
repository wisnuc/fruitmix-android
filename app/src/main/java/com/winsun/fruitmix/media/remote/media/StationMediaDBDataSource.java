package com.winsun.fruitmix.media.remote.media;

import android.content.Context;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/18.
 */

public class StationMediaDBDataSource {

    private DBUtils dbUtils;

    private static StationMediaDBDataSource instance;

    public static StationMediaDBDataSource getInstance(Context context) {

        if (instance == null)
            instance = new StationMediaDBDataSource(context);

        return instance;
    }

    private StationMediaDBDataSource(Context context) {

        dbUtils = DBUtils.getInstance(context);

    }

    public void getMedia(BaseLoadDataCallback<Media> callback) {

        List<Media> medias = dbUtils.getAllRemoteMedia();

        callback.onSucceed(medias, new OperationSuccess());

    }

    public void clearAllMedias() {

        dbUtils.deleteAllRemoteMedia();
    }


    public void insertMedias(Collection<Media> medias) {

        dbUtils.insertRemoteMedias(medias);

    }
}
