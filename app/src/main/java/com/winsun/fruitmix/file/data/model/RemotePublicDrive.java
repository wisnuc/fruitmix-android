package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/10/16.
 */

public class RemotePublicDrive extends RemoteFolder {

    public RemotePublicDrive() {
        super();

        setFileTypeResID(R.drawable.specified_shared_disk);

        setTime(0);
    }

}
