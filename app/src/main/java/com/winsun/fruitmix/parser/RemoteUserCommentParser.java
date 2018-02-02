package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/22.
 */

public class RemoteUserCommentParser extends BaseRemoteDataParser implements RemoteDatasParser<UserComment> {

    @Override
    public List<UserComment> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONArray jsonArray = new JSONArray(root);

        List<UserComment> userComments = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String uuid = jsonObject.optString("uuid");
            long time = jsonObject.optLong("ctime");

            long index = jsonObject.optLong("index");

            String comment = jsonObject.optString("comment");

            JSONObject tweeter = jsonObject.optJSONObject("tweeter");

            User creator = new User();
            creator.setAssociatedWeChatGUID(tweeter.optString("id"));

            JSONArray list = jsonObject.optJSONArray("list");

            List<Media> medias = new ArrayList<>();
            List<AbstractFile> files = new ArrayList<>();

            for (int j = 0; j < list.length(); j++) {

                JSONObject listItem = list.optJSONObject(j);

                String name = listItem.optString("filename");
                String hash = listItem.optString("sha256");
                long size = listItem.optLong("size");

                if (listItem.has("metadata")) {

                    Media media = new Media();
                    media.setName(name);
                    media.setSize(size);

                    media.setUuid(hash);

                    JSONObject metadata = listItem.optJSONObject("metadata");

                    media.setWidth(metadata.optString("w"));
                    media.setHeight(metadata.optString("h"));

                    media.setLocal(false);

                    String type = metadata.optString("m");
                    media.setType(type);

                    String dateTime = metadata.optString("date");
                    if (dateTime.isEmpty()) {
                        dateTime = metadata.optString("datetime");
                    }

                    if (dateTime.equals("") || dateTime.length() < 10) {
                        media.setFormattedTime(Util.DEFAULT_DATE);
                    } else {

                        dateTime = dateTime.substring(0, 10).replace(":", "-") + dateTime.substring(10);

                        media.setFormattedTime(dateTime);

                    }

                    medias.add(media);

                } else {

                    RemoteFile abstractFile = new RemoteFile();

                    abstractFile.setFileHash(hash);
                    abstractFile.setName(name);

                    abstractFile.setFileTypeResID(FileUtil.getFileTypeResID(name));
                    abstractFile.setSize(size);

                    files.add(abstractFile);

                }

            }

            if (files.size() == 0) {

                MediaComment mediaComment = new MediaComment(uuid, creator, time, "", medias);

                mediaComment.setIndex(index);
                mediaComment.setText(comment);

                userComments.add(mediaComment);

            } else {

                for (Media media : medias) {

                    RemoteFile file = new RemoteFile();

                    String name = media.getName();

                    file.setName(name);
                    file.setFileHash(media.getUuid());
                    file.setFileTypeResID(FileUtil.getFileTypeResID(name));
                    file.setSize(media.getSize());

                    files.add(file);

                }

                FileComment fileComment = new FileComment(uuid, creator, time, "", files);

                fileComment.setIndex(index);
                fileComment.setText(comment);

                userComments.add(fileComment);

            }

        }

        return userComments;
    }

}
