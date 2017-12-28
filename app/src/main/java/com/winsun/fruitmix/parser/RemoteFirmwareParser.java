package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.firmware.model.CheckUpdateState;
import com.winsun.fruitmix.firmware.model.Firmware;
import com.winsun.fruitmix.firmware.model.FirmwareState;
import com.winsun.fruitmix.firmware.model.NewFirmwareState;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class RemoteFirmwareParser extends BaseRemoteDataParser implements RemoteDatasParser<Firmware> {

    @Override
    public List<Firmware> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        JSONObject appifi = jsonObject.optJSONObject("appifi");

        FirmwareState firmwareState;

        String currentVersionName = "";

        if (appifi == null) {

            firmwareState = FirmwareState.NULL;

        } else {

            String state = appifi.optString("state");

            switch (state) {
                case "Starting":
                    firmwareState = FirmwareState.STARTING;
                    break;
                case "Started":
                    firmwareState = FirmwareState.STARTED;
                    break;
                case "Stopping":
                    firmwareState = FirmwareState.STOPPING;
                    break;
                case "Stopped":
                    firmwareState = FirmwareState.STOPPED;
                    break;
                default:
                    firmwareState = FirmwareState.NULL;
                    break;
            }

            currentVersionName = appifi.optString("tagName");

        }

        JSONArray releases = jsonObject.optJSONArray("releases");

        JSONObject release = releases.getJSONObject(0);

        String state = release.optString("state");

        NewFirmwareState newFirmwareState;

        switch (state) {
            case "Idle":
                newFirmwareState = NewFirmwareState.IDLE;
                break;
            case "Failed":
                newFirmwareState = NewFirmwareState.FAILED;
                break;
            case "Ready":
                newFirmwareState = NewFirmwareState.READY;
                break;
            case "Downloading":
                newFirmwareState = NewFirmwareState.DOWNLOADING;
                break;
            case "Repacking":
                newFirmwareState = NewFirmwareState.REPACKING;
                break;
            case "Verifying":
                newFirmwareState = NewFirmwareState.VERIFYING;
            default:
                newFirmwareState = NewFirmwareState.NULL;

        }

        JSONObject remote = release.optJSONObject("remote");

        if (remote == null)
            remote = release.optJSONObject("local");

        String newVersionName = remote.optString("tag_name");

        String releaseDate = remote.optString("published_at").substring(0, 10);

        JSONObject view = release.optJSONObject("view");

        long length = 0L;
        long downloaded = 0L;

        if (view != null) {

            length = view.optLong("length");

            if (length != 0) {

                downloaded = view.optLong("bytesWritten");

            }

        }

        JSONObject fetch = jsonObject.optJSONObject("fetch");

        CheckUpdateState checkUpdateState;

        String updateState = fetch.optString("state");

        if (updateState.equals("Working"))
            checkUpdateState = CheckUpdateState.WORKING;
        else if (updateState.equals("Pending"))
            checkUpdateState = CheckUpdateState.PENDING;
        else
            checkUpdateState = CheckUpdateState.NULL;

        Firmware firmware = new Firmware(currentVersionName, firmwareState, newVersionName, newFirmwareState,
                length, downloaded, releaseDate, checkUpdateState);

        return Collections.singletonList(firmware);
    }

}
