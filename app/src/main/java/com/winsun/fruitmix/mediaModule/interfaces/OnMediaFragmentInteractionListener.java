package com.winsun.fruitmix.mediaModule.interfaces;

import com.winsun.fruitmix.mediaModule.model.MediaShare;

/**
 * Created by Administrator on 2016/11/2.
 */

public interface OnMediaFragmentInteractionListener {

    boolean isRemoteMediaShareLoaded();

    void modifyMediaShare(MediaShare mediashare);

    void deleteMediaShare(MediaShare mediashare);
}
