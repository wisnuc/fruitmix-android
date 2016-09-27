package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.MediaShare;
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
public class RemoteMediaShareParser implements RemoteDataParser<MediaShare> {

    @Override
    public List<MediaShare> parse(String json) {

        List<MediaShare> mediaShares = new ArrayList<>();

        JSONArray jsonArray;
        JSONObject itemRaw;

        try {
            jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                itemRaw = jsonArray.getJSONObject(i);

                Log.d("winsun", "" + itemRaw);
                JSONArray jsonArr;

                MediaShare mediaShare = new MediaShare();

                mediaShare.setUuid(itemRaw.getString("uuid"));

                if (itemRaw.getJSONObject("latest").has("creator"))
                    mediaShare.setCreatorUUID(itemRaw.getJSONObject("latest").getString("creator"));
                else
                    mediaShare.setCreatorUUID(itemRaw.getJSONObject("latest").getJSONArray("maintainers").getString(0));

                if (itemRaw.getJSONObject("latest").getString("album").equals("true")) {
                    mediaShare.setAlbum(true);
                } else {
                    mediaShare.setAlbum(false);
                }

                mediaShare.setTime(itemRaw.getJSONObject("latest").getString("mtime"));

                if (itemRaw.getJSONObject("latest").getString("archived").equals("true")) {
                    mediaShare.setArchived(true);
                } else {
                    mediaShare.setArchived(false);
                }

                mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(itemRaw.getJSONObject("latest").getString("mtime")))));
                if (itemRaw.getJSONObject("latest").getString("album").equals("true")) {
                    mediaShare.setTitle(itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("albumname"));
                    mediaShare.setDesc(itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("desc"));
                }

                jsonArr = itemRaw.getJSONObject("latest").getJSONArray("contents");
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

                jsonArr = itemRaw.getJSONObject("latest").getJSONArray("viewers");
                if (jsonArr.length() > 0) {
                    List<String> viewers = new ArrayList<>(jsonArr.length());
                    for (int j = 0; j < jsonArr.length(); j++) {
                        viewers.add(jsonArr.getString(j));
                    }
                    mediaShare.setViewer(viewers);
                } else {
                    mediaShare.setViewer(Collections.<String>emptyList());
                }

                jsonArr = itemRaw.getJSONObject("latest").getJSONArray("maintainers");
                if (jsonArr.length() > 0) {
                    List<String> maintainers = new ArrayList<>(jsonArr.length());
                    for (int j = 0; j < jsonArr.length(); j++) {
                        maintainers.add(jsonArr.getString(j));
                    }
                    mediaShare.setMaintainer(maintainers);
                } else {
                    mediaShare.setMaintainer(Collections.<String>emptyList());
                }

                mediaShare.setLocked(false);

                mediaShares.add(mediaShare);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaShares;
    }
}
