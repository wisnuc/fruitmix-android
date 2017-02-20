package com.winsun.fruitmix.mediaModule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaInMediaShareLoader;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPhotoActivity extends Activity implements View.OnClickListener {

    public static final String TAG = EditPhotoActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.finish)
    TextView mFinish;
    @BindView(R.id.edit_photo_gridview)
    RecyclerView mEditPhotoRecyclerView;
    @BindView(R.id.add_album)
    FloatingActionButton mAddPhoto;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private int mSpanCount = 3;
    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private MediaShare mediaShare;
    private MediaShare modifiedMediaShare;
    private List<Media> mPhotoList;

    private ImageLoader mImageLoader;

    private ProgressDialog mDialog;

    private MediaInMediaShareLoader loader;
    private MediaInMediaShareLoader.OnMediaInMediaShareLoadListener listener;

    public static final int SPAN_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        ButterKnife.bind(this);

        mContext = this;

        initImageLoader();

        mBack.setOnClickListener(this);
        mAddPhoto.setOnClickListener(this);
        mFinish.setOnClickListener(this);

        String mediaShareUUID = getIntent().getStringExtra(Util.KEY_MEDIA_SHARE_UUID);
        mediaShare = LocalCache.findMediaShareInLocalCacheMap(mediaShareUUID);
        modifiedMediaShare = mediaShare.cloneMyself();

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);

        mEditPhotoRecyclerView.setLayoutManager(manager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

        loader = MediaInMediaShareLoader.INSTANCE;
        initOnMediaInMediaShareLoadListener();
        loadMedia(mediaShare.getMediaUUIDInMediaShareContents(), true, true);
    }

    private void loadMedia(List<String> mediaKeyList, boolean clearMedias, boolean reloadMedias) {
        loader.startLoad(mediaKeyList, listener, clearMedias, reloadMedias);
    }

    private void initOnMediaInMediaShareLoadListener() {
        listener = new MediaInMediaShareLoader.OnMediaInMediaShareLoadListener() {
            @Override
            public void onMediaInMediaShareLoaded() {

                mPhotoList = loader.getMedias();

                if (loadingLayout.getVisibility() != View.GONE)
                    loadingLayout.setVisibility(View.GONE);

                if (mPhotoList.isEmpty()) {
                    mEditPhotoRecyclerView.setVisibility(View.GONE);
                    noContentLayout.setVisibility(View.VISIBLE);
                } else {
                    mEditPhotoRecyclerView.setVisibility(View.VISIBLE);
                    noContentLayout.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        if (action.equals(Util.PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED) || action.equals(Util.PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED)) {

            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            OperationResult operationResult = operationEvent.getOperationResult();

            OperationResultType operationResultType = operationResult.getOperationResultType();

            switch (operationResultType) {
                case SUCCEED:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                    getIntent().putExtra(Util.KEY_MEDIA_SHARE_UUID, modifiedMediaShare.getUuid());
                    EditPhotoActivity.this.setResult(RESULT_OK, getIntent());
                    finish();
                    break;
                case MALFORMED_URL_EXCEPTION:
                case SOCKET_TIMEOUT_EXCEPTION:
                case IO_EXCEPTION:
                case NETWORK_EXCEPTION:
                    Toast.makeText(mContext, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
                    EditPhotoActivity.this.setResult(RESULT_CANCELED, getIntent());
                    finish();
                    break;
            }

        }

    }


    private void fillMediaShareContents(List<String> selectedImageKeys) {
        for (String imageKey : selectedImageKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setMediaUUID(imageKey);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            modifiedMediaShare.addMediaShareContent(mediaShareContent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_album:
                Intent intent = new Intent(mContext, NewAlbumPicChooseActivity.class);

                intent.putStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST, (ArrayList<String>) mediaShare.getMediaUUIDInMediaShareContents());
                intent.putExtra(Util.EDIT_PHOTO, true);
                startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
                break;
            case R.id.finish:

                if (!Util.getNetworkState(mContext)) {
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                MediaShare diffContentsOriginalMediaShare = mediaShare.cloneMyself();
                diffContentsOriginalMediaShare.clearMediaShareContents();
                diffContentsOriginalMediaShare.initMediaShareContents(mediaShare.getDifferentMediaShareContentInCurrentMediaShare(modifiedMediaShare));

                MediaShare diffContentsModifiedMediaShare = modifiedMediaShare.cloneMyself();
                diffContentsModifiedMediaShare.clearMediaShareContents();
                diffContentsModifiedMediaShare.initMediaShareContents(modifiedMediaShare.getDifferentMediaShareContentInCurrentMediaShare(mediaShare));

                int diffOriginalMediaShareContentSize = diffContentsOriginalMediaShare.getMediaContentsListSize();
                int diffModifiedMediaShareContentSize = diffContentsModifiedMediaShare.getMediaContentsListSize();
                if (diffOriginalMediaShareContentSize == 0 && diffModifiedMediaShareContentSize == 0) {

                    finish();
                }

                if (modifiedMediaShare.getMediaContentsListSize() != 0) {
                    modifiedMediaShare.setCoverImageUUID(modifiedMediaShare.getFirstMediaDigestInMediaContentsList());
                } else {
                    modifiedMediaShare.setCoverImageUUID("");
                }

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                FNAS.editPhotoInRemoteMediaShare(mContext, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            loadMedia(new ArrayList<>(LocalCache.mediaKeysInCreateAlbum), true, true);

            fillMediaShareContents(LocalCache.mediaKeysInCreateAlbum);
            LocalCache.mediaKeysInCreateAlbum.clear();

        }
    }

    class EditPhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.photo_item)
        NetworkImageView mPhotoItem;
        @BindView(R.id.del_photo)
        ImageView mDelPhoto;
        @BindView(R.id.del_photo_layout)
        FrameLayout mDelPhotoLayout;

        private Media mMap;

        EditPhotoViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void refreshView(final int position) {
            mMap = mPhotoList.get(position);

            String imageUrl = mMap.getImageThumbUrl(mContext);
            mImageLoader.setShouldCache(!mMap.isLocal());

            if (mMap.isLocal())
                mPhotoItem.setOrientationNumber(mMap.getOrientationNumber());

            setMainPicScreenHeight(mPhotoItem, mMap);

            mPhotoItem.setTag(imageUrl);
            mPhotoItem.setDefaultImageResId(R.drawable.placeholder_photo);
            mPhotoItem.setImageUrl(imageUrl, mImageLoader);

            mDelPhotoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    modifiedMediaShare.removeMediaShareContent(getAdapterPosition());

                    Log.i(TAG, "onClick: photo uuid" + mPhotoList.get(getAdapterPosition()).getUuid());

                    mPhotoList.remove(getAdapterPosition());
                    mAdapter.notifyItemRemoved(getAdapterPosition());

                }
            });
        }

    }

    private void setMainPicScreenHeight(NetworkImageView mainPic, Media media) {

        if (media.isLocal())
            return;

        int mediaWidth = Integer.parseInt(media.getWidth());
        int mediaHeight = Integer.parseInt(media.getHeight());
        int actualWidth;
        int actualHeight;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mainPic.getLayoutParams();

        actualWidth = Util.calcScreenWidth(EditPhotoActivity.this) / SPAN_COUNT;
        actualHeight = mediaHeight * actualWidth / mediaWidth;

        layoutParams.height = actualHeight;

        mainPic.setLayoutParams(layoutParams);
    }

    class EditPhotoAdapter extends RecyclerView.Adapter<EditPhotoViewHolder> {
        @Override
        public int getItemCount() {
            return mPhotoList == null ? 0 : mPhotoList.size();
        }

        @Override
        public EditPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.edit_photo_item, parent, false);

            return new EditPhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EditPhotoViewHolder holder, int position) {

            holder.refreshView(position);
        }

    }

}
