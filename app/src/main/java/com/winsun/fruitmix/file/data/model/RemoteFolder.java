package com.winsun.fruitmix.file.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.util.FNAS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFolder extends AbstractRemoteFile {

    public RemoteFolder() {

        setFileTypeResID(R.drawable.folder_icon);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public String getTimeDateText() {
        if (getTime().equals(""))
            return "";
        else {
            return super.getTimeDateText();
        }
    }

}
