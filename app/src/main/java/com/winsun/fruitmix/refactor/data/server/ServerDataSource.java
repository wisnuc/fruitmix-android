package com.winsun.fruitmix.refactor.data.server;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.util.FNAS;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class ServerDataSource implements DataSource{

    public List<EquipmentAlias> loadEquipmentAlias(String url) {

        List<EquipmentAlias> equipmentAliases = new ArrayList<>();

        try {

            String str = FNAS.RemoteCallWithUrl(url).getResponseData();

            JSONArray json = new JSONArray(str);

            int length = json.length();

            for (int i = 0; i < length; i++) {
                JSONObject itemRaw = json.getJSONObject(i);

                String ip = itemRaw.getString("ipv4");
                EquipmentAlias equipmentAlias = new EquipmentAlias();
                equipmentAlias.setIpv4(ip);
                equipmentAliases.add(equipmentAlias);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return equipmentAliases;
    }

    public List<User> loadUserByLoginApi(String url) {

        List<User> users = new ArrayList<>();

        try {

            String str = FNAS.RemoteCallWithUrl(url).getResponseData();

            JSONArray json = new JSONArray(str);

            int length = json.length();

            for (int i = 0; i < length; i++) {
                JSONObject itemRaw = json.getJSONObject(i);
                User user = new User();
                user.setUserName(itemRaw.getString("username"));
                user.setUuid(itemRaw.getString("uuid"));
                user.setAvatar(itemRaw.getString("avatar"));
                users.add(user);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return users;
    }

    @Override
    public void deleteToken() {

    }

    @Override
    public OperateUserResult insertUser(String userName, String userPassword) {
        return null;
    }

    @Override
    public OperationResult insertUsers(List<User> users) {
        return null;
    }

    @Override
    public OperateMediaShareResult insertRemoteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {
        return null;
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteRemoteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {
        return null;
    }

    @Override
    public UsersLoadOperationResult loadUsers() {
        return null;
    }

    @Override
    public User loadUser(String userUUID) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {
        return null;
    }

    @Override
    public Collection<String> loadLocalMediaUUIDs() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs) {
        return null;
    }

    @Override
    public Media loadMedia(String mediaKey) {
        return null;
    }

    @Override
    public void updateLocalMediasUploadedFalse() {

    }

    @Override
    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadAllMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFiles() {
        return null;
    }

    @Override
    public OperationResult deleteDownloadedFile(List<String> fileUUIDs) {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {
        return null;
    }

    @Override
    public User loadCurrentLoginUser() {
        return null;
    }

    @Override
    public LoadTokenParam getLoadTokenParam() {
        return null;
    }

    @Override
    public boolean getShowAlbumTipsValue() {
        return false;
    }

    @Override
    public void saveShowAlbumTipsValue(boolean value) {

    }

    @Override
    public boolean getShowPhotoReturnTipsValue() {
        return false;
    }

    @Override
    public void saveShowPhotoReturnTipsValue(boolean value) {

    }
}
