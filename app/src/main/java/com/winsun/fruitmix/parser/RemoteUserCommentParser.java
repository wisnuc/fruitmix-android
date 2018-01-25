package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.FileUtil;

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

            JSONArray list = jsonObject.optJSONArray("list");

            List<Media> medias = new ArrayList<>();
            List<AbstractFile> files = new ArrayList<>();

            for (int j = 0; j < list.length(); j++) {

                JSONObject listItem = list.optJSONObject(j);

                String name = listItem.optString("filename");
                String hash = listItem.optString("sha256");

                if (listItem.has("metadata")) {

                    Media media = new Media();
                    media.setName(name);
                    media.setUuid(hash);

                    JSONObject metadata = listItem.optJSONObject("metadata");

                    media.setWidth(metadata.optString("w"));
                    media.setHeight(metadata.optString("h"));

                    media.setLocal(false);

                    medias.add(media);

                } else {

                    RemoteFile abstractFile = new RemoteFile();

                    abstractFile.setFileHash(hash);
                    abstractFile.setName(name);

                    abstractFile.setFileTypeResID(FileUtil.getFileTypeResID(name));

                    files.add(abstractFile);

                }

            }

            if (files.size() == 0) {

                MediaComment mediaComment = new MediaComment(uuid, null, time, "",medias);

                mediaComment.setIndex(index);
                mediaComment.setText(comment);

                userComments.add(mediaComment);

            } else {

                files.addAll(medias);

                FileComment fileComment = new FileComment(uuid, null, time, "",files);

                fileComment.setIndex(index);
                fileComment.setText(comment);

                userComments.add(fileComment);

            }

        }

        return userComments;
    }

}
