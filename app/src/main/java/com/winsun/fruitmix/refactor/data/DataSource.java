package com.winsun.fruitmix.refactor.data;

import com.android.volley.toolbox.StringRequest;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.loadOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface DataSource {

    OperationResult saveUser(User user);

    OperationResult saveMediaShare(MediaShare mediaShare);

    OperationResult modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare);

    OperationResult modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare);

    OperationResult deleteMediaShare(MediaShare mediaShare);

    DeviceIDLoadOperationResult loadDeviceID();

    UsersLoadOperationResult loadUsers();

    MediasLoadOperationResult loadMedias();

    MediaSharesLoadOperationResult loadMediaShares();

    FilesLoadOperationResult loadFiles();

    FileSharesLoadOperationResult loadFileShares();

    TokenLoadOperationResult loadToken(LoadTokenParam param);

    User loadCurrentLoginUser();

}
