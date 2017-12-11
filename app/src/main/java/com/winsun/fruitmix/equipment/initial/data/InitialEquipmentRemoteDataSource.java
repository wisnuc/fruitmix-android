package com.winsun.fruitmix.equipment.initial.data;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskVolumeViewModel;
import com.winsun.fruitmix.equipment.search.data.EquipmentBootInfo;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithEquipmentBootInfo;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.parser.RemoteEquipmentBootInfoParser;
import com.winsun.fruitmix.parser.RemoteInsertUserParser;
import com.winsun.fruitmix.user.User;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/12/7.
 */

public class InitialEquipmentRemoteDataSource extends BaseRemoteDataSourceImpl implements InitialEquipmentDataSource {

    public static final String TAG = InitialEquipmentRemoteDataSource.class.getSimpleName();

    public static final String isPartitioned = "isPartitioned";

    private static final String STORAGE = "/storage";

    public static final String STORAGE_VOLUME = "/storage/volumes";

    public static final String BOOT = "/boot";

    public static final String USERS = "/users";

    public InitialEquipmentRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    @Override
    public void getStorageInfo(@NotNull String ip, @NotNull BaseLoadDataCallback<EquipmentDiskVolume> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip, STORAGE);

        wrapper.loadCall(httpRequest, callback, new RemoteEquipmentDiskVolume());
    }

    private class RemoteEquipmentDiskVolume implements RemoteDatasParser<EquipmentDiskVolume> {

        @Override
        public List<EquipmentDiskVolume> parse(String json) throws JSONException {

            JSONObject jsonObject = new JSONObject(json);

            JSONArray jsonArray = jsonObject.optJSONArray("blocks");

            List<EquipmentDiskVolume> equipmentDiskVolumes = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject block = jsonArray.optJSONObject(i);

                if (block.has("isDisk") && block.optBoolean("isDisk")) {

                    String type = "";

                    if (block.has("isATA"))
                        type = "ATA";
                    else if (block.has("isSCSI"))
                        type = "SCSI";
                    else if (block.has("USB"))
                        type = "USB";

                    String state = "";

                    if (block.optBoolean("isFileSystem")) {
                        state = block.optString("fileSystemType");
                    } else if (block.optBoolean("isPartitioned ")) {
                        state = isPartitioned;
                    }

                    EquipmentDiskVolume equipmentDiskVolume = new EquipmentDiskVolume(block.optString("model"), block.optString("name"),
                            block.optLong("size") * 512, type, state,
                            block.optString("unformattable"), block.optBoolean("removable"));

                    equipmentDiskVolumes.add(equipmentDiskVolume);
                }

            }

            return equipmentDiskVolumes;
        }

    }

    @Override
    public void installSystem(@NotNull final String ip, @NotNull final String userName, @NotNull final String userPassword,
                              @NotNull String mode, @NotNull List<? extends DiskVolumeViewModel> diskVolumeViewModels,
                              @NotNull final BaseOperateDataCallback<User> callback) {

        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        for (DiskVolumeViewModel diskVolumeViewModel : diskVolumeViewModels) {
            jsonArray.put(diskVolumeViewModel.getName());
        }

        try {
            jsonObject.putOpt("target", jsonArray);

            jsonObject.put("mode", mode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mkfs = jsonObject.toString();

        Log.d(TAG, "installSystem: mkfs: " + mkfs);

        HttpRequest httpRequest = httpRequestFactory.createPostRequestWithoutToken(ip, STORAGE_VOLUME, mkfs);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                handleMkfsSucceed(ip, data.get(0), userName, userPassword, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        }, new RemoteDatasParser<String>() {
            @Override
            public List<String> parse(String json) throws JSONException {
                return Collections.singletonList(new JSONObject(json).optString("uuid"));
            }
        });

    }

    private void handleMkfsSucceed(final String ip, final String fileSystemUUID, final String userName, final String userPwd,
                                   final BaseOperateDataCallback<User> callback) {

        HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip, STORAGE);

        wrapper.loadCall(httpRequest, new BaseLoadDataCallback<Void>() {
            @Override
            public void onSucceed(List<Void> data, OperationResult operationResult) {

                handleRefreshStorageSucceed(ip, fileSystemUUID, userName,
                        userPwd, callback);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteDatasParser<Void>() {
            @Override
            public List<Void> parse(String json) throws JSONException {
                return null;
            }
        });

    }

    private void handleRefreshStorageSucceed(final String ip, String fileSystemUUID, final String userName, final String userPwd,
                                             final BaseOperateDataCallback<User> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("current", fileSystemUUID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String install = jsonObject.toString();

        Log.d(TAG, "handleRefreshStorageSucceed: install: " + install);

        HttpRequest httpRequest = httpRequestFactory.createPatchRequestWithoutToken(ip, BOOT, install);

        wrapper.operateCall(httpRequest, new BaseOperateDataCallback<Void>() {
            @Override
            public void onSucceed(Void data, OperationResult result) {

                checkInstallSucceed(ip, userName, userPwd, callback);
            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        }, new RemoteDataParser<Void>() {
            @Override
            public Void parse(String json) throws JSONException {
                return null;
            }
        });

    }

    private void checkInstallSucceed(String ip, String userName, String userPwd,
                                     final BaseOperateDataCallback<User> callback){

        while (true){

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            HttpRequest httpRequest = httpRequestFactory.createGetRequestWithoutToken(ip,BOOT);

            OperationResult operationResult = wrapper.loadEquipmentBootInfo(httpRequest,new RemoteEquipmentBootInfoParser());

            if(operationResult.getOperationResultType() != OperationResultType.SUCCEED){

                callback.onFail(operationResult);
                break;
            }

            EquipmentBootInfo equipmentBootInfo = ((OperationSuccessWithEquipmentBootInfo)operationResult).getEquipmentBootInfos().get(0);

            if(equipmentBootInfo.getCurrent() != null && equipmentBootInfo.getCurrent().length() > 0){

                if(equipmentBootInfo.getState().equals("started")){

                    try {
                        // this may be due to worker not started yet
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handleInstallSucceed(ip,userName,userPwd,callback);

                    break;


                }else if(equipmentBootInfo.getState().equals("stopping")){

                    callback.onFail(new OperationFail("device initWizard: fruitmix stopping (unexpected), stop"));

                    break;
                }


            }else{

                callback.onFail(new OperationFail("device initWizard: fruitmix is null, legal"));
                break;

            }

        }


    }


    private void handleInstallSucceed(String ip, String userName, String userPwd,
                                      final BaseOperateDataCallback<User> callback) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("username", userName);
            jsonObject.put("password", userPwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String createFirstUser = jsonObject.toString();

        Log.d(TAG, "handleInstallSucceed: createFirstUser:" + createFirstUser);

        HttpRequest httpRequest = httpRequestFactory.createPostRequestWithoutToken(ip, USERS, createFirstUser);

        wrapper.operateCall(httpRequest, callback, new RemoteInsertUserParser());

    }


}
