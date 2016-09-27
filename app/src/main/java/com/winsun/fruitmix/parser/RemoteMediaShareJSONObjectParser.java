package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.MediaShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/9/27.
 */

public class RemoteMediaShareJSONObjectParser {

    public String getRemoteMediaShareUUID(JSONObject itemRaw) throws JSONException {
        return itemRaw.getString("uuid");
    }
}
