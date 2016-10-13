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

    public MediaShare getRemoteMediaShare(JSONObject itemRaw) throws JSONException {
        Log.d("winsun", "" + itemRaw);
        JSONArray jsonArr;

        itemRaw = itemRaw.getJSONObject("doc");

        MediaShare mediaShare = new MediaShare();

        mediaShare.setUuid(itemRaw.getString("uuid"));

        mediaShare.setCreatorUUID(itemRaw.getString("author"));

        JSONObject album = itemRaw.optJSONObject("album");

        if(album == null){
            mediaShare.setAlbum(false);
        }else {
            mediaShare.setAlbum(true);

            mediaShare.setTitle(album.getString("title"));
            mediaShare.setDesc(album.getString("text"));
        }

        mediaShare.setTime(itemRaw.getString("mtime"));

        mediaShare.setArchived(false);

        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(itemRaw.getString("mtime")))));

        jsonArr = itemRaw.getJSONArray("contents");
        if (jsonArr.length() > 0) {
            List<String> imageDigests = new ArrayList<>(jsonArr.length());
            for (int j = 0; j < jsonArr.length(); j++) {
                imageDigests.add(jsonArr.getJSONObject(j).getString("digest").toLowerCase());
            }

            mediaShare.setImageDigests(imageDigests);

            mediaShare.setCoverImageDigest(imageDigests.get(0));
        } else {
            mediaShare.setImageDigests(Collections.<String>emptyList());
            mediaShare.setCoverImageDigest("");
        }

        jsonArr = itemRaw.getJSONArray("viewers");
        if (jsonArr.length() > 0) {
            List<String> viewers = new ArrayList<>(jsonArr.length());
            for (int j = 0; j < jsonArr.length(); j++) {

                viewers.add(jsonArr.getString(j));
            }
            mediaShare.setViewers(viewers);
        } else {
            mediaShare.setViewers(Collections.<String>emptyList());
        }

        jsonArr = itemRaw.getJSONArray("maintainers");
        if (jsonArr.length() > 0) {
            List<String> maintainers = new ArrayList<>(jsonArr.length());
            for (int j = 0; j < jsonArr.length(); j++) {
                maintainers.add(jsonArr.getString(j));
            }
            mediaShare.setMaintainers(maintainers);
        } else {
            mediaShare.setMaintainers(Collections.<String>emptyList());
        }

        mediaShare.setLocal(false);

        return mediaShare;
    }
}
