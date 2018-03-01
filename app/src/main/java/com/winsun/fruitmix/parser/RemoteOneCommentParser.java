package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.TextComment;
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
 * Created by Administrator on 2018/2/7.
 */

public class RemoteOneCommentParser {

    public UserComment parse(JSONObject jsonObject) {

        if (jsonObject == null)
            return null;

        String uuid = jsonObject.optString("uuid");
        long time = jsonObject.optLong("ctime");

        long index = jsonObject.optLong("index");

        String comment = jsonObject.optString("comment");

        User creator = new User();

        JSONObject tweeter = jsonObject.optJSONObject("tweeter");

        String userGUID;

        if (tweeter != null) {
            userGUID = tweeter.optString("id");
        } else {
            userGUID = jsonObject.optString("tweeter");
        }

        creator.setAssociatedWeChatGUID(userGUID);

        String commentType = jsonObject.optString("type");

        switch (commentType) {
            case "boxmessage": {

                SystemMessageTextComment textComment = new SystemMessageTextComment(uuid, creator, time, "", "", comment);

                textComment.setIndex(index);

                return textComment;

            }
            case "list":

                JSONArray list = jsonObject.optJSONArray("list");

                if (list.length() == 0) {

                    TextComment textComment = new TextComment(uuid, creator, time, "", "", comment);

                    textComment.setIndex(index);

                    return textComment;

                }

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

                        abstractFile.setUuid(Util.createLocalUUid());

                        abstractFile.setFileHash(hash);
                        abstractFile.setName(name);

                        abstractFile.setSize(size);

                        files.add(abstractFile);

                    }

                }

                if (files.size() == 0) {

                    MediaComment mediaComment = new MediaComment(uuid, creator, time, "", "", medias);

                    mediaComment.setIndex(index);
                    mediaComment.setText(comment);

                    return mediaComment;

                } else {

                    for (Media media : medias) {

                        RemoteFile file = new RemoteFile();

                        String name = media.getName();

                        file.setName(name);
                        file.setFileHash(media.getUuid());

                        file.setSize(media.getSize());

                        files.add(file);

                    }

                    FileComment fileComment = new FileComment(uuid, creator, time, "", "", files);

                    fileComment.setIndex(index);
                    fileComment.setText(comment);

                    return fileComment;

                }

            default: {

                TextComment textComment = new TextComment(uuid, creator, time, "", "", comment);

                textComment.setIndex(index);

                return textComment;

            }
        }

    }

}
