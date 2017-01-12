package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/9/27.
 */

public class RemoteMediaShareJSONObjectParser {

    private SimpleDateFormat mSimpleDateFormat;
    private Date mDate;


    public RemoteMediaShareJSONObjectParser(){

        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        mDate = new Date();
    }


    public MediaShare getRemoteMediaShare(JSONObject itemRaw) throws JSONException {

        JSONArray jsonArr;

        MediaShare mediaShare = new MediaShare();

        mediaShare.setShareDigest(itemRaw.optString("digest"));

        itemRaw = itemRaw.getJSONObject("doc");

        mediaShare.setUuid(itemRaw.optString("uuid"));

        mediaShare.setCreatorUUID(itemRaw.optString("author"));

        JSONObject album = itemRaw.optJSONObject("album");

        if (album == null) {
            mediaShare.setAlbum(false);

            mediaShare.setTitle("");
            mediaShare.setDesc("");

        } else {
            mediaShare.setAlbum(true);

            mediaShare.setTitle(album.optString("title"));
            mediaShare.setDesc(album.optString("text"));
        }

        mediaShare.setTime(itemRaw.optString("mtime"));

        mediaShare.setArchived(false);

        mDate.setTime(Long.parseLong(itemRaw.optString("mtime")));
        mediaShare.setDate(mSimpleDateFormat.format(mDate));

        jsonArr = itemRaw.getJSONArray("contents");
        if (jsonArr.length() > 0) {

            MediaShareContent mediaShareContent;
            for (int j = 0; j < jsonArr.length(); j++) {

                JSONObject jsonObject = jsonArr.getJSONObject(j);

                mediaShareContent = new MediaShareContent();
                mediaShareContent.setKey(jsonObject.optString("digest").toLowerCase());

                String author = jsonObject.optString("author");
                if (author.equals("")) {
                    author = jsonObject.optString("creator");
                }

                mediaShareContent.setAuthor(author.toLowerCase());

                String time = jsonObject.optString("time");
                if (author.equals("")) {
                    time = jsonObject.optString("ctime");
                }

                mediaShareContent.setTime(time.toLowerCase());

                mediaShare.addMediaShareContent(mediaShareContent);
            }

            mediaShare.setCoverImageKey(mediaShare.getFirstMediaDigestInMediaContentsList());
        } else {
            mediaShare.clearMediaShareContents();
            mediaShare.setCoverImageKey("");
        }

        jsonArr = itemRaw.getJSONArray("viewers");
        if (jsonArr.length() > 0) {

            for (int j = 0; j < jsonArr.length(); j++) {

                mediaShare.addViewer(jsonArr.optString(j));
            }

        } else {
            mediaShare.clearViewers();
        }

        jsonArr = itemRaw.getJSONArray("maintainers");
        if (jsonArr.length() > 0) {

            for (int j = 0; j < jsonArr.length(); j++) {
                mediaShare.addMaintainer(jsonArr.optString(j));
            }

        } else {
            mediaShare.clearMaintainers();
        }

        mediaShare.setLocal(false);
        mediaShare.setSticky(itemRaw.getBoolean("sticky"));


        return mediaShare;
    }
}
