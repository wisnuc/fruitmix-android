package com.winsun.fruitmix.mediaModule;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.EditTextPreIme;
import com.winsun.fruitmix.eventbus.MediaShareCommentOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.CustomTransitionListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/5/9.
 */
public class MediaShareCommentActivity extends AppCompatActivity implements IImageLoadListener {

    public static final String TAG = MediaShareCommentActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.send)
    ImageView ivSend;
    @BindView(R.id.mainPic)
    NetworkImageView ivMain;
    @BindView(R.id.send_text)
    EditTextPreIme tfContent;
    @BindView(R.id.comment_list)
    RecyclerView lvComment;

    private Media media;
    private List<Comment> commentData;

    private String mComment;

    private Context mContext;

    private CommentRecyclerViewAdapter mAdapter;

    private ProgressDialog mDialog;

    private ImageLoader mImageLoader;

    private boolean showSoftInputWhenEnter = false;

    private GestureDetectorCompat gestureDetectorCompat;

    private boolean isExpanded = false;
    private boolean isRecyclerViewScrollToEnd = false;

    private boolean isRecyclerViewScrollToEndTemp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.postponeEnterTransition(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_comment);

        mContext = this;

        ButterKnife.bind(this);

        initImageLoader();

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        lvComment.setLayoutManager(manager);
        lvComment.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new CommentRecyclerViewAdapter();
        lvComment.setAdapter(mAdapter);
        lvComment.addOnScrollListener(new RecyclerScrollListener());

        setSupportActionBar(toolbar);

        collapsingToolbarLayout.setTitle(getString(R.string.comment));
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(mContext, R.color.white));
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedToolbarTitle);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {

                    isExpanded = true;

                } else {

                    isExpanded = false;

                    isRecyclerViewScrollToEndTemp = isRecyclerViewScrollToEnd;
                    isRecyclerViewScrollToEnd = false;

                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) lvComment.getLayoutManager();
                    if (isRecyclerViewScrollToEndTemp) {
                        linearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        tfContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    appBarLayout.setExpanded(false);
                } else {
                    appBarLayout.setExpanded(true);
                }
            }
        });

        commentData = new ArrayList<>();

        showSoftInputWhenEnter = getIntent().getBooleanExtra(Util.KEY_SHOW_SOFT_INPUT_WHEN_ENTER, false);

        retrieveMediaByImageKey();

        loadMedia();

        reloadList();

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tfContent.clearFocus();
                Util.hideSoftInput(MediaShareCommentActivity.this);

                if(!Util.getNetworkState(mContext)){
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                mComment = tfContent.getText() + "";

                if (mComment.isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.send_comment_hint), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "onClick: mediaUUID:" + media.getUuid());

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                FNAS.createLocalMediaComment(mContext, media.getUuid(), createComment(media.getBelongingMediaShareUUID(), mComment));
            }
        });

        GestureListener gestureListener = new GestureListener();
        gestureDetectorCompat = new GestureDetectorCompat(mContext, gestureListener);

    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);
    }

    private void loadMedia() {
        ivMain.registerImageLoadListener(this);
        ivMain.setTransitionName(media.getUuid());

        mImageLoader.setShouldCache(!media.isLocal());

        if (media.isLocal())
            ivMain.setOrientationNumber(media.getOrientationNumber());

        String url = media.getImageOriginalUrl(this);
        ivMain.setTag(url);
        ivMain.setDefaultImageResId(R.drawable.new_placeholder);
//        ivMain.setDefaultBackgroundColor(ContextCompat.getColor(mContext,R.color.default_imageview_color));

        ivMain.setOrientationNumber(media.getOrientationNumber());
        ivMain.setImageUrl(url, mImageLoader);
    }

    private void retrieveMediaByImageKey() {
        Media imageRaw;

        String imageKey = getIntent().getStringExtra(Util.IMAGE_KEY);

        imageRaw = LocalCache.findMediaInLocalMediaMap(imageKey);
        if (imageRaw == null) {
            imageRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(imageKey);

            if (imageRaw == null) {
                media = new Media();

            } else {
                media = imageRaw;
                media.setLocal(false);
            }

        } else {

            media = imageRaw;
            media.setLocal(true);
        }

        media.setSelected(false);

        for (MediaShare shareRaw : LocalCache.RemoteMediaShareMapKeyIsUUID.values()) {

            if (shareRaw.getMediaUUIDInMediaShareContents().contains(media.getUuid())) {
                Log.d(TAG, "shareRaw uuid: " + shareRaw.getUuid());

                media.setBelongingMediaShareUUID(shareRaw.getUuid());
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ivMain.unregisterImageLoadListener();

        mContext = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finishActivity();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        switch (action) {
            case Util.LOCAL_COMMENT_DELETED:

                Log.i(TAG, "local comment changed");

                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();

                if (operationEvent.getOperationResult().getOperationResultType().equals(OperationResultType.SUCCEED)) {
                    reloadList();
                }

                break;
            case Util.REMOTE_COMMENT_CREATED: {

                OperationResultType operationResultType = operationEvent.getOperationResult().getOperationResultType();

                switch (operationResultType) {
                    case SUCCEED:

                        Comment comment = ((MediaShareCommentOperationEvent) operationEvent).getComment();
                        String imageUUID = ((MediaShareCommentOperationEvent) operationEvent).getImageUUID();

                        FNAS.deleteLocalMediaComment(mContext, imageUUID, comment);

                        break;
                    case MALFORMED_URL_EXCEPTION:
                    case SOCKET_TIMEOUT_EXCEPTION:
                    case IO_EXCEPTION:

                        if (mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();

                        Toast.makeText(mContext, operationEvent.getOperationResult().getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                        break;
                }

                break;
            }
            case Util.LOCAL_COMMENT_CREATED: {

                OperationResultType operationResultType = operationEvent.getOperationResult().getOperationResultType();

                switch (operationResultType) {
                    case SUCCEED:

                        Comment comment = ((MediaShareCommentOperationEvent) operationEvent).getComment();

                        if (Util.getNetworkState(mContext)) {

                            String imageUUID = ((MediaShareCommentOperationEvent) operationEvent).getImageUUID();

                            FNAS.createRemoteMediaComment(mContext, imageUUID, comment);
                        }

                        tfContent.setText("");
                        break;
                    case MALFORMED_URL_EXCEPTION:
                    case SOCKET_TIMEOUT_EXCEPTION:
                    case IO_EXCEPTION:
                        if (mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();

                        Toast.makeText(mContext, operationEvent.getOperationResult().getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                        tfContent.setText("");
                        break;
                }

                break;
            }
            case Util.LOCAL_MEDIA_COMMENT_RETRIEVED:
            case Util.REMOTE_MEDIA_COMMENT_RETRIEVED:
                reloadList();
                break;
        }


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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetectorCompat.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


            if (e1 != null && e2 != null && e2.getY() - e1.getY() > 20 && isExpanded) {

                finishActivity();

            } else if (e1 != null && e2 != null && e1.getY() - e2.getY() > 20 && isRecyclerViewScrollToEnd) {

                tfContent.requestFocus();
                Util.showSoftInput(MediaShareCommentActivity.this, tfContent);

                //TODO:研究共享元素转场动画，是否支持灵活自定义，以及卡顿问题
            }

            return false;

        }
    }

    private class RecyclerScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = lvComment.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

                    int lastItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    isRecyclerViewScrollToEnd = lastItemPosition + 1 == mAdapter.getItemCount();

                }
            }

        }
    }

    private Comment createComment(String shareId, String commentText) {

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

        fillCommentData();

        sortCommentData();

        mAdapter.commentList.clear();
        mAdapter.commentList.addAll(commentData);
        mAdapter.notifyDataSetChanged();

    }

    private void fillCommentData() {
        for (Map.Entry<String, List<Comment>> entry : LocalCache.LocalMediaCommentMapKeyIsImageUUID.entrySet()) {
            if (entry.getKey().equals(media.getUuid())) {
                commentData.addAll(entry.getValue());
            }
        }

        for (Map.Entry<String, List<Comment>> entry : LocalCache.RemoteMediaCommentMapKeyIsImageUUID.entrySet()) {
            if (entry.getKey().equals(media.getUuid())) {
                commentData.addAll(entry.getValue());
            }
        }
    }

    private void sortCommentData() {
        Collections.sort(commentData, new Comparator<Comment>() {
            @Override
            public int compare(Comment lhs, Comment rhs) {

                long time1 = Long.parseLong(lhs.getTime());
                long time2 = Long.parseLong(rhs.getTime());
                if (time1 < time2)
                    return 1;
                else if (time1 > time2)
                    return -1;
                else return 0;

            }
        });
        Log.d(TAG, commentData + "");
    }

    private class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewHolder> {

        List<Comment> commentList;

        CommentRecyclerViewAdapter() {
            commentList = new ArrayList<>();
        }

        @Override
        public int getItemCount() {
            if (commentList == null) return 0;
            return commentList.size();
        }

        @Override
        public CommentRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.share_comment_cell, parent, false);

            return new CommentRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentRecyclerViewHolder holder, int position) {
            holder.refreshView(commentList.get(position));
        }
    }

    class CommentRecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avatar)
        TextView ivAvatar;
        @BindView(R.id.comment)
        TextView lbComment;
        @BindView(R.id.date)
        TextView lbDate;
        @BindView(R.id.normal)
        View vNormal;
        @BindView(R.id.header)
        View vHeader;

        CommentRecyclerViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(Comment currentItem) {

            refreshViewVisibility(currentItem);

            if (currentItem != null) {
                User map = LocalCache.RemoteUserMapKeyIsUUID.get(currentItem.getCreator());
                ivAvatar.setText(map.getDefaultAvatar());
                ivAvatar.setBackgroundResource(map.getDefaultAvatarBgColorResourceId());
            }
        }

        private void refreshViewVisibility(Comment currentItem) {
            if (currentItem == null) {
                vNormal.setVisibility(View.GONE);
                vHeader.setVisibility(View.VISIBLE);
            } else {
                vNormal.setVisibility(View.VISIBLE);
                vHeader.setVisibility(View.GONE);
                lbComment.setText(currentItem.getText());
                lbDate.setText(currentItem.getFormatTime());
            }
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
        } else {
            tfContent.requestFocus();
            Util.showSoftInput(MediaShareCommentActivity.this, tfContent);
        }
    }

}

