package com.winsun.fruitmix.list.data;

import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * Created by Administrator on 2018/3/5.
 */

public interface MediaInTweetDataSource {

    void downloadMedia(Media media, BaseOperateCallback callback);

}
