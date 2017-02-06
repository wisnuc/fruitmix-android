package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaInMediaShareLoader;
import com.winsun.fruitmix.model.ImageGifLoaderInstance;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/4/28.
 */
public class AlbumPicContentActivity extends AppCompatActivity {

    public static final String TAG = AlbumPicContentActivity.class.getSimpleName();

    @BindView(R.id.mainGrid)
    GridView mainGridView;
    @BindView(R.id.back)
    ImageView ivBack;
    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private List<Media> mediaList;

    private MenuItem mPrivatePublicMenu;

    private MediaShare mediaShare;

    private Context mContext;

    private boolean mShowMenu;

    private ImageLoader mImageLoader;

    private boolean mShowCommentBtn = false;

    private ProgressDialog mDialog;

    private boolean isOperated = false;

    private Bundle reenterState;

    private MediaInMediaShareLoader loader;
    private MediaInMediaShareLoader.OnMediaInMediaShareLoadListener listener;

    private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (reenterState != null) {

                int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
                int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

                if (initialPhotoPosition != currentPhotoPosition) {

                    names.clear();
                    sharedElements.clear();

                    Media media = mediaList.get(currentPhotoPosition);

                    String sharedElementName = media.getKey();
                    View newSharedElement = mainGridView.findViewWithTag(sharedElementName);

                    names.add(sharedElementName);
                    sharedElements.put(sharedElementName, newSharedElement);
                }

                reenterState = null;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContext = this;

        setExitSharedElementCallback(sharedElementCallback);

        initImageLoader();

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);
        mediaShare = LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);

        mShowMenu = getIntent().getBooleanExtra(Util.NEED_SHOW_MENU, true);
        mShowCommentBtn = getIntent().getBooleanExtra(Util.KEY_SHOW_COMMENT_BTN, false);

        setContentView(R.layout.activity_album_pic_content);

        ButterKnife.bind(this);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackOperation();
                finish();
            }
        });

        mainGridView.setAdapter(new PicGridViewAdapter(this));

        mTitleTextView.setText(mediaShare.getTitle());

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        loader = MediaInMediaShareLoader.INSTANCE;
        initOnMediaInMediaShareLoadListener();
        loadMedia(true, true);
    }

    private void loadMedia(boolean clearMedias, boolean reloadMedias) {
        loader.startLoad(mediaShare.getMediaUUIDInMediaShareContents(), listener, clearMedias, reloadMedias);
    }

    private void initOnMediaInMediaShareLoadListener() {
        listener = new MediaInMediaShareLoader.OnMediaInMediaShareLoadListener() {
            @Override
            public void onMediaInMediaShareLoaded() {

                mediaList = loader.getMedias();

                if (loadingLayout.getVisibility() != View.GONE)
                    loadingLayout.setVisibility(View.GONE);

                if (mediaList.isEmpty()) {
                    mainGridView.setVisibility(View.GONE);
                    noContentLayout.setVisibility(View.VISIBLE);
                } else {
                    mainGridView.setVisibility(View.VISIBLE);
                    noContentLayout.setVisibility(View.GONE);
                    ((BaseAdapter) mainGridView.getAdapter()).notifyDataSetChanged();
                }
            }
        };
    }

    private void initImageLoader() {

        ImageGifLoaderInstance imageGifLoaderInstance = ImageGifLoaderInstance.INSTANCE;
        mImageLoader = imageGifLoaderInstance.getImageLoader(mContext);

    }

    @Override
    protected void onStart() {
        super.onStart();

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

        mContext = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.LOCAL_SHARE_DELETED) || action.equals(Util.REMOTE_SHARE_DELETED)) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            OperationResult operationResult = operationEvent.getOperationResult();

            OperationResultType operationResultType = operationResult.getOperationResultType();

            switch (operationResultType) {
                case SUCCEED:
                    ((Activity) mContext).setResult(RESULT_OK);
                    break;
                case LOCAL_MEDIA_SHARE_UPLOADING:
                case NO_NETWORK_EXCEPTION:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                    break;
            }

            finish();

        } else if (action.equals(Util.LOCAL_SHARE_MODIFIED) || action.equals(Util.REMOTE_SHARE_MODIFIED)) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            OperationResult operationResult = operationEvent.getOperationResult();

            OperationResultType operationResultType = operationResult.getOperationResultType();

            switch (operationResultType) {
                case SUCCEED:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

                    mediaShare = ((MediaShareOperationEvent) operationEvent).getMediaShare();

                    if (mediaShare.getViewersListSize() == 0) {
                        mPrivatePublicMenu.setTitle(getString(R.string.set_public));
                    } else {
                        mPrivatePublicMenu.setTitle(getString(R.string.set_private));
                    }

                    isOperated = true;

                    break;
                default:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                    break;
            }

        }

    }

    @Override
    public void onBackPressed() {

        onBackOperation();

        super.onBackPressed();
    }

    private void onBackOperation() {
        if (isOperated)
            setResult(RESULT_OK);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        reenterState = new Bundle(data.getExtras());
        int initialPhotoPosition = reenterState.getInt(Util.INITIAL_PHOTO_POSITION);
        int currentPhotoPosition = reenterState.getInt(Util.CURRENT_PHOTO_POSITION);

        if (initialPhotoPosition != currentPhotoPosition) {

            mainGridView.smoothScrollToPosition(currentPhotoPosition);

/*            ActivityCompat.postponeEnterTransition(AlbumPicContentActivity.this);
            mainGridView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mainGridView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mainGridView.requestLayout();


                    return true;
                }
            });*/
            ActivityCompat.startPostponedEnterTransition(AlbumPicContentActivity.this);
        }
    }

    private void showPhotoSlider(int position, View sharedElement, String sharedElementName) {

        PhotoSliderActivity.setMediaList(mediaList);

        Intent intent = new Intent();
        intent.putExtra(Util.INITIAL_PHOTO_POSITION, position);
        intent.putExtra(Util.KEY_SHOW_COMMENT_BTN, mShowCommentBtn);
        intent.setClass(this, PhotoSliderActivity.class);

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedElement, sharedElementName);
        startActivity(intent, optionsCompat.toBundle());
    }

    class PicGridViewAdapter extends BaseAdapter {

        AlbumPicContentActivity activity;

        public PicGridViewAdapter(AlbumPicContentActivity activity_) {
            activity = activity_;
        }

        @Override
        public int getCount() {
            if (activity.mediaList == null) return 0;
            return activity.mediaList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            final Media currentItem;
            final NetworkImageView ivMain;

            if (convertView == null)
                view = LayoutInflater.from(activity).inflate(R.layout.photo_list_cell, parent, false);
            else view = convertView;

            currentItem = (Media) this.getItem(position);

            ivMain = (NetworkImageView) view.findViewById(R.id.mainPic);

            String imageUrl = currentItem.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!currentItem.isLocal());

            if (currentItem.isLocal())
                ivMain.setOrientationNumber(currentItem.getOrientationNumber());

            ivMain.setTag(imageUrl);
            ivMain.setDefaultImageResId(R.drawable.placeholder_photo);
            ivMain.setImageUrl(imageUrl, mImageLoader);

            ivMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String sharedElementName = currentItem.getKey();
                    ViewCompat.setTransitionName(ivMain, sharedElementName);
                    activity.showPhotoSlider(position, ivMain, currentItem.getKey());
                }
            });

            return view;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public Object getItem(int position) {
            return activity.mediaList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mShowMenu) {

            getMenuInflater().inflate(R.menu.album_menu, menu);

            mPrivatePublicMenu = menu.findItem(R.id.set_private_public);

            if (mediaShare.getViewersListSize() == 0) {
                mPrivatePublicMenu.setTitle(getString(R.string.set_public));
            } else {
                mPrivatePublicMenu.setTitle(getString(R.string.set_private));
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, mediaShare.isLocal() + "");

        if (Util.getNetworkState(mContext)) {
            if (mediaShare.isLocal()) {
                Toast.makeText(mContext, getString(R.string.local_media_share_uploading), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            if (!mediaShare.isLocal()) {
                Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        if (!checkPermissionToOperate()) {
            Toast.makeText(mContext, getString(R.string.no_operate_media_share_permission), Toast.LENGTH_SHORT).show();

            return true;
        }

        Intent intent;
        switch (item.getItemId()) {
            case R.id.setting_album:
                intent = new Intent(this, ModifyAlbumActivity.class);
                intent.putExtra(Util.MEDIASHARE_UUID, mediaShare.getUuid());
                startActivityForResult(intent, Util.KEY_MODIFY_ALBUM_REQUEST_CODE);
                break;
            case R.id.edit_photo:
                intent = new Intent(this, EditPhotoActivity.class);
                intent.putExtra(Util.KEY_MEDIA_SHARE_UUID, mediaShare.getUuid());
                startActivityForResult(intent, Util.KEY_EDIT_PHOTO_REQUEST_CODE);
                break;
            case R.id.set_private_public:
                setPublicPrivate();
                break;
            case R.id.delete_album:
                deleteCurrentAlbum();
                break;
            default:
        }

        return true;
    }

    private boolean checkPermissionToOperate() {
        return mediaShare.checkMaintainersListContainCurrentUserUUID() || mediaShare.getCreatorUUID().equals(FNAS.userUUID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_EDIT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            String mediaShareUUID = data.getStringExtra(Util.KEY_MEDIA_SHARE_UUID);
            mediaShare = LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);

            loadMedia(true,true);

        } else if (requestCode == Util.KEY_MODIFY_ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            String title = data.getStringExtra(Util.UPDATED_ALBUM_TITLE);
            mediaShare.setTitle(title);
            mTitleTextView.setText(mediaShare.getTitle());
        }

        isOperated = true;
    }

    private void deleteCurrentAlbum() {

        new AlertDialog.Builder(mContext).setMessage(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Util.getNetworkState(mContext)) {

                            mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);
                            FNAS.deleteRemoteMediaShare(mContext, mediaShare);

                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).setNegativeButton(getString(R.string.cancel), null).create().show();

    }

    private void setPublicPrivate() {

        if (!Util.getNetworkState(mContext)) {
            Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();

            return;
        }

        MediaShare cloneMediaShare = mediaShare.cloneMyself();
        String requestData;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        if (cloneMediaShare.getViewersListSize() == 0) {


            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                cloneMediaShare.addViewer(userUUID);
            }

            stringBuilder.append(cloneMediaShare.createStringOperateViewersInMediaShare(Util.ADD));

        } else {

            stringBuilder.append(cloneMediaShare.createStringOperateViewersInMediaShare(Util.DELETE));

            cloneMediaShare.clearViewers();
        }

        stringBuilder.append("]");
        requestData = stringBuilder.toString();

        mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

        FNAS.modifyRemoteMediaShare(mContext, cloneMediaShare, requestData);

    }

}
