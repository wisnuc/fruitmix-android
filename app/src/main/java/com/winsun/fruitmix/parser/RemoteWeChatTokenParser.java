package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.token.WechatTokenUserWrapper;
import com.winsun.fruitmix.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/10.
 */

public class RemoteWeChatTokenParser extends BaseRemoteDataParser implements RemoteDatasParser<WechatTokenUserWrapper> {

    public List<WechatTokenUserWrapper> parse(String json) throws JSONException {

        String rootStr = checkHasWrapper(json);

        WechatTokenUserWrapper wechatTokenUserWrapper = new WechatTokenUserWrapper();

        JSONObject root = new JSONObject(rootStr);

        String token = root.optString("token");

        wechatTokenUserWrapper.setToken(token);

        JSONObject user = root.getJSONObject("user");

        String nickName = user.optString("nickName");
        String avatarUrl = user.optString("avatarUrl");
        String guid = user.optString("id");

        wechatTokenUserWrapper.setNickName(nickName);
        wechatTokenUserWrapper.setAvatarUrl(avatarUrl);
        wechatTokenUserWrapper.setGuid(guid);

        return Collections.singletonList(wechatTokenUserWrapper);
    }

}
