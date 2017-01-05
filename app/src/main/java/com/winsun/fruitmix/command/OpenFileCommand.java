package com.winsun.fruitmix.command;

import android.content.Context;

import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/1/5.
 */

public class OpenFileCommand extends AbstractCommand {

    private Context mContext;
    private String mFileName;

    public OpenFileCommand(Context context, String fileName) {
        mContext = context;
        mFileName = fileName;
    }

    @Override
    public void execute() {
        FileUtil.openAbstractRemoteFile(mContext, mFileName);
    }

    @Override
    public void unExecute() {

    }
}
