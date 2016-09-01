package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.util.FNAS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteShareParser implements RemoteDataParser<Share> {

    @Override
    public List<Share> parse(String json) {

        List<Share> shares = new ArrayList<>();

        JSONArray jsonArray, jsonArr;
        JSONObject itemRaw;

        Share share;

        try {
            jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                itemRaw = jsonArray.getJSONObject(i);
                Log.d("winsun", "" + itemRaw);

                share = new Share();

                share.setUuid(itemRaw.getString("uuid"));

                if (itemRaw.getJSONObject("latest").has("creator"))
                    share.setCreator(itemRaw.getJSONObject("latest").getString("creator"));
                else
                    share.setCreator(itemRaw.getJSONObject("latest").getJSONArray("maintainers").getString(0));

                if (itemRaw.getJSONObject("latest").getString("album").equals("true")) {
                    share.setAlbum(true);
                } else {
                    share.setAlbum(false);
                }

                share.setTime(itemRaw.getJSONObject("latest").getString("mtime"));

                if (itemRaw.getJSONObject("latest").getString("archived").equals("true")) {
                    share.setArchived(true);
                } else {
                    share.setArchived(false);
                }

                share.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(itemRaw.getJSONObject("latest").getString("mtime")))));
                if (itemRaw.getJSONObject("latest").getString("album").equals("true")) {
                    share.setTitle(itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("albumname"));
                    share.setDesc(itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("desc"));
                }

                jsonArr = itemRaw.getJSONObject("latest").getJSONArray("contents");
                if (jsonArr.length() > 0) {
                    List<String> imageDigests = new ArrayList<>(jsonArr.length());
                    for (int j = 0; j < jsonArr.length(); j++) {
                        imageDigests.add(jsonArr.getJSONObject(j).getString("digest").toLowerCase());
                    }

                    share.setImageDigests(imageDigests);

                    share.setCoverImageDigest(imageDigests.get(0));
                } else {
                    share.setImageDigests(Collections.<String>emptyList());
                }

                jsonArr = itemRaw.getJSONObject("lastest").getJSONArray("viewer");
                if (jsonArr.length() > 0) {
                    List<String> viewers = new ArrayList<>(jsonArr.length());
                    for (int j = 0; j < jsonArr.length(); j++) {
                        viewers.add(jsonArr.getString(j));
                    }
                    share.setViewer(viewers);
                } else {
                    share.setViewer(Collections.<String>emptyList());
                }

                jsonArr = itemRaw.getJSONObject("lastest").getJSONArray("maintainer");
                if (jsonArr.length() > 0) {
                    List<String> maintainers = new ArrayList<>(jsonArr.length());
                    for (int j = 0; j < jsonArr.length(); j++) {
                        maintainers.add(jsonArr.getString(j));
                    }
                    share.setMaintainer(maintainers);
                } else {
                    share.setMaintainer(Collections.<String>emptyList());
                }

                if (share.getViewer().size() <= 1 && share.getMaintainer().size() <= 1)
                    share.setPrivate(true);
                else share.setPrivate(false);

                share.setMaintained(share.getMaintainer().contains(FNAS.userUUID));

                share.setLocked(false);

                shares.add(share);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return shares;
    }
}
