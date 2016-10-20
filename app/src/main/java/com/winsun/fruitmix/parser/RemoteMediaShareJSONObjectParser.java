package com.winsun.fruitmix.parser;

import android.util.Log;

import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.MediaShareContent;

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

        MediaShare mediaShare = new MediaShare();

        mediaShare.setShareDigest(itemRaw.getString("digest"));

        itemRaw = itemRaw.getJSONObject("doc");

        mediaShare.setUuid(itemRaw.getString("uuid"));

        mediaShare.setCreatorUUID(itemRaw.getString("author"));

        JSONObject album = itemRaw.optJSONObject("album");

        if (album == null) {
            mediaShare.setAlbum(false);
        } else {
            mediaShare.setAlbum(true);

            mediaShare.setTitle(album.getString("title"));
            mediaShare.setDesc(album.getString("text"));
        }

        mediaShare.setTime(itemRaw.getString("mtime"));

        mediaShare.setArchived(false);

        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(itemRaw.getString("mtime")))));

        jsonArr = itemRaw.getJSONArray("contents");
        if (jsonArr.length() > 0) {

            MediaShareContent mediaShareContent;
            for (int j = 0; j < jsonArr.length(); j++) {

                JSONObject jsonObject = jsonArr.getJSONObject(j);

                mediaShareContent = new MediaShareContent();
                mediaShareContent.setDigest(jsonObject.getString("digest").toLowerCase());

                String author = jsonObject.optString("author");
                if (author.equals("")) {
                    author = jsonObject.getString("creator");
                }

                mediaShareContent.setAuthor(author.toLowerCase());


                String time = jsonObject.optString("time");
                if (author.equals("")) {
                    time = jsonObject.getString("ctime");
                }

                mediaShareContent.setTime(time.toLowerCase());

                mediaShare.addMediaShareContent(mediaShareContent);
            }

            mediaShare.setCoverImageDigest(mediaShare.getFirstMediaDigestInMediaContentsList());
        } else {
            mediaShare.clearMediaShareContents();
            mediaShare.setCoverImageDigest("");
        }

        jsonArr = itemRaw.getJSONArray("viewers");
        if (jsonArr.length() > 0) {

            for (int j = 0; j < jsonArr.length(); j++) {

                mediaShare.addViewer(jsonArr.getString(j));
            }

        } else {
            mediaShare.clearViewers();
        }

        jsonArr = itemRaw.getJSONArray("maintainers");
        if (jsonArr.length() > 0) {

            for (int j = 0; j < jsonArr.length(); j++) {
                mediaShare.addMaintainer(jsonArr.getString(j));
            }

        } else {
            mediaShare.clearMaintainers();
        }

        mediaShare.setLocal(false);
        mediaShare.setSticky(itemRaw.getBoolean("sticky"));


        return mediaShare;
    }
}
