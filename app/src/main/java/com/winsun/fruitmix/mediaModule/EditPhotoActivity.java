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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private int mSpanCount = 3;
    private Context mContext;
    private EditPhotoAdapter mAdapter;

    private MediaShare mediaShare;
    private MediaShare modifiedMediaShare;
    private List<Media> mPhotoList;

    private ImageLoader mImageLoader;

    private ProgressDialog mDialog;

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

        mediaShare = getIntent().getParcelableExtra(Util.KEY_MEDIASHARE);
        modifiedMediaShare = mediaShare.cloneMyself();

        GridLayoutManager mManager = new GridLayoutManager(mContext, mSpanCount);
        mEditPhotoRecyclerView.setLayoutManager(mManager);
        mEditPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new EditPhotoAdapter();
        mEditPhotoRecyclerView.setAdapter(mAdapter);

        mPhotoList = new ArrayList<>();
        fillPhotoList(mediaShare.getMediaKeyInMediaShareContents());
        mAdapter.notifyDataSetChanged();

    }

    private void initImageLoader() {
        mImageLoader = new ImageLoader(RequestQueueInstance.getInstance(this).getRequestQueue(), ImageLruCache.instance());
        Map<String, String> headers = new HashMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        Log.i(TAG, FNAS.JWT);
        mImageLoader.setHeaders(headers);
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
                    getIntent().putExtra(Util.KEY_MEDIASHARE, modifiedMediaShare);
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

    private void fillPhotoList(List<String> selectedImageKeys) {

        Media picItem;
        Media picItemRaw;
        for (String aStArr : selectedImageKeys) {

            picItemRaw = LocalCache.findMediaInLocalMediaMap(aStArr);

            if (picItemRaw == null) {

                picItemRaw = LocalCache.RemoteMediaMapKeyIsUUID.get(aStArr);

                if (picItemRaw == null) {
                    picItem = new Media();
                    picItem.setUuid(aStArr);
                    picItem.setLocal(false);
                } else {

                    picItem = picItemRaw.cloneSelf();
                    picItem.setLocal(false);
                }


            } else {

                picItem = picItemRaw.cloneSelf();
                picItem.setLocal(true);

            }

            picItem.setSelected(false);

            mPhotoList.add(picItem);


        }

    }

    private void fillMediaShareContents(List<String> selectedImageKeys) {
        for (String imageKey : selectedImageKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setKey(imageKey);
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

                intent.putStringArrayListExtra(Util.KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST, (ArrayList<String>) mediaShare.getMediaKeyInMediaShareContents());
                intent.putExtra(Util.EDIT_PHOTO, true);
                startActivityForResult(intent, Util.KEY_CHOOSE_PHOTO_REQUEST_CODE);
                break;
            case R.id.finish:

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

                mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

                if (modifiedMediaShare.getMediaContentsListSize() != 0) {
                    modifiedMediaShare.setCoverImageKey(modifiedMediaShare.getFirstMediaDigestInMediaContentsList());
                } else {
                    modifiedMediaShare.setCoverImageKey("");
                }

                if (Util.getNetworkState(mContext)) {
                    FNAS.editPhotoInRemoteMediaShare(mContext, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);
                } else {
                    FNAS.editPhotoInLocalMediaShare(mContext, diffContentsOriginalMediaShare, diffContentsModifiedMediaShare, modifiedMediaShare);
                }

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CHOOSE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            fillPhotoList(LocalCache.mediaKeysInCreateAlbum);
            fillMediaShareContents(LocalCache.mediaKeysInCreateAlbum);

            LocalCache.mediaKeysInCreateAlbum.clear();

            mAdapter.notifyDataSetChanged();
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
