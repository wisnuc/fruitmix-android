package com.winsun.fruitmix;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.OfflineTask;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.services.LocalCommentService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;

import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Administrator on 2016/5/9.
 */
public class ShareCommentActivity extends Activity {

    public static final String TAG = ShareCommentActivity.class.getSimpleName();

    ImageView ivBack, ivSend;
    NetworkImageView ivMain;
    Map<String, Object> imageData;
    List<Comment> commentData;
    EditText tfContent;

    ListView lvComment;

    String mCommment;

    private DBUtils dbUtils;

    private Context mContext;

    private CustomBroadCastReceiver mReceiver;
    private LocalBroadcastManager mManager;
    private CommentListViewAdapter mAdapter;

    private ProgressDialog mDialog;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Map<String, String> imageRaw;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_comment);

        mContext = this;

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "JWT " + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mReceiver = new CustomBroadCastReceiver();
        mManager = LocalBroadcastManager.getInstance(this);
        mManager.registerReceiver(mReceiver, new IntentFilter(Util.LOCAL_COMMENT_CHANGED));

        lvComment = (ListView) findViewById(R.id.comment_list);
        mAdapter = new CommentListViewAdapter();
        lvComment.setAdapter(mAdapter);

        ivBack = (ImageView) findViewById(R.id.back);
        ivSend = (ImageView) findViewById(R.id.send);
        tfContent = (EditText) findViewById(R.id.send_text);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commentData = new ArrayList<>();

        String imageUUID = getIntent().getStringExtra("imageUUID");
        imageRaw = LocalCache.MediasMap.get(imageUUID);
        if (imageRaw == null) {
            imageRaw = LocalCache.LocalImagesMap2.get(imageUUID);

            imageData = new HashMap<String, Object>();
            imageData.put("uuid", imageRaw.get("uuid"));
            imageData.put("width", imageRaw.get("width"));
            imageData.put("height", imageRaw.get("height"));
            imageData.put("cacheType", "local");
            imageData.put("thumb", imageRaw.get("thumb"));
        } else {
            imageData = new HashMap<String, Object>();
            imageData.put("uuid", imageRaw.get("uuid"));
            imageData.put("width", imageRaw.get("width"));
            imageData.put("height", imageRaw.get("height"));
            imageData.put("cacheType", "nas");
            imageData.put("resHash", imageRaw.get("uuid"));
        }

        Map<String, Map<String, String>> documentMap = new HashMap<>();
        documentMap.putAll(LocalCache.DocumentsMap);

        for (Map<String, String> shareRaw : documentMap.values()) {

            Log.d("winsun", "sss1 " + shareRaw);
            if (shareRaw.containsKey("images") && shareRaw.get("images").contains((String) imageData.get("uuid"))) {
                Log.d("winsun", "ssss " + shareRaw.get("uuid"));
                imageData.put("shareInstance", shareRaw.get("uuid"));
                break;
            }
        }

        ivMain = (NetworkImageView) findViewById(R.id.mainPic);

        //load ivMain
        if (imageData.get("cacheType").equals("local")) {
            String url = String.valueOf(imageData.get("thumb"));

            mImageLoader.setShouldCache(false);
            ivMain.setTag(url);
            ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
            ivMain.setImageUrl(url, mImageLoader);
        } else {
            String url = FNAS.Gateway + "/media/" + imageData.get("resHash") + "?type=original";

            mImageLoader.setShouldCache(true);
            ivMain.setTag(url);
            ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
            ivMain.setImageUrl(url, mImageLoader);
        }

        reloadList();

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Util.hideSoftInput(ShareCommentActivity.this);

                mCommment = tfContent.getText() + "";

                if (mCommment.isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.no_comment_content), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("winsun", tfContent.getText() + "");
                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);

                    }

                    @Override
                    protected Boolean doInBackground(Object... params) {

//                        String request = "/media/" + imageData.get("uuid") + "?type=comments";
//                        String data = "{\"shareid\":\"" + imageData.get("shareInstance") + "\", \"text\":\"" + mCommment + "\"}";

                        createCommentInLocalCommentDatabase(String.valueOf(imageData.get("uuid")), String.valueOf(imageData.get("shareInstance")), mCommment);

                        if (Util.getNetworkState(mContext)) {
                            LocalCommentService.startActionLocalCommentTask(mContext);
                        }

                        return true;

/*                        try {
                            FNAS.PostRemoteCall(request, data);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();

                            // insert offline work add by liang.wu
                            DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                            dbUtils.openWritableDB();

                            OfflineTask offlineTask = new OfflineTask();
                            offlineTask.setHttpType(OfflineTask.HttpType.POST);
                            offlineTask.setOperationType(OfflineTask.OperationType.CREATE);
                            offlineTask.setRequest(request);
                            offlineTask.setData(data);
                            offlineTask.setOperationCount(0);
                            dbUtils.insertTask(offlineTask);

                            //show local change add by liang.wu
                            if (commentData == null) {
                                commentData = new ArrayList<>();
                            }
                            Comment commentItem = new Comment();
                            commentItem.setCreator(FNAS.userUUID);
                            commentItem.setTime(String.valueOf(System.currentTimeMillis()));
                            commentItem.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
                            commentItem.setShareId("");
                            commentItem.setText(mCommment);
                            commentData.add(commentItem);

                            String uuid = String.valueOf(imageData.get("uuid"));
                            dbUtils.insertComment(commentItem, uuid);

                            dbUtils.close();
                            //end add

                            return false;
                        }*/
                    }

                    @Override
                    protected void onPostExecute(Boolean sSuccess) {

                        if (sSuccess) {
                            reloadList();

                            tfContent.setText("");

                        } else {
                            //Snackbar.make(ivSend, getString(R.string.operation_fail), Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();

                            tfContent.setText("");
                            //end add
                        }

                    }

                }.execute();
            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createCommentInLocalCommentDatabase(String imageUUid, String shareId, String commentText) {
        dbUtils = DBUtils.SINGLE_INSTANCE;

        Comment commentItem = new Comment();
        commentItem.setCreator(FNAS.userUUID);
        commentItem.setTime(String.valueOf(System.currentTimeMillis()));
        commentItem.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
        commentItem.setShareId(shareId);
        commentItem.setText(commentText);
        commentData.add(commentItem);

        String uuid = String.valueOf(imageUUid);

        Log.i(TAG, "create uuid" + uuid);

        dbUtils.insertLocalComment(commentItem, uuid);

    }

    public void reloadList() {
        new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected Boolean doInBackground(Object... params) {
                String str;
                Comment commentItem;
                JSONArray json;
                int i;

                try {

                    commentData.clear();

                    String uuid = String.valueOf(imageData.get("uuid"));

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
                    commentData.addAll(dbUtils.getLocalImageCommentByUUid(uuid));

                    if (Util.getNetworkState(mContext)) {

                        str = FNAS.RemoteCall("/media/" + imageData.get("uuid") + "?type=comments");
                        json = new JSONArray(str);
                        for (i = 0; i < json.length(); i++) {
                            commentItem = new Comment();
                            commentItem.setCreator(json.getJSONObject(i).getString("creator"));
                            commentItem.setTime(json.getJSONObject(i).getString("datatime"));
                            commentItem.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(json.getJSONObject(i).getString("datatime")))));
                            commentItem.setShareId(json.getJSONObject(i).getString("shareid"));
                            commentItem.setText(json.getJSONObject(i).getString("text"));
                            commentData.add(commentItem);
                        }
                    } else {

                        commentData.addAll(dbUtils.getRemoteImageCommentByUUid(uuid));
                    }

                    Collections.sort(commentData, new Comparator<Comment>() {
                        @Override
                        public int compare(Comment lhs, Comment rhs) {

                            long mtime1 = Long.parseLong(lhs.getTime());
                            long mtime2 = Long.parseLong(rhs.getTime());
                            if (mtime1 < mtime2)
                                return 1;
                            else if (mtime1 > mtime2)
                                return -1;
                            else return 0;

                        }
                    });
                    Log.d(TAG, commentData + "");
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean sSuccess) {

                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();

                //reloadList();
                mAdapter.commentList.clear();
                mAdapter.commentList.addAll(commentData);
                ((BaseAdapter) (lvComment.getAdapter())).notifyDataSetChanged();
            }

        }.execute();
    }

    private class CustomBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Util.LOCAL_COMMENT_CHANGED)) {
                reloadList();
            }
        }
    }

    class CommentListViewAdapter extends BaseAdapter {

        List<Comment> commentList;

        public CommentListViewAdapter() {
            commentList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            if (commentList == null) return 0;
            return commentList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            TextView ivAvatar;
            TextView lbComment, lbDate;
            View vNormal, vHeader;
            final Comment currentItem;

            if (convertView == null)
                view = LayoutInflater.from(ShareCommentActivity.this).inflate(R.layout.share_comment_cell, parent, false);
            else view = convertView;

            vNormal = (View) view.findViewById(R.id.normal);
            vHeader = (View) view.findViewById(R.id.header);

            ivAvatar = (TextView) view.findViewById(R.id.avatar);
            lbComment = (TextView) view.findViewById(R.id.comment);
            lbDate = (TextView) view.findViewById(R.id.date);

            currentItem = (Comment) this.getItem(position);

            if (currentItem == null) {
                vNormal.setVisibility(View.GONE);
                vHeader.setVisibility(View.VISIBLE);
            } else {
                vNormal.setVisibility(View.VISIBLE);
                vHeader.setVisibility(View.GONE);
                lbComment.setText(currentItem.getText());
                lbDate.setText(currentItem.getFormatTime());
            }

            Map<String, String> map = LocalCache.UsersMap.get(currentItem.getCreator());
            ivAvatar.setText(map.get("avatar_default"));

            int color = Integer.parseInt(map.get("avatar_default_color"));
            switch (color) {
                case 0:
                    ivAvatar.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                    break;
                case 1:
                    ivAvatar.setBackgroundResource(R.drawable.user_portrait_bg_green);
                    break;
                case 2:
                    ivAvatar.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                    break;
            }

            return view;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Object getItem(int position) {
            return commentList.get(position);
        }
    }
}

