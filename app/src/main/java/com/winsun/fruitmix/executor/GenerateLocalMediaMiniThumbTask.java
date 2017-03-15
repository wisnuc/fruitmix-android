package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GenerateLocalMediaMiniThumbTask implements Runnable {

    private DBUtils dbUtils;
    private Media media;

    public GenerateLocalMediaMiniThumbTask(Media media, DBUtils dbUtils) {
        this.media = media;
        this.dbUtils = dbUtils;
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public void run() {

        boolean result = FileUtil.writeBitmapToLocalPhotoThumbnailFolder(media);

        if (result) {

            if (dbUtils != null && dbUtils.isOpen())
                dbUtils.updateLocalMedia(media);
        }

    }
}
