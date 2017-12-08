package com.winsun.fruitmix.equipment.initial.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.parser.RemoteDatasParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/7.
 */

public class InitialEquipmentRemoteDataSource extends BaseRemoteDataSourceImpl implements InitialEquipmentDataSource {

    public static final String isPartitioned = "isPartitioned";

    private static final String STORAGE = "/storage";

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

}
