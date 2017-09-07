package com.winsun.fruitmix.media.local.media;

import android.content.Context;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/18.
 */

public class LocalMediaAppDBDataSource {

    private DBUtils dbUtils;

    private static LocalMediaAppDBDataSource instance;

    public static LocalMediaAppDBDataSource getInstance(Context context) {

        if (instance == null)
            instance = new LocalMediaAppDBDataSource(context);

        return instance;
    }

    private LocalMediaAppDBDataSource(Context context) {
        dbUtils = DBUtils.getInstance(context);
    }

    public void getMedia(BaseLoadDataCallback<Media> callback) {

        callback.onSucceed(dbUtils.getAllLocalMedia(), new OperationSuccess());

    }

    public void insertMedias(Collection<Media> medias) {

        if (medias.isEmpty())
            return;

        dbUtils.insertLocalMedias(medias);

    }

    public boolean updateMedia(Media media) {
        return dbUtils.updateLocalMedia(media) > 0;
    }


    public long deleteMediaByPath(Collection<String> mediaPaths){
        return dbUtils.deleteLocalMedias(mediaPaths);
    }


}
