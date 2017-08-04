package com.winsun.fruitmix.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.mediaModule.model.Media;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface MediaDataSourceRepository {

    void getMedia(final BaseLoadDataCallback<Media> callback);

}
