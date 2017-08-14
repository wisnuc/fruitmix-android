package com.winsun.fruitmix.file.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LocalFolder extends AbstractLocalFile {
    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public String getTimeDateText() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(getTime())));
    }
}
