package com.winsun.fruitmix.media.local.media;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/9/6.
 */

public interface MediaInSystemDBLoadCallback {

    void onSucceed(List<String> currentAllMediaPathInSystemDB,List<String> currentAllVideoPathInSystemDB, List<Media> newMedia, List<Video> newVideos, OperationResult operationResult);

}
