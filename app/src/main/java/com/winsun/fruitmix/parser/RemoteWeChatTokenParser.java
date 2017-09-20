package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/10.
 */

public class RemoteWeChatTokenParser extends BaseRemoteDataParser implements RemoteDatasParser<WeChatTokenUserWrapper> {

    public List<WeChatTokenUserWrapper> parse(String json) throws JSONException {

        String rootStr = checkHasWrapper(json);

        WeChatTokenUserWrapper weChatTokenUserWrapper = new WeChatTokenUserWrapper();

        JSONObject root = new JSONObject(rootStr);

        String token = root.optString("token");

        weChatTokenUserWrapper.setToken(token);

        JSONObject user = root.getJSONObject("user");

        String nickName = user.optString("nickName");
        String avatarUrl = user.optString("avatarUrl");
        String guid = user.optString("id");

        weChatTokenUserWrapper.setNickName(nickName);
        weChatTokenUserWrapper.setAvatarUrl(avatarUrl);
        weChatTokenUserWrapper.setGuid(guid);

        return Collections.singletonList(weChatTokenUserWrapper);
    }

}
