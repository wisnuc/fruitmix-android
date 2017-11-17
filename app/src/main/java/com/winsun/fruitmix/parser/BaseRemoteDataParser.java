package com.winsun.fruitmix.parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Administrator on 2017/9/14.
 */

public class BaseRemoteDataParser {

    protected String checkHasWrapper(String json) throws JSONException {

        try {

            Object object = new JSONTokener(json).nextValue();

            if (object instanceof JSONObject) {

                JSONObject jsonObject = (JSONObject) object;
                if (jsonObject.has("data"))
                    return jsonObject.optString("data");
                else
                    return json;

            } else
                return json;

        } catch (JSONException e) {

            return json;

        } catch (OutOfMemoryError e) {

            return json;

        }

    }

}
