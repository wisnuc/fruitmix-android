package com.winsun.fruitmix.refactor.data;

import com.android.volley.toolbox.StringRequest;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface DataSource {

    boolean saveUser(User user);

    boolean saveMediaShare(MediaShare mediaShare);

    boolean modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare);

    boolean modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare);

    boolean deleteMediaShare(MediaShare mediaShare);

    String loadDeviceID();

    List<User> loadUsers();

    List<Media> loadMedias();

    List<MediaShare> loadMediaShares();

    List<AbstractRemoteFile> loadFile();

    List<AbstractRemoteFile> loadFileShare();

    String loadToken();


}
