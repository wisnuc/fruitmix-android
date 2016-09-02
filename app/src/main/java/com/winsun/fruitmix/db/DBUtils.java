package com.winsun.fruitmix.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.OfflineTask;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/7/8.
 */
public enum DBUtils {

    SINGLE_INSTANCE;

    private DBHelper dbHelper;
    private SQLiteDatabase database;

    private AtomicInteger referenceCount;

    DBUtils() {
        dbHelper = new DBHelper(Util.APPLICATION_CONTEXT);
        referenceCount = new AtomicInteger();
    }

    private synchronized void openWritableDB() {

        referenceCount.incrementAndGet();

        database = dbHelper.getWritableDatabase();
    }

    private synchronized void openReadableDB() {

        referenceCount.incrementAndGet();

        database = dbHelper.getReadableDatabase();
    }

    private synchronized void close() {

        if (referenceCount.decrementAndGet() == 0) {
            database.close();
        }
    }

    private boolean isOpen() {
        return database.isOpen();
    }

    public long insertTask(OfflineTask task) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TASK_KEY_HTTP_TYPE, task.getHttpType().toString());
        contentValues.put(DBHelper.TASK_KEY_OPERATION_TYPE, task.getOperationType().toString());
        contentValues.put(DBHelper.TASK_KEY_REQUEST, task.getRequest());
        contentValues.put(DBHelper.TASK_KEY_DATA, task.getData());
        contentValues.put(DBHelper.TASK_KEY_OPERATION_COUNT, task.getOperationCount());

        long returnValue = database.insert(DBHelper.TASK_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long insertRemoteComment(Comment comment, String imageUUid) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COMMENT_KEY_CREATOR, comment.getCreator());
        contentValues.put(DBHelper.COMMENT_KEY_TIME, comment.getTime());
        contentValues.put(DBHelper.COMMENT_KEY_FORMAT_TIME, comment.getFormatTime());
        contentValues.put(DBHelper.COMMENT_KEY_SHARE_ID, comment.getShareId());
        contentValues.put(DBHelper.COMMENT_KEY_TEXT, comment.getText());
        contentValues.put(DBHelper.COMMENT_IMAGE_UUID, imageUUid);

        long returnValue = database.insert(DBHelper.REMOTE_COMMENT_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long insertLocalComment(Comment comment, String imageUUid) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COMMENT_KEY_CREATOR, comment.getCreator());
        contentValues.put(DBHelper.COMMENT_KEY_TIME, comment.getTime());
        contentValues.put(DBHelper.COMMENT_KEY_FORMAT_TIME, comment.getFormatTime());
        contentValues.put(DBHelper.COMMENT_KEY_SHARE_ID, comment.getShareId());
        contentValues.put(DBHelper.COMMENT_KEY_TEXT, comment.getText());
        contentValues.put(DBHelper.COMMENT_IMAGE_UUID, imageUUid);

        long returnValue = database.insert(DBHelper.LOCAL_COMMENT_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long insertLocalShare(MediaShare mediaShare) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_UUID, mediaShare.getUuid());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_CREATOR, mediaShare.getCreator());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_TIME, mediaShare.getTime());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_TITLE, mediaShare.getTitle());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_DESC, mediaShare.getDesc());

        StringBuilder builder = new StringBuilder();
        for (String image : mediaShare.getImageDigests()) {
            builder.append(image);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_DIGEST, builder.toString());

        builder.setLength(0);
        for (String viewer : mediaShare.getViewer()) {
            builder.append(viewer);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_VIEWER, builder.toString());

        builder.setLength(0);
        for (String maintainer : mediaShare.getMaintainer()) {
            builder.append(maintainer);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_MAINTAINER, builder.toString());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_IS_ALBUM, mediaShare.isAlbum() ? 1 : 0);

        long returnValue = database.insert(DBHelper.LOCAL_SHARE_TABLE_NAME, null, contentValues);

        close();

        return returnValue;
    }

    public long deleteTask(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.TASK_TABLE_NAME, DBHelper.TASK_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteRemoteComment(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_COMMENT_TABLE_NAME, DBHelper.COMMENT_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteAllRemoteComment() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.REMOTE_COMMENT_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteLocalComment(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, DBHelper.COMMENT_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

    public long deleteLocalCommentByUUid(String uuid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        close();

        return returnValue;
    }

    public long deleteAllLocalComment() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_COMMENT_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public long deleteLocalShare(int id) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_SHARE_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;

    }


    public long deleteLocalShareByUUid(String UUid) {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, DBHelper.LOCAL_SHARE_KEY_UUID + " = ?", new String[]{UUid});

        close();

        return returnValue;
    }

    public long deleteAllLocalShare() {

        openWritableDB();

        long returnValue = database.delete(DBHelper.LOCAL_SHARE_TABLE_NAME, null, null);

        close();

        return returnValue;
    }

    public List<OfflineTask> getAllOfflineTask() {

        openReadableDB();

        List<OfflineTask> list = new ArrayList<>();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.TASK_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            OfflineTask offlineTask = new OfflineTask();
            offlineTask.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_KEY_ID)));
            offlineTask.setHttpType(OfflineTask.HttpType.valueOf(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_KEY_HTTP_TYPE))));
            offlineTask.setOperationType(OfflineTask.OperationType.valueOf(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_KEY_OPERATION_TYPE))));
            offlineTask.setRequest(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_KEY_REQUEST)));
            offlineTask.setData(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_KEY_DATA)));
            offlineTask.setOperationCount(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_KEY_OPERATION_COUNT)));
            list.add(offlineTask);
        }
        cursor.close();

        close();

        return list;
    }

    public Map<String, List<Comment>> getAllLocalImageComment() {

        openReadableDB();

        Map<String, List<Comment>> map = new HashMap<>();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_COMMENT_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String imageUuid = cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_IMAGE_UUID));
            List<Comment> commentList;
            if (map.containsKey(imageUuid)) {

                commentList = map.get(imageUuid);
                Comment comment = new Comment();
                comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
                comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
                comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
                comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
                comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
                comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
                commentList.add(comment);

            } else {
                commentList = new ArrayList<>();
                Comment comment = new Comment();
                comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
                comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
                comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
                comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
                comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
                comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
                commentList.add(comment);

                map.put(imageUuid, commentList);
            }
        }
        cursor.close();

        close();

        return map;
    }

    public List<Comment> getLocalImageCommentByUUid(String uuid) {

        openReadableDB();

        List<Comment> commentList = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_COMMENT_TABLE_NAME + " where " + DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});

        while (cursor.moveToNext()) {

            Comment comment = new Comment();
            comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
            comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
            comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
            comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
            comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
            comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
            commentList.add(comment);

        }
        cursor.close();

        close();

        return commentList;
    }

    public List<Comment> getRemoteImageCommentByUUid(String uuid) {

        openReadableDB();

        List<Comment> commentList = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_COMMENT_TABLE_NAME + " where " + DBHelper.COMMENT_IMAGE_UUID + " = ?", new String[]{uuid});
        while (cursor.moveToNext()) {

            Comment comment = new Comment();
            comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
            comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
            comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
            comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
            comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
            comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
            commentList.add(comment);

        }
        cursor.close();

        close();

        return commentList;
    }

    public Map<String, List<Comment>> getAllRemoteImageComment() {

        openReadableDB();

        Map<String, List<Comment>> map = new HashMap<>();
        Cursor cursor = database.rawQuery("select * from " + DBHelper.REMOTE_COMMENT_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String imageUuid = cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_IMAGE_UUID));
            List<Comment> commentList;
            if (map.containsKey(imageUuid)) {

                commentList = map.get(imageUuid);
                Comment comment = new Comment();
                comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
                comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
                comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
                comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
                comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
                comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
                commentList.add(comment);

            } else {
                commentList = new ArrayList<>();
                Comment comment = new Comment();
                comment.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COMMENT_KEY_ID)));
                comment.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_CREATOR)));
                comment.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TIME)));
                comment.setFormatTime(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_FORMAT_TIME)));
                comment.setShareId(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_SHARE_ID)));
                comment.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COMMENT_KEY_TEXT)));
                commentList.add(comment);

                map.put(imageUuid, commentList);
            }
        }
        cursor.close();

        close();

        return map;
    }

    public List<MediaShare> getAllLocalAlbum() {

        openReadableDB();

        List<MediaShare> list = new ArrayList<>();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_SHARE_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            boolean isAlbum = cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_IS_ALBUM)) == 1;
            if (!isAlbum)
                continue;

            MediaShare mediaShare = new MediaShare();
            mediaShare.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_ID)));
            mediaShare.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_UUID)));
            mediaShare.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_CREATOR)));
            mediaShare.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TIME)));
            mediaShare.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TITLE)));
            mediaShare.setDesc(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DESC)));
            mediaShare.setImageDigests(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DIGEST)).split(",")));
            mediaShare.setViewer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_VIEWER)).split(",")));
            mediaShare.setMaintainer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_MAINTAINER)).split(",")));
            mediaShare.setAlbum(true);
            list.add(mediaShare);
        }
        cursor.close();

        close();

        return list;
    }

    public List<MediaShare> getAllLocalShare() {

        openReadableDB();

        List<MediaShare> list = new ArrayList<>();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_SHARE_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            MediaShare mediaShare = new MediaShare();
            mediaShare.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_ID)));
            mediaShare.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_UUID)));
            mediaShare.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_CREATOR)));
            mediaShare.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TIME)));
            mediaShare.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TITLE)));
            mediaShare.setDesc(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DESC)));
            mediaShare.setImageDigests(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DIGEST)).split(",")));
            mediaShare.setViewer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_VIEWER)).split(",")));
            mediaShare.setMaintainer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_MAINTAINER)).split(",")));
            mediaShare.setAlbum(cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_IS_ALBUM)) == 1);
            list.add(mediaShare);
        }
        cursor.close();

        close();

        return list;
    }

    public MediaShare getLocalShareByUuid(String uuid) {

        openReadableDB();

        Cursor cursor = database.rawQuery("select * from " + DBHelper.LOCAL_SHARE_TABLE_NAME + " where " + DBHelper.LOCAL_SHARE_KEY_UUID + " = ?", new String[]{uuid});
        MediaShare mediaShare = new MediaShare();
        while (cursor.moveToNext()) {
            mediaShare.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_ID)));
            mediaShare.setUuid(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_UUID)));
            mediaShare.setCreator(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_CREATOR)));
            mediaShare.setTime(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TIME)));
            mediaShare.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_TITLE)));
            mediaShare.setDesc(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DESC)));
            mediaShare.setImageDigests(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_DIGEST)).split(",")));
            mediaShare.setViewer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_VIEWER)).split(",")));
            mediaShare.setMaintainer(Arrays.asList(cursor.getString(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_MAINTAINER)).split(",")));
            mediaShare.setAlbum(cursor.getInt(cursor.getColumnIndex(DBHelper.LOCAL_SHARE_KEY_IS_ALBUM)) == 1);
        }
        cursor.close();

        close();

        return mediaShare;
    }

    public long updateLocalShare(MediaShare mediaShare, String Uuid) {

        openWritableDB();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_UUID, mediaShare.getUuid());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_CREATOR, mediaShare.getCreator());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_TIME, mediaShare.getTime());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_TITLE, mediaShare.getTitle());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_DESC, mediaShare.getDesc());
        StringBuilder builder = new StringBuilder();
        for (String image : mediaShare.getImageDigests()) {
            builder.append(image);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_DIGEST, builder.toString());

        builder.setLength(0);
        for (String viewer : mediaShare.getViewer()) {
            builder.append(viewer);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_VIEWER, builder.toString());

        builder.setLength(0);
        for (String maintainer : mediaShare.getMaintainer()) {
            builder.append(maintainer);
            builder.append(",");
        }

        contentValues.put(DBHelper.LOCAL_SHARE_KEY_MAINTAINER, builder.toString());
        contentValues.put(DBHelper.LOCAL_SHARE_KEY_IS_ALBUM, mediaShare.isAlbum() ? 1 : 0);

        long returnValue = database.update(DBHelper.LOCAL_SHARE_TABLE_NAME, contentValues, DBHelper.LOCAL_SHARE_KEY_UUID + " = ?", new String[]{Uuid});

        close();

        return returnValue;
    }

    public long modifyOperationCount(int operationCount, int id) {

        openWritableDB();

        //database.execSQL("update " + DBHelper.TABLE_NAME + " set " + DBHelper.KEY_OPERATION_COUNT + " = " + operationCount + " where " + DBHelper.KEY_ID + " = " + id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TASK_KEY_OPERATION_COUNT, operationCount);
        long returnValue = database.update(DBHelper.TASK_TABLE_NAME, contentValues, DBHelper.TASK_KEY_ID + " = ?", new String[]{String.valueOf(id)});

        close();

        return returnValue;
    }

}
