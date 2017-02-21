package com.winsun.fruitmix.refactor.data.server;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.util.FNAS;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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
    public OperationResult saveUser(User user) {
        return null;
    }

    @Override
    public OperationResult saveMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteMediaShare(MediaShare mediaShare) {
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
    public MediasLoadOperationResult loadMedias() {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadFiles() {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadFileShares() {
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
}
