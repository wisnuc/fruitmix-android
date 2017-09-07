package com.winsun.fruitmix.upload.media;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.File;

/**
 * Created by Administrator on 2017/9/6.
 */

public class CheckMediaIsExistStrategy {

    private static CheckMediaIsExistStrategy instance;

    public static CheckMediaIsExistStrategy getInstance() {
        if(instance == null)
            instance = new CheckMediaIsExistStrategy();
        return instance;
    }

    public boolean checkMediaIsExist(Media media){

        File file = new File(media.getOriginalPhotoPath());

        return file.exists();

    }

}
