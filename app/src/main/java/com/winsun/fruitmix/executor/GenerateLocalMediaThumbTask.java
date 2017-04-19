package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GenerateLocalMediaThumbTask implements Runnable {

    private DBUtils dbUtils;
    private Media media;
    private boolean stopGenerateThumb;

    public GenerateLocalMediaThumbTask(Media media, DBUtils dbUtils, boolean stopGenerateThumb) {
        this.media = media;
        this.dbUtils = dbUtils;
        this.stopGenerateThumb = stopGenerateThumb;
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public void run() {

        if (stopGenerateThumb) return;

        boolean result = FileUtil.writeBitmapToLocalPhotoThumbnailFolder(media);

        if (result && !stopGenerateThumb) {

            if (dbUtils != null)
                dbUtils.updateLocalMedia(media);
        }

    }
}
