package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/14.
 */

public class BaseRemoteDataParser {

    protected String checkHasWrapper(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("data")) {
                return jsonObject.optString("data");
            } else
                return json;


        } catch (JSONException e) {

            return json;

        }

    }

}
