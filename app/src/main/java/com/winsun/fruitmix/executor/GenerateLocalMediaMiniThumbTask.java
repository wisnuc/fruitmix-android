package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FileUtil;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GenerateLocalMediaMiniThumbTask implements Callable<Boolean> {

    private DBUtils dbUtils;
    private Media media;
    private boolean stopGenerateMiniThumb;

    public GenerateLocalMediaMiniThumbTask(Media media, DBUtils dbUtils, boolean stopGenerateMiniThumb) {
        this.media = media;
        this.dbUtils = dbUtils;
        this.stopGenerateMiniThumb = stopGenerateMiniThumb;
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public Boolean call() throws Exception{

        if (stopGenerateMiniThumb) return false;

        boolean result = FileUtil.writeBitmapToLocalPhotoMiniThumbnailFolder(media);

        if (result && !stopGenerateMiniThumb) {

            if (dbUtils != null)
                dbUtils.updateLocalMedia(media);
        }

        return true;
    }
}
