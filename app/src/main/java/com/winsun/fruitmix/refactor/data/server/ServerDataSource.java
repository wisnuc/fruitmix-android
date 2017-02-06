package com.winsun.fruitmix.refactor.data.server;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.data.DataSource;

/**
 * Created by Administrator on 2017/2/6.
 */

public class ServerDataSource implements DataSource {
    @Override
    public void saveUser(User user, DataOperationCallback callback) {

    }

    @Override
    public void saveMedia(Media media, DataOperationCallback callback) {

    }

    @Override
    public void saveMediaShare(MediaShare mediaShare, DataOperationCallback callback) {

    }

    @Override
    public void modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback) {

    }

    @Override
    public void modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback) {

    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare, DataOperationCallback callback) {

    }

    @Override
    public void retrieveDeviceID(RetrieveDeviceIDOperationCallback callback) {

    }

    @Override
    public void retrieveUser(RetrieveUserOperationCallback callback) {

    }

    @Override
    public void retrieveLocalMedia(RetrieveMediaOperationCallback callback) {

    }

    @Override
    public void retrieveRemoteMedia(RetrieveMediaOperationCallback callback) {

    }

    @Override
    public void retrieveMediaShare(RetrieveMediaShareOperationCallback callback) {

    }

    @Override
    public void retrieveFile(RetrieveFileOperationCallback callback) {

    }

    @Override
    public void retrieveFileShare(RetrieveFileOperationCallback callback) {

    }

    @Override
    public void retrieveToken(RetrieveTokenOperationCallback callback) {

    }
}
