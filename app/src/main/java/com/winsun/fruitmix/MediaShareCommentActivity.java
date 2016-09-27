package com.winsun.fruitmix;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowCompat;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;

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
public class MediaShareCommentActivity extends Activity implements IImageLoadListener {

    public static final String TAG = MediaShareCommentActivity.class.getSimpleName();

    ImageView ivBack, ivSend;
    NetworkImageView ivMain;
    Media media;
    List<Comment> commentData;
    EditText tfContent;

    ListView lvComment;

    String mCommment;

    private Context mContext;

    private CustomBroadCastReceiver mReceiver;
    private LocalBroadcastManager mManager;
    private CommentListViewAdapter mAdapter;
    private IntentFilter filter;

    private ProgressDialog mDialog;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private boolean showSoftInputWhenEnter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Media imageRaw;

        ActivityCompat.postponeEnterTransition(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_comment);

        mContext = this;

        mRequestQueue = RequestQueueInstance.REQUEST_QUEUE_INSTANCE.getmRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);

        mReceiver = new CustomBroadCastReceiver();
        mManager = LocalBroadcastManager.getInstance(this);
        filter = new IntentFilter(Util.REMOTE_COMMENT_CREATED);
        filter.addAction(Util.LOCAL_COMMENT_CREATED);
        filter.addAction(Util.LOCAL_MEDIA_COMMENT_RETRIEVED);
        filter.addAction(Util.REMOTE_MEDIA_COMMENT_RETRIEVED);
        filter.addAction(Util.LOCAL_COMMENT_DELETED);

        lvComment = (ListView) findViewById(R.id.comment_list);
        mAdapter = new CommentListViewAdapter();
        lvComment.setAdapter(mAdapter);

        ivBack = (ImageView) findViewById(R.id.back);
        ivSend = (ImageView) findViewById(R.id.send);
        tfContent = (EditText) findViewById(R.id.send_text);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        commentData = new ArrayList<>();

        showSoftInputWhenEnter = getIntent().getBooleanExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, false);

        media = new Media();
        String imageUUID = getIntent().getStringExtra(Util.IMAGE_UUID);
        imageRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(imageUUID);
        if (imageRaw == null) {
            imageRaw = LocalCache.LocalMediaMapKeyIsUUID.get(imageUUID);

            media.setLocal(true);
            media.setThumb(imageRaw.getThumb());
        } else {

            media.setLocal(false);
        }

        media.setUuid(imageRaw.getUuid());
        media.setWidth(imageRaw.getWidth());
        media.setHeight(imageRaw.getHeight());
        media.setSelected(false);

        for (MediaShare shareRaw : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {

            Log.d("winsun", "sss1 " + shareRaw);
            if (shareRaw.getImageDigests().contains(media.getUuid())) {
                Log.d("winsun", "ssss " + shareRaw.getUuid());

                media.setBelongingMediaShareUUID(shareRaw.getUuid());
                break;
            }
        }

        ivMain = (NetworkImageView) findViewById(R.id.mainPic);
        ivMain.registerImageLoadListener(this);

        ivMain.setTransitionName(media.getUuid());

        if (media.isLocal()) {
            String url = media.getThumb();

            mImageLoader.setShouldCache(false);
            ivMain.setTag(url);
            ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
            ivMain.setImageUrl(url, mImageLoader);
        } else {
            String url = String.format(getString(R.string.original_photo_url), FNAS.Gateway + Util.MEDIA_PARAMETER + "/" + media.getUuid());

            mImageLoader.setShouldCache(true);
            ivMain.setTag(url);
            ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
            ivMain.setImageUrl(url, mImageLoader);
        }

        reloadList();

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Util.hideSoftInput(MediaShareCommentActivity.this);

                mCommment = tfContent.getText() + "";

                if (mCommment.isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.no_comment_content), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, tfContent.getText() + "");
                Log.i(TAG, "onClick: mediaUUID:" + media.getUuid());

                mDialog = ProgressDialog.show(mContext, getString(R.string.operating_title), getString(R.string.loading_message), true, false);
                Intent operationIntent = new Intent(Util.OPERATION);
                operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
                operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, media.getUuid());
                operationIntent.putExtra(Util.OPERATION_COMMENT, generateComment(media.getBelongingMediaShareUUID(), mCommment));
                mManager.sendBroadcast(operationIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mManager.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mManager.unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void finishAfterTransition() {
        Intent intent = new Intent();

        int initialPhotoPosition = getIntent().getIntExtra(Util.INITIAL_PHOTO_POSITION, 0);

        intent.putExtra(Util.INITIAL_PHOTO_POSITION, initialPhotoPosition);
        intent.putExtra(Util.CURRENT_PHOTO_POSITION, initialPhotoPosition);
        setResult(RESULT_OK, intent);

        super.finishAfterTransition();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private Comment generateComment(String shareId, String commentText) {

        Comment commentItem = new Comment();
        commentItem.setCreator(FNAS.userUUID);
        commentItem.setTime(String.valueOf(System.currentTimeMillis()));
        commentItem.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
        commentItem.setShareId(shareId);
        commentItem.setText(commentText);

        return commentItem;
    }

    public void reloadList() {

        commentData.clear();

        for (Map.Entry<String, Comment> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {
            if (entry.getKey().equals(media.getUuid())) {
                commentData.add(entry.getValue());
            }
        }

        for (Map.Entry<String, Comment> entry : LocalCache.RemoteMediaCommentMapKeyIsImageUUID.entrySet()) {
            if (entry.getKey().equals(media.getUuid())) {
                commentData.add(entry.getValue());
            }
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

        mAdapter.commentList.clear();
        mAdapter.commentList.addAll(commentData);
        ((BaseAdapter) (lvComment.getAdapter())).notifyDataSetChanged();



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
                view = LayoutInflater.from(MediaShareCommentActivity.this).inflate(R.layout.share_comment_cell, parent, false);
            else view = convertView;

            vNormal = view.findViewById(R.id.normal);
            vHeader = view.findViewById(R.id.header);

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

            if (currentItem != null) {
                User map = LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreator());
                ivAvatar.setText(map.getDefaultAvatar());

                int color = Integer.parseInt(map.getDefaultAvatarBgColor());
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

    @Override
    public void onImageLoadFinish(String url, View view) {
        ActivityCompat.startPostponedEnterTransition(this);

        if (showSoftInputWhenEnter) {
            showSoftInputAfterTransitionEnd();
        }

    }

    private void showSoftInputAfterTransitionEnd() {
        if (Util.checkRunningOnLollipopOrHigher()) {
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    tfContent.requestFocus();
                    Util.showSoftInput(MediaShareCommentActivity.this, tfContent);
                }
            });
        }
    }

    private class CustomBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Util.LOCAL_COMMENT_DELETED)) {

                Log.i(TAG, "local comment changed");

                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();

                if (intent.getStringExtra(Util.OPERATION_RESULT).equals(OperationResult.SUCCEED.name())) {
                    reloadList();
                }

            } else if (intent.getAction().equals(Util.REMOTE_COMMENT_CREATED)) {

                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:

                        Comment comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);
                        String imageUUID = intent.getStringExtra(Util.OPERATION_IMAGE_UUID);
                        Intent operationIntent = new Intent(Util.OPERATION);
                        operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.DELETE.name());
                        operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
                        operationIntent.putExtra(Util.OPERATION_COMMENT, comment);
                        operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, imageUUID);
                        mManager.sendBroadcast(operationIntent);

                        break;
                    case FAIL:

                        if (mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();

                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                        break;
                }

            } else if (intent.getAction().equals(Util.LOCAL_COMMENT_CREATED)) {


                String result = intent.getStringExtra(Util.OPERATION_RESULT);

                OperationResult operationResult = OperationResult.valueOf(result);

                switch (operationResult) {
                    case SUCCEED:

                        Comment comment = intent.getParcelableExtra(Util.OPERATION_COMMENT);

                        if (Util.getNetworkState(mContext)) {
                            Intent operationIntent = new Intent(Util.OPERATION);
                            operationIntent.putExtra(Util.OPERATION_TYPE, OperationType.CREATE.name());
                            operationIntent.putExtra(Util.OPERATION_TARGET_TYPE, OperationTargetType.REMOTE_MEDIA_COMMENT.name());
                            operationIntent.putExtra(Util.OPERATION_IMAGE_UUID, intent.getStringExtra(Util.OPERATION_IMAGE_UUID));
                            operationIntent.putExtra(Util.OPERATION_COMMENT, comment);
                            mManager.sendBroadcast(operationIntent);
                        }

                        tfContent.setText("");
                        break;
                    case FAIL:

                        if (mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();

                        Toast.makeText(mContext, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
                        tfContent.setText("");
                        break;
                }

            } else if (intent.getAction().equals(Util.LOCAL_MEDIA_COMMENT_RETRIEVED) || intent.getAction().equals(Util.REMOTE_MEDIA_COMMENT_RETRIEVED)) {
                reloadList();
            }
        }
    }

}

