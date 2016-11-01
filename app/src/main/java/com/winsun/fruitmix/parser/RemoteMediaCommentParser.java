package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.Comment;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/9/2.
 */
public class RemoteMediaCommentParser implements RemoteDataParser<Comment> {

    @Override
    public List<Comment> parse(String json) {

        JSONArray jsonArray;
        List<Comment> comments = new ArrayList<>();
        Comment comment;

        try {
            jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                comment = new Comment();
                comment.setCreator(jsonArray.getJSONObject(i).getString("creator"));
                comment.setTime(jsonArray.getJSONObject(i).getString("datatime"));
                comment.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(jsonArray.getJSONObject(i).getString("datatime")))));
                comment.setShareId(jsonArray.getJSONObject(i).getString("shareid"));
                comment.setText(jsonArray.getJSONObject(i).getString("text"));
                comments.add(comment);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return comments;
    }
}
