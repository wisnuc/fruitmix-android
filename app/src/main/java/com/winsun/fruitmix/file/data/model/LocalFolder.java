package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LocalFolder extends AbstractLocalFile {

    public LocalFolder() {

        setFileTypeResID(R.drawable.folder_icon);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

}
